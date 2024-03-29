package com.ky.graduation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ky.graduation.entity.Device;
import com.ky.graduation.result.ResultVo;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-01
 */
public interface IDeviceService extends IService<Device> {

    /**
     * 设备查询分页
     *
     * @param page
     * @param limit
     * @param labName
     * @param sort
     * @return
     */
    ResultVo listDevice(long page, long limit, String labName, String sort);

    /**
     * 获取所属实验室名称
     *
     * @param deviceId
     * @return
     */
    ResultVo getBelongLabName(int deviceId);

    /**
     * 更新或新增设备
     *
     * @param device
     * @return
     */
    ResultVo createOrUpdate(Device device);

    /**
     * 设备删除
     *
     * @param id
     * @return
     */
    ResultVo deleteDevice(int id);

    /**
     * find belongs lab info
     *
     * @param deviceId
     * @return
     */
    ResultVo belongLab(int deviceId);
}
