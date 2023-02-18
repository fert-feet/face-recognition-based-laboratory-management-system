package com.ky.graduation.service;

import com.ky.graduation.entity.Face;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ky.graduation.result.ResultVo;
import com.ky.graduation.vo.WeChatLoginVO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-02-01
 */
public interface IFaceService extends IService<Face> {

    /**
     * 微信小程序登录
     * @param weChatLoginVO
     * @return
     */
    ResultVo login(WeChatLoginVO weChatLoginVO);

    /**
     * 查询人员人脸数据
     * @param personId
     * @return
     */
    ResultVo findPersonFace(int personId);

    /**
     * 人脸上传
     * @param imgList
     * @param personId
     * @return
     * @throws IOException
     */
    ResultVo faceUpload(List<MultipartFile> imgList, int personId) throws IOException;

    /**
     * 照片删除
     *
     * @param faceId
     * @param personId
     * @return
     */
    ResultVo deleteFace(int faceId, int personId);
}
