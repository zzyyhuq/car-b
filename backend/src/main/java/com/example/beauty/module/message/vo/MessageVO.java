package com.example.beauty.module.message.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MessageVO {

    private Long id;
    private Long userId;
    private String userName;
    private String type;
    private String typeText;
    private Long orderId;
    private String orderNo;
    private Integer rating;
    private String content;
    private String reply;
    private LocalDateTime replyTime;
    private Integer status;
    private String statusText;
    private LocalDateTime createTime;
}
