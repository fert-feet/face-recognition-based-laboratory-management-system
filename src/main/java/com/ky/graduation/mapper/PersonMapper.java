package com.ky.graduation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ky.graduation.entity.Device;
import com.ky.graduation.entity.Person;

import java.util.List;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-05
 */
public interface PersonMapper extends BaseMapper<Person> {

    /**
     * 查找该人员存在于哪些人脸机
     * @param personId
     * @return
     */
    List<Device> findDeviceListContainPerson(Integer personId);
}
