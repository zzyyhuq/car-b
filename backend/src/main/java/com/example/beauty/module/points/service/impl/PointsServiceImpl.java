package com.example.beauty.module.points.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.beauty.common.PageResult;
import com.example.beauty.common.exception.BusinessException;
import com.example.beauty.common.security.UserContext;
import com.example.beauty.module.points.dto.PointsExchangeQuery;
import com.example.beauty.module.points.dto.PointsGoodsQuery;
import com.example.beauty.module.points.dto.PointsGoodsSaveDTO;
import com.example.beauty.module.points.entity.PointsExchange;
import com.example.beauty.module.points.entity.PointsGoods;
import com.example.beauty.module.points.entity.PointsRecord;
import com.example.beauty.module.points.mapper.PointsExchangeMapper;
import com.example.beauty.module.points.mapper.PointsGoodsMapper;
import com.example.beauty.module.points.mapper.PointsRecordMapper;
import com.example.beauty.module.points.service.PointsService;
import com.example.beauty.module.points.vo.PointsExchangeVO;
import com.example.beauty.module.user.entity.SysUser;
import com.example.beauty.module.user.mapper.SysUserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PointsServiceImpl implements PointsService {

    private static final DateTimeFormatter EXCHANGE_NO_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    private static final String STATUS_PENDING = "PENDING";
    private static final String TYPE_ORDER_GAIN = "ORDER_GAIN";
    private static final String TYPE_EXCHANGE_COST = "EXCHANGE_COST";

    private static final int DEFAULT_EXCHANGE_LIMIT = 20;
    private static final int MAX_EXCHANGE_LIMIT = 50;
    private static final int DEFAULT_RECORD_LIMIT = 50;
    private static final int MAX_RECORD_LIMIT = 100;

    private final PointsGoodsMapper goodsMapper;
    private final PointsExchangeMapper exchangeMapper;
    private final PointsRecordMapper recordMapper;
    private final SysUserMapper sysUserMapper;

    public PointsServiceImpl(PointsGoodsMapper goodsMapper,
                             PointsExchangeMapper exchangeMapper,
                             PointsRecordMapper recordMapper,
                             SysUserMapper sysUserMapper) {
        this.goodsMapper = goodsMapper;
        this.exchangeMapper = exchangeMapper;
        this.recordMapper = recordMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public List<PointsGoods> listOnSale() {
        return goodsMapper.selectList(new LambdaQueryWrapper<PointsGoods>()
                .eq(PointsGoods::getStatus, 1)
                .orderByAsc(PointsGoods::getPointsPrice)
                .orderByAsc(PointsGoods::getId));
    }

    @Override
    public PageResult<PointsGoods> pageGoods(PointsGoodsQuery query) {
        LambdaQueryWrapper<PointsGoods> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getName()), PointsGoods::getName, query.getName())
                .eq(query.getStatus() != null, PointsGoods::getStatus, query.getStatus())
                .orderByDesc(PointsGoods::getCreateTime);
        Page<PointsGoods> page = goodsMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()), wrapper);
        return PageResult.of(page);
    }

    @Override
    public Long createGoods(PointsGoodsSaveDTO dto) {
        PointsGoods goods = new PointsGoods();
        BeanUtils.copyProperties(dto, goods);
        if (goods.getStatus() == null) {
            goods.setStatus(1);
        }
        goodsMapper.insert(goods);
        log.info("新增积分商品 goodsId={} name={} pointsPrice={} stock={}",
                goods.getId(), goods.getName(), goods.getPointsPrice(), goods.getStock());
        return goods.getId();
    }

    @Override
    public void updateGoods(Long id, PointsGoodsSaveDTO dto) {
        requireGoods(id);
        PointsGoods goods = new PointsGoods();
        BeanUtils.copyProperties(dto, goods);
        goods.setId(id);
        // updateById 忽略 null，没传的字段保持原值
        goodsMapper.updateById(goods);
    }

    @Override
    public void updateGoodsStatus(Long id, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(400, "状态只能是 0（下架）或 1（上架）");
        }
        requireGoods(id);
        PointsGoods goods = new PointsGoods();
        goods.setId(id);
        goods.setStatus(status);
        goodsMapper.updateById(goods);
    }

    @Override
    public void deleteGoods(Long id) {
        requireGoods(id);
        goodsMapper.deleteById(id);
        log.info("删除积分商品 goodsId={} operator={}", id, UserContext.getId());
    }

    private PointsGoods requireGoods(Long id) {
        PointsGoods goods = goodsMapper.selectById(id);
        if (goods == null) {
            throw new BusinessException(404, "积分商品不存在");
        }
        return goods;
    }

    /**
     * 兑换流程：校验 → 扣库存 → 扣积分 → 写兑换单 → 写流水。
     * 全程一个事务，任何一步失败都会回滚库存和积分。
     * 扣库存、扣积分都用带条件的 UPDATE，避免并发超兑。
     */
    @Override
    @Transactional
    public PointsExchangeVO exchange(Long goodsId) {
        Long userId = UserContext.getId();
        PointsGoods goods = goodsMapper.selectById(goodsId);
        if (goods == null || !Integer.valueOf(1).equals(goods.getStatus())) {
            throw new BusinessException(404, "积分商品不存在或已下架");
        }
        if (goods.getStock() == null || goods.getStock() <= 0) {
            throw new BusinessException(400, "该商品已兑完");
        }
        int cost = goods.getPointsPrice() == null ? 0 : goods.getPointsPrice();
        if (cost <= 0) {
            throw new BusinessException(400, "商品积分配置有误");
        }

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        int balance = user.getPoints() == null ? 0 : user.getPoints();
        if (balance < cost) {
            throw new BusinessException(400, "积分不足，还差 " + (cost - balance) + " 积分");
        }

        int stockRows = goodsMapper.update(null, new LambdaUpdateWrapper<PointsGoods>()
                .eq(PointsGoods::getId, goodsId)
                .eq(PointsGoods::getStatus, 1)
                .gt(PointsGoods::getStock, 0)
                .setSql("stock = stock - 1"));
        if (stockRows == 0) {
            throw new BusinessException(400, "该商品已兑完");
        }

        int pointRows = sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, userId)
                .ge(SysUser::getPoints, cost)
                .setSql("points = points - " + cost));
        if (pointRows == 0) {
            throw new BusinessException(400, "积分不足");
        }

        PointsExchange exchange = new PointsExchange();
        exchange.setExchangeNo(nextExchangeNo());
        exchange.setUserId(userId);
        exchange.setGoodsId(goodsId);
        exchange.setPointsCost(cost);
        exchange.setStatus(STATUS_PENDING);
        exchange.setExchangeTime(LocalDateTime.now().withNano(0));
        exchangeMapper.insert(exchange);

        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setChangeAmount(-cost);
        record.setType(TYPE_EXCHANGE_COST);
        record.setRefId(exchange.getId());
        record.setRemark("兑换「" + goods.getName() + "」");
        recordMapper.insert(record);

        SysUser latest = sysUserMapper.selectById(userId);
        PointsExchangeVO vo = toVO(exchange, goods.getName());
        vo.setPointsBalance(latest == null ? balance - cost : latest.getPoints());
        log.info("积分兑换 userId={} goodsId={} cost={} exchangeNo={}",
                userId, goodsId, cost, exchange.getExchangeNo());
        return vo;
    }

    @Override
    public List<PointsExchangeVO> myExchanges(Integer limit) {
        int size = clamp(limit, DEFAULT_EXCHANGE_LIMIT, MAX_EXCHANGE_LIMIT);
        List<PointsExchange> exchanges = exchangeMapper.selectPage(
                        new Page<>(1, size, false),
                        new LambdaQueryWrapper<PointsExchange>()
                                .eq(PointsExchange::getUserId, UserContext.getId())
                                .orderByDesc(PointsExchange::getExchangeTime)
                                .orderByDesc(PointsExchange::getId))
                .getRecords();
        if (exchanges.isEmpty()) {
            return List.of();
        }

        Set<Long> goodsIds = exchanges.stream().map(PointsExchange::getGoodsId)
                .collect(Collectors.toSet());
        Map<Long, PointsGoods> goodsMap = goodsMapper.selectBatchIds(goodsIds).stream()
                .collect(Collectors.toMap(PointsGoods::getId, goods -> goods));

        return exchanges.stream().map(exchange -> {
            PointsGoods goods = goodsMap.get(exchange.getGoodsId());
            return toVO(exchange, goods == null ? "商品已删除" : goods.getName());
        }).toList();
    }

    @Override
    public PageResult<PointsExchangeVO> pageExchanges(PointsExchangeQuery query) {
        LambdaQueryWrapper<PointsExchange> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(query.getExchangeNo()),
                        PointsExchange::getExchangeNo, query.getExchangeNo())
                .eq(StringUtils.hasText(query.getStatus()), PointsExchange::getStatus, query.getStatus())
                .eq(query.getUserId() != null, PointsExchange::getUserId, query.getUserId())
                .eq(query.getGoodsId() != null, PointsExchange::getGoodsId, query.getGoodsId())
                .orderByDesc(PointsExchange::getExchangeTime)
                .orderByDesc(PointsExchange::getId);
        Page<PointsExchange> page = exchangeMapper.selectPage(
                new Page<>(query.getPage(), query.getSize()), wrapper);

        List<PointsExchange> records = page.getRecords();
        Set<Long> goodsIds = records.stream().map(PointsExchange::getGoodsId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Set<Long> userIds = records.stream().map(PointsExchange::getUserId)
                .filter(Objects::nonNull).collect(Collectors.toSet());
        Map<Long, PointsGoods> goodsMap = goodsIds.isEmpty() ? Map.of()
                : goodsMapper.selectBatchIds(goodsIds).stream()
                .collect(Collectors.toMap(PointsGoods::getId, goods -> goods));
        Map<Long, SysUser> userMap = userIds.isEmpty() ? Map.of()
                : sysUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, user -> user));

        List<PointsExchangeVO> vos = records.stream().map(exchange -> {
            PointsGoods goods = goodsMap.get(exchange.getGoodsId());
            SysUser user = userMap.get(exchange.getUserId());
            return toVO(exchange,
                    goods == null ? "商品已删除" : goods.getName(),
                    user == null ? "用户已删除" : user.getNickname());
        }).toList();

        PageResult<PointsExchangeVO> result = new PageResult<>();
        result.setTotal(page.getTotal());
        result.setPage(page.getCurrent());
        result.setSize(page.getSize());
        result.setRecords(vos);
        return result;
    }

    @Override
    @Transactional
    public void verifyExchange(Long id) {
        PointsExchange exchange = exchangeMapper.selectById(id);
        if (exchange == null) {
            throw new BusinessException(404, "兑换单不存在");
        }
        if (!STATUS_PENDING.equals(exchange.getStatus())) {
            throw new BusinessException(400, "该兑换单已核销或已取消");
        }

        PointsExchange update = new PointsExchange();
        update.setId(id);
        update.setStatus("VERIFIED");
        update.setVerifyTime(LocalDateTime.now().withNano(0));
        exchangeMapper.updateById(update);
        log.info("兑换单核销 exchangeNo={} operator={}", exchange.getExchangeNo(), UserContext.getId());
    }

    @Override
    public List<PointsRecord> myRecords(Integer limit) {
        int size = clamp(limit, DEFAULT_RECORD_LIMIT, MAX_RECORD_LIMIT);
        return recordMapper.selectPage(
                        new Page<>(1, size, false),
                        new LambdaQueryWrapper<PointsRecord>()
                                .eq(PointsRecord::getUserId, UserContext.getId())
                                .orderByDesc(PointsRecord::getCreateTime)
                                .orderByDesc(PointsRecord::getId))
                .getRecords();
    }

    @Override
    @Transactional
    public void award(Long userId, Long orderId, int points, String remark) {
        if (userId == null || orderId == null || points <= 0) {
            return;
        }
        Long awarded = recordMapper.selectCount(new LambdaQueryWrapper<PointsRecord>()
                .eq(PointsRecord::getType, TYPE_ORDER_GAIN)
                .eq(PointsRecord::getRefId, orderId));
        if (awarded > 0) {
            log.info("订单积分已发放，跳过 orderId={}", orderId);
            return;
        }
        if (sysUserMapper.selectById(userId) == null) {
            log.warn("发放订单积分失败，用户不存在 userId={} orderId={}", userId, orderId);
            return;
        }

        sysUserMapper.update(null, new LambdaUpdateWrapper<SysUser>()
                .eq(SysUser::getId, userId)
                .setSql("points = points + " + points));

        PointsRecord record = new PointsRecord();
        record.setUserId(userId);
        record.setChangeAmount(points);
        record.setType(TYPE_ORDER_GAIN);
        record.setRefId(orderId);
        record.setRemark(remark);
        recordMapper.insert(record);
        log.info("发放订单积分 userId={} orderId={} points={}", userId, orderId, points);
    }

    private PointsExchangeVO toVO(PointsExchange exchange, String goodsName) {
        return toVO(exchange, goodsName, null);
    }

    private PointsExchangeVO toVO(PointsExchange exchange, String goodsName, String userName) {
        PointsExchangeVO vo = new PointsExchangeVO();
        BeanUtils.copyProperties(exchange, vo);
        vo.setGoodsName(goodsName);
        vo.setUserName(userName);
        vo.setStatusText(statusText(exchange.getStatus()));
        return vo;
    }

    private int clamp(Integer limit, int defaultValue, int max) {
        if (limit == null || limit <= 0) {
            return defaultValue;
        }
        return Math.min(limit, max);
    }

    private String nextExchangeNo() {
        return "EX" + LocalDateTime.now().format(EXCHANGE_NO_FORMAT)
                + ThreadLocalRandom.current().nextInt(100, 1000);
    }

    private String statusText(String status) {
        if (status == null) {
            return "";
        }
        return switch (status) {
            case "PENDING" -> "待核销";
            case "VERIFIED" -> "已核销";
            case "CANCELLED" -> "已取消";
            default -> status;
        };
    }
}
