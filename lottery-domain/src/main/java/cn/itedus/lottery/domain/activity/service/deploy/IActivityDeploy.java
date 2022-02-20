package cn.itedus.lottery.domain.activity.service.deploy;

import cn.itedus.lottery.domain.activity.model.req.ActivityConfigReq;

/**
 * @description: 部署活动配置接口
 */
public interface IActivityDeploy {

    /**
     * 创建活动信息
     *
     * @param req 活动配置信息
     */
    void createActivity(ActivityConfigReq req);

    /**
     * 修改活动信息
     *
     * @param req 活动配置信息
     */
    void updateActivity(ActivityConfigReq req);

}
