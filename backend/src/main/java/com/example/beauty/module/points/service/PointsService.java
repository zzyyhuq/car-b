package com.example.beauty.module.points.service;

import com.example.beauty.module.points.entity.PointsGoods;
import com.example.beauty.module.points.entity.PointsRecord;
import com.example.beauty.module.points.vo.PointsExchangeVO;

import java.util.List;

public interface PointsService {

    /** 车主端积分商品，只返回上架的 */
    List<PointsGoods> listOnSale();

    /** 当前登录车主兑换商品 */
    PointsExchangeVO exchange(Long goodsId);

    /** 我的兑换记录 */
    List<PointsExchangeVO> myExchanges(Integer limit);

    /** 我的积分流水 */
    List<PointsRecord> myRecords(Integer limit);

    /** 订单完成发放积分，同一订单只发一次 */
    void award(Long userId, Long orderId, int points, String remark);
}
