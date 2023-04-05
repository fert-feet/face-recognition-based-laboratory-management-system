package com.ky.graduation.web;

import com.ky.graduation.entity.Laboratory;
import com.ky.graduation.entity.PersonLaboratory;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.ILaboratoryService;
import com.ky.graduation.vo.CreatePersonAuthenticationVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 前端控制器
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-01
 */
@RestController
@RequestMapping("/laboratory")
public class LaboratoryController {

    @Resource
    private ILaboratoryService laboratoryService;

    /**
     * 实验室查询分页
     *
     * @param page
     * @param limit
     * @param name
     * @param sort
     * @return
     */
    @PostMapping("/listPage")
    public ResultVo list(
            long page,
            long limit,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String sort) {
        return laboratoryService.listLab(page, limit, name, sort);
    }

    /**
     * 实验室不分页查询
     *
     * @return
     */
    @GetMapping("/list")
    public ResultVo list() {
        List<Laboratory> labList = laboratoryService.list();
        return ResultVo.success().data("labList", labList);
    }

    /**
     * 更新或新增实验室
     *
     * @param laboratory
     * @return
     */
    @PostMapping("/createOrUpdate")
    public ResultVo create(@RequestBody Laboratory laboratory) {
        if (laboratoryService.saveOrUpdate(laboratory)) {
            return ResultVo.success();
        }
        return ResultVo.error();
    }

    /**
     * 实验室删除功能
     *
     * @return
     */
    @PostMapping("/delete")
    public ResultVo delete(int id) {
        if (laboratoryService.removeById(id)) {
            return ResultVo.success();
        }
        return ResultVo.error();
    }

    /**
     * 查询实验室已授权的人
     *
     * @param id
     * @return
     */
    @PostMapping("/authenticated")
    public ResultVo authenticatedPerson(int id,
                                        long page,
                                        long limit,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(required = false) String sort) {
        return laboratoryService.findAuthenticatedPerson(id, page, limit, name, sort);
    }

    /**
     * 查询实验室已授权人员不分页
     *
     * @param id
     * @return
     */
    @PostMapping("/authenticatedList")
    public ResultVo authenticatedPersonList(int id) {
        return laboratoryService.findAuthenticatedPersonList(id);
    }

    /**
     * 取消本实验室对某人的授权
     *
     * @param personLaboratory
     * @return
     */
    @PostMapping("/cancelAuthentication")
    public ResultVo cancelPersonAuthentication(@RequestBody PersonLaboratory personLaboratory) {
        return laboratoryService.cancelAuthentication(personLaboratory);
    }

    /**
     * 新增本实验室对某人的授权
     *
     * @param createVo
     * @return
     */
    @PostMapping("/createAuthentication")
    public ResultVo createPersonAuthentication(@RequestBody CreatePersonAuthenticationVO createVo) {
        return laboratoryService.createAuthentication(createVo);
    }

    /**
     * 查询实验室名称
     *
     * @return
     */
    @GetMapping("/listLabsName")
    public ResultVo listLabsName() {
        return laboratoryService.listLabsName();
    }

    /**
     * 根据实验室名称查询设备
     *
     * @param labName
     * @return
     */
    @PostMapping("/deviceList")
    public ResultVo deviceList(String labName) {
        return laboratoryService.deviceList(labName);
    }

}
