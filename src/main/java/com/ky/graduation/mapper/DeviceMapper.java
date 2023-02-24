package com.ky.graduation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ky.graduation.entity.Device;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-01
 */
public interface DeviceMapper extends BaseMapper<Device> {

    /**
     * 获取所属实验室名称
     *
     * @param deviceId
     * @return
     */
    String getBelongLab(int deviceId);
}
