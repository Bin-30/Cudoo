package cn.tedu.mall.order.controller;

import cn.tedu.mall.common.restful.JsonPage;
import cn.tedu.mall.common.restful.JsonResult;
import cn.tedu.mall.order.service.IOmsOrderService;
import cn.tedu.mall.pojo.order.dto.OrderAddDTO;
import cn.tedu.mall.pojo.order.dto.OrderListTimeDTO;
import cn.tedu.mall.pojo.order.dto.OrderStateUpdateDTO;
import cn.tedu.mall.pojo.order.vo.OrderAddVO;
import cn.tedu.mall.pojo.order.vo.OrderListVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Api(tags = "订单模块")
@RequestMapping("/oms/order")
public class OmsOrderController {
    @Autowired
    private IOmsOrderService omsOrderService;

    @ApiOperation("新增订单")
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('ROLE_user')")
    public JsonResult<OrderAddVO> add(@Validated OrderAddDTO orderAddDTO){
        OrderAddVO orderAddVO = omsOrderService.addOrder(orderAddDTO);
        return JsonResult.ok(orderAddVO);
    }

    @GetMapping("/list")
    @ApiOperation("分页查询当前用户指定时间范围订单")
    @PreAuthorize("hasRole('user')")
    public JsonResult<JsonPage<OrderListVO>> listUserOrders(OrderListTimeDTO orderListTimeDTO){
        JsonPage<OrderListVO> jsonPage = omsOrderService.listOrdersBetweenTimes(orderListTimeDTO);
        return JsonResult.ok(jsonPage);
    }

    @PostMapping("/update/state")
    @ApiOperation("修改订单状态")
    @PreAuthorize("hasRole('user')")
    public JsonResult updateState(OrderStateUpdateDTO orderStateUpdateDTO){
        omsOrderService.updateOrderState(orderStateUpdateDTO);
        return JsonResult.ok();
    }
}
