package com.ucpaas.sms.service.finance.inventory;

import java.math.BigDecimal;
import java.util.*;

import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.entity.po.DataAuthorityCondition;
import com.ucpaas.sms.service.AgentInfoService;
import com.ucpaas.sms.service.account.AccountService;
import com.ucpaas.sms.util.AgentUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ucpaas.sms.dao.MessageMasterDao;
import com.ucpaas.sms.model.PageContainer;

@Service
public class OEMInventoryServiceImpl implements OEMInventoryService{
	
//	private static final Logger LOGGER = LoggerFactory.getLogger(OEMInventoryServiceImpl.class);
	
	@Autowired
	private MessageMasterDao masterDao;
//	@Autowired
//	private LogService logService;
	@Autowired
	private AgentInfoService agentInfoService;

	@Autowired
	private AccountService accountService;
	
	/**
	 * 查询OEM代理商库存信息
	 */
	@Override
	public PageContainer queryAgentInventory(Map<String, Object> params) {
		return masterDao.getSearchPage("oemInventory.queryAgentInventory", "oemInventory.queryAgentInventoryCount", params);
	}

	/**
	 * @param params      查询条件
	 * @param userSession 用户信息
	 * @Description: 查询OEM代理商库存信息(数据权限控制)
	 * @Author: tanjiangqiang
	 * @Date: 2017/12/18 - 14:16
	 */
	@Override
	public PageContainer queryOEMAgentInventory(Map<String, Object> params, UserSession userSession) {
		DataAuthorityCondition dataAuthorityCondition = AgentUtils.getDataAuthorityCondition(userSession.getId(), true, false);
		Set<Integer> agentIds = agentInfoService.findOEMAgentIdByBelongSales(dataAuthorityCondition);
		params.put("agentIds", agentIds);
		if (null == agentIds || agentIds.isEmpty()){
			return new PageContainer();
		}
		return this.queryAgentInventory(params);
	}


	/**
	 * 查询OEM客户库存信息
	 */
	@Override
	public PageContainer queryClientInventory(Map<String, String> params) {
		return masterDao.getSearchPage("oemInventory.queryClientInventory", "oemInventory.queryClientInventoryCount", params);
	}

	@Override
	public PageContainer queryOEMClientInventory(Map<String, Object> params, UserSession userSession) {
		DataAuthorityCondition dataAuthorityCondition = AgentUtils.getDataAuthorityCondition(userSession.getId(), true, false);
		Set<String> clientids = accountService.findAllListByBelongSales(dataAuthorityCondition);
		if (null == clientids || clientids.isEmpty()){
			return new PageContainer();
		}
		params.put("clientids",clientids);
		return masterDao.getSearchPage("oemInventory.queryClientInventory", "oemInventory.queryClientInventoryCount", params);
	}

	/**
	 * 导出OEM代理商库存信息
	 */
	@Override
	public List<Map<String, Object>> exportAgentInventory(Map<String, Object> params) {
		List<Map<String, Object>> datas =masterDao.getSearchList("oemInventory.exportAgentInventory", params);

		for(Map<String, Object> data:datas){
			String unit_price_str = data.get("unit_price").toString();
			if(!unit_price_str.equals("-")){
				BigDecimal unit_price = new BigDecimal(unit_price_str);
				data.put("unit_price", unit_price.setScale(4, BigDecimal.ROUND_HALF_UP).toString());
			}  
		}
		return datas;
	}

	/**
	  * @Description: 导出OEM代理商库存信息(数据权限控制)
	  * @Author: tanjiangqiang
	  * @Date: 2017/12/18 - 16:05
	  * @param params 查询条件
	  * @param userSession 用户信息
	  *
	  */
	@Override
	public List<Map<String, Object>> exportOEMAgentInventory(Map<String, Object> params, UserSession userSession){
		DataAuthorityCondition dataAuthorityCondition = AgentUtils.getDataAuthorityCondition(userSession.getId(), true, false);
		Set<Integer> agentIds = agentInfoService.findOEMAgentIdByBelongSales(dataAuthorityCondition);
		params.put("agentIds", agentIds);
		if (null == agentIds || agentIds.isEmpty()){
			return new ArrayList<>();
		}
		return this.exportAgentInventory(params);
	}


	/**
	 * 导出OEM客户库存信息
	 */
	@Override
	public List<Map<String, Object>> exportClientInventory(Map<String, Object> params) {
		List<Map<String, Object>> datas = masterDao.getSearchList("oemInventory.exportClientInventory", params);

		for(Map<String, Object> data:datas){
			String unit_price_str = data.get("unit_price").toString();
			if(!unit_price_str.equals("-")){
				BigDecimal unit_price = new BigDecimal(unit_price_str);
				data.put("unit_price", unit_price.setScale(4, BigDecimal.ROUND_HALF_UP).toString());
			} 
		}
		return datas;
	}

	@Override
	public List<Map<String, Object>> exportOEMClientInventory(Map<String, Object> params, UserSession userSession){
		DataAuthorityCondition dataAuthorityCondition = AgentUtils.getDataAuthorityCondition(userSession.getId(), true, false);
		Set<String> clientids = accountService.findAllListByBelongSales(dataAuthorityCondition);
		if (null == clientids || clientids.isEmpty()){
			return new ArrayList<>();
		}
		params.put("clientids",clientids);
		return this.exportClientInventory(params);
	}

	@Override
	public Map agentTotal(Map<String, Object> params) {
		Map data = masterDao.getOneInfo("oemInventory.agentTotal", params);
		if(data==null)
			data = new HashMap<>();
		String remain_num_t = data.get("remain_num_t")==null?"0条":data.get("remain_num_t").toString()+"条";
		BigDecimal remain_amount_y = data.get("remain_amount_y")==null?BigDecimal.ZERO:new BigDecimal(data.get("remain_amount_y").toString());
		String remain_amount_y_str = "0元";
		if(remain_amount_y.compareTo(BigDecimal.ZERO)!=0)
			remain_amount_y_str = remain_amount_y.setScale(2, BigDecimal.ROUND_HALF_UP).toString()+"元";
		data.put("remain_num", remain_num_t+"/"+remain_amount_y_str);
		return data;
	}

	/**
	  * @Description: OEM代理商库存总计(权限控制)
	  * @Author: tanjiangqiang
	  * @Date: 2017/12/18 - 14:26
	  * @param params 查询条件
	  * @param userSession 用户信息
	  *
	  */
	@Override
	public Map agentOEMTotal(Map<String, Object> params, UserSession userSession) {
		DataAuthorityCondition dataAuthorityCondition = AgentUtils.getDataAuthorityCondition(userSession.getId(), true, false);
		Set<Integer> agentIds = agentInfoService.findOEMAgentIdByBelongSales(dataAuthorityCondition);
		params.put("agentIds", agentIds);
		if (null == agentIds || agentIds.isEmpty()){
			return new HashMap();
		}
		return this.agentTotal(params);
	}


	@Override
	public Map clientTotal(Map<String, Object> params) {
		Map data = masterDao.getOneInfo("oemInventory.clientTotal", params);
		if(data==null)
			data = new HashMap<>();
		String remain_num_t = data.get("remain_num_t")==null?"0条":data.get("remain_num_t").toString()+"条";
		BigDecimal remain_amount_y = data.get("remain_amount_y")==null?BigDecimal.ZERO:new BigDecimal(data.get("remain_amount_y").toString());
		String remain_amount_y_str = "0元";
		if(remain_amount_y.compareTo(BigDecimal.ZERO)!=0)
			remain_amount_y_str = remain_amount_y.setScale(4, BigDecimal.ROUND_HALF_UP).toString()+"元";
		data.put("remain_num", remain_num_t+"/"+remain_amount_y_str);
		return data;
	}

	@Override
	public Map clientOEMTotal(Map<String, Object> params, UserSession userSession) {
		DataAuthorityCondition dataAuthorityCondition = AgentUtils.getDataAuthorityCondition(userSession.getId(), true, false);
		Set<String> clientids = accountService.findAllListByBelongSales(dataAuthorityCondition);
		if (null == clientids || clientids.isEmpty()){
			return new HashMap();
		}
		params.put("clientids",clientids);
		return this.clientTotal(params);
	}

	
	
//	//处理到期时间
//	private void handleEndTime(Map<String, Object> map){
//		
//		String effective_time =  (String) map.get("effective_time");
//		int active_period = (int) map.get("active_period");
//		if(effective_time == null){
//			map.put("end_time", null);
//			if(active_period == 0){
//				map.put("active_period", "无限期");
//			}
//		}else{
//			if(active_period == 0){
//				map.put("end_time", "9999-01-01 00:00:00");
//				map.put("active_period", "无限期");
//			}else{
//				DateTimeFormatter format1 = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
//				DateTime startDateTime = DateTime.parse(effective_time, format1);
//				startDateTime = startDateTime.plusDays(active_period);
//				map.put("end_time", startDateTime.toString("yyyy-MM-dd HH:mm:ss"));
//			}
//		}
//	}
}
