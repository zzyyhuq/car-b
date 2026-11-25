package com.example.beauty.module.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.beauty.module.order.entity.ServiceOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface ServiceOrderMapper extends BaseMapper<ServiceOrder> {

    /** 已完成/已评价订单的累计营收 */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM service_order "
            + "WHERE status IN ('COMPLETED', 'EVALUATED')")
    BigDecimal sumCompletedAmount();
}
