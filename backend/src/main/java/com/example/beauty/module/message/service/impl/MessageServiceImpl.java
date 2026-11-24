package com.example.beauty.module.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.beauty.common.PageResult;
import com.example.beauty.common.constant.OrderStatus;
import com.example.beauty.common.exception.BusinessException;
import com.example.beauty.common.security.UserContext;
import com.example.beauty.module.message.dto.EvaluationCreateDTO;
import com.example.beauty.module.message.dto.FeedbackCreateDTO;
import com.example.beauty.module.message.dto.MessageQuery;
import com.example.beauty.module.message.entity.Message;
import com.example.beauty.module.message.mapper.MessageMapper;
import com.example.beauty.module.message.service.MessageService;
import com.example.beauty.module.message.vo.MessageVO;
import com.example.beauty.module.order.entity.ServiceOrder;
import com.example.beauty.module.order.mapper.ServiceOrderMapper;
import com.example.beauty.module.order.service.OrderService;
import com.example.beauty.module.order.vo.OrderVO;
import com.example.beauty.module.user.entity.SysUser;
import com.example.beauty.module.user.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MessageServiceImpl implements MessageService {

    private static final String TYPE_EVALUATION = "EVALUATION";
    private static final String TYPE_FEEDBACK = "FEEDBACK";

    private static final int STATUS_PENDING = 0;
    private static final int STATUS_APPROVED = 1;
    private static final int STATUS_HIDDEN = 2;

    private static final int DEFAULT_MY_LIMIT = 50;
    private static final int MAX_MY_LIMIT = 100;

    private final MessageMapper messageMapper;
    private final SysUserMapper sysUserMapper;
    private final ServiceOrderMapper orderMapper;
    private final OrderService orderService;

    public MessageServiceImpl(MessageMapper messageMapper,
                              SysUserMapper sysUserMapper,
                              ServiceOrderMapper orderMapper,
                              OrderService orderService) {
        this.messageMapper = messageMapper;
        this.sysUserMapper = sysUserMapper;
        this.orderMapper = orderMapper;
        this.orderService = orderService;
    }

    @Override
    @Transactional
    public Long evaluate(EvaluationCreateDTO dto) {
        OrderVO order = orderService.detail(dto.getOrderId());
        if (OrderStatus.EVALUATED.equals(order.getStatus())) {
            throw new BusinessException(400, "该订单已评价");
        }
        if (!OrderStatus.COMPLETED.equals(order.getStatus())) {
            throw new BusinessException(400, "订单完成后才能评价");
        }
        Long exists = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getType, TYPE_EVALUATION)
                .eq(Message::getOrderId, dto.getOrderId()));
        if (exists > 0) {
            throw new BusinessException(400, "该订单已评价");
        }

        Message message = new Message();
        message.setUserId(UserContext.getId());
        message.setType(TYPE_EVALUATION);
        message.setOrderId(dto.getOrderId());
        message.setRating(dto.getRating());
        message.setContent(dto.getContent());
        // 评价默认直接通过，留言需要管理员审核
        message.setStatus(STATUS_APPROVED);
        messageMapper.insert(message);

        // 订单状态同步改为已评价，和评价记录在同一事务
        orderService.markEvaluated(dto.getOrderId());
        log.info("车主评价 orderId={} orderNo={} rating={} messageId={}",
                dto.getOrderId(), order.getOrderNo(), dto.getRating(), message.getId());
        return message.getId();
    }

    @Override
    public Long feedback(FeedbackCreateDTO dto) {
        Message message = new Message();
        message.setUserId(UserContext.getId());
        message.setType(TYPE_FEEDBACK);
        message.setContent(dto.getContent());
        message.setStatus(STATUS_PENDING);
        messageMapper.insert(message);
        log.info("车主留言 messageId={} userId={}", message.getId(), UserContext.getId());
        return message.getId();
    }

    @Override
    public List<MessageVO> myMessages(Integer limit) {
        int size = limit == null || limit <= 0 ? DEFAULT_MY_LIMIT : Math.min(limit, MAX_MY_LIMIT);
        List<Message> messages = messageMapper.selectPage(
                        new Page<>(1, size, false),
                        new LambdaQueryWrapper<Message>()
                                .eq(Message::getUserId, UserContext.getId())
                                .orderByDesc(Message::getCreateTime)
                                .orderByDesc(Message::getId))
                .getRecords();
        return toVOList(messages);
    }

    @Override
    public MessageVO getByOrder(Long orderId) {
        // 复用订单详情权限：车主只能看自己的订单
        orderService.detail(orderId);
        Message message = messageMapper.selectOne(new LambdaQueryWrapper<Message>()
                .eq(Message::getType, TYPE_EVALUATION)
                .eq(Message::getOrderId, orderId)
                .orderByDesc(Message::getId)
                .last("LIMIT 1"));
        if (message == null) {
            return null;
        }
        return toVOList(List.of(message)).get(0);
    }

    @Override
    public PageResult<MessageVO> page(MessageQuery query) {
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StringUtils.hasText(query.getType()), Message::getType, query.getType())
                .eq(query.getStatus() != null, Message::getStatus, query.getStatus())
                .like(StringUtils.hasText(query.getKeyword()), Message::getContent, query.getKeyword())
                .eq(query.getUserId() != null, Message::getUserId, query.getUserId())
                .eq(query.getOrderId() != null, Message::getOrderId, query.getOrderId())
                .orderByDesc(Message::getCreateTime)
                .orderByDesc(Message::getId);
        Page<Message> page = messageMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()), wrapper);

        PageResult<MessageVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setPage(page.getCurrent());
        result.setSize(page.getSize());
        result.setRecords(toVOList(page.getRecords()));
        return result;
    }

    @Override
    public void reply(Long id, String reply) {
        getRaw(id);
        Message update = new Message();
        update.setId(id);
        update.setReply(reply);
        update.setReplyTime(LocalDateTime.now().withNano(0));
        messageMapper.updateById(update);
        log.info("回复留言 messageId={} operator={}", id, UserContext.getId());
    }

    @Override
    public void audit(Long id, Integer status) {
        if (status == null || status < STATUS_PENDING || status > STATUS_HIDDEN) {
            throw new BusinessException(400, "审核状态只能是 0（待审核）、1（通过）、2（隐藏）");
        }
        getRaw(id);
        Message update = new Message();
        update.setId(id);
        update.setStatus(status);
        messageMapper.updateById(update);
        log.info("审核留言 messageId={} status={} operator={}", id, status, UserContext.getId());
    }

    private Message getRaw(Long id) {
        Message message = messageMapper.selectById(id);
        if (message == null) {
            throw new BusinessException(404, "留言不存在");
        }
        return message;
    }

    private List<MessageVO> toVOList(List<Message> messages) {
        if (messages == null || messages.isEmpty()) {
            return List.of();
        }
        Set<Long> userIds = messages.stream().map(Message::getUserId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> orderIds = messages.stream().map(Message::getOrderId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, SysUser> userMap = userIds.isEmpty() ? Map.of()
                : sysUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, user -> user));
        Map<Long, ServiceOrder> orderMap = orderIds.isEmpty() ? Map.of()
                : orderMapper.selectBatchIds(orderIds).stream()
                .collect(Collectors.toMap(ServiceOrder::getId, order -> order));

        return messages.stream().map(message -> toVO(message,
                        message.getUserId() == null ? null : userMap.get(message.getUserId()),
                        message.getOrderId() == null ? null : orderMap.get(message.getOrderId())))
                .toList();
    }

    private MessageVO toVO(Message message, SysUser user, ServiceOrder order) {
        MessageVO vo = new MessageVO();
        BeanUtils.copyProperties(message, vo);
        if (user != null) {
            vo.setUserName(user.getNickname());
        }
        if (order != null) {
            vo.setOrderNo(order.getOrderNo());
        }
        vo.setTypeText(typeText(message.getType()));
        vo.setStatusText(statusText(message.getStatus()));
        return vo;
    }

    private String typeText(String type) {
        if (TYPE_EVALUATION.equals(type)) {
            return "评价";
        }
        if (TYPE_FEEDBACK.equals(type)) {
            return "留言";
        }
        return type == null ? "" : type;
    }

    private String statusText(Integer status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case STATUS_PENDING -> "待审核";
            case STATUS_APPROVED -> "已通过";
            case STATUS_HIDDEN -> "已隐藏";
            default -> String.valueOf(status);
        };
    }
}
