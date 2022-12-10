package cn.tedu.mall.seckill.timer.job;

import cn.tedu.mall.pojo.seckill.model.SeckillSku;
import cn.tedu.mall.pojo.seckill.model.SeckillSpu;
import cn.tedu.mall.seckill.mapper.SeckillSkuMapper;
import cn.tedu.mall.seckill.mapper.SeckillSpuMapper;
import cn.tedu.mall.seckill.utils.SeckillCacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.math.RandomUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
public class SeckillInitialJob implements Job {
    @Autowired
    private SeckillSpuMapper seckillSpuMapper;
    @Autowired
    private SeckillSkuMapper seckillSkuMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        LocalDateTime time = LocalDateTime.now().plusMinutes(5);
        List<SeckillSpu> spuByTime = seckillSpuMapper.findSeckillSpuByTime(time);
        for (SeckillSpu spu : spuByTime) {
            Long spuId = spu.getSpuId();
            List<SeckillSku> skuBySpuId = seckillSkuMapper.findSeckillSkuBySpuId(spuId);
            for (SeckillSku sku : skuBySpuId) {
                String stockKey = SeckillCacheUtils.getStockKey(sku.getSkuId());
                if (redisTemplate.hasKey(stockKey)){
                    log.debug("{}号商品已经缓存过了",sku.getSkuId());
                }else {
                    stringRedisTemplate.boundValueOps(stockKey).set(sku.getSeckillStock()+"",
                            125*60*1000+ RandomUtils.nextInt(10000), TimeUnit.MILLISECONDS);
                    log.debug("{}号商品成功缓存",sku.getSkuId());
                }
            }
            String randCodeKey = SeckillCacheUtils.getRandCodeKey(spu.getSpuId());
            if (redisTemplate.hasKey(randCodeKey)){
                String randCode = redisTemplate.boundValueOps(randCodeKey).get() + "";
                log.debug("{}号商品随机码已经缓存过了{}",spu.getSpuId(),randCode);
            }else {
                int randCode = RandomUtils.nextInt(900000) + 10000;
                redisTemplate.boundValueOps(randCodeKey).set(randCode,
                        125*60*1000+ RandomUtils.nextInt(10000), TimeUnit.MILLISECONDS);
                log.debug("{}号商品随机码成功缓存{}",spu.getSpuId(),randCode);
            }
        }
    }
}
