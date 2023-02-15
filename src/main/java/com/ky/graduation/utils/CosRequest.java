package com.ky.graduation.utils;

import com.ky.graduation.vo.CosConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * @author: Ky2Fe
 * @program: graduation
 * @description: 封装云存储
 **/

@Component
public class CosRequest {

    @Resource
    private CosConfig cosConfig;

    /**
     * 上传文件初始化
     * @return
     */
    public COSClient initCosClient(){
        COSCredentials cred = new BasicCOSCredentials(cosConfig.getSecretId() + "R", cosConfig.getSecretKey() + "y");
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setRegion(new Region(cosConfig.getRegionName()));
        return new COSClient(cred,clientConfig);
    }

    public String putObject(MultipartFile imgMultiFile) throws IOException {
        COSClient cosClient = initCosClient();
        // 文件类型转换，spring只能接收MultipartFile类型图片
        File imgFile = FileUtils.multipartFileToFile(imgMultiFile);
        String key = imgFile.getName();
        PutObjectRequest putRequest = new PutObjectRequest(cosConfig.getBucketName(), key, imgFile);
        PutObjectResult putResult = cosClient.putObject(putRequest);
        // 释放资源
        cosClient.shutdown();
        // 拼接人脸链接
        String faceUrl = cosConfig.getCosHost() + "/" + key;
        return faceUrl;
    }

}
