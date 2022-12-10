package cn.tedu.mall.seckill.exception;

import cn.tedu.mall.common.restful.JsonResult;
import cn.tedu.mall.common.restful.ResponseCode;
import cn.tedu.mall.pojo.seckill.dto.SeckillOrderAddDTO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SeckillFallBack {
    public static JsonResult fallBack(String randCode, SeckillOrderAddDTO seckillOrderAddDTO,
                                      Throwable throwable){
        log.error("降级处理");
        return JsonResult.failed(ResponseCode.INTERNAL_SERVER_ERROR,throwable.getMessage());
    }
}
