/*
 * ========================================================================
 * 龙果学院： www.roncoo.com （微信公众号：RonCoo_com）
 * 超级教程系列：《微服务架构的分布式事务解决方案》视频教程
 * 讲师：吴水成（水到渠成），840765167@qq.com
 * 课程地址：http://www.roncoo.com/course/view/7ae3d7eddc4742f78b0548aa8bd9ccdb
 * ========================================================================
 */
package org.rhine.order.domain.repository;

import org.rhine.order.domain.entity.Order;
import org.rhine.order.domain.entity.OrderLine;
import org.rhine.order.infrastructure.dao.OrderLineMapper;
import org.rhine.order.infrastructure.dao.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Created by changming.xie on 4/1/16.
 */
@Repository
public class OrderRepository {

    @Autowired
    OrderMapper orderDao;

    @Autowired
    OrderLineMapper orderLineDao;

    /**
     * 创建订单记录.
     * @param order
     */
    public void createOrder(Order order) {
        orderDao.insert(order);

        for(OrderLine orderLine:order.getOrderLines()) {
            orderLineDao.insert(orderLine);
        }
    }

    /**
     * 更新订单记录.
     * @param order
     */
    public void updateOrder(Order order) {
        orderDao.update(order);
    }
    
    public Order findByMerchantOrderNo(String merchantOrderNo){
        return orderDao.findByMerchantOrderNo(merchantOrderNo);
    }
}
