package com.ucpaas.sms.service.common;

import java.util.List;
import java.util.Map;

public interface OEMCommonService {
	
	/**
	 * @Description: 获取t_sms_agent_client_param 表中的参数
	 * @author: Niu.T 
	 * @date: 2016年11月24日    下午4:43:03
	 * @param paramKey
	 * @return List<Map<String,Object>>
	 */
	List<Map<String, Object>> getAgentParams(List<String> params);
	 
	/**
	 * @Description: 获t_sms_agent_client_param 表中单个参数
	 * @author: Niu.T 
	 * @date: 2016年11月24日    下午4:52:48
	 * @param paramKey
	 * @return Map<String,Object>
	 */
	Map<String,Object> getOneAgentParam(String paramKey);

	/**
	 * @Description: OEM代理属性配置 / 添加参数
	 * @author: Niu.T 
	 * @date: 2016年11月29日    下午8:55:40
	 * @param params
	 * @return List<Map<String,Object>>
	 */
	Map<String, Object> addOrUpdateAgentParams(Map<String, String> params);

	/**
	 * @Description: OEM代理商属性配置 / 添加OEM返点比例
	 * @author: Niu.T 
	 * @date: 2016年12月5日    下午3:12:27
	 * @param params
	 * @return Map<String,Object>
	 */
	Map<String, Object> addOrUpdateRebate(Map<String, String> params);
	
}
