package com.ucpaas.sms.mapper.accessSlave;


import com.ucpaas.sms.dto.ClientConsumeVO;
import com.ucpaas.sms.entity.access.AccessSendStat;
import com.ucpaas.sms.model.Page;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface AccessSendStatMapper{

	int insert(AccessSendStat accessSendStat);
	
	int insertBatch(List<AccessSendStat> accessSendStatList);
	
	int delete(int id);
	
	int update(AccessSendStat accessSendStat);
	
	int updateSelective(AccessSendStat accessSendStat);
	
	AccessSendStat getById(int id);

	List<ClientConsumeVO> queryMonthSumList(Map params);

	List<ClientConsumeVO> queryTop(Map params);

	List<AccessSendStat> queryList(Page<AccessSendStat> page);

	List<AccessSendStat> querySumList(Page<AccessSendStat> page);

	List<ClientConsumeVO> queryTotalList(Map params);

	int count(AccessSendStat accessSendStat);

}