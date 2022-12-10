package cn.tedu.mall.seckill.mapper;

import cn.tedu.mall.pojo.seckill.model.SeckillSpu;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SeckillSpuMapper {

    List<SeckillSpu> selectSeckillSpu();

    List<SeckillSpu> findSeckillSpuByTime(LocalDateTime time);

    SeckillSpu findSeckillSpuById(Long spuId);

    Long[] findSeckillSpuId();
}
