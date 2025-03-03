package com.darc.downbit.config.cos;


import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import lombok.Data;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/12/8-23:09:23
 * @description
 */
@Data
@SpringBootConfiguration
@ConfigurationProperties(prefix = "downbit.cos")
public class CosConfig {
    private String bucket;
    private String region;
    private String secretId;
    private String secretKey;

    @Bean
    public COSClient cosClient() {
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setRegion(new Region(region));
        return new COSClient(cred, clientConfig);
    }
}
