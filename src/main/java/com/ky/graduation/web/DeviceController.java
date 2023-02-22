package com.ky.graduation.web;

import com.ky.graduation.entity.Device;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.IDeviceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.regex.Pattern;

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
@Slf4j
public class DeviceController {

    public static final int Origin_IpLeng_th = 16;

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

    /**
     * 更新或新增设备
     * @param device
     * @return
     */
    @PostMapping("/createOrUpdate")
    public ResultVo create(@RequestBody Device device) {
        if (device.getIpAdress().length() <= Origin_IpLeng_th) {
            device.setIpAdress("http://" + device.getIpAdress() + ":8090");
        }
        log.info("deviceIpLength---{}", device.getIpAdress().length());
        log.info("deviceIp---{}", device.getIpAdress());
        return deviceService.createOrUpdate(device);
    }

    /**
     * 设备删除
     * @param id
     * @return
     */
    @PostMapping("/delete")
    public ResultVo delete(int id) {
        if (deviceService.removeById(id)) {
            return ResultVo.success();
        }
        return ResultVo.error();
    }

    /**
     * 获取所属实验室名称
     * @return
     */
    @PostMapping("/belongLab")
    public ResultVo belongLab(int deviceId) {
        return deviceService.getBelongLab(deviceId);
    }

}
