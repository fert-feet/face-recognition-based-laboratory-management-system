package com.ky.graduation.utils;

import com.ky.graduation.vo.CosConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.region.Region;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

/**
 * @author: Ky2Fe
 * @program: graduation
 * @description: 封装云存储
 **/

@Component
public class CosRequest {

    @Resource
    private CosConfig cosConfig;

    @Bean
    public COSClient initCosClient(){
        COSCredentials cred = new BasicCOSCredentials(cosConfig.getSecretId(), cosConfig.getSecretKey());
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setRegion(new Region(cosConfig.getRegionName()));
        return new COSClient(cred,clientConfig);
    }
}
