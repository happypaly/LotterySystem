package cn.itedus.lottery.domain.strategy.service.draw;

import cn.itedus.lottery.common.Constants;
import cn.itedus.lottery.domain.strategy.model.aggregates.StrategyRich;
import cn.itedus.lottery.domain.strategy.model.req.DrawReq;
import cn.itedus.lottery.domain.strategy.model.res.DrawResult;
import cn.itedus.lottery.domain.strategy.model.vo.AwardRateInfo;
import cn.itedus.lottery.domain.strategy.model.vo.DrawAwardInfo;
import cn.itedus.lottery.domain.strategy.service.algorithm.IDrawAlgorithm;
import cn.itedus.lottery.infrastructure.po.Award;
import cn.itedus.lottery.infrastructure.po.Strategy;
import cn.itedus.lottery.infrastructure.po.StrategyDetail;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * 定义抽象抽奖过程，模板模式
 */
public abstract class AbstractDrawBase extends DrawStrategySupport implements IDrawExec {

    private Logger logger= LoggerFactory.getLogger(AbstractDrawBase.class);

    @Override
    public DrawResult doDrawExec(DrawReq req) {

        // 1.获取抽奖策略
        StrategyRich strategyRich = strategyRepository.queryStrategyRich(req.getStrategyId());
        Strategy strategy = strategyRich.getStrategy();

        // 2.校验抽奖策略是否已经初始化到内存
        this.checkAndInitRateData(req.getStrategyId(), strategy.getStrategyMode(), strategyRich.getStrategyDetailList());

        // 3.获取不在抽奖范围内的奖品列表，包括：奖品库存为空、风控策略、临时调整等
        List<String> excludeAwardIds=this.queryExcludeAwardIds(req.getStrategyId());

        // 4.执行抽奖算法
        String awardId = this.drawAlgorithm(req.getStrategyId(),drawAlgorithmGroup.get(strategy.getStrategyMode()),excludeAwardIds);

        // 5.包装中奖结果
        return buildDrawResult(req.getuId(),req.getStrategyId(),awardId);
    }

    /**
     * 校验抽奖策略是否已经初始化到内存
     *
     * @param strategyId         策略ID
     * @param strategyMode       抽奖策略模式
     * @param strategyDetailList 抽奖策略详情
     */
    public void checkAndInitRateData(Long strategyId, Integer strategyMode, List<StrategyDetail> strategyDetailList) {

        // 非单项概率，不必存入缓存
        if (!Constants.StrategyMode.SINGLE.getCode().equals(strategyMode)) return;

        // 获取抽奖策略方式 IDrawAlgorithm为抽奖策略统一接口
        IDrawAlgorithm drawAlgorithm = drawAlgorithmGroup.get(strategyMode);

        // 判断是否已经，做了数据初始化
        if (drawAlgorithm.isExistRateTuple(strategyId)) return;

        // 初始化数据
        List<AwardRateInfo> awardRateInfoList = new ArrayList<>(strategyDetailList.size());
        for (StrategyDetail strategyDetail : strategyDetailList) {
            awardRateInfoList.add(new AwardRateInfo(strategyDetail.getAwardId(), strategyDetail.getAwardRate()));
        }
        drawAlgorithm.initRateTuple(strategyId, awardRateInfoList);

    }

    /**
     * 获取不在抽奖范围内的奖品列表，包括：奖品库存为空、风控策略、临时调整等，这类数据是含有业务逻辑的，所以需要由具体的实现方决定
     *
     * @param strategyId 策略Id
     * @return
     */
    protected abstract List<String> queryExcludeAwardIds(Long strategyId);

    /**
     * 执行抽奖算法
     *
     * @param strategyId      策略ID
     * @param drawAlgorithm  抽奖算法逻辑
     * @param excludeAwardIds 排除的抽奖ID集合
     * @return
     */
    protected abstract String drawAlgorithm(Long strategyId, IDrawAlgorithm drawAlgorithm, List<String> excludeAwardIds);

    /**
     * 包装抽奖结果
     *
     * @param uId        用户ID
     * @param strategyId 策略ID
     * @param awardId    奖品ID，null情况：并发抽奖情况下，库存临界值1->0，会有用户中奖结果为null
     * @return
     */
    private DrawResult buildDrawResult(String uId, Long strategyId, String awardId){
        if (null == awardId) {
            logger.info("执行策略抽奖完成【未中奖】，用户：{} 策略ID：{}", uId, strategyId);
            return new DrawResult(uId, strategyId, Constants.DrawState.FAIL.getCode());
        }

        Award award = super.queryAwardInfoByAwardId(awardId);
        DrawAwardInfo drawAwardInfo = new DrawAwardInfo(award.getAwardId(), award.getAwardType(), award.getAwardName(), award.getAwardContent());
        logger.info("执行策略抽奖完成【已中奖】，用户：{} 策略ID：{} 奖品ID：{} 奖品名称：{}", uId, strategyId, awardId, award.getAwardName());

        return new DrawResult(uId, strategyId, Constants.DrawState.SUCCESS.getCode(), drawAwardInfo);
    }

}
