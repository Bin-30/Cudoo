package cn.tedu.mall.seckill.service.impl;

import cn.tedu.mall.common.exception.CoolSharkServiceException;
import cn.tedu.mall.common.pojo.domain.CsmallAuthenticationInfo;
import cn.tedu.mall.common.restful.ResponseCode;
import cn.tedu.mall.order.service.IOmsOrderService;
import cn.tedu.mall.pojo.order.dto.OrderAddDTO;
import cn.tedu.mall.pojo.order.dto.OrderItemAddDTO;
import cn.tedu.mall.pojo.order.vo.OrderAddVO;
import cn.tedu.mall.pojo.seckill.dto.SeckillOrderAddDTO;
import cn.tedu.mall.pojo.seckill.dto.SeckillOrderItemAddDTO;
import cn.tedu.mall.pojo.seckill.model.Success;
import cn.tedu.mall.pojo.seckill.vo.SeckillCommitVO;
import cn.tedu.mall.seckill.config.RabbitMqComponentConfiguration;
import cn.tedu.mall.seckill.service.ISeckillService;
import cn.tedu.mall.seckill.utils.SeckillCacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SeckillServiceImpl implements ISeckillService{
    @DubboReference
    private IOmsOrderService dubboOrderService;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public SeckillCommitVO commitSeckill(SeckillOrderAddDTO seckillOrderAddDTO) {
        Long skuId = seckillOrderAddDTO.getSeckillOrderItemAddDTO().getSkuId();
        Long userId = getUserId();
        String checkKey = SeckillCacheUtils.getReseckillCheckKey(skuId, userId);
        Long seckillTimes = stringRedisTemplate.boundValueOps(checkKey).increment();
        if (seckillTimes>1){
            throw new CoolSharkServiceException(ResponseCode.NOT_ACCEPTABLE,"只能购买一次");
        }

        String stockKey = SeckillCacheUtils.getStockKey(skuId);
        Long afterStock = stringRedisTemplate.boundValueOps(stockKey).decrement();
//        if (afterStock<0){
//            throw new CoolSharkServiceException(ResponseCode.NOT_ACCEPTABLE,"商品已售罄");
//        }

        OrderAddDTO orderAddDTO = convertSeckillOrderToOrder(seckillOrderAddDTO);
        orderAddDTO.setUserId(userId);

        OrderAddVO orderAddVO = dubboOrderService.addOrder(orderAddDTO);

        Success success = new Success();
        BeanUtils.copyProperties(seckillOrderAddDTO.getSeckillOrderItemAddDTO(),success);
        success.setUserId(userId);
        success.setOrderSn(orderAddVO.getSn());
        success.setSeckillPrice(seckillOrderAddDTO.getSeckillOrderItemAddDTO().getPrice());
        rabbitTemplate.convertAndSend(
                RabbitMqComponentConfiguration.SECKILL_EX,
                RabbitMqComponentConfiguration.SECKILL_RK,
                success);

        SeckillCommitVO commitVO = new SeckillCommitVO();
        BeanUtils.copyProperties(orderAddVO,commitVO);
        return commitVO;
    }

    private OrderAddDTO convertSeckillOrderToOrder(SeckillOrderAddDTO seckillOrderAddDTO) {
        OrderAddDTO orderAddDTO = new OrderAddDTO();
        BeanUtils.copyProperties(seckillOrderAddDTO,orderAddDTO);
        SeckillOrderItemAddDTO seckillOrderItemAddDTO = seckillOrderAddDTO.getSeckillOrderItemAddDTO();
        OrderItemAddDTO orderItemAddDTO = new OrderItemAddDTO();
        BeanUtils.copyProperties(seckillOrderItemAddDTO,orderItemAddDTO);
        List<OrderItemAddDTO> list = new ArrayList<>();
        list.add(orderItemAddDTO);
        orderAddDTO.setOrderItems(list);
        return orderAddDTO;
    }


    public CsmallAuthenticationInfo getUserInfo(){
        // 声明SpringSecurity上下文对象
        UsernamePasswordAuthenticationToken authenticationToken=
                (UsernamePasswordAuthenticationToken)
                        SecurityContextHolder.getContext().getAuthentication();
        // 为了保险起见,判断一下这个对象是否为空
        if(authenticationToken==null){
            throw new CoolSharkServiceException(ResponseCode.UNAUTHORIZED,"没有登录");
        }
        // 从上下文中获取登录用户的信息
        // 这个信息是由JWT解析获得的
        CsmallAuthenticationInfo csmallAuthenticationInfo=
                (CsmallAuthenticationInfo) authenticationToken.getCredentials();
        // 返回登录信息
        return csmallAuthenticationInfo;
    }
    // 业务逻辑层大多数方法都是只需要用户的ID,所以专门编写一个方法返回id
    public Long getUserId(){
        return getUserInfo().getId();
    }

}
