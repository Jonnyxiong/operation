package com.ucpaas.sms.service.account;

import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.user.entity.JsmsAccount;
import com.jsmsframework.user.entity.JsmsClientInfoExt;
import com.jsmsframework.user.entity.JsmsUserPropertyLog;
import com.ucpaas.sms.common.entity.R;
import com.ucpaas.sms.entity.message.Account;
import com.ucpaas.sms.entity.message.AgentInfo;
import com.ucpaas.sms.entity.po.AccountPo;
import com.ucpaas.sms.entity.po.ClientBalanceAlarmPo;
import com.ucpaas.sms.entity.po.DataAuthorityCondition;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.model.SmsOauthpic;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface AccountService {

	PageContainer query(Map<String, String> params);

	List<Map<String, Object>> queryAll(Map<String, String> params);

	PageContainer queryClientInfo(Map<String, String> params);

	PageContainer clientOrderRemain(Map<String, String> params);

	List<Map<String, Object>> queryAllClientInfo(Map<String, String> params);

	/**
	 * @Description: 获取代理商的详细信息
	 * @author: Niu.T
	 * @date: 2016年11月21日 下午8:18:13
	 * @param agentId
	 * @return Map<String,Object>
	 */
	Map<String, Object> queryAgentDetailInfo(int agentId);

	Map<String, Object> queryClientDetailInfo(String clientId);

	public Map<String, Object> updateStatus(Map<String, String> params);

	/**
	 * @Description: 修改代理商类型
	 * @author: Niu.T
	 * @date: 2016年11月22日 下午7:01:59
	 * @param params
	 * @return Map<String,Object>
	 */
	Map<String, Object> updateAgentType(Map<String, String> params);

	public Map<String, Object> updateClientStatus(String clientId, int status);

	/**
	 * @Description: 获取代理商OEM配置资料
	 * @author: Niu.T
	 * @date: 2016年11月21日 下午8:30:59
	 * @param params
	 * @return Map<String,Object>
	 */
	public Map<String, Object> getOEMdataConfig(Map<String, String> params);

	/**
	 * @Description: 上传OEM代理商配置资料
	 * @author: Niu.T
	 * @date: 2016年11月29日 上午9:58:02
	 * @param params
	 * @return Map<String,Object>
	 */
	Map<String, Object> updateAgentInfo(Map<String, String> params);

	/**
	 * 添加客户
	 * 
	 * @param account
	 *            客户对象
	 * @return
	 */
	R addClient(Account account, Long adminId, String pageUrl, String ip,boolean sale);

	/**
	 * 修改客户
	 * 
	 * @param account
	 * @return
	 */
	Map<String, Object> updateClient(Account account, Long adminId, String pageUrl, String ip);

	/**
	 * 修改客户的计费规则
	 * @param account
	 * @param adminId
	 * @param pageUrl
	 * @param ip
	 * @return
	 */
	R updateClienttChargeRule(Account account, Long adminId, String pageUrl, String ip);

	/**
	 * 修改客户
	 *
	 * @param account
	 * @return
	 */
	ResultVO updatePsd(JsmsAccount account);

	/**
	 * 查询代理商的信息
	 * 
	 * @return
	 */
	List<AgentInfo> findAgentInfoList(Integer agentType);
	/**
	 * 查询已经资质认证的代理商的信息
	 *
	 * @return
	 */
	List<AgentInfo> findAgentInfoList2(Integer agentType);

	/**
	 * 查询直客列表
	 * 
	 *
	 */
	//Page findDirectclientList(Page<AccountPo> page);
	PageContainer findDirectclientList(Map<String, String> params);

	List<Map<String, Object>> findAllDirectclientList(Map params);

	AccountPo getDirectclientDetailInfo(String clientId);

	AccountPo getClientInfo(String clientId);

	String checkClient(Account account);

	String checkDirectclient(Account account);
	/**
	 * @Description: 添加用户资质信息,返回结果信息(map)
	 * @param smsOauthPic
	 * @return: Map<String,Object>
	 */
	Map<String,Object> addCerInfo(SmsOauthpic smsOauthPic,Long userId) throws Exception;

	/**
	 * @Description: 修改用户资质信息,返回结果信息(map)
	 * @return: Map<String,Object>
	 */
	Map<String,Object> updateCerInfo(SmsOauthpic smsOauthPic) throws Exception;

	/**
	 * 查询未绑定的子账户信息
	 *
	 * @return
	 */
	List<Account> getAllSubAccount();
	/**
	 * 查询已绑定的子账户信息
	 *
	 * @return
	 */
	List<Account> getAllBindingSubAccount(String clientid);


	/**
	 * 子账户信息管理
	 *
	 * @return
	 */
	boolean updateBindingSubAccountOfMessage(String clientid,Map<String,String> params)throws Exception;

	Account getAccount(String clientId);
	/*
	**
			* 修改接口密码
	 *
			 * @param account
	 * @return
			 */
	ResultVO updateWebPsd(JsmsClientInfoExt account);

	/**
	 * 是否是其他客户的子客户
	 *
	 * @return
	 */
	int getparentId(String clientId);


	/**
	 * 获取客户将要生效的计费规则
	 * @param clientId
	 * @return
	 */
	JsmsUserPropertyLog getLatelyChargeRuleByClientid(String clientId);

	R saveClientBalanceAlarm(ClientBalanceAlarmPo clientBalanceAlarm);

	PageContainer queryCustomerStarLevel(Map<String, String> params);

	List<Map<String, Object>> queryAllCustomerStarLevel(Map<String, String> params);

	AccountPo getAccountPoOfStarLevel(String clientId);

	Set<String> findAllListByBelongSales(DataAuthorityCondition dataAuthorityCondition);

	/**
	 * @Description: 查询该归属销售下的客户总数
	 * @Author: tanjiangqiang
	 * @Date: 2018/1/17 - 14:58
	 * @param belongSale 归属销售
	 * @param notStatus 状态
	 *
	 */
	int getCountByBelongSale(long belongSale, int notStatus);

	/**
	 * @param client_id 子账户id
	 * @param operator  操作者(为空则传0)
	 * @Description: 判断子账户有没有赠送短信, 没有则赠送, 有则不赠送
	 * @Author: tanjiangqiang
	 * @Date: 2018/1/3 - 10:59
	 */
	void isGiveMessage(String client_id, Long operator);

	/**
	 * @param clientid 子账户id
	 * @param operator 操作者(为空则传0)
	 * @Description: 新增子账户bestow_sms属性为1(1为已赠送短信)
	 * @Author: tanjiangqiang
	 * @Date: 2018/1/3 - 10:03
	 */
	void insertValueByClientidAndProperty(String clientid, Long operator);

	/**
	 * @param clientid 子账户id
	 * @param operator 操作者(为空则传0)
	 * @Description: 根据子账户id更新子账户bestow_sms属性为1(1为已赠送短信)
	 * @Author: tanjiangqiang
	 * @Date: 2018/1/3 - 10:03
	 */
	void updateValueByClientidAndProperty(String clientid, Long operator);
}
