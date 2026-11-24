package com.example.beauty.module.activity.service;

import com.example.beauty.common.PageResult;
import com.example.beauty.module.activity.dto.ActivityQuery;
import com.example.beauty.module.activity.dto.ActivitySaveDTO;
import com.example.beauty.module.activity.entity.Activity;

import java.util.List;

public interface ActivityService {

    /** 车主端活动列表，只返回进行中的，按开始时间倒序 */
    List<Activity> listOngoing(Integer limit);

    /** 管理端分页 */
    PageResult<Activity> page(ActivityQuery query);

    /** 详情；车主只能看进行中的 */
    Activity getById(Long id);

    Long create(ActivitySaveDTO dto);

    void update(Long id, ActivitySaveDTO dto);

    /** 1 上线，0 下线 */
    void updateStatus(Long id, Integer status);

    void delete(Long id);
}
