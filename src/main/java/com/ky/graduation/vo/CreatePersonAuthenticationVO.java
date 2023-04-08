package com.ky.graduation.vo;

import lombok.Data;

/**
 * @author: Ky2Fe
 * @program: graduation
 * @description: 新增实验室对某人的授权
 **/

@Data
public class CreatePersonAuthenticationVO {

    Integer personId;

    Integer labId;

    String personName;

    String idNumber;

}
