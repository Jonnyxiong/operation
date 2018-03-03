package com.ucpaas.sms.mapper.message;


import com.jsmsframework.common.dto.JsmsPage;
import com.ucpaas.sms.entity.message.ClientBalanceAlarm;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface ClientBalanceAlarmMapper{

	int insert(ClientBalanceAlarm clientBalanceAlarm);
	
	int insertBatch(List<ClientBalanceAlarm> clientBalanceAlarmList);
	
	int delete(Integer id);
	
	int update(ClientBalanceAlarm clientBalanceAlarm);
	
	int updateSelective(ClientBalanceAlarm clientBalanceAlarm);
	/**
	 * 幂等更新
	 * @param idempotentParams
	 * @return
	 */
	int updateIdempotent(Map<String,ClientBalanceAlarm> idempotentParams);

	ClientBalanceAlarm getById(Integer id);

	ClientBalanceAlarm getByClientId(String clientId);

	List<ClientBalanceAlarm> queryList(JsmsPage<ClientBalanceAlarm> page);
	
	int count(ClientBalanceAlarm clientBalanceAlarm);

}