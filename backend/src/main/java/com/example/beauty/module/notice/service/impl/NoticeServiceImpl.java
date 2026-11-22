package com.example.beauty.module.notice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.beauty.module.notice.entity.Notice;
import com.example.beauty.module.notice.mapper.NoticeMapper;
import com.example.beauty.module.notice.service.NoticeService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {

    /** 首页默认展示 5 条，最多 20 条，避免一次拉全表 */
    private static final int DEFAULT_LIMIT = 5;
    private static final int MAX_LIMIT = 20;

    private final NoticeMapper noticeMapper;

    public NoticeServiceImpl(NoticeMapper noticeMapper) {
        this.noticeMapper = noticeMapper;
    }

    @Override
    public List<Notice> listPublished(Integer limit) {
        int size = limit == null || limit <= 0 ? DEFAULT_LIMIT : Math.min(limit, MAX_LIMIT);
        Page<Notice> page = new Page<>(1, size, false);
        return noticeMapper.selectPage(page, new LambdaQueryWrapper<Notice>()
                        .eq(Notice::getStatus, 1)
                        .orderByDesc(Notice::getPublishTime)
                        .orderByDesc(Notice::getId))
                .getRecords();
    }
}
