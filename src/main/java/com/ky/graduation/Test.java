package com.ky.graduation;

import com.ky.graduation.utils.FaceImgLocalStoreUtil;
import com.qcloud.cos.utils.IOUtils;
import jakarta.annotation.Resource;
import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author : kyrie
 * @description: : 测试
 **/

public class Test {

    @Resource
    private FaceImgLocalStoreUtil localStoreUtil;

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


    public void test() throws IOException {
        File file = new File("D:\\C盘迁移\\桌面\\temp\\face.jpg");

        // compress file
        File compressedFile = compress(file);

        FileInputStream fileInputStream = new FileInputStream(compressedFile);
        //使用IO流将其转换为字节数组
        byte[] encodeBytes = IOUtils.toByteArray(fileInputStream);
        //将字节转换为base64
        String encodeBase64 = Base64.encodeBase64String(encodeBytes);

        fileInputStream.close();
    }

    public static void main(String[] args) {
        List<String> list = new ArrayList<>();
        list.add("LAN_EXP-8012");
        list.add("LAN_EXP-3009");
        Stream<String> stringStream = list.stream().filter(item -> item.contains("8"));
        stringStream.forEach(System.out::println);
    }
}
