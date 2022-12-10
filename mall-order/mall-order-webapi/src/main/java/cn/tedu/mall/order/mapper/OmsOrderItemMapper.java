package cn.tedu.mall.order.mapper;

import cn.tedu.mall.pojo.order.model.OmsOrderItem;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface OmsOrderItemMapper {
    int insertOrderItems(List<OmsOrderItem> omsOrderItemList);
}
