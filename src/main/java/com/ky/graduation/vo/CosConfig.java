package com.ky.graduation.vo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author: Ky2Fe
 * @program: graduation
 * @description: 云存储配置类
 **/

@Data
@Component
@ConfigurationProperties(prefix = "tencent.cos.file")
public class CosConfig {
    String secretId;

    String secretKey;

    String bucketName;

    String regionName;

    String cosHost;
}
