package com.ky.graduation.utils;

import com.qcloud.cos.utils.IOUtils;
import org.apache.commons.codec.binary.Base64;
import org.springframework.util.ResourceUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @作者: tt—Tang
 * @描述: convert image into base64
 **/

public class Base64Convert {

    public static String convertToBase64() throws IOException {
        //使用spring boot自带的ResourceUtils从资源路径中获取文件
        File file = ResourceUtils.getFile("D:\\C盘迁移\\桌面\\temp\\face.jpg");
        FileInputStream fileInputStream = new FileInputStream(file);
        //使用IO流将其转换为字节数组
        byte[] bytes = IOUtils.toByteArray(fileInputStream);
        //将字节转换为base64
        String encodeBase64 = Base64.encodeBase64String(bytes);
        //关闭IO流
        fileInputStream.close();
        return encodeBase64;
    }

    public static void main(String[] args) throws IOException {
        System.out.println(convertToBase64());
    }
}
