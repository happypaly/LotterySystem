package cn.itedus.lottery.infrastructure.dao;

import cn.itedus.lottery.infrastructure.po.Award;
import org.apache.ibatis.annotations.Mapper;

/**

 */
@Mapper
public interface IAwardDao {

    Award queryAwardInfo(String awardId);

}
