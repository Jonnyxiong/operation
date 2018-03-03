package com.ucpaas.sms.mapper.message;

import com.ucpaas.sms.entity.message.OrderList;
import com.ucpaas.sms.entity.po.OrderListPo;
import com.ucpaas.sms.model.Page;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface OrderListMapper {

	int insert(OrderList model);

	int insertBatch(List<OrderList> modelList);

	int delete(String id);

	int update(OrderList model);

	int updateSelective(OrderList model);

	OrderList getById(String id);

	OrderListPo getPoById(String id);

	List<OrderList> queryList(Page<OrderList> page);

	List<OrderListPo> queryListNew(Page<OrderListPo> page);

	int count(Map<String, Object> params);

	int deleteResourceRefById(String orderId);

	int batchInsertResourceRef(Map<String, Object> params);
}