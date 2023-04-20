package com.ky.graduation.generator;

import com.baomidou.mybatisplus.generator.FastAutoGenerator;
import com.baomidou.mybatisplus.generator.config.OutputFile;
import com.baomidou.mybatisplus.generator.engine.FreemarkerTemplateEngine;

import java.util.Collections;

/**
 * @author: Ky2Fe
 * @program: graduation
 * @description: 代码生成器
 **/

public class Generator {
    public static final String URL = "jdbc:mysql://localhost:3306/ky_graduate?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
    public static final String USER_NAME = "root";
    public static final String PASS_WORD = "lwz7254591";
    public static final String AUTHOR = "kyrie";
    public static final String OUTPUT_DIR = "D:\\Project\\Graduate\\backend\\graduation\\src\\main\\java\\com\\ky\\graduation\\generator";
    public static final String PARENT_DIR = "com.aquarium";
    public static final String XML_DIR = "D:\\Project\\Graduate\\backend\\graduation\\src\\main\\java\\com\\ky\\graduation\\generator\\com\\fxml";

    public static void main(String[] args) {
        FastAutoGenerator.create(URL, USER_NAME, PASS_WORD)
                .globalConfig(builder -> {
                    builder.author(AUTHOR) // 设置作者
                            .outputDir(OUTPUT_DIR); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent(PARENT_DIR) // 设置父包名
                            .pathInfo(Collections.singletonMap(OutputFile.xml, XML_DIR)); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    // 设置需要生成的表名
                    builder.addInclude("face")
                            .entityBuilder()
                            //开启Lombok
                            .enableLombok()
                            .controllerBuilder()
                            //开启RestController
                            .enableRestStyle();
                })
                // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .templateEngine(new FreemarkerTemplateEngine())
                .execute();
    }
}
