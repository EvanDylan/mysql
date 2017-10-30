package org.mengyun.tcctransaction.api;

import java.lang.reflect.Method;

/**
 * Created by changming.xie on 1/18/17.
 * 操作Transaction上下文接口.
 */
public interface TransactionContextEditor {

    public TransactionContext get(Object target, Method method, Object[] args);

    public void set(TransactionContext transactionContext, Object target, Method method, Object[] args);

}
