package com.example.beauty.module.order.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OrderStatusDTO {

    /** 目标状态，取值见 OrderStatus；取消订单请走取消接口 */
    @NotBlank(message = "请选择目标状态")
    private String status;
}
