package com.ky.graduation.web;

import com.ky.graduation.entity.Device;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.IDeviceService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-01
 */
@RestController
@RequestMapping("/device")
@Slf4j
public class DeviceController {

    @Resource
    private IDeviceService deviceService;

    /**
     * 设备查询分页
     *
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
        return deviceService.listDevice(page, limit, labName, sort);
    }

    /**
     * 更新或新增设备
     *
     * @param device
     * @return
     */
    @PostMapping("/createOrUpdate")
    public ResultVo create(@RequestBody Device device) {
        return deviceService.createOrUpdate(device);
    }

    /**
     * 设备删除
     *
     * @param id
     * @return
     */
    @PostMapping("/delete")
    public ResultVo delete(int id) {
        return deviceService.deleteDevice(id);
    }

    /**
     * 获取所属实验室名称
     *
     * @return
     */
    @PostMapping("/belongLabName")
    public ResultVo belongLabName(int deviceId) {
        return deviceService.getBelongLabName(deviceId);
    }

    /**
     * get belongs lab
     *
     * @param deviceId
     */
    @GetMapping("/belongLab")
    public ResultVo belongLab(int deviceId) {
        return deviceService.belongLab(deviceId);
    }

}
