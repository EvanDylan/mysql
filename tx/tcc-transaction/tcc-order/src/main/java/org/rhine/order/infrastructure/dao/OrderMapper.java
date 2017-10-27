package org.rhine.order.infrastructure.dao;

import org.rhine.order.domain.entity.Order;

import java.util.List;

public interface OrderMapper {

    Order findById(Long d);

    List<Order> findAll();

    Order findByMerchantOrderNo(String merchantOrderNo);

    int insert(Order record);

    int update(Order record);
}