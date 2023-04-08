package com.ky.graduation.dto;

import lombok.Data;

import java.util.List;

/**
 * @作者: ky2fe
 * @描述: for store converted data
 **/

@Data
public class FaceImgLocalStoreDTO {
    List<byte[]> imgByteEncodeList;

    List<String> imgBase64EncodeList;
}
