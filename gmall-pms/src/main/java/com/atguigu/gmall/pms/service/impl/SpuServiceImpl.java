package com.atguigu.gmall.pms.service.impl;

import com.atguigu.gmall.pms.entity.*;
//import com.atguigu.gmall.pms.feign.GmallSmsClient;
import com.atguigu.gmall.pms.mapper.*;
import com.atguigu.gmall.pms.service.*;
import com.atguigu.gmall.pms.vo.SkuVo;
import com.atguigu.gmall.pms.vo.SpuAttrValueVo;
import com.atguigu.gmall.pms.vo.SpuVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;


@Service("spuService")
public class SpuServiceImpl extends ServiceImpl<SpuMapper, SpuEntity> implements SpuService {



    @Autowired
    private SpuDescService descService;

    @Autowired
    private SpuAttrValueService baseAttrService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private SkuImagesService imagesService;

    @Autowired
    private SkuAttrValueService saleAttrService;

//    @Autowired
//    private GmallSmsClient smsClient;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<SpuEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<SpuEntity>()
        );

        return new PageResultVo(page);
    }

    @Override
    public PageResultVo querySpuByCidAndPage(PageParamVo pageParamVo, Long cid) {
        QueryWrapper<SpuEntity> wrapper = new QueryWrapper<>();

        // category_id=225 and (id=7 or name like '%7%')
        // 分类id条件
        if (cid != 0){
            wrapper.eq("category_id", cid);
        }

        // 关键字查询
        String key = pageParamVo.getKey();
        if (StringUtils.isNotBlank(key)){
            wrapper.and(t -> t.eq("id", key).or().like("name", key));
        }

        IPage<SpuEntity> page = this.page(
                pageParamVo.getPage(),
                wrapper
        );

        return new PageResultVo(page);
    }






    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void bigSave(SpuVo spu) {
        // 1.先保存spu相关信息
        // 1.1. 保存pms_spu---------------------------------------
        Long spuId = saveSpu(spu);

        // 1.2. 保存pms_spu_desc
        //this.saveSpuDesc(spu, spuId);
        this.descService.saveSpuDesc(spu, spuId);

//        int i = 1/0;

        // 1.3. 保存pms_spu_attr_value-----------------------------
        saveBaseAttr(spu, spuId);

        // 2.再保存sku相关信息
        saveSkuInfo(spu, spuId);
    }



    // 1.1. 保存pms_spu
    private Long saveSpu(SpuVo spu) {
        spu.setCreateTime(new Date());
        spu.setUpdateTime(spu.getCreateTime());
        this.save(spu);
        return spu.getId();
    }



    // 1.3. 保存pms_spu_attr_value   保存基本属性
    private void saveBaseAttr(SpuVo spu, Long spuId) {
        List<SpuAttrValueVo> baseAttrs = spu.getBaseAttrs();
        if (!CollectionUtils.isEmpty(baseAttrs)){
//         需要的是entity 集合，而不是VO集合
//            vo转entity
            this.baseAttrService.saveBatch(baseAttrs.stream().map(spuAttrValueVo -> {
                SpuAttrValueEntity spuAttrValueEntity = new SpuAttrValueEntity();
                BeanUtils.copyProperties(spuAttrValueVo, spuAttrValueEntity);
                spuAttrValueEntity.setSpuId(spuId);
                return spuAttrValueEntity;
            }).collect(Collectors.toList()));
        }
    }




    // 2.再保存sku相关信息
    private void saveSkuInfo(SpuVo spu, Long spuId) {

        List<SkuVo> skus = spu.getSkus();
        if (CollectionUtils.isEmpty(skus)){
            return;
        }
        // 遍历
        skus.forEach(sku -> {
            // 2.1. 保存pms_sku
            sku.setSpuId(spuId);
            sku.setCategoryId(spu.getCategoryId());
            sku.setBrandId(spu.getBrandId());
            // 设置默认图片
            List<String> images = sku.getImages();
            if (!CollectionUtils.isEmpty(images)){
                sku.setDefaultImage(StringUtils.isNotBlank(sku.getDefaultImage()) ? sku.getDefaultImage() : images.get(0));
            }
            this.skuMapper.insert(sku);
            Long skuId = sku.getId();


            // 2.2. 保存pms_sku_images
            if (!CollectionUtils.isEmpty(images)){

                this.imagesService.saveBatch(images.stream().map(image -> {
                    SkuImagesEntity skuImagesEntity = new SkuImagesEntity();
                    skuImagesEntity.setUrl(image);
                    skuImagesEntity.setSkuId(skuId);
                    skuImagesEntity.setDefaultStatus(StringUtils.equals(sku.getDefaultImage(), image) ? 1 : 0);
                    return skuImagesEntity;
                }).collect(Collectors.toList()));
            }


            // 2.3. 保存pms_sku_attr_value
            List<SkuAttrValueEntity> saleAttrs = sku.getSaleAttrs();
            if (!CollectionUtils.isEmpty(saleAttrs)){
                saleAttrs.forEach(skuAttrValueEntity -> skuAttrValueEntity.setSkuId(skuId));
                this.saleAttrService.saveBatch(saleAttrs);
            }


//            // 3.最后保存营销信息
//            SkuSaleVo skuSaleVo = new SkuSaleVo();
//            BeanUtils.copyProperties(sku, skuSaleVo);
//            skuSaleVo.setSkuId(skuId);
//            this.smsClient.saveSales(skuSaleVo);
        });
    }




}