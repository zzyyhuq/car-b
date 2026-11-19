package com.example.beauty.module.order.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OrderCancelDTO {

    /** 取消原因，不填则按角色给默认值 */
    @Size(max = 255, message = "取消原因最多 255 个字")
    private String reason;
}
