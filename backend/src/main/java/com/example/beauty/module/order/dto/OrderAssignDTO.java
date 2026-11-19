package com.example.beauty.module.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderAssignDTO {

    @NotNull(message = "请选择员工")
    private Long staffId;
}
