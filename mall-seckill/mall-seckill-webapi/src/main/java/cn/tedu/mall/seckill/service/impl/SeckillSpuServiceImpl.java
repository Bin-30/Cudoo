package cn.tedu.mall.seckill.service.impl;

import cn.tedu.mall.common.exception.CoolSharkServiceException;
import cn.tedu.mall.common.restful.JsonPage;
import cn.tedu.mall.common.restful.ResponseCode;
import cn.tedu.mall.pojo.product.vo.SpuDetailStandardVO;
import cn.tedu.mall.pojo.product.vo.SpuStandardVO;
import cn.tedu.mall.pojo.seckill.model.SeckillSpu;
import cn.tedu.mall.pojo.seckill.vo.SeckillSkuVO;
import cn.tedu.mall.pojo.seckill.vo.SeckillSpuDetailSimpleVO;
import cn.tedu.mall.pojo.seckill.vo.SeckillSpuVO;
import cn.tedu.mall.product.service.seckill.IForSeckillSpuService;
import cn.tedu.mall.seckill.mapper.SeckillSpuMapper;
import cn.tedu.mall.seckill.service.ISeckillSpuService;
import cn.tedu.mall.seckill.utils.RedisBloomUtils;
import cn.tedu.mall.seckill.utils.SeckillCacheUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang.math.RandomUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillSpuServiceImpl implements ISeckillSpuService {
    @Autowired
    private SeckillSpuMapper seckillSpuMapper;
    @DubboReference
    private IForSeckillSpuService dubboSpuService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedisBloomUtils redisBloomUtils;

    public static final String SECKILL_SPU_DETAIL_VO_PREFIX = "seckill:spu:detail:vo:";

    @Override
    public JsonPage<SeckillSpuVO> listSeckillSpus(Integer page, Integer pageSize) {
        PageHelper.startPage(page,pageSize);
        List<SeckillSpu> seckillSpus = seckillSpuMapper.selectSeckillSpu();
        List<SeckillSpuVO> spuVOS = new ArrayList<>();
        for (SeckillSpu spu : seckillSpus) {
            Long spuId = spu.getSpuId();
            SpuStandardVO spuStandardVO = dubboSpuService.getSpuById(spuId);
            SeckillSpuVO seckillSpuVO = new SeckillSpuVO();
            BeanUtils.copyProperties(spuStandardVO,seckillSpuVO);
            seckillSpuVO.setSeckillListPrice(spu.getListPrice());
            seckillSpuVO.setStartTime(spu.getStartTime());
            seckillSpuVO.setEndTime(spu.getEndTime());
            spuVOS.add(seckillSpuVO);
        }
        return JsonPage.restPage(new PageInfo<>(spuVOS));
    }

    @Override
    public SeckillSpuVO getSeckillSpu(Long spuId) {
//        String bloomFilterKey = SeckillCacheUtils.getBloomFilterKey(LocalDate.now());
//        if (!redisBloomUtils.bfexists(bloomFilterKey,spuId+"")){
//            throw new CoolSharkServiceException(ResponseCode.NOT_FOUND,"商品不存在");
//        }
        SeckillSpuVO seckillSpuVO = null;
        String spuVOKey = SeckillCacheUtils.getSeckillSpuVOKey(spuId);
        if (redisTemplate.hasKey(spuVOKey)){
            seckillSpuVO = (SeckillSpuVO) redisTemplate.boundValueOps(spuVOKey).get();
        }else {
            SeckillSpu seckillSpu = seckillSpuMapper.findSeckillSpuById(spuId);
            if(seckillSpu==null){
                throw new CoolSharkServiceException(ResponseCode.NOT_FOUND,
                        "您访问的商品不存在");
            }
            SpuStandardVO spuStandardVO = dubboSpuService.getSpuById(spuId);
            seckillSpuVO = new SeckillSpuVO();
            BeanUtils.copyProperties(spuStandardVO,seckillSpuVO);
            seckillSpuVO.setSeckillListPrice(seckillSpu.getListPrice());
            seckillSpuVO.setStartTime(seckillSpu.getStartTime());
            seckillSpuVO.setEndTime(seckillSpu.getEndTime());
            redisTemplate.boundValueOps(spuVOKey).set(seckillSpuVO,
                    120*60*1000+RandomUtils.nextInt(10000),TimeUnit.MILLISECONDS);
        }
        LocalDateTime now = LocalDateTime.now();
        Duration afterTime = Duration.between(now, seckillSpuVO.getStartTime());
        Duration beforeTime = Duration.between(seckillSpuVO.getEndTime(), now);
        if (afterTime.isNegative()&&beforeTime.isNegative()){
            String randCodeKey = SeckillCacheUtils.getRandCodeKey(spuId);
            String randCode = redisTemplate.boundValueOps(randCodeKey).get() + "";
            seckillSpuVO.setUrl("/seckill/"+randCode);
            System.out.println("--------url赋值随机码为:"+randCode+"---------");
        }
        return seckillSpuVO;
    }

    @Override
    public SeckillSpuDetailSimpleVO getSeckillSpuDetail(Long spuId) {
        String seckillDetailKey = SECKILL_SPU_DETAIL_VO_PREFIX+spuId;
        SeckillSpuDetailSimpleVO detailSimpleVO;
        if (redisTemplate.hasKey(seckillDetailKey)){
            detailSimpleVO= (SeckillSpuDetailSimpleVO) redisTemplate.boundValueOps(seckillDetailKey).get();
            return detailSimpleVO;
        }
        SpuDetailStandardVO spuDetailById = dubboSpuService.getSpuDetailById(spuId);
        detailSimpleVO = new SeckillSpuDetailSimpleVO();
        BeanUtils.copyProperties(spuDetailById,detailSimpleVO);
        redisTemplate.boundValueOps(seckillDetailKey).set(detailSimpleVO,
                120*60*1000+ RandomUtils.nextInt(10000), TimeUnit.MILLISECONDS);
        return detailSimpleVO;
    }
}
