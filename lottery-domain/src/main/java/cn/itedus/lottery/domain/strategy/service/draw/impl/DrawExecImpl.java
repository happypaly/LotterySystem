package cn.itedus.lottery.domain.strategy.service.draw.impl;

import cn.itedus.lottery.domain.strategy.model.aggregates.StrategyRich;
import cn.itedus.lottery.domain.strategy.model.req.DrawReq;
import cn.itedus.lottery.domain.strategy.model.res.DrawResult;
import cn.itedus.lottery.domain.strategy.repository.IStrategyRepository;
import cn.itedus.lottery.domain.strategy.service.algorithm.IDrawAlgorithm;
import cn.itedus.lottery.domain.strategy.service.draw.DrawBase;
import cn.itedus.lottery.domain.strategy.service.draw.IDrawExec;
import cn.itedus.lottery.infrastructure.po.Award;
import cn.itedus.lottery.infrastructure.po.Strategy;
import cn.itedus.lottery.infrastructure.po.StrategyDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 */
@Service("drawExec")
public class DrawExecImpl extends DrawBase implements IDrawExec {

    private Logger logger = LoggerFactory.getLogger(DrawExecImpl.class);

    List<String> excludeAwardIds=new ArrayList<>();

    @Resource
    private IStrategyRepository strategyRepository;

    @Override
    public DrawResult doDrawExec(DrawReq req) {
        logger.info("执行策略抽奖开始，strategyId：{}", req.getStrategyId());

        // 获取抽奖策略配置数据 包括策略id、策略配置、策略明细
        StrategyRich strategyRich = strategyRepository.queryStrategyRich(req.getStrategyId());
        // 获取策略配置
        Strategy strategy = strategyRich.getStrategy();
        // 获取策略明细  包括奖品中奖概率
        List<StrategyDetail> strategyDetailList = strategyRich.getStrategyDetailList();

        // 校验和初始化数据
        checkAndInitRateData(req.getStrategyId(), strategy.getStrategyMode(), strategyDetailList);

        // 根据策略方式抽奖
        IDrawAlgorithm drawAlgorithm = drawAlgorithmMap.get(strategy.getStrategyMode());

        String awardId = drawAlgorithm.randomDraw(req.getStrategyId(), excludeAwardIds);
        excludeAwardIds.add(awardId);

        // 获取奖品信息
        Award award = strategyRepository.queryAwardInfo(awardId);

        logger.info("执行策略抽奖完成，中奖用户：{} 奖品ID：{} 奖品名称：{}", req.getuId(), awardId, award.getAwardName());

        // 封装结果
        return new DrawResult(req.getuId(), req.getStrategyId(), awardId, award.getAwardName());
    }

}
