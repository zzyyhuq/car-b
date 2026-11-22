package com.example.beauty.module.notice.controller;

import com.example.beauty.common.Result;
import com.example.beauty.module.notice.entity.Notice;
import com.example.beauty.module.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "公告", description = "小程序首页公告")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/notice")
public class NoticeController {

    private final NoticeService noticeService;

    public NoticeController(NoticeService noticeService) {
        this.noticeService = noticeService;
    }

    @Operation(summary = "查询已发布公告（车主端）", description = "按发布时间倒序，limit 默认 5、最大 20")
    @GetMapping("/list")
    public Result<List<Notice>> list(@RequestParam(required = false) Integer limit) {
        return Result.ok(noticeService.listPublished(limit));
    }
}
