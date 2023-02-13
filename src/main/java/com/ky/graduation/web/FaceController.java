package com.ky.graduation.web;

import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.IFaceService;
import com.ky.graduation.service.IPersonService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
