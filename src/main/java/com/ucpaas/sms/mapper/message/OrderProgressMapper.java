package com.ucpaas.sms.mapper.message;

import com.ucpaas.sms.entity.message.OrderProgress;
import com.ucpaas.sms.entity.po.OrderProgressPo;
import com.ucpaas.sms.model.Page;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


@Repository
public interface OrderProgressMapper{

	int insert(OrderProgress model);
	
	int insertBatch(List<OrderProgress> modelList);
	
	int delete(Long id);
	
	int update(OrderProgress model);
	
	int updateSelective(OrderProgress model);
	
	OrderProgress getById(Long id);
	
	List<OrderProgress> queryList(Page<OrderProgress> page);

	List<OrderProgressPo> queryOrderProgressList(String orderId);
	
	int count(Map<String, Object> params);

}