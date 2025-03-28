package com.darc.downbit.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.darc.downbit.common.constant.RoleType;
import com.darc.downbit.common.dto.RestResp;
import com.darc.downbit.common.dto.rep.LoginDto;
import com.darc.downbit.common.dto.rep.PhoneLoginDto;
import com.darc.downbit.common.dto.rep.RegisterDto;
import com.darc.downbit.common.dto.resp.PhoneLoginResp;
import com.darc.downbit.common.exception.BadRequestException;
import com.darc.downbit.common.exception.DatabaseException;
import com.darc.downbit.config.auth.AuthUser;
import com.darc.downbit.config.http.SmsApi;
import com.darc.downbit.dao.entity.Favorite;
import com.darc.downbit.dao.entity.User;
import com.darc.downbit.dao.mapper.FavoriteMapper;
import com.darc.downbit.dao.mapper.UserMapper;
import com.darc.downbit.dao.mapper.UserRoleMapper;
import com.darc.downbit.service.AuthService;
import com.darc.downbit.util.CommonUtil;
import com.darc.downbit.util.JwtUtil;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
    SmsApi smsApi;
    @Resource
    PasswordEncoder passwordEncoder;


    @Value("${downbit.jwt.expiration}")
    private long expiration;

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.password}")
    private String password;

    /**
     * 使用用户名密码登录
     *
     * @param loginDto 登录信息
     * @return RestResp<String>
     */
    @Override
    public String loginByUsername(String captchaKey, LoginDto loginDto) {
        log.info("进入了service层");
        log.info("mysql的url:{}", url);
        log.info("mysql的password:{}", password);
        String username = loginDto.getUsername();
        if (redisTemplate.hasKey("loginUser:" + username)) {
            User user = redisTemplate.opsForValue().get("loginUser:" + username);
            if (user == null) {
                throw new DatabaseException("redis缓存的用户登录信息异常");
            }
            if (request.getHeader("User-Agent").equals(user.getDevice()) && request.getRemoteAddr().equals(user.getIp())) {
                throw new BadRequestException("用户:" + username + "已经登录");
            }
        }

        String password = loginDto.getPassword();
        String captcha = loginDto.getCaptcha();
        // 如果loginKey不为空,则说明是注册后的登录,此时不需要验证码

        validateCaptcha(captchaKey, captcha);
        // 验证码验证后,无论验证是否成功,删除redis中的验证码
        stringRedisTemplate.delete(captchaKey);

        String newToken = authenticateUser(username, password, normalAuthenticationManager);

        if (newToken == null) {
            throw new BadRequestException("登录失败,用户名或密码错误");
        }
        return newToken;
    }

    @Override
    @Transactional
    public PhoneLoginResp loginByPhone(PhoneLoginDto loginDto, String captchaKey) {
        // 1. 验证手机号格式
        String phone = loginDto.getPhone();
        if (!CommonUtil.isPhone(phone)) {
            throw new BadRequestException("手机号格式不正确");
        }

        // 2. 验证图形验证码
        validateCaptcha(captchaKey, loginDto.getCaptcha());
        // 验证码验证后删除
        stringRedisTemplate.delete(captchaKey);

        // 3. 验证短信验证码
        String smsCode = stringRedisTemplate.opsForValue().get("smsCode:" + phone);
        if (smsCode == null || !smsCode.equals(loginDto.getCode())) {
            throw new BadRequestException("短信验证码错误或已过期");
        }
        // 验证成功后删除短信验证码
        stringRedisTemplate.delete("smsCode:" + phone);

        // 4. 检查用户是否已注册
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>().eq("phone", phone);
        User user = userMapper.selectOne(queryWrapper);

        // 5. 如果用户未注册，先注册
        if (user == null) {
            user = new User();
            // 使用手机号作为用户名
            user.setUsername(phone);
            user.setNickname("用户" + phone.substring(phone.length() - 4));
            user.setPhone(phone);
            // 生成随机密码
            String randomPassword = UUID.randomUUID().toString().substring(0, 8);
            user.setPassword(passwordEncoder.encode(randomPassword));
            user.setIntro("这是一个简介....");
            user.setIp(request.getRemoteAddr());
            user.setDevice(request.getHeader("User-Agent"));

            // 插入用户记录
            if (userMapper.insert(user) < 1) {
                throw new DatabaseException("用户注册失败");
            }

            // 插入用户角色关联
            if (userRoleMapper.insertByUsernameAndRole(user.getUsername(), RoleType.NORMAL) < 1) {
                throw new DatabaseException("用户角色关联失败");
            }

            // 创建默认收藏夹
            Favorite favorite = new Favorite();
            favorite.setFavoriteName("默认收藏夹");
            favorite.setUserId(user.getUserId());
            if (favoriteMapper.insert(favorite) < 1) {
                throw new DatabaseException("默认收藏夹添加失败");
            }
        }

        // 6. 更新用户登录信息
        user.setIp(request.getRemoteAddr());
        user.setDevice(request.getHeader("User-Agent"));
        userMapper.update(user, new QueryWrapper<User>().eq("user_id", user.getUserId()));

        // 7. 生成新的 UUID 并创建 JWT
        String newUuid = UUID.randomUUID().toString();
        user.setUuid(newUuid);

        // 8. 将用户信息保存到 Redis
        String userKey = "loginUser:" + user.getUsername();
        redisTemplate.opsForValue().set(userKey, user, expiration, TimeUnit.MILLISECONDS);

        // 9. 生成并返回 JWT
        String token = JwtUtil.createToken(newUuid, user.getUsername(), expiration);
        return new PhoneLoginResp(token, user.getUsername());
    }

    @Override
    public BufferedImage getCaptcha(String key) {
        String captchaCode = defaultKaptcha.createText();
        stringRedisTemplate.opsForValue().set(key, captchaCode, 5, TimeUnit.MINUTES);
        return defaultKaptcha.createImage(captchaCode);
    }


    @Override
    @Transactional
    public RestResp<String> register(String captchaKey, RegisterDto registerDto) {
        String registerLock = "register_limit:" + registerDto.getUuid();
        try {
            Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(registerLock, "1", 10, TimeUnit.SECONDS);
            if (Boolean.FALSE.equals(result)) {
                return RestResp.badRequest("注册过于频繁,请稍后再试");
            }
            String username = registerDto.getUsername();
            // 查询数据库中是否已经有此用户,如果有此用户则不允许注册
            Wrapper<User> selectByUsername = new QueryWrapper<User>().eq("username", username);
            if (userMapper.selectOne(selectByUsername) != null) {
                response.setStatus(HttpStatus.BAD_REQUEST.value());
                return RestResp.badRequest("用户名:" + username + "已经被注册了");
            }

            validateCaptcha(captchaKey, registerDto.getCaptcha());
            // 验证码验证成功后,删除redis中的验证码
            stringRedisTemplate.delete(captchaKey);


            User user = new User();
            user.setUsername(username);
            user.setNickname(registerDto.getNickname());
            user.setIntro("这是一个简介....");
            user.setPhone(registerDto.getPhone());
            user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
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
            response.setStatus(HttpStatus.OK.value());
            return RestResp.ok("ok");
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

    @Override
    public synchronized void sendSmsCode(String phone) {
        if (phone == null) {
            throw new BadRequestException("没有手机号");
        }
        //验证手机号是否合法
        if (!CommonUtil.isPhone(phone)) {
            throw new BadRequestException("手机号不合法");
        }
        //生成一个随机的验证码,拼接在params
        String code = CommonUtil.generateRandomCode(6);
        String params = "**code**:" + code + ",**minute**:5";
        smsApi.sendSms(phone, "908e94ccf08b4476ba6c876d13f084ad", "2e65b1bb3d054466b82f0c9d125465e2", params).block();
        stringRedisTemplate.opsForValue().set("smsCode:" + phone, code, 5, TimeUnit.MINUTES);
    }


    /**
     * 验证是否与redis中存储的验证码一致
     *
     * @param captchaKey 验证码key
     * @param captcha    验证码
     * @return RestResp<String>
     */
    private void validateCaptcha(String captchaKey, String captcha) {
        if (Boolean.FALSE.equals(stringRedisTemplate.hasKey(captchaKey))) {
            throw new BadRequestException("验证码已过期");
        }
        String recordedCaptcha = stringRedisTemplate.opsForValue().get(captchaKey);
        if (recordedCaptcha == null || !recordedCaptcha.equals(captcha)) {
            throw new BadRequestException("验证码错误");
        }
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
        log.info("进入了验证用户方法");
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication = authenticationManager.authenticate(authRequest);

        if (authentication.isAuthenticated()) {
            AuthUser authUser = (AuthUser) authentication.getPrincipal();
            User user = authUser.getUser();
            user.setIp(request.getRemoteAddr());
            user.setDevice(request.getHeader("User-Agent"));
            userMapper.update(user, new QueryWrapper<User>().eq("user_id", user.getUserId()));
            String newUuid = UUID.randomUUID().toString();
            user.setUuid(newUuid);
            String userKey = "loginUser:" + user.getUsername();
            redisTemplate.opsForValue().set(userKey, user, expiration, TimeUnit.MILLISECONDS);
            return JwtUtil.createToken(newUuid, username, expiration);
        }
        return null;
    }

}
