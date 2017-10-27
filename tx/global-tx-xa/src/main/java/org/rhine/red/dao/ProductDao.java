package org.rhine.red.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class ProductDao {

    @Autowired
    @Qualifier(value = "productJdbcTemplate")
    JdbcTemplate jdbcTemplate;

    public void updateProduct() {
        jdbcTemplate.update("UPDATE tb_product SET qty = qty - 1 WHERE id = 1");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public long queryProductQty() {
        return jdbcTemplate.queryForObject("SELECT qty FROM tb_product WHERE id  = 1", Long.class);
    }
}
