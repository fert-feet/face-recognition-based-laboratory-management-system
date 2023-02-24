package com.ky.graduation.service.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ky.graduation.device.RequestResult;
import com.ky.graduation.entity.Device;
import com.ky.graduation.entity.Face;
import com.ky.graduation.entity.Laboratory;
import com.ky.graduation.entity.Person;
import com.ky.graduation.mapper.DeviceMapper;
import com.ky.graduation.mapper.FaceMapper;
import com.ky.graduation.mapper.LaboratoryMapper;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.IDeviceService;
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
 * 服务实现类
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-01
 */
@Service
@Slf4j
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements IDeviceService {

    private static final String SORT_REVERSE = "-id";
    @Resource
    private DeviceMapper deviceMapper;
    @Resource
    private FaceMapper faceMapper;
    @Resource
    private LaboratoryMapper laboratoryMapper;
    @Resource
    private SendRequest sendRequest;
    @Value("${requestUrl.person.deletePerson}")
    private String deletePersonUrl;
    @Value("${requestUrl.person.createPerson}")
    private String createPersonUrl;
    @Value("${requestUrl.device.setPassWord}")
    private String setPassWordUrl;
    @Value("${requestUrl.face.createFace}")
    private String createFaceUrl;

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
        return ResultVo.success().data("items", selectPage.getRecords()).data("total", selectPage.getTotal());
    }

    @Override
    public ResultVo getBelongLab(int deviceId) {
        String belongLab = deviceMapper.getBelongLab(deviceId);
        return ResultVo.success().data("labName", belongLab);
    }

    @Override
    public ResultVo createOrUpdate(Device device) {
        log.info("labName---{}", device.getLaboratoryName());
        // 为新增则直接操作数据库
        if (device.getId() == null) {
            deviceMapper.insert(device);
            return ResultVo.success();
        }
        // 此情况为防止传入实验室信息为NULL且为编辑的情况
        if (device.getLaboratoryName() == null) {
            return updateDeviceInfo(device);
        }
        // 若传入实验室与设备绑定实验室相同，则为编辑设备信息操作
        LambdaQueryWrapper<Device> deviceWrapper = Wrappers.lambdaQuery();
        deviceWrapper.eq(Device::getLaboratoryName, device.getLaboratoryName()).eq(Device::getId, device.getId());
        if (deviceMapper.exists(deviceWrapper)) {
            return updateDeviceInfo(device);
        }
        LinkedMultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        // 若为更新所属实验室状态（包括取消和更改），则首先清空该设备人员以及照片信息
        multiValueMap.set("pass", device.getPassword());
        multiValueMap.set("id", "-1");
        RequestResult deletePersonRequest = sendRequest.sendPostRequest(device.getIpAdress(), deletePersonUrl, multiValueMap);
        log.info("deletePersonRequest---{}", deletePersonRequest.getMsg());
        // 取消分配实验室
        if (device.getLaboratoryName().equals("")) {
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
            personJson.set("iDNumber", person.getIdNumber());
            personJson.set("password", "123456");
            multiValueMap.set("person", personJson);
            RequestResult createPersonRequest = sendRequest.sendPostRequest(device.getIpAdress(), createPersonUrl, multiValueMap);
            log.info("createPersonRequest---{}", createPersonRequest.getMsg());
            // 将人员照片也传入
            LambdaQueryWrapper<Face> faceWrapper = Wrappers.lambdaQuery();
            faceWrapper.eq(Face::getPersonId, person.getId());
            List<Face> faceList = faceMapper.selectList(faceWrapper);
            // 循环传入每个人的人脸照片
            faceList.forEach(face -> {
                multiValueMap.set("pass", device.getPassword());
                multiValueMap.set("personId", person.getId());
                multiValueMap.set("faceId", face.getFaceId());
                multiValueMap.set("url", face.getUrl());
                sendRequest.sendPostRequest(device.getIpAdress(), createFaceUrl, multiValueMap);
            });
        });
        return ResultVo.success();
    }

    @Override
    public ResultVo deleteDevice(int id) {
        // 删除设备中所有人员信息
        Device device = deviceMapper.selectById(id);
        LinkedMultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.set("pass", device.getPassword());
        multiValueMap.set("id", "-1");
        sendRequest.sendPostRequest(device.getIpAdress(), deletePersonUrl, multiValueMap);
        // 删除数据库中设备字段
        if (deviceMapper.deleteById(id) > 0) {
            return ResultVo.success();
        }
        return ResultVo.error();
    }

    private ResultVo updateDeviceInfo(Device device) {
        // 若更改密码，则需请求设备进行更改
        Device selectDevice = deviceMapper.selectById(device.getId());
        if (!selectDevice.getPassword().equals(device.getPassword())) {
            LinkedMultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
            // 旧密码
            multiValueMap.set("oldPass", selectDevice.getPassword());
            // 新密码
            multiValueMap.set("newPass", device.getPassword());
            RequestResult setPassWordRequest = sendRequest.sendPostRequest(device.getIpAdress(), setPassWordUrl, multiValueMap);
            log.info("setPassWordRequest---{}", setPassWordRequest.getMsg());
        }
        // 直接更新信息即可
        deviceMapper.updateById(device);
        return ResultVo.success();
    }

}
