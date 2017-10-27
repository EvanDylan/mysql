package org.rhine.capital.service;

import org.rhine.capital.api.CapitalAccountService;
import org.rhine.capital.domain.repository.CapitalAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Created by twinkle.zhou on 16/11/11.
 */
@Service("capitalAccountService")
public class CapitalAccountServiceImpl implements CapitalAccountService {


    @Autowired
    CapitalAccountRepository capitalAccountRepository;

    @Override
    public BigDecimal getCapitalAccountByUserId(long userId) {
        return capitalAccountRepository.findByUserId(userId).getBalanceAmount();
    }
}