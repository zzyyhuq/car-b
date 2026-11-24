package com.example.beauty.module.notice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class NoticeSaveDTO {

    @NotBlank(message = "请输入公告标题")
    @Size(max = 100, message = "公告标题最多 100 个字")
    private String title;

    private String content;

    /** 1 已发布，0 草稿；新增不传默认草稿 */
    private Integer status;
}
