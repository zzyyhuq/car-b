package com.example.beauty.module.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FeedbackCreateDTO {

    @NotBlank(message = "请输入留言内容")
    @Size(max = 1000, message = "留言最多 1000 个字")
    private String content;
}
