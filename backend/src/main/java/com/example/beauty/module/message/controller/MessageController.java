package com.example.beauty.module.message.controller;

import com.example.beauty.common.PageResult;
import com.example.beauty.common.Result;
import com.example.beauty.common.constant.Role;
import com.example.beauty.common.security.RequireRole;
import com.example.beauty.module.message.dto.EvaluationCreateDTO;
import com.example.beauty.module.message.dto.FeedbackCreateDTO;
import com.example.beauty.module.message.dto.MessageAuditDTO;
import com.example.beauty.module.message.dto.MessageQuery;
import com.example.beauty.module.message.dto.MessageReplyDTO;
import com.example.beauty.module.message.service.MessageService;
import com.example.beauty.module.message.vo.MessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "留言与评价", description = "车主评价/留言，管理端审核与回复")
@SecurityRequirement(name = "BearerAuth")
@RestController
@RequestMapping("/api/message")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @Operation(summary = "评价已完成订单（车主）", description = "评价成功后订单状态变为已评价")
    @PostMapping("/evaluate")
    @RequireRole({Role.OWNER})
    public Result<Long> evaluate(@RequestBody @Valid EvaluationCreateDTO dto) {
        return Result.ok(messageService.evaluate(dto));
    }

    @Operation(summary = "提交留言（车主）", description = "留言默认待审核")
    @PostMapping
    @RequireRole({Role.OWNER})
    public Result<Long> feedback(@RequestBody @Valid FeedbackCreateDTO dto) {
        return Result.ok(messageService.feedback(dto));
    }

    @Operation(summary = "我的评价与留言（车主）")
    @GetMapping("/my")
    @RequireRole({Role.OWNER})
    public Result<List<MessageVO>> my(@RequestParam(required = false) Integer limit) {
        return Result.ok(messageService.myMessages(limit));
    }

    @Operation(summary = "查询订单评价", description = "没有评价时 data 为 null；车主只能查自己的订单")
    @GetMapping("/order/{orderId}")
    public Result<MessageVO> byOrder(@PathVariable Long orderId) {
        return Result.ok(messageService.getByOrder(orderId));
    }

    @Operation(summary = "留言/评价分页（管理端）", description = "支持 type/status/keyword 筛选")
    @GetMapping("/page")
    @RequireRole({Role.ADMIN})
    public Result<PageResult<MessageVO>> page(MessageQuery query) {
        return Result.ok(messageService.page(query));
    }

    @Operation(summary = "回复留言/评价（管理员）")
    @PutMapping("/{id}/reply")
    @RequireRole({Role.ADMIN})
    public Result<Void> reply(@PathVariable Long id, @RequestBody @Valid MessageReplyDTO dto) {
        messageService.reply(id, dto.getReply());
        return Result.ok();
    }

    @Operation(summary = "审核留言/评价（管理员）", description = "status：0 待审核，1 通过，2 隐藏")
    @PutMapping("/{id}/audit")
    @RequireRole({Role.ADMIN})
    public Result<Void> audit(@PathVariable Long id, @RequestBody @Valid MessageAuditDTO dto) {
        messageService.audit(id, dto.getStatus());
        return Result.ok();
    }
}
