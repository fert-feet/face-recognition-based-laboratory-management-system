package com.ky.graduation.service.impl;

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
import com.ky.graduation.utils.SendDeviceRequest;
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
    private SendDeviceRequest sendDeviceRequest;
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
        deletePersonInAllDevice(authenticateVO);
        // 将对应人员分配的实验室都删除
        if (!deleteLabDistributedToPerson(authenticateVO)) {
            return ResultVo.error();
        }
        // 传入的实验室id列表为空时，只更改人员分配状态
        if (authenticateVO.getLabIdList().size() == 0) {
            if (!changePersonDistributeStatus(authenticateVO, (byte) 0)) {
                return ResultVo.error();
            }
        }
        // id列表不为空时，循环插入数据库，请求各人脸机新增人员
        authenticateVO.getLabIdList().forEach(labId -> {
            // append person into device
            appendPersonIntoDevices(authenticateVO, labId);
            // append person-lab relationship into db
            appendRelationship(authenticateVO, labId);
            // change distributed status
            changePersonDistributeStatus(authenticateVO, (byte) 1);
        });
        return ResultVo.success();
    }

    /**
     * append person-lab relationship
     *
     * @param authenticateVO
     * @param labId
     */
    private boolean appendRelationship(AuthenticateLabToPersonVO authenticateVO, Integer labId) {
        PersonLaboratory personLaboratory = new PersonLaboratory();
        // 添加人员与实验室授权关系
        personLaboratory.setPId(authenticateVO.getPersonId());
        personLaboratory.setLabId(labId);
        return personLaboratoryMapper.insert(personLaboratory) >= 1;
    }

    /**
     * append person information into devices
     *
     * @param authenticateVO
     * @param labId
     */
    private void appendPersonIntoDevices(AuthenticateLabToPersonVO authenticateVO, Integer labId) {
        // find devices that bind lab
        List<Device> devices = findDevicesOfLab(labId);
        // 查询该人员信息
        Person person = personMapper.selectById(authenticateVO.getPersonId());
        // 查询人员照片
        List<Face> faces = findFacesOfPerson(authenticateVO.getPersonId());
        deviceAppendPerson(devices, faces, person);
    }

    /**
     * request to device of append person into device
     *
     * @param devices
     * @param faces
     * @param person
     */
    private void deviceAppendPerson(List<Device> devices, List<Face> faces, Person person) {
        devices.forEach(device -> {
            // append person into device
            sendDeviceRequest.createOrUpdateDevicePerson(device.getPassword(), device.getIpAddress(), person, false);
            // 遍历人脸照片列表，添加到每个人脸机，双重循环不可避免
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
     * find all photos info of person
     *
     * @param personId
     * @return
     */
    private List<Face> findFacesOfPerson(Integer personId) {
        LambdaQueryWrapper<Face> faceWrapper = Wrappers.lambdaQuery();
        faceWrapper.eq(Face::getPersonId, personId);
        return faceMapper.selectList(faceWrapper);
    }

    /**
     * find all devices of lab
     *
     * @param labId
     * @return
     */
    private List<Device> findDevicesOfLab(Integer labId) {
        LambdaQueryWrapper<Device> query = Wrappers.lambdaQuery();
        query.eq(Device::getLaboratoryId, labId);
        // 找到该每个实验室对应的人脸机
        return deviceMapper.selectList(query);
    }

    /**
     * change person distributed status
     *
     * @param authenticateVO
     */
    private boolean changePersonDistributeStatus(AuthenticateLabToPersonVO authenticateVO, byte distributedStatus) {
        Person person = new Person();
        person.setId(authenticateVO.getPersonId());
        person.setIsDistributed(distributedStatus);
        return personMapper.updateById(person) >= 1;
    }

    /**
     * delete all relationship of person and lab
     *
     * @param authenticateVO
     */
    private boolean deleteLabDistributedToPerson(AuthenticateLabToPersonVO authenticateVO) {
        LambdaQueryWrapper<PersonLaboratory> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(PersonLaboratory::getPId, authenticateVO.getPersonId());
        return personLaboratoryMapper.delete(wrapper) >= 0;
    }

    /**
     * delete person info in all device
     *
     * @param authenticateVO
     */
    private void deletePersonInAllDevice(AuthenticateLabToPersonVO authenticateVO) {
        List<Device> deviceList = personMapper.findDeviceListContainPerson(authenticateVO.getPersonId());
        LinkedMultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        deviceList.forEach(device -> {
            // delete person and photos in device
            sendDeviceRequest.deleteDevicePerson(device.getPassword(), device.getIpAddress(), authenticateVO.getPersonId().toString());
        });
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
            updatePeronInDevice(person);
        }
        // 设置默认密码
        person.setPassword(person.getIdNumber());
        if (personMapper.updateById(person) < 1) {
            return ResultVo.error();
        }
        return ResultVo.success();
    }

    /**
     * raise update person request
     *
     * @param person
     */
    private void updatePeronInDevice(Person person) {
        // find all device that contain this person
        List<Device> deviceList = personMapper.findDeviceListContainPerson(person.getId());
        raiseUpdatePersonRequest(person, deviceList);
    }

    /**
     * raise update person info in device
     *
     * @param person
     * @param deviceList
     */
    private void raiseUpdatePersonRequest(Person person, List<Device> deviceList) {
        // 循环请求请求人脸机进行更新
        deviceList.forEach(device -> {
            sendDeviceRequest.createOrUpdateDevicePerson(device.getPassword(), device.getIpAddress(), person, true);
        });
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
                RequestResult deletePersonRequest = sendDeviceRequest.sendPostRequest(device.getIpAddress(), deletePersonUrl, multiValueMap);
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

    @Override
    public ResultVo findAuthenticatedLabIds(int id) {
        List<Integer> authenticatedLabIds = laboratoryMapper.findAuthenticatedLabIds(id);
        return ResultVo.success().data("authenticatedLabIds", authenticatedLabIds);
    }

}
