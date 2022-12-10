package cn.tedu.mall.front.service.impl;

import cn.tedu.mall.common.restful.JsonPage;
import cn.tedu.mall.front.service.IFrontProductService;
import cn.tedu.mall.pojo.product.vo.*;
import cn.tedu.mall.product.service.front.IForFrontAttributeService;
import cn.tedu.mall.product.service.front.IForFrontSkuService;
import cn.tedu.mall.product.service.front.IForFrontSpuService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j
@Service
public class FrontProductServiceImpl implements IFrontProductService {
    @DubboReference
    private IForFrontSpuService dubboSpuService;
    @DubboReference
    private IForFrontSkuService dubboSkuService;
    @DubboReference
    private IForFrontAttributeService dubboAttributeService;

    @Override
    public JsonPage<SpuListItemVO> listSpuByCategoryId(Long categoryId, Integer page, Integer pageSize) {
        JsonPage<SpuListItemVO> list = dubboSpuService.listSpuByCategoryId(categoryId, page, pageSize);
        return list;
    }

    @Override
    public SpuStandardVO getFrontSpuById(Long id) {
        SpuStandardVO spuStandardVO = dubboSpuService.getSpuById(id);
        return spuStandardVO;
    }

    @Override
    public List<SkuStandardVO> getFrontSkusBySpuId(Long spuId) {
        List<SkuStandardVO> list = dubboSkuService.getSkusBySpuId(spuId);
        return list;
    }

    @Override
    public SpuDetailStandardVO getSpuDetail(Long spuId) {
        SpuDetailStandardVO spuDetailById = dubboSpuService.getSpuDetailById(spuId);
        return spuDetailById;
    }

    @Override
    public List<AttributeStandardVO> getSpuAttributesBySpuId(Long spuId) {
        List<AttributeStandardVO> attributes = dubboAttributeService.getSpuAttributesBySpuId(spuId);
        return attributes;
    }
}
