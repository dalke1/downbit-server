package com.darc.downbit.util;

import com.darc.downbit.common.exception.EmptyFileNameException;
import com.darc.downbit.dao.mapper.ImgMapper;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.MultiObjectDeleteException;
import com.qcloud.cos.http.HttpMethodName;
import com.qcloud.cos.model.DeleteObjectsRequest;
import com.qcloud.cos.model.DeleteObjectsResult;
import com.tencent.cloud.cos.util.MD5;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Value("${downbit.cos.cdn}")
    private String cdn;

    @Value("${downbit.cos.cdn-key}")
    private String cdnKey;

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
            if (redisTemplate.hasKey("avatarUrl:common")) {
                return redisTemplate.opsForValue().get("avatarUrl:common");
            } else {
                String url = getUrl("/common/dalke.jpg");
                redisTemplate.opsForValue().set("avatarUrl:common", url, 1, TimeUnit.HOURS);
                return url;
            }
        }
        String uri = "/avatar/" + username + "/" + fileName;
        String url = getUrl(uri);
        redisTemplate.opsForValue().set("avatarUrl:" + username, url, 1, TimeUnit.HOURS);
        return url;
    }

    public String getCoverUrl(String username, String fileName) {
        if (fileName == null) {
            throw new EmptyFileNameException("封面文件名为空");
        }
        if (redisTemplate.hasKey("coverUrl:" + fileName)) {
            return redisTemplate.opsForValue().get("coverUrl:" + fileName);
        }
        String uri = "/cover/" + username + "/" + fileName;
        String url = getUrl(uri);
        redisTemplate.opsForValue().set("coverUrl:" + fileName, url, 1, TimeUnit.HOURS);
        return url;
    }

    public String getVideoUrl(String username, String fileName) {
        if (fileName == null) {
            throw new EmptyFileNameException("视频文件名为空");
        }
        if (redisTemplate.hasKey("videoUrl:" + fileName)) {
            return redisTemplate.opsForValue().get("videoUrl:" + fileName);
        }
        String uri = "/video/" + username + "/" + fileName;
        String url = getUrl(uri);
        redisTemplate.opsForValue().set("videoUrl:" + fileName, url, 1, TimeUnit.HOURS);
        return url;
    }

    public String getUploadAvatarUrl(String username, String filename) {
        return getUploadUrl("avatar/" + username + "/" + filename);
    }

    public String getUploadCoverUrl(String username, String filename) {
        return getUploadUrl("cover/" + username + "/" + filename);
    }

    public String getUploadVideoUrl(String username, String filename) {
        return getUploadUrl("video/" + username + "/" + filename);
    }

    public String getUrl(String uri) {
        long timestamp = System.currentTimeMillis();
        String signature = MD5.stringToMD5(cdnKey + uri + timestamp);
        return cdn + uri + "?sign=" + signature + "&t=" + timestamp;
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

    public void deleteFiles(List<String> fileNameList) {
        DeleteObjectsRequest deleteObjectsRequest = new DeleteObjectsRequest(bucket);
        List<DeleteObjectsRequest.KeyVersion> keyList = fileNameList.stream()
                .map(DeleteObjectsRequest.KeyVersion::new)
                .toList();
        deleteObjectsRequest.setKeys(keyList);
        try {
            DeleteObjectsResult deleteObjectsResult = cosClient.deleteObjects(deleteObjectsRequest);
            List<DeleteObjectsResult.DeletedObject> deleteObjectResultArray = deleteObjectsResult.getDeletedObjects();
        } catch (MultiObjectDeleteException mde) {
            // 如果部分删除成功部分失败, 返回 MultiObjectDeleteException
            List<DeleteObjectsResult.DeletedObject> deleteObjects = mde.getDeletedObjects();
            List<MultiObjectDeleteException.DeleteError> deleteErrors = mde.getErrors();
            log.info(mde.getMessage());
        } catch (CosClientException e) {
            log.error(e.getMessage());
        }
    }
}
