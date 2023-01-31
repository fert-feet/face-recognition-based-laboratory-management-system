package com.ky.graduation.service.impl;

import com.ky.graduation.entity.Person;
import com.ky.graduation.mapper.PersonMapper;
import com.ky.graduation.service.IPersonService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-01-31
 */
@Service
public class PersonServiceImpl extends ServiceImpl<PersonMapper, Person> implements IPersonService {

}
