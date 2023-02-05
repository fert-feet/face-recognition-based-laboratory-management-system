package com.ky.graduation.service;

import com.ky.graduation.entity.Person;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ky.graduation.result.ResultVo;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-05
 */
public interface IPersonService extends IService<Person> {

    /**
     * 人员查询分页
     * @param page
     * @param limit
     * @param name
     * @param sort
     * @return
     */
    ResultVo listPerson(long page, long limit, String name, String sort);
}
