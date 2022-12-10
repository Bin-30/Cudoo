package cn.tedu.mall.seckill.mapper;

import cn.tedu.mall.pojo.seckill.model.SeckillSku;
import cn.tedu.mall.pojo.seckill.vo.SeckillSkuVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeckillSkuMapper {

    List<SeckillSku> findSeckillSkuBySpuId(Long spuId);

    void updateReduceStockBySkuId(@Param("skuId") Long skuId,@Param("quantity") Integer quantity);
}
