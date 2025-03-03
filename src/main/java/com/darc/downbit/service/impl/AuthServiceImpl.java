package com.darc.downbit.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.darc.downbit.common.constant.RoleType;
import com.darc.downbit.common.dto.RestResp;
import com.darc.downbit.common.dto.rep.LoginDto;
import com.darc.downbit.common.dto.rep.PhoneLoginDto;
import com.darc.downbit.common.exception.DatabaseException;
import com.darc.downbit.config.auth.AuthUser;
import com.darc.downbit.dao.entity.Favorite;
import com.darc.downbit.dao.entity.User;
import com.darc.downbit.dao.mapper.FavoriteMapper;
import com.darc.downbit.dao.mapper.UserMapper;
import com.darc.downbit.dao.mapper.UserRoleMapper;
import com.darc.downbit.service.AuthService;
import com.darc.downbit.util.JwtUtil;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.image.BufferedImage;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/11/20-4:08:49
 * @description
 */
@Service
public class AuthServiceImpl implements AuthService {
    @Resource
    HttpServletRequest request;
    @Resource
    HttpServletResponse response;
    @Resource
    AuthenticationManager normalAuthenticationManager;
    @Resource
    RedisTemplate<String, User> redisTemplate;
    @Resource
    StringRedisTemplate stringRedisTemplate;
    @Resource
    DefaultKaptcha defaultKaptcha;
    @Resource
    UserMapper userMapper;
    @Resource
    UserRoleMapper userRoleMapper;
    @Resource
    FavoriteMapper favoriteMapper;
    @Resource
    PasswordEncoder passwordEncoder;


    @Value("${downbit.jwt.expiration}")
    private long expiration;

    /**
     * 使用用户名密码登录
     *
     * @param loginDto 登录信息
     * @return RestResp<String>
     */
    @Override
    public RestResp<String> loginByUsername(String captchaKey, String loginKey, LoginDto loginDto) {
        String username = loginDto.getUsername();
        if (redisTemplate.hasKey("loginUser:" + username)) {
            User user = redisTemplate.opsForValue().get("loginUser:" + username);
            if (user == null) {
                throw new DatabaseException("redis缓存的用户登录信息异常");
            }
            if (request.getHeader("User-Agent").equals(user.getDevice()) && request.getRemoteAddr().equals(user.getIp())) {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                return RestResp.badRequest("用户:" + username + "已经登录");
            }
        }

        String password = loginDto.getPassword();
        String captcha = loginDto.getCaptcha();

        // 如果loginKey不为空,则说明是注册后的登录,此时不需要验证码
        if (loginKey == null || Boolean.FALSE.equals(stringRedisTemplate.hasKey(loginKey))) {
            RestResp<String> failResult = validateCaptcha(captchaKey, captcha);
            // 验证码验证后,无论验证是否成功,删除redis中的验证码
            stringRedisTemplate.delete(captchaKey);
            if (failResult != null) {
                return failResult;
            }
        }

        String newToken = authenticateUser(username, password, normalAuthenticationManager);

        if (newToken == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return RestResp.unauthorized("登录失败,用户名或密码错误");
        }
        return RestResp.ok(newToken);
    }

    @Override
    public RestResp<String> loginByPhone(PhoneLoginDto loginDto) {
        return null;
    }

    @Override
    public BufferedImage getCaptcha(String key) {
        String captchaCode = defaultKaptcha.createText();
        stringRedisTemplate.opsForValue().set(key, captchaCode, 5, TimeUnit.MINUTES);
        return defaultKaptcha.createImage(captchaCode);
    }


    @Override
    @Transactional
    public RestResp<String> register(String captchaKey, LoginDto loginDto) {
        String registerLock = "register_limit:" + loginDto.getUuid();
        try {
            Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(registerLock, "1", 10, TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(result)) {
                return RestResp.badRequest("注册过于频繁,请稍后再试");
            }

            String username = loginDto.getUsername();
            // 查询数据库中是否已经有此用户,如果有此用户则不允许注册
            Wrapper<User> selectByUsername = new QueryWrapper<User>().eq("username", username);
            if (userMapper.selectOne(selectByUsername) != null) {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                return RestResp.badRequest("用户名:" + username + "已经被注册了");
            }

            RestResp<String> failResult = validateCaptcha(captchaKey, loginDto.getCaptcha());
            if (failResult != null) {
                return failResult;
            }
            // 验证码验证成功后,删除redis中的验证码
            stringRedisTemplate.delete(captchaKey);


            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(loginDto.getPassword()));
            user.setIp(request.getRemoteAddr());
            user.setDevice(request.getHeader("User-Agent"));
            // 插入用户记录
            if (userMapper.insert(user) < 1) {
                throw new DatabaseException("用户注册失败");
            }
            // 默认为普通用户,在数据库中的role表可以查看到所有的角色,字段为role_name
            user.setRole("NORMAL_USER");
            // 插入用户角色关联
            if (userRoleMapper.insertByUsernameAndRole(user.getUsername(), RoleType.NORMAL) < 1) {
                throw new DatabaseException("用户角色关联失败");
            }
            Favorite favorite = new Favorite();
            favorite.setFavoriteName("默认收藏夹");
            favorite.setUserId(user.getUserId());
            // 添加默认收藏夹
            if (favoriteMapper.insert(favorite) < 1) {
                throw new DatabaseException("默认收藏夹添加失败");
            }
            String loginKey = UUID.randomUUID().toString();
            stringRedisTemplate.opsForValue().set(loginKey, user.getUsername(), 10, TimeUnit.SECONDS);
            response.setStatus(HttpStatus.OK.value());
            return RestResp.ok(loginKey);
        } finally {
            redisTemplate.delete(registerLock);
        }
    }

    @Override
    public RestResp<String> logout() {
        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (authUser == null || Boolean.FALSE.equals(redisTemplate.hasKey("loginUser:" + authUser.getUser().getUsername()))) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return RestResp.unauthorized("未登录");
        }
        redisTemplate.delete("loginUser:" + authUser.getUser().getUsername());
        return RestResp.ok("注销成功");
    }

    @Override
    public RestResp<String> refreshToken() {
        return null;
    }


    /**
     * 验证是否与redis中存储的验证码一致
     *
     * @param captchaKey 验证码key
     * @param captcha    验证码
     * @return RestResp<String>
     */
    private RestResp<String> validateCaptcha(String captchaKey, String captcha) {
        if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(captchaKey))) {
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return RestResp.badRequest("验证码已过期");
        }
        String recordedCaptcha = stringRedisTemplate.opsForValue().get(captchaKey);
        if (recordedCaptcha == null || !recordedCaptcha.equals(captcha)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return RestResp.unauthorized("验证码错误");
        }

        return null;
    }

    /**
     * 使用用户名密码登录或者手机号验证码登录并生成新的token
     *
     * @param username              登录的用户名,也可以是手机号
     * @param password              登录的密码,也可以是验证码
     * @param authenticationManager 自定义的认证管理器在com.darc.downbit.config.SecurityConfig中配置,
     *                              传入NormalAuthenticationManager用于验证用户名和密码,
     *                              传入PhoneAuthenticationManager用于验证手机号和验证码
     * @return String
     */
    private String authenticateUser(String username, String password, AuthenticationManager authenticationManager) {
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(authRequest);
        if (authentication.isAuthenticated()) {
            AuthUser authUser = (AuthUser) authentication.getPrincipal();
            User user = authUser.getUser();
            user.setIp(request.getRemoteAddr());
            user.setDevice(request.getHeader("User-Agent"));
            userMapper.update(user, new QueryWrapper<User>().eq("ip", user.getIp()).eq("device", user.getDevice()));
            String newUuid = UUID.randomUUID().toString();
            user.setUuid(newUuid);
            String userKey = "loginUser:" + user.getUsername();
            redisTemplate.opsForValue().set(userKey, user, expiration, TimeUnit.MILLISECONDS);
            return JwtUtil.createToken(newUuid, username, expiration);
        }
        return null;
    }

}
