package com.ky.graduation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ky.graduation.entity.Device;
import com.ky.graduation.mapper.DeviceMapper;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.IDeviceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-01
 */
@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements IDeviceService {

    @Resource
    private DeviceMapper deviceMapper;

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
        if (device.getId() == null) {
            deviceMapper.insert(device);
            return ResultVo.success();
        }
        // 取消分配实验室
        if (device.getLaboratoryName() == null) {
            device.setLaboratoryName(null);
        }
        deviceMapper.updateById(device);
        return ResultVo.success();
    }

}
