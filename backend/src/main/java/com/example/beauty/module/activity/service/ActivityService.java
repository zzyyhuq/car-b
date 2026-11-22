package com.example.beauty.module.activity.service;

import com.example.beauty.module.activity.entity.Activity;

import java.util.List;

public interface ActivityService {

    /** 车主端活动列表，只返回进行中的，按开始时间倒序 */
    List<Activity> listOngoing(Integer limit);
}
