package org.rhine.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Service;
import org.springframework.test.annotation.Commit;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;

@Service
@Transactional
public class OrderService {

    @Resource
    private JdbcTemplate jdbcTemplate;


    public void placeOrder() throws Exception {
        jdbcTemplate.update("UPDATE tb_product SET qty = qty - 1 WHERE id = 1");
        jdbcTemplate.update("INSERT INTO tb_order (productId) VALUES (1)");
    }

    public void placeOrderFail() {
        jdbcTemplate.update("UPDATE tb_product SET qty = qty - 1 WHERE id = 1");
        if (1 == 1) {
            throw new RuntimeException();
        }
        jdbcTemplate.update("INSERT INTO tb_order (productId) VALUES (1)");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long queryProductQty() {
        return jdbcTemplate.queryForObject("SELECT qty FROM tb_product WHERE id  = 1", Long.class);
    }

}
