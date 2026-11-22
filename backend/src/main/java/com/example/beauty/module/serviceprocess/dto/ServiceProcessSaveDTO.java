package com.example.beauty.module.serviceprocess.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ServiceProcessSaveDTO {

    @NotNull(message = "请选择订单")
    private Long orderId;

    @NotBlank(message = "请选择进度节点")
    private String node;

    @Size(max = 500, message = "进度说明最多 500 个字")
    private String content;

    /** 照片地址，逗号分隔 */
    @Size(max = 2000, message = "照片地址过长")
    private String images;
}
