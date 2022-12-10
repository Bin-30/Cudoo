package cn.tedu.mall.seckill.exception;

import cn.tedu.mall.common.restful.JsonResult;
import cn.tedu.mall.common.restful.ResponseCode;
import cn.tedu.mall.pojo.seckill.dto.SeckillOrderAddDTO;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SeckillBlockHandler {

    public static JsonResult seckillBlock(String randCode, SeckillOrderAddDTO seckillOrderAddDTO,
                                          BlockException e){
        log.error("一个请求被限流了");
        return JsonResult.failed(ResponseCode.INTERNAL_SERVER_ERROR,"请稍后再试！");
    }
}
