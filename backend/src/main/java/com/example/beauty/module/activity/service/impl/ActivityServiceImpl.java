package com.example.beauty.module.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.beauty.common.PageResult;
import com.example.beauty.common.constant.Role;
import com.example.beauty.common.exception.BusinessException;
import com.example.beauty.common.security.UserContext;
import com.example.beauty.module.activity.dto.ActivityQuery;
import com.example.beauty.module.activity.dto.ActivitySaveDTO;
import com.example.beauty.module.activity.entity.Activity;
import com.example.beauty.module.activity.mapper.ActivityMapper;
import com.example.beauty.module.activity.service.ActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class ActivityServiceImpl implements ActivityService {

    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 20;

    private final ActivityMapper activityMapper;

    public ActivityServiceImpl(ActivityMapper activityMapper) {
        this.activityMapper = activityMapper;
    }

    @Override
    public List<Activity> listOngoing(Integer limit) {
        int size = limit == null || limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        Page<Activity> page = new Page<>(1, size, false);
        return activityMapper.selectPage(page, new LambdaQueryWrapper<Activity>()
                        .eq(Activity::getStatus, 1)
                        .orderByDesc(Activity::getStartTime)
                        .orderByDesc(Activity::getId))
                .getRecords();
    }

    @Override
    public PageResult<Activity> page(ActivityQuery query) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getTitle()), Activity::getTitle, query.getTitle())
                .eq(query.getStatus() != null, Activity::getStatus, query.getStatus())
                .orderByDesc(Activity::getStartTime)
                .orderByDesc(Activity::getId);
        Page<Activity> page = activityMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()), wrapper);
        return PageResult.of(page);
    }

    @Override
    public Activity getById(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BusinessException(404, "活动不存在");
        }
        if (Role.OWNER.equals(UserContext.getRole())
                && !Integer.valueOf(1).equals(activity.getStatus())) {
            throw new BusinessException(404, "活动不存在");
        }
        return activity;
    }

    @Override
    public Long create(ActivitySaveDTO dto) {
        checkTime(dto.getStartTime(), dto.getEndTime());
        Activity activity = new Activity();
        BeanUtils.copyProperties(dto, activity);
        if (activity.getStatus() == null) {
            activity.setStatus(1);
        }
        activityMapper.insert(activity);
        log.info("新增活动 activityId={} title={} status={}",
                activity.getId(), activity.getTitle(), activity.getStatus());
        return activity.getId();
    }

    @Override
    public void update(Long id, ActivitySaveDTO dto) {
        getById(id);
        checkTime(dto.getStartTime(), dto.getEndTime());
        Activity update = new Activity();
        BeanUtils.copyProperties(dto, update);
        update.setId(id);
        activityMapper.updateById(update);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "状态只能是 0（下线）或 1（上线）");
        }
        getById(id);
        Activity update = new Activity();
        update.setId(id);
        update.setStatus(status);
        activityMapper.updateById(update);
        log.info("活动状态变更 activityId={} status={} operator={}", id, status, UserContext.getId());
    }

    @Override
    public void delete(Long id) {
        getById(id);
        activityMapper.deleteById(id);
        log.info("删除活动 activityId={} operator={}", id, UserContext.getId());
    }

    private void checkTime(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime != null && endTime != null && endTime.isBefore(startTime)) {
            throw new BusinessException(400, "结束时间不能早于开始时间");
        }
    }
}
