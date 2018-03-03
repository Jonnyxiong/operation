/**
 * Copyright &copy; 2012-2016 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.ucpaas.sms.mapper.message;


import com.jsmsframework.finance.entity.JsmsUserPriceLog;
import com.ucpaas.sms.dto.ClientConsumeVO;
import com.ucpaas.sms.entity.message.Account;
import com.ucpaas.sms.entity.po.AccountPo;
import com.ucpaas.sms.entity.po.DataAuthorityCondition;
import com.ucpaas.sms.model.Page;
import com.ucpaas.sms.model.SmsOauthpic;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 子账号DAO接口
 * 
 * @author lpjLiu
 * @version 2017-07-12
 */
@Repository
public interface AccountMapper {

	String getOemDomainNameByAgentId(String agentId);

	Account accountApplyCheckInAcc(Account account);

	int checkAccount(Account account);

	int insert(Account account);

	int update(Account account);

	int updateSelective(Account account);

	Map<String, Object> getExtendportAssign(Map<String, Object> params);

	int updateExtendportAssign(Map<String, Object> params);

	int batchAddUserPrice(List<JsmsUserPriceLog> userPriceLogList);

	List<AccountPo> findList(Page<AccountPo> page);

	List<Map<String, Object>> findAllListOfMap(Map params);

	AccountPo getDirectclientInfo(String clientId);

	AccountPo getClientInfo(String clientId);

	/**
	 * 仅为更新时查询需要的字段
	 * 
	 * @return
	 */
	Account getForUpdate(String clientId);

	/**
	 * 根据客户类型查询客户
	 * 
	 * @param clientType
	 *            客户组的客户类型、0 代理商子客户、1 直客
	 * @return
	 */
	List<Account> getAccountsForAccountGroup(@Param("clientType") Integer clientType);

	List<ClientConsumeVO> queryStatisticClientInfo(@Param("clientids") Set clientids);

	List<String> queryIdByName(@Param("clientName") String clientName);
	/**
	 * 添加用户资质信息
	 */
	public int addCerInfo(SmsOauthpic smsOauthPic);

	/**
	 * 修改用户资质信息
	 */
	public int updateCerInfo(SmsOauthpic smsOauthPic);
	/**
	 * 用户信息更改时,更新账户信息
	 */
	public int updateAccWithCer(SmsOauthpic SmsOauthPic);

	/**
	 * 查询未绑定的子账户信息
	 *
	 * @return
	 */
	public List<Account> getAllSubAccount();
	/**
	 * 查询已绑定的子账户信息
	 *
	 * @return
	 */
	public List<Account> getAllBindingSubAccount(String clientid);

	/**
	 * 将已绑定的子账户信息解除绑定
	 *
	 * @return
	 */
	boolean updateBindingSubAccountNull(@Param("clientId")String clientId);
	/**
	 * 将未绑定的子账户信息进行绑定
	 *
	 * @return
	 */
	boolean updateBindingSubAccount(@Param("clientId")String clientId,@Param("parentId")String parentId);

	Account getAccount(String clientId);

	/**
	 * 更新是否支持子账户
	 *
	 * @return
	 */
	boolean updateExtValue(@Param("clientId") String clientId,@Param("extValue") Integer extValue);

	/**
	 * 是否是其他客户的子客户
	 *
	 * @return
	 */
	int getparentId(@Param("clientId")String clientId);
	/**
	 * 用户信息更改时,更新账户信息   代理商
	 */
	public int updateAccWithCerOfInfo(SmsOauthpic SmsOauthPic);

	/**
	 * 添加用户资质信息    直客
	 */
	public int addCerInfoOfZK(SmsOauthpic smsOauthPic);
	/**
	 * 更新用户资质信息    直客
	 */
	public int updateCerInfoOfZK(SmsOauthpic smsOauthPic);
	/**
	 * 修改用户资质信息   代理商
	 */
	public int updateInfo(SmsOauthpic smsOauthPic);
    //代理商子客户、直客是否已经存在认证信息
	public int getAccountOfSize(String clientid);
	//代理商是否已经存在认证信息
	public int getCerInfo(int agentid);

	AccountPo getAccountPoOfStarLevel(@Param("clientId")String clientId);

	//根据代理商id,客户id,认证类型获取资质认证数据
	public List<SmsOauthpic> getSmsOauthpic(@Param("agentId") Integer agentId,@Param("clientid") String clientid,@Param("oauthType") Integer oauthType);
	//根据资质表id删除一条资质
	public int deleteSmsOauthpic(@Param("id") Integer id);
	//按权限获取获取客户总数和本月新增客户个数(needQuerySaleIsNullData)
	public int getAgentUserNum(DataAuthorityCondition dataAuthorityCondition);

	//获取t_sms_oem_client_order的数量(已经赠送短信)
	int countClientOrder(@Param("clientId") String clientId);

	Set<String> findAllListByBelongSales(DataAuthorityCondition dataAuthorityCondition);

	/**
	  * @Description: 查询该归属销售下的客户总数
	  * @Author: tanjiangqiang
	  * @Date: 2018/1/17 - 14:58
	  * @param belongSale 归属销售
	  * @param notStatus 状态
	  *
	  */
	int getCountByBelongSale(@Param("belongSale") long belongSale, @Param("notStatus") int notStatus);
}