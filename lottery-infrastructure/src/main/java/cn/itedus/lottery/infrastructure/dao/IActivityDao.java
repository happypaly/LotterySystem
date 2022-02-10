package cn.itedus.lottery.infrastructure.dao;

import cn.itedus.lottery.infrastructure.po.Activity;
import org.apache.ibatis.annotations.Mapper;

/**

 */
@Mapper
public interface IActivityDao {

   void insert(Activity req);

   Activity queryActivityById(Long activityId);

}
