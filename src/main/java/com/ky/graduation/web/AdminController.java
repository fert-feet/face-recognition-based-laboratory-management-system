package com.ky.graduation.web;

import com.ky.graduation.entity.Admin;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.IAdminService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-01
 */
@RestController
@RequestMapping("/user")
@Slf4j
public class AdminController {

    @Resource
    private IAdminService adminService;

    /**
     * 系统登录
     *
     * @param admin
     * @return
     */
    @PostMapping("/login")
    public ResultVo login(@RequestBody Admin admin) {
        return adminService.login(admin);
    }

    /**
     * 登出
     *
     * @return
     */
    @PostMapping("/logout")
    public ResultVo logout() {
        return ResultVo.success();
    }
}
