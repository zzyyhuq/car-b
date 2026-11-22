package com.example.beauty.module.activity.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.beauty.module.activity.entity.Activity;
import com.example.beauty.module.activity.mapper.ActivityMapper;
import com.example.beauty.module.activity.service.ActivityService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ActivityServiceImpl implements ActivityService {

    /** 首页默认展示 5 个，最多 20 个 */
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
}
