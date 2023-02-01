package com.ky.graduation.utils;

import cn.hutool.json.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * @author: Ky2Fe
 * @program: graduation
 * @description: 封装请求
 **/

@Component
@Slf4j
public class SendRequest {

    /**
     * 发送get请求（有参数）
     *
     * @param url
     * @param params
     * @return JSONObject
     */
    public static JSONObject sendGetRequest(String url,MultiValueMap<String,Object> params) {
        RestTemplate client = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpMethod method = HttpMethod.POST;
        // 以表单的方式提交
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //将请求头部和参数合成一个请求
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(params, headers);
        //执行HTTP请求，将返回的结构使用ResultVO类格式化
        ResponseEntity<JSONObject> response = client.exchange(url, method, requestEntity, JSONObject.class);
        return response.getBody();
    }

    /**
     * 发送get请求（无参数）
     *
     * @param url
     * @return JSONObject
     */
    public static JSONObject sendGetRequest(String url) {
        RestTemplate client = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpMethod method = HttpMethod.GET;
        // 以表单的方式提交
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //将请求头部和参数合成一个请求
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(headers);
        //执行HTTP请求，将返回的结构使用ResultVO类格式化
        ResponseEntity<JSONObject> response = client.exchange(url, method, requestEntity, JSONObject.class);
        return response.getBody();
    }

    /**
     *发送POST请求
     *
     * @param url
     * @param params
     * @return JSONObject
     */
    public static JSONObject sendPostRequest(String url, MultiValueMap<String, Object> params) {
        RestTemplate client = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpMethod method = HttpMethod.POST;
        // 以表单的方式提交
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //将请求头部和参数合成一个请求
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(params, headers);
        //执行HTTP请求，将返回的结构使用ResultVO类格式化
        ResponseEntity<JSONObject> response = client.exchange(url, method, requestEntity, JSONObject.class);
        return response.getBody();
    }

}
