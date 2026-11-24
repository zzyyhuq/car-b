package com.example.beauty.module.activity.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActivitySaveDTO {

    @NotBlank(message = "请输入活动标题")
    @Size(max = 100, message = "活动标题最多 100 个字")
    private String title;

    private String content;

    @Size(max = 255, message = "封面地址过长")
    private String cover;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    /** 1 进行中，0 已下线；新增不传默认进行中 */
    private Integer status;
}
