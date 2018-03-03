package com.ucpaas.sms.mapper.accessSlave;


import com.ucpaas.sms.entity.access.BackPaymentStat;
import com.ucpaas.sms.model.Page;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface BackPaymentStatMapper{

	int insert(BackPaymentStat backPaymentStat);
	
	int insertBatch(List<BackPaymentStat> backPaymentStatList);
	
	int delete(Integer id);
	
	int update(BackPaymentStat backPaymentStat);
	
	int updateSelective(BackPaymentStat backPaymentStat);
	
	BackPaymentStat getById(Integer id);
	
	List<BackPaymentStat> queryList(Page<BackPaymentStat> page);

	BackPaymentStat querySum(Map params);

	int count(BackPaymentStat backPaymentStat);

}