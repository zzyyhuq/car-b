package com.example.beauty.module.order.service;

import com.example.beauty.common.PageResult;
import com.example.beauty.module.order.dto.OrderAdminCreateDTO;
import com.example.beauty.module.order.dto.OrderCreateDTO;
import com.example.beauty.module.order.dto.OrderQuery;
import com.example.beauty.module.order.vo.OrderVO;

public interface OrderService {

    /** 车主预约下单，返回订单 id */
    Long create(OrderCreateDTO dto);

    /** 门店到店开单 */
    Long adminCreate(OrderAdminCreateDTO dto);

    /** 我的订单（车主） */
    PageResult<OrderVO> myPage(OrderQuery query);

    /** 订单分页（管理端） */
    PageResult<OrderVO> page(OrderQuery query);

    OrderVO detail(Long id);

    /** 派单给员工，待接单会同时变成已接单 */
    void assign(Long id, Long staffId);

    void cancel(Long id, String reason);

    /** 按状态机流转，取消不在这里处理 */
    void updateStatus(Long id, String status);

    /** 车辆是否已被订单引用（车主删车前检查） */
    boolean hasOrderByCar(Long carId);
}
