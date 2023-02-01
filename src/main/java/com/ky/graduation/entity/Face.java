package com.ky.graduation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * 
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-01
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
    private String  imgBase64;

    /**
     * 是否不严格检测照片质量	false（默认）：严格	true：不严格	
     */
    private Byte isEasyWay;
}
