package com.ky.graduation.controller;

import com.ky.graduation.result.ResultVo;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author Ky2Fe
 * @since 2023-01-31
 */
@RestController
@RequestMapping("/admin")
public class AdminController {
    @RequestMapping("/hello")
    public ResultVo test() {
            return ResultVo.success();
    }
}
