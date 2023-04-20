package com.ky.graduation.utils;

import cn.hutool.core.io.file.FileNameUtil;
import com.ky.graduation.vo.CosConfig;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.qcloud.cos.region.Region;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author: Ky2Fe
 * @program: graduation
 * @description: 封装云存储
 **/

@Component
@Slf4j
public class CosRequest {

    public static final String PIC_RULE = "imageMogr2/scrop/720x1280/size-limit/1m!";
    @Resource
    private CosConfig cosConfig;

    /**
     * 将图片前缀进行替换
     *
     * @param str
     * @return
     */
    private static String getOriginUrl(String str) {
        String regex = "-cope(?=\\.[^.]+$)";
        return str.replaceAll(regex, "");
    }

    /**
     * 上传文件初始化
     *
     * @return
     */
    public COSClient initCosClient() {
        COSCredentials cred = new BasicCOSCredentials(System.getenv("secretId"), System.getenv("secretKey"));
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setRegion(new Region(cosConfig.getRegionName()));
        return new COSClient(cred, clientConfig);
    }

    /**
     * 图片列表上传请求
     *
     * @param imgMultiFileList
     * @return
     * @throws IOException
     */
    public LinkedList<String> putObject(List<MultipartFile> imgMultiFileList) throws IOException {
        COSClient cosClient = initCosClient();
        // 文件类型转换，spring只能接收MultipartFile类型图片
        LinkedList<File> imgFileList = FileUtils.multipartFileToFile(imgMultiFileList);
        // 存储key
        LinkedList<String> keyLinkedList = new LinkedList<>();

        imgFileList.forEach(imgFile -> {
            String key = imgFile.getName();
            String copedPicKey = copeFileKey(key);
            keyLinkedList.add(copedPicKey);
            log.info("处理后的图片Key---{}", copedPicKey);
            PicOperations picOperations = setPicRuleForUpload(copedPicKey, PIC_RULE);
            // raise upload picture request
            raisePutObjectRequest(picOperations, cosClient, key, imgFile);
        });
        log.info("keyList---{}", keyLinkedList);
        cosClient.shutdown();
        // 返回生成的文件名（包括后缀）
        return keyLinkedList;
    }

    /**
     * raise put object request
     *
     * @param picOperations
     * @param cosClient
     * @param key
     * @param imgFile
     */
    private void raisePutObjectRequest(PicOperations picOperations, COSClient cosClient, String key, File imgFile) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosConfig.getBucketName(), key, imgFile);
        // 上传COS存储
        putObjectRequest.setPicOperations(picOperations);
        // 发起请求
        PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
        log.info("cOSRequestResult---{}", putObjectResult.toString());
    }

    /**
     * set picture rule
     *
     * @param copedPicKey
     * @param picRule
     */
    private PicOperations setPicRuleForUpload(String copedPicKey, String picRule) {
        // 新建图片操作类
        PicOperations picOperations = new PicOperations();
        // 是否返回原图信息
        picOperations.setIsPicInfo(1);
        // 操作规则类
        PicOperations.Rule rule = new PicOperations.Rule();
        List<PicOperations.Rule> ruleList = new LinkedList<>();
        // 图片裁剪，并限制大小
        rule.setRule(PIC_RULE);
        rule.setBucket(cosConfig.getBucketName());
        rule.setFileId(copedPicKey);
        // 添加规则列表
        ruleList.add(rule);
        picOperations.setRules(ruleList);
        return picOperations;
    }

    /**
     * cope with key
     *
     * @param key
     * @return
     */
    private String copeFileKey(String key) {
        // 获取文件前缀名
        String prefix = FileNameUtil.getPrefix(key);
        // 获取文件后缀名
        String suffix = FileNameUtil.getSuffix(key);
        // 处理后的图片名称（Key），也是实际使用的url中的文件名
        return prefix + "-cope" + "." + suffix;
    }

    /**
     * 删除图片请求
     *
     * @param key
     */
    public void deleteObject(String key) {
        COSClient cosClient = initCosClient();
        // 因为图像处理后会分为两个文件，一个一个源文件，一个处理后的文件，因此需要将两个都删除
        // 原文件
        cosClient.deleteObject(cosConfig.getBucketName(), key);
        // 处理后的文件
        cosClient.deleteObject(cosConfig.getBucketName(), getOriginUrl(key));
        cosClient.shutdown();
    }

}
