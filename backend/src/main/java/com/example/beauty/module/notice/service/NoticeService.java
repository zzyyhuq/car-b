package com.example.beauty.module.notice.service;

import com.example.beauty.module.notice.entity.Notice;

import java.util.List;

public interface NoticeService {

    /** 车主端公告列表，只返回已发布的，按发布时间倒序 */
    List<Notice> listPublished(Integer limit);
}
