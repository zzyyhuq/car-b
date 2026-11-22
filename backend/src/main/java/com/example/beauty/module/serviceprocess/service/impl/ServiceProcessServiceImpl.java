package com.example.beauty.module.serviceprocess.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.beauty.common.constant.OrderStatus;
import com.example.beauty.common.constant.ServiceNode;
import com.example.beauty.common.exception.BusinessException;
import com.example.beauty.common.security.UserContext;
import com.example.beauty.module.order.service.OrderService;
import com.example.beauty.module.order.vo.OrderVO;
import com.example.beauty.module.serviceprocess.dto.ServiceProcessSaveDTO;
import com.example.beauty.module.serviceprocess.entity.ServiceProcess;
import com.example.beauty.module.serviceprocess.mapper.ServiceProcessMapper;
import com.example.beauty.module.serviceprocess.service.ServiceProcessService;
import com.example.beauty.module.serviceprocess.vo.ServiceProcessVO;
import com.example.beauty.module.user.entity.SysUser;
import com.example.beauty.module.user.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ServiceProcessServiceImpl implements ServiceProcessService {

    private final ServiceProcessMapper processMapper;
    private final SysUserMapper sysUserMapper;
    private final OrderService orderService;

    public ServiceProcessServiceImpl(ServiceProcessMapper processMapper,
                                     SysUserMapper sysUserMapper,
                                     OrderService orderService) {
        this.processMapper = processMapper;
        this.sysUserMapper = sysUserMapper;
        this.orderService = orderService;
    }

    @Override
    public List<ServiceProcessVO> listByOrder(Long orderId) {
        // 复用订单详情里的权限校验：车主只能看自己的订单
        orderService.detail(orderId);

        List<ServiceProcess> processes = processMapper.selectList(
                new LambdaQueryWrapper<ServiceProcess>()
                        .eq(ServiceProcess::getOrderId, orderId)
                        .orderByAsc(ServiceProcess::getCreateTime)
                        .orderByAsc(ServiceProcess::getId));
        if (processes.isEmpty()) {
            return List.of();
        }

        Set<Long> staffIds = processes.stream().map(ServiceProcess::getStaffId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, SysUser> staffMap = staffIds.isEmpty() ? Map.of()
                : sysUserMapper.selectBatchIds(staffIds).stream()
                .collect(Collectors.toMap(SysUser::getId, user -> user));

        return processes.stream()
                .map(process -> toVO(process, staffMap.get(process.getStaffId())))
                .toList();
    }

    @Override
    public Long add(ServiceProcessSaveDTO dto) {
        OrderVO order = orderService.detail(dto.getOrderId());
        if (!ServiceNode.valid(dto.getNode())) {
            throw new BusinessException(400, "未知的进度节点");
        }
        if (OrderStatus.CANCELLED.equals(order.getStatus())) {
            throw new BusinessException(400, "订单已取消，不能登记进度");
        }
        if (OrderStatus.PENDING.equals(order.getStatus())) {
            throw new BusinessException(400, "订单还未开始服务，不能登记进度");
        }
        if (OrderStatus.COMPLETED.equals(order.getStatus())
                || OrderStatus.EVALUATED.equals(order.getStatus())) {
            throw new BusinessException(400, "订单已完成，不能再登记进度");
        }

        ServiceProcess process = new ServiceProcess();
        process.setOrderId(dto.getOrderId());
        process.setStaffId(UserContext.getId());
        process.setNode(dto.getNode());
        process.setContent(dto.getContent());
        process.setImages(dto.getImages());
        processMapper.insert(process);
        log.info("登记服务进度 orderId={} orderNo={} node={} staffId={}",
                dto.getOrderId(), order.getOrderNo(), dto.getNode(), UserContext.getId());
        return process.getId();
    }

    private ServiceProcessVO toVO(ServiceProcess process, SysUser staff) {
        ServiceProcessVO vo = new ServiceProcessVO();
        BeanUtils.copyProperties(process, vo);
        vo.setNodeText(ServiceNode.text(process.getNode()));
        vo.setImages(splitImages(process.getImages()));
        if (staff != null) {
            vo.setStaffName(staff.getNickname());
        }
        return vo;
    }

    private List<String> splitImages(String images) {
        if (!StringUtils.hasText(images)) {
            return List.of();
        }
        return Arrays.stream(images.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }
}
