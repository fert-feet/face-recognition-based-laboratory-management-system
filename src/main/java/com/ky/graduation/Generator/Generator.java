package com.ky.graduation.Generator;

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
    public static final String AUTHOR = "Ky2Fe";
    public static final String OUTPUT_DIR = "D:\\Project\\Graduate\\backend\\graduation\\src\\main\\java";
    public static final String PARENT_DIR = "com.ky.graduation";
    public static final String XML_DIR = "D:\\Project\\Graduate\\backend\\graduation\\src\\main\\resources\\mapper";

    public static void main(String[] args) {
        FastAutoGenerator.create(URL, USER_NAME, PASS_WORD)
                .globalConfig(builder -> {
                    builder.author(AUTHOR) // 设置作者
                            .fileOverride() // 覆盖已生成文件
                            .outputDir(OUTPUT_DIR); // 指定输出目录
                })
                .packageConfig(builder -> {
                    builder.parent(PARENT_DIR) // 设置父包名
                            .pathInfo(Collections.singletonMap(OutputFile.xml, XML_DIR)); // 设置mapperXml生成路径
                })
                .strategyConfig(builder -> {
                    builder.addInclude("admin","device","face","person","laboratory")// 设置需要生成的表名
                            .entityBuilder()
                            .enableLombok() //开启Lombok
                            .controllerBuilder()
                            .enableRestStyle(); //开启RestController
                })
                .templateEngine(new FreemarkerTemplateEngine()) // 使用Freemarker引擎模板，默认的是Velocity引擎模板
                .execute();
    }
}
