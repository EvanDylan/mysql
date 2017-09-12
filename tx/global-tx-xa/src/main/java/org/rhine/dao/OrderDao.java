package org.rhine.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OrderDao {

    @Autowired
    @Qualifier(value = "orderJdbcTemplate")
    JdbcTemplate jdbcTemplate;

    public void insertOrder(int productId) {
        jdbcTemplate.update("INSERT INTO tb_order (productId) VALUES (?)", productId);
    }

}
