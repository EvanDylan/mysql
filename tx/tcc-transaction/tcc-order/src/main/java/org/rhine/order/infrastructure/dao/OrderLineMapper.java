package org.rhine.order.infrastructure.dao;

import org.rhine.order.domain.entity.OrderLine;

public interface OrderLineMapper {

    OrderLine findById(Integer id);

    int insert(OrderLine record);

}