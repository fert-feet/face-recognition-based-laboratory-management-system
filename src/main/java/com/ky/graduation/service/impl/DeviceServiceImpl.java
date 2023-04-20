package com.ky.graduation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
    public static final int CANCEL_CHOOSE_LAB_CODE = -1;
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
    public ResultVo getBelongLabName(int deviceId) {
        String belongLab = deviceMapper.getBelongLab(deviceId);
        return ResultVo.success().data("labName", belongLab);
    }

    @Override
    public ResultVo createOrUpdate(Device device) {
        // 为新增则直接操作数据库
        if (device.getId() == null) {
            deviceMapper.insert(device);
            return ResultVo.success();
        }

        // when received lab equals origin lab, then this is an update device info request
        if (compareBindLaboratory(device)) {
            return updateDeviceInfo(device);
        }

        // if update device belong-lab status,then delete all data in device first
        sendDeviceRequest.deleteDevicePerson(device.getPassword(), device.getIpAddress(), "-1");

        // cancel distribute lab to device
        if (device.getLaboratoryId().equals(CANCEL_CHOOSE_LAB_CODE)) {
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
        // if update belongs lab, then update labName in device at the same time
        // update lab name according to lab id
        String labName = laboratoryMapper.selectById(device.getLaboratoryId()).getName();
        device.setLaboratoryName(labName);
        if (deviceMapper.updateById(device) < 1) {
            return false;
        }
        // migrate person information in newly bind lab to device meanwhile
        migratePersonInLabToDevice(device);
        return true;
    }

    /**
     * migrate information to device
     *
     * @param device
     * @return
     */
    private void migratePersonInLabToDevice(Device device) {
        // find all person under this lab
        LinkedList<Person> authenticatedPersonList = laboratoryMapper.findAuthenticatedPerson(device.getLaboratoryId());
        // create person id and photos in device
        authenticatedPersonList.forEach(person -> {
            sendDeviceRequest.createOrUpdateDevicePerson(device.getPassword(), device.getIpAddress(), person, false);
            // fetch person all face photos and send to device to create
            List<Face> faceList = findAllPersonPhotos(person);
            // send create face request to device
            faceList.forEach(face -> {
                sendDeviceRequest.createDevicePersonFace(device.getPassword(), device.getIpAddress(), face, person);
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
        device.setLaboratoryName("N");
        device.setLaboratoryId(0);
        if (deviceMapper.updateById(device) < 1) {
            return ResultVo.error();
        }
        return ResultVo.success();
    }

    private boolean compareBindLaboratory(Device device) {
        LambdaQueryWrapper<Device> deviceWrapper = Wrappers.lambdaQuery();
        // 匹配旧绑定实验室和新实验室
        deviceWrapper
                .eq(Device::getId, device.getId())
                .eq(Device::getLaboratoryId, device.getLaboratoryId());
        return deviceMapper.exists(deviceWrapper);
    }

    @Override
    public ResultVo deleteDevice(int id) {
        // 删除设备中所有人员信息
        Device device = deviceMapper.selectById(id);
        sendDeviceRequest.deleteDevicePerson(device.getPassword(), device.getIpAddress(), "-1");

        // 删除数据库中设备字段
        if (deviceMapper.deleteById(id) > 0) {
            return ResultVo.success();
        }
        return ResultVo.error();
    }

    @Override
    public ResultVo belongLab(int deviceId) {
        Device device = deviceMapper.selectById(deviceId);
        // set lab info
        Laboratory laboratory = new Laboratory();
        laboratory.setId(device.getLaboratoryId());
        laboratory.setName(device.getLaboratoryName());
        return ResultVo.success().data("item", laboratory);
    }

    private ResultVo updateDeviceInfo(Device device) {
        // 若更改密码，则需请求设备进行更改
        Device selectDevice = deviceMapper.selectById(device.getId());
        if (!selectDevice.getPassword().equals(device.getPassword())) {
            sendDeviceRequest.updateDevicePassword(selectDevice.getIpAddress(), selectDevice.getPassword(), device.getPassword());
        }
        // 直接更新信息即可
        deviceMapper.updateById(device);
        return ResultVo.success();
    }


}
