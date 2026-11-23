package com.example.beauty.module.points.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PointsGoodsSaveDTO {

    @NotBlank(message = "请输入商品名称")
    @Size(max = 50, message = "商品名称最多 50 个字")
    private String name;

    @Size(max = 500, message = "商品描述最多 500 个字")
    private String description;

    @NotNull(message = "请输入所需积分")
    @Min(value = 1, message = "所需积分至少为 1")
    private Integer pointsPrice;

    @NotNull(message = "请输入库存")
    @Min(value = 0, message = "库存不能为负数")
    private Integer stock;

    @Size(max = 255, message = "图片地址过长")
    private String image;

    /** 1 上架，0 下架；新增时不传默认上架 */
    private Integer status;
}
