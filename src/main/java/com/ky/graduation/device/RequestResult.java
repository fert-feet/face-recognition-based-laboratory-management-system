package com.ky.graduation.device;

import lombok.Data;

/**
 * @author: Ky2Fe
 * @program: graduation
 * @description: 人脸机请求返回类
 **/

@Data
public class RequestResult {
    String code;

    String msg;

    Object data;

    Integer result;

    Boolean success;
}
