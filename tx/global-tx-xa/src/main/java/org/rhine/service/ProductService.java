package org.rhine.service;

import org.rhine.red.dao.OrderDao;
import org.rhine.red.dao.ProductDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Transactional
public class ProductService {

    @Resource
    OrderDao orderDao;

    @Resource
    ProductDao productDao;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long queryProductQty() {
        return productDao.queryProductQty();
    }
}
