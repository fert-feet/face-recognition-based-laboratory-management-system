package com.ky.graduation.service;

import com.ky.graduation.entity.Laboratory;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ky.graduation.result.ResultVo;

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
}
