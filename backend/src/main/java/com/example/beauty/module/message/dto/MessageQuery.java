package com.example.beauty.module.message.dto;

import lombok.Data;

@Data
public class MessageQuery {

    private long page = 1;
    private long size = 10;

    /** EVALUATION / FEEDBACK */
    private String type;

    /** 0 待审核，1 已通过，2 已隐藏 */
    private Integer status;

    /** 内容模糊搜索 */
    private String keyword;

    private Long userId;

    private Long orderId;
}
