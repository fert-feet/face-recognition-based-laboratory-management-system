package com.ky.graduation.service;

import com.ky.graduation.entity.Laboratory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ky.graduation.entity.PersonLaboratory;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.vo.CreatePersonAuthenticationVO;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-01
 */
public interface ILaboratoryService extends IService<Laboratory> {

    /**
     * 实验室查询分页
     *
     * @param page
     * @param limit
     * @param name
     * @param sort
     * @return
     */
    ResultVo listLab(long page, long limit, String name, String sort);

    /**
     * 查询实验室已授权的人
     *
     * @param id
     * @param page
     * @param limit
     * @param name
     * @param sort
     * @return
     */
    ResultVo findAuthenticatedPerson(int id, long page, long limit, String name, String sort);

    /**
     * 取消实验室对某人的授权
     * @param personLaboratory
     * @return
     */
    ResultVo cancelAuthentication(PersonLaboratory personLaboratory);

    /**
     * 新增实验室对某人的授权
     * @param createVo
     * @return
     */
    ResultVo createAuthentication(CreatePersonAuthenticationVO createVo);

    /**
     * 查询实验室已授权人员不分页
     * @param id
     * @return
     */
    ResultVo findAuthenticatedPersonList(int id);

    /**
     * 查询实验室名称
     * @return
     */
    ResultVo listLabsName();
}
