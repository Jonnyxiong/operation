package com.ucpaas.sms.service;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;
import com.jsmsframework.common.enums.*;
import com.jsmsframework.finance.entity.JsmsClientBalanceAlarm;
import com.jsmsframework.finance.entity.JsmsUserPriceLog;
import com.jsmsframework.finance.service.JsmsClientBalanceAlarmService;
import com.jsmsframework.order.service.JsmsAccountFinanceService;
import com.jsmsframework.product.entity.JsmsOemAgentProduct;
import com.jsmsframework.product.exception.JsmsOemAgentProductException;
import com.jsmsframework.product.service.JsmsOemAgentProductService;
import com.jsmsframework.user.entity.JsmsClientInfoExt;
import com.jsmsframework.user.entity.JsmsOemDataConfig;
import com.jsmsframework.user.entity.JsmsUserPropertyLog;
import com.jsmsframework.user.mapper.JsmsOemDataConfigMapper;
import com.jsmsframework.user.service.JsmsClientInfoExtService;
import com.jsmsframework.user.service.JsmsUserPropertyLogService;
import com.ucpaas.sms.common.entity.R;
import com.ucpaas.sms.common.util.Collections3;
import com.ucpaas.sms.common.util.MD5;
import com.ucpaas.sms.common.util.SecurityUtils;
import com.ucpaas.sms.constant.LogConstant;
import com.ucpaas.sms.constant.LogConstant.LogType;
import com.ucpaas.sms.dto.AgentRequest;
import com.ucpaas.sms.entity.message.*;
import com.ucpaas.sms.entity.po.ClientBalanceAlarmPo;
import com.ucpaas.sms.entity.po.DataAuthorityCondition;
import com.ucpaas.sms.enums.AgentType;
import com.ucpaas.sms.enums.LogEnum;
import com.ucpaas.sms.enums.WebId;
import com.ucpaas.sms.exception.AgentException;
import com.ucpaas.sms.exception.OperationException;
import com.ucpaas.sms.mapper.message.AccountMapper;
import com.ucpaas.sms.mapper.message.AgentInfoMapper;
import com.ucpaas.sms.mapper.message.OemDataConfig;
import com.ucpaas.sms.model.Page;
import com.ucpaas.sms.model.SmsOauthpic;
import com.ucpaas.sms.service.account.AccountService;
import com.ucpaas.sms.service.audit.CustomerAuditService;
import com.ucpaas.sms.service.common.CommonSeqService;
import com.ucpaas.sms.service.common.CommonService;
import com.ucpaas.sms.service.common.LogService;
import com.ucpaas.sms.service.email.EmailService;
import com.ucpaas.sms.util.AgentIdUtil;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.JacksonUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author huangwenjie
 * @description 代理商信息表
 * @date 2017-07-13
 */
@Service
public class AgentInfoServiceImpl implements AgentInfoService {

	private static Logger logger = LoggerFactory.getLogger(AgentInfoServiceImpl.class);

	@Autowired
	private AgentInfoMapper agentInfoMapper;
	@Autowired
	private OemDataConfigService oemDataConfigService;
	@Autowired
	private UserService userService;
	@Autowired
	private UserRoleService userRoleService;
	@Autowired
	private AgentAccountService agentAccountService;
	@Autowired
	private LogService logService;
	@Autowired
	private MailpropService mailpropService;

	@Autowired
	private EmailService emailService;
	@Autowired
	private RoleService roleService;

	@Autowired
	private JsmsOemAgentProductService oemAgentProductService;

	@Autowired
	private AccountMapper accountMapper;
	@Autowired
	private CommonSeqService commonSeqService;
	@Autowired
	private JsmsClientInfoExtService jsmsClientInfoExtService;
	@Autowired
	private JsmsClientBalanceAlarmService jsmsClientBalanceAlarmService;
	@Autowired
	private JsmsUserPropertyLogService jsmsUserPropertyLogService;
	@Autowired
	private JsmsAccountFinanceService jsmsAccountFinanceService;
	@Autowired
	private CustomerAuditService customerAuditService;
	@Autowired
	private JsmsOemDataConfigMapper jsmsOemDataConfigMapper;
	@Autowired
	private CommonService commonService;
	@Autowired
	private AccountService accountService;

	// @Resource
	// private void
	// setAgentAccountService(com.ucpaas.sms.service.AgentAccountService
	// agentAccountService){
	// this.agentAccountService = agentAccountService;
	// }

	@Override
	@Transactional("message")
	public int insert(AgentInfo model) {
		return this.agentInfoMapper.insert(model);
	}

	@Override
	@Transactional("message")
	public int insertBatch(List<AgentInfo> modelList) {
		return this.agentInfoMapper.insertBatch(modelList);
	}

	@Override
	@Transactional("message")
	public int update(AgentInfo model) {
		AgentInfo old = this.agentInfoMapper.getByAgentId(model.getAgentId());
		if (old == null) {
			return 0;
		}
		return this.agentInfoMapper.update(model);
	}

	@Override
	@Transactional("message")
	public int updateSelective(AgentInfo model) {
		AgentInfo old = this.agentInfoMapper.getByAgentId(model.getAgentId());
		if (old != null)
			return this.agentInfoMapper.updateSelective(model);
		return 0;
	}

	@Override
	@Transactional("message")
	public AgentInfo getByAgentId(Integer agentId) {
		AgentInfo model = this.agentInfoMapper.getByAgentId(agentId);
		return model;
	}

	@Override
	@Transactional("message")
	public AgentInfo getByAdminId(Integer adminId) {
		AgentInfo model = this.agentInfoMapper.getByAdminId(adminId);
		return model;
	}

	/**
	 * 账户设置余额告警，验证码500条、通知500条、营销500条、国际10元
	 * @param account
	 * @param agentMobile
	 * @param agentEmail
	 * @return
	 */
	public int insertClientBalanceAlarm(Account account, String agentMobile, String agentEmail) {
		if (account == null || StringUtils.isBlank(account.getClientid()))
		{
			return 0;
		}

		ClientBalanceAlarmPo clientBalanceAlarm = new ClientBalanceAlarmPo();
		clientBalanceAlarm.setClientid(account.getClientid());
		clientBalanceAlarm.setYzmAlarmNumber(500);
		clientBalanceAlarm.setTzAlarmNumber(500);
		clientBalanceAlarm.setYxAlarmNumber(500);
		clientBalanceAlarm.setGjAlarmAmount(new BigDecimal("10"));

		if (StringUtils.isNotBlank(agentMobile)) {
			clientBalanceAlarm.setAlarmPhone(agentMobile);
		}
		if (StringUtils.isNotBlank(account.getMobile())) {
			if (StringUtils.isNotBlank(clientBalanceAlarm.getAlarmPhone())) {
				clientBalanceAlarm.setAlarmPhone(clientBalanceAlarm.getAlarmPhone() + "," + account.getMobile());
			} else {
				clientBalanceAlarm.setAlarmPhone(account.getMobile());
			}
		}

		if (StringUtils.isNotBlank(agentEmail)) {
			clientBalanceAlarm.setAlarmEmail(agentEmail);
		}

		if (StringUtils.isNotBlank(account.getEmail())) {
			if (StringUtils.isNotBlank(clientBalanceAlarm.getAlarmEmail())) {
				clientBalanceAlarm.setAlarmEmail(clientBalanceAlarm.getAlarmEmail() + "," + account.getEmail());
			} else {
				clientBalanceAlarm.setAlarmEmail(account.getEmail());
			}
		}
		// 构造验证码
		JsmsClientBalanceAlarm yzm = buildJsmsClientBalanceAlarm(clientBalanceAlarm, clientBalanceAlarm.getYzmAlarmNumber(),
				null, ClientAlarmType.验证码.getValue());
		// 构造通知
		JsmsClientBalanceAlarm tz = buildJsmsClientBalanceAlarm(clientBalanceAlarm, clientBalanceAlarm.getTzAlarmNumber(),
				null, ClientAlarmType.通知.getValue());
		// 构造营销
		JsmsClientBalanceAlarm yx = buildJsmsClientBalanceAlarm(clientBalanceAlarm, clientBalanceAlarm.getYxAlarmNumber(),
				null, ClientAlarmType.营销.getValue());
		// 构造国际
		JsmsClientBalanceAlarm gj = buildJsmsClientBalanceAlarm(clientBalanceAlarm, null,
				clientBalanceAlarm.getGjAlarmAmount(), ClientAlarmType.国际.getValue());

		List<JsmsClientBalanceAlarm> list = new ArrayList<>();
		list.add(yzm);
		list.add(tz);
		list.add(yx);
		list.add(gj);

		logger.debug("客户余额提醒设置添加 {}", JSON.toJSONString(list));
		int count = jsmsClientBalanceAlarmService.insertBatch(list);
		if (count !=0 && count != list.size())
		{
			count = 0;
		}
		return count;
	}

	private JsmsClientBalanceAlarm buildJsmsClientBalanceAlarm(ClientBalanceAlarmPo clientBalanceAlarmPo, Integer alarmNumber, BigDecimal alarmAmount, int type)
	{
		if (clientBalanceAlarmPo == null)
		{
			return null;
		}

		JsmsClientBalanceAlarm jsmsClientBalanceAlarm = new JsmsClientBalanceAlarm();
		jsmsClientBalanceAlarm.setClientid(clientBalanceAlarmPo.getClientid());
		jsmsClientBalanceAlarm.setAlarmPhone(clientBalanceAlarmPo.getAlarmPhone());
		jsmsClientBalanceAlarm.setAlarmEmail(clientBalanceAlarmPo.getAlarmEmail());
		jsmsClientBalanceAlarm.setAlarmType(type);
		jsmsClientBalanceAlarm.setAlarmNumber(alarmNumber);
		jsmsClientBalanceAlarm.setAlarmAmount(alarmAmount);
		jsmsClientBalanceAlarm.setReminderNumber(0);
		jsmsClientBalanceAlarm.setResetTime(Calendar.getInstance().getTime());
		jsmsClientBalanceAlarm.setCreateTime(jsmsClientBalanceAlarm.getResetTime());
		jsmsClientBalanceAlarm.setUpdateTime(jsmsClientBalanceAlarm.getResetTime());
		return jsmsClientBalanceAlarm;
	}

	@Override
	@Transactional("message")
	public Page queryList(Page page) {
		List<AgentInfo> list = this.agentInfoMapper.queryList(page);
		page.setData(list);
		return page;
	}

	@Override
	@Transactional("message")
	public int count(Map<String, Object> params) {
		return this.agentInfoMapper.count(params);
	}

	@Override
	@Transactional("message")
	public R insert(AgentRequest agentRequest, Long userId, String pageUrl, String ip,boolean isSale) {
		logger.debug("添加代理商{}", JacksonUtil.toJSON(agentRequest));
		Date now = new Date();

		String password = agentRequest.getPassword();
		//代理商名称
		if(StringUtils.isBlank(agentRequest.getCompany())){
			throw new AgentException("代理商名称不能为空!");
		}
		//邮箱
		if(StringUtils.isBlank(agentRequest.getEmail())){
			throw new AgentException("邮箱不能为空!");
		}
		//手机号
		if(StringUtils.isBlank(agentRequest.getMobile())){
			throw new AgentException("手机号码不能为空");
		}
		//归属销售
		if(agentRequest.getBelongSale()<0){
			throw new AgentException("归属销售不能为空");
		}
		//密码
		if(StringUtils.isBlank(password)){
			throw new AgentException("登入密码不能为空!");
		}

		if(password.length() < 8 || password.length() > 12){
			throw new AgentException("登入密码长度介于8至12之间!");
		}

		// 1. 判断手机号和邮箱是否被使用了
		Page page = new Page<>();
		page.setPageSize(-1);
		page.getParams().put("email", agentRequest.getEmail());
		List<Integer> webIds = Arrays.asList(WebId.OEM代理商.getValue(), WebId.品牌代理商.getValue());
		page.getParams().put("webIds", webIds);

		List<User> users = userService.queryList(page).getData();
		logger.debug("根据邮箱查询是否有该用户 ,params={}，users={}", JacksonUtil.toJSON(page), users);
		if (null != users && !users.isEmpty()) {
			logger.debug("查询出用户users={}", JacksonUtil.toJSON(users));
			throw new AgentException("邮箱已经被使用");
		}
		Integer type = agentRequest.getAgentType();
//		AgentType webId = null ;
//		if (type.equals(5)) {
//			webId = AgentType.OEM代理商;
//		} else if (type.equals(2)) {
//			webId = AgentType.品牌代理商;
//		}
		WebId webId = null ;
		if (type.equals(5)) {
			webId =WebId.OEM代理商;
		} else if (type.equals(2)) {
			webId=WebId.品牌代理商;
		}
		// 校验手机号唯一性
		List<User> mUser = userService.checkByMobile(agentRequest.getMobile(), String.valueOf(webId.getValue()));
		if (null != mUser && !mUser.isEmpty()) {
			StringBuilder msg = new StringBuilder();
			msg.append("手机号码已经被使用");
			throw new AgentException(msg);
		}

		// 2. 新增用户信息(添加信息到t_sms_user表中)
		//String password = UUID.randomUUID().toString().replace("-", "").substring(4, 12);
		User user = new User();
		user.setSid(SecurityUtils.generateSid());
		user.setEmail(agentRequest.getEmail());
		user.setRealname(agentRequest.getCompany());
		user.setPassword(MD5.md5(SecurityUtils.encryptMD5(password)));
		user.setUserType("1");
		user.setStatus("1");
		user.setMobile(agentRequest.getMobile());
		user.setCreateDate(now);
		user.setUpdateDate(now);
		user.setLoginTimes(0);
		if (agentRequest.isOEM()) {
			user.setWebId(WebId.OEM代理商.getValue());
		}

		if (agentRequest.isBrand()) {
			user.setWebId(WebId.品牌代理商.getValue());
		}

		logger.debug("新增用户信息{}", JacksonUtil.toJSON(user));
		int userCount = userService.insert(user);
		if (userCount == 0) {
			throw new AgentException("新增代理商失败");
		}

		// 3.新增用户角色关系(添加信息到t_sms_user_role表中)
		UserRole userRole = new UserRole();
		userRole.setUserId(user.getId());
		if (agentRequest.isOEM()) {
			List<Role> roles = roleService.queryRoleList(WebId.OEM代理商.getValue().toString());
			for (Role role : roles) {
				if (StringUtils.isNotEmpty(role.getRoleName()) && role.getRoleName().equals("OEM代理商")) {
					userRole.setRoleId(role.getId());
					break;
				}
			}
		}

		if (agentRequest.isBrand()) {
			userRole.setRoleId(3);
		}

		logger.debug("新增用户角色关联信息{}", JacksonUtil.toJSON(userRole));
		int in = userRoleService.insert(userRole);
		if(in!=1){
			throw new AgentException("新增用户角色关联信息失败!");
		}
		int agentId = AgentIdUtil.getAgentId();
		// 4.新增代理商信息表数据
		agentRequest.setAgentName(agentRequest.getCompany());
		agentRequest.setShorterName(agentRequest.getCompany());
		agentRequest.setAdminId(user.getId());
		agentRequest.setStatus("1");
		if(isSale){
            agentRequest.setOauthStatus(OauthStatusEnum.待认证.getValue());
        }else{
            agentRequest.setOauthStatus(OauthStatusEnum.证件已认证.getValue());//3为认证正常
            //认证时间
            agentRequest.setOauthDate(now);
        }
		agentRequest.setAgentId(agentId);
		agentRequest.setCreateTime(now);
		agentRequest.setUpdateTime(now);
		agentRequest.setOauthDate(now);
		logger.debug("新增代理商信息{}", JacksonUtil.toJSON(agentRequest));
		//添加信息到t_sms_agent_info表中
		int agentCount = agentInfoMapper.insert(agentRequest);
		if (agentCount !=1) {
			throw new AgentException("新增代理商失败!");
		}else{
			logger.debug("新增代理商成功!");
		}

		//资质认证的校验
		if (StringUtils.isBlank(agentRequest.getIdType())) {
			throw new AgentException("证件类型不能为空!");
		}
		if (StringUtils.isBlank(agentRequest.getIdNbr())) {
			throw new AgentException("证件号码不能为空!");
		}
		if (StringUtils.isBlank(agentRequest.getImgUrl())) {
			throw new AgentException("证件图片不能为空!");
		}
		//5.添加资质--添加数据到t_sms_oauth_pic表中
		SmsOauthpic smsOauthpic = new SmsOauthpic();
		smsOauthpic.setAgentId(agentRequest.getAgentId().toString());
		smsOauthpic.setClientId(null);
		smsOauthpic.setIdNbr(agentRequest.getIdNbr());//证件号码
		smsOauthpic.setImgUrl(agentRequest.getImgUrl());//证件图片
		smsOauthpic.setIdType(agentRequest.getIdType());//证件类型
		smsOauthpic.setCreateDate(now);
		smsOauthpic.setUpdateDate(now);
		smsOauthpic.setStatus("1");
		smsOauthpic.setOauthType("1");//代理商资质认证
		int addCer = accountMapper.addCerInfo(smsOauthpic);
		if(addCer!=1){
			logger.debug("添加客户资质:失败");
			throw new AgentException("添加客户资质:失败!");
		}else{
			logger.debug("添加客户资质: 成功");
		}

		//6.是否贴牌
		//域名为空,不贴牌
		if (StringUtils.isBlank(agentRequest.getDomainName())) {
			OemDataConfig oemConfig = new OemDataConfig();
			BeanUtils.copyProperties(agentRequest, oemConfig);
			logger.debug("生成OEM数据{}", JacksonUtil.toJSON(oemConfig));
			//客户端地址不能为空(需要去除http)
			if(StringUtils.isNotBlank(ConfigUtils.client_site_url)){
				String clientUrl=ConfigUtils.client_site_url;
				if(clientUrl.contains("//")){
					String[] ss = clientUrl.split("//");
					oemConfig.setDomainName(ss[1]);
				}else{
					oemConfig.setDomainName(ConfigUtils.client_site_url);
				}
			}
			oemConfig.setCopyrightText("2014-2018 深圳市云之讯网络技术有限公司 粤ICP备14046848号");
			oemConfig.setNavigationBackcolor("#506470");
			oemConfig.setNavigationTextColor("#506470");
			oemConfig.setHySmsDiscount(BigDecimal.valueOf(1));
			oemConfig.setYxSmsDiscount(BigDecimal.valueOf(1));
			oemConfig.setGjSmsDiscount(BigDecimal.valueOf(1));
			//添加信息到t_sms_oem_data_config表中
			int i = oemDataConfigService.insert(oemConfig);
			if(i!=1){
				throw new AgentException("添加oem相关数据失败!");
			}else{
				logger.debug("添加oem相关数据成功!");
			}
		} else {
			//贴牌,为OEM代理商
			//if (agentRequest.isOEM() && (!ConfigUtils.oem_client_url.equals(agentRequest.getDomainName()))) {
				//版权文字不能空
				if(StringUtils.isBlank(agentRequest.getCopyrightText())){
					throw new AgentException("版权文字不能为空");
				}
				OemDataConfig oemConfig = new OemDataConfig();
				BeanUtils.copyProperties(agentRequest, oemConfig);
				logger.debug("生成OEM数据{}", JacksonUtil.toJSON(oemConfig));
					//如果是默认域名,需要处理掉http
				if(oemConfig.getDomainName().equals(ConfigUtils.client_site_url)){
					String clientUrl=ConfigUtils.client_site_url;
					if(clientUrl.contains("//")){
						String[] ss = clientUrl.split("//");
						oemConfig.setDomainName(ss[1]);
					}
				}
				//添加信息到t_sms_oem_data_config表中
				int i = oemDataConfigService.insert(oemConfig);
				if(i!=1){
					throw new AgentException("添加oem相关数据失败!");
				}else{
					logger.debug("添加oem相关数据成功!");
				}
			//}
		}

		// 7.新增代理商账户表信息
		AgentAccount agentAccount = new AgentAccount();
		agentAccount.setAgentId(agentRequest.getAgentId());
		agentAccount.setBalance(BigDecimal.ZERO);
		agentAccount.setCreditBalance(BigDecimal.ZERO);
		agentAccount.setAccumulatedIncome(BigDecimal.ZERO);
		agentAccount.setCommissionIncome(BigDecimal.ZERO);
		agentAccount.setAccumulatedRecharge(BigDecimal.ZERO);
		//添加数据到t_sms_agent_account表中
		int i = agentAccountService.insert(agentAccount);
		if(i!=1){
			throw new AgentException("新增代理商账户信息失败!");
		}

		//创建客户帐号
		String prefix = "b";
		String clientId = commonSeqService.getOrAddId(prefix);
		if (StringUtils.isBlank(clientId))
		{
			throw new AgentException("生成客户帐号失败，请重试!");
		}

		Account account = new Account();
		if(!isSale){
		//8.开通子账户
		if (agentRequest.getPaytype() != null && agentRequest.getAgentOwned() != null) {//根据付费方式和使用对象都有值认为是开通子账户
			//校验部分数据库不能为空的参数
			check(agentRequest);
			//代理商自己使用
			if ((AgentOwned.代理商自己使用.getValue()).equals(agentRequest.getAgentOwned())) {
				logger.debug("代理商自己使用,子客户认证信息克隆代理商资质");
				//创建客户帐号t_sms_account
				account.setOauthStatus(OauthStatusEnum.证件已认证.getValue());
				account.setOauthDate(now);
				insertAccount(agentRequest, account, clientId);

				//自动认证，继承代理商资质信息
				smsOauthpic.setClientId(clientId);
				smsOauthpic.setOauthType("2");//客户资质认证
				int addCerr = accountMapper.addCerInfo(smsOauthpic);
				if(addCerr!=1){
					throw new AgentException("资质copy失败!");
				}
				//赠送测试短信
				//customerAuditService.giveShortMessage(account);
				// Mod by lpjLiu: 生成订单ID
				List<Long> orderIds = new ArrayList<>();
				for (int j = 0; j < 3; j++) {
					orderIds.add(customerAuditService.getOemAgentOrderId());
				}
				JsmsOemDataConfig oemDataConfig = jsmsOemDataConfigMapper.getOemDataConfig(agentId);
				if(oemDataConfig==null){
					logger.debug("代理商：{}------->没有对应的oem资料", agentId);
				}else{
					Integer testProductId = oemDataConfig.getTestProductId();
					Integer testSmsNumber = oemDataConfig.getTestSmsNumber();
					Integer oemDataId = oemDataConfig.getId();
					//赠送测试短信
					jsmsAccountFinanceService.giveShortMessage(clientId,agentRequest.getPaytype(),
							agentRequest.getAgentId(),agentRequest.getName(), orderIds,testProductId,testSmsNumber,oemDataId);
					// 新增子账户bestow_sms属性值为1,标识已赠送短信
					accountService.insertValueByClientidAndProperty(clientId, userId);
				}

			} else if ((AgentOwned.代理商子客户使用.getValue()).equals(agentRequest.getAgentOwned())) {//代理商下级客户
				logger.debug("代理商子客户使用,需要再次认证资质,会提交结果后选择跳转到子客户认证页面");
				//不需要在这里认证,创建客户帐号
				//创建客户帐号t_sms_account
				account.setOauthStatus(OauthStatusEnum.待认证.getValue());
				insertAccount(agentRequest, account, clientId);

				//审核成功并资质认证之后赠送测试短信,还需要设置余额警告和赠送500等 todo
			}else{
				throw new AgentException("使用对象取值错误!");
			}
			boolean updateIdStatus = commonSeqService.updateClientIdStatus(clientId);
			logger.debug("插入账户数据及更新clientid状态结束======================================");
			//设置余额警告,验证码500条、通知500条、营销500条、国际10元
			int addAlarm = this.insertClientBalanceAlarm(account, agentRequest.getMobile(), agentRequest.getEmail());
			if (addAlarm <= 0) {
				logger.debug("设置余额警告和赠送短信失败!");
				throw new AgentException("设置余额警告和赠送短信失败!");
			}
			//8.1添加扩展信息表(用户信息拓展表t_sms_client_info_ext)
			JsmsClientInfoExt clientInfoExt = new JsmsClientInfoExt();
			clientInfoExt.setClientid(clientId);
			clientInfoExt.setParentId(clientId);
			clientInfoExt.setRemark(null);
			clientInfoExt.setUpdator(userId);
			clientInfoExt.setWebPassword(password);
			clientInfoExt.setExtValue(0);//默认不支持
			clientInfoExt.setStarLevel(3);
			clientInfoExt.setUpdateDate(Calendar.getInstance().getTime());
			int addExtCount = jsmsClientInfoExtService.insert(clientInfoExt);
			if(addExtCount>0){
				logger.debug("添加用户扩展信息成功");
			}else{
				logger.debug("添加用户扩展信息失败");
				throw new AgentException("添加用户扩展信息失败");
			}
			int batchAddUserPrice = 0;
			//8.2.后付费需要加入价格
			if(PayType.后付费.getValue().equals(agentRequest.getPaytype())){
				//后付费用户价格变更记录表不能为空
				if(Collections3.isEmpty(agentRequest.getUserPriceList())){
					throw new AgentException("验证码短信或通知短信或营销短信单价不能为空!");
				}
				// 插入用户价格
				for (JsmsUserPriceLog userPriceLog : agentRequest.getUserPriceList()) {
					if (userPriceLog.getSmstype() == null) {
						throw new AgentException("短信类型不能为空");
					}
					if (userPriceLog.getUserPrice() == null) {
						throw new AgentException("短信单价不能为空");
					}
					userPriceLog.setClientid(clientId);
					userPriceLog.setEffectDate(now);
					userPriceLog.setCreateTime(now);
					userPriceLog.setUpdateTime(now);
				}
				if (agentRequest.getUserPriceList() != null && agentRequest.getUserPriceList().size() > 0) {
					batchAddUserPrice = accountMapper.batchAddUserPrice(agentRequest.getUserPriceList());
				}
			}
			//8.3 增加计费规则
			JsmsUserPropertyLog userPropertyLog = new JsmsUserPropertyLog();
			userPropertyLog.setId(null);
			userPropertyLog.setClientid(clientId);
			userPropertyLog.setProperty("charge_rule");
			userPropertyLog.setValue(agentRequest.getChargeRule().toString());
			userPropertyLog.setEffectDate(now);
			userPropertyLog.setOperator(userId);
			userPropertyLog.setCreateTime(now);
			userPropertyLog.setUpdateTime(now);
			userPropertyLog.setRemark(null);
			int addChareRule = jsmsUserPropertyLogService.insert(userPropertyLog);
			if (updateIdStatus && addExtCount > 0 && addChareRule > 0
					&& batchAddUserPrice == agentRequest.getUserPriceList().size()) {
				// add by lpjLiu 20170928 增加操作日志
				String desc = prefix.equals("a") ? "直客开户" : "子账户开户";
				logService.add(LogConstant.LogType.add, LogEnum.账户信息管理.getValue(), userId, pageUrl, ip,
						"账户信息管理-客户开户-" + desc, JSON.toJSONString(account));
				//data.put("result", "success");
				//data.put("msg", "添加客户成功");
			} else {
				throw new AgentException("添加客户失败");
			}
		}
        }
		// Mod by lpjLiu 2017-12-21 唐晶提JIRA不发送邮件
		// 12.发邮件
//		String vUrl = ConfigUtils.agent_site_url;// 获取代理商服务器站点地址
//		Mailprop mailprop = null;
//		if (agentRequest.isBrand()) {
//			//根据邮件配置表t_sms_mailprop获取邮件配置信息
//			mailprop = mailpropService.getById(100017l); // 品牌代理商邮件
//			vUrl = ConfigUtils.agent_site_url;// 获取代理商服务器站点地址
//		}
//		if (agentRequest.isOEM()) {
//			mailprop = mailpropService.getById(100022l); // OEM代理商邮件
//			vUrl = ConfigUtils.oem_agent_site_url;// oem代理商站点地址
//		}
//
//		// 发送开户邮件到邮箱
//		String body = mailprop.getText();
//		body = body.replace("vUrl", vUrl);
//		body = body.replace("vemail", agentRequest.getEmail());
//		body = body.replace("password", password);
//		boolean sendEmail = emailService.sendHtmlEmail(agentRequest.getEmail(), mailprop.getSubject(), body);
//		if (sendEmail) {
//			logger.info("{}代理商邮件发送成功", agentRequest.getAgentId());
//		} else {
//			logger.info("{}代理商邮件发送失败, email={},password={}", agentRequest.getAgentId(), agentRequest.getEmail(),
//					user.getPassword());
//			throw new AgentException("代理商邮件发送失败!");
//		}

		logService.add(LogType.update, LogEnum.账户信息管理.getValue(), userId, pageUrl, ip, "账户信息管理-代理商申请记录：新增代理商",
				JacksonUtil.toJSON(agentRequest));
		logger.debug("增加代理商记录数为{}", agentCount);//agentCount只是个int数字

		Map<String, Object> data = Maps.newHashMap();
		//R 对象：包含代理商登录地址、代理商ID、邮箱、手机、密码，客户端网址、帐号、密码、接口帐号、接口密码
		data.put("agentLoginUrl",ConfigUtils.oem_agent_site_url);
		data.put("agentId", agentRequest.getAgentId());
		data.put("email", agentRequest.getEmail());
		data.put("mobile", agentRequest.getMobile());
		data.put("password", password);
		data.put("isSale",isSale);
		data.put("clientUrl",ConfigUtils.client_site_url);
		//帐号
		data.put("clientId",clientId);
		//密码
		data.put("password",password);
		//接口帐号
		data.put("interfaceClientId",clientId);
		//接口密码
		data.put("interfacePassword",password);
		return R.ok("新增代理商成功", data);
	}
	//增加计费规则
	private void addChargeRule(AgentRequest agentRequest, User user, String clientId) {
		JsmsUserPropertyLog userPropertyLog = new JsmsUserPropertyLog();
		userPropertyLog.setId(null);
		userPropertyLog.setClientid(clientId);
		userPropertyLog.setProperty("charge_rule");
		userPropertyLog.setValue(agentRequest.getChargeRule().toString());
		userPropertyLog.setEffectDate(agentRequest.getCreateTime());
		//userPropertyLog.setOperator(adminId);
		userPropertyLog.setOperator(user.getId());
		userPropertyLog.setCreateTime(agentRequest.getCreateTime());
		userPropertyLog.setUpdateTime(agentRequest.getUpdateTime());
		userPropertyLog.setRemark(null);
		int addChareRule = jsmsUserPropertyLogService.insert(userPropertyLog);
		if(addChareRule!=1){
			throw new AgentException("增加计费规则失败!");
		}
	}

	private void check(AgentRequest agentRequest) {
		//用户名称
		if(StringUtils.isBlank(agentRequest.getName())){
			throw new AgentException("用户名称不能为空!");
		}
		// 付费类型
		if (agentRequest.getPaytype() == null || agentRequest.getPaytype() > 1 || agentRequest.getPaytype() < 0) {
			throw new AgentException("付费类型只能是预付费或后付费!");
		}
		//短信协议
		if (agentRequest.getSmsfrom() > 6 || agentRequest.getSmsfrom() < 1) {
			throw new OperationException("客户接入使用协议类型只能是SMPP协议/CMPP2.0协议/CMPP3.0协议/SGIP协议/SMGP协议/HTTPS协议");
		}

		if (agentRequest.getSmsfrom().equals(6) && agentRequest.getHttpProtocolType() == null) {
			throw new AgentException("请选择https子协议类型");
		}
		if (agentRequest.getSmsfrom().equals(6) && agentRequest.getHttpProtocolType().equals(2) && agentRequest.getSmstype() == null) {
			throw new AgentException("请选择短信类型");
		}
		if (agentRequest.getPaytype() == 1
				&& (agentRequest.getUserPriceList() == null || agentRequest.getUserPriceList().size() <= 0)) {
			throw new AgentException("短信单价不能为空");
		}
		if (agentRequest.getNeedreport() > 3 || agentRequest.getNeedreport() < 0) {
			throw new AgentException("状态报告取值错误");
		}
		// 上行获取方式
		Set<Integer> sets = new HashSet<>();
		sets.add(0);
		sets.add(1);
		sets.add(3);
		if (agentRequest.getNeedmo() == null || !sets.contains(agentRequest.getNeedmo())) {
			throw new AgentException("是否需要上行取值错误!");
		}

		// Add by lpjLiu 2017-12-26 V 44_201712_运营中心_修订版本 验证状态报告地址或上行地址是否需要填写
		if (agentRequest.getSmsfrom().equals(SmsFrom.HTTPS.getValue()))
		{
			// 验证 状态报告地址
			if (agentRequest.getNeedreport().intValue() == 1 || agentRequest.getNeedreport().intValue() == 2)
			{
				if (StringUtils.isBlank(agentRequest.getDeliveryurl()))
				{
					throw new AgentException("状态报告回调地址长度必须介于 1 和 100 之间");
				}
			}

			// 验证 上行地址
			if (agentRequest.getNeedmo().intValue() == 1)
			{
				if (StringUtils.isBlank(agentRequest.getMourl()))
				{
					throw new AgentException("上行回调地址长度必须介于 1 和 100 之间");
				}
			}
		}

		if (agentRequest.getSmsfrom() == 4) {
			// SGIP协议接入客户提供的上行端口长度必须介于 1 和 11 之间
			if (com.ucpaas.sms.common.util.StringUtils.isBlank(agentRequest.getMoport()) || agentRequest.getMoport().length() > 11) {
				throw new AgentException("SGIP协议接入客户提供的上行端口长度必须介于 1 和 11 之间");
			}

			// SGIP协议接入客户提供的上行IP长度必须介于 1 和 100 之间
			if (com.ucpaas.sms.common.util.StringUtils.isBlank(agentRequest.getMoip()) || agentRequest.getMoip().length() > 100) {
				throw new AgentException("SGIP协议接入客户提供的上行IP长度必须介于 1 和 100 之间");
			}

			// 提供给SGIP协议接入客户的节点编码
			if (agentRequest.getNodeid() == null) {
				throw new AgentException("SGIP协议接入客户的节点编码不能为空");
			}

			// 提供给SGIP协议接入客户的节点编码
			if (agentRequest.getNodeid() < -100000000 || agentRequest.getNodeid() > 100000000) {
				throw new AgentException("SGIP协议接入客户的节点编码值太小或太大");
			}
		}
		if (agentRequest.getNeedextend() > 1 || agentRequest.getNeedextend() < 0) {
			throw new AgentException("自扩展位取值错误");
		}
		if (agentRequest.getNeedextend() == 1 && (agentRequest.getExtendSize() > 10 || agentRequest.getExtendSize() < 0)) {
			throw new AgentException("自扩展位取值范围为0至10");
		}
		//计费规则
		sets.clear();
		sets.add(0);
		sets.add(1);
		sets.add(2);
		if (agentRequest.getChargeRule() == null || !sets.contains(agentRequest.getChargeRule())) {
			throw new AgentException("计费规则不能为空，必须是 提交量/成功量/明确成功量");
		}
		//使用对象
		if(agentRequest.getAgentOwned()<0 || agentRequest.getAgentOwned()>1){
			throw new AgentException("使用对象选值错误!");
		}
	}
	private void insertAccount(AgentRequest agentRequest, Account account, String clientId) {

		//设置id
		String uuid=UUID.randomUUID().toString().replace("-", "");
		account.setId(uuid);
		//子扩展数量
		account.setExtendSize(agentRequest.getExtendSize());
		// 添加用户id(从公用序列中取,如果没有则生成后再取)
		account.setClientid(clientId);
		// 获取系统参数,默认的identify
		String identify = (String) commonService.getSysParams("DEFAULT_IDENTIFY").get("param_value");
		account.setIdentify(Integer.valueOf(identify));
		//ip白名单
		if(StringUtils.isBlank(agentRequest.getIp())){
			account.setIp("*");
		}else{
			account.setIp(agentRequest.getIp());
		}
		//密码(必填)
		account.setPassword(agentRequest.getPassword());
		//手机号(非必填)
        //account.setMobile(agentRequest.getMobile());
        //地址(非必填)
       // account.setAddress(agentRequest.getAddress());
        //邮箱(非必填)
       // account.setEmail(agentRequest.getEmail());

		//用户名称
		account.setName(agentRequest.getName());
		account.setStatus(1);
		account.setRealname(agentRequest.getName());
		// 是否超频计费 不需要
		account.setIsoverratecharge(0);
		// 是否支持签名对应签名端口，0：不支持，1：支持
		account.setSignextend(0);
		// 连接节点数
		account.setNodenum(1);
		// 状态报告/上行短信 最小拉取间隔(秒)
		account.setGetreportInterval(5);
		// 单个请求拉取状态报告的最大条数
		account.setGetreportMaxsize(30);
		// 设置客户级别
		account.setClientLevel(2);
		// 设置端口扩展类型和用户端口
		account.setExtendtype(6);
		account.setExtendport(this.getExtendportForOpenAcc(account.getExtendtype()));

		//连接速度100
		account.setAccessSpeed(100);
		account.setUpdatetime(new Date());
		//代理商id
		account.setAgentId(agentRequest.getAgentId());
		//用户类型 todo
		account.setClientType(2);
		//状态报告获取方式
		account.setNeedreport(agentRequest.getNeedreport());
		//上行获取方式
		account.setNeedmo(agentRequest.getNeedmo());
		//是否需要审核
		account.setNeedaudit(3);
		//创建时间
		account.setCreatetime(new Date());
		//状态报告回调地址
		account.setDeliveryurl(agentRequest.getDeliveryurl());
		//上行回调地址
		account.setMourl(agentRequest.getMourl());
		//付费方式
		account.setPaytype(agentRequest.getPaytype());
		//是否支持自扩展
		account.setNeedextend(agentRequest.getNeedextend());
		account.setBelongSale(agentRequest.getBelongSale());
		//使用对象
		account.setAgentOwned(agentRequest.getAgentOwned());
		//备注
		account.setRemarks(agentRequest.getRemark());
		//短信协议
		account.setSmsfrom(agentRequest.getSmsfrom());
		//http子协议
		account.setHttpProtocolType(agentRequest.getHttpProtocolType());
		//模版审核通知回调地址
		account.setNoticeurl(agentRequest.getNoticeurl());
		//短信类型，0：通知短信，4：验证码短信，5：营销短信（适用于标准协议）
		account.setSmstype(agentRequest.getSmstype());
		// SGIP协议接入客户提供的上行IP
		account.setMoip(agentRequest.getMoip());
		// SGIP协议接入客户提供的上行端口
		account.setMoport(agentRequest.getMoport());
		// 提供给SGIP协议接入客户的节点编码
		account.setNodeid(agentRequest.getNodeid());

		//保存到用户信息表t_sms_account
		int i = accountMapper.insert(account);
		if (i > 0) {
			logger.debug("插入数据到用户信息表t_sms_account成功");
		} else {
			logger.debug("插入数据到用户表t_sms_account失败");
			throw new AgentException("插入数据到用户表t_sms_account失败");
		}
	}

	@Override
	public R checkOemAgentProduct(List<JsmsOemAgentProduct> oemAgentProducts) {
		if (Collections3.isEmpty(oemAgentProducts)) {
			return R.error("请至少设置一种产品的折扣价或折扣率");
		}

		String msg = null;
		for (JsmsOemAgentProduct oemAgentProduct : oemAgentProducts) {
			if (oemAgentProduct.getAgentId() == null) {
				msg = "代理商Id是必须的";
				break;
			}
			if (oemAgentProduct.getProductId() == null) {
				msg = "产品Id是必须的";
				break;
			}

			if (oemAgentProduct.getGjSmsDiscount() == null && oemAgentProduct.getDiscountPrice() == null) {
				msg = "必须设置折扣率或折后价";
				break;
			}

			if (oemAgentProduct.getGjSmsDiscount() != null
					&& (oemAgentProduct.getGjSmsDiscount().compareTo(BigDecimal.ZERO) < 0
					|| oemAgentProduct.getGjSmsDiscount().compareTo(BigDecimal.ONE) > 0)) {
				msg = "国际短信的折扣率必须是0至1之间的值";
				break;
			}

			if (oemAgentProduct.getDiscountPrice() != null
					&& (oemAgentProduct.getDiscountPrice().compareTo(BigDecimal.ZERO) < 0
					|| oemAgentProduct.getDiscountPrice().compareTo(new BigDecimal(1000 * 100000)) > 0)) {
				msg = "折后价必须是0至1亿之间的值";
				break;
			}
		}

		if (msg != null) {
			return R.error(msg);
		}
		return null;
	}

	@Override
	@Transactional("message")
	public R saveOemAgentProduct(List<JsmsOemAgentProduct> oemAgentProducts, String pageUrl, String ip) {

		Calendar calendar = Calendar.getInstance();
		int count = 0;
		String msg = "";
		for (JsmsOemAgentProduct oemAgentProduct : oemAgentProducts) {
			JsmsOemAgentProduct queryResult = oemAgentProductService.getByAgentIdAndProductId(oemAgentProduct);
			if (queryResult == null) {
				oemAgentProduct.setCreateTime(calendar.getTime());
				oemAgentProduct.setUpdateTime(calendar.getTime());

				// 插入
				int deal = oemAgentProductService.insert(oemAgentProduct);
				count += deal;
				logService.add(LogType.add, LogEnum.账户信息管理.getValue(), oemAgentProduct.getAdminId(), pageUrl, ip,
						"账户信息管理-客户管理-折扣设置：添加产品折扣", JSON.toJSONString(oemAgentProduct));
			} else {
				oemAgentProduct.setId(queryResult.getId());
				oemAgentProduct.setCreateTime(null);
				oemAgentProduct.setUpdateTime(calendar.getTime());

				// 更新
				int deal = oemAgentProductService.updateSelective(oemAgentProduct);
				count += deal;
				logService.add(LogType.update, LogEnum.账户信息管理.getValue(), oemAgentProduct.getAdminId(), pageUrl, ip,
						"账户信息管理-客户管理-折扣设置：修改产品折扣", JSON.toJSONString(oemAgentProduct));
			}
		}

		if (count == oemAgentProducts.size()) {
			return R.ok("折扣设置保存成功");
		} else {
			throw new JsmsOemAgentProductException("折扣设置保存失败");
		}
	}
	// 获取扩展端口
	private String getExtendportForOpenAcc(Integer extendtype) {
		String extendport = null;

		Map<String, Object> getParams = new HashMap<>();
		getParams.put("extendtype", extendtype);

		Map<String, Object> data = accountMapper.getExtendportAssign(getParams);
		if (data == null || data.get("extendtype") == null) {
			throw new OperationException("没有可用的端口分配");
		}

		if (data.get("reusenumber") != null && !"".equals(data.get("reusenumber").toString())) {
			String reusenumberStr = data.get("reusenumber").toString();
			String[] reusenumberArry = reusenumberStr.split(",");

			if (reusenumberArry.length > 1) {
				String newReusenumberStr = "";
				String startStr = reusenumberArry[0];

				newReusenumberStr = newReusenumberStr + startStr;
				for (int i = 1; i < reusenumberArry.length - 1; i++) {
					newReusenumberStr = newReusenumberStr + "," + reusenumberArry[i];
				}

				// 更新重用端口
				Map<String, Object> updateParams = new HashMap<>();
				updateParams.put("reusenumber", newReusenumberStr);
				updateParams.put("extendtype", extendtype);

				updateParams.put("oldReusenumber", reusenumberStr); // 判断值是否修改(乐观锁)

				int i = accountMapper.updateExtendportAssign(updateParams);
				if (i == 0) {
					throw new OperationException("更新重用端口失败");
				}
			}

			if (reusenumberArry.length == 1) {
				Map<String, Object> updateParams = new HashMap<>();
				updateParams.put("reusenumber", "");
				updateParams.put("extendtype", extendtype);

				updateParams.put("oldReusenumber", reusenumberStr); // 判断值是否修改(乐观锁)

				int i = accountMapper.updateExtendportAssign(updateParams);
				if (i == 0) {
					throw new OperationException("更新重用端口失败");
				}
			}
			extendport = reusenumberArry[reusenumberArry.length - 1];

		} else {
			int endnumber = Integer.valueOf(data.get("endnumber").toString());
			int currentnumber = Integer.valueOf(data.get("currentnumber").toString());

			int newCurrentnumber = currentnumber + 1;
			Map<String, Object> updateParams = new HashMap<>();
			updateParams.put("extendtype", extendtype);
			updateParams.put("currentnumber", newCurrentnumber);
			updateParams.put("oldCurrentnumber", currentnumber);

			if (newCurrentnumber > endnumber) {
				updateParams.put("status", 1); // 如果当前端口已经使用完，必须修改状态为禁用
				updateParams.put("oldStatus", data.get("status"));
			}

			int i = accountMapper.updateExtendportAssign(updateParams);
			if (i == 0) {
				throw new OperationException("更新端口分配范围表失败");
			}

			extendport = currentnumber + "";
		}

		return extendport;
	}


	@Override
	public Set<Integer> findOEMAgentIdByBelongSales(DataAuthorityCondition dataAuthorityCondition){
		Set<Integer> agentIds = new HashSet<>();
		List<AgentInfo> agentInfoList = agentInfoMapper.findAllListByBelongSales(dataAuthorityCondition);
		for (AgentInfo agentInfo : agentInfoList) {
			if (AgentType.OEM代理商.getValue().equals(agentInfo.getAgentType())) {
				agentIds.add(agentInfo.getAgentId());
			}
		}
		return agentIds;
	}
	/**
	 * @Description: 根据权限获取所属销售下或所属销售为空的所有代理商id
	 * @Author: xiaoqingwen
	 * @Date: 2018/02/01 - 09:33
	 * @param dataAuthorityCondition 数据权限参数
	 *此接口是为了修改线上财务管理-客户财务-余额账单根据回退条数查找只能查找到OEM代理商的问题  v5.19.0
	 */
//	@Override
//	public Set<Integer> findAgentIdByBelongSales(DataAuthorityCondition dataAuthorityCondition) {
//		Set<Integer> agentIds = new HashSet<>();
//		List<AgentInfo> agentInfoList = agentInfoMapper.findAllListByBelongSales(dataAuthorityCondition);
//		for (AgentInfo agentInfo : agentInfoList) {
//			agentIds.add(agentInfo.getAgentId());
//		}
//		return agentIds;
//	}

	/**
	 * @Description: 根据权限获取所属销售下或所属销售为空的所有代理商id
	 * @Author: xiaoqingwen
	 * @Date: 2018/02/01 - 10:11
	 * @param dataAuthorityCondition 数据权限参数
	 *此接口是为了修改线上财务管理-客户财务-余额账单根据回退条数查找只能查找到OEM代理商的问题  v5.19.5(现在版本)
	 */
	@Override
	public Set<Integer> findAgentIdByBelongSales(DataAuthorityCondition dataAuthorityCondition) {
		Set<Integer> agentIds = new HashSet<>();
		List<AgentInfo> agentInfoList = agentInfoMapper.findAllListByBelongSales(dataAuthorityCondition);
		for (AgentInfo agentInfo : agentInfoList) {
			agentIds.add(agentInfo.getAgentId());
		}
		return agentIds;
	}

}
