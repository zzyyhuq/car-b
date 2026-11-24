package com.example.beauty.module.notice.service;

import com.example.beauty.common.PageResult;
import com.example.beauty.module.notice.dto.NoticeQuery;
import com.example.beauty.module.notice.dto.NoticeSaveDTO;
import com.example.beauty.module.notice.entity.Notice;

import java.util.List;

public interface NoticeService {

    /** 车主端公告列表，只返回已发布的，按发布时间倒序 */
    List<Notice> listPublished(Integer limit);

    /** 管理端分页 */
    PageResult<Notice> page(NoticeQuery query);

    /** 详情；车主只能看已发布的 */
    Notice getById(Long id);

    Long create(NoticeSaveDTO dto);

    void update(Long id, NoticeSaveDTO dto);

    /** 1 发布，0 下线 */
    void updateStatus(Long id, Integer status);

    void delete(Long id);
}
