package com.ky.graduation.service.impl;

import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ky.graduation.device.RequestResult;
import com.ky.graduation.entity.*;
import com.ky.graduation.mapper.*;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.IPersonService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ky.graduation.utils.SendRequest;
import com.ky.graduation.vo.AuthenticateLabToPersonVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-05
 */
@Service
@Slf4j
public class PersonServiceImpl extends ServiceImpl<PersonMapper, Person> implements IPersonService {

    @Resource
    private SendRequest sendRequest;

    @Resource
    private PersonMapper personMapper;

    @Resource
    private LaboratoryMapper laboratoryMapper;

    @Resource
    private DeviceMapper deviceMapper;

    @Resource
    private PersonLaboratoryMapper personLaboratoryMapper;

    @Value("${requestUrl.person.createPerson}")
    private String createPersonUrl;

    @Value("${requestUrl.person.deletePerson}")
    private String deletePersonUrl;

    private static final String SORT_REVERSE = "-id";

    private static final String AUTHENTICATED_SQL = "SELECT lab_id FROM person_laboratory WHERE p_id=";


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
        return ResultVo.success().data("items",selectPage.getRecords()).data("total",selectPage.getTotal());
    }

    @Override
    public ResultVo findAuthenticatedLab(int id) {
        LambdaQueryWrapper<Laboratory> wrapper = Wrappers.lambdaQuery();
        // IN语句查询授权给人员的实验室
        wrapper.inSql(Laboratory::getId,AUTHENTICATED_SQL+id);
        List<Laboratory> laboratories = laboratoryMapper.selectList(wrapper);
        return ResultVo.success().data("authorizedLabList",laboratories);
    }

    @Override
    public ResultVo authenticateToPerson(AuthenticateLabToPersonVO authenticateVO) {
        // 删除存在于各实验室人脸机中该人员的信息
        List<Device> deviceList = personMapper.findDeviceListContainPerson(authenticateVO.getPersonId());
        LinkedMultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        // 循环向各个人脸机发送删除请求
        deviceList.forEach(device -> {
            multiValueMap.set("pass", device.getPassword());
            multiValueMap.set("id", authenticateVO.getPersonId().toString());
            RequestResult result = sendRequest.sendPostRequest(device.getIpAdress(), deletePersonUrl, multiValueMap);
            log.info("requestResult---{}", result.getMsg());
        });
        // 将对应人员分配的实验室都删除
        LambdaQueryWrapper<PersonLaboratory> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(PersonLaboratory::getPId,authenticateVO.getPersonId());
        personLaboratoryMapper.delete(wrapper);
        Person person = new Person();
        // 传入的实验室id列表为空时，只更改人员分配状态
        if (authenticateVO.getLabIdList().size() == 0){
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
            // 循环对人脸机发起请求，该双重循环不可避免
            for (Device device:devices){
                personJson.set("id", authenticateVO.getPersonId().toString());
                personJson.set("name", selectPerson.getName());
                personJson.set("iDNumber",selectPerson.getIdNumber());
                multiValueMap.set("pass", device.getPassword());
                multiValueMap.set("person", personJson);
                RequestResult result = sendRequest.sendPostRequest(device.getIpAdress(), createPersonUrl, multiValueMap);
                log.info("requestResult---{}", result.getMsg());
            }
            // 每个循环都需要创建对象
            PersonLaboratory personLaboratory = new PersonLaboratory();
            personLaboratory.setPId(authenticateVO.getPersonId());
            personLaboratory.setLabId(labId);
            personLaboratoryMapper.insert(personLaboratory);
            person.setId(authenticateVO.getPersonId());
            person.setIsDistributed((byte) 1);
            personMapper.updateById(person);
        });
        return ResultVo.success();
    }

}
