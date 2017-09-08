package org.rhine.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-context.xml"})
@Transactional(transactionManager = "transactionManager")
public class OrderServiceTest {


    @Resource
    OrderService orderService;

    /**
     * 测试下单
     * @throws Exception
     */
    @Test
    public void placeOrder() throws Exception {

        orderService.placeOrder();

    }

    /**
     * 测试事务回滚
     */
    @Test
    public void placeOrderFail() {
        long oldQty = orderService.queryProductQty();
        try {
            orderService.placeOrderFail();
        } catch (Exception e) {

        }
        long newQty = orderService.queryProductQty();
        Assert.assertEquals(oldQty, newQty);
    }


}
