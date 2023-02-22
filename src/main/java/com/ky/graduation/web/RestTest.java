package com.ky.graduation.web;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.utils.SendRequest;
import jakarta.annotation.Resource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;

/**
 * @author: Ky2Fe
 * @program: graduation
 * @description: 人脸机测试
 **/

@RestController
@RequestMapping("/deviceTest")
public class RestTest {

    @Resource
    private SendRequest sendRequest;

    @GetMapping("/test")
    public ResultVo test() {
        LinkedMultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.add("pass", "lwz7254591");
        HashMap<String, Object> personJson = new HashMap<>(3);
        personJson.put("id", "7");
        personJson.put("name", "彭于晏");
        personJson.put("iDNumber", "123456789");
        multiValueMap.add("person", JSONUtil.toJsonStr(personJson));
        JSONObject result = sendRequest.sendPostRequest("/person/create", multiValueMap);
        return ResultVo.success().data("result",result);
    }
}
