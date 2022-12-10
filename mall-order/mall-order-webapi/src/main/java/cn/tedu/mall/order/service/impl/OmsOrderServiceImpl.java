package cn.tedu.mall.order.service.impl;

import cn.tedu.mall.common.exception.CoolSharkServiceException;
import cn.tedu.mall.common.pojo.domain.CsmallAuthenticationInfo;
import cn.tedu.mall.common.restful.JsonPage;
import cn.tedu.mall.common.restful.ResponseCode;
import cn.tedu.mall.order.mapper.OmsCartMapper;
import cn.tedu.mall.order.mapper.OmsOrderItemMapper;
import cn.tedu.mall.order.mapper.OmsOrderMapper;
import cn.tedu.mall.order.service.IOmsCartService;
import cn.tedu.mall.order.service.IOmsOrderService;
import cn.tedu.mall.order.utils.IdGeneratorUtils;
import cn.tedu.mall.pojo.order.dto.OrderAddDTO;
import cn.tedu.mall.pojo.order.dto.OrderItemAddDTO;
import cn.tedu.mall.pojo.order.dto.OrderListTimeDTO;
import cn.tedu.mall.pojo.order.dto.OrderStateUpdateDTO;
import cn.tedu.mall.pojo.order.model.OmsCart;
import cn.tedu.mall.pojo.order.model.OmsOrder;
import cn.tedu.mall.pojo.order.model.OmsOrderItem;
import cn.tedu.mall.pojo.order.vo.OrderAddVO;
import cn.tedu.mall.pojo.order.vo.OrderDetailVO;
import cn.tedu.mall.pojo.order.vo.OrderListVO;
import cn.tedu.mall.product.service.order.IForOrderSkuService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@DubboService
public class OmsOrderServiceImpl implements IOmsOrderService {
    @Autowired
    private OmsOrderMapper orderMapper;
    @Autowired
    private OmsOrderItemMapper orderItemMapper;
    @Autowired
    private IOmsCartService omsCartService;
    @DubboReference
    private IForOrderSkuService dubboSkuService;

    @GlobalTransactional
    @Override
    public OrderAddVO addOrder(OrderAddDTO orderAddDTO) {
        OmsOrder order = new OmsOrder();
        BeanUtils.copyProperties(orderAddDTO,order);
        loadOrder(order);

        List<OrderItemAddDTO> itemAddDTOS = orderAddDTO.getOrderItems();
        if (itemAddDTOS==null||itemAddDTOS.isEmpty()){
            throw new CoolSharkServiceException(ResponseCode.NOT_FOUND,"必须包含一件商品");
        }
        List<OmsOrderItem> omsOrderItems = new ArrayList<>();
        for (OrderItemAddDTO addDTO : itemAddDTOS) {
            OmsOrderItem orderItem = new OmsOrderItem();
            BeanUtils.copyProperties(addDTO,orderItem);
            orderItem.setId(IdGeneratorUtils.getDistributeId("order_item"));
            orderItem.setOrderId(order.getId());
            omsOrderItems.add(orderItem);

            int rows = dubboSkuService.reduceStockNum(orderItem.getSkuId(), orderItem.getQuantity());
            if (rows==0){
                throw new CoolSharkServiceException(ResponseCode.BAD_REQUEST,
                        "库存不足!");
            }

            OmsCart omsCart = new OmsCart();
            omsCart.setUserId(order.getUserId());
            omsCart.setSkuId(orderItem.getSkuId());
            omsCartService.removeUserCarts(omsCart);
        }
        orderMapper.insertOrder(order);
        orderItemMapper.insertOrderItems(omsOrderItems);

        OrderAddVO orderAddVO = new OrderAddVO();
        BeanUtils.copyProperties(order,orderAddVO);
        orderAddVO.setPayAmount(order.getAmountOfActualPay());
        orderAddVO.setCreateTime(order.getGmtCreate());

        return orderAddVO;
    }

    private void loadOrder(OmsOrder order) {
        order.setId(IdGeneratorUtils.getDistributeId("order"));
        order.setSn(UUID.randomUUID().toString());
        if (order.getState()==null){
            order.setState(0);
        }

        if (order.getUserId()==null){
            order.setUserId(getUserId());
        }

        BigDecimal discount = order.getAmountOfDiscount();
        BigDecimal freight = order.getAmountOfFreight();
        BigDecimal price = order.getAmountOfOriginalPrice();
        BigDecimal actualPay = price.add(freight).subtract(discount);
        order.setAmountOfActualPay(actualPay);

        LocalDateTime time = LocalDateTime.now();
        order.setGmtOrder(time);
        order.setGmtModified(time);
        order.setGmtCreate(time);
    }

    @Override
    public void updateOrderState(OrderStateUpdateDTO orderStateUpdateDTO) {
        OmsOrder order = new OmsOrder();
        BeanUtils.copyProperties(orderStateUpdateDTO,order);
        orderMapper.updateStateById(order);
    }

    @Override
    public JsonPage<OrderListVO> listOrdersBetweenTimes(OrderListTimeDTO orderListTimeDTO) {
        validaTime(orderListTimeDTO);
        orderListTimeDTO.setUserId(getUserId());
        PageHelper.startPage(orderListTimeDTO.getPage(), orderListTimeDTO.getPageSize());

        List<OrderListVO> list = orderMapper.selectOrdersBetweenTimes(orderListTimeDTO);
        return JsonPage.restPage(new PageInfo<>(list));
    }

    private void validaTime(OrderListTimeDTO orderListTimeDTO) {
        LocalDateTime start = orderListTimeDTO.getStartTime();
        LocalDateTime end = orderListTimeDTO.getEndTime();
        if (start==null||end==null){
            start = LocalDateTime.now().minusMonths(1);
            end = LocalDateTime.now();
            orderListTimeDTO.setStartTime(start);
            orderListTimeDTO.setEndTime(end);
        }else {
            if (end.toInstant(ZoneOffset.of("+8")).toEpochMilli() < start.toInstant(ZoneOffset.of("+8")).toEpochMilli()){
                throw new CoolSharkServiceException(ResponseCode.BAD_REQUEST,
                        "结束时间应大于起始时间!");
            }
        }
    }

    @Override
    public OrderDetailVO getOrderDetail(Long id) {
        return null;
    }

    public CsmallAuthenticationInfo getUserInfo(){
        UsernamePasswordAuthenticationToken authenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        if (authenticationToken==null){
            throw new CoolSharkServiceException(ResponseCode.UNAUTHORIZED,"没有登录");
        }
        CsmallAuthenticationInfo csmallAuthenticationInfo =
                (CsmallAuthenticationInfo) authenticationToken.getCredentials();
        return csmallAuthenticationInfo;
    }

    public Long getUserId(){
        return getUserInfo().getId();
    }
}
