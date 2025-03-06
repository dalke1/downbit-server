package com.darc.downbit.util;

import com.darc.downbit.common.exception.EmptyFileNameException;
import com.darc.downbit.dao.mapper.ImgMapper;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.Headers;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.GeneratePresignedUrlRequest;
import com.qcloud.cos.model.ResponseHeaderOverrides;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/20-22:16:16
 * @description
 */
@Component
@Slf4j
public class CosUtil {

    @Value("${downbit.cos.bucket}")
    private String bucket;

    @Resource
    COSClient cosClient;

    @Resource
    RedisTemplate<String, String> redisTemplate;

    @Resource
    ImgMapper imgMapper;

    // 生成签名的过期时间


    public String getAvatarUrl(String username) {
        if (redisTemplate.hasKey("avatarUrl:" + username)) {
            return redisTemplate.opsForValue().get("avatarUrl:" + username);
        }
        String fileName = imgMapper.getAvatarByUsername(username);
        if (fileName == null) {
//            throw new EmptyFileNameException("头像文件名为空");
            return redisTemplate.opsForValue().get("avatarUrl:root");
        }
        String signature = "avatar:" + UUID.randomUUID();
        String key = "avatar/" + username + "/" + fileName;

        String url = getUrl(key, HttpMethodName.GET, signature, "image/jpeg");
        redisTemplate.opsForValue().set("avatarUrl:" + username, url, 1, TimeUnit.DAYS);
        return url;
    }

    public String getCoverUrl(String username, String fileName) {
        if (fileName == null) {
            throw new EmptyFileNameException("封面文件名为空");
        }
        if (redisTemplate.hasKey("coverUrl:" + fileName)) {
            return redisTemplate.opsForValue().get("coverUrl:" + fileName);
        }
        String signature = "cover:" + UUID.randomUUID();
        String key = "cover/" + username + "/" + fileName;
        String url = getUrl(key, HttpMethodName.GET, signature, "image/jpeg");
        redisTemplate.opsForValue().set("coverUrl:" + fileName, url, 1, TimeUnit.DAYS);
        return url;
    }

    public String getVideoUrl(String username, String fileName) {
        if (fileName == null) {
            throw new EmptyFileNameException("视频文件名为空");
        }
        if (redisTemplate.hasKey("videoUrl:" + fileName)) {
            return redisTemplate.opsForValue().get("videoUrl:" + fileName);
        }
        String signature = "video:" + UUID.randomUUID();
        String key = "video/" + username + "/" + fileName;
        String url = getUrl(key, HttpMethodName.GET, signature, "video/mp4");
        redisTemplate.opsForValue().set("videoUrl:" + fileName, url, 1, TimeUnit.DAYS);
        return url;
    }

    public String getUploadAvatarUrl(String username, String filename) {
        String key = "avatar/" + username + "/" + filename;
        return getUploadUrl(key);
    }

    public String getUploadCoverUrl(String username, String filename) {
        String key = "cover/" + username + "/" + filename;
        return getUploadUrl(key);
    }

    public String getUploadVideoUrl(String username, String filename) {
        String key = "video/" + username + "/" + filename;
        return getUploadUrl(key);
    }

    public String getUrl(String key, HttpMethodName method, String signature, String contentType) {
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(bucket, key, method);
        if (method == HttpMethodName.GET) {
            ResponseHeaderOverrides responseHeaders = new ResponseHeaderOverrides();
            responseHeaders.setContentType(contentType);
            responseHeaders.setContentLanguage("zh-CN");
            // 设置响应头为需要缓存
            responseHeaders.setCacheControl("max-age=" + TimeUnit.DAYS.toMillis(1) + TimeUnit.MINUTES.toMillis(10));
            request.setResponseHeaders(responseHeaders);
        }
        Date expiration = new Date(System.currentTimeMillis() + TimeUnit.DAYS.toMillis(1) + TimeUnit.MINUTES.toMillis(10));
        request.setExpiration(expiration);
        // 填写本次请求的头部，需与实际请求相同，能够防止用户篡改此签名的 HTTP 请求的头部
        String host = cosClient.getClientConfig().getEndpointBuilder().buildGeneralApiEndpoint(bucket);
        request.putCustomRequestHeader(Headers.HOST, host);
        // 填写本次请求的参数，需与实际请求相同，能够防止用户篡改此签名的 HTTP 请求的参数
        request.addRequestParameter("signature", signature);
        return cosClient.generatePresignedUrl(request).toString();
    }


    public String getUploadUrl(String key) {
        // 填写本次请求的 header。Host 头部会自动补全，只需填入其他头部
        Map<String, String> headers = new HashMap<>();
        // 填写本次请求的 params
        Map<String, String> params = new HashMap<>();
        Date uploadExpiration = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10));
        URL url = cosClient.generatePresignedUrl(bucket, key, uploadExpiration, HttpMethodName.PUT, headers, params);
        return url.toString();
    }
}
