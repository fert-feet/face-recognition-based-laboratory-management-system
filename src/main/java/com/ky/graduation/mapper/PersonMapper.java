package com.ky.graduation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ky.graduation.entity.Device;
import com.ky.graduation.entity.Face;
import com.ky.graduation.entity.Person;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-05
 */
public interface PersonMapper extends BaseMapper<Person> {

    /**
     * 查找该人员存在于哪些人脸机
     *
     * @param personId
     * @return
     */
    LinkedList<Device> findDeviceListContainPerson(Integer personId);

    /**
     * find all photos info of person
     *
     * @param personId
     * @return
     */
    List<Face> findFacesOfPerson(Integer personId);
}
