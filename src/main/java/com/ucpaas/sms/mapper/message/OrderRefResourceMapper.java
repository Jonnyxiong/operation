package com.ucpaas.sms.mapper.message;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ucpaas.sms.entity.message.OrderRefResource;
import com.ucpaas.sms.model.Page;

/**
 * @description 订单资源关联表
 * @author huangwenjie
 * @date 2017-06-29
 */
@Repository
public interface OrderRefResourceMapper{

	int insert(OrderRefResource model);
	
	int insertBatch(List<OrderRefResource> modelList);
	
	int delete(Integer id);
	
	int update(OrderRefResource model);
	
	int updateSelective(OrderRefResource model);
	
	OrderRefResource getById(Integer id);
	
	List<OrderRefResource> queryList(Page<OrderRefResource> page);
	
	int count(Map<String,Object> params);

}