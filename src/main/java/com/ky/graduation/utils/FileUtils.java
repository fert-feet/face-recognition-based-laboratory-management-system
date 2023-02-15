package com.ky.graduation.utils;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

/**
 * @author: Ky2Fe
 * @program: graduation
 * @description: 文件类工具
 **/

public class FileUtils {

    /**
     * 接口只能接受MultipartFile, 腾讯云需要File
     * 故 MultipartFile => File
     *
     * @param multiFile 上传文件
     * @return file
     */
    public static File multipartFileToFile(MultipartFile multiFile) throws IOException {
        // 获取文件名
        String fileName = multiFile.getOriginalFilename();
        // 获取文件后缀
        assert fileName != null;
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        // 防止生成的临时文件重复,文件名随机码, UUID
        File file = File.createTempFile(UUID.randomUUID().toString().replaceAll("-", ""), suffix);
        multiFile.transferTo(file);
        return file;
    }
}
