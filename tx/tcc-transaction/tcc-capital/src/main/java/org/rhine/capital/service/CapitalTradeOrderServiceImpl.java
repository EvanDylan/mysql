/*
 * ====================================================================
 * 龙果学院： www.roncoo.com （微信公众号：RonCoo_com）
 * 超级教程系列：《微服务架构的分布式事务解决方案》视频教程
 * 讲师：吴水成，840765167@qq.com
 * 课程地址：http://www.roncoo.com/course/view/7ae3d7eddc4742f78b0548aa8bd9ccdb
 * ====================================================================
 */
package org.rhine.capital.service;

import org.mengyun.tcctransaction.api.Compensable;
import org.mengyun.tcctransaction.api.TransactionContext;
import org.mengyun.tcctransaction.dubbo.context.DubboTransactionContextEditor;
import org.rhine.capital.api.CapitalTradeOrderService;
import org.rhine.capital.api.dto.CapitalTradeOrderDto;
import org.rhine.capital.domain.entity.CapitalAccount;
import org.rhine.capital.domain.entity.TradeOrder;
import org.rhine.capital.domain.repository.CapitalAccountRepository;
import org.rhine.capital.domain.repository.TradeOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by changming.xie on 4/2/16.
 */
@Service("capitalTradeOrderService")
public class CapitalTradeOrderServiceImpl implements CapitalTradeOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(CapitalTradeOrderServiceImpl.class);

    @Autowired
    CapitalAccountRepository capitalAccountRepository;

    @Autowired
    TradeOrderRepository tradeOrderRepository;

    @Override
    @Compensable(confirmMethod = "confirmRecord", cancelMethod = "cancelRecord", transactionContextEditor = DubboTransactionContextEditor.class)
    @Transactional(rollbackFor = Exception.class)
    public String record(CapitalTradeOrderDto tradeOrderDto) {
        LOG.info("==>capital try record called");

        TradeOrder tradeOrder = new TradeOrder(
                tradeOrderDto.getSelfUserId(),
                tradeOrderDto.getOppositeUserId(),
                tradeOrderDto.getMerchantOrderNo(),
                tradeOrderDto.getAmount()
        );

        tradeOrderRepository.insert(tradeOrder);

        CapitalAccount transferFromAccount = capitalAccountRepository.findByUserId(tradeOrderDto.getSelfUserId());

        // 先扣减付款方资金帐户资金
        transferFromAccount.transferFrom(tradeOrderDto.getAmount());

        capitalAccountRepository.save(transferFromAccount);
        return "success";
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmRecord(CapitalTradeOrderDto tradeOrderDto) {
    	LOG.info("==>capital confirm record called");

        TradeOrder tradeOrder = tradeOrderRepository.findByMerchantOrderNo(tradeOrderDto.getMerchantOrderNo());

        tradeOrder.confirm();
        tradeOrderRepository.update(tradeOrder);

        CapitalAccount transferToAccount = capitalAccountRepository.findByUserId(tradeOrderDto.getOppositeUserId());

        // 增加收款方资金帐户资金
        transferToAccount.transferTo(tradeOrderDto.getAmount());

        capitalAccountRepository.save(transferToAccount);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelRecord(CapitalTradeOrderDto tradeOrderDto) {
    	LOG.info("==>capital cancel record called");

        TradeOrder tradeOrder = tradeOrderRepository.findByMerchantOrderNo(tradeOrderDto.getMerchantOrderNo());

        if(null != tradeOrder && "DRAFT".equals(tradeOrder.getStatus())) {
            tradeOrder.cancel();
            tradeOrderRepository.update(tradeOrder);

            CapitalAccount capitalAccount = capitalAccountRepository.findByUserId(tradeOrderDto.getSelfUserId());

            // 对扣减付款方资金帐户资金进行反操作
            capitalAccount.cancelTransfer(tradeOrderDto.getAmount());

            capitalAccountRepository.save(capitalAccount);
        }
    }
}