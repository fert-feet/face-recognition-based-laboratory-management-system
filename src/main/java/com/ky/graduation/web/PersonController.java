package com.ky.graduation.web;

import com.ky.graduation.entity.Person;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.IPersonService;
import com.ky.graduation.vo.AuthenticateLabToPersonVO;
import jakarta.annotation.Resource;
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
@RequestMapping("/person")
public class PersonController {

    @Resource
    private IPersonService personService;

    /**
     * 人员查询分页
     *
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
        return personService.listPerson(page, limit, name, sort);
    }

    /**
     * list person and no split in page
     *
     * @return
     */
    @GetMapping("/listNoPage")
    public ResultVo listNoPage() {
        return ResultVo.success().data("items", personService.list());
    }

    /**
     * 人员注册或更新
     *
     * @param person
     * @return
     */
    @PostMapping("/createOrUpdate")
    public ResultVo create(@RequestBody Person person) {
        return personService.createOrUpdate(person);
    }

    /**
     * 人员删除功能
     *
     * @param id
     * @return
     */
    @PostMapping("/delete")
    public ResultVo delete(int id) {
        return personService.deletePerson(id);
    }

    /**
     * 查询已授权给该人员的实验室
     *
     * @param id
     * @return
     */
    @PostMapping("/authenticated")
    public ResultVo authenticatedLab(int id) {
        return personService.findAuthenticatedLab(id);
    }

    /**
     * 查询已授权给该人员的实验室 ID
     *
     * @param id
     * @return
     */
    @GetMapping("/authenticatedLabIds")
    public ResultVo authenticatedLabId(@RequestParam(name = "personId") int id) {
        return personService.findAuthenticatedLabIds(id);
    }

    /**
     * 新增多个实验室对人员的许可
     *
     * @param authenticateVo
     * @return
     */
    @PostMapping("/authenticateLabToPerson")
    public ResultVo authenticateToPerson(@RequestBody AuthenticateLabToPersonVO authenticateVo) {
        return personService.authenticateToPerson(authenticateVo);
    }

}
