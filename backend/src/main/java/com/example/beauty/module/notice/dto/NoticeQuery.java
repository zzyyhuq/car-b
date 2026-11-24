package com.example.beauty.module.notice.dto;

import lombok.Data;

@Data
public class NoticeQuery {

    private long page = 1;
    private long size = 10;

    /** 标题模糊搜索 */
    private String title;

    /** 1 已发布，0 草稿 */
    private Integer status;
}
