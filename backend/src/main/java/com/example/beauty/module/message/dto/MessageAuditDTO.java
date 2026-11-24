package com.example.beauty.module.message.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MessageAuditDTO {

    /** 0 待审核，1 已通过，2 已隐藏 */
    @NotNull(message = "请选择审核状态")
    private Integer status;
}
