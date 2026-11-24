package com.example.beauty.module.notice.controller;

import com.example.beauty.common.PageResult;
import com.example.beauty.common.Result;
import com.example.beauty.common.constant.Role;
import com.example.beauty.common.security.RequireRole;
import com.example.beauty.module.notice.dto.NoticeQuery;
import com.example.beauty.module.notice.dto.NoticeSaveDTO;
import com.example.beauty.module.notice.entity.Notice;
import com.example.beauty.module.notice.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "公告", description = "小程序首页公告与管理端公告管理")
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

    @Operation(summary = "分页查询公告（管理端）")
    @GetMapping("/page")
    @RequireRole({Role.ADMIN})
    public Result<PageResult<Notice>> page(NoticeQuery query) {
        return Result.ok(noticeService.page(query));
    }

    @Operation(summary = "公告详情", description = "车主只能看已发布的公告")
    @GetMapping("/{id}")
    public Result<Notice> detail(@PathVariable Long id) {
        return Result.ok(noticeService.getById(id));
    }

    @Operation(summary = "新增公告（管理员）")
    @PostMapping
    @RequireRole({Role.ADMIN})
    public Result<Long> create(@RequestBody @Valid NoticeSaveDTO dto) {
        return Result.ok(noticeService.create(dto));
    }

    @Operation(summary = "修改公告（管理员）")
    @PutMapping("/{id}")
    @RequireRole({Role.ADMIN})
    public Result<Void> update(@PathVariable Long id, @RequestBody @Valid NoticeSaveDTO dto) {
        noticeService.update(id, dto);
        return Result.ok();
    }

    @Operation(summary = "发布/下线公告（管理员）", description = "status：1 发布，0 下线")
    @PutMapping("/{id}/status")
    @RequireRole({Role.ADMIN})
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        noticeService.updateStatus(id, status);
        return Result.ok();
    }

    @Operation(summary = "删除公告（管理员）")
    @DeleteMapping("/{id}")
    @RequireRole({Role.ADMIN})
    public Result<Void> delete(@PathVariable Long id) {
        noticeService.delete(id);
        return Result.ok();
    }
}
