package com.ky.graduation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ky.graduation.entity.Laboratory;
import com.ky.graduation.entity.Person;
import org.apache.ibatis.annotations.Param;

import java.util.LinkedList;
import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-01
 */
public interface LaboratoryMapper extends BaseMapper<Laboratory> {

    /**
     * 查询实验室已授权的人
     *
     * @param id
     * @return
     */
    LinkedList<Person> findAuthenticatedPerson(@Param(value = "labId") int id);

    /**
     * 查询实验室名称
     *
     * @return
     */
    List<String> listLabsName();
}
