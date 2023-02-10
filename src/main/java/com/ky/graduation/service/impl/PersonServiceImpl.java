package com.ky.graduation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ky.graduation.entity.Laboratory;
import com.ky.graduation.entity.Person;
import com.ky.graduation.entity.PersonLaboratory;
import com.ky.graduation.mapper.LaboratoryMapper;
import com.ky.graduation.mapper.PersonLaboratoryMapper;
import com.ky.graduation.mapper.PersonMapper;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.IPersonService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ky.graduation.vo.AuthenticateLabToPersonVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-05
 */
@Service
@Slf4j
public class PersonServiceImpl extends ServiceImpl<PersonMapper, Person> implements IPersonService {

    @Resource
    private PersonMapper personMapper;

    @Resource
    private LaboratoryMapper laboratoryMapper;

    @Resource
    private PersonLaboratoryMapper personLaboratoryMapper;

    private static final String SORT_REVERSE = "-id";

    private static final String AUTHENTICATED_SQL = "SELECT lab_id FROM person_laboratory WHERE p_id=";


    @Override
    public ResultVo listPerson(long page, long limit, String name, String sort) {
        Page<Person> labPage = new Page<>();
        labPage.setCurrent(page);
        labPage.setSize(limit);
        LambdaQueryWrapper<Person> wrapper = Wrappers.lambdaQuery();
        if (name != null && !StringUtils.isBlank(name)) {
            // 根据人员姓名查询
            wrapper.like(Person::getName, name);
        }
        if (SORT_REVERSE.equals(sort)) {
            // 倒序排列
            wrapper.orderByDesc(Person::getId);
        }
        // 顺序排列
        wrapper.orderByAsc(Person::getId);
        Page<Person> selectPage = personMapper.selectPage(labPage, wrapper);
        return ResultVo.success().data("items",selectPage.getRecords()).data("total",selectPage.getTotal());
    }

    @Override
    public ResultVo findAuthenticatedLab(int id) {
        LambdaQueryWrapper<Laboratory> wrapper = Wrappers.lambdaQuery();
        // IN语句查询授权给人员的实验室
        wrapper.inSql(Laboratory::getId,AUTHENTICATED_SQL+id);
        List<Laboratory> laboratories = laboratoryMapper.selectList(wrapper);
        return ResultVo.success().data("authorizedLabList",laboratories);
    }

    @Override
    public ResultVo authenticateToPerson(AuthenticateLabToPersonVO authenticateVO) {
        // 在person_laboratory中间表中添加记录
        authenticateVO.getLabIdList().stream().forEach(labId ->{
            // 每次循环都需要创建对象,不然会重复
            PersonLaboratory personLaboratory = new PersonLaboratory();
            personLaboratory.setPId(authenticateVO.getPersonId());
            personLaboratory.setLabId(labId);
            personLaboratoryMapper.insert(personLaboratory);
        });
        return ResultVo.success();
    }
}
