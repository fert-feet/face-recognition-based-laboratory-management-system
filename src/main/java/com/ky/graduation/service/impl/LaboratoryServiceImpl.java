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
import com.ky.graduation.result.ResultCode;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.ILaboratoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ky.graduation.vo.CreatePersonAuthenticationVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-01
 */
@Service
@Slf4j
public class LaboratoryServiceImpl extends ServiceImpl<LaboratoryMapper, Laboratory> implements ILaboratoryService {

    @Resource
    private LaboratoryMapper laboratoryMapper;

    @Resource
    private PersonMapper personMapper;

    @Resource
    private PersonLaboratoryMapper personLaboratoryMapper;

    /**
     * 按照id反向排列
     */
    private static final String SORT_REVERSE = "-id";

    private static final String AUTHENTICATED_SQL = "SELECT p_id FROM person_laboratory WHERE lab_id=";

    @Override
    public ResultVo listLab(long page, long limit, String name, String sort) {
        Page<Laboratory> labPage = new Page<>();
        labPage.setCurrent(page);
        labPage.setSize(limit);
        LambdaQueryWrapper<Laboratory> wrapper = Wrappers.lambdaQuery();
        if (name != null && !StringUtils.isBlank(name)) {
            wrapper.like(Laboratory::getName, name);
        }
        if (SORT_REVERSE.equals(sort)) {
            // 倒序排列
            wrapper.orderByDesc(Laboratory::getId);
        }
        // 顺序排列
        wrapper.orderByAsc(Laboratory::getId);
        Page<Laboratory> selectPage = laboratoryMapper.selectPage(labPage, wrapper);
        return ResultVo.success().data("items",selectPage.getRecords()).data("total",selectPage.getTotal());
    }

    @Override
    public ResultVo findAuthenticatedPerson(int id, long page, long limit, String name, String sort) {
        Page<Person> labPage = new Page<>();
        labPage.setCurrent(page);
        labPage.setSize(limit);
        LambdaQueryWrapper<Person> wrapper = Wrappers.lambdaQuery();
        wrapper.inSql(Person::getId,AUTHENTICATED_SQL+id);
        if (name != null && !StringUtils.isBlank(name)) {
            wrapper.like(Person::getName, name);
        }
        if (SORT_REVERSE.equals(sort)) {
            // 倒序排列
            wrapper.orderByDesc(Person::getId);
        }
        wrapper.orderByAsc(Person::getId);
        Page<Person> selectPage = personMapper.selectPage(labPage, wrapper);
        return ResultVo.success().data("items",selectPage.getRecords()).data("total",selectPage.getTotal());
    }

    @Override
    public ResultVo cancelAuthentication(PersonLaboratory personLaboratory) {
        // 邪乎，pId驼峰传不进来，需要pid才能传，我也不知道为什么
        LambdaQueryWrapper<PersonLaboratory> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(PersonLaboratory::getPId,personLaboratory.getPId()).eq(PersonLaboratory::getLabId,personLaboratory.getLabId());
        int delete = personLaboratoryMapper.delete(wrapper);
        if (delete > 0){
            return ResultVo.success();
        }
        return ResultVo.error();
    }

    @Override
    public ResultVo createAuthentication(CreatePersonAuthenticationVO createVo) {
        LambdaQueryWrapper<Person> wrapper = Wrappers.lambdaQuery();
        // 根据人名和身份证号找人员id
        wrapper.eq(Person::getName,createVo.getPersonName()).eq(Person::getIdNumber,createVo.getIdNumber());
        Person person = personMapper.selectOne(wrapper);
        if(person == null){
            return ResultVo.error().status(ResultCode.EMPTY_QUERY);
        }
        // 根据人员id和实验室id进行新增操作
        PersonLaboratory personLaboratory = new PersonLaboratory();
        personLaboratory.setPId(person.getId());
        personLaboratory.setLabId(createVo.getLabId());
        int insert = personLaboratoryMapper.insert(personLaboratory);
        if (insert > 0){
            return ResultVo.success();
        }
        return ResultVo.error();
    }
}
