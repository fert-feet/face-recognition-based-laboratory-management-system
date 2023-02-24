package com.ky.graduation.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author: Ky2Fe
 * @program: ky-vue-background
 * @description: 返回信息代码枚举
 **/

@Getter
@AllArgsConstructor
public enum ResultCode implements StatusCode {

    //统一成功
    SUCCESS(200, "成功"),

    FAIL(1001, "后台错误"),

    VALID_DATA(1002, "参数校验错误"),

    RUNTIME_ERROR(1003, "运行时异常"),

    SQL_ERROR(1004, "数据库异常"),

    HTTP_ERROR(1005, "HTTP异常"),

    NULL_POINT_ERROR(1006, "空指针异常"),

    EMPTY_QUERY(1007, "查询结果为空"),

    VALIDATE_ERROR(1008, "账号或密码错误"),

    PARAMS_ERROR(1009, "传入参数异常"),

    PARAMS_TYPE_ERROR(1010, "传入参数类型异常"),

    COS_CLIENT_ERROR(1011, "COS客户端异常"),

    COS_SERVICE_ERROR(1012, "COS服务端异常"),

    IO_ERROR(1013, "IO异常"),

    DEVICE_REQUEST_ERROR(2000, "人脸机请求失败"),

    ;

    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态信息
     */
    private final String msg;

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMsg() {
        return msg;
    }
}
