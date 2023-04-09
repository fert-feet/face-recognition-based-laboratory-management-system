package com.ky.graduation.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ky.graduation.dto.FaceImgLocalStoreDTO;
import com.ky.graduation.entity.Device;
import com.ky.graduation.entity.Face;
import com.ky.graduation.entity.Person;
import com.ky.graduation.mapper.FaceMapper;
import com.ky.graduation.mapper.PersonMapper;
import com.ky.graduation.result.ResultCode;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.IFaceService;
import com.ky.graduation.utils.CosRequest;
import com.ky.graduation.utils.FaceImgLocalStoreUtil;
import com.ky.graduation.utils.SendDeviceRequest;
import com.ky.graduation.vo.CosConfig;
import com.ky.graduation.vo.WeChatLoginVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * <p>
 * 服务实现类
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

    public static final String COS = "COS";

    @Resource
    private PersonMapper personMapper;

    @Resource
    private FaceMapper faceMapper;

    @Resource
    private SendDeviceRequest sendDeviceRequest;
    public static final String LOCAL = "LOCAL";
    @Resource
    private FaceImgLocalStoreUtil storeUtil;
    @Value("${pictureUploadOption.faceImgStoreMode}")
    private String storeMode;

    @Override
    public ResultVo login(WeChatLoginVO weChatLoginVO) {
        // 查询账号密码是否正确
        LambdaQueryWrapper<Person> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Person::getIdNumber, weChatLoginVO.getIdNumber()).eq(Person::getPassword, weChatLoginVO.getPassword());
        Person person = personMapper.selectOne(wrapper);
        if (person == null) {
            return ResultVo.error().status(ResultCode.VALIDATE_ERROR);
        }
        // 成功并返回个人信息
        return ResultVo.success().data("personInfo", person);
    }

    @Override
    public ResultVo findPersonFace(int personId) {
        LambdaQueryWrapper<Face> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Face::getPersonId, personId);
        List<Face> faceList = faceMapper.selectList(wrapper);
        return ResultVo.success().data("faceList", faceList);
    }

    @Override
    public ResultVo faceUpload(List<MultipartFile> imgList, int personId) throws IOException, SQLException {
        List<MultipartFile> imgFileList = Optional.ofNullable(imgList).orElse(List.of());

        // change upload way when upload-flag change, default upload mode is local
        if (COS.equals(storeMode)) {
            return uploadFaceInCOSMode(imgFileList, personId);
        }
        return uploadFaceInLocalMode(imgFileList, personId);
    }

    /**
     * upload face image in local way, that store face photos
     *
     * @param imgFileList
     * @param personId
     * @return
     */
    private ResultVo uploadFaceInLocalMode(List<MultipartFile> imgFileList, int personId) throws IOException, SQLException {
        // TODO need to complete local mode
        return ResultVo.success();
    }

    /**
     * store face image in db and device
     *
     * @param faceLocalStoreDTO
     * @param personId
     */
    private void storeFaceImg(FaceImgLocalStoreDTO faceLocalStoreDTO, int personId) throws SQLException {
    }

    /**
     * store face photos base64 encode into device
     *
     * @param personId
     * @param imgBase64EncodeList
     */
    private void storeFaceImgInDevice(int personId, List<String> imgBase64EncodeList) {
        for (String imgEncode : imgBase64EncodeList) {
        }
    }

    /**
     * store img bytes in local db with longBlob type
     *
     * @param personId
     * @param imgByteEncodeList
     */
    private boolean storeFaceImgInDB(int personId, List<byte[]> imgByteEncodeList) throws SQLException {
        for (byte[] imgBytes : imgByteEncodeList) {
            Face face = new Face();
            face.setPersonId(personId);

            // insert into db
            if (faceMapper.insert(face) != 1) {
                throw new SQLException("face insert error!");
            }
        }
        return true;
    }

    /**
     * upload face in COS way, that send face image to cos server
     *
     * @param imgFileList
     * @param personId
     */
    private ResultVo uploadFaceInCOSMode(List<MultipartFile> imgFileList, int personId) throws IOException {
        // 上传COS存储
        LinkedList<String> keyList = cosRequest.putObject(imgFileList);
        // upload face to device
        uploadFaceToDevice(keyList, personId);
        // 将是否设置人脸变为已设置
        if (!changePersonPhotosStatus(personId, (byte) 1)) {
            return ResultVo.error();
        }
        return ResultVo.success();
    }

    /**
     * change isSetFace status
     *
     * @param personId
     * @param setPhotosStatus
     * @return
     */
    private boolean changePersonPhotosStatus(int personId, byte setPhotosStatus) {
        Person person = new Person();
        person.setId(personId);
        person.setIsSetFace(setPhotosStatus);
        return personMapper.updateById(person) >= 1;
    }

    /**
     * upload face photos to device
     *
     * @param keyList
     * @param personId
     */
    private void uploadFaceToDevice(LinkedList<String> keyList, int personId) {
        // find all devices that contain this person and do upload
        List<Device> deviceList = personMapper.findDeviceListContainPerson(personId);
        keyList.forEach(key -> {
            // 拼接人脸链接
            String faceUrl = cosConfig.getCosHost() + "/" + key;
            // upload face to db
            Face face = insertFaceIntoDB(key, faceUrl, personId);
            // raise upload request to db
            if (deviceList.size() > 0) {
                raiseFaceReqToDB(deviceList, face, buildPersonForDeviceReq(personId));
            }
        });
    }

    /**
     * raise upload face photos request to device
     *
     * @param deviceList
     * @param face
     * @param person
     */
    private void raiseFaceReqToDB(List<Device> deviceList, Face face, Person person) {
        deviceList.forEach(device -> {
            sendDeviceRequest.createDevicePersonFace(device.getPassword(), device.getPassword(), face, person);
        });
    }

    /**
     * build person object
     *
     * @param personId
     * @return
     */
    private Person buildPersonForDeviceReq(int personId) {
        Person person = new Person();
        person.setId(personId);
        return person;
    }

    /**
     * insert face into db
     *
     * @param key
     * @param faceUrl
     * @param personId
     */
    private Face insertFaceIntoDB(String key, String faceUrl, int personId) {
        Face face = new Face();
        face.setUrl(faceUrl);
        face.setPersonId(personId);
        face.setName(key);
        if (faceMapper.insert(face) < 1) {
            return null;
        }
        return face;
    }

    @Override
    public ResultVo deleteFace(int faceId, int personId) {
        //TODO change delete logic
        Face face = faceMapper.selectById(faceId);
        // 删除每个设备中这个人的此张相片
        LinkedList<Device> deviceList = personMapper.findDeviceListContainPerson(personId);
        if (deviceList.size() > 0) {
            raiseDeleteFaceReqToDB(deviceList, face);
        }
        if (faceMapper.deleteById(faceId) < 1) {
            return ResultVo.error();
        }
        // 若该人员已经没有人脸，则改变人脸设置状态
        if (checkPersonFaceStatus(personId)) {
            changePersonPhotosStatus(personId, (byte) 0);
        }
        // 根据key删除云端照片
        cosRequest.deleteObject(face.getName());
        return ResultVo.success();
    }

    /**
     * check if person still has face photos
     *
     * @param personId
     * @return
     */
    private boolean checkPersonFaceStatus(int personId) {
        LambdaQueryWrapper<Face> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(Face::getPersonId, personId);
        return faceMapper.selectList(wrapper).size() == 0;
    }

    /**
     * raise delete person face request
     *
     * @param deviceList
     * @param face
     */
    private void raiseDeleteFaceReqToDB(LinkedList<Device> deviceList, Face face) {
        deviceList.forEach(device -> {
            sendDeviceRequest.deleteDevicePersonFace(device.getPassword(), device.getIpAddress(), face.getFaceId().toString());
        });
    }
}
