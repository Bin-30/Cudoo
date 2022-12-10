package cn.tedu.mall.order.mapper;

import cn.tedu.mall.pojo.order.model.OmsCart;
import cn.tedu.mall.pojo.order.vo.CartStandardVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OmsCartMapper {

    OmsCart selectExistsCart(@Param("userId") Long userId,@Param("skuId") Long skuId);

    int insert(OmsCart OmsCart);

    int updateQuantityById(OmsCart omsCart);

    List<CartStandardVO> selectCartByUserId(Long userId);

    int deleteCartByIds(Long[] ids);

    int deleteCartByUserIds(Long userId);

    int deleteCartByUserIdAndSkuId(OmsCart omsCart);
}
