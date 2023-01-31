package com.ky.graduation.result;

/**
 * @author: Ky2Fe
 * @program: ky-vue-background
 * @description: 返回状态码接口
 **/

public interface StatusCode {

    /**
     * 状态码
     *
     * @return
     */
    public int getCode();

    /**
     * 状态信息
     *
     * @return
     */
    public String getMsg();

}
