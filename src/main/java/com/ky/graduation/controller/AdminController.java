package com.ky.graduation.controller;

import com.ky.graduation.result.ResultVo;
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
 * @since 2023-02-01
 */
@RestController
@RequestMapping("/admin")
public class AdminController {
    @PostMapping("/test")
    public ResultVo test(String url) {
        if (url != null) {
            return ResultVo.success().data("url",url);
        }
        return ResultVo.error();
    }

}
