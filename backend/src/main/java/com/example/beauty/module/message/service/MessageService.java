package com.example.beauty.module.message.service;

import com.example.beauty.common.PageResult;
import com.example.beauty.module.message.dto.EvaluationCreateDTO;
import com.example.beauty.module.message.dto.FeedbackCreateDTO;
import com.example.beauty.module.message.dto.MessageQuery;
import com.example.beauty.module.message.vo.MessageVO;

import java.util.List;

public interface MessageService {

    /** 车主评价已完成订单，同时把订单标记为已评价 */
    Long evaluate(EvaluationCreateDTO dto);

    /** 车主提交留言，默认待审核 */
    Long feedback(FeedbackCreateDTO dto);

    /** 我的评价与留言 */
    List<MessageVO> myMessages(Integer limit);

    /** 查询某订单的评价，没有返回 null；车主只能查自己的订单 */
    MessageVO getByOrder(Long orderId);

    /** 管理端分页 */
    PageResult<MessageVO> page(MessageQuery query);

    void reply(Long id, String reply);

    /** 0 待审核，1 通过，2 隐藏 */
    void audit(Long id, Integer status);
}
