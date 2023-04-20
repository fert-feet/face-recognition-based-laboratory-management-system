package com.ky.graduation.exception.myExp;

/**
 * @author : ky2fe
 * @description: face exception when upload to device
 **/

public class FaceException extends RuntimeException {
    public FaceException(Throwable e) {
        super(e.getMessage(), e);
    }

    public FaceException(String message) {
        super(message);
    }
}
