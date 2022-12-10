package cn.tedu.mall.front.controller;

import cn.tedu.mall.common.restful.JsonPage;
import cn.tedu.mall.common.restful.JsonResult;
import cn.tedu.mall.front.service.IFrontProductService;
import cn.tedu.mall.pojo.product.vo.AttributeStandardVO;
import cn.tedu.mall.pojo.product.vo.SpuDetailStandardVO;
import cn.tedu.mall.pojo.product.vo.SpuListItemVO;
import cn.tedu.mall.pojo.product.vo.SpuStandardVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.spring.web.json.Json;

import java.util.List;

@RestController
@Api(tags = "前台商品spu模块")
@RequestMapping("/front/spu")
public class FrontSpuController {
    @Autowired
    private IFrontProductService frontProductService;

    @GetMapping("/list/{categoryId}")
    @ApiOperation("根据分类id分页查询spu列表")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "分类id",name="categoryId",example = "3",
                    required = true,dataType = "long"),
            @ApiImplicitParam(value = "页码",name="page",example = "1",
                    required = true,dataType = "int"),
            @ApiImplicitParam(value = "每页条数",name="pageSize",example = "2",
                    required = true,dataType = "int")
    })
    public JsonResult<JsonPage<SpuListItemVO>> listSpuByPage(@PathVariable Long categoryId, Integer page, Integer pageSize){
        JsonPage<SpuListItemVO> jsonPage = frontProductService.listSpuByCategoryId(categoryId, page, pageSize);
        return JsonResult.ok(jsonPage);
    }

    @ApiOperation("根据spuId查询spu信息")
    @GetMapping("/{spuId}")
    @ApiImplicitParam(value = "spuId",name="spuId",example = "1",
            required = true,dataType = "long")
    public JsonResult<SpuStandardVO> getFrontSpuById(@PathVariable Long spuId){
        SpuStandardVO spuStandardVO = frontProductService.getFrontSpuById(spuId);
        return JsonResult.ok(spuStandardVO);
    }


    @GetMapping("/template/{id}")
    @ApiOperation("根据spuId查询所有参数选项")
    @ApiImplicitParam(value = "spuId",name="id",example = "1",
            required = true,dataType = "long")
    public JsonResult<List<AttributeStandardVO>> getAttributesBySpuId(@PathVariable Long id){
        List<AttributeStandardVO> attributes = frontProductService.getSpuAttributesBySpuId(id);
        return JsonResult.ok(attributes);
    }



}
