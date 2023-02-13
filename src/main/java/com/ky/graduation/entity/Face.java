package com.ky.graduation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * <p>
 * 
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-08
 */
@Getter
@Setter
public class Face implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 人脸id
     */
    @TableId(value = "face_id", type = IdType.AUTO)
    private Integer faceId;

    /**
     * 人员id
     */
    private Integer personId;

    /**
     * 人脸照片base64编码
     */
    private String  imgEncode;

    /**
     * 是否不严格检测照片质量	false（默认）：严格	true：不严格	
     */
    private Byte isEasyWay;

    /**
     * 人脸照片外链
     */
    private String imgUrl;
}
