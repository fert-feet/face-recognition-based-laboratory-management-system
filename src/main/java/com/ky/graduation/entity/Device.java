package com.ky.graduation.entity;

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
public class Device implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     *  设备id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 设备密码
     */
    private String password;

    /**
     * 设备IP地址
     */
    private String ipAdress;

    /**
     * 设备序列号
     */
    private String deviceKey;

    /**
     * 所属部门名称
     */
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private String laboratoryName;
}
