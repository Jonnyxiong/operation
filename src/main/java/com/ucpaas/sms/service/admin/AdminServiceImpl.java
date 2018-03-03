package com.ucpaas.sms.service.admin;

import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.common.util.MD5;
import com.ucpaas.sms.common.util.SecurityUtils;
import com.ucpaas.sms.constant.LogConstant.LogType;
import com.ucpaas.sms.constant.UserConstant;
import com.ucpaas.sms.dao.MessageMasterDao;
import com.ucpaas.sms.enums.LogEnum;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.common.LogService;
import com.ucpaas.sms.util.api.RestUtils;
import com.ucpaas.sms.util.api.RestUtils.SmsTemplateId;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理员中心
 * 
 * @author xiejiaan
 */
@Service
@Transactional
public class AdminServiceImpl implements AdminService {
	private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);
	@Autowired
	private MessageMasterDao masterDao;
	@Autowired
	private LogService logService;

	@Override
	public Map<String, Object> getAdmin(Long id) {
		return masterDao.getOneInfo("admin.getAdmin", id);
	}

	@Override
	public Map<String, Object> saveAdmin(Map<String, String> params) {
		logger.debug("保存管理员资料，添加/修改：" + params);
		
		Map<String, Object> data = new HashMap<String, Object>();
		
		//判断管理员身份和web应用是否对应
//		Integer role_id = Integer.valueOf(params.get("role_id"));
//		Integer web_id = Integer.valueOf(params.get("web_id"));
		
//		Map<String,Object> webIdMap = masterDao.getOneInfo("admin.getWebIdByRoleId", role_id);
//		Integer web_id_temp = (Integer) webIdMap.get("web_id");
//
//		if(!web_id_temp.equals(web_id)){
//			data.put("result", "fail");
//			data.put("msg", "管理员身份和管理员所属的web应用系统不匹配，请重新选择");
//			return data;
//		}
		
		Map<String, Object> check = masterDao.getOneInfo("admin.checkAdmin", params);// 查重
		if (check != null) {
			if (params.get("email").equals(check.get("email"))) {
				data.put("result", "fail");
				data.put("msg", "管理账号已被使用，请重新输入");
				return data;

			} else if (params.get("mobile").equals(check.get("mobile"))) {
				data.put("result", "fail");
				data.put("msg", "联系手机已被使用，请重新输入");
				return data;
			}
		}

		String password = params.get("password");
		if (StringUtils.isNotBlank(password)) {
//			password = EncryptUtils.encodeMd5(password);
			password = MD5.md5(password);
			params.put("password", password);
		} else {
			params.remove("password");
		}
		String id = params.get("id");
		Long userId = Long.valueOf(params.get("userId"));
		String pageUrl = params.get("pageUrl");
		String ip = params.get("ip");
		if (StringUtils.isBlank(id)) {// 添加管理员
//			String sid = SecurityUtils.generateSid();
//
//			params.put("sid", sid);
//			int i = masterDao.insert("admin.insertAdmin", params);
//			if (i > 0) {
//				data.put("result", "success");
//				data.put("msg", "添加成功");
//				params.put("user_id", String.valueOf(params.get("id")));
//				masterDao.insert("admin.insertAdminRole", params);// 添加角色
//			} else {
//				data.put("result", "fail");
//				data.put("msg", "添加失败");
//			}
//			logService.add(LogType.add, LogEnum.管理中心_运营平台.getValue(),"管理员中心-管理员管理：添加管理员", params, data);
			logService.add(LogType.add,  LogEnum.管理中心_运营平台.getValue(), userId, pageUrl, ip,"管理员中心-管理员管理：添加管理员", params, data);

		} else {// 修改管理员
			int i = masterDao.update("admin.updateAdmin", params);
			if (i > 0) {
				data.put("result", "success");
				data.put("msg", "修改成功");

//				String roleId = params.get("role_id");
//				if (StringUtils.isNotBlank(roleId) && !roleId.equals(params.get("old_role_id"))) {// 修改角色
//					params.put("user_id", String.valueOf(params.get("id")));
//					masterDao.update("admin.updateAdminRole", params);
//				}
			} else {
				data.put("result", "fail");
				data.put("msg", "管理员不存在，修改失败");
			}
			logService.add(LogType.update,  LogEnum.管理中心_运营平台.getValue(), userId, pageUrl, ip,"管理员中心-管理员管理：修改管理员", params, data);
		}

		return data;
	}

	

	@Override
	public Map<String, Object> savePassword(Map<String, String> params) {
		Map<String, Object> data = new HashMap<String, Object>();

		String password = params.get("password");
		password = SecurityUtils.encryptMD5(password);
		password = MD5.md5(password);
		params.put("password", password);

		password = params.get("newPassword");
		password = SecurityUtils.encryptMD5(password);
		password = MD5.md5(password);
		params.put("newPassword", password);

		int i = masterDao.update("admin.savePassword", params);
		if (i > 0) {
			data.put("result", "success");
			data.put("msg", "修改成功");
		} else {
			data.put("result", "fail");
			data.put("msg", "当前密码错误，修改失败");
		}
		Long userId = Long.valueOf(params.get("userId"));
		String pageUrl = params.get("pageUrl");
		String ip = params.get("ip");
		logService.add(LogType.update,  LogEnum.管理中心_运营平台.getValue(), userId, pageUrl, ip,"管理员中心：修改密码", params, data);
		return data;
	}

	@Override
	public PageContainer query(Map<String, String> params) {
		return masterDao.getSearchPage("admin.query", "admin.queryCount", params);
	}

	@Override
	public Map<String, Object> updateStatus(Long id, int status, Long userId, String pageUrl, String ip) {
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, Object> params = new HashMap<String, Object>();
		String msg;
		LogType logType = null ;
		switch (status) {
		case UserConstant.USER_STATUS_1:
			msg = "启用";
			logType =  LogType.update;
			break;
		case UserConstant.USER_STATUS_2:
			msg = "禁用";
			logType =  LogType.update;
			break;
		case UserConstant.USER_STATUS_3:
			msg = "删除";
			logType =  LogType.delete;
			break;
		default:
			data.put("result", "fail");
			data.put("msg", "不是恢复或删除，操作失败");
			return data;
		}
		if (Long.valueOf(1).equals(id)) {
			data.put("result", "fail");
			data.put("msg", "不可以删除系统超级管理员");
			return data;
		}

		params.put("id", id);
		params.put("status", status);
		int i = 0;
		if(status == UserConstant.USER_STATUS_3){
			i = masterDao.delete("admin.deleteUser", params);
		}else{
			i = masterDao.update("admin.updateStatus", params);
		} 
		
		if(i > 0){
			data.put("result", "success");
			data.put("msg", msg + "成功");
		}else{
			data.put("result", "fail");
			data.put("msg", "管理员不存在或不能删除，" + msg + "失败");
		}
		logService.add(logType,  LogEnum.管理中心_运营平台.getValue(), userId, pageUrl, ip,"管理员中心-管理员管理：修改管理员状态", msg, params, data);
		return data;
	}

	@Override
	public Map<String, Object> sendVerifyCode(String mobile, Long userId, String pageUrl, String ip) {
		Map<String, Object> data = new HashMap<String, Object>();
		if (masterDao.getSearchSize("admin.checkMobile", mobile) > 0) {
			data.put("result", "fail");
			data.put("msg", "联系手机已经被绑定，请重新输入");
			return data;
		}

		String verifyCode = RandomStringUtils.randomNumeric(4);
		boolean result = RestUtils.sendTemplateSMS(SmsTemplateId.verify_code, mobile, verifyCode);
		if (result) {
			data.put("encrypt_verify_code", SecurityUtils.encryptMD5(mobile + verifyCode));
			data.put("result", "success");
			data.put("msg", "发送短信验证码成功");
		} else {
			data.put("result", "fail");
			data.put("msg", "发送短信验证码失败，请联系管理员");
		}
		logService.add(LogType.update,LogEnum.管理中心_运营平台.getValue(), userId, pageUrl, ip,"管理员中心-管理员资料-修改：获取验证码", mobile, verifyCode, data);
		return data;
	}

	/**
	 * 确认转交
	 * @param parmas
	 */
	@Override
	public ResultVO confirmTransfer(Map<String, String> parmas) {

		String oldUserId = parmas.get("oldUserId");
		String newUserId = parmas.get("newUserId");

		//查看归属销售对应的代理商数目
		Map<String,Object> oldUserIdParams = new HashMap<>();
		oldUserIdParams.put("belong_sale",oldUserId);
		int updateAgentNum = masterDao.getOneInfo("admin.getAgentNumBySale",oldUserIdParams);

		//查看归属销售对应的客户数目
		int uddateAccountNum = masterDao.getOneInfo("admin.getAccountNumBySale",oldUserIdParams);

		int totalUpdateNum = updateAgentNum + uddateAccountNum;
		if(totalUpdateNum == 0){
			return  ResultVO.failure("该销售名下无客户！");
		}

		Map<String,Object> updateSaleParams = new HashMap<>();
		updateSaleParams.put("old_belong_sale",oldUserId);
		updateSaleParams.put("new_belong_sale",newUserId);

		//先转代理商的归属销售
		int updateAgent = masterDao.update("admin.updateAgentSale", updateSaleParams);

		//在转oem的归属销售
		int updateAccount = masterDao.update("admin.updateAccountSale", updateSaleParams);
		if(updateAgent + updateAccount > 0){
			return  ResultVO.successDefault();
		}else {
			return  ResultVO.failure("数据已被修改, 请刷新后再试");
		}
	}



}
