package com.example.beauty.module.serviceprocess.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 服务过程记录，对应 service_process 表。
 * 一个订单可以有多条，按时间顺序组成车主端看到的进度时间线。
 */
@Data
@TableName("service_process")
public class ServiceProcess {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long orderId;

    /** 登记员工 id */
    private Long staffId;

    /** 节点编码：RECEIVE / CLEAN / CHECK / DELIVER */
    private String node;

    private String content;

    /** 照片地址，逗号分隔 */
    private String images;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
