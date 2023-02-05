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
 * @since 2023-02-05
 */
@Getter
@Setter
public class Person implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 人员id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 人员姓名
     */
    private String name;

    /**
     * 人员密码，任何涉及个人密码的方面都可以使用
     */
    private String password;

    /**
     * 身份证号码，用于登录
     */
    private String idNumber;

    /**
     * 是否已分配实验室权限
     */
    private Byte isDistributed;
}
