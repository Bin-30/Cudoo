package cn.tedu.mall.order.mapper;

import cn.tedu.mall.pojo.order.dto.OrderListTimeDTO;
import cn.tedu.mall.pojo.order.model.OmsOrder;
import cn.tedu.mall.pojo.order.vo.OrderListVO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OmsOrderMapper {
    int insertOrder(OmsOrder order);

    List<OrderListVO> selectOrdersBetweenTimes(OrderListTimeDTO orderListTimeDTO);

    int updateStateById(OmsOrder order);
}
