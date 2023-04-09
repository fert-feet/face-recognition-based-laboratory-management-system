package com.ky.graduation.utils;

import com.ky.graduation.mapper.FaceMapper;
import com.qcloud.cos.utils.IOUtils;
import org.apache.commons.codec.binary.Base64;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

class FaceImgLocalStoreUtilTest {

    @Mock
    private FaceMapper faceMapper;

    @Test
    void faceImgLocalStoreProcess() throws IOException {
        File file = new File("D:\\C盘迁移\\桌面\\temp\\face.jpg");

        FileInputStream fileInputStream = new FileInputStream(file);
        //使用IO流将其转换为字节数组
        byte[] encodeBytes = IOUtils.toByteArray(fileInputStream);
        //将字节转换为base64
        String encodeBase64 = Base64.encodeBase64String(encodeBytes);
        fileInputStream.close();

        System.out.println(encodeBase64);
    }
}