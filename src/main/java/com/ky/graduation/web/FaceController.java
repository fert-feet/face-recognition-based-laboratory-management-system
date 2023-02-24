package com.ky.graduation.web;

import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.IFaceService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 前端控制器
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
     *
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
     * @param imgList
     * @param personId
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    public ResultVo faceUpload(List<MultipartFile> imgList, int personId) throws IOException {
        return faceService.faceUpload(imgList, personId);
    }

    /**
     * 照片删除
     *
     * @param faceId
     * @param personId
     * @return
     */
    @PostMapping("/delete")
    public ResultVo delete(int faceId, int personId) {
        return faceService.deleteFace(faceId, personId);
    }
}
