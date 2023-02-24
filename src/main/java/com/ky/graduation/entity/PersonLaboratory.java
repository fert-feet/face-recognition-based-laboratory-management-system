package com.ky.graduation.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
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
@TableName("person_laboratory")
public class PersonLaboratory implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 中间表id
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 人员id
     */
    private Integer pId;

    /**
     * 实验室id
     */
    private Integer labId;
}
