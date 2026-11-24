package com.example.beauty.module.message.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 留言/评价，对应 message 表。
 * type = EVALUATION 时 orderId、rating 有值；type = FEEDBACK 时只看 content。
 */
@Data
@TableName("message")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** EVALUATION 评价 / FEEDBACK 留言 */
    private String type;

    /** 评价关联的订单 id，留言为空 */
    private Long orderId;

    /** 星级 1-5，留言为空 */
    private Integer rating;

    private String content;

    /** 管理员回复 */
    private String reply;

    private LocalDateTime replyTime;

    /** 0 待审核，1 已通过，2 已隐藏 */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
