package com.ky.graduation.utils;

import com.ky.graduation.dto.FaceImgLocalStoreDTO;
import com.ky.graduation.mapper.FaceMapper;
import com.qcloud.cos.utils.IOUtils;
import jakarta.annotation.Resource;
import org.apache.commons.codec.binary.Base64;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @作者: ky2fe
 * @描述: convert image into base64
 **/

@Component
public class FaceImgLocalStoreUtil {

    @Resource
    private FaceMapper faceMapper;

    public FaceImgLocalStoreDTO faceImgLocalStoreProcess(List<MultipartFile> imgList) throws IOException {
        // convert MultipartFile type to File type
        List<File> faceImgFileList = FileUtils.multipartFileToFile(imgList);
        // convert file type to byte[] for store in local db with longBlob type

        return convertToBytesAndBase64(faceImgFileList);
    }

    /**
     * convert file type into bytes type
     *
     * @param faceImgFileList
     * @return
     */
    private FaceImgLocalStoreDTO convertToBytesAndBase64(List<File> faceImgFileList) throws IOException {
        FaceImgLocalStoreDTO storeDTO = new FaceImgLocalStoreDTO();
        List<byte[]> faceBytesList = new ArrayList<>();
        List<String> faceBase64List = new ArrayList<>();

        for (File imgFile : faceImgFileList) {
            FileInputStream fileInputStream = new FileInputStream(imgFile);
            //使用IO流将其转换为字节数组
            byte[] encodeBytes = IOUtils.toByteArray(fileInputStream);
            //将字节转换为base64
            String encodeBase64 = Base64.encodeBase64String(encodeBytes);

            faceBytesList.add(encodeBytes);
            faceBase64List.add(encodeBase64);
            fileInputStream.close();
        }

        // set list in dto
        storeDTO.setImgByteEncodeList(faceBytesList);
        storeDTO.setImgBase64EncodeList(faceBase64List);
        return storeDTO;
    }

    public static void main(String[] args) throws IOException {
        FaceImgLocalStoreUtil faceImgLocalStoreUtil = new FaceImgLocalStoreUtil();
        faceImgLocalStoreUtil.test();
    }

    public void test() throws IOException {
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
