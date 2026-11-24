package com.example.beauty.module.message.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MessageReplyDTO {

    @NotBlank(message = "请输入回复内容")
    @Size(max = 1000, message = "回复最多 1000 个字")
    private String reply;
}
