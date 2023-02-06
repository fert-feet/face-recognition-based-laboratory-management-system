package com.ky.graduation.vo;

import lombok.Data;

import java.util.List;

/**
 * @author: Ky2Fe
 * @program: graduation
 * @description: 批量授权实验室许可给本人员
 **/

@Data
public class AuthenticateLabToPersonVO {
    List<Integer> labIdList;

    Integer personId;
}
