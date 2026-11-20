package com.example.beauty.module.car.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CarSaveDTO {

    @NotBlank(message = "请输入车牌号")
    @Size(max = 20, message = "车牌号最多 20 个字符")
    private String plateNo;

    @Size(max = 50, message = "品牌最多 50 个字")
    private String brand;

    @Size(max = 50, message = "型号最多 50 个字")
    private String model;

    @Size(max = 20, message = "颜色最多 20 个字")
    private String color;
}
