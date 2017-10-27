package org.rhine.order.enums;

public enum OrderStatusEnum {

    DRAFT("创建"),

    PAYING("支付中"),

    CONFIRMED("完成"),

    PAY_FAILED("支付失败");

    private String desc;

    OrderStatusEnum(String desc) {
        this.desc = desc;
    }
}
