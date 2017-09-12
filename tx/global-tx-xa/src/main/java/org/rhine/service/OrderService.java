package org.rhine.service;

import org.rhine.dao.OrderDao;
import org.rhine.dao.ProductDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
public class OrderService {

    @Resource
    OrderDao orderDao;

    @Resource
    ProductDao productDao;

    public void placeOrder() {
        productDao.updateProduct();
        orderDao.insertOrder(1);
    }

    public void placeOrderFail() {
        productDao.updateProduct();
        if (1 == 1) {
            throw new RuntimeException();
        }
        orderDao.insertOrder(1);
    }

}
