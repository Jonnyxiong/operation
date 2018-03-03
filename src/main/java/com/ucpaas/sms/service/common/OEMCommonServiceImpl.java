package com.ucpaas.sms.service.common;

import com.alibaba.fastjson.JSON;
import com.ucpaas.sms.constant.LogConstant.LogType;
import com.ucpaas.sms.dao.MessageMasterDao;
import com.ucpaas.sms.enums.LogEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class OEMCommonServiceImpl implements OEMCommonService {

	@Autowired
	private LogService logService;

	@Autowired
	private MessageMasterDao masterDao;
	
	/**
	 * 获取t_sms_agent_client_param 参数
	 */
	@Override
	public List<Map<String, Object>> getAgentParams(List<String> params) {
		return masterDao.getSearchList("oemCommon.getAgentParams", params);
	}

	/**
	 * 获取单个t_sms_agent_client_param 参数
	 */
	@Override
	public Map<String, Object> getOneAgentParam(String paramKey) {
		return masterDao.getOneInfo("oemCommon.getOneAgentParam", paramKey);
	}

	/**
	 * 添加代理商属性配置t_sms_agent_client_param 参数
	 */
	@Override
	public Map<String, Object> addOrUpdateAgentParams(Map<String, String> params) {
		Map<String, Object> data = new HashMap<String,Object>();
		String paramKey = params.get("param_key");
		if(paramKey != null && "due_time".equals(paramKey)){
			params.put("checkFlag", "true");
			params.put("param_key", "OEM_PRODUCT_DUETIME");
		}else if(paramKey != null && "gj_sms_discount".equals(paramKey)){
			params.put("param_key", "OEM_GJ_SMS_DISCOUNT");
		}else if(paramKey != null && "rebate_price".equals(paramKey)){
			params.put("param_key", "OEM_AGENT_REBATE_PRICE");
		}
		paramKey = params.get("param_key");
		int paramsNum = checkAgentParamsNum(params);

		if(paramsNum < 1){
			logService.add(LogType.add, LogEnum.产品管理.getValue(), Long.parseLong(params.get("adminId")), params.get("pageUrl"), params.get("ip"),
					"产品管理-OEM产品管理-OEM代理属性配置：添加代理商参数", JSON.toJSONString(params));
		}else {
			logService.add(LogType.update, LogEnum.产品管理.getValue(), Long.parseLong(params.get("adminId")), params.get("pageUrl"), params.get("ip"),
					"产品管理-OEM产品管理-OEM代理属性配置：更新代理商参数", JSON.toJSONString(params));
		}

		if(paramsNum < 1){ // 添加
			int insert = masterDao.insert("oemCommon.addAgentParams", params);
			if(insert > 0){
				data.put("result", "success");
				data.put("msg", "添加成功");
			}
		}else {
			String action = params.get("action");
			if(paramKey != null && "OEM_PRODUCT_DUETIME".equals(paramKey) && action != null && !"2".equals(action) ){
				data.put("result", "fail");
				data.put("msg", "该参数及参数值已经存在...");
			}else{

				data = updateAgentParams(params);
			}
		}
		return data;
	}

	/**
	 * 检查OEM配置参数是否存在
	 */
	private int checkAgentParamsNum(Map<String, String> params) {
		return 	(int)masterDao.getOneInfo("oemCommon.checkAgentParamsNum", params);
	}

	/**
	 * 修改OEM配置
	 */
	private Map<String, Object> updateAgentParams(Map<String, String> params) {
		Map<String, Object> data = new HashMap<String,Object>();
		int update = masterDao.update("oemCommon.updateAgentParams", params);
		String action = params.get("action");
		if(update > 0){
			data.put("result", "success");
			data.put("msg", "2".equals(action) ? "删除成功" :"修改成功");
		}else{
			data.put("result", "fail");
			data.put("msg", "2".equals(action) ? "删除失败" :"修改失败");
		}
		return data;
	}

	/**
	 * 添加OEM代理商返点比例
	 */
	@Override
	@Deprecated
	public Map<String, Object> addOrUpdateRebate(Map<String, String> params) {
		Map<String,Object> data = new HashMap<String,Object>();
		String action = params.get("edit_action");
		String userId = params.get("userId");
		params.put("updator", userId);
		if(action != null && "add".equals(action)){
			logService.add(LogType.add, LogEnum.产品管理.getValue(), Long.parseLong(userId), params.get("pageUrl"), params.get("ip"),
					"产品管理-OEM产品管理-OEM代理属性配置：添加返点比例", JSON.toJSONString(params));

			int insert = masterDao.insert("oemProduct.insertAgentRebate", params);
			data.put("result", insert > 0 ? "success" :"fail");
			data.put("msg", insert > 0 ? "添加成功" :"添加失败");
		}else if(action != null && "update".equals(action)){
			logService.add(LogType.update, LogEnum.产品管理.getValue(), Long.parseLong(userId), params.get("pageUrl"), params.get("ip"),
					"产品管理-OEM产品管理-OEM代理属性配置：修改返点比例", JSON.toJSONString(params));

			int update = masterDao.update("oemProduct.updateAgentRebate", params);
			data.put("result", update > 0 ? "success" :"fail");
			data.put("msg", update > 0 ? "修改成功" :"修改失败");
		}else if(action != null && "delete".equals(action)){
			logService.add(LogType.delete, LogEnum.产品管理.getValue(), Long.parseLong(userId), params.get("pageUrl"), params.get("ip"),
					"产品管理-OEM产品管理-OEM代理属性配置：删除返点比例", JSON.toJSONString(params));

			int delete = masterDao.delete("oemProduct.deleteAgentRebate", params);
			if(delete > 1){
				throw new RuntimeException("删除代理商返点比例异常:一次只能删除一条记录,当前删除记录数----->"+delete);
			}else{
				data.put("result", delete > 0 ? "success" :"fail");
				data.put("msg", delete > 0 ? "删除成功" :"删除失败");
			}
		}
		return data;
	}

}
