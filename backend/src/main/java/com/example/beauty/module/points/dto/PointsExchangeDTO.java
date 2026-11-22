package com.example.beauty.module.points.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PointsExchangeDTO {

    @NotNull(message = "请选择要兑换的商品")
    private Long goodsId;
}
