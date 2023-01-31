package com.ky.graduation.result;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Ky2Fe
 * @program: ky-vue-background
 * @description: 统一返回类，支持链式调用
 **/

@Data
public class ResultVo {

    /**
     * 状态代码
     */
    private int code;

    /**
     * 状态信息
     */
    private String msg;

    /**
     * 数据
     */
    private Map<String, Object> data = new HashMap<>();

    /**
     * 成功返回结果
     *
     * @return resultVo
     */
    public static ResultVo success() {
        ResultVo resultVo = new ResultVo();
        resultVo.setCode(ResultCode.SUCCESS.getCode());
        resultVo.setMsg(ResultCode.SUCCESS.getMsg());
        return resultVo;
    }

    /**
     * 失败返回结果
     *
     * @return resultVo
     */
    public static ResultVo error() {
        ResultVo resultVo = new ResultVo();
        resultVo.setCode(ResultCode.FAIL.getCode());
        resultVo.setMsg(ResultCode.FAIL.getMsg());
        return resultVo;
    }

    /**
     * 自定义状态，使用枚举类里的状态和状态码
     *
     * @param status
     * @return
     */
    public ResultVo status(StatusCode status) {
        this.setMsg(status.getMsg());
        this.setCode(status.getCode());
        return this;
    }

    /**
     * 自定义数据，支持链式调用
     *
     * @param key
     * @param data
     * @return this
     */
    public ResultVo data(String key, Object data) {
        this.data.put(key, data);
        return this;
    }
}
