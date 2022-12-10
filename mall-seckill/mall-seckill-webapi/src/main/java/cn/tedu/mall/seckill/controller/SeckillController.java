package cn.tedu.mall.seckill.controller;

import cn.tedu.mall.common.exception.CoolSharkServiceException;
import cn.tedu.mall.common.restful.JsonResult;
import cn.tedu.mall.common.restful.ResponseCode;
import cn.tedu.mall.pojo.seckill.dto.SeckillOrderAddDTO;
import cn.tedu.mall.pojo.seckill.vo.SeckillCommitVO;
import cn.tedu.mall.seckill.exception.SeckillBlockHandler;
import cn.tedu.mall.seckill.exception.SeckillFallBack;
import cn.tedu.mall.seckill.service.ISeckillService;
import cn.tedu.mall.seckill.utils.SeckillCacheUtils;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.spring.web.json.Json;

@RestController
@Api(tags = "秒杀订单")
@RequestMapping("/seckill")
public class SeckillController {
    @Autowired
    private ISeckillService service;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostMapping("/{randCode}")
    @ApiOperation("生成秒杀订单")
    @PreAuthorize("hasRole('user')")
    @SentinelResource(value = "seckill",
            blockHandlerClass = SeckillBlockHandler.class,blockHandler = "seckillBlock",
            fallbackClass = SeckillFallBack.class,fallback = "FallBack")
    public JsonResult<SeckillCommitVO> commitSeckill(@PathVariable String randCode,
                                                     @Validated SeckillOrderAddDTO seckillOrderAddDTO){
        Long spuId = seckillOrderAddDTO.getSpuId();
        String randCodeKey = SeckillCacheUtils.getRandCodeKey(spuId);
        if (redisTemplate.hasKey(randCodeKey)){
            String redisRandCode = redisTemplate.boundValueOps(randCodeKey).get()+"";
            if (!redisRandCode.equals(randCode)){
                throw new CoolSharkServiceException(ResponseCode.NOT_FOUND,"没有指定商品");
            }
            SeckillCommitVO commitVO = service.commitSeckill(seckillOrderAddDTO);
            return JsonResult.ok(commitVO);
        }else {
            throw new CoolSharkServiceException(ResponseCode.NOT_FOUND,"没有指定商品");
        }
    }
}
