package com.ky.graduation.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ky.graduation.entity.Person;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.vo.AuthenticateLabToPersonVO;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-05
 */
public interface IPersonService extends IService<Person> {

    /**
     * 人员查询分页
     *
     * @param page
     * @param limit
     * @param name
     * @param sort
     * @return
     */
    ResultVo listPerson(long page, long limit, String name, String sort);

    /**
     * 查询已授权给该人员的实验室
     *
     * @param id
     * @return
     */
    ResultVo findAuthenticatedLab(int id);

    /**
     * 新增多个实验室对人员的许可
     *
     * @param authenticateVO
     * @return
     */
    ResultVo authenticateToPerson(AuthenticateLabToPersonVO authenticateVO);

    /**
     * 新增或更新人员
     *
     * @param person
     * @return
     */
    ResultVo createOrUpdate(Person person);

    /**
     * 人员删除
     *
     * @param id
     * @return
     */
    ResultVo deletePerson(int id);

    /**
     * 查询已授权给该人员的实验室 id
     *
     * @param id
     * @return
     */
    ResultVo findAuthenticatedLabIds(int id);
}
