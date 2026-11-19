package com.example.beauty.module.car.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 车主车辆。订单必须关联车辆，所以先建最小实体和 Mapper，车辆管理页阶段 4 再做。
 */
@Data
@TableName("car")
public class Car {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 车主 id */
    private Long userId;

    private String plateNo;

    private String brand;

    private String model;

    private String color;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
