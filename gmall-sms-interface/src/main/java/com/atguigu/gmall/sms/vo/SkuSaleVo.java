package com.atguigu.gmall.sms.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author XieHaidong
 * @create 2021-01-21 21:00
 */
@Data
public class SkuSaleVo {

    private long skuId;

    // 积分优惠信息
    private BigDecimal growBounds;
    private BigDecimal buyBounds;
    private List<Integer> work;

    // 满减优惠信息
    private BigDecimal fullPrice;
    private BigDecimal reducePrice;
    private Integer fullAddOther;

    // 打折优惠信息
    private Integer fullCount;
    private BigDecimal discount;
    private Integer ladderAddOther;
}
