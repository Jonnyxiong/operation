package com.ucpaas.sms.service.admin;

import java.util.Map;

import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.model.PageContainer;

/**
 * 管理员中心
 * 
 * @author xiejiaan
 */
public interface AdminService {

	/**
	 * 获取管理员资料
	 * 
	 * @param sid
	 * @return
	 */
	Map<String, Object> getAdmin(Long id);

	/**
	 * 保存管理员资料，包括添加、修改
	 * 
	 * @param admin
	 * @return
	 */
	Map<String, Object> saveAdmin(Map<String, String> admin);
 
	/**
	 * 保存密码
	 * 
	 * @param params
	 * @return
	 */
	Map<String, Object> savePassword(Map<String, String> params);

	/**
	 * 查询管理员
	 * 
	 * @param params
	 * @return
	 */
	PageContainer query(Map<String, String> params);

	/**
	 * 修改状态：恢复、删除
	 * 
	 * @param sid
	 * @param status
	 * @return
	 */
	Map<String, Object> updateStatus(Long id, int status, Long userId, String pageUrl, String ip);
	/**
	 * 发送短信验证码
	 * 
	 * @param mobile
	 *            手机号码
	 * @return
	 */ 
	Map<String, Object> sendVerifyCode(String mobile, Long userId, String pageUrl, String ip);


	/**
	 * 确认转交
	 * @param parmas
	 */
	ResultVO confirmTransfer(Map<String,String> parmas);


}
