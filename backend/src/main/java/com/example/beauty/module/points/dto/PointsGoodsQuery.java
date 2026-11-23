package com.example.beauty.module.points.dto;

import lombok.Data;

@Data
public class PointsGoodsQuery {

    private long page = 1;
    private long size = 10;

    /** 商品名称模糊搜索 */
    private String name;

    /** 1 上架，0 下架 */
    private Integer status;
}
