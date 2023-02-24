package com.ky.graduation.utils;

import cn.hutool.http.HttpException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ky.graduation.device.RequestResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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

    public static final String SUCCESS_CODE = "LAN_SUS-0";
    @Value("${requestUrl.baseUrl}")
    String baseUrl;

    /**
     * 发送get请求（有参数）
     *
     * @param url
     * @param params
     * @return JSONObject
     */
    public RequestResult sendGetRequest(String ip, String url, MultiValueMap<String, Object> params) {
        RestTemplate client = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpMethod method = HttpMethod.POST;
        // 以表单的方式提交
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //将请求头部和参数合成一个请求
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(params, headers);
        // Url拼接
        String requestUrl = "http://" + ip + ":8090" + url;
        log.info("requestUrl---{}", requestUrl);
        //执行HTTP请求，将返回的结构使用ResultVO类格式化
        ResponseEntity<JSONObject> response = client.exchange(requestUrl, method, requestEntity, JSONObject.class);
        RequestResult requestResult = JSONUtil.toBean(response.getBody(), RequestResult.class);
        if (requestResult != null && !requestResult.getCode().equals(SUCCESS_CODE)) {
            throw new HttpException(requestResult.getMsg());
        }
        return requestResult;
    }

    /**
     * 发送get请求（无参数）
     *
     * @param url
     * @return JSONObject
     */
    public RequestResult sendGetRequest(String ip, String url) {
        RestTemplate client = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpMethod method = HttpMethod.GET;
        // 以表单的方式提交
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //将请求头部和参数合成一个请求
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(headers);
        String requestUrl = "http://" + ip + ":8090" + url;
        log.info("requestUrl---{}", requestUrl);
        //执行HTTP请求，将返回的结构使用ResultVO类格式化
        ResponseEntity<JSONObject> response = client.exchange(requestUrl, method, requestEntity, JSONObject.class);
        RequestResult requestResult = JSONUtil.toBean(response.getBody(), RequestResult.class);
        if (requestResult != null && !requestResult.getCode().equals(SUCCESS_CODE)) {
            throw new HttpException(requestResult.getMsg());
        }
        return requestResult;
    }

    /**
     * 发送POST请求
     *
     * @param url
     * @param params
     * @return JSONObject
     */
    public RequestResult sendPostRequest(String ip, String url, MultiValueMap<String, Object> params) {
        RestTemplate client = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        HttpMethod method = HttpMethod.POST;
        // 以表单的方式提交
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //将请求头部和参数合成一个请求
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(params, headers);
        String requestUrl = "http://" + ip + ":8090" + url;
        log.info("requestUrl---{}", requestUrl);
        //执行HTTP请求，将返回的结构使用ResultVO类格式化
        ResponseEntity<JSONObject> response = client.exchange(requestUrl, method, requestEntity, JSONObject.class);
        RequestResult requestResult = JSONUtil.toBean(response.getBody(), RequestResult.class);
        if (requestResult != null && !requestResult.getCode().equals(SUCCESS_CODE)) {
            throw new HttpException(requestResult.getMsg());
        }
        return requestResult;
    }

}
