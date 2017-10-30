package org.mengyun.tcctransaction.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

/**
 * Created by changmingxie on 11/8/15.
 */
@Aspect
public abstract class ResourceCoordinatorAspect {

    /**
     * 由子类{@code ConfigurableCoordinatorAspect.class}初始化执行拦截后处理工作的ResourceCoordinatorInterceptor.
     */
    private ResourceCoordinatorInterceptor resourceCoordinatorInterceptor;

    /**
     * 拦截所有加上{@code Compensable.class}注解的方法
     */
    @Pointcut("@annotation(org.mengyun.tcctransaction.api.Compensable)")
    public void transactionContextCall() {

    }

    /**
     * 环绕通知,方法执行前和执行后,都会执行一次,每次被注解的方法执行该段逻辑都会被执行两次.
     * @param pjp
     * @return
     * @throws Throwable
     */
    @Around("transactionContextCall()")
    public Object interceptTransactionContextMethod(ProceedingJoinPoint pjp) throws Throwable {
        return resourceCoordinatorInterceptor.interceptTransactionContextMethod(pjp);
    }

    public void setResourceCoordinatorInterceptor(ResourceCoordinatorInterceptor resourceCoordinatorInterceptor) {
        this.resourceCoordinatorInterceptor = resourceCoordinatorInterceptor;
    }

    public abstract int getOrder();
}
