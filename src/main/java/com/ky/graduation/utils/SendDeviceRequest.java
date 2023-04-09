package com.ky.graduation.utils;

import cn.hutool.http.HttpException;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ky.graduation.device.RequestResult;
import com.ky.graduation.entity.Face;
import com.ky.graduation.entity.Person;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * @author: Ky2Fe
 * @program: graduation
 * @description: 封装请求
 **/

@Component
@Slf4j
public class SendDeviceRequest {

    public static final String SUCCESS_CODE = "LAN_SUS-0";
    @Value("${requestUrl.baseUrl}")
    String baseUrl;

    @Value("${requestUrl.face.createFace}")
    private String createFaceUrl;

    @Value("${requestUrl.person.createPerson}")
    private String createPersonUrl;

    @Value("${requestUrl.person.deletePerson}")
    private String deletePersonUrl;

    @Value("${deviceOption.isOpenDevice}")
    private boolean isOpenDevice;

    @Value("${requestUrl.face.deleteFace}")
    private String deleteFaceUrl;

    @Value("${requestUrl.person.updatePerson}")
    private String updatePersonUrl;


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
        // skip device request when isOpenDevice is false
        if (!isOpenDevice) {
            return new RequestResult();
        }
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

    /**
     * delete person information in device
     *
     * @param devicePassword
     * @param deviceIpAddress
     * @param personId
     */
    public void deleteDevicePerson(String devicePassword, String deviceIpAddress, String personId) {
        LinkedMultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        // set password and personId in device
        // when id equals -1, then delete all data that remain in device
        multiValueMap.set("pass", devicePassword);
        multiValueMap.set("id", personId);
        RequestResult deletePersonRequest = this.sendPostRequest(deviceIpAddress, deletePersonUrl, multiValueMap);
        log.info("deletePersonRequest---{}", deletePersonRequest.getMsg());
    }

    /**
     * create or update person in device
     *
     * @param devicePassword
     * @param deviceIpAddress
     */
    public void createOrUpdateDevicePerson(String devicePassword, String deviceIpAddress, Person person, boolean updateFlag) {
        LinkedMultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.set("pass", devicePassword);
        JSONObject personJson = new JSONObject();
        personJson.set("id", String.valueOf(person.getId()));
        personJson.set("name", person.getName());
        personJson.set("iDNumber", person.getIdNumber());
        personJson.set("password", "123456");
        multiValueMap.set("person", personJson);
        // check if update request
        if (updateFlag) {
            RequestResult updatePersonRequest = this.sendPostRequest(deviceIpAddress, updatePersonUrl, multiValueMap);
            log.info("updatePersonRequest---{}", updatePersonRequest.getMsg());
        } else {
            RequestResult createPersonRequest = this.sendPostRequest(deviceIpAddress, createPersonUrl, multiValueMap);
            log.info("createPersonRequest---{}", createPersonRequest.getMsg());
        }
    }

    /**
     * create person face photos in device
     *
     * @param devicePassword
     * @param deviceIpAddress
     * @param face
     * @param person
     */
    public void createDevicePersonFace(String devicePassword, String deviceIpAddress, Face face, Person person) {
        LinkedMultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();

        multiValueMap.set("pass", devicePassword);
        multiValueMap.set("personId", String.valueOf(person.getId()));
        multiValueMap.set("faceId", String.valueOf(face.getFaceId()));
        multiValueMap.set("url", face.getUrl());

        // send request to device
        RequestResult requestResult = this.sendPostRequest(deviceIpAddress, createFaceUrl, multiValueMap);
        log.info("createPersonFaceRequest---{}", requestResult.getMsg());
    }

    /**
     * delete person face photos in device
     *
     * @param devicePassword
     * @param deviceIpAddress
     * @param faceId
     */
    public void deleteDevicePersonFace(String devicePassword, String deviceIpAddress, String faceId) {
        LinkedMultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.set("pass", devicePassword);
        multiValueMap.set("faceId", faceId);
        RequestResult requestResult = this.sendPostRequest(deviceIpAddress, deleteFaceUrl, multiValueMap);
        log.info("deletePersonFaceRequest---{}", requestResult.getMsg());
    }


}
