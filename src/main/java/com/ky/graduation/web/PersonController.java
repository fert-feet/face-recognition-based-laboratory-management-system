package com.ky.graduation.web;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.ky.graduation.entity.Person;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.IPersonService;
import com.ky.graduation.vo.AuthenticateLabToPersonVO;
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
        // 若为新注册人员，则设置默认密码
        if (StringUtils.isEmpty(person.getPassword())){
            person.setPassword(person.getIdNumber());
        }
        if (personService.saveOrUpdate(person)){
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

    /**
     * 查询已授权给该人员的实验室
     * @param id
     * @return
     */
    @PostMapping("/authenticated")
    public ResultVo authenticatedLab(int id) {
        return personService.findAuthenticatedLab(id);
    }

    /**
     * 新增多个实验室对人员的许可
     * @param authenticateVo
     * @return
     */
    @PostMapping("/authenticateLabToPerson")
    public ResultVo authenticateToPerson(@RequestBody AuthenticateLabToPersonVO authenticateVo) {
        return personService.authenticateToPerson(authenticateVo);
    }

    /**
     * 查询人员人脸数据
     * @param personId
     * @return
     */
    @GetMapping("/face")
    public ResultVo findPersonFace(int personId) {
        return personService.findPersonFace(personId);
    }
}
