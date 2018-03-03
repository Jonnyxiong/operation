package com.ucpaas.sms.service.finance.inventory;

import java.util.List;
import java.util.Map;

import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.model.PageContainer;

public interface OEMInventoryService {
	/**
	 * @Description: 查询OEM代理商库存信息
	 * @author: Niu.T 
	 * @date: 2016年11月26日    上午11:14:33
	 * @param params
	 * @return PageContainer
	 */
	PageContainer queryAgentInventory(Map<String, Object> params);

	/**
	 * @param params      查询条件
	 * @param userSession 用户信息
	 * @Description: 查询OEM代理商库存信息(数据权限控制)
	 * @Author: tanjiangqiang
	 * @Date: 2017/12/18 - 14:16
	 */
	PageContainer queryOEMAgentInventory(Map<String, Object> params, UserSession userSession);

	/**
	 * @Description: 查询OEM客户库存信息
	 * @author: Niu.T 
	 * @date: 2016年11月26日    上午11:15:38
	 * @param params
	 * @return PageContainer
	 */
	PageContainer queryClientInventory(Map<String, String> params);

	/**
	  * @Description: 查询OEM客户库存信息(数据权限控制)
	  * @Author: tanjiangqiang
	  * @Date: 2017/12/25 - 15:59
	  * @param params 查询条件
	  * @param userSession 用户信息
	  *
	  */
	PageContainer queryOEMClientInventory(Map<String, Object> params, UserSession userSession);

	/**
	 * @Description: 导出OEM代理商库存信息
	 * @author: Niu.T 
	 * @date: 2016年12月7日    下午2:27:34
	 * @param params
	 * @return List<Map<String,Object>>
	 */
	List<Map<String, Object>> exportAgentInventory(Map<String, Object> params);

	/**
	 * @Description: 导出OEM代理商库存信息(数据权限控制)
	 * @Author: tanjiangqiang
	 * @Date: 2017/12/18 - 16:05
	 * @param params 查询条件
	 * @param userSession 用户信息
	 *
	 */
	List<Map<String, Object>> exportOEMAgentInventory(Map<String, Object> params, UserSession userSession);

	/**
	 * @Description: 导出OEM客户库存信息
	 * @author: Niu.T 
	 * @date: 2016年12月7日    下午2:27:39
	 * @param params
	 * @return List<Map<String,Object>>
	 */
	List<Map<String, Object>> exportClientInventory(Map<String, Object> params);

	/**
	  * @Description: 导出OEM客户库存信息(数据权限控制)
	  * @Author: tanjiangqiang
	  * @Date: 2017/12/25 - 16:40
	  * @param params 查询条件
	  * @param userSession 用户信息
	  *
	  */
	List<Map<String, Object>> exportOEMClientInventory(Map<String, Object> params, UserSession userSession);

	/**
	 * OEM代理商库存总计
	 * @param params
	 * @return
	 */
	Map agentTotal(Map<String, Object> params);

	/**
	 * @Description: OEM代理商库存总计(权限控制)
	 * @Author: tanjiangqiang
	 * @Date: 2017/12/18 - 14:26
	 * @param params 查询条件
	 * @param userSession 用户信息
	 *
	 */
	Map agentOEMTotal(Map<String, Object> params, UserSession userSession);

	/**
	 * OEM客户库存总计
	 * @param params
	 * @return
	 */
	Map clientTotal(Map<String, Object> params);

	/**
	  * @Description: OEM客户库存总计(数据权限控制)
	  * @Author: tanjiangqiang
	  * @Date: 2017/12/25 - 16:28
	  * @param params 查询条件
	  * @param userSession 用户信息
	  *
	  */
	Map clientOEMTotal(Map<String, Object> params, UserSession userSession);


}
