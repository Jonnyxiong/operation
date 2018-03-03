package com.ucpaas.sms.service.common;

import java.util.List;
import java.util.Map;

public interface OpsIndexService {
	
	Map<String, Object> view(Map<String, String> params);

	/**
	 * @Description: 查询代理商趋势
	 * @author: Niu.T 
	 * @date: 2016年12月9日    下午2:58:50
	 * @param params
	 * @return List<Map<String,Object>>
	 */
	List<Map<String, Object>> queryAgentTendency(Map<String, Object> params);

	/**
	 * @Description: 查看客户趋势
	 * @author: Niu.T 
	 * @date: 2016年12月12日    下午12:15:26
	 * @param formData
	 * @return List<Map<String,Object>>
	 */
	List<Map<String, Object>> queryClientTendency(Map<String, Object> params);

	/**
	 * @Description: 查看消费趋势
	 * @author: Niu.T 
	 * @date: 2016年12月12日    下午12:15:32
	 * @param formData`
	 * @return List<Map<String,Object>>
	 */
	List<Map<String, Object>> queryConsumeTendency(Map<String, String> params);

}
