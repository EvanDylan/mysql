package org.rhine.order.infrastructure.dao;

import org.rhine.order.domain.entity.Product;

import java.util.List;

public interface ProductMapper {

    Product findById(long productId);

    List<Product> findByShopId(long shopId);
}