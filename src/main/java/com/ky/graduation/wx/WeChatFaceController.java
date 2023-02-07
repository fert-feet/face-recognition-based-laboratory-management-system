package com.ky.graduation.wx;

import com.ky.graduation.result.ResultVo;
import com.ky.graduation.service.IFaceService;
import com.ky.graduation.vo.WeChatLoginVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-01
 */
@RestController
@RequestMapping("/face")
public class WeChatFaceController {

    @Resource
    private IFaceService faceService;

    /**
     * 微信小程序登录
     * @param weChatLoginVO
     * @return
     */
    @PostMapping("/login")
    public ResultVo login(@RequestBody WeChatLoginVO weChatLoginVO) {
        return faceService.login(weChatLoginVO);
    }
}
