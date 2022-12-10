package cn.tedu.mall.order.controller;

import cn.tedu.mall.common.restful.JsonPage;
import cn.tedu.mall.common.restful.JsonResult;
import cn.tedu.mall.order.service.IOmsCartService;
import cn.tedu.mall.pojo.order.dto.CartAddDTO;
import cn.tedu.mall.pojo.order.dto.CartUpdateDTO;
import cn.tedu.mall.pojo.order.vo.CartStandardVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/oms/cart")
@Api(tags = "购物车管理模块")
public class OmsCartController {
    @Autowired
    private IOmsCartService omsCartService;

    @ApiOperation("添加购物车")
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_user')")
    public JsonResult addCart(CartAddDTO cartDTO){
        omsCartService.addCart(cartDTO);
        return JsonResult.ok("添加购物车完成");
    }


    @ApiOperation("分页查询购物车信息")
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('ROLE_user')")
    @ApiImplicitParams({
            @ApiImplicitParam(value = "页码",name="page",dataType = "int"),
            @ApiImplicitParam(value = "每页条数",name="pageSize",dataType = "int")
    })
    public JsonResult<JsonPage<CartStandardVO>> listCartByPage(Integer page, Integer pageSize){
        JsonPage<CartStandardVO> jsonPage = omsCartService.listCarts(page, pageSize);
        return JsonResult.ok(jsonPage);
    }

    @PostMapping("/delete")
    @ApiOperation("根据id数组删除购物车中的sku信息")
    @ApiImplicitParam(value = "要删除的id数组",name="ids",
            required = true,dataType = "array")
    @PreAuthorize("hasRole('user')")
    public JsonResult removeCartsByIds(Long[] ids){
        omsCartService.removeCart(ids);
        return JsonResult.ok("运行了删除功能!");
    }

    @PostMapping("/delete/all")
    @ApiOperation("清空当前用户的购物车")
    @PreAuthorize("hasRole('user')")
    public JsonResult removeCartsByUserId(){
        omsCartService.removeAllCarts();
        return JsonResult.ok("购物车已清空");
    }

    // 修改购物车数量
    @PostMapping("/update/quantity")
    @ApiOperation("修改购物车数量")
    @PreAuthorize("hasRole('user')")
    public JsonResult updateQuantity(@Validated CartUpdateDTO cartUpdateDTO){
        omsCartService.updateQuantity(cartUpdateDTO);
        return JsonResult.ok("修改完成!");
    }
}
