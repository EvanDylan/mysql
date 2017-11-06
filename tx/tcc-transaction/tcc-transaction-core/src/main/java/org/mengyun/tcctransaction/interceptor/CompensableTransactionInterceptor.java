package org.mengyun.tcctransaction.interceptor;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.mengyun.tcctransaction.NoExistedTransactionException;
import org.mengyun.tcctransaction.SystemException;
import org.mengyun.tcctransaction.Transaction;
import org.mengyun.tcctransaction.TransactionManager;
import org.mengyun.tcctransaction.api.*;
import org.mengyun.tcctransaction.common.MethodType;
import org.mengyun.tcctransaction.support.FactoryBuilder;
import org.mengyun.tcctransaction.utils.CompensableMethodUtils;
import org.mengyun.tcctransaction.utils.ReflectionUtils;
import org.mengyun.tcctransaction.utils.TransactionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Created by changmingxie on 10/30/15.
 */
public class CompensableTransactionInterceptor {

    static final Logger logger = LoggerFactory.getLogger(CompensableTransactionInterceptor.class.getSimpleName());

    private TransactionManager transactionManager;

    private Set<Class<? extends Exception>> delayCancelExceptions;

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public void setDelayCancelExceptions(Set<Class<? extends Exception>> delayCancelExceptions) {
        this.delayCancelExceptions = delayCancelExceptions;
    }

    public Object interceptCompensableMethod(ProceedingJoinPoint pjp) throws Throwable {

        /**
         * 获取被{@code Compensable.class}注解的方法.
         */
        Method method = CompensableMethodUtils.getCompensableMethod(pjp);

        logger.info("补偿拦截器-------------------" + method.getName() + "----------------------");

        /**
         * 获取方法本身的注解信息.
         */
        Compensable compensable = method.getAnnotation(Compensable.class);

        /**
         * 获取事务隔离级别,和Spring一样默认是REQUIRED(全部使用事务并且合并到一个事务中处理).
         */
        Propagation propagation = compensable.propagation();
        /**
         * 初始化事务上下文信息(通过FactoryBuilder缓存{@code TransactionContextEditor.class}具体子类已经对象信息)
         *
         * 不同的TransactionContextEditor子类获取事务上下文信息的方式不一样,{@code DefaultTransactionContextEditor.class}通过隐藏参数方式传递context信息,
         * dubbo则是利用的框架提供RpcContext来完成的参数传递.
         */
        TransactionContext transactionContext = FactoryBuilder.factoryOf(compensable.transactionContextEditor()).getInstance().get(pjp.getTarget(), method, pjp.getArgs());

        /**
         * 是否异步
         */
        boolean asyncConfirm = compensable.asyncConfirm();

        boolean asyncCancel = compensable.asyncCancel();

        /**
         * 是否已经有事务的参与者
         */
        boolean isTransactionActive = transactionManager.isTransactionActive();

        /**
         * 检查事务的合法性
         */
        if (!TransactionUtils.isLegalTransactionContext(isTransactionActive, propagation, transactionContext)) {
            throw new SystemException("no active compensable transaction while propagation is mandatory for method " + method.getName());
        }

        /**
         * 判断当前事务参与者是根事务、子事务、正常事务。
         */
        MethodType methodType = CompensableMethodUtils.calculateMethodType(propagation, isTransactionActive, transactionContext);

        switch (methodType) {
            /**
             * 如果是主事务
             */
            case ROOT:
                return rootMethodProceed(pjp, asyncConfirm, asyncCancel);

            /**
             * 如果是服务提供方事务
             */
            case PROVIDER:
                return providerMethodProceed(pjp, transactionContext, asyncConfirm, asyncCancel);
            default:
                return pjp.proceed();
        }
    }


    private Object rootMethodProceed(ProceedingJoinPoint pjp, boolean asyncConfirm, boolean asyncCancel) throws Throwable {

        Object returnValue = null;

        Transaction transaction = null;

        try {

            /**
             * 创建事务
             */
            transaction = transactionManager.begin();
            logger.info("补偿拦截器start root transaction-------------------" + CompensableMethodUtils.getCompensableMethod(pjp).getName() + TransactionXid.byteArrayToUUID(transaction.getXid().getGlobalTransactionId()).toString() + "----------------------");

            try {
                /**
                 * 执行被代理类方法
                 */
                returnValue = pjp.proceed();
            } catch (Throwable tryingException) {

                /**
                 * 如果是配置中的需要延迟执行cancel逻辑的异常(给confirm重试的机会.)
                 */
                if (isDelayCancelException(tryingException)) {

                }

                /**
                 * 直接回滚
                 */
                else {
                    logger.warn(String.format("compensable transaction trying failed. transaction content:%s", JSON.toJSONString(transaction)), tryingException);

                    transactionManager.rollback(asyncCancel);
                }

                throw tryingException;
            }

            /**
             * 没有异常提交事务.
             */
            logger.info("补偿拦截器commit root transaction-------------------" + CompensableMethodUtils.getCompensableMethod(pjp).getName() + "----------------------");
            transactionManager.commit(asyncConfirm);

        } finally {
            transactionManager.cleanAfterCompletion(transaction);
        }

        return returnValue;
    }

    private Object providerMethodProceed(ProceedingJoinPoint pjp, TransactionContext transactionContext, boolean asyncConfirm, boolean asyncCancel) throws Throwable {

        logger.info("补偿拦截器start provider transaction-------------------" + CompensableMethodUtils.getCompensableMethod(pjp).getName() + "----------------------");

        Transaction transaction = null;
        try {

            switch (TransactionStatus.valueOf(transactionContext.getStatus())) {

                /**
                 * 如果事务还处于TRYING,则创建事务,设定当前事务为事务TransactionType.BRANCH;
                 */
                case TRYING:
                    transaction = transactionManager.propagationNewBegin(transactionContext);
                    logger.info("补偿拦截器创建 Branch transaction-------------------" + CompensableMethodUtils.getCompensableMethod(pjp).getName() + "----------------------");
                    return pjp.proceed();

                /**
                 * 更新事务状态,并且提交.
                 */
                case CONFIRMING:
                    try {
                        transaction = transactionManager.propagationExistBegin(transactionContext);
                        logger.info("补偿拦截器commit Branch  transaction-------------------" + CompensableMethodUtils.getCompensableMethod(pjp).getName() + "----------------------");
                        transactionManager.commit(asyncConfirm);
                    } catch (NoExistedTransactionException excepton) {
                        //the transaction has been commit,ignore it.
                    }
                    break;

                /**
                 * 更新事务状态,然后回滚.
                 */
                case CANCELLING:

                    try {
                        transaction = transactionManager.propagationExistBegin(transactionContext);
                        transactionManager.rollback(asyncCancel);
                    } catch (NoExistedTransactionException exception) {
                        //the transaction has been rollback,ignore it.
                    }
                    break;
                default:
                    // never happened
                    break;
            }

        } finally {
            /**
             * 清空当前线程相关的事务,擦屁股..
             */
            transactionManager.cleanAfterCompletion(transaction);
        }

        Method method = ((MethodSignature) (pjp.getSignature())).getMethod();

        return ReflectionUtils.getNullValue(method.getReturnType());
    }

    private boolean isDelayCancelException(Throwable throwable) {

        if (delayCancelExceptions != null) {
            for (Class delayCancelException : delayCancelExceptions) {

                Throwable rootCause = ExceptionUtils.getRootCause(throwable);

                if (delayCancelException.isAssignableFrom(throwable.getClass())
                        || (rootCause != null && delayCancelException.isAssignableFrom(rootCause.getClass()))) {
                    return true;
                }
            }
        }

        return false;
    }

}
