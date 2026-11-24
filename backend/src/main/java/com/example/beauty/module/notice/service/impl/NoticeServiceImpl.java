package com.example.beauty.module.notice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.beauty.common.PageResult;
import com.example.beauty.common.constant.Role;
import com.example.beauty.common.exception.BusinessException;
import com.example.beauty.common.security.UserContext;
import com.example.beauty.module.notice.dto.NoticeQuery;
import com.example.beauty.module.notice.dto.NoticeSaveDTO;
import com.example.beauty.module.notice.entity.Notice;
import com.example.beauty.module.notice.mapper.NoticeMapper;
import com.example.beauty.module.notice.service.NoticeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class NoticeServiceImpl implements NoticeService {

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

    @Override
    public PageResult<Notice> page(NoticeQuery query) {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getTitle()), Notice::getTitle, query.getTitle())
                .eq(query.getStatus() != null, Notice::getStatus, query.getStatus())
                .orderByDesc(Notice::getCreateTime);
        Page<Notice> page = noticeMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()), wrapper);
        return PageResult.of(page);
    }

    @Override
    public Notice getById(Long id) {
        Notice notice = noticeMapper.selectById(id);
        if (notice == null) {
            throw new BusinessException(404, "公告不存在");
        }
        if (Role.OWNER.equals(UserContext.getRole())
                && !Integer.valueOf(1).equals(notice.getStatus())) {
            throw new BusinessException(404, "公告不存在");
        }
        return notice;
    }

    @Override
    public Long create(NoticeSaveDTO dto) {
        Notice notice = new Notice();
        BeanUtils.copyProperties(dto, notice);
        notice.setType("NOTICE");
        if (notice.getStatus() == null) {
            notice.setStatus(0);
        }
        if (Integer.valueOf(1).equals(notice.getStatus())) {
            notice.setPublishTime(LocalDateTime.now().withNano(0));
        }
        noticeMapper.insert(notice);
        log.info("新增公告 noticeId={} title={} status={}",
                notice.getId(), notice.getTitle(), notice.getStatus());
        return notice.getId();
    }

    @Override
    public void update(Long id, NoticeSaveDTO dto) {
        Notice existing = getById(id);
        Notice update = new Notice();
        BeanUtils.copyProperties(dto, update);
        update.setId(id);
        if (Integer.valueOf(1).equals(dto.getStatus()) && existing.getPublishTime() == null) {
            update.setPublishTime(LocalDateTime.now().withNano(0));
        }
        noticeMapper.updateById(update);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "状态只能是 0（下线）或 1（发布）");
        }
        Notice existing = getById(id);
        Notice update = new Notice();
        update.setId(id);
        update.setStatus(status);
        if (status == 1 && existing.getPublishTime() == null) {
            update.setPublishTime(LocalDateTime.now().withNano(0));
        }
        noticeMapper.updateById(update);
        log.info("公告状态变更 noticeId={} status={} operator={}", id, status, UserContext.getId());
    }

    @Override
    public void delete(Long id) {
        getById(id);
        noticeMapper.deleteById(id);
        log.info("删除公告 noticeId={} operator={}", id, UserContext.getId());
    }
}
