package com.example.beauty.module.activity.dto;

import lombok.Data;

@Data
public class ActivityQuery {

    private long page = 1;
    private long size = 10;

    /** 标题模糊搜索 */
    private String title;

    /** 1 进行中，0 已下线 */
    private Integer status;
}
