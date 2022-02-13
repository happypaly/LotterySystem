package cn.itedus.lottery.domain.strategy.service.draw;

import cn.itedus.lottery.domain.strategy.model.req.DrawReq;
import cn.itedus.lottery.domain.strategy.model.res.DrawResult;

/**
 * 抽奖执行接口
 */
public interface IDrawExec {

    /**
     * 抽奖方法
     * @param req 抽奖参数：用户ID、策略ID
     * @return    中奖结果
     */
    DrawResult doDrawExec(DrawReq req);

}
