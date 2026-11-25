package com.example.beauty.module.stats.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.beauty.common.constant.OrderStatus;
import com.example.beauty.common.constant.Role;
import com.example.beauty.module.activity.entity.Activity;
import com.example.beauty.module.activity.mapper.ActivityMapper;
import com.example.beauty.module.activitysignup.entity.ActivitySignup;
import com.example.beauty.module.activitysignup.mapper.ActivitySignupMapper;
import com.example.beauty.module.message.entity.Message;
import com.example.beauty.module.message.mapper.MessageMapper;
import com.example.beauty.module.notice.entity.Notice;
import com.example.beauty.module.notice.mapper.NoticeMapper;
import com.example.beauty.module.order.entity.ServiceOrder;
import com.example.beauty.module.order.mapper.ServiceOrderMapper;
import com.example.beauty.module.points.entity.PointsExchange;
import com.example.beauty.module.points.mapper.PointsExchangeMapper;
import com.example.beauty.module.stats.service.StatsService;
import com.example.beauty.module.stats.vo.StatsOverviewVO;
import com.example.beauty.module.user.entity.SysUser;
import com.example.beauty.module.user.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class StatsServiceImpl implements StatsService {

    private final SysUserMapper userMapper;
    private final ServiceOrderMapper orderMapper;
    private final PointsExchangeMapper exchangeMapper;
    private final MessageMapper messageMapper;
    private final NoticeMapper noticeMapper;
    private final ActivityMapper activityMapper;
    private final ActivitySignupMapper signupMapper;

    public StatsServiceImpl(SysUserMapper userMapper,
                            ServiceOrderMapper orderMapper,
                            PointsExchangeMapper exchangeMapper,
                            MessageMapper messageMapper,
                            NoticeMapper noticeMapper,
                            ActivityMapper activityMapper,
                            ActivitySignupMapper signupMapper) {
        this.userMapper = userMapper;
        this.orderMapper = orderMapper;
        this.exchangeMapper = exchangeMapper;
        this.messageMapper = messageMapper;
        this.noticeMapper = noticeMapper;
        this.activityMapper = activityMapper;
        this.signupMapper = signupMapper;
    }

    @Override
    public StatsOverviewVO overview() {
        StatsOverviewVO vo = new StatsOverviewVO();

        vo.setUserCount(userMapper.selectCount(null));
        vo.setOwnerCount(userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRole, Role.OWNER)));
        vo.setStaffCount(userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRole, Role.STAFF)));

        vo.setOrderCount(orderMapper.selectCount(null));
        vo.setTodayOrderCount(orderMapper.selectCount(new LambdaQueryWrapper<ServiceOrder>()
                .ge(ServiceOrder::getCreateTime, LocalDate.now().atStartOfDay())));
        vo.setPendingOrderCount(orderMapper.selectCount(new LambdaQueryWrapper<ServiceOrder>()
                .eq(ServiceOrder::getStatus, OrderStatus.PENDING)));
        vo.setCompletedOrderCount(orderMapper.selectCount(new LambdaQueryWrapper<ServiceOrder>()
                .in(ServiceOrder::getStatus, OrderStatus.COMPLETED, OrderStatus.EVALUATED)));
        BigDecimal revenue = orderMapper.sumCompletedAmount();
        vo.setTotalRevenue(revenue == null ? BigDecimal.ZERO : revenue);

        vo.setPendingExchangeCount(exchangeMapper.selectCount(
                new LambdaQueryWrapper<PointsExchange>().eq(PointsExchange::getStatus, "PENDING")));
        vo.setPendingMessageCount(messageMapper.selectCount(
                new LambdaQueryWrapper<Message>().eq(Message::getStatus, 0)));
        vo.setNoticeCount(noticeMapper.selectCount(
                new LambdaQueryWrapper<Notice>().eq(Notice::getStatus, 1)));
        vo.setActivityCount(activityMapper.selectCount(
                new LambdaQueryWrapper<Activity>().eq(Activity::getStatus, 1)));
        vo.setSignupCount(signupMapper.selectCount(
                new LambdaQueryWrapper<ActivitySignup>().eq(ActivitySignup::getStatus, 1)));
        return vo;
    }
}
