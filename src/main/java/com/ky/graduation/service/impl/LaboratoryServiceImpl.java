package com.ky.graduation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ky.graduation.entity.Laboratory;
import com.ky.graduation.entity.Person;
import com.ky.graduation.mapper.LaboratoryMapper;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.ILaboratoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-01
 */
@Service
public class LaboratoryServiceImpl extends ServiceImpl<LaboratoryMapper, Laboratory> implements ILaboratoryService {

    @Resource
    private LaboratoryMapper laboratoryMapper;

    /**
     * 按照id反向排列
     */
    private static final String SORT_REVERSE = "-id";

    @Override
    public ResultVo listLab(long page, long limit, String name, String sort) {
        Page<Laboratory> labPage = new Page<>();
        labPage.setCurrent(page);
        labPage.setSize(limit);
        LambdaQueryWrapper<Laboratory> wrapper = Wrappers.lambdaQuery();
        if (name != null && !StringUtils.isBlank(name)) {
            wrapper.like(Laboratory::getName, name);
        }
        if (sort != null && SORT_REVERSE.equals(sort)) {
            // 倒序排列
            wrapper.orderByDesc(Laboratory::getId);
        }
        // 顺序排列
        wrapper.orderByAsc(Laboratory::getId);
        Page<Laboratory> selectPage = laboratoryMapper.selectPage(labPage, wrapper);
        return ResultVo.success().data("items",selectPage.getRecords()).data("total",selectPage.getTotal());
    }

    @Override
    public ResultVo findAuthenticatedPerson(int id) {
        List<Person> personList = laboratoryMapper.findAuthenticatedPerson(id);
        return ResultVo.success().data("total",personList.size()).data("authenticatedPerson",personList);
    }
}
