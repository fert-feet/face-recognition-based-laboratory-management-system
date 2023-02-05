package com.ky.graduation.controller;

import com.ky.graduation.entity.Device;
import com.ky.graduation.entity.Laboratory;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.IDeviceService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-01
 */
@RestController
@RequestMapping("/device")
public class DeviceController {

    @Resource
    private IDeviceService deviceService;
    /**
     * 设备查询分页
     * @param page
     * @param limit
     * @param labName
     * @param sort
     * @return
     */
    @PostMapping("/list")
    public ResultVo list(
            long page,
            long limit,
            @RequestParam(required = false) String labName,
            @RequestParam(required = false) String sort) {
        return deviceService.listDevice(page, limit, labName,sort);
    }


}
