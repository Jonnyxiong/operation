package com.ucpaas.sms.service.audit;

import com.ucpaas.sms.entity.message.Account;
import com.ucpaas.sms.model.PageContainer;

import java.util.Map;

public interface CustomerAuditService {
	
	PageContainer query(Map<String, String> params); 
	
	Map<String, Object> view(Map<String, String> params); 
	
	Map<String, Object> audit(Map<String, String> params);

	/**
	 * 获取oem代理商订单id序号的最大值
	 * @param orderIdPre
	 * @return
	 */
	String getOemAgentOrderTheMostNumForMinute(String orderIdPre);

	/**
	 * 获取oem客户订单id序号的最大值
	 * @param orderIdPre
	 * @return
	 */
	String getOemClientOrderTheMostNumForMinute(String orderIdPre);

	/**
	 * 谨慎使用
	 * @param client_id
	 */
	@Deprecated
	void giveShortMessage(Account account);

	Long getOemAgentOrderId();

	/*Map<String, Object> viewOfAccount(Map<String, String> params);

	Map<String, Object> viewOfAccountZK(Map<String, String> params);*/
}
