package com.ky.graduation.service.impl;

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
import com.ky.graduation.utils.SendDeviceRequest;
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
    private SendDeviceRequest sendDeviceRequest;
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
        // when received lab equals null or equals with origin lab, then this is an update device info request
        if (device.getLaboratoryName() == null || compareBindLaboratory(device)) {
            return updateDeviceInfo(device);
        }
        LinkedMultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        // if update device belong-lab status,then delete all data in device first
        sendDeviceRequest.deleteDevicePerson(device.getPassword(), device.getIpAdress(), "-1");
        // 取消分配实验室
        if (device.getLaboratoryName().equals("")) {
            return cancelDistributeLab(device);
        }
        // change belongs lab
        if (!changeDeviceBelongsLab(device)) {
            return ResultVo.error();
        }
        return ResultVo.success();
    }

    /**
     * 更改设备所属实验室
     *
     * @param device
     * @return
     */
    private boolean changeDeviceBelongsLab(Device device) {
        // if update belongs lab, then update id at the same time
        LambdaQueryWrapper<Laboratory> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Laboratory::getName, device.getLaboratoryName());
        // update lab id according to lab name
        Laboratory laboratory = laboratoryMapper.selectOne(wrapper);
        device.setLaboratoryId(laboratory.getId());
        if (deviceMapper.updateById(device) < 1) {
            return false;
        }
        // migrate person information in newly bind lab to device meanwhile
        migratePersonInLabToDevice(device, laboratory);
        return true;
    }

    /**
     * migrate information to device
     *
     * @param device
     * @param laboratory
     * @return
     */
    private void migratePersonInLabToDevice(Device device, Laboratory laboratory) {
        // find all person under this lab
        LinkedList<Person> authenticatedPersonList = laboratoryMapper.findAuthenticatedPerson(laboratory.getId());
        // create person id and photos in device
        authenticatedPersonList.forEach(person -> {
            sendDeviceRequest.createDevicePerson(device.getPassword(), device.getIpAdress(), person);
            // fetch person all face photos and send to device to create
            List<Face> faceList = findAllPersonPhotos(person);
            // send create face request to device
            faceList.forEach(face -> {
                sendDeviceRequest.createDevicePersonFace(device.getPassword(), device.getIpAdress(), face, person);
            });
        });
    }

    /**
     * find all photos of person by person id
     *
     * @param person
     * @return
     */
    private List<Face> findAllPersonPhotos(Person person) {
        LambdaQueryWrapper<Face> faceWrapper = Wrappers.lambdaQuery();
        faceWrapper.eq(Face::getPersonId, person.getId());
        return faceMapper.selectList(faceWrapper);
    }

    /**
     * 取消分配实验室
     *
     * @param device
     */
    private ResultVo cancelDistributeLab(Device device) {
        device.setLaboratoryName(null);
        device.setLaboratoryId(null);
        if (deviceMapper.updateById(device) < 1) {
            return ResultVo.error();
        }
        return ResultVo.success();
    }

    private boolean compareBindLaboratory(Device device) {
        LambdaQueryWrapper<Device> deviceWrapper = Wrappers.lambdaQuery();
        // 匹配旧绑定实验室和新实验室
        deviceWrapper
                .eq(Device::getLaboratoryName, device.getLaboratoryName())
                .eq(Device::getId, device.getId());
        return deviceMapper.exists(deviceWrapper);
    }

    @Override
    public ResultVo deleteDevice(int id) {
        // 删除设备中所有人员信息
        Device device = deviceMapper.selectById(id);
        LinkedMultiValueMap<String, Object> multiValueMap = new LinkedMultiValueMap<>();
        multiValueMap.set("pass", device.getPassword());
        multiValueMap.set("id", "-1");
        sendDeviceRequest.sendPostRequest(device.getIpAdress(), deletePersonUrl, multiValueMap);
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
            RequestResult setPassWordRequest = sendDeviceRequest.sendPostRequest(device.getIpAdress(), setPassWordUrl, multiValueMap);
            log.info("setPassWordRequest---{}", setPassWordRequest.getMsg());
        }
        // 直接更新信息即可
        deviceMapper.updateById(device);
        return ResultVo.success();
    }

}
