package com.example.beauty.module.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 到店开单：员工/管理员替车主建单，所以比车主下单多一个用户 id。
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderAdminCreateDTO extends OrderCreateDTO {

    @NotNull(message = "请选择车主")
    private Long userId;
}
