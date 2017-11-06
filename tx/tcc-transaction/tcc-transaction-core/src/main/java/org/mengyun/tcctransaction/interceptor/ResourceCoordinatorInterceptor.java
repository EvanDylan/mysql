package org.mengyun.tcctransaction.interceptor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.mengyun.tcctransaction.InvocationContext;
import org.mengyun.tcctransaction.Participant;
import org.mengyun.tcctransaction.Transaction;
import org.mengyun.tcctransaction.TransactionManager;
import org.mengyun.tcctransaction.api.Compensable;
import org.mengyun.tcctransaction.api.TransactionContext;
import org.mengyun.tcctransaction.api.TransactionStatus;
import org.mengyun.tcctransaction.api.TransactionXid;
import org.mengyun.tcctransaction.support.FactoryBuilder;
import org.mengyun.tcctransaction.utils.CompensableMethodUtils;
import org.mengyun.tcctransaction.utils.ReflectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

/**
 * Created by changmingxie on 11/8/15.
 */
public class ResourceCoordinatorInterceptor {

    static final Logger logger = LoggerFactory.getLogger(ResourceCoordinatorInterceptor.class.getSimpleName());

    private TransactionManager transactionManager;


    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public Object  interceptTransactionContextMethod(ProceedingJoinPoint pjp) throws Throwable {

        logger.info("资源协调拦截器-------------------" + CompensableMethodUtils.getCompensableMethod(pjp).getName() + "----------------------");

        /**
         * 获取和当前线程绑定的Transaction(通过{@code ThreadLocal.class}实现)
         */
        Transaction transaction = transactionManager.getCurrentTransaction();

        if (transaction != null) {

            switch (transaction.getStatus()) {
                case TRYING:
                    enlistParticipant(pjp);
                    break;
                case CONFIRMING:
                    break;
                case CANCELLING:
                    break;
            }
        }

         return pjp.proceed(pjp.getArgs());
    }

    /**
     * 入队当前参与者相关事务信息,并落地.
     * @param pjp
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private void enlistParticipant(ProceedingJoinPoint pjp) throws IllegalAccessException, InstantiationException {

        /**
         * 获取注解方法.
         */
        Method method = CompensableMethodUtils.getCompensableMethod(pjp);
        logger.info("资源协调拦截器入队参与者-------------------" + method.getName() + "----------------------");
        if (method == null) {
            throw new RuntimeException(String.format("join point not found method, point is : %s", pjp.getSignature().getName()));
        }

         /**
         * 获取方法上的{@code Compensable.class}注解.
         */
        Compensable compensable = method.getAnnotation(Compensable.class);

        /**
         * 获取注解上confirm、cancel方法名.
         */
        String confirmMethodName = compensable.confirmMethod();
        String cancelMethodName = compensable.cancelMethod();

        /**
         * 获取当前事务的全局id,并设定新的分支id
         */
        Transaction transaction = transactionManager.getCurrentTransaction();
        logger.info("--------------------" + TransactionXid.byteArrayToUUID(transaction.getXid().getGlobalTransactionId()).toString());
        TransactionXid xid = new TransactionXid(transaction.getXid().getGlobalTransactionId());

        /**
         * 如果注解中没有TransactionContext(事务上下文)对象,则初始化事务上下文并设定事务id、事务状态等信息.
         */
        if (FactoryBuilder.factoryOf(compensable.transactionContextEditor()).getInstance().get(pjp.getTarget(), method, pjp.getArgs()) == null) {
            FactoryBuilder.factoryOf(compensable.transactionContextEditor()).getInstance().set(new TransactionContext(xid, TransactionStatus.TRYING.getId()), pjp.getTarget(), ((MethodSignature) pjp.getSignature()).getMethod(), pjp.getArgs());
        }

        /**
         * 获取被代理类.
         */
        Class targetClass = ReflectionUtils.getDeclaringType(pjp.getTarget().getClass(), method.getName(), method.getParameterTypes());

        /**
         * 初始化回调上下文对象,设定被代理类、confirm、参数类型、以及参数等信息.
         */
        InvocationContext confirmInvocation = new InvocationContext(targetClass,
                confirmMethodName,
                method.getParameterTypes(), pjp.getArgs());

        /**
         * 初始化回调上下文对象,设定被代理类、cancel、参数类型、以及参数等信息.
         */
        InvocationContext cancelInvocation = new InvocationContext(targetClass,
                cancelMethodName,
                method.getParameterTypes(), pjp.getArgs());

        /**
         * 初始化当前事务参与者对象,是定事务id,confirm、cancel回调对象以及操作事务上下文对象.
         */
        Participant participant =
                new Participant(
                        xid,
                        confirmInvocation,
                        cancelInvocation,
                        compensable.transactionContextEditor());

        /**
         * 将当前事务参与者相关信息入列到整个事务内,并将整个事务落地.
         */
        transactionManager.enlistParticipant(participant);

    }


}
