package com.ky.graduation.controller;

import com.ky.graduation.entity.Person;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.IPersonService;
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
@RequestMapping("/person")
public class PersonController {

    @Resource
    private IPersonService personService;

    /**
     * 人员查询分页
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
        return personService.listPerson(page, limit, name,sort);
    }

    /**
     * 人员注册或更新
     * @param person
     * @return
     */
    @PostMapping("/createOrUpdate")
    public ResultVo create(@RequestBody Person person) {
        if (personService.saveOrUpdate(person)) {
            return ResultVo.success();
        }
        return ResultVo.error();
    }

    /**
     * 人员删除功能
     * @param id
     * @return
     */
    @PostMapping("/delete")
    public ResultVo delete(int id) {
        // 级联删除
        if (personService.removeById(id)) {
            return ResultVo.success();
        }
        return ResultVo.error();
    }

}
