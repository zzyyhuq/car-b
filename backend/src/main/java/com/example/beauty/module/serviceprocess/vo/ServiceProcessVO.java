package com.example.beauty.module.serviceprocess.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ServiceProcessVO {

    private Long id;
    private Long orderId;
    private String node;
    private String nodeText;
    private String content;
    private List<String> images;
    private Long staffId;
    private String staffName;
    private LocalDateTime createTime;
}
