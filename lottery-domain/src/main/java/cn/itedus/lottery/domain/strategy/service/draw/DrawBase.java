package cn.itedus.lottery.domain.strategy.service.draw;

import cn.itedus.lottery.domain.strategy.model.vo.AwardRateInfo;
import cn.itedus.lottery.domain.strategy.service.algorithm.IDrawAlgorithm;
import cn.itedus.lottery.infrastructure.po.StrategyDetail;

import java.util.ArrayList;
import java.util.List;

/**
 * 校验和初始化数据
 */
public class DrawBase extends DrawConfig {

    public void checkAndInitRateData(Long strategyId, Integer strategyMode, List<StrategyDetail> strategyDetailList) {
        if (1 != strategyMode) return;
        // 获取抽奖策略方式 IDrawAlgorithm为抽奖策略统一接口
        IDrawAlgorithm drawAlgorithm = drawAlgorithmMap.get(strategyMode);
        // 判断是否已经，做了数据初始化
        boolean existRateTuple = drawAlgorithm.isExistRateTuple(strategyId);
        if (existRateTuple) return;

        //初始化数据
        List<AwardRateInfo> awardRateInfoList = new ArrayList<>(strategyDetailList.size());
        for (StrategyDetail strategyDetail : strategyDetailList) {
            awardRateInfoList.add(new AwardRateInfo(strategyDetail.getAwardId(), strategyDetail.getAwardRate()));
        }
        drawAlgorithm.initRateTuple(strategyId, awardRateInfoList);

    }

}
