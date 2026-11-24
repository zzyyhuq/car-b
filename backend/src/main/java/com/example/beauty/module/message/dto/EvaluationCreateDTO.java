package com.example.beauty.module.message.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EvaluationCreateDTO {

    @NotNull(message = "请选择要评价的订单")
    private Long orderId;

    @NotNull(message = "请选择星级")
    @Min(value = 1, message = "星级最低 1 星")
    @Max(value = 5, message = "星级最高 5 星")
    private Integer rating;

    @Size(max = 1000, message = "评价内容最多 1000 个字")
    private String content;
}
