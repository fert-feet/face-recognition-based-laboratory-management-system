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
    public ResultVo faceUpload(MultipartFile img, int personId) throws IOException {
        // 上传COS存储
        String key = cosRequest.putObject(img);
        // 拼接人脸链接
        String faceUrl = cosConfig.getCosHost() + "/" + key;
        Face face = new Face();
        face.setImgUrl(faceUrl);
        face.setPersonId(personId);
        face.setImgKey(key);
        // 人脸传到数据库
        if (faceMapper.insert(face) > 0){
            // 对接人脸机需要id
            log.info("插入---{}",face.getFaceId());
            return ResultVo.success().data("faceUrl",faceUrl).data("faceKey",key).data("faceId",face.getFaceId());
        }
        return ResultVo.error();
    }

    @Override
    public ResultVo deleteFace(int faceId) {
        Face face = faceMapper.selectById(faceId);
        if (faceMapper.deleteById(faceId) <= 0){
            return ResultVo.error();
        }
        // 根据key删除云端照片
        cosRequest.deleteObject(face.getImgKey());
        return ResultVo.success();
    }
}
