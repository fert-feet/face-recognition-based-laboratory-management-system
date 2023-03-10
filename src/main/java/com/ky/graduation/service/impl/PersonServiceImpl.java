package com.ky.graduation.service.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ky.graduation.device.RequestResult;
import com.ky.graduation.entity.*;
import com.ky.graduation.mapper.*;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.IPersonService;
import com.ky.graduation.utils.CosRequest;
import com.ky.graduation.utils.SendRequest;
import com.ky.graduation.vo.AuthenticateLabToPersonVO;
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
 * @since 2023-02-05
 */
@Service
@Slf4j
public class PersonServiceImpl extends ServiceImpl<PersonMapper, Person> implements IPersonService {

    private static final String SORT_REVERSE = "-id";
    private static final String AUTHENTICATED_SQL = "SELECT lab_id FROM person_laboratory WHERE p_id=";
    @Resource
    private SendRequest sendRequest;
    @Resource
    private PersonMapper personMapper;
    @Resource
    private LaboratoryMapper laboratoryMapper;
    @Resource
    private FaceMapper faceMapper;
    @Resource
    private DeviceMapper deviceMapper;
    @Resource
    private CosRequest cosRequest;
    @Resource
    private PersonLaboratoryMapper personLaboratoryMapper;
    @Value("${requestUrl.person.createPerson}")
    private String createPersonUrl;
    @Value("${requestUrl.person.deletePerson}")
    private String deletePersonUrl;
    @Value("${requestUrl.face.createFace}")
    private String createFaceUrl;
    @Value("${requestUrl.person.updatePerson}")
    private String updatePersonUrl;

    @Override
    public ResultVo listPerson(long page, long limit, String name, String sort) {
        Page<Person> labPage = new Page<>();
        labPage.setCurrent(page);
        labPage.setSize(limit);
        LambdaQueryWrapper<Person> wrapper = Wrappers.lambdaQuery();
        if (name != null && !StringUtils.isBlank(name)) {
            // 根据人员姓名查询
            wrapper.like(Person::getName, name);
        }
        if (SORT_REVERSE.equals(sort)) {
            // 倒序排列
            wrapper.orderByDesc(Person::getId);
        }
        // 顺序排列
        wrapper.orderByAsc(Person::getId);
        Page<Person> selectPage = personMapper.selectPage(labPage, wrapper);
        return ResultVo.success().data("items", selectPage.getRecords()).data("total", selectPage.getTotal());
    }

    @Override
    public ResultVo findAuthenticatedLab(int id) {
        LambdaQueryWrapper<Laboratory> wrapper = Wrappers.lambdaQuery();
        // IN语句查询授权给人员的实验室
        wrapper.inSql(Laboratory::getId, AUTHENTICATED_SQL + id);
        List<Laboratory> laboratories = laboratoryMapper.selectList(wrapper);
        return ResultVo.success().data("authorizedLabList", laboratories);
    }

    @Override
    public ResultVo authenticateToPerson(AuthenticateLabToPersonVO authenticateVO) {
        // 删除存在于各实验室人脸机中该人员的信息
        List<Device> deviceList = personMapper.findDeviceListContainPerson(authenticateVO.getPersonId());
        LinkedMultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        // 循环向各个人脸机发送删除请求
        deviceList.forEach(device -> {
            // 人员删除，同时清空人员人脸
            multiValueMap.set("pass", device.getPassword());
            multiValueMap.set("id", authenticateVO.getPersonId().toString());
            RequestResult deletePersonResult = sendRequest.sendPostRequest(device.getIpAdress(), deletePersonUrl, multiValueMap);
            log.info("deletePersonResult---{}", deletePersonResult.getMsg());
        });
        // 将对应人员分配的实验室都删除
        LambdaQueryWrapper<PersonLaboratory> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(PersonLaboratory::getPId, authenticateVO.getPersonId());
        personLaboratoryMapper.delete(wrapper);
        Person person = new Person();

        // 传入的实验室id列表为空时，只更改人员分配状态
        if (authenticateVO.getLabIdList().size() == 0) {
            person.setId(authenticateVO.getPersonId());
            person.setIsDistributed((byte) 0);
            personMapper.updateById(person);
            return ResultVo.success();
        }
        // id列表不为空时，循环插入数据库，请求各人脸机新增人员
        authenticateVO.getLabIdList().forEach(labId -> {
            LambdaQueryWrapper<Device> query = Wrappers.lambdaQuery();
            query.eq(Device::getLaboratoryId, labId);
            // 找到该每个实验室对应的人脸机
            List<Device> devices = deviceMapper.selectList(query);
            // 查询该人员信息
            Person selectPerson = personMapper.selectById(authenticateVO.getPersonId());
            JSONObject personJson = new JSONObject();
            // 查询人员照片
            LambdaQueryWrapper<Face> faceWrapper = Wrappers.lambdaQuery();
            faceWrapper.eq(Face::getPersonId, authenticateVO.getPersonId());
            List<Face> faceList = faceMapper.selectList(faceWrapper);
            // 循环对人脸机发起请求，该双重循环不可避免
            devices.forEach(device -> {
                // 人员添加
                personJson.set("id", authenticateVO.getPersonId().toString());
                personJson.set("name", selectPerson.getName());
                personJson.set("iDNumber", selectPerson.getIdNumber());
                personJson.set("password", "123456");
                multiValueMap.set("pass", device.getPassword());
                multiValueMap.set("person", personJson);
                RequestResult createPersonResult = sendRequest.sendPostRequest(device.getIpAdress(), createPersonUrl, multiValueMap);
                log.info("createPersonResult---{}", createPersonResult.getMsg());
                // 遍历人脸照片列表，添加到每个人脸机，双重循环不可避免
                faceList.forEach(face -> {
                    multiValueMap.set("personId", authenticateVO.getPersonId().toString());
                    multiValueMap.set("faceId", face.getFaceId().toString());
                    multiValueMap.set("url", face.getUrl());
                    sendRequest.sendPostRequest(device.getIpAdress(), createFaceUrl, multiValueMap);
                });
            });
            // 每个循环都需要创建对象
            PersonLaboratory personLaboratory = new PersonLaboratory();
            // 添加人员与实验室授权关系
            personLaboratory.setPId(authenticateVO.getPersonId());
            personLaboratory.setLabId(labId);
            personLaboratoryMapper.insert(personLaboratory);
            // 添加人员授权状态
            person.setId(authenticateVO.getPersonId());
            person.setIsDistributed((byte) 1);
            personMapper.updateById(person);
        });
        return ResultVo.success();
    }

    @Override
    public ResultVo createOrUpdate(Person person) {
        // id为空则为新增，直接操作数据库
        if (person.getId() == null) {
            personMapper.insert(person);
            return ResultVo.success();
        }
        // 若已经写入人脸机中，则需要请求人脸机进行修改
        if (person.getIsDistributed() == 1) {
            // 查找人员所在的各设备
            List<Device> deviceList = personMapper.findDeviceListContainPerson(person.getId());
            LinkedMultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
            // 循环请求请求人脸机进行更新
            deviceList.forEach(device -> {
                JSONObject personJson = new JSONObject();
                personJson.set("id", person.getId().toString());
                personJson.set("name", person.getName());
                // 暂时写死，只允许6位，后续改写
                personJson.set("password", "132456");
                personJson.set("iDNumber", person.getIdNumber());
                multiValueMap.set("person", personJson);
                multiValueMap.set("pass", device.getPassword());
                // 发起请求
                RequestResult requestResult = sendRequest.sendPostRequest(device.getIpAdress(), updatePersonUrl, multiValueMap);
                log.info("requestResult---{}", requestResult.getMsg());
            });
        }
        // 设置默认密码
        person.setPassword(person.getIdNumber());
        personMapper.updateById(person);
        return ResultVo.success();
    }

    @Override
    public ResultVo deletePerson(int id) {
        // 查询人员是否存在于设备中
        LinkedList<Device> deviceList = personMapper.findDeviceListContainPerson(id);
        //若存在，则删除设备中此人员以及照片
        if (deviceList.size() > 0) {
            deviceList.forEach(device -> {
                LinkedMultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
                multiValueMap.set("pass", device.getPassword());
                multiValueMap.set("id", id);
                RequestResult deletePersonRequest = sendRequest.sendPostRequest(device.getIpAdress(), deletePersonUrl, multiValueMap);
                log.info("deletePersonRequest---{}", deletePersonRequest.getMsg());
            });
        }
        // 若不存在设备中，则在云端删除照片
        LambdaQueryWrapper<Face> faceWrapper = Wrappers.lambdaQuery();
        faceWrapper.eq(Face::getPersonId, id);
        List<Face> faceList = faceMapper.selectList(faceWrapper);
        // 若有人脸信息，循环向云端发起删除请求
        if (faceList.size() > 0) {
            faceList.forEach(face -> {
                cosRequest.deleteObject(face.getName());
            });
        }
        // 操作数据库，级联删除，实验室授权关系与人员照片
        if (personMapper.deleteById(id) > 0) {
            return ResultVo.success();
        }
        return ResultVo.error();
    }

}
