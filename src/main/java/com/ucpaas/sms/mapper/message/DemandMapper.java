package com.ucpaas.sms.mapper.message;

import com.ucpaas.sms.entity.message.Demand;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface DemandMapper {
	Demand get(String id);

	List<Demand> findAllList(Map params);

	Integer getCount(Map params);

	List<Demand> findList(Map params);

	int insert(Demand entity);

	int update(Demand entity);

	int updateStatus(Demand demand);

	/**
	 * 获取当天最大的ID序列，可能为空
	 *
	 */
	int getMaxIdSeq();

	/**
	 * 获取订单关联
	 * 
	 * @param demandId
	 * @return Map <br>
	 *         [refId,orderId]
	 */
	String getOrderRefById(String demandId);

	/**
	 * 关闭关联
	 * 
	 * @param demandId
	 */
	int closeOrderRefById(String demandId);

	/**
	 * 获取需求关联的资源
	 * 
	 * @param demandId
	 */
	List<String> findResourceById(String demandId);

	int batchInsertResourceRef(Map<String, Object> params);

	Demand getDemandByOrderId(String orderId);
}