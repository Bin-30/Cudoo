package cn.tedu.mall.seckill.consumer;

import cn.tedu.mall.pojo.seckill.model.Success;
import cn.tedu.mall.seckill.config.RabbitMqComponentConfiguration;
import cn.tedu.mall.seckill.mapper.SeckillSkuMapper;
import cn.tedu.mall.seckill.mapper.SuccessMapper;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = {RabbitMqComponentConfiguration.SECKILL_QUEUE})
public class SeckillQueueConsumer {
    @Autowired
    private SeckillSkuMapper seckillSkuMapper;
    @Autowired
    private SuccessMapper successMapper;

    @RabbitHandler
    public void process(Success success){
        seckillSkuMapper.updateReduceStockBySkuId(success.getSkuId(), success.getQuantity());
        successMapper.saveSuccess(success);
    }
}
