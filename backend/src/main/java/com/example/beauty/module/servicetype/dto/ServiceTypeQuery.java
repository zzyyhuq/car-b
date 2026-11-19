package com.example.beauty.module.servicetype.dto;

import lombok.Data;

@Data
public class ServiceTypeQuery {

    private long page = 1;
    private long size = 10;

    /** 项目名称模糊搜索 */
    private String name;

    private String category;

    /** 1 上架，0 下架 */
    private Integer status;
}
