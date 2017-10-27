package org.rhine.order.domain.entity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class OrderLine implements Serializable {

    private static final long serialVersionUID = -913759176656468186L;

    private long id;

    private long productId; // 商品id

    private int quantity; // 数量

    private BigDecimal unitPrice; // 单价

    public OrderLine() {

    }

    /**
     *
     * @param productId
     *            商品ID.
     * @param quantity
     *            数量.
     * @param unitPrice
     *            单价.
     */
    public OrderLine(Long productId, Integer quantity,BigDecimal unitPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getTotalAmount() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public long getId() {
        return id;
    }
}