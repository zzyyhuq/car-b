package com.example.beauty.module.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车主预约下单。金额由后端按服务项目价格计算，前端传的价格一律不信任。
 */
@Data
public class OrderCreateDTO {

    @NotNull(message = "请选择车辆")
    private Long carId;

    @NotNull(message = "请选择服务项目")
    private Long serviceTypeId;

    /** 预约到店时间，可不填 */
    private LocalDateTime appointmentTime;

    @Size(max = 255, message = "备注最多 255 个字")
    private String remark;
}
