package com.example.beauty.module.serviceprocess.service;

import com.example.beauty.module.serviceprocess.dto.ServiceProcessSaveDTO;
import com.example.beauty.module.serviceprocess.vo.ServiceProcessVO;

import java.util.List;

public interface ServiceProcessService {

    /** 按订单查询进度时间线；车主只能查自己的订单 */
    List<ServiceProcessVO> listByOrder(Long orderId);

    /** 员工登记一条进度，返回记录 id */
    Long add(ServiceProcessSaveDTO dto);
}
