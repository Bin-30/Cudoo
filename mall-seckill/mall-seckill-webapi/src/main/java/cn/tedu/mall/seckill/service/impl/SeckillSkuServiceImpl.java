package cn.tedu.mall.seckill.service.impl;


import cn.tedu.mall.pojo.product.vo.SkuStandardVO;
import cn.tedu.mall.pojo.seckill.model.SeckillSku;
import cn.tedu.mall.pojo.seckill.vo.SeckillSkuVO;
import cn.tedu.mall.product.service.seckill.IForSeckillSkuService;
import cn.tedu.mall.seckill.mapper.SeckillSkuMapper;
import cn.tedu.mall.seckill.service.ISeckillSkuService;
import cn.tedu.mall.seckill.utils.SeckillCacheUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillSkuServiceImpl implements ISeckillSkuService {
    @Autowired
    private SeckillSkuMapper seckillSkuMapper;
    @DubboReference
    private IForSeckillSkuService dubboSkuService;
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<SeckillSkuVO> listSeckillSkus(Long spuId) {
        List<SeckillSkuVO> list = new ArrayList<>();
        List<SeckillSku> seckillSkus = seckillSkuMapper.findSeckillSkuBySpuId(spuId);
        for (SeckillSku seckillSku : seckillSkus) {
            SeckillSkuVO seckillSkuVO = null;
            Long skuId = seckillSku.getSkuId();
            String skuVOKey = SeckillCacheUtils.getSeckillSkuVOKey(skuId);
            if (redisTemplate.hasKey(skuVOKey)) {
                seckillSkuVO = (SeckillSkuVO) redisTemplate.boundValueOps(skuVOKey).get();
            } else {
                seckillSkuVO = new SeckillSkuVO();
                SkuStandardVO skuStandardVO = dubboSkuService.getById(skuId);
                BeanUtils.copyProperties(skuStandardVO, seckillSkuVO);
                seckillSkuVO.setSeckillLimit(seckillSku.getSeckillLimit());
                seckillSkuVO.setSeckillPrice(seckillSku.getSeckillPrice());
                seckillSkuVO.setStock(seckillSku.getSeckillStock());
                seckillSkuVO.setPrice(seckillSku.getSeckillPrice());
                redisTemplate.boundValueOps(skuVOKey).set(seckillSkuVO,
                        120 * 60 * 1000 + RandomUtils.nextInt(10000), TimeUnit.MILLISECONDS);
            }
            list.add(seckillSkuVO);
        }
        return list;
    }
}
