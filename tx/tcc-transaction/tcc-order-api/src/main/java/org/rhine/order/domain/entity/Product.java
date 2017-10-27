package org.rhine.order.domain.entity;

import java.io.Serializable;

public class Product implements Serializable{

    private static final long serialVersionUID = -6428333846772057747L;

    private Integer productId;

    private Integer shopId;

    private String productName;

    private Long price;

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public Integer getShopId() {
        return shopId;
    }

    public void setShopId(Integer shopId) {
        this.shopId = shopId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName == null ? null : productName.trim();
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }
}