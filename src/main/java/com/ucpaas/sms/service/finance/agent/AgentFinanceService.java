package com.ucpaas.sms.service.finance.agent;

import java.util.List;
import java.util.Map;

import com.jsmsframework.common.dto.JsmsPage;
import com.ucpaas.sms.model.PageContainer;

public interface AgentFinanceService {
	
	PageContainer query(Map<String, String> params);
	
	List<Map<String, Object>> queryAll(Map<String, String> params);
	
	Map<String, Object> getAgentInfoByAgentID(int agent_id);
	
	/**
	 * @Description: 获取代理商的指定的条件的客户
	 * @author: Niu.T 
	 * @date: 2016年11月23日    上午9:59:41
	 * @param params
	 * @return Map<String,Object>
	 */
	List<Map<String, Object>> getClientByCondition(Map<String, String> params);



	/**
	 * @Description: 获取代理商的指定的条件的客户
	 * @author: Niu.T
	 * @date: 2016年11月23日    上午9:59:41
	 * @param params
	 * @return Map<String,Object>
	 */
	Map<String, Object> getClientByCondition1(Map<String, String> params);

	/**
	 * @Description: 代理商财务 余额操作,修改余额 并添加余额账单信息
	 * @author: Niu.T 
	 * @date: 2016年11月26日    下午3:50:30
	 * @param params
	 * @return Map<String,Object>
	 */
	Map<String, Object> balanceAction(Map<String, String> params);

	JsmsPage initOnlinePayMent(JsmsPage page);

	JsmsPage initOnlinePayMentParam(Map<String, String> params);

	/**
	 * @Description: 代理商财务 押金操作,修改押金 并添加押金账单信息
	 * @author: Niu.T 
	 * @date: 2016年11月26日    下午7:35:30
	 * @param
	 * @return Map<String,Object>
	 */
	Map<String, Object> depositAction(Map<String, String> params);

	JsmsPage initOnlinePayMent4Export(JsmsPage page);

	/**
	 * @Description: 代理商财务 授信操作,修改授信额
	 * @author: Niu.T 
	 * @date: 2016年11月28日    上午10:21:44
	 * @param
	 * @return Map<String,Object>
	 */
	Map<String, Object> createAction(Map<String, String> params);


	/**
	 * 查询代理商的各种财务总计
	 * @param params
	 * @return
	 */
	Map<String, Object> sumTotal(Map<String, String> params);
	
}
