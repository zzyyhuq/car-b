package com.example.beauty.module.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.beauty.common.PageResult;
import com.example.beauty.common.constant.OrderStatus;
import com.example.beauty.common.constant.Role;
import com.example.beauty.common.exception.BusinessException;
import com.example.beauty.common.security.UserContext;
import com.example.beauty.module.car.entity.Car;
import com.example.beauty.module.car.mapper.CarMapper;
import com.example.beauty.module.order.dto.OrderAdminCreateDTO;
import com.example.beauty.module.order.dto.OrderCreateDTO;
import com.example.beauty.module.order.dto.OrderQuery;
import com.example.beauty.module.order.entity.ServiceOrder;
import com.example.beauty.module.order.mapper.ServiceOrderMapper;
import com.example.beauty.module.order.service.OrderService;
import com.example.beauty.module.order.vo.OrderVO;
import com.example.beauty.module.servicetype.entity.ServiceType;
import com.example.beauty.module.servicetype.mapper.ServiceTypeMapper;
import com.example.beauty.module.user.entity.SysUser;
import com.example.beauty.module.user.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {

    private static final DateTimeFormatter ORDER_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private final ServiceOrderMapper orderMapper;
    private final CarMapper carMapper;
    private final ServiceTypeMapper serviceTypeMapper;
    private final SysUserMapper sysUserMapper;

    public OrderServiceImpl(ServiceOrderMapper orderMapper, CarMapper carMapper,
                            ServiceTypeMapper serviceTypeMapper, SysUserMapper sysUserMapper) {
        this.orderMapper = orderMapper;
        this.carMapper = carMapper;
        this.serviceTypeMapper = serviceTypeMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public Long create(OrderCreateDTO dto) {
        Long userId = UserContext.getId();
        Car car = requireOwnedCar(dto.getCarId(), userId);
        ServiceType serviceType = requireOnSale(dto.getServiceTypeId());
        checkAppointmentTime(dto.getAppointmentTime());

        ServiceOrder order = buildOrder(userId, car, serviceType, dto.getAppointmentTime(), dto.getRemark());
        orderMapper.insert(order);
        log.info("车主下单 orderNo={} userId={} carId={} serviceTypeId={} amount={}",
                order.getOrderNo(), userId, car.getId(), serviceType.getId(), order.getAmount());
        return order.getId();
    }

    @Override
    public Long adminCreate(OrderAdminCreateDTO dto) {
        SysUser owner = sysUserMapper.selectById(dto.getUserId());
        if (owner == null || !Role.OWNER.equals(owner.getRole())) {
            throw new BusinessException(400, "车主不存在");
        }
        if (owner.getStatus() != null && owner.getStatus() == 0) {
            throw new BusinessException(400, "车主账号已被禁用");
        }
        Car car = requireOwnedCar(dto.getCarId(), owner.getId());
        ServiceType serviceType = requireOnSale(dto.getServiceTypeId());
        checkAppointmentTime(dto.getAppointmentTime());

        ServiceOrder order = buildOrder(owner.getId(), car, serviceType, dto.getAppointmentTime(), dto.getRemark());
        orderMapper.insert(order);
        log.info("到店开单 orderNo={} userId={} operator={}",
                order.getOrderNo(), owner.getId(), UserContext.getId());
        return order.getId();
    }

    @Override
    public PageResult<OrderVO> myPage(OrderQuery query) {
        LambdaQueryWrapper<ServiceOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ServiceOrder::getUserId, UserContext.getId())
                .eq(StringUtils.hasText(query.getStatus()), ServiceOrder::getStatus, query.getStatus())
                .orderByDesc(ServiceOrder::getCreateTime);
        Page<ServiceOrder> page = orderMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()), wrapper);
        return toPageResult(page);
    }

    @Override
    public PageResult<OrderVO> page(OrderQuery query) {
        LambdaQueryWrapper<ServiceOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getOrderNo()), ServiceOrder::getOrderNo, query.getOrderNo())
                .eq(StringUtils.hasText(query.getStatus()), ServiceOrder::getStatus, query.getStatus())
                .eq(query.getStaffId() != null, ServiceOrder::getStaffId, query.getStaffId())
                .ge(query.getStartDate() != null, ServiceOrder::getCreateTime,
                        query.getStartDate() == null ? null : query.getStartDate().atStartOfDay())
                .lt(query.getEndDate() != null, ServiceOrder::getCreateTime,
                        query.getEndDate() == null ? null : query.getEndDate().plusDays(1).atStartOfDay())
                .orderByDesc(ServiceOrder::getCreateTime);

        if (StringUtils.hasText(query.getPlateNo())) {
            List<Long> carIds = carMapper.selectList(new LambdaQueryWrapper<Car>()
                            .like(Car::getPlateNo, query.getPlateNo()))
                    .stream().map(Car::getId).toList();
            if (carIds.isEmpty()) {
                return emptyPage(query);
            }
            wrapper.in(ServiceOrder::getCarId, carIds);
        }

        Page<ServiceOrder> page = orderMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()), wrapper);
        return toPageResult(page);
    }

    @Override
    public OrderVO detail(Long id) {
        ServiceOrder order = getRaw(id);
        checkViewPermission(order);
        return toVOList(List.of(order)).get(0);
    }

    @Override
    public void assign(Long id, Long staffId) {
        ServiceOrder order = getRaw(id);
        if (!OrderStatus.PENDING.equals(order.getStatus()) && !OrderStatus.ACCEPTED.equals(order.getStatus())) {
            throw new BusinessException(400, "当前状态不能派单");
        }
        SysUser staff = sysUserMapper.selectById(staffId);
        if (staff == null || !Role.STAFF.equals(staff.getRole())) {
            throw new BusinessException(400, "员工不存在");
        }
        if (staff.getStatus() != null && staff.getStatus() == 0) {
            throw new BusinessException(400, "员工账号已被禁用");
        }

        ServiceOrder update = new ServiceOrder();
        update.setId(id);
        update.setStaffId(staffId);
        if (OrderStatus.PENDING.equals(order.getStatus())) {
            update.setStatus(OrderStatus.ACCEPTED);
        }
        orderMapper.updateById(update);
        log.info("订单派单 orderNo={} staffId={} operator={}", order.getOrderNo(), staffId, UserContext.getId());
    }

    @Override
    public void cancel(Long id, String reason) {
        ServiceOrder order = getRaw(id);
        String role = UserContext.getRole();
        if (Role.OWNER.equals(role) && !Objects.equals(order.getUserId(), UserContext.getId())) {
            throw new BusinessException(403, "无权操作该订单");
        }
        if (!OrderStatus.cancellable(order.getStatus())) {
            throw new BusinessException(400, "当前状态不能取消");
        }

        String cancelReason = StringUtils.hasText(reason) ? reason
                : (Role.OWNER.equals(role) ? "车主取消" : "门店取消");
        ServiceOrder update = new ServiceOrder();
        update.setId(id);
        update.setStatus(OrderStatus.CANCELLED);
        update.setCancelReason(cancelReason);
        orderMapper.updateById(update);
        log.info("订单取消 orderNo={} operator={} role={} reason={}",
                order.getOrderNo(), UserContext.getId(), role, cancelReason);
    }

    @Override
    public void updateStatus(Long id, String status) {
        ServiceOrder order = getRaw(id);
        if (!OrderStatus.valid(status)) {
            throw new BusinessException(400, "未知的订单状态");
        }
        if (OrderStatus.CANCELLED.equals(status)) {
            throw new BusinessException(400, "取消订单请使用取消接口");
        }
        if (!OrderStatus.canTransit(order.getStatus(), status)) {
            throw new BusinessException(400, "订单状态不能从「" + OrderStatus.text(order.getStatus())
                    + "」变更为「" + OrderStatus.text(status) + "」");
        }

        ServiceOrder update = new ServiceOrder();
        update.setId(id);
        update.setStatus(status);
        // 员工接单时如果还没有负责人，默认把自己设为负责人
        if (OrderStatus.ACCEPTED.equals(status) && order.getStaffId() == null
                && Role.STAFF.equals(UserContext.getRole())) {
            update.setStaffId(UserContext.getId());
        }
        if (OrderStatus.COMPLETED.equals(status)) {
            update.setFinishTime(LocalDateTime.now());
            // TODO 模块 5：订单完成时按 金额 × 积分倍率 发放积分并写 points_record
        }
        orderMapper.updateById(update);
        log.info("订单状态流转 orderNo={} {} -> {} operator={}",
                order.getOrderNo(), order.getStatus(), status, UserContext.getId());
    }

    @Override
    public boolean hasOrderByCar(Long carId) {
        return orderMapper.selectCount(new LambdaQueryWrapper<ServiceOrder>()
                .eq(ServiceOrder::getCarId, carId)) > 0;
    }

    private ServiceOrder buildOrder(Long userId, Car car, ServiceType serviceType,
                                    LocalDateTime appointmentTime, String remark) {
        ServiceOrder order = new ServiceOrder();
        order.setOrderNo(nextOrderNo());
        order.setUserId(userId);
        order.setCarId(car.getId());
        order.setServiceTypeId(serviceType.getId());
        order.setAppointmentTime(appointmentTime);
        order.setStatus(OrderStatus.PENDING);
        order.setAmount(serviceType.getPrice());
        order.setPointsDeduct(0);
        order.setRemark(remark);
        return order;
    }

    private Car requireOwnedCar(Long carId, Long userId) {
        Car car = carMapper.selectById(carId);
        if (car == null || !Objects.equals(car.getUserId(), userId)) {
            throw new BusinessException(404, "车辆不存在或不属于该车主");
        }
        return car;
    }

    private ServiceType requireOnSale(Long serviceTypeId) {
        ServiceType serviceType = serviceTypeMapper.selectById(serviceTypeId);
        if (serviceType == null) {
            throw new BusinessException(404, "服务项目不存在");
        }
        if (serviceType.getStatus() == null || serviceType.getStatus() != 1) {
            throw new BusinessException(400, "服务项目已下架");
        }
        return serviceType;
    }

    private void checkAppointmentTime(LocalDateTime appointmentTime) {
        if (appointmentTime != null && appointmentTime.isBefore(LocalDateTime.now())) {
            throw new BusinessException(400, "预约时间不能早于当前时间");
        }
    }

    private ServiceOrder getRaw(Long id) {
        ServiceOrder order = orderMapper.selectById(id);
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }
        return order;
    }

    private void checkViewPermission(ServiceOrder order) {
        if (Role.OWNER.equals(UserContext.getRole())
                && !Objects.equals(order.getUserId(), UserContext.getId())) {
            throw new BusinessException(403, "无权查看该订单");
        }
    }

    private String nextOrderNo() {
        return "BO" + LocalDateTime.now().format(ORDER_NO_FORMAT)
                + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    private PageResult<OrderVO> toPageResult(Page<ServiceOrder> page) {
        PageResult<OrderVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setPage(page.getCurrent());
        result.setSize(page.getSize());
        result.setRecords(toVOList(page.getRecords()));
        return result;
    }

    private PageResult<OrderVO> emptyPage(OrderQuery query) {
        PageResult<OrderVO> result = new PageResult<>();
        result.setTotal(0);
        result.setPage(query.getPage());
        result.setSize(query.getSize());
        result.setRecords(List.of());
        return result;
    }

    /** 批量补齐车辆、服务项目、车主/员工昵称，避免列表 N+1 查询 */
    private List<OrderVO> toVOList(List<ServiceOrder> orders) {
        if (orders == null || orders.isEmpty()) {
            return List.of();
        }
        Set<Long> carIds = orders.stream().map(ServiceOrder::getCarId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> typeIds = orders.stream().map(ServiceOrder::getServiceTypeId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> userIds = orders.stream()
                .flatMap(o -> java.util.stream.Stream.of(o.getUserId(), o.getStaffId()))
                .filter(Objects::nonNull).collect(Collectors.toSet());

        Map<Long, Car> carMap = loadMap(carIds, carMapper, Car::getId);
        Map<Long, ServiceType> typeMap = loadMap(typeIds, serviceTypeMapper, ServiceType::getId);
        Map<Long, SysUser> userMap = loadMap(userIds, sysUserMapper, SysUser::getId);

        List<OrderVO> list = new ArrayList<>(orders.size());
        for (ServiceOrder order : orders) {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);
            Car car = carMap.get(order.getCarId());
            if (car != null) {
                vo.setPlateNo(car.getPlateNo());
                vo.setCarBrand(car.getBrand());
                vo.setCarModel(car.getModel());
            }
            ServiceType serviceType = typeMap.get(order.getServiceTypeId());
            if (serviceType != null) {
                vo.setServiceName(serviceType.getName());
            }
            SysUser owner = userMap.get(order.getUserId());
            if (owner != null) {
                vo.setUserName(owner.getNickname());
            }
            SysUser staff = userMap.get(order.getStaffId());
            if (staff != null) {
                vo.setStaffName(staff.getNickname());
            }
            vo.setStatusText(OrderStatus.text(order.getStatus()));
            list.add(vo);
        }
        return list;
    }

    private <T> Map<Long, T> loadMap(Collection<Long> ids, BaseMapper<T> mapper, Function<T, Long> idGetter) {
        if (ids == null || ids.isEmpty()) {
            return Map.of();
        }
        return mapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(idGetter, t -> t, (a, b) -> a));
    }
}
