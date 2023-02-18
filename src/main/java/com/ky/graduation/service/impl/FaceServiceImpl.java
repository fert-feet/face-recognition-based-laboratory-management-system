package com.ky.graduation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ky.graduation.entity.Face;
import com.ky.graduation.entity.Person;
import com.ky.graduation.mapper.FaceMapper;
import com.ky.graduation.mapper.PersonMapper;
import com.ky.graduation.result.ResultCode;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.IFaceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ky.graduation.utils.CosRequest;
import com.ky.graduation.vo.CosConfig;
import com.ky.graduation.vo.WeChatLoginVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.LinkedList;
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
@Slf4j
public class FaceServiceImpl extends ServiceImpl<FaceMapper, Face> implements IFaceService {

    @Resource
    private CosConfig cosConfig;

    @Resource
    private CosRequest cosRequest;

    @Resource
    private PersonMapper personMapper;

    @Resource
    private FaceMapper faceMapper;

    @Override
    public ResultVo login(WeChatLoginVO weChatLoginVO) {
        LambdaQueryWrapper<Person> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Person::getIdNumber,weChatLoginVO.getIdNumber()).eq(Person::getPassword,weChatLoginVO.getPassword());
        Person person = personMapper.selectOne(wrapper);
        if (person == null) {
            return ResultVo.error().status(ResultCode.VALIDATE_ERROR);
        }
        // 成功并返回个人信息
        return ResultVo.success().data("personInfo",person);
    }

    @Override
    public ResultVo findPersonFace(int personId) {
        LambdaQueryWrapper<Face> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Face::getPersonId,personId);
        List<Face> faceList = faceMapper.selectList(wrapper);
        return ResultVo.success().data("faceList",faceList);
    }

    @Override
    public ResultVo faceUpload(List<MultipartFile> imgList, int personId) throws IOException {
        log.info("personId---{}",personId);
        // 上传COS存储
        LinkedList<String> keyList = cosRequest.putObject(imgList);
        // 拼接人脸链接
        keyList.forEach(key -> {
            String faceUrl = cosConfig.getCosHost() + "/" + key;
            Face face = new Face();
            face.setUrl(faceUrl);
            face.setPersonId(personId);
            face.setName(key);
            // 人脸传到数据库
            if (faceMapper.insert(face) > 0){
                log.info("插入---{}",face.getFaceId());
            }
        });
        // 将是否设置人脸变为已设置
        Person person = new Person();
        person.setId(personId);
        person.setIsSetFace((byte) 1);
        personMapper.updateById(person);
        return ResultVo.success();
    }

    @Override
    public ResultVo deleteFace(int faceId, int personId) {
        Face face = faceMapper.selectById(faceId);
        if (faceMapper.deleteById(faceId) < 1){
            return ResultVo.error();
        }
        // 若该人员已经没有人脸，则改变人脸设置状态
        LambdaQueryWrapper<Face> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Face::getPersonId,personId);
        List<Face> faceList = faceMapper.selectList(wrapper);
        if (faceList.size() == 0) {
            Person person = new Person();
            person.setId(personId);
            person.setIsSetFace((byte) 0);
            personMapper.updateById(person);
        }
        // 根据key删除云端照片
        cosRequest.deleteObject(face.getName());
        return ResultVo.success();
    }
}
