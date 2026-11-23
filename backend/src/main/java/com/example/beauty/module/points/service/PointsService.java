package com.example.beauty.module.points.service;

import com.example.beauty.common.PageResult;
import com.example.beauty.module.points.dto.PointsExchangeQuery;
import com.example.beauty.module.points.dto.PointsGoodsQuery;
import com.example.beauty.module.points.dto.PointsGoodsSaveDTO;
import com.example.beauty.module.points.entity.PointsGoods;
import com.example.beauty.module.points.entity.PointsRecord;
import com.example.beauty.module.points.vo.PointsExchangeVO;

import java.util.List;

public interface PointsService {

    /** 车主端积分商品，只返回上架的 */
    List<PointsGoods> listOnSale();

    /** 管理端商品分页 */
    PageResult<PointsGoods> pageGoods(PointsGoodsQuery query);

    Long createGoods(PointsGoodsSaveDTO dto);

    void updateGoods(Long id, PointsGoodsSaveDTO dto);

    void updateGoodsStatus(Long id, Integer status);

    /** 逻辑删除 */
    void deleteGoods(Long id);

    /** 当前登录车主兑换商品 */
    PointsExchangeVO exchange(Long goodsId);

    /** 我的兑换记录 */
    List<PointsExchangeVO> myExchanges(Integer limit);

    /** 管理端兑换单分页 */
    PageResult<PointsExchangeVO> pageExchanges(PointsExchangeQuery query);

    /** 到店核销兑换单：PENDING → VERIFIED */
    void verifyExchange(Long id);

    /** 我的积分流水 */
    List<PointsRecord> myRecords(Integer limit);

    /** 订单完成发放积分，同一订单只发一次 */
    void award(Long userId, Long orderId, int points, String remark);
}
