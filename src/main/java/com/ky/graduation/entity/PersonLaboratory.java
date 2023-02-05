package com.ky.graduation.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

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
