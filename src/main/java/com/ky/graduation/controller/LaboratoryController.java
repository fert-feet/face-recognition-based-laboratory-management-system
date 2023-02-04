package com.ky.graduation.controller;

import com.ky.graduation.entity.Laboratory;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.ILaboratoryService;
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
@RequestMapping("/laboratory")
public class LaboratoryController {

    @Resource
    private ILaboratoryService laboratoryService;

    /**
     * 实验室查询分页
     * @param page
     * @param limit
     * @param name
     * @param sort
     * @return
     */
    @PostMapping("/list")
    public ResultVo list(
            long page,
            long limit,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String sort) {
        return laboratoryService.listLab(page, limit, name,sort);
    }

    /**
     * 更新或新增实验室
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

}
