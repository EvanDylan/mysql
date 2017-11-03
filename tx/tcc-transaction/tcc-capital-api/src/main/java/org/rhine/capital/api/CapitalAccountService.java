package org.rhine.capital.api;

import org.mengyun.tcctransaction.api.Compensable;

import java.math.BigDecimal;

/**
 * Created by twinkle.zhou on 16/11/11.
 */
public interface CapitalAccountService {

    @Compensable
    BigDecimal getCapitalAccountByUserId(long userId);
}