package com.ky.graduation.exception;

import com.ky.graduation.result.ResultCode;
import com.ky.graduation.result.ResultVo;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.IOException;
import java.sql.SQLException;

/**
 * @author: Ky2Fe
 * @program: ky-vue-background
 * @description: 控制层异常处理
 **/

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 数据库异常
     * @param e
     * @return ResultVo
     */
    @ExceptionHandler( value = SQLException.class )
    @ResponseBody
    public ResultVo sqlException(SQLException e) {
        log.error("数据库异常---{}", e.getMessage());
        return ResultVo.error().status(ResultCode.SQL_ERROR);
    }

    /**
     * 空指针异常
     * @param e
     * @return ResultVo
     */
    @ExceptionHandler( value = NullPointerException.class)
    @ResponseBody
    public ResultVo nullPointExceptionHandler(NullPointerException e) {
        log.warn("空指针异常---{}", e.getMessage());
        return ResultVo.error().status(ResultCode.NULL_POINT_ERROR);
    }


    /**
     * 处理传入体参数校验异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = BindException.class)
    @ResponseBody
    public ResultVo methodArgumentNotValidExceptionHandler(BindException e) {
        //获取错误信息
        ObjectError bindError = e.getBindingResult().getAllErrors().get(0);
        log.error("传入体参数校验异常---{}", bindError.getDefaultMessage());
        return ResultVo.error().status(ResultCode.VALID_DATA);
    }

    /**
     * 处理传入参数校验异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = MissingServletRequestParameterException.class)
    @ResponseBody
    public ResultVo methodArgumentNotValidExceptionHandler(MissingServletRequestParameterException e) {
        //获取错误信息
        log.error("传入参数校验异常---{}", e.getMessage());
        return ResultVo.error().status(ResultCode.VALID_DATA);
    }

    /**
     * 处理HTTP异常
     *
     * @param e
     * @return
     */
    @ExceptionHandler(value = ServletException.class)
    @ResponseBody
    public ResultVo servletExceptionHandler(ServletException e) {
        //获取错误信息
        log.error("HTTP异常---{}", e.getMessage());
        return ResultVo.error().status(ResultCode.HTTP_ERROR);
    }

    /**
     * 处理传入参数空缺异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = IllegalStateException.class)
    @ResponseBody
    public ResultVo illegalStateExceptionHandler(IllegalStateException e) {
        //获取错误信息
        log.error("传入参数空缺---{}", e.getMessage());
        return ResultVo.error().status(ResultCode.PARAMS_ERROR);
    }

    /**
     * 处理传入参数异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = MethodArgumentTypeMismatchException.class)
    @ResponseBody
    public ResultVo methodArgumentTypeMismatchExceptionHandler(MethodArgumentTypeMismatchException e) {
        //获取错误信息
        log.error("传入参数类型异常---{}", e.getMessage());
        return ResultVo.error().status(ResultCode.PARAMS_TYPE_ERROR);
    }

    /**
     * 处理COS客户端异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = CosClientException.class)
    @ResponseBody
    public ResultVo cosClientExceptionHandler(CosClientException e) {
        //获取错误信息
        log.error("COS客户端异常---{}", e.getMessage());
        return ResultVo.error().status(ResultCode.COS_CLIENT_ERROR);
    }

    /**
     * 处理COS服务端异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = CosServiceException.class)
    @ResponseBody
    public ResultVo cosServiceExceptionHandler(CosServiceException e) {
        //获取错误信息
        log.error("COS服务端异常---{}", e.getMessage());
        return ResultVo.error().status(ResultCode.COS_SERVICE_ERROR);
    }

    /**
     * 处理IO异常
     * @param e
     * @return
     */
    @ExceptionHandler(value = IOException.class)
    @ResponseBody
    public ResultVo iOExceptionHandler(IOException e) {
        //获取错误信息
        log.error("IO异常---{}", e.getMessage());
        return ResultVo.error().status(ResultCode.IO_ERROR);
    }




}
