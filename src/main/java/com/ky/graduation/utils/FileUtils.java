package com.ky.graduation.utils;

import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
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
     * @param multipartFileList 上传文件
     * @return file
     */
    public static LinkedList<File> multipartFileToFile(List<MultipartFile> multipartFileList) throws IOException {
        // 存储转换后的文件
        LinkedList<File> imgFileLinkedList = new LinkedList<>();
        // 获取文件名
        for (MultipartFile multipartFile : multipartFileList) {
            String fileName = multipartFile.getOriginalFilename();
            // 获取文件后缀
            assert fileName != null;
            String suffix = fileName.substring(fileName.lastIndexOf("."));
            // 防止生成的临时文件重复,文件名随机码, UUID
            File file = File.createTempFile(UUID.randomUUID().toString().replaceAll("-", ""), suffix);
            multipartFile.transferTo(file);
            imgFileLinkedList.add(file);
        }
        return imgFileLinkedList;
    }

    /**
     * 压缩图片大小和分辨率
     *
     * @param inputFile 输入文件
     * @return 压缩后的图片文件对象
     * @throws IOException 如果无法读取或写入文件，则抛出IOException
     */
    public static File compress(File inputFile) throws IOException {
        // 读取输入文件
        BufferedImage image = ImageIO.read(inputFile);

        // 计算压缩比例
        double ratio = 1.0;
        long imageSize = inputFile.length();
        // 超过2MB需要压缩
        if (imageSize > 1024 * 1024) {
            ratio = Math.sqrt((double) imageSize / (1024 * 1024));
        }

        // 压缩图片分辨率
        int width = (int) (image.getWidth() / ratio);
        int height = (int) (image.getHeight() / ratio);
        if (width > 1920 || height > 1080) { // 分辨率超过1080p需要压缩
            double scale = Math.min(1920.0 / width, 1080.0 / height);
            width = (int) (width * scale);
            height = (int) (height * scale);
        }

        // 压缩图片大小
        BufferedImage compressedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = compressedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();

        // 创建输出文件
        String outputFileName = "compressed_" + inputFile.getName();
        File outputFile = new File(inputFile.getParent(), outputFileName);

        // 写入压缩后的图片到输出文件
        OutputStream out = new FileOutputStream(outputFile);
        ImageIO.write(compressedImage, "jpg", out);
        out.close();

        return outputFile;
    }
}
