package com.example.beauty.module.servicetype.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 新增/修改服务项目共用一个 DTO，字段不多，没必要拆两个。
 */
@Data
public class ServiceTypeSaveDTO {

    @NotBlank(message = "请输入项目名称")
    @Size(max = 50, message = "项目名称最多 50 个字")
    private String name;

    @Size(max = 30, message = "分类最多 30 个字")
    private String category;

    @Size(max = 500, message = "描述最多 500 个字")
    private String description;

    @NotNull(message = "请输入价格")
    @DecimalMin(value = "0", message = "价格不能为负数")
    private BigDecimal price;

    @Min(value = 1, message = "预计时长至少 1 分钟")
    private Integer duration;

    @Size(max = 255, message = "图片地址过长")
    private String image;

    @DecimalMin(value = "0", message = "积分倍率不能为负数")
    private BigDecimal pointsRate;

    /** 1 上架，0 下架；新增时不传默认上架 */
    private Integer status;
}
