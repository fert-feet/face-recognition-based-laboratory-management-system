package com.ky.graduation.service;

import com.ky.graduation.entity.Admin;
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
public interface IAdminService extends IService<Admin> {

    /**
     * 系统登陆
     * @param username
     * @param password
     * @return
     */
    ResultVo login(String username, String password);
}
