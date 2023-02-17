package com.ky.graduation.web;

import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.IFaceService;
import com.ky.graduation.service.IPersonService;
import com.ky.graduation.utils.CosRequest;
import com.qcloud.cos.model.PutObjectResult;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-13
 */
@RestController
@RequestMapping("/face")
public class FaceController {

    @Resource
    private IFaceService faceService;

    /**
     * 查询人员人脸数据
     * @param personId
     * @return
     */
    @GetMapping("/list")
    public ResultVo findPersonFace(int personId) {
        return faceService.findPersonFace(personId);
    }

    /**
     * 人脸上传
     *
     * @param img
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    public ResultVo faceUpload(MultipartFile img, int personId) throws IOException {
        return faceService.faceUpload(img, personId);
    }

    /**
     * 照片删除
     * @param faceId
     * @param personId
     * @return
     */
    @PostMapping("/delete")
    public ResultVo delete(int faceId,int personId) {
        return faceService.deleteFace(faceId,personId);
    }
}
