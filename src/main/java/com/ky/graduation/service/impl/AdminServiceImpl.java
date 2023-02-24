package com.ky.graduation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ky.graduation.entity.Admin;
import com.ky.graduation.mapper.AdminMapper;
import com.ky.graduation.result.ResultCode;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.IAdminService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-01
 */
@Service
public class AdminServiceImpl extends ServiceImpl<AdminMapper, Admin> implements IAdminService {

    @Resource
    private AdminMapper adminMapper;

    @Override
    public ResultVo login(Admin admin) {
        LambdaQueryWrapper<Admin> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Admin::getUsername, admin.getUsername());
        Admin user = adminMapper.selectOne(wrapper);
        if (user == null || !user.getPassword().equals(admin.getPassword())) {
            return ResultVo.error().status(ResultCode.VALIDATE_ERROR);
        }
        return ResultVo.success();
    }
}
