package org.rhine.order.infrastructure.dao;

import org.rhine.order.domain.entity.Shop;

public interface ShopMapper {

    Shop findById(long id);

}