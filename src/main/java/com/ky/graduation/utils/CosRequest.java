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
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
        log.info("fileName---{}",key);
        // 上传COS存储
        cosClient.putObject(cosConfig.getBucketName(),key,imgFile);
        cosClient.shutdown();
        // 返回生成的文件名（包括后缀）
        return key;
    }

    public boolean deleteObject(String key){
        COSClient cosClient = initCosClient();
        cosClient.deleteObject(cosConfig.getBucketName(),key);
        cosClient.shutdown();
        return true;
    }

}
