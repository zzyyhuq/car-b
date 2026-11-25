package com.example.beauty.module.activitysignup.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.beauty.common.exception.BusinessException;
import com.example.beauty.common.security.UserContext;
import com.example.beauty.module.activity.entity.Activity;
import com.example.beauty.module.activity.service.ActivityService;
import com.example.beauty.module.activitysignup.entity.ActivitySignup;
import com.example.beauty.module.activitysignup.mapper.ActivitySignupMapper;
import com.example.beauty.module.activitysignup.service.ActivitySignupService;
import com.example.beauty.module.activitysignup.vo.ActivitySignupVO;
import com.example.beauty.module.user.entity.SysUser;
import com.example.beauty.module.user.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ActivitySignupServiceImpl implements ActivitySignupService {

    private static final int STATUS_CANCELLED = 0;
    private static final int STATUS_SIGNED = 1;

    private final ActivitySignupMapper signupMapper;
    private final ActivityService activityService;
    private final SysUserMapper sysUserMapper;

    public ActivitySignupServiceImpl(ActivitySignupMapper signupMapper,
                                     ActivityService activityService,
                                     SysUserMapper sysUserMapper) {
        this.signupMapper = signupMapper;
        this.activityService = activityService;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    @Transactional
    public void signup(Long activityId) {
        Activity activity = activityService.getById(activityId);
        if (!Integer.valueOf(1).equals(activity.getNeedSignup())) {
            throw new BusinessException(400, "该活动仅公告，无需报名");
        }

        Long userId = UserContext.getId();
        ActivitySignup existing = find(activityId, userId);
        if (existing != null && Integer.valueOf(STATUS_SIGNED).equals(existing.getStatus())) {
            throw new BusinessException(400, "你已报名该活动");
        }

        LocalDateTime now = LocalDateTime.now().withNano(0);
        if (existing == null) {
            ActivitySignup signup = new ActivitySignup();
            signup.setActivityId(activityId);
            signup.setUserId(userId);
            signup.setSignupTime(now);
            signup.setStatus(STATUS_SIGNED);
            try {
                signupMapper.insert(signup);
            } catch (DuplicateKeyException e) {
                // 并发重复提交时唯一索引兜底
                throw new BusinessException(400, "你已报名该活动");
            }
        } else {
            ActivitySignup update = new ActivitySignup();
            update.setId(existing.getId());
            update.setStatus(STATUS_SIGNED);
            update.setSignupTime(now);
            signupMapper.updateById(update);
        }
        log.info("活动报名 activityId={} userId={}", activityId, userId);
    }

    @Override
    public void cancel(Long activityId) {
        ActivitySignup existing = find(activityId, UserContext.getId());
        if (existing == null || !Integer.valueOf(STATUS_SIGNED).equals(existing.getStatus())) {
            throw new BusinessException(400, "你还没有报名该活动");
        }

        ActivitySignup update = new ActivitySignup();
        update.setId(existing.getId());
        update.setStatus(STATUS_CANCELLED);
        signupMapper.updateById(update);
        log.info("取消活动报名 activityId={} userId={}", activityId, UserContext.getId());
    }

    @Override
    public ActivitySignupVO mySignup(Long activityId) {
        ActivitySignup signup = find(activityId, UserContext.getId());
        if (signup == null) {
            return null;
        }
        return toVO(signup, sysUserMapper.selectById(signup.getUserId()));
    }

    @Override
    public List<ActivitySignupVO> listByActivity(Long activityId) {
        // 管理端查看，活动存在即可，不限制上下线
        activityService.getById(activityId);

        List<ActivitySignup> signups = signupMapper.selectList(
                new LambdaQueryWrapper<ActivitySignup>()
                        .eq(ActivitySignup::getActivityId, activityId)
                        .orderByDesc(ActivitySignup::getSignupTime)
                        .orderByDesc(ActivitySignup::getId));
        if (signups.isEmpty()) {
            return List.of();
        }

        Set<Long> userIds = signups.stream().map(ActivitySignup::getUserId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = userIds.isEmpty() ? Map.of()
                : sysUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, user -> user));

        return signups.stream()
                .map(signup -> toVO(signup,
                        signup.getUserId() == null ? null : userMap.get(signup.getUserId())))
                .toList();
    }

    private ActivitySignup find(Long activityId, Long userId) {
        return signupMapper.selectOne(new LambdaQueryWrapper<ActivitySignup>()
                .eq(ActivitySignup::getActivityId, activityId)
                .eq(ActivitySignup::getUserId, userId)
                .last("LIMIT 1"));
    }

    private ActivitySignupVO toVO(ActivitySignup signup, SysUser user) {
        ActivitySignupVO vo = new ActivitySignupVO();
        BeanUtils.copyProperties(signup, vo);
        if (user != null) {
            vo.setUserName(user.getNickname());
            vo.setPhone(user.getPhone());
        }
        vo.setStatusText(Integer.valueOf(STATUS_SIGNED).equals(signup.getStatus())
                ? "已报名" : "已取消");
        return vo;
    }
}
