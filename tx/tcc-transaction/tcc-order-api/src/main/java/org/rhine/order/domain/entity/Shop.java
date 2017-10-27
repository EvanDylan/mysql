package org.rhine.order.domain.entity;

import java.io.Serializable;

public class Shop implements Serializable {

    private static final long serialVersionUID = 1156794348486795538L;

    private Integer shopId;

    private Integer ownerUserId;

    public Integer getShopId() {
        return shopId;
    }

    public void setShopId(Integer shopId) {
        this.shopId = shopId;
    }

    public Integer getOwnerUserId() {
        return ownerUserId;
    }

    public void setOwnerUserId(Integer ownerUserId) {
        this.ownerUserId = ownerUserId;
    }
}