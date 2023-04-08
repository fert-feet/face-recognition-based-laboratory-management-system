package com.ky.graduation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ky.graduation.entity.*;
import com.ky.graduation.mapper.DeviceMapper;
import com.ky.graduation.mapper.LaboratoryMapper;
import com.ky.graduation.mapper.PersonLaboratoryMapper;
import com.ky.graduation.mapper.PersonMapper;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.ILaboratoryService;
import com.ky.graduation.utils.SendDeviceRequest;
import com.ky.graduation.vo.CreatePersonAuthenticationVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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
public class LaboratoryServiceImpl extends ServiceImpl<LaboratoryMapper, Laboratory> implements ILaboratoryService {

    /**
     * 按照id反向排列
     */
    private static final String SORT_REVERSE = "-id";
    private static final String AUTHENTICATED_SQL = "SELECT p_id FROM person_laboratory WHERE lab_id=";
    @Resource
    private LaboratoryMapper laboratoryMapper;
    @Resource
    private PersonMapper personMapper;
    @Resource
    private PersonLaboratoryMapper personLaboratoryMapper;
    @Resource
    private DeviceMapper deviceMapper;

    @Resource
    private SendDeviceRequest sendDeviceRequest;

    @Override
    public ResultVo listLab(long page, long limit, String name, String sort) {
        Page<Laboratory> labPage = new Page<>();
        labPage.setCurrent(page);
        labPage.setSize(limit);
        LambdaQueryWrapper<Laboratory> wrapper = Wrappers.lambdaQuery();
        if (name != null && !StringUtils.isBlank(name)) {
            // 根据实验室名称查询
            wrapper.like(Laboratory::getName, name);
        }
        if (SORT_REVERSE.equals(sort)) {
            // 倒序排列
            wrapper.orderByDesc(Laboratory::getId);
        }
        // 顺序排列
        wrapper.orderByAsc(Laboratory::getId);
        Page<Laboratory> selectPage = laboratoryMapper.selectPage(labPage, wrapper);
        return ResultVo.success().data("items", selectPage.getRecords()).data("total", selectPage.getTotal());
    }

    @Override
    public ResultVo findAuthenticatedPerson(int id, long page, long limit, String name, String sort) {
        Page<Person> labPage = new Page<>();
        labPage.setCurrent(page);
        labPage.setSize(limit);
        LambdaQueryWrapper<Person> wrapper = Wrappers.lambdaQuery();
        wrapper.inSql(Person::getId, AUTHENTICATED_SQL + id);
        if (name != null && !StringUtils.isBlank(name)) {
            wrapper.like(Person::getName, name);
        }
        if (SORT_REVERSE.equals(sort)) {
            // 倒序排列
            wrapper.orderByDesc(Person::getId);
        }
        wrapper.orderByAsc(Person::getId);
        Page<Person> selectPage = personMapper.selectPage(labPage, wrapper);
        return ResultVo.success().data("items", selectPage.getRecords()).data("total", selectPage.getTotal());
    }

    @Override
    public ResultVo cancelAuthentication(PersonLaboratory personLaboratory) {
        // find person that authorized by lab
        List<PersonLaboratory> personLaboratoryList = fetchPersonLabList(personLaboratory);
        int deleteNum = deletePersonLabRelationship(personLaboratory);

        // check if deleted labs was the last lab that authorize to person
        // if not, then just return
        if (deleteNum > 0 && personLaboratoryList.size() > deleteNum) {
            return ResultVo.success();
        }

        // yes, then change person distributed status
        if (deleteNum > 0 && personLaboratoryList.size() == deleteNum) {
            if (changePersonDistributedStatus(personLaboratory.getPId(), false)) {
                return ResultVo.success();
            }
        }

        // delete person in device belongs to lab
        raiseDeletePersonReqInDevice(personLaboratory.getPId(), personLaboratory.getLabId());
        return ResultVo.error();
    }

    /**
     * raise device delete person request
     *
     * @param pId
     * @param labId
     */
    private void raiseDeletePersonReqInDevice(Integer pId, Integer labId) {
        List<Device> deviceList = findBindDeviceList(labId);
        deviceList.forEach(device -> {
            sendDeviceRequest.deleteDevicePerson(device.getPassword(), device.getIpAddress(), String.valueOf(pId));
        });
    }

    private boolean changePersonDistributedStatus(Integer pId, boolean distributedStatus) {
        Person person = new Person();
        person.setId(pId);

        // set person distributed status
        person.setIsDistributed((byte) 0);
        if (distributedStatus) {
            person.setIsDistributed((byte) 1);
        }

        return personMapper.updateById(person) >= 1;
    }

    /**
     * delete the relationship of person and lab
     *
     * @param personLaboratory
     * @return
     */
    private int deletePersonLabRelationship(PersonLaboratory personLaboratory) {
        LambdaQueryWrapper<PersonLaboratory> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(PersonLaboratory::getPId, personLaboratory.getPId());
        wrapper.eq(PersonLaboratory::getLabId, personLaboratory.getLabId());

        return personLaboratoryMapper.delete(wrapper);
    }

    /**
     * find person that authorized by lab
     *
     * @param personLaboratory
     * @return
     */
    private List<PersonLaboratory> fetchPersonLabList(PersonLaboratory personLaboratory) {
        LambdaQueryWrapper<PersonLaboratory> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(PersonLaboratory::getPId, personLaboratory.getPId());
        return personLaboratoryMapper.selectList(wrapper);
    }

    @Override
    public ResultVo createAuthentication(CreatePersonAuthenticationVO createVo) {
        // change person distributed status and create new relationship between person and lab
        if (!(changePersonDistributedStatus(createVo.getPersonId(), true) && createPersonLabRelationShip(createVo.getPersonId(), createVo.getLabId()))) {
            return ResultVo.error();
        }
        List<Device> deviceList = findBindDeviceList(createVo.getLabId());
        raiseCreatePersonReqInDevice(deviceList, createVo.getPersonId());
        return ResultVo.success();
    }

    /**
     * raise device request to create person information in device
     *
     * @param deviceList
     * @param personId
     */
    private void raiseCreatePersonReqInDevice(List<Device> deviceList, Integer personId) {
        Person person = personMapper.selectById(personId);
        List<Face> faces = personMapper.findFacesOfPerson(personId);

        deviceList.forEach(device -> {
            sendDeviceRequest.createOrUpdateDevicePerson(device.getPassword(), device.getIpAddress(), person, false);
            // append face photos of person into device meanwhile
            appendPersonPhotosIntoDevice(device, faces, person);
        });
    }

    /**
     * append photos into device
     *
     * @param device
     * @param faces
     * @param person
     */
    private void appendPersonPhotosIntoDevice(Device device, List<Face> faces, Person person) {
        faces.forEach(face -> {
            // raise append photos request to device
            sendDeviceRequest.createDevicePersonFace(device.getPassword(), device.getIpAddress(), face, person);
        });
    }

    /**
     * find devices belongs to lab
     *
     * @param labId
     * @return
     */
    private List<Device> findBindDeviceList(Integer labId) {
        LambdaQueryWrapper<Device> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Device::getLaboratoryId, labId);
        return deviceMapper.selectList(wrapper);
    }

    /**
     * create new relationship of person and lab
     *
     * @param personId
     * @param labId
     */
    private boolean createPersonLabRelationShip(Integer personId, Integer labId) {
        PersonLaboratory personLaboratory = new PersonLaboratory();

        personLaboratory.setPId(personId);
        personLaboratory.setLabId(labId);

        return personLaboratoryMapper.insert(personLaboratory) >= 1;
    }


    @Override
    public ResultVo findAuthenticatedPersonList(int id) {
        LambdaQueryWrapper<Person> wrapper = Wrappers.lambdaQuery();
        wrapper.inSql(Person::getId, AUTHENTICATED_SQL + id);
        List<Person> personList = personMapper.selectList(wrapper);
        log.info(personList.toString());
        return ResultVo.success().data("personList", personList);
    }

    @Override
    public ResultVo listLabsName() {
        List<String> labNameList = laboratoryMapper.listLabsName();
        return ResultVo.success().data("labNameList", labNameList);
    }

    @Override
    public ResultVo deviceList(String labName) {
        LambdaQueryWrapper<Device> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Device::getLaboratoryName, labName);
        List<Device> deviceList = deviceMapper.selectList(wrapper);
        return ResultVo.success().data("deviceList", deviceList);
    }
}
