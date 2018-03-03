package com.ucpaas.sms.mapper.message;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ucpaas.sms.entity.message.OrderRefDemand;
import com.ucpaas.sms.model.Page;

/**
 * @description 订单资源关联表
 * @author huangwenjie
 * @date 2017-06-29
 */
@Repository
public interface OrderRefDemandMapper{

	int insert(OrderRefDemand model);
	
	int insertBatch(List<OrderRefDemand> modelList);
	
	int delete(Integer id);
	
	int update(OrderRefDemand model);
	
	int updateSelective(OrderRefDemand model);
	
	OrderRefDemand getById(Integer id);
	
	List<OrderRefDemand> queryList(Page<OrderRefDemand> page);
	
	int count(Map<String,Object> params);

}