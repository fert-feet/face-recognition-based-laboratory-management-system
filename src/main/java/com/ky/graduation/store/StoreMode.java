package com.ky.graduation.store;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @作者: ky2fe
 * @描述: set store mode enum
 **/

@Getter
@AllArgsConstructor
public enum StoreMode {

    /**
     * 上传 COS 服务器
     */
    COS("COS"),

    /**
     * 本地存储
     */
    LOCAL("LOCAL");

    private final String mode;
}
