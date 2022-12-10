package cn.tedu.mall.order.service.impl;

import cn.tedu.mall.common.exception.CoolSharkServiceException;
import cn.tedu.mall.common.pojo.domain.CsmallAuthenticationInfo;
import cn.tedu.mall.common.restful.JsonPage;
import cn.tedu.mall.common.restful.ResponseCode;
import cn.tedu.mall.order.mapper.OmsCartMapper;
import cn.tedu.mall.order.service.IOmsCartService;
import cn.tedu.mall.pojo.order.dto.CartAddDTO;
import cn.tedu.mall.pojo.order.dto.CartUpdateDTO;
import cn.tedu.mall.pojo.order.model.OmsCart;
import cn.tedu.mall.pojo.order.vo.CartStandardVO;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class OmsCartServiceImpl implements IOmsCartService {
    @Autowired
    private OmsCartMapper omsCartMapper;

    @Override
    public void addCart(CartAddDTO cartDTO) {
        Long userId = getUserId();
        OmsCart omsCart = omsCartMapper.selectExistsCart(userId, cartDTO.getSkuId());
        if (omsCart == null) {
            omsCart = new OmsCart();
            BeanUtils.copyProperties(cartDTO, omsCart);
            omsCart.setUserId(userId);
            omsCartMapper.insert(omsCart);
            return;
        }
        omsCart.setQuantity(cartDTO.getQuantity() + omsCart.getQuantity());
        omsCartMapper.updateQuantityById(omsCart);
    }

    @Override
    public JsonPage<CartStandardVO> listCarts(Integer page, Integer pageSize) {
        if (page == null || pageSize == null) {
            page = 1;
            pageSize = 5;
        }
        PageHelper.startPage(page, pageSize);
        List<CartStandardVO> list = omsCartMapper.selectCartByUserId(getUserId());
        JsonPage<CartStandardVO> jsonPage = JsonPage.restPage(new PageInfo<>(list));
        return jsonPage;
    }

    @Override
    public void removeCart(Long[] ids) {
        int rows = omsCartMapper.deleteCartByIds(ids);
        if (rows == 0) {
            throw new CoolSharkServiceException(ResponseCode.NOT_FOUND, "商品不存在");
        }
    }

    @Override
    public void removeAllCarts() {
        int rows = omsCartMapper.deleteCartByUserIds(getUserId());
        if (rows == 0) {
            throw new CoolSharkServiceException(ResponseCode.NOT_FOUND, "购物车不存在商品");
        }
    }

    @Override
    public void removeUserCarts(OmsCart omsCart) {
        omsCartMapper.deleteCartByUserIdAndSkuId(omsCart);
    }

    @Override
    public void updateQuantity(CartUpdateDTO cartUpdateDTO) {
        OmsCart omsCart = new OmsCart();
        BeanUtils.copyProperties(cartUpdateDTO, omsCart);
        omsCartMapper.updateQuantityById(omsCart);
    }

    public CsmallAuthenticationInfo getUserInfo() {
        UsernamePasswordAuthenticationToken authenticationToken =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        if (authenticationToken == null) {
            throw new CoolSharkServiceException(ResponseCode.UNAUTHORIZED, "没有登录");
        }
        CsmallAuthenticationInfo csmallAuthenticationInfo =
                (CsmallAuthenticationInfo) authenticationToken.getCredentials();
        return csmallAuthenticationInfo;
    }

    public Long getUserId() {
        return getUserInfo().getId();
    }
}
