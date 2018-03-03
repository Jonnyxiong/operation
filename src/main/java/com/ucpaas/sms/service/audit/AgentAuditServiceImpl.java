package com.ucpaas.sms.service.audit;

import java.util.HashMap;
import java.util.Map;

import com.ucpaas.sms.common.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucpaas.sms.common.util.SecurityUtils;
import com.ucpaas.sms.constant.LogConstant.LogType;
import com.ucpaas.sms.dao.MessageMasterDao;
import com.ucpaas.sms.enums.LogEnum;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.common.LogService;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.common.util.StringUtils;
@Service
@Transactional
public class AgentAuditServiceImpl implements AgentAuditService {
	
	@Autowired
	private MessageMasterDao masterDao;
	@Autowired
	private LogService logService;
	
	@Override
	public PageContainer query(Map<String, String> params) {
		return masterDao.getSearchPage("agentAudit.query", "agentAudit.queryCount", params);
	}

	@Override
	public Map<String, Object> audit(Map<String, String> params) {
		Map<String, Object> result = new HashMap<String, Object>();
		if(StringUtils.isBlank(params.get("type"))){
			int i = masterDao.getOneInfo("agentAudit.checkAgentOauthStatus", params);
			if(i < 1){
				result.put("result", "fail");
				result.put("msg", "审核失败！该代理商资质已经被审核");
				return result;
			}
		}
		int num = masterDao.update("agentAudit.updateStatus", params);
		Long userId = Long.valueOf(params.get("userId"));
		String pageUrl = params.get("pageUrl");
		String ip = params.get("ip");
		if(num > 0){
			result.put("result", "success");
			result.put("msg", "审核资质认证完成");
			masterDao.insert("agentAudit.insertAuditLog", params);
		}else{
			result.put("result", "fail");
			result.put("msg", "审核资质认证失败");
		}
//		logService.add(LogType.update, LogEnum.审核管理.getValue(),
//				"审核管理管理- 代理商资质审核：审核代理商资质", params, result);
		logService.add(LogType.update,  LogEnum.审核管理.getValue(), userId, pageUrl, ip,
				"审核管理管理- 客户资质审核：审核客户资质", params, result);
		
		return result;
	}

	@Override
	public Map<String, Object> view(Map<String, String> params) {
		Map<String,Object> data = new HashMap<String,Object>();
		if(StringUtils.isNotBlank(params.get("type"))){
			data = masterDao.getOneInfo("agentAudit.getAuditInfoOfAudit", params);
			data.put("remark","");
		}else{
			data = masterDao.getOneInfo("agentAudit.getAuditInfo", params);
		}
		if(data != null){
			String imgUrl = (String)data.get("img_url");
			if(imgUrl != null){
				String smspImgUrl =  ConfigUtils.smsp_img_url.endsWith("/")? ConfigUtils.smsp_img_url.substring(0, ConfigUtils.smsp_img_url.lastIndexOf("/")):ConfigUtils.smsp_img_url;
				data.put("img_url", SecurityUtils.encodeDes3(imgUrl)); // 给路径加密
				data.put("smsp_img_url", smspImgUrl); // 添加图片服务器地址
			}
		}

		return data;
	}

}
