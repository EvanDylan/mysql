/*
 * ========================================================================
 * 龙果学院： www.roncoo.com （微信公众号：RonCoo_com）
 * 超级教程系列：《微服务架构的分布式事务解决方案》视频教程
 * 讲师：吴水成（水到渠成），840765167@qq.com
 * 课程地址：http://www.roncoo.com/course/view/7ae3d7eddc4742f78b0548aa8bd9ccdb
 * ========================================================================
 */
package org.rhine.redpacket.service;

import org.mengyun.tcctransaction.api.Compensable;
import org.mengyun.tcctransaction.dubbo.context.DubboTransactionContextEditor;
import org.rhine.redpacket.api.RedPacketTradeOrderService;
import org.rhine.redpacket.api.dto.RedPacketTradeOrderDto;
import org.rhine.redpacket.domain.entity.RedPacketAccount;
import org.rhine.redpacket.domain.entity.TradeOrder;
import org.rhine.redpacket.domain.repository.RedPacketAccountRepository;
import org.rhine.redpacket.domain.repository.TradeOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Created by changming.xie on 4/2/16.
 */
@Service("redPacketTradeOrderService")
public class RedPacketTradeOrderServiceImpl implements RedPacketTradeOrderService {

    private static final Logger LOG = LoggerFactory.getLogger(RedPacketTradeOrderServiceImpl.class);

    @Autowired
    RedPacketAccountRepository redPacketAccountRepository;

    @Autowired
    TradeOrderRepository tradeOrderRepository;

    @Override
    @Compensable(confirmMethod = "confirmRecord",cancelMethod = "cancelRecord", transactionContextEditor = DubboTransactionContextEditor.class)
    @Transactional(rollbackFor = Exception.class)
    public String record(RedPacketTradeOrderDto tradeOrderDto) {
    	LOG.info("==>red packet try record called");

        TradeOrder tradeOrder = new TradeOrder(
                tradeOrderDto.getSelfUserId(),
                tradeOrderDto.getOppositeUserId(),
                tradeOrderDto.getMerchantOrderNo(),
                tradeOrderDto.getAmount()
        );

        tradeOrderRepository.insert(tradeOrder);

        RedPacketAccount transferFromAccount = redPacketAccountRepository.findByUserId(tradeOrderDto.getSelfUserId());

        transferFromAccount.transferFrom(tradeOrderDto.getAmount());

        redPacketAccountRepository.save(transferFromAccount);

        return "success";
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirmRecord(RedPacketTradeOrderDto tradeOrderDto) {
    	LOG.info("==>red packet confirm record called");

        TradeOrder tradeOrder = tradeOrderRepository.findByMerchantOrderNo(tradeOrderDto.getMerchantOrderNo());

        tradeOrder.confirm();
        tradeOrderRepository.update(tradeOrder);

        RedPacketAccount transferToAccount = redPacketAccountRepository.findByUserId(tradeOrderDto.getOppositeUserId());

        transferToAccount.transferTo(tradeOrderDto.getAmount());

        redPacketAccountRepository.save(transferToAccount);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelRecord(RedPacketTradeOrderDto tradeOrderDto) {
    	LOG.info("==>red packet cancel record called");

        TradeOrder tradeOrder = tradeOrderRepository.findByMerchantOrderNo(tradeOrderDto.getMerchantOrderNo());

        if(null != tradeOrder && "DRAFT".equals(tradeOrder.getStatus())) {
            tradeOrder.cancel();
            tradeOrderRepository.update(tradeOrder);

            RedPacketAccount capitalAccount = redPacketAccountRepository.findByUserId(tradeOrderDto.getSelfUserId());

            capitalAccount.cancelTransfer(tradeOrderDto.getAmount());

            redPacketAccountRepository.save(capitalAccount);
        }
    }
}
