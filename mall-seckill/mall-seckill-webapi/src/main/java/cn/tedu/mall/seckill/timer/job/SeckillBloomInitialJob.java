package cn.tedu.mall.seckill.timer.job;

import cn.tedu.mall.seckill.mapper.SeckillSpuMapper;
import cn.tedu.mall.seckill.utils.RedisBloomUtils;
import cn.tedu.mall.seckill.utils.SeckillCacheUtils;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
/**
public class SeckillBloomInitialJob implements Job {
    @Autowired
    private RedisBloomUtils redisBloomUtils;
    @Autowired
    private SeckillSpuMapper seckillSpuMapper;
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String bloomTodayKey = SeckillCacheUtils.getBloomFilterKey(LocalDate.now());
        String bloomTomorrowKey = SeckillCacheUtils.getBloomFilterKey(LocalDate.now().plusDays(1));
        Long[] spuIds = seckillSpuMapper.findSeckillSpuId();
        String[] spuIdStr = new String[spuIds.length];
        for (int i = 0; i < spuIds.length; i++) {
            spuIdStr[i] = spuIds[i]+"";
        }
        redisBloomUtils.bfmadd(bloomTodayKey,spuIdStr);
        redisBloomUtils.bfmadd(bloomTomorrowKey,spuIdStr);
    }
}
*/