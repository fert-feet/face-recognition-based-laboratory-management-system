package com.ky.graduation.service.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ky.graduation.device.RequestResult;
import com.ky.graduation.entity.Device;
import com.ky.graduation.entity.Laboratory;
import com.ky.graduation.entity.Person;
import com.ky.graduation.mapper.DeviceMapper;
import com.ky.graduation.mapper.LaboratoryMapper;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.IDeviceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ky.graduation.utils.SendRequest;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-01
 */
@Service
@Slf4j
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements IDeviceService {

    @Resource
    private DeviceMapper deviceMapper;

    @Resource
    private LaboratoryMapper laboratoryMapper;

    @Resource
    private SendRequest sendRequest;

    @Value("${requestUrl.person.deletePerson}")
    private String deletePersonUrl;

    @Value("${requestUrl.person.createPerson}")
    private String createPersonUrl;

    private static final String SORT_REVERSE = "-id";

    @Override
    public ResultVo listDevice(long page, long limit, String labName, String sort) {
        Page<Device> labPage = new Page<>();
        labPage.setCurrent(page);
        labPage.setSize(limit);
        LambdaQueryWrapper<Device> wrapper = Wrappers.lambdaQuery();
        if (labName != null && !StringUtils.isBlank(labName)) {
            // 根据所属实验室名称查找设备
            wrapper.like(Device::getLaboratoryName, labName);
        }
        if (SORT_REVERSE.equals(sort)) {
            // 倒序排列
            wrapper.orderByDesc(Device::getId);
        }
        // 顺序排列
        wrapper.orderByAsc(Device::getId);
        Page<Device> selectPage = deviceMapper.selectPage(labPage, wrapper);
        return ResultVo.success().data("items",selectPage.getRecords()).data("total",selectPage.getTotal());
    }

    @Override
    public ResultVo getBelongLab(int deviceId) {
        String belongLab = deviceMapper.getBelongLab(deviceId);
        return ResultVo.success().data("labName",belongLab);
    }

    @Override
    public ResultVo createOrUpdate(Device device) {
        // 为新增则直接操作数据库
        if (device.getId() == null) {
            deviceMapper.insert(device);
            return ResultVo.success();
        }
        LinkedMultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        // 若为更新所属实验室状态（包括取消和更改），则首先清空该设备人员以及照片信息
        multiValueMap.set("pass", device.getPassword());
        multiValueMap.set("id", "-1");
        RequestResult deletePersonRequest = sendRequest.sendPostRequest(device.getIpAdress(), deletePersonUrl, multiValueMap);
        log.info("deletePersonRequest---{}", deletePersonRequest.getMsg());
        // 取消分配实验室
        if (device.getLaboratoryName() == null) {
            // 将实验室相关字段都置空
            device.setLaboratoryName(null);
            device.setLaboratoryId(null);
            deviceMapper.updateById(device);
            return ResultVo.success();
        }
        // 若更改所属实验室，则同时更新所属实验室id
        LambdaQueryWrapper<Laboratory> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Laboratory::getName, device.getLaboratoryName());
        // 根据实验室名称更新实验室id
        Laboratory laboratory = laboratoryMapper.selectOne(wrapper);
        device.setLaboratoryId(laboratory.getId());
        deviceMapper.updateById(device);
        // 将更改后的实验室中的人员迁移到此设备中
        LinkedList<Person> authenticatedPersonList = laboratoryMapper.findAuthenticatedPerson(laboratory.getId());
        JSONObject personJson = new JSONObject();
        authenticatedPersonList.forEach(person -> {
            personJson.set("id", person.getId().toString());
            personJson.set("name", person.getName());
            personJson.set("iDNumber",person.getIdNumber());
            personJson.set("password","123456");
            multiValueMap.set("person", personJson);
            RequestResult createPersonRequest = sendRequest.sendPostRequest(device.getIpAdress(), createPersonUrl, multiValueMap);
            log.info("createPersonRequest---{}", createPersonRequest.getMsg());
        });
        return ResultVo.success();
    }

}
