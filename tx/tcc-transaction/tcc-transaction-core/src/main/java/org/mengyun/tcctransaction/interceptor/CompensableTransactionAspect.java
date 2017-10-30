package org.mengyun.tcctransaction.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Created by changmingxie on 10/30/15.
 */
@Aspect
public abstract class CompensableTransactionAspect {

    /**
     * 由子类{@code ConfigurableTransactionAspect.class}初始化执行拦截后处理工作的{@code CompensableTransactionInterceptor.class}
     */
    private CompensableTransactionInterceptor compensableTransactionInterceptor;

    public void setCompensableTransactionInterceptor(CompensableTransactionInterceptor compensableTransactionInterceptor) {
        this.compensableTransactionInterceptor = compensableTransactionInterceptor;
    }

    /**
     * 切面,拦截所有加了{@code Compensable.class}注解的方法
     */
    @Pointcut("@annotation(org.mengyun.tcctransaction.api.Compensable)")
    public void compensableService() {

    }

    /**
     * 环绕通知.
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around("compensableService()")
    public Object interceptCompensableMethod(ProceedingJoinPoint pjp) throws Throwable {

        return compensableTransactionInterceptor.interceptCompensableMethod(pjp);
    }

    public abstract int getOrder();
}
