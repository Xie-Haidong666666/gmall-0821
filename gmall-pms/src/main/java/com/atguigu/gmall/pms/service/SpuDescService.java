package com.atguigu.gmall.pms.service;

import com.atguigu.gmall.pms.vo.SpuVo;
import com.baomidou.mybatisplus.extension.service.IService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;
import com.atguigu.gmall.pms.entity.SpuDescEntity;

import java.util.Map;

/**
 * spu信息介绍
 *
 * @author XieHaiDong
 * @email 1343851470@qq.com
 * @date 2021-01-18 23:17:03
 */
public interface SpuDescService extends IService<SpuDescEntity> {
    void saveSpuDesc(SpuVo spu, Long spuId);
    PageResultVo queryPage(PageParamVo paramVo);
}

