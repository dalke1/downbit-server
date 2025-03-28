package com.darc.downbit.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.darc.downbit.common.dto.resp.ImgRespDto;
import com.darc.downbit.common.dto.resp.UserInfo;
import com.darc.downbit.common.exception.NoPermissionException;
import com.darc.downbit.config.auth.AuthConfig;
import com.darc.downbit.config.auth.AuthUser;
import com.darc.downbit.dao.entity.File;
import com.darc.downbit.dao.entity.User;
import com.darc.downbit.dao.entity.Video;
import com.darc.downbit.dao.mapper.FileMapper;
import com.darc.downbit.dao.mapper.UserMapper;
import com.darc.downbit.dao.mapper.VideoMapper;
import com.darc.downbit.service.UserService;
import com.darc.downbit.util.CommonUtil;
import com.darc.downbit.util.CosUtil;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author darc
 * @description 针对表【user】的数据库操作Service实现
 * @createDate 2024-07-22 05:13:45
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private CosUtil cosUtil;

    @Resource
    private FileMapper fileMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private VideoMapper videoMapper;

    @Override
    public List<User> listAll() {
        return userMapper.selectList(null);
    }

    @Override
    public boolean addUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userMapper.insert(user) > 0;
    }

    @Override
    public ImgRespDto getAvatar() {
        AuthUser authUser = (AuthUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = authUser.getUser();
        String username = user.getUsername();
        return new ImgRespDto(cosUtil.getAvatarUrl(username));
    }

    @Override
    public UserInfo getUserInfo() {
        AuthUser authUser = AuthConfig.getAuthUser();
        if (authUser == null) {
            throw new NoPermissionException("用户未登录");
        }
        User user = authUser.getUser();
        String username = user.getUsername();
        String avatarUrl = cosUtil.getAvatarUrl(username);

        return new UserInfo(username, user.getNickname(), user.getIntro(), avatarUrl);
    }

    @Override
    public String getUploadAvatarUrl(String fileName) {
        AuthUser authUser = AuthConfig.getAuthUser();
        if (authUser == null) {
            throw new NoPermissionException("用户未登录");
        }
        User user = authUser.getUser();
        String username = user.getUsername();
        return cosUtil.getUploadAvatarUrl(username, fileName);
    }

    @Override
    public void updateUserInfo(String nickname, String intro, String fileName) {
        AuthUser authUser = AuthConfig.getAuthUser();
        if (authUser == null) {
            throw new NoPermissionException("用户未登录");
        }
        User user = authUser.getUser();
        if (nickname != null) {
            user.setNickname(nickname);
        }
        if (intro != null) {
            user.setIntro(intro);
        }
        if (fileName != null) {
            Integer avatarId = user.getAvatarId();
            if (avatarId == null) {
                File file = new File();
                file.setFileType("avatar");
                file.setFileName(fileName);
                file.setCreatedTime(new Date());
                file.setUpdatedTime(new Date());
                fileMapper.insert(file);
                user.setAvatarId(file.getFileId());
            } else {
                File file = fileMapper.selectOne(new QueryWrapper<File>().eq("file_id", avatarId));
                String oldFileName = file.getFileName();
                cosUtil.deleteFiles(List.of("avatar/" + user.getUsername() + "/" + oldFileName));
                redisTemplate.delete("avatarUrl:" + user.getUsername());
                file.setFileName(fileName);
                file.setUpdatedTime(new Date());
                fileMapper.updateById(file);
            }
        }
        userMapper.updateById(user);
        redisTemplate.opsForValue().set("loginUser:" + user.getUsername(), user);
    }

    @Override
    public String likeTotal(String username) {
        User user = userMapper.selectOne(new QueryWrapper<User>().eq("username", username));
        List<Video> videoList = videoMapper.selectList(new QueryWrapper<Video>().eq("user_id", user.getUserId()));
        List<Integer> countList = new ArrayList<>();
        redisTemplate.executePipelined((RedisCallback<?>) connection -> {
            for (Video video : videoList) {
                Integer count = (Integer) redisTemplate.opsForHash().get("likeCount", video.getVideoId().toString());
                if (count != null) {
                    countList.add(count);
                }
            }
            return null;
        });
        long sum = countList.stream()
                .mapToInt(Integer::intValue)
                .sum();

        return CommonUtil.formatNumberToString(sum);
    }
}




