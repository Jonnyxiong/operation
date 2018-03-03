package com.ucpaas.sms.service.account;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.common.enums.*;
import com.jsmsframework.common.service.JsmsLogService;
import com.jsmsframework.common.util.JsonUtil;
import com.jsmsframework.finance.dto.UserPriceLogDTO;
import com.jsmsframework.finance.entity.JsmsClientBalanceAlarm;
import com.jsmsframework.finance.entity.JsmsUserPriceLog;
import com.jsmsframework.finance.exception.JsmsClientBalanceAlarmException;
import com.jsmsframework.finance.mapper.JsmsUserPriceLogMapper;
import com.jsmsframework.finance.service.JsmsClientBalanceAlarmService;
import com.jsmsframework.order.entity.JsmsClientOrder;
import com.jsmsframework.order.entity.JsmsOemClientPool;
import com.jsmsframework.order.enums.GroupParamsType;
import com.jsmsframework.order.service.JsmsAccountFinanceService;
import com.jsmsframework.order.service.JsmsClientOrderService;
import com.jsmsframework.order.service.JsmsOemClientPoolService;
import com.jsmsframework.sale.credit.constant.SysConstant;
import com.jsmsframework.sale.credit.service.JsmsSaleCreditService;
import com.jsmsframework.user.entity.*;
import com.jsmsframework.user.exception.JsmsUserPropertyLogException;
import com.jsmsframework.user.mapper.JsmsOemDataConfigMapper;
import com.jsmsframework.user.mapper.JsmsUserPropertyLogMapper;
import com.jsmsframework.user.service.*;
import com.ucpaas.sms.common.entity.R;
import com.ucpaas.sms.common.util.*;
import com.ucpaas.sms.constant.Constant;
import com.ucpaas.sms.constant.LogConstant;
import com.ucpaas.sms.dao.MessageMasterDao;
import com.ucpaas.sms.dto.JsmsOemClientPoolDTO;
import com.ucpaas.sms.entity.message.Account;
import com.ucpaas.sms.entity.message.AgentInfo;
import com.ucpaas.sms.entity.po.AccountPo;
import com.ucpaas.sms.entity.po.ClientBalanceAlarmPo;
import com.ucpaas.sms.enums.AccountEnums;
import com.ucpaas.sms.entity.po.DataAuthorityCondition;
import com.ucpaas.sms.enums.LogEnum;
import com.ucpaas.sms.enums.SMSType;
import com.ucpaas.sms.exception.AgentException;
import com.ucpaas.sms.exception.OperationException;
import com.ucpaas.sms.mapper.message.AccountMapper;
import com.ucpaas.sms.mapper.message.AgentInfoMapper;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.model.SmsOauthpic;
import com.ucpaas.sms.service.AgentInfoService;
import com.ucpaas.sms.service.audit.AgentAuditService;
import com.ucpaas.sms.service.audit.CustomerAuditService;
import com.ucpaas.sms.service.common.CommonSeqService;
import com.ucpaas.sms.service.common.CommonService;
import com.ucpaas.sms.service.common.LogService;
import com.ucpaas.sms.util.AgentUtils;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.JacksonUtil;
import com.ucpaas.sms.util.PageConvertUtil;
import com.ucpaas.sms.util.beans.BeanUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class AccountServiceImpl implements AccountService {
	private static final Logger logger = LoggerFactory.getLogger(AccountServiceImpl.class);

	@Autowired
	private MessageMasterDao masterDao;
	@Autowired
	private LogService logService;

	@Autowired
	private AccountMapper accountMapper;

	@Autowired
	private CommonService commonService;

	@Autowired
	private CommonSeqService commonSeqService;

	@Autowired
	private AgentInfoMapper agentInfoMapper;

	@Autowired
	private JsmsUserPriceLogMapper userPriceLogMapper;

	@Autowired
	private ApplyRecordService applyRecordService;
	@Autowired
	private CustomerAuditService customerAuditService;

	@Autowired
	private JsmsAgentInfoService jsmsAgentInfoService;

	@Autowired
	private JsmsUserService jsmsUserService;

	@Autowired
	private JsmsAccountService jsmsAccountService;

	@Autowired
	private JsmsOemDataConfigService jsmsOemDataConfigService;

	@Autowired
	private JsmsClientOrderService jsmsClientOrderService;

	@Autowired
	private JsmsClientInfoExtService jsmsClientInfoExtService;

	@Autowired
	private JsmsLogService jsmsLogService;
	@Autowired
	private AgentAuditService agentAuditService;

	@Autowired
	private JsmsUserPropertyLogService jsmsUserPropertyLogService;

	@Autowired
	private JsmsOemClientPoolService jsmsOemClientPoolService;

	@Autowired
	private JsmsSaleCreditService jsmsSaleCreditService;

	@Autowired
	private JsmsClientBalanceAlarmService jsmsClientBalanceAlarmService;

	@Autowired
	private AgentInfoService agentInfoService;
	@Autowired
	private JsmsAccountFinanceService jsmsAccountFinanceService;
	@Autowired
	private AccountService accountService;
	@Autowired
	private JsmsOemDataConfigMapper jsmsOemDataConfigMapper;
	@Autowired
	private JsmsUserPropertyLogMapper jsmsUserPropertyLogMapper;


	@Override
	public PageContainer query(Map<String, String> params) {
		Map<String, Object> objectMap = Maps.newHashMap();
		objectMap.putAll(params);

		// 构造数据权限查询条件
		Long userId = params.get("userId") == null ? null : Long.parseLong(params.get("userId").toString());
		if (userId != null) {
			objectMap.put("dataAuthorityCondition", AgentUtils.getDataAuthorityCondition(userId, true, false));
		}

		return masterDao.getSearchPage("agentManage.queryAgentInfo", "agentManage.queryAgentInfoCount", objectMap);
	}

	@Override
	public List<Map<String, Object>> queryAll(Map<String, String> params) {
		Map<String, Object> objectMap = Maps.newHashMap();
		objectMap.putAll(params);

		// 构造数据权限查询条件
		Long userId = params.get("userId") == null ? null : Long.parseLong(params.get("userId").toString());
		if (userId != null) {
			objectMap.put("dataAuthorityCondition", AgentUtils.getDataAuthorityCondition(userId, true, false));
		}

		return masterDao.getSearchList("agentManage.queryAllAgentInfo", objectMap);
	}

	private class TempCondition{
		List<String> clientids;
		List<TempClientInfo> clientLists;
	}

	private class TempClientInfo{
		String clientid;
		JsmsClientInfoExt clientInfoExt;
		JsmsUserPropertyLog userPropertyLog;
	}

	private TempCondition initClientConditions(Map<String, Object> objectMap)
	{
		// 返回结果
		TempCondition result = new TempCondition();

		// 构造出需要查询的客户ID列表
		result.clientids = new ArrayList<>();

		if (objectMap == null)
		{
			// 客户ID放入-1，若查询将其带入条件后，那么查询不到一个客户
			result.clientids.add("-1");

			if (result.clientids != null)
			{
				objectMap.put("clientids", result.clientids);
			}

			return result;
		}

		// 构造出查询后需要填充的数据
		result.clientLists = new ArrayList<>();

		Object obj = null;

		// 1. 查询扩展信息
		JsmsClientInfoExt clientInfoExt = new JsmsClientInfoExt();

		// 若客户星级不为空，查询扩展信息将带入客户星级
		obj = objectMap.get("starLevel");
		if (obj != null && StringUtils.isNotBlank(obj.toString()))
		{
			clientInfoExt.setStarLevel(Integer.parseInt(obj.toString()));
		}

		// 若扩展值不为空，查询扩展信息将带入扩展值
		obj = objectMap.get("extValue");
		if (obj != null && StringUtils.isNotBlank(obj.toString()))
		{
			clientInfoExt.setExtValue(Integer.parseInt(obj.toString()));
		}

		List<JsmsClientInfoExt> clientInfoExts = jsmsClientInfoExtService.findList(clientInfoExt);

		// 若扩展信息为空，表示没有查询到一个客户，因为客户与客户扩展信息是一一对应的
		if (Collections3.isEmpty(clientInfoExts))
		{
			// 客户ID放入-1，若查询将其带入条件后，那么查询不到一个客户
			result.clientids.add("-1");
			if (result.clientids != null)
			{
				objectMap.put("clientids", result.clientids);
			}

			return result;
		}

		// 4. 查询计费规则
		obj = objectMap.get("value");
		String value = obj == null ? null : obj.toString();
		List<JsmsUserPropertyLog> userPropertyLogs = jsmsUserPropertyLogService.findLastEffectDateList(Constant.CHARGE_RULE, value);

		// 循环处理客户扩展信息列表
		for (JsmsClientInfoExt infoExt : clientInfoExts) {
			// 若查询计费规则为空，直接添加此ID到查询条件
			if (StringUtils.isBlank(value))
			{
				// 放入条件
				result.clientids.add(infoExt.getClientid());

				// 放入客户的相关信息，用于查询后填充字段的值
				TempClientInfo tempClientInfo = new TempClientInfo();
				tempClientInfo.clientid = infoExt.getClientid();
				tempClientInfo.clientInfoExt = infoExt;

				for (JsmsUserPropertyLog userPropertyLog : userPropertyLogs) {
					if (userPropertyLog.getClientid().equals(infoExt.getClientid()))
					{
						tempClientInfo.userPropertyLog = userPropertyLog;
						break;
					}
				}

				result.clientLists.add(tempClientInfo);

				continue;
			}

			// 查询是否存在用户属性配置
			JsmsUserPropertyLog userProperty = null;
			for (JsmsUserPropertyLog userPropertyLog : userPropertyLogs) {
				if (userPropertyLog.getClientid().equals(infoExt.getClientid()))
				{
					userProperty = userPropertyLog;
					break;
				}
			}

			if (userProperty == null)
			{
				continue;
			}

			// 放入条件
			result.clientids.add(infoExt.getClientid());

			// 放入客户的相关信息，用于查询后填充字段的值
			TempClientInfo tempClientInfo = new TempClientInfo();
			tempClientInfo.clientid = infoExt.getClientid();
			tempClientInfo.clientInfoExt = infoExt;
			tempClientInfo.userPropertyLog = userProperty;
			result.clientLists.add(tempClientInfo);
		}

		if (result.clientids != null)
		{
			objectMap.put("clientids", result.clientids);
		}

		return result;
	}

	private void dealClientInfoResult(List<Map<String, Object>> data, TempCondition tempCondition){
		if (data == null || data.isEmpty() || tempCondition == null)
		{
			return;
		}

		for (Map<String, Object> item : data) {
			// 获取客户Id
			String clientid = item.get("clientid") == null ? null : item.get("clientid").toString();
			if (StringUtils.isBlank(clientid))
			{
				continue;
			}

			// 获取扩展信息
			if (Collections3.isEmpty(tempCondition.clientLists))
			{
				break;
			}

			TempClientInfo tempClientInfo = null;
			for (TempClientInfo temp : tempCondition.clientLists) {
				if (temp.clientid.equals(clientid))
				{
					tempClientInfo = temp;
					break;
				}
			}

			// 未获取到客户的扩展信息，跳出，不再处理
			if (tempClientInfo == null || tempClientInfo.clientInfoExt == null)
			{
				break;
			}

			item.put("ext_value", tempClientInfo.clientInfoExt.getExtValue());
			item.put("extValueStr", SupportEnum.getDescByValue(tempClientInfo.clientInfoExt.getExtValue()));

			item.put("star_level", tempClientInfo.clientInfoExt.getStarLevel());
			item.put("starLevelStr", tempClientInfo.clientInfoExt.getStarLevel()+"星");

			// 付费类型
			String paytype = item.get("paytype") == null  ? PayType.预付费.toString() : item.get("paytype").toString();
			item.put("paytypeDesc", PayType.getDescByValue(Integer.parseInt(paytype)).replace("费", ""));

			// 状态描述
			String status = item.get("status") == null  ? AccountEnums.Status.未激活.toString() : item.get("status").toString();
			item.put("statusName", AccountEnums.Status.getDescByValue(Integer.parseInt(status)));

			// 获取代理商类型
			Integer agentType = null;
			try {
				agentType = item.get("agent_type")  == null ? null : Integer.parseInt(item.get("agent_type").toString());
			}catch (Exception e)
			{
				logger.error("查询子客户信息时，代理商类型转为int失败{}", e);
			}

			if (agentType == null)
			{
				item.put("agent_type_name", "");
			}
			else {
				item.put("agent_type_name", AgentType.getDescByValue(agentType));
			}

			// 设置认证状态
			String oauth_status = item.get("oauth_status") == null ? "" : item.get("oauth_status").toString();
			if (StringUtils.isBlank(oauth_status))
			{
				item.put("oauth_status", OauthStatusEnum.待认证.getDesc());
			} else {
				if (oauth_status.equals(OauthStatusEnum.待认证.getValue().toString()))
				{
					item.put("oauth_status", OauthStatusEnum.待认证.getDesc());
				}
				else if (oauth_status.equals(OauthStatusEnum.证件已认证.getValue().toString()))
				{
					item.put("oauth_status", "已认证");
				}
				else if (oauth_status.equals(OauthStatusEnum.认证不通过.getValue().toString()))
				{
					item.put("oauth_status", OauthStatusEnum.认证不通过.getDesc());
				}
				else {
					item.put("oauth_status", OauthStatusEnum.待认证.getDesc());
				}
			}

			// 设置计费规则及描述
			JsmsUserPropertyLog userPropertyLog = tempClientInfo.userPropertyLog;
			if (userPropertyLog == null)
			{
				item.put("value", ChargeRule.成功量计费.getValue());
				item.put("valueStr", ChargeRule.成功量计费.getDesc().replace("计费", ""));
			} else {
				item.put("value", userPropertyLog.getValue());
				item.put("valueStr", ChargeRule.getDescByValue(Integer.parseInt(userPropertyLog.getValue())).replace("计费", ""));
			}
		}
	}

	@Override
	public PageContainer queryClientInfo(Map<String, String> params) {

		Map<String, Object> objectMap = Maps.newHashMap();
		objectMap.putAll(params);
		List<JsmsClientOrder> remainQuantity = new ArrayList<>();
		List<JsmsOemClientPool> remainOemQuantity = new ArrayList<>();
		JsmsOemClientPool jsmsOemClientPool = new JsmsOemClientPool();

		// 构造数据权限查询条件
		Long userId = params.get("userId") == null ? null : Long.parseLong(params.get("userId").toString());
		if (userId != null) {
			objectMap.put("dataAuthorityCondition", AgentUtils.getDataAuthorityCondition(userId, true, false));
		}

		// Mod by lpjLiu 20171221 优化查询
		// PageContainer<Map<String, Object>> pageContainer = masterDao.getSearchPage("clientManage.queryClientInfo",
		//		"clientManage.queryClientInfoCount", objectMap);

		// 初始化条件
		TempCondition tempCondition = initClientConditions(objectMap);

		// 查询结果
		PageContainer<Map<String, Object>> pageContainer = masterDao.getSearchPage("clientManage.queryClientInfoNew",
				"clientManage.queryClientInfoCountNew", objectMap);

		// 构造结果
		dealClientInfoResult(pageContainer.getList(), tempCondition);

		ArrayList<GroupParamsType> groupParams = new ArrayList();
		groupParams.add(GroupParamsType.产品类型);
		/*
		 * groupParams.add(GroupParamsType.运营商);
		 * groupParams.add(GroupParamsType.单价);
		 * groupParams.add(GroupParamsType.到期时间);
		 */
		for (Map map : pageContainer.getList()) {
			String clientid = (String) map.get("clientid");
			if (("OEM代理商").equals(String.valueOf(map.get("agent_type_name")))) {
				jsmsOemClientPool.setClientId(clientid);
				remainOemQuantity = jsmsOemClientPoolService.getListByClientPoolInfo(jsmsOemClientPool);
				for (JsmsOemClientPool oemPool : remainOemQuantity) {
					// 产品类型，0：行业，1：营销，2：国际，3：验证码，4：通知，7：USSD，8：闪信，9：挂机短信
					// 其中0、1、3、4为普通短信，2为国际短信
					if (ProductType.国际.getValue().equals(oemPool.getProductType())) {
						if (map.get("interType") == null) {
							map.put("interType", oemPool.getRemainAmount());
						} else {
							map.put("interType", oemPool.getRemainAmount().add((BigDecimal) map.get("interType")));
						}
					} else if (oemPool.getProductType() != null && oemPool.getProductType() < 5) { //
						if (map.get("normalType") == null) {
							map.put("normalType", oemPool.getRemainNumber());
						} else {
							map.put("normalType", oemPool.getRemainNumber()
									+ Integer.parseInt(String.valueOf((map.get("normalType")))));
						}
					}
				}
			} else {
				remainQuantity = jsmsClientOrderService.getOrderRemainQuantity(clientid, groupParams, null);
				for (JsmsClientOrder clientOrder : remainQuantity) {
					// 产品类型，0：行业，1：营销，2：国际，3：验证码，4：通知，7：USSD，8：闪信，9：挂机短信
					// 其中0、1、3、4为普通短信，2为国际短信
					if (ProductType.国际.getValue().equals(clientOrder.getProductType())) {
						if (map.get("interType") == null) {
							map.put("interType", clientOrder.getRemainQuantity());
						} else {
							map.put("interType",
									clientOrder.getRemainQuantity().add((BigDecimal) map.get("interType")));
						}
					} else if (clientOrder.getProductType() != null && clientOrder.getProductType() < 5) { //
						if (map.get("normalType") == null) {
							map.put("normalType", clientOrder.getRemainQuantity());
						} else {
							map.put("normalType",
									clientOrder.getRemainQuantity().add((BigDecimal) map.get("normalType")));
						}
					}
				}
			}

		}
		return pageContainer;
	}

	@Override
	public PageContainer clientOrderRemain(Map<String, String> params) {

		JsmsPage jsmsPage = PageConvertUtil.paramToPage(params);
		JsmsOemClientPool jsmsOemClientPool = new JsmsOemClientPool();
		List<JsmsOemClientPool> remainOemQuantity = new ArrayList<>();
		List<JsmsOemClientPoolDTO> remainOemQuantityDto = new ArrayList<>();
		ArrayList<GroupParamsType> groupParams = new ArrayList();
		groupParams.add(GroupParamsType.产品类型);
		groupParams.add(GroupParamsType.运营商);
		groupParams.add(GroupParamsType.单价);
		groupParams.add(GroupParamsType.到期时间);
		String clientid = (String) params.get("clientid");
		Set<Integer> productTypes = new HashSet<>();
		if ("2".equals(params.get("productType"))) {
			productTypes.add(2);
			jsmsOemClientPool.setProductType(2);
		} else {
			productTypes.add(0);
			productTypes.add(1);
			productTypes.add(3);
			productTypes.add(4);
		}
		if ("Br".equals(jsmsPage.getParams().get("agent_type"))) {
//			jsmsClientOrderService.queryRemainQuantityList(jsmsPage, clientid, groupParams, productTypes);
			jsmsClientOrderService.queryRemainQuantity(jsmsPage, clientid, groupParams, productTypes);
		} else if ("OEM".equals(jsmsPage.getParams().get("agent_type"))) {
			jsmsOemClientPool.setClientId(clientid);
//			remainOemQuantity = jsmsOemClientPoolService.getListByClientPoolInfo(jsmsOemClientPool);
			remainOemQuantity = jsmsOemClientPoolService.queryRemainQuantityClientPoolInfo(jsmsOemClientPool);
			if (remainOemQuantity != null && remainOemQuantity.size() != 0) {
				for (int i = 0; i < remainOemQuantity.size(); i++) {
					JsmsOemClientPoolDTO jsmsOemClientPoolDTO = new JsmsOemClientPoolDTO();
					if (jsmsOemClientPool.getProductType() == null) {
						if (!remainOemQuantity.get(i).getProductType().equals(2)) {
							BeanUtils.copyProperties(remainOemQuantity.get(i), jsmsOemClientPoolDTO);
							if (remainOemQuantity.get(i).getDueTime() != null) {
								jsmsOemClientPoolDTO.setEndTime(remainOemQuantity.get(i).getDueTime());
							}
							if (remainOemQuantity.get(i).getRemainNumber() != null) {
								jsmsOemClientPoolDTO
										.setRemainQuantity((double) (remainOemQuantity.get(i).getRemainNumber()));
							}
							remainOemQuantityDto.add(jsmsOemClientPoolDTO);
						}
					}
					if (jsmsOemClientPool.getProductType() != null && jsmsOemClientPool.getProductType().equals(2)) {
						BeanUtils.copyProperties(remainOemQuantity.get(i), jsmsOemClientPoolDTO);
						if (remainOemQuantity.get(i).getDueTime() != null) {
							jsmsOemClientPoolDTO.setEndTime(remainOemQuantity.get(i).getDueTime());
						}
						if (remainOemQuantity.get(i).getRemainAmount() != null
								&& remainOemQuantity.get(i).getRemainAmount().compareTo(BigDecimal.ZERO) == 1) {
							jsmsOemClientPoolDTO.setRemainQuantity(
									remainOemQuantity.get(i).getRemainAmount().setScale(4).doubleValue());
						}
						remainOemQuantityDto.add(jsmsOemClientPoolDTO);
					}
				}
			}
			jsmsPage.setData(remainOemQuantityDto);
		}

		return PageConvertUtil.pageToContainer(jsmsPage);
	}

	@Override
	public List<Map<String, Object>> queryAllClientInfo(Map<String, String> params) {
		Map<String, Object> objectMap = Maps.newHashMap();
		objectMap.putAll(params);

		// 构造数据权限查询条件
		Long userId = params.get("userId") == null ? null : Long.parseLong(params.get("userId").toString());
		if (userId != null) {
			objectMap.put("dataAuthorityCondition", AgentUtils.getDataAuthorityCondition(userId, true, false));
		}

		// 初始化条件
		TempCondition tempCondition = initClientConditions(objectMap);

		//List<Map<String, Object>> data = masterDao.getSearchList("clientManage.queryAllClientInfo", objectMap);
		List<Map<String, Object>> data = masterDao.getSearchList("clientManage.queryAllClientInfoNew", objectMap);

		// 构造结果
		dealClientInfoResult(data, tempCondition);

		ArrayList<GroupParamsType> groupParams = new ArrayList();
		groupParams.add(GroupParamsType.产品类型);
		/*
		 * groupParams.add(GroupParamsType.运营商);
		 * groupParams.add(GroupParamsType.单价);
		 * groupParams.add(GroupParamsType.到期时间);
		 */
		for (Map map : data) {
			String clientid = (String) map.get("clientid");
			if (("OEM代理商").equals(String.valueOf(map.get("agent_type_name")))) {
				JsmsOemClientPool jsmsOemClientPool = new JsmsOemClientPool();
				jsmsOemClientPool.setClientId(clientid);
				List<JsmsOemClientPool> remainOemQuantity = jsmsOemClientPoolService.getListByClientPoolInfo(jsmsOemClientPool);
				for (JsmsOemClientPool oemPool : remainOemQuantity) {
					// 产品类型，0：行业，1：营销，2：国际，3：验证码，4：通知，7：USSD，8：闪信，9：挂机短信
					// 其中0、1、3、4为普通短信，2为国际短信
					if (ProductType.国际.getValue().equals(oemPool.getProductType())) {
						if (map.get("interType") == null) {
							map.put("interType", oemPool.getRemainAmount());
						} else {
							map.put("interType", oemPool.getRemainAmount().add((BigDecimal) map.get("interType")));
						}
					} else if (oemPool.getProductType() != null && oemPool.getProductType() < 5) { //
						if (map.get("normalType") == null) {
							map.put("normalType", oemPool.getRemainNumber());
						} else {
							map.put("normalType", oemPool.getRemainNumber()
									+ Integer.parseInt(String.valueOf((map.get("normalType")))));
						}
					}
				}
				if (map.get("interType") == null) {
					map.put("interType", "0.0000");
				} else {
					BigDecimal interType = (BigDecimal) map.get("interType");
					map.put("interType", interType.setScale(4, BigDecimal.ROUND_DOWN));
				}
				if (map.get("normalType") == null) {
					map.put("normalType", "0");
				} else {
					BigDecimal normalType = new BigDecimal(String.valueOf(map.get("normalType")));
					map.put("normalType", normalType.setScale(0, BigDecimal.ROUND_DOWN));
				}
			} else {
				List<JsmsClientOrder> remainQuantity = jsmsClientOrderService.getOrderRemainQuantity(clientid, groupParams, null);
				for (JsmsClientOrder clientOrder : remainQuantity) {
					// 产品类型，0：行业，1：营销，2：国际，3：验证码，4：通知，7：USSD，8：闪信，9：挂机短信
					// 其中0、1、3、4为普通短信，2为国际短信
					if (ProductType.国际.getValue().equals(clientOrder.getProductType())) {
						if (map.get("interType") == null) {
							map.put("interType", clientOrder.getRemainQuantity());
						} else {
							map.put("interType",
									clientOrder.getRemainQuantity().add((BigDecimal) map.get("interType")));
						}
					} else if (clientOrder.getProductType() != null && clientOrder.getProductType() < 5) { //
						if (map.get("normalType") == null) {
							map.put("normalType", clientOrder.getRemainQuantity());
						} else {
							map.put("normalType",
									clientOrder.getRemainQuantity().add((BigDecimal) map.get("normalType")));
						}
					}
				}
				if (map.get("interType") == null) {
					map.put("interType", "0.0000");
				} else {
					BigDecimal interType = (BigDecimal) map.get("interType");
					map.put("interType", interType.setScale(4, BigDecimal.ROUND_DOWN));
				}
				if (map.get("normalType") == null) {
					map.put("normalType", "0");
				} else {
					BigDecimal normalType = new BigDecimal(String.valueOf(map.get("normalType")));
					map.put("normalType", normalType.setScale(0, BigDecimal.ROUND_DOWN));
				}
			}

		}
		return data;
	}

	@Override
	public Map<String, Object> queryAgentDetailInfo(int agentId) {
		Map<String, Object> data = masterDao.getOneInfo("agentManage.queryAgentDetailInfo", agentId);
		int clientNum = masterDao.getOneInfo("agentManage.getClientNumByAgentId", agentId);
		data.put("convertOEM", clientNum < 1 ? true : false);
		int agentType = (int) data.get("agent_type"); // 代理商类型,1:销售代理商,2:品牌代理商,3:资源合作商,4:代理商和资源合作,5:OEM代理商
		int oemDataNum = 0;
		if (agentType == 5) {
			oemDataNum = masterDao.getOneInfo("agentManage.checkDataExist", agentId);
		}
		data.put("dataExist", oemDataNum > 0 ? true : false);
		return data;
	}

	@Override
	public Map<String, Object> queryClientDetailInfo(String clientId) {
		return masterDao.getOneInfo("clientManage.queryClientDetailInfo", clientId);
	}

	@Transactional("message_old")
	@Override
	public Map<String, Object> updateStatus(Map<String, String> params) {
		Map<String, Object> data = new HashMap<String, Object>();

		int i = masterDao.update("agentManage.updateStatus", params);
		Long userId = Long.valueOf(params.get("userId"));
		String pageUrl = params.get("pageUrl");
		String ip = params.get("ip");
		logService.add(LogConstant.LogType.update, LogEnum.账户信息管理.getValue(), userId, pageUrl, ip,
				"账户信息管理-客户管理：修改账户状态", params.get("agentId"), params.get("status"));
		if (i > 0) {
			data.put("result", "success");
			data.put("msg", "操作成功");
		} else {
			data.put("result", "fail");
			data.put("msg", "操作失败");
		}
		return data;
	}

	/**
	 * 修改代理商类型
	 */
	@Transactional("message_old")
	@Override
	public Map<String, Object> updateAgentType(Map<String, String> params) {
		Map<String, Object> data = new HashMap<String, Object>();
		int i = masterDao.update("agentManage.updateAgentType", params);
		if (params.get("agentType") != null && "5".equals(params.get("agentType"))) { // 5:OEM代理商
			params.put("webId", "4");
			int j = masterDao.update("agentManage.updateAgentWebId", params);
			String roleId = masterDao.getOneInfo("agentManage.getOEMRoleId", params);
			if (roleId != null && !"".equals(roleId)) {
				params.put("roleId", roleId);
				int k = masterDao.update("agentManage.updateAgentRole", params);
				if (i > 0 && j > 0 && k > 0) {
					data.put("result", "success");
					data.put("msg", "修改成功");
				} else if (i < 1 && j < 0 && k < 0) {
					data.put("result", "fail");
					data.put("msg", "修改失败");
				} else {
					throw new RuntimeException("修改代理商类型错误:(1)代理商类型、代理商web_id和OEM角色没有同时更新");
				}
			} else {
				data.put("result", "fail");
				data.put("msg", "没有OEM代理商角色,请联系管理员添加角色");
			}
			return data;
		}

		if (i > 0) {
			data.put("result", "success");
			data.put("msg", "修改成功");
		} else {
			data.put("result", "fail");
			data.put("msg", "修改失败");
		}
		return data;
	}

	@Transactional("message_old")
	@SuppressWarnings("static-access")
	@Override
	public Map<String, Object> updateClientStatus(String clientId, int status) {
		Map<String, Object> data = new HashMap<String, Object>();
		String msg;
		LogConstant.LogType logType = null;
		switch (status) {
		case 1:
			msg = "解冻";
			logType = logType.update;
			break;
		case 5:
			msg = "冻结";
			logType = logType.update;
			break;
		case 6:
			msg = "注销";
			logType = logType.update;
			break;
		default:
			data.put("result", "fail");
			data.put("msg", "状态不正确，操作失败");
			return data;
		}

		Map<String, Object> params = new HashMap<String, Object>();
		params.put("clientId", clientId);
		params.put("status", status);
		if (status == 6) {
			if (!releaseExtendport(params)) {
				data.put("result", "fail");
				data.put("msg", "请求超时，请稍后再试");
				return data;
			}
		}
		int i = masterDao.update("clientManage.updateStatus", params);
		if (i > 0) {
			data.put("result", "success");
			data.put("msg", msg + "成功");
		} else {
			data.put("result", "fail");
			data.put("msg", "任务不存在，" + msg + "失败");
			if (status == 6) {
				logger.debug("注销失败！");
				throw new RuntimeException("释放端口成功, 注销失败");
			}
		}
		return data;
	}

	private boolean releaseExtendport(Map<String, Object> params) {
		Map<String, Object> clientInfo = masterDao.getOneInfo("clientManage.getClientInfo", params);
		if (clientInfo == null || clientInfo.get("extendport") == null) {
			return true;
		}
		return masterDao.update("clientManage.releaseExtendport", clientInfo) > 0 ? true : false;
	}

	/**
	 * @Description: 获取OEM资料配置
	 * @author: Niu.T
	 * @date: 2016年11月21日 下午8:31:54
	 * @param params
	 * @return Map<String,Object>
	 */
	@Override
	public Map<String, Object> getOEMdataConfig(Map<String, String> params) {
		Map<String, Object> data = masterDao.getOneInfo("agentManage.queryAgentDetailInfo", params);
		String agentId = params.get("agentId");
		int oemDataNum = masterDao.getOneInfo("agentManage.checkDataExist", agentId);
		// data.put("dataExist", oemDataNum > 0 ? true : false);
		if (oemDataNum > 0) {
			Map<String, Object> oemData = masterDao.getOneInfo("agentManage.getOEMDataConfig", params);
			List<String> columns = new ArrayList<String>();
			columns.add("tab_logo_url");
			columns.add("logo_url");
			columns.add("company_logo_url");
			columns.add("api_document_url");
			columns.add("FAQ_document_url");
			data.putAll(encodeColumn(oemData, columns));
			data.put("edit", "update"); // 修改

			// 添加短信测试数量标识
			if (oemData.get("test_product_id") == null || oemData.get("test_sms_number") == null
					|| "0".equals(oemData.get("test_sms_number").toString())) {
				data.put("isHavetestSmsNum", "no");
			} else {
				data.put("isHavetestSmsNum", "yes");

				// 生成产品名字
				String product_id = oemData.get("test_product_id").toString();
				Map<String, Object> productIdParams = new HashMap<>();
				productIdParams.put("product_id", product_id);
				Map<String, Object> productInfoMap = masterDao.getOneInfo("customerAudit.getOemProductInfoByProductId",
						productIdParams);
				data.put("product_name", productInfoMap.get("product_name").toString());
			}

		} else {
			data.put("domain_name", "oemclient.sms.ucpaas.com");
			data.put("copyright_text", "2014-2018 深圳市云之讯网络技术有限公司 粤ICP备14046848号");
			data.put("tab_logo_url", null);
			data.put("logo_url", null);
			data.put("company_logo_url", null);
			data.put("api_document_url", null);
			data.put("FAQ_document_url", null);
			data.put("navigation_backcolor", "#506470");
			data.put("navigation_text_color", "#506470");
			data.put("hy_sms_discount", 1);
			data.put("yx_sms_discount", 1);
			data.put("gj_sms_discount", 1);
			data.put("edit", "");

			// 添加短信测试数量标识
			data.put("isHavetestSmsNum", "no");

		}
		return data;
	}

	/**
	 * @Description: 将url加密
	 * @author: Niu.T
	 * @date: 2016年11月29日 下午5:15:59
	 * @param data
	 * @param columns
	 * @return Map<String,Object>
	 */
	private Map<String, Object> encodeColumn(Map<String, Object> data, List<String> columns) {
		if (data == null)
			return data;
		for (String column : columns) {

			if (data.get(column) != null && !"".equals(data.get(column))) {

				data.put(column, SecurityUtils.encodeDes3(data.get(column).toString()));
			} else {
				data.put(column, "");
			}
		}
		return data;
	}

	/**
	 * 上传OEM代理商资料
	 */
	@Transactional("message")
	@Override
	public Map<String, Object> updateAgentInfo(Map<String, String> params) {
		Map<String, Object> data = new HashMap<>();

		if (StringUtils.isBlank(params.get("belong_sale"))) {
			data.put("result", "fail");
			data.put("msg", "归属销售不能为空");
			return data;
		}

		Calendar calendar = Calendar.getInstance();

		boolean isModAgent = false;

		// 查询代理商的信息
		String agentId = params.get("agent_id");
		JsmsAgentInfo agentInfo = jsmsAgentInfoService.getByAgentId(Integer.parseInt(agentId));

		// 更新基本资料
		JsmsAgentInfo jsmsAgentInfo = new JsmsAgentInfo();
		jsmsAgentInfo.setAgentId(Integer.parseInt(agentId));

		// 代理商名称
		if (StringUtils.isNotBlank(params.get("agent_name"))
				&& !agentInfo.getAgentName().equals(params.get("agent_name"))) {
			isModAgent = true;
			jsmsAgentInfo.setAgentName(params.get("agent_name"));
		}

		// 联系地址
		if (StringUtils.isBlank(agentInfo.getAddress())) {
			agentInfo.setAddress("");
		}
		if (StringUtils.isNotBlank(params.get("address")) && !agentInfo.getAddress().equals(params.get("address"))) {
			isModAgent = true;
			jsmsAgentInfo.setAddress(params.get("address"));
		}

		// 备注
		if (StringUtils.isBlank(agentInfo.getRemark())) {
			agentInfo.setRemark("");
		}
		if (StringUtils.isNotBlank(params.get("remark")) && !agentInfo.getRemark().equals(params.get("remark"))) {
			isModAgent = true;
			jsmsAgentInfo.setRemark(params.get("remark"));
		}

		// 手机号码
		String mobile = params.get("mobile");
		if (StringUtils.isNotBlank(mobile) && !agentInfo.getMobile().equals(mobile)) {
			isModAgent = true;
			jsmsAgentInfo.setMobile(mobile);
		}

		// 更新代理商的归属销售
		JsmsAccount jsmsAccount = null;
		Long belongSale = Long.parseLong(params.get("belong_sale"));
		Long oldBelongSale = agentInfo.getBelongSale();
		if (oldBelongSale == null || belongSale.compareTo(oldBelongSale) != 0) {
			isModAgent = true;
			jsmsAgentInfo.setBelongSale(belongSale);

			jsmsAccount = new JsmsAccount();
			jsmsAccount.setBelongSale(belongSale);
			jsmsAccount.setAgentId(Integer.parseInt(agentId));
		}

		int modCount;

		// 修改代理商
		if (isModAgent) {
			//修改归属销售财务
			com.jsmsframework.common.dto.R r=jsmsSaleCreditService.singleBelongSaleChaned(oldBelongSale,belongSale,Integer.parseInt(agentId));
			if(Objects.equals(SysConstant.FAIL_CODE,r.getCode())){
				data.put("result", SysConstant.FAIL);
				data.put("msg", r.getMsg());
				return data;
			}

			jsmsAgentInfo.setUpdateTime(calendar.getTime());
			modCount = jsmsAgentInfoService.updateSelective(jsmsAgentInfo);

			if (modCount <= 0) {
				throw new OperationException("修改代理商资料失败");
			}

			// 更细客户的销售归属
			if (jsmsAccount != null) {
				int update = jsmsAccountService.updateBelongSale(jsmsAccount);
				logger.debug("更新代理商归属销售时, 同时修改其名下的客户 ---> update = {}", update);
			}
		}

		boolean isModUser = false;
		JsmsUser user = jsmsUserService.getById(String.valueOf(agentInfo.getAdminId()));

		// 用户修改
		String email = params.get("email");
		JsmsUser jsmsUser = new JsmsUser();
		jsmsUser.setId(user.getId());

		// 手机号是否修改
		if (StringUtils.isNotBlank(jsmsAgentInfo.getMobile())) {
			isModUser = true;
			jsmsUser.setMobile(jsmsAgentInfo.getMobile());
		}

		// 邮箱是否修改
		if (StringUtils.isNotBlank(email) && !email.equals(user.getEmail())) {
			isModUser = true;
			jsmsUser.setEmail(email);
		}
		// 修改用户
		if (isModUser) {
			jsmsUser.setUpdateDate(calendar.getTime());
			modCount = jsmsUserService.updateSelective(jsmsUser);

			if (modCount <= 0) {
				throw new OperationException("修改代理商管理员资料失败");
			}
		}
		// 仅OEM代理商进行上传资料的控制
		if (agentInfo.getAgentType().equals(5)) {
			int oemDataNum = masterDao.getOneInfo("agentManage.checkDataExist", agentId);
			logger.debug("oemDataNum大于0则更新记录，否则则新增记录。oemDataNum={}", oemDataNum);
			int resultNum;
			params.put("domain", params.get("domain_name"));
			List<Map<String, Object>> dcs = masterDao.selectList("agentManage.getOEMDataConfigByDomain", params);
			if (!Collections3.isEmpty(dcs)) {
				String clientUrl=ConfigUtils.client_site_url;
				if(clientUrl.contains("//")){
					String[] ss = clientUrl.split("//");
					clientUrl=ss[1];
				}
				for (Map<String, Object> d : dcs) {
					String dbAgentId = d.get("agent_id") != null ? d.get("agent_id").toString() : "";
					String domain = d.get("domain_name") != null ? d.get("domain_name").toString() : "";
					//if (!dbAgentId.equals(agentId) && !domain.equals("oemclient.sms.ucpaas.com")) {
					if (!dbAgentId.equals(agentId) && !domain.equals(clientUrl)) {
						throw new OperationException("域名已存在，请修改后再提交。代理商id=" + d.get("agent_id") + "已使用该域名");
					}
				}
			}

			// 转换类
			JsmsOemDataConfig dataConfig = new JsmsOemDataConfig();
			dataConfig.setAgentId(Integer.parseInt(agentId));
			dataConfig.setDomainName(params.get("domain"));
			dataConfig.setCopyrightText(params.get("copyright"));
			dataConfig.setTabLogoUrl(params.get("h_tab_logo"));
			dataConfig.setLogoUrl(params.get("h_logo"));
			dataConfig.setCompanyLogoUrl(params.get("h_company_logo"));
			dataConfig.setApiDocumentUrl(params.get("h_api_document"));
			dataConfig.setFAQDocumentUrl(params.get("h_FAQ_document"));
			dataConfig.setNavigationBackcolor(params.get("nav_backcolor"));
			dataConfig.setNavigationTextColor(params.get("nav_text_color"));

			if (StringUtils.isNotBlank(params.get("productId"))) {
				dataConfig.setTestProductId(Integer.parseInt(params.get("productId")));
			}

			if (StringUtils.isNotBlank(params.get("productNum"))) {
				dataConfig.setTestSmsNumber(Integer.parseInt(params.get("productNum")));
			}

			resultNum = oemDataNum > 0 ? jsmsOemDataConfigService.updateSelectiveByAgentId(dataConfig)
					: jsmsOemDataConfigService.insert(dataConfig);

			if (resultNum <= 0) {
				data.put("result", "fail");
				data.put("msg", "修改代理商资料失败");
				return data;
			}
		}

		data.put("result", "success");
		data.put("msg", "修改代理商资料成功");

		return data;
	}

	@Override
	@Transactional("message") // todo 事务
	public R addClient(Account account, Long adminId, String pageUrl, String ip,boolean sale) {
		logger.debug("账户管理-添加客户开始：" + JacksonUtil.toJSON(account));
		Map<String, Object> data = Maps.newHashMap();

		Date now = new Date();
		// 创建时clientid查重
		int count = accountMapper.checkAccount(account);
		if (count > 0) {
			throw new OperationException("账号已经存在，请重新输入");
		}

		//if (account.getAccessSpeed() > 3000 || account.getAccessSpeed() < 0) {
		//	throw new OperationException("客户接入速度取值范围为0至3000");
		//}
		if(account.getAccessSpeed()==null){
			account.setAccessSpeed(100);
		}

		if (account.getPaytype() > 1 || account.getPaytype() < 0) {
			throw new OperationException("付费类型只能是预付费或后付费");
		}

		if (account.getSmsfrom() > 6 || account.getSmsfrom() < 1) {
			throw new OperationException("客户接入使用协议类型只能是SMPP协议/CMPP2.0协议/CMPP3.0协议/SGIP协议/SMGP协议/HTTPS协议");
		}

		if (account.getSmsfrom().equals(6) && account.getHttpProtocolType() == null) {
			throw new OperationException("请选择https子协议类型");
		}

		if (account.getSmsfrom().equals(6) && account.getHttpProtocolType().equals(2) && account.getSmstype() == null) {
			throw new OperationException("请选择短信类型");
		}

		if (account.getPaytype() == 1
				&& (account.getUserPriceList() == null || account.getUserPriceList().size() <= 0)) {
			throw new OperationException("短信单价不能为空");
		}

		if (account.getClientType() > 2 || account.getClientType() < 1) {
			throw new OperationException("客户类型只能是个人用户或企业用户");
		}

		if (account.getNeedreport() > 3 || account.getNeedreport() < 0) {
			throw new OperationException("状态报告取值错误");
		}

		// add by lpjLiu 2017-09-26 v2.2.2 v5.14.0
		if (account.getClientInfoExt() == null) {
			throw new OperationException("用户扩展信息不能为空");
		}

		if (account.getClientInfoExt().getExtValue() == null) {
			throw new OperationException("是否支持子帐号不能为空");
		}

		if (account.getClientInfoExt().getExtValue() != 0 && account.getClientInfoExt().getExtValue() != 1) {
			throw new OperationException("是否支持子帐号取值错误");
		}
		// 上行获取方式
		Set<Integer> sets = new HashSet<>();
		sets.add(0);
		sets.add(1);
		sets.add(3);
		if (!sets.contains(account.getNeedmo())) {
			throw new OperationException("是否需要上行取值错误");
		}

		// Add by lpjLiu 2017-12-26 V 44_201712_运营中心_修订版本 验证状态报告地址或上行地址是否需要填写
		if (account.getSmsfrom().equals(SmsFrom.HTTPS.getValue()))
		{
			// 验证 状态报告地址
			if (account.getNeedreport().intValue() == 1 || account.getNeedreport().intValue() == 2)
			{
				if (StringUtils.isBlank(account.getDeliveryurl()))
				{
					throw new OperationException("状态报告回调地址长度必须介于 1 和 100 之间");
				}
			}

			// 验证 上行地址
			if (account.getNeedmo().intValue() == 1)
			{
				if (StringUtils.isBlank(account.getMourl()))
				{
					throw new OperationException("上行回调地址长度必须介于 1 和 100 之间");
				}
			}
		}

		if (account.getNeedaudit() > 3 || account.getNeedaudit() < 0) {
			throw new OperationException("是否审核取值错误");
		}

		if (account.getSmsfrom() == 4) {
			// SGIP协议接入客户提供的上行端口长度必须介于 1 和 11 之间
			if (StringUtils.isBlank(account.getMoport()) || account.getMoport().length() > 11) {
				throw new OperationException("SGIP协议接入客户提供的上行端口长度必须介于 1 和 11 之间");
			}

			// SGIP协议接入客户提供的上行IP长度必须介于 1 和 100 之间
			if (StringUtils.isBlank(account.getMoip()) || account.getMoip().length() > 100) {
				throw new OperationException("SGIP协议接入客户提供的上行IP长度必须介于 1 和 100 之间");
			}

			// 提供给SGIP协议接入客户的节点编码
			if (account.getNodeid() == null) {
				throw new OperationException("SGIP协议接入客户的节点编码不能为空");
			}

			// 提供给SGIP协议接入客户的节点编码
			if (account.getNodeid() < -100000000 || account.getNodeid() > 100000000) {
				throw new OperationException("SGIP协议接入客户的节点编码值太小或太大");
			}
		}

		if (account.getNeedextend() > 1 || account.getNeedextend() < 0) {
			throw new OperationException("自扩展位取值错误");
		}

		if (account.getNeedextend() == 1 && (account.getExtendSize() > 10 || account.getExtendSize() < 0)) {
			throw new OperationException("自扩展位取值范围为0至10");
		}

		// 邮箱
		if (StringUtils.isNotBlank(account.getEmail()) && account.getEmail().length() > 100) {
			throw new OperationException("邮箱地址长度必须介于 0 和 100 之间");
		}

		// 手机号
		if (StringUtils.isNotBlank(account.getMobile()) && account.getMobile().length() > 20) {
			throw new OperationException("手机号码长度必须介于 0 和 20 之间");
		}

		// 校验邮箱或手机号
		Account accountQuery;
		if (StringUtils.isNotBlank(account.getEmail()) || StringUtils.isNotBlank(account.getMobile())) {
			accountQuery = new Account();
			accountQuery.setEmail(account.getEmail());
			accountQuery.setMobile(account.getMobile());
			String msg = checkAccountMobileAndEmail(accountQuery);
			if (StringUtils.isNotBlank(msg)) {
				throw new OperationException(msg);
			}
		}

		// Add by lpjLiu 20171012 v2.3.0 v5.15.0
		sets.clear();
		sets.add(0);
		sets.add(1);
		sets.add(2);
		if (account.getChargeRule() == null || !sets.contains(account.getChargeRule())) {
			throw new OperationException("计费规则不能为空，必须是 提交量/成功量/明确成功量");
		}

		// 成功量需要设置 是否自动返还
		if (account.getChargeRule().intValue() == ChargeRule.成功量计费.getValue().intValue()) {
			if (account.getClientInfoExt() == null || account.getClientInfoExt().getExtValue() == null) {
				throw new OperationException("当计费规则为成功量时，需要设置是否自动返还");
			}
			sets.clear();
			sets.add(0);
			sets.add(1);
			sets.add(3);
			if (!sets.contains(account.getClientInfoExt().getExtValue())) {
				throw new OperationException("当计费规则为成功量时，需要设置是否自动返还, 是否自动返还的取值错误");
			}
		}

		// 代理商为空是直客，直客以a开头，代理商以b开头
		String prefix = "b";
		if (account.getAgentId() == null) {
			prefix = "a";
		}

		// 添加主键,32位随机字符串
		account.setId(UUID.randomUUID().toString().replace("-", ""));

		// 添加用户id(从公用序列中取,如果没有则生成后再取)
		String clientId=commonSeqService.getOrAddId(prefix);
		if (StringUtils.isBlank(clientId))
		{
			throw new OperationException("生成子客户帐号失败，请重试!");
		}


		account.setClientid(clientId);

		// 设置密码
		if (StringUtils.isBlank(account.getPassword()) || account.getPassword().length() < 8
				|| account.getPassword().length() > 12) {
			throw new OperationException("密码长度介于8至12之间");
		}

		// 获取系统参数,默认的identify
		String identify = (String) commonService.getSysParams("DEFAULT_IDENTIFY").get("param_value");
		account.setIdentify(Integer.valueOf(identify));

		// Mod: lpjLiu 20170929 默认为待认证 v2.2.2 v5.14.0
		//代理商自己使用
		if (account.getAgentId() == null )
		{
			account.setAgentOwned(AgentOwned.代理商子客户使用.getValue());
			account.setOauthStatus(OauthStatusEnum.待认证.getValue());
			account.setOauthDate(null);
		} else {
			if((AgentOwned.代理商自己使用.getValue()).equals(account.getAgentOwned())){
				//销售创建
				/*if(sale){
					account.setOauthStatus(OauthStatusEnum.待认证.getValue());
					account.setOauthDate(null);
				}else{*/
					account.setOauthStatus(OauthStatusEnum.证件已认证.getValue());
					account.setOauthDate(now);
				/*}*/
			}else{
				account.setOauthStatus(OauthStatusEnum.待认证.getValue());
				account.setOauthDate(null);
			}
		}

		// 认证状态，2：待认证 ，3：证件已认证(正常)，4：认证不通过
		// 选择了代理商且短信协议为“HTTPS”时，认证状态为待认证，不能进行修改，而非“HTTPS”短信或“HTTPS”且未选择代理商时，认证状态默认为已认证。
		/*
		 * if (account.getSmsfrom() == 6 && account.getPaytype() == 0 &&
		 * account.getAgentId() != null) { account.setOauthStatus(2); } else {
		 * account.setOauthStatus(3);
		 * account.setOauthDate(Calendar.getInstance().getTime()); }
		 */

		if (StringUtils.isBlank(account.getIp())) {
			account.setIp("*");
		}

		account.setStatus(1);

		// 真实名称
		account.setRealname(account.getName());

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

		Calendar calendar = Calendar.getInstance();
		account.setCreatetime(calendar.getTime());
		account.setUpdatetime(account.getCreatetime());

		// 添加params数据到数据库
		logger.debug("插入账户数据及更新clientid状态开始======================================");
		int addCount = accountMapper.insert(account);
		if(addCount!=1){
			throw new OperationException("插入账户数据失败!");
		}
		boolean updateIdStatus = commonSeqService.updateClientIdStatus(account.getClientid());
		logger.debug("插入账户数据及更新clientid状态结束======================================");
		//account.setAgentOwned(1);
		//直客的代理商id是没有值的,代理商子客户是有值的
		if(null!=account.getAgentId()){
			//是否代理商自己使用0为否,1为是
			if(account.getAgentOwned()<0 || account.getAgentOwned()>1){
				throw new OperationException("使用对象赋值错误!");
			}
		}
		//代理商自己使用
		if((AgentOwned.代理商自己使用.getValue()).equals(account.getAgentOwned())){
			//根据代理商的id获取其资质
			List<SmsOauthpic> smsOauthpicList = accountMapper.getSmsOauthpic(account.getAgentId(),"",1);
			if(smsOauthpicList==null && smsOauthpicList.size()<1){
				throw new OperationException("代理商已认证,但是认证信息不存在!");
			}
			if(smsOauthpicList.size()!=1){
				throw new OperationException("代理商资质信息多份!");
			}
			SmsOauthpic smsOauthpic = smsOauthpicList.get(0);
			//自动认证，继承代理商资质信息
			smsOauthpic.setCreateDate(now);
			smsOauthpic.setUpdateDate(now);
			smsOauthpic.setStatus("1");
			smsOauthpic.setOauthType("2");//客户资质认证
			smsOauthpic.setClientId(clientId);
			int addCerr = accountMapper.addCerInfo(smsOauthpic);
			if(addCerr!=1){
				throw new OperationException("资质copy失败!");
			}
		}

		//设置默认余额告警
		// 根据代理商id获取其信息
		AgentInfo agentInfo = agentInfoMapper.getByAgentId(account.getAgentId());
		if(agentInfo!=null){
			JsmsUser user = jsmsUserService.getById(agentInfo.getAdminId().toString());
			int addAlarm = agentInfoService.insertClientBalanceAlarm(account, agentInfo.getMobile(), user.getEmail());
			if (addAlarm <= 0){
				logger.debug("设置余额警告和赠送短信失败!");
				throw new AgentException("设置余额警告和赠送短信失败!");
			}
		}

		// Add by lpjLiu 2017-09-26 v2.2.2 v5.14.0 添加扩展信息表
		JsmsClientInfoExt clientInfoExt = account.getClientInfoExt();
		clientInfoExt.setClientid(account.getClientid());
		clientInfoExt.setParentId(null);
		clientInfoExt.setRemark(null);
		clientInfoExt.setUpdator(adminId);
		clientInfoExt.setUpdateDate(Calendar.getInstance().getTime());
		clientInfoExt.setWebPassword(account.getPassword());
		int addExtCount = jsmsClientInfoExtService.insert(clientInfoExt);
		if(addExtCount!=1){
			throw new OperationException("添加扩展信息失败!");
		}
		if (OauthStatusEnum.证件已认证.getValue().equals(account.getOauthStatus())){
			// 判断子账户有没有赠送短信, 没有则赠送, 有则不赠送
			accountService.isGiveMessage(clientId, adminId);
		}

		// 插入用户价格
		for (JsmsUserPriceLog userPriceLog : account.getUserPriceList()) {
			if (userPriceLog.getSmstype() == null) {
				throw new OperationException("短信类型不能为空");
			}
			if (userPriceLog.getUserPrice() == null) {
				throw new OperationException("短信单价不能为空");
			}

			userPriceLog.setClientid(account.getClientid());
			userPriceLog.setEffectDate(account.getCreatetime());
			userPriceLog.setCreateTime(account.getCreatetime());
			userPriceLog.setUpdateTime(account.getUpdatetime());
		}

		int batchAddUserPrice = 0;
		if (account.getUserPriceList() != null && account.getUserPriceList().size() > 0) {
			batchAddUserPrice = accountMapper.batchAddUserPrice(account.getUserPriceList());
		}

		// Add by lpjLiu 2017-09-26 v2.3.0 v5.15.0 增加计费规则
		JsmsUserPropertyLog userPropertyLog = new JsmsUserPropertyLog();
		userPropertyLog.setId(null);
		userPropertyLog.setClientid(account.getClientid());
		userPropertyLog.setProperty("charge_rule");
		userPropertyLog.setValue(account.getChargeRule().toString());
		userPropertyLog.setEffectDate(account.getCreatetime());
		userPropertyLog.setOperator(adminId);
		userPropertyLog.setCreateTime(account.getCreatetime());
		userPropertyLog.setUpdateTime(account.getUpdatetime());
		userPropertyLog.setRemark(null);
		int addChareRule = jsmsUserPropertyLogService.insert(userPropertyLog);

		if (addCount > 0 && updateIdStatus && addExtCount > 0 && addChareRule > 0
				&& batchAddUserPrice == account.getUserPriceList().size()) {
			// add by lpjLiu 20170928 增加操作日志
			String desc = prefix.equals("a") ? "直客开户" : "子账户开户";
			logService.add(LogConstant.LogType.add, LogEnum.账户信息管理.getValue(), adminId, pageUrl, ip,
					"账户信息管理-客户开户-" + desc, JSON.toJSONString(account));
			//data.put("result", "success");
			//data.put("msg", "添加客户成功");
		} else {
			throw new OperationException("添加客户失败");
		}

		logger.debug("账户管理-添加客户结束");
		//包含客户端网址、帐号、邮箱、手机、密码、接口帐号、接口密码
		data.put("clientUrl", ConfigUtils.client_site_url);
		//帐号
		data.put("clientId",account.getClientid());
		data.put("email", account.getEmail());
		data.put("mobile", account.getMobile());
		data.put("password", account.getPassword());
		//接口帐号
		data.put("interfaceClientId",account.getClientid());
		//接口密码
		data.put("interfacePassword",account.getPassword());

		return R.ok("success",data);
	}

	private String checkAccountMobileAndEmail(Account account) {
		Account checkEmailAndMobile = accountMapper.accountApplyCheckInAcc(account);
		if (null != checkEmailAndMobile) {
			if (StringUtils.isNotBlank(checkEmailAndMobile.getEmail())) {
				return "邮箱已经被注册";
			}

			if (StringUtils.isNoneBlank(checkEmailAndMobile.getMobile())) {
				return "手机已经被注册";
			}
		}
		return null;
	}

	@Transactional("message_old")
	@Override
	public ResultVO updatePsd(JsmsAccount account) {
		account.setPassword(Encodes.decodeBase64String(account.getPassword()));
		int update = jsmsAccountService.updateByClientId(account);
		if (update > 0) {
			return ResultVO.successDefault();
		} else {
			return ResultVO.failure("密码修改失败!");
		}
	}

	@Transactional("message")
	@Override
	public Map<String, Object> updateClient(Account account, Long adminId, String pageUrl, String ip) {
		// 是否直客
		boolean isZK = account.getAgentId() == null;
		String desc = isZK ? "直客" : "代理商子客户";
		String desc1 = isZK ? "直客管理" : "代理商子客户管理";
		logger.debug("账户管理-更新{}开始：{}", desc, JacksonUtil.toJSON(account));
		Map<String, Object> data = Maps.newHashMap();

		Date nowTime = new Date();
		// 检查参数
		String msg = isZK ? checkDirectclient(account) : checkClient(account);
		if (StringUtils.isNotBlank(msg)) {
			data.put("result", "fail");
			data.put("msg", msg);
			return data;
		}

		AccountPo accountData = isZK ? this.getDirectclientDetailInfo(account.getClientid())
				: this.getClientInfo(account.getClientid());

		// 如果是不支持更新为支持
		if (accountData.getExt_value() == 0 && account.getExtValue() == 1) {
			this.updateExtValue(accountData.getClientid(), account.getExtValue());
		}
		// 如果由支持更新为不支持
		if (accountData.getExt_value() == 1 && account.getExtValue() == 0) {
			this.updateExtValue(accountData.getClientid(), account.getExtValue());
		}

		// Mod by lpjLiu 20170929 认证状态不更新 v2.2.2 v5.14.0
		// 认证状态，2：待认证 ，3：证件已认证(正常)，4：认证不通过
		// 选择了代理商且短信协议为“HTTPS”时，认证状态为待认证，不能进行修改，而非“HTTPS”短信或“HTTPS”且未选择代理商时，认证状态默认为已认证。
		/*
		 * if (account.getSmsfrom() == 6 && account.getPaytype() == 0 &&
		 * account.getAgentId() != null) { } else { if (account.getOauthStatus()
		 * != 3) { account.setOauthStatus(3);
		 * account.setOauthDate(Calendar.getInstance().getTime()); } }
		 */
		account.setOauthStatus(null);
		account.setOauthDate(null);
		if (account.getUserPriceList().size() == 3) {
			account.setSmstype(9);
		} else if (account.getUserPriceList().size() == 1 && account.getSize() != 3) {
			account.setSmstype(account.getUserPriceList().get(0).getSmstype());
		} else if (account.getUserPriceList().size() == 2 && account.getSize() != 3) {
			account.setSmstype(null);
		}

		if (isZK)
		{
			if (account.getAgentOwned() == null)
			{
				account.setAgentOwned(AgentOwned.代理商子客户使用.getValue());
			}
		}

		//是否代理商自有用户帐号，0：否，1：是
		if(account.getAgentOwned()<0 || account.getAgentOwned()>1){
			data.put("result", "fail");
			data.put("msg", "使用对象取值错误!");
			return data;
		}
//原始数据
		Account accountOld = accountMapper.getAccount(account.getClientid());
		//代理商下级客户(原为代理商自己使用)
		if((AgentOwned.代理商子客户使用.getValue()).equals(account.getAgentOwned())&&accountOld.getAgentOwned().equals(AgentOwned.代理商自己使用.getValue())){
			//更新帐号的agent_owned为0
			account.setAgentOwned(0);
			//删除原有资质
			List<SmsOauthpic> smsOauthpicList = accountMapper.getSmsOauthpic(account.getAgentId(), account.getClientid(), 2);
			if(smsOauthpicList!=null && smsOauthpicList.size()>0){
				for (int j = 0; j<smsOauthpicList.size(); j++){
					SmsOauthpic smsOauthpic = smsOauthpicList.get(j);
					int i = accountMapper.deleteSmsOauthpic(smsOauthpic.getId());
					if(i!=1){
						data.put("result", "fail");
						data.put("msg", "资质删除失败!");
						return data;
					}
				}
			}
			//修改状态为待认证
			account.setOauthStatus(OauthStatusEnum.待认证.getValue());
			//修改认证时间为空
			account.setOauthDate(null);
		}
		//代理商自己使用(原为代理商下级客户)
		if((AgentOwned.代理商自己使用.getValue()).equals(account.getAgentOwned())&&accountOld.getAgentOwned().equals(AgentOwned.代理商子客户使用.getValue())){
			//删除原有资质

			List<SmsOauthpic> smsOauthpicList = accountMapper.getSmsOauthpic(account.getAgentId(), account.getClientid(), 2);
			if(smsOauthpicList!=null && smsOauthpicList.size()>0){
				for (int j = 0; j<smsOauthpicList.size(); j++){
					SmsOauthpic smsOauthpic = smsOauthpicList.get(j);
					int i = accountMapper.deleteSmsOauthpic(smsOauthpic.getId());
					if(i!=1){
						data.put("result", "fail");
						data.put("msg", "资质删除失败!");
						return data;
					}
				}
			}
			//更新帐号的agent_owned为1
			account.setAgentOwned(1);
			//从代理商克隆资质
			List<SmsOauthpic> smsOauthpicLi = accountMapper.getSmsOauthpic(account.getAgentId(),"",1);
			if(Collections3.isEmpty(smsOauthpicLi)){
				data.put("result", "fail");
				data.put("msg", "代理商资质不存在!");
				return data;
			}
			if (smsOauthpicLi.size()>1){
				data.put("result", "fail");
				data.put("msg", "代理商资质存在相同多份!");
				return data;
			}
			SmsOauthpic smsOauthpic = smsOauthpicLi.get(0);
			smsOauthpic.setClientId(account.getClientid());
			smsOauthpic.setCreateDate(nowTime);
			smsOauthpic.setUpdateDate(nowTime);
			smsOauthpic.setStatus("1");//有效
			smsOauthpic.setOauthType("2");//客户资质认证
			accountMapper.addCerInfo(smsOauthpic);
			//修改状态为认证通过
			account.setOauthStatus(OauthStatusEnum.证件已认证.getValue());
			//修改认证时间
			account.setOauthDate(nowTime);
			JsmsAccount jsmsAccount = jsmsAccountService.getByClientId(account.getClientid());

			// 判断子账户有没有赠送短信, 没有则赠送, 有则不赠送
			accountService.isGiveMessage(jsmsAccount.getClientid(),adminId);
		}
		int updateCount = accountMapper.updateSelective(account);

		// 更新价格
		if (account.getUserPriceList() != null && account.getUserPriceList().size() > 0) {
			// 今天
			Calendar now = Calendar.getInstance();
			String nowStr = DateUtils.formatDate(now.getTime());
			// 明天
			Calendar tomorrow = Calendar.getInstance();
			tomorrow.setTime(now.getTime());
			tomorrow.set(Calendar.DATE, tomorrow.get(Calendar.DATE) + 1);
			String tomorrowStr = DateUtils.formatDate(tomorrow.getTime());

			for (JsmsUserPriceLog userPriceLog : account.getUserPriceList()) {
				UserPriceLogDTO userPriceLogDTO = new UserPriceLogDTO();
				BeanUtil.copyProperties(userPriceLog, userPriceLogDTO);
				if (userPriceLogDTO.getClientid() == null) {
					userPriceLogDTO.setClientid(account.getClientid());
				}

				if (userPriceLogDTO.getSmstype() == null) {
					throw new OperationException("修改短信单价的短信类型不能为空");
				}

				if (userPriceLogDTO.getUserPrice() == null) {
					throw new OperationException("修改短信单价的短信单价不能为空");
				}

				// Mod by lpjLiu 2017-09-28 生效日期修改 JIRA:
				// http://172.16.5.13:8080/browse/SMS-804

				// 设置生效日期为今天，查询单价
				/*
				 * userPriceLogDTO.setEffectDateStr(nowStr); JsmsUserPriceLog
				 * price = userPriceLogMapper.getPriceOnUpdate(userPriceLogDTO);
				 * if (price == null) { // 当天没有，插入，生效日期为当天
				 * userPriceLogDTO.setEffectDate(now.getTime());
				 * userPriceLogDTO.setCreateTime(now.getTime());
				 * userPriceLogDTO.setUpdateTime(now.getTime());
				 * userPriceLogMapper.insert(userPriceLogDTO); } else { //
				 * 当天有，查询明天 userPriceLogDTO.setEffectDateStr(tomorrowStr);
				 * JsmsUserPriceLog price1 =
				 * userPriceLogMapper.getPriceOnUpdate(userPriceLogDTO);
				 *
				 * // 明天有就更新，明天没有就插入 if (price1 == null) {
				 * userPriceLogDTO.setEffectDate(tomorrow.getTime());
				 * userPriceLogDTO.setCreateTime(now.getTime());
				 * userPriceLogDTO.setUpdateTime(now.getTime());
				 * userPriceLogMapper.insert(userPriceLogDTO); } else {
				 * price1.setUserPrice(userPriceLogDTO.getUserPrice());
				 * price1.setUpdateTime(now.getTime());
				 * userPriceLogMapper.updatePrice(price1); } }
				 */

				// 如果（用户账号+ 短信类型）没有记录--- 插入，当天生效
				// 如果（用户账号+ 短信类型）有记录-， 且存在记录的生效时间为次日时，更新记录。
				// 如果（用户账号+ 短信类型）有记录-， 且所有记录的生效时间<=当天，插入新记录，生效时间为次日生效

				// 设置生效日期为明天，查询记录
				userPriceLogDTO.setEffectDateStr(tomorrowStr);
				JsmsUserPriceLog price = userPriceLogMapper.getPriceOnUpdate(userPriceLogDTO);

				// 若有记录，更新记录
				if (price != null) {
					price.setUserPrice(userPriceLogDTO.getUserPrice());
					price.setUpdateTime(now.getTime());
					userPriceLogMapper.updatePrice(price);
				}
				// 若明天没有记录
				else {
					// 根据用户帐号和短信类型查询
					price = userPriceLogMapper.getLatestPrice(userPriceLogDTO);

					// 若不存在，插入，生效日期为今天
					if (price == null) {
						userPriceLogDTO.setEffectDate(now.getTime());
					} else {
						userPriceLogDTO.setEffectDate(tomorrow.getTime());
					}

					userPriceLogDTO.setCreateTime(now.getTime());
					userPriceLogDTO.setUpdateTime(now.getTime());
					userPriceLogMapper.insert(userPriceLogDTO);
				}

			}
		}

		if (updateCount > 0) {
			// add by lpjLiu 20170928 增加操作日志
			logService.add(LogConstant.LogType.update, LogEnum.账户信息管理.getValue(), adminId, pageUrl, ip,
					"账户信息管理-客户开户-" + desc1, JSON.toJSONString(account));

			data.put("result", "success");
			data.put("msg", "更新" + desc + ":" + account.getClientid() + "成功");
		} else if (updateCount == 0) {
			data.put("result", "fail");
			data.put("msg", "更新" + desc + ":" + account.getClientid() + "失败");
		} else {
			throw new OperationException("添加客户失败");
		}
		logger.debug("账户管理-更新{}:{}结束：{}", desc, account.getClientid());
		return data;
	}

	@Override
	@Transactional("message")
	public R updateClienttChargeRule(Account account, Long adminId, String pageUrl, String ip) {
		R r;
		logger.debug("账户信息管理-客户信息详情-更新计费规则 开始：客户帐号={} 计费规则={}", account.getClientid(), account.getChargeRule());

		if (StringUtils.isBlank(account.getClientid())) {
			return R.error("客户ID不能为空");
		}

		int count = accountMapper.checkAccount(account);
		if (count <= 0) {
			return R.error("客户账号不存在");
		}

		Set<Integer> sets = new HashSet<>();
		sets.clear();
		sets.add(0);
		sets.add(1);
		sets.add(2);
		if (account.getChargeRule() == null || !sets.contains(account.getChargeRule())) {
			return R.error("计费规则不能为空，必须是 提交量/成功量/明确成功量");
		}

		if (account.getEffectDate() == null) {
			return R.error("计费规则的生效日期不能为空");
		}

		boolean needUpdate = false;
		// 成功量需要设置 是否自动返还
		if (account.getChargeRule().intValue() == ChargeRule.成功量计费.getValue().intValue()) {
			needUpdate = true;
			if (account.getExtValue() == null || account.getExtValue() == null) {
				return R.error("当计费规则为成功量时，需要设置是否自动返还");
			}

			sets.clear();
			sets.add(0);
			sets.add(1);
			sets.add(3);
			if (!sets.contains(account.getExtValue())) {
				return R.error("当计费规则为成功量时，需要设置是否自动返还, 是否自动返还的取值错误");
			}
		}

		int updateCount = 0;
		String date = DateUtils.formatDate(account.getEffectDate(), "yyyyMMdd");
		JsmsUserPropertyLog userPropertyLog = jsmsUserPropertyLogService
				.getCanUpdateChargeRuleByClientIdAndEffectDate(account.getClientid(), date);

		if (userPropertyLog == null) {
			userPropertyLog = new JsmsUserPropertyLog();
			userPropertyLog.setId(null);
			userPropertyLog.setClientid(account.getClientid());
			userPropertyLog.setProperty("charge_rule");
			userPropertyLog.setValue(account.getChargeRule().toString());
			userPropertyLog.setEffectDate(account.getEffectDate());
			userPropertyLog.setOperator(adminId);
			userPropertyLog.setCreateTime(Calendar.getInstance().getTime());
			userPropertyLog.setUpdateTime(Calendar.getInstance().getTime());
			userPropertyLog.setRemark(null);
			updateCount = jsmsUserPropertyLogService.insert(userPropertyLog);
		} else {
			userPropertyLog.setClientid(null);
			userPropertyLog.setProperty("charge_rule");
			userPropertyLog.setValue(account.getChargeRule().toString());
			userPropertyLog.setEffectDate(account.getEffectDate());
			userPropertyLog.setOperator(adminId);
			userPropertyLog.setCreateTime(null);
			userPropertyLog.setUpdateTime(null);
			userPropertyLog.setRemark(null);
			updateCount = jsmsUserPropertyLogService.updateSelective(userPropertyLog);
		}

		// 更新扩展属性
		if (needUpdate) {
			accountMapper.updateExtValue(account.getClientid(), account.getExtValue());
		}

		if (updateCount > 0) {
			// add by lpjLiu 20170928 增加操作日志
			logService.add(LogConstant.LogType.update, LogEnum.账户信息管理.getValue(), adminId, pageUrl, ip,
					"账户信息管理-客户开户-客户信息详情-更新计费规则", JSON.toJSONString(userPropertyLog),
					JSON.toJSONString(userPropertyLog));

			r = R.ok("修改计费规则成功");
		} else {
			throw new OperationException("修改计费规则失败");
		}

		logger.debug("账户信息管理-客户信息详情-更新计费规则 结束：客户帐号={} 计费规则={}", account.getClientid(), account.getChargeRule());
		return r;
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
	public List<AgentInfo> findAgentInfoList(Integer agentType) {
		return agentInfoMapper.findAgentInfoList(agentType);
	}
	@Override
	public List<AgentInfo> findAgentInfoList2(Integer agentType) {
		return agentInfoMapper.findAgentInfoList2(agentType);
	}

	@Override
	public PageContainer findDirectclientList(Map<String, String> params) {
		Map<String, Object> objectMap = Maps.newHashMap();
		objectMap.putAll(params);
		// 构造数据权限查询条件

		Long userId = params.get("userId") == null ? null : Long.parseLong(params.get("userId").toString());
		if (userId != null) {
			objectMap.put("dataAuthorityCondition", AgentUtils.getDataAuthorityCondition(userId, true, false));
		}
		//page.setData(this.accountMapper.findList(page));
		PageContainer<Map<String, Object>> pageContainer = masterDao.getSearchPage("clientManage.findList",
				"clientManage.findListCount", objectMap);
		return pageContainer;
	}

	@Override
	public List<Map<String, Object>> findAllDirectclientList(Map params) {
		// 构造数据权限查询条件
		Long userId = params.get("userId") == null ? null : Long.parseLong(params.get("userId").toString());
		if (userId != null) {
			params.put("dataAuthorityCondition", AgentUtils.getDataAuthorityCondition(userId, true, false));
		}

		return this.accountMapper.findAllListOfMap(params);
	}

	@Override
	public AccountPo getDirectclientDetailInfo(String clientId) {
		AccountPo po = accountMapper.getDirectclientInfo(clientId);
		Integer chargeRule = jsmsUserPropertyLogService.getChargeRuleByClientIdAndDate(clientId,
				new DateTime().toString("yyyyMMdd"));
		if (chargeRule != null) {
			po.setChargeRule(chargeRule);
			po.setChargeRuleStr(ChargeRuleType.getDescByValue(chargeRule));
		} else { // 直客默认计费规则为成功量
			po.setChargeRule(ChargeRuleType.成功量.getValue());
			po.setChargeRuleStr(ChargeRuleType.成功量.getDesc());
		}

		JsmsUserPropertyLog latelyjsmsUserPropertyLog = getLatelyChargeRuleByClientid(clientId);
		if (latelyjsmsUserPropertyLog != null) {
			try {
				Integer nextChargeRule = Integer.valueOf(latelyjsmsUserPropertyLog.getValue());
				po.setNextChargeRule(nextChargeRule);
				po.setNextChargeRuleStr(ChargeRuleType.getDescByValue(nextChargeRule) + "计费于"
						+ new SimpleDateFormat("yyyy-MM-dd").format(latelyjsmsUserPropertyLog.getEffectDate())
						+ "日零点生效！");
			} catch (Exception e) {
				logger.error("计费规则有误", e);
			}
		}

		po.setUserPriceList(getJsmsUserPriceLogs(clientId, po.getSmstype(), po.getSmsfrom()));
		if (po.getUserPriceList() == null) {
			po.setUserPriceList(new ArrayList<JsmsUserPriceLog>());
		}
		return po;
	}

	public JsmsUserPropertyLog getLatelyChargeRuleByClientid(String clientId) {
		JsmsPage page = new JsmsPage();
		page.setRows(Integer.MAX_VALUE);
		page.getParams().put("property", "charge_rule");
		page.getParams().put("clientid", clientId);
		List<JsmsUserPropertyLog> jsmsUserPropertyLogs = jsmsUserPropertyLogService.queryList(page).getData();
		JsmsUserPropertyLog latelyjsmsUserPropertyLog = null;
		long min = Long.MAX_VALUE;
		long now = new Date().getTime();
		for (JsmsUserPropertyLog jsmsUserPropertyLog : jsmsUserPropertyLogs) {
			long distance = jsmsUserPropertyLog.getEffectDate().getTime() - now;
			if (distance < 0)
				continue;
			if (distance < min) {
				min = distance;
				latelyjsmsUserPropertyLog = jsmsUserPropertyLog;
			}
		}

		return latelyjsmsUserPropertyLog;
	}

	@SuppressWarnings("Duplicates")
	@Override
	public AccountPo getClientInfo(String clientId) {
		AccountPo po = accountMapper.getClientInfo(clientId);
		po.setUserPriceList(getJsmsUserPriceLogs(clientId, po.getSmstype(), po.getSmsfrom()));
		if (po.getUserPriceList() == null) {
			po.setUserPriceList(new ArrayList<JsmsUserPriceLog>());
		}
		Integer chargeRule = jsmsUserPropertyLogService.getChargeRuleByClientIdAndDate(clientId,
				new DateTime().toString("yyyyMMdd"));
		if (chargeRule != null) {
			po.setChargeRule(chargeRule);
			po.setChargeRuleStr(ChargeRuleType.getDescByValue(chargeRule));
		} else { // 直客默认计费规则为提交量
			po.setChargeRule(ChargeRuleType.提交量.getValue());
			po.setChargeRuleStr(ChargeRuleType.提交量.getDesc());
		}

		JsmsUserPropertyLog latelyjsmsUserPropertyLog = getLatelyChargeRuleByClientid(clientId);
		if (latelyjsmsUserPropertyLog != null) {
			try {
				Integer nextChargeRule = Integer.valueOf(latelyjsmsUserPropertyLog.getValue());
				po.setNextChargeRule(nextChargeRule);
				po.setNextChargeRuleStr(ChargeRuleType.getDescByValue(nextChargeRule) + "计费于"
						+ new SimpleDateFormat("yyyy-MM-dd").format(latelyjsmsUserPropertyLog.getEffectDate())
						+ "日零点生效！");
			} catch (Exception e) {
				logger.error("计费规则有误", e);
			}
		}

		return po;
	}

	@Override
	public Account getAccount(String clientId) {
		Account po = accountMapper.getAccount(clientId);
		return po;
	}

	private List<JsmsUserPriceLog> getJsmsUserPriceLogs(String clientId, Integer smstype, Integer smsfrom) {
		List<JsmsUserPriceLog> list = Lists.newArrayList();
		JsmsUserPriceLog query = new JsmsUserPriceLog();
		query.setClientid(clientId);
		if (smstype != null) {
			query.setSmstype(smstype);
			JsmsUserPriceLog priceLog = userPriceLogMapper.getLatestPrice(query);
			if (priceLog != null) {
				list.add(priceLog);
			}
		} else {
			if (smsfrom == 6) {
				query.setSmstype(SMSType.通知短信.getValue());
				JsmsUserPriceLog priceLog = userPriceLogMapper.getLatestPrice(query);
				if (priceLog != null) {
					list.add(priceLog);
				}

				query.setSmstype(SMSType.验证码短信.getValue());
				priceLog = userPriceLogMapper.getLatestPrice(query);
				if (priceLog != null) {
					list.add(priceLog);
				}

				query.setSmstype(SMSType.营销短信.getValue());
				priceLog = userPriceLogMapper.getLatestPrice(query);
				if (priceLog != null) {
					list.add(priceLog);
				}

			}
		}
		return list;
	}

	private String checkClientBase(Account account) {
		// 客户密码长度必须介于 1 和 16 之间
		if (StringUtils.isNotBlank(account.getPassword()) && account.getRemarks().length() > 16) {
			return "客户密码长度必须介于 1 和 16 之间";
		}

		// 归属销售
		if (account.getBelongSale() == null) {
			return "客户的归属销售不能为空";
		}

		// 付费类型
		if (account.getPaytype() == null || account.getPaytype() > 1 || account.getPaytype() < 0) {
			return "付费类型只能是预付费或后付费";
		}

		// 客户类型
		if (account.getClientType() == null || account.getClientType() > 2 || account.getClientType() < 1) {
			return "客户类型只能是个人用户或企业用户";
		}

		// 客户名称
		if (StringUtils.isBlank(account.getName()) || account.getName().length() > 50) {
			return "客户名称长度必须介于 1 和 50 之间";
		}
		account.setRealname(account.getName());

		// 邮箱
		if (StringUtils.isNotBlank(account.getEmail()) && account.getEmail().length() > 100) {
			return "邮箱地址长度必须介于 0 和 100 之间";
		}

		// 手机号
		if (StringUtils.isNotBlank(account.getMobile()) && account.getMobile().length() > 20) {
			return "手机号码长度必须介于 0 和 20 之间";
		}

		// 校验邮箱或手机号
		if (StringUtils.isNotBlank(account.getEmail()) || StringUtils.isNotBlank(account.getMobile())) {
			String msg = checkAccountMobileAndEmail(account);
			if (StringUtils.isNotBlank(msg)) {
				return msg;
			}
		}

		// 地址 客户地址长度必须介于 0 和 200 之间
		if (StringUtils.isNotBlank(account.getAddress()) && account.getAddress().length() > 200) {
			return "客户地址长度必须介于 0 和 200 之间";
		}

		// 短信协议
		if (account.getSmsfrom() == null || account.getSmsfrom() > 6 || account.getSmsfrom() < 1) {
			return "客户接入使用协议类型只能是SMPP协议/CMPP2.0协议/CMPP3.0协议/SGIP协议/SMGP协议/HTTPS协议";
		}
		if (account.getSmsfrom().equals(6) && account.getHttpProtocolType() == null) {
			return "请选择https子协议类型";
		}

		if (account.getSmsfrom().equals(6) && account.getHttpProtocolType().equals(2) && account.getSmstype() == null) {
			return "请选择短信类型";
		}

		// IP白名单 IP地址长度必须介于 1 和 512 之间
		if (StringUtils.isBlank(account.getIp())) {
			account.setIp("*");
		} else {
			if (account.getIp().length() > 512) {
				return "IP地址长度必须介于 1 和 512 之间";
			}
		}

		// 客户接入速度
		if (account.getAccessSpeed() == null || account.getAccessSpeed() < 0 || account.getAccessSpeed() > 3000) {
			return "速率不能为空且必须是数字，取值范围为0至3000";
		}

		// 备注
		if (StringUtils.isNotBlank(account.getRemarks()) && account.getRemarks().length() > 200) {
			return "备注长度必须介于 0 和 200 之间";
		}

		if (account.getNeedextend() == null || account.getNeedextend() > 1 || account.getNeedextend() < 0) {
			return "是否支持自扩展取值错误";
		}

		if (account.getNeedextend() == 1 && (account.getExtendSize() < 0 || account.getExtendSize() > 10)) {
			return "自扩展位取值范围为0至10";
		}

		// 状态报告获取方式
		if (account.getNeedreport() == null || account.getNeedreport() > 3 || account.getNeedreport() < 0) {
			return "状态报告取值错误";
		}

		// 上行获取方式
		Set<Integer> sets = new HashSet<>();
		sets.add(0);
		sets.add(1);
		sets.add(3);
		if (account.getNeedmo() == null || !sets.contains(account.getNeedmo())) {
			return "是否需要上行取值错误";
		}

		// 状态报告回调地址 状态报告回调地址长度必须介于 0 和 100 之间
		if (StringUtils.isNotBlank(account.getDeliveryurl()) && account.getDeliveryurl().length() > 100) {
			return "状态报告回调地址长度必须介于 0 和 100 之间";
		}

		// 上行回调地址 上行回调地址长度必须介于 0 和 100 之间
		if (StringUtils.isNotBlank(account.getMourl()) && account.getMourl().length() > 100) {
			return "状态报告回调地址长度必须介于 0 和 100 之间";
		}

		// 审核模板通知回调地址
		if (StringUtils.isNotBlank(account.getNoticeurl()) && account.getNoticeurl().length() > 100) {
			return "审核模板通知回调地址长度必须介于 0 和 100 之间";
		}

		// 后付费需要设置单价
		// if (account.getPaytype() == 1
		// && (account.getUserPriceList() == null ||
		// account.getUserPriceList().size() <= 0)) {
		// return "短信单价不能为空";
		// }

		// add begin
		// add by lpjLiu 2017-09-26 v2.2.2 v5.14.0
		// if (account.getClientInfoExt() == null) {
		// return "用户扩展信息不能为空";
		// }
		//
		// if (account.getClientInfoExt().getExtValue() == null) {
		// return "是否支持子帐号不能为空";
		// }
		//
		// if (account.getClientInfoExt().getExtValue() != 0 &&
		// account.getClientInfoExt().getExtValue() != 1) {
		// return "是否支持子帐号取值错误";
		// }
		// add end

		// Add by lpjLiu 2017-12-26 V 44_201712_运营中心_修订版本 验证状态报告地址或上行地址是否需要填写
		if (account.getSmsfrom().equals(SmsFrom.HTTPS.getValue()))
		{
			// 验证 状态报告地址
			if (account.getNeedreport().intValue() == 1 || account.getNeedreport().intValue() == 2)
			{
				if (StringUtils.isBlank(account.getDeliveryurl()))
				{
					return "状态报告回调地址长度必须介于 1 和 100 之间";
				}
			}

			// 验证 上行地址
			if (account.getNeedmo().intValue() == 1)
			{
				if (StringUtils.isBlank(account.getMourl()))
				{
					return "上行回调地址长度必须介于 1 和 100 之间";
				}
			}
		}

		return null;
	}

	@Override
	public String checkClient(Account account) {
		// 预付
		String msg = checkClientBase(account);
		if (StringUtils.isNotBlank(msg)) {
			return msg;
		}

		// 归属代理商
		if (account.getAgentId() == null) {
			return "客户的代理商不能为空";
		}

		return null;
	}

	@Override
	public String checkDirectclient(Account account) {
		String msg = checkClientBase(account);
		if (StringUtils.isNotBlank(msg)) {
			return msg;
		}

		if (account.getSmsfrom() == 4) {
			// SGIP协议接入客户提供的上行端口长度必须介于 1 和 11 之间
			if (StringUtils.isBlank(account.getMoport()) || account.getMoport().length() > 11) {
				return "SGIP协议接入客户提供的上行端口长度必须介于 1 和 11 之间";

			}

			// SGIP协议接入客户提供的上行IP长度必须介于 1 和 100 之间
			if (StringUtils.isBlank(account.getMoip()) || account.getMoip().length() > 100) {
				return "SGIP协议接入客户提供的上行IP长度必须介于 1 和 100 之间";
			}

			// 提供给SGIP协议接入客户的节点编码
			if (account.getNodeid() == null || account.getNodeid() > 100000000) {
				return "SGIP协议接入客户的节点编码不能为空";
			}

			// 提供给SGIP协议接入客户的节点编码
			if (account.getNodeid() < -100000000 || account.getNodeid() > 100000000) {
				return "SGIP协议接入客户的节点编码值太小或太大";
			}
		}

		return null;
	}

	/**
	 * 添加用户资质信息,返回结果信息(map)
	 */
	@Transactional("message")
	@Override
	public Map<String, Object> addCerInfo(SmsOauthpic smsOauthPic, Long userId) throws Exception {
		Map<String, Object> data = new HashMap<String, Object>();
		int updateAcc = 0;
		int addCer = 0;
		if (StringUtils.isEmpty(smsOauthPic.getImgUrl())) {
			data.put("result", "fail");
			data.put("msg", "请上传图片");
			return data;
		}
		smsOauthPic.setUpdateDate(new Date());// 添加资质更新时间
		if (StringUtils.isNotBlank(smsOauthPic.getClientId())) {// 代理商子客户，直客
			if (StringUtils.isBlank(smsOauthPic.getReason())) {// 代理商子客户
				if (accountMapper.getAccountOfSize(smsOauthPic.getClientId()) == 0) {
					addCer = accountMapper.addCerInfo(smsOauthPic); // 添加用户信息
				} else {
					addCer = accountMapper.updateCerInfo(smsOauthPic);
				}
			} else {// 直客
				if (accountMapper.getAccountOfSize(smsOauthPic.getClientId()) == 0) {
					addCer = accountMapper.addCerInfoOfZK(smsOauthPic); // 添加用户信息
				} else {
					addCer = accountMapper.updateCerInfoOfZK(smsOauthPic);
				}
			}
			// updateAcc = accountMapper.updateAccWithCer(smsOauthPic);// 更新客户状态
			if(smsOauthPic.getOauthStatus().equals("3")){
				customerAudit(smsOauthPic.getClientId(), smsOauthPic.getOauthStatus(), "认证成功", userId, smsOauthPic.getAgentId());
			}else if(smsOauthPic.getOauthStatus().equals("2")){
				customerAudit(smsOauthPic.getClientId(), smsOauthPic.getOauthStatus(), "待认证", userId, smsOauthPic.getAgentId());
			}
		} else {// 代理商
			smsOauthPic.setOauthType("1");
			if (accountMapper.getCerInfo(Integer.parseInt(smsOauthPic.getAgentId())) == 0) {
				addCer = accountMapper.addCerInfo(smsOauthPic); // 添加用户信息
			} else {
				addCer = accountMapper.updateInfo(smsOauthPic);
			}
			// updateAcc = accountMapper.updateAccWithCerOfInfo(smsOauthPic);//
			// 更新客户状态(赠送短信)
			if(smsOauthPic.getOauthStatus().equals("3")){
				agentAudit(smsOauthPic.getAgentId(), smsOauthPic.getOauthStatus(), "认证成功", userId);
			}else if(smsOauthPic.getOauthStatus().equals("2")){
				agentAudit(smsOauthPic.getAgentId(), smsOauthPic.getOauthStatus(), "待认证", userId);
			}
		}

		if (addCer > 0) {
			data.put("result", "success");
			data.put("msg", "添加成功");
			logger.debug("添加客户资质: 成功");
		} else if (addCer == 0 && updateAcc == 0) {
			data.put("result", "fail");
			data.put("msg", "添加失败");
			logger.debug("添加客户资质: 失败");
		} else {
			throw new RuntimeException("添加客户资质信息:同步更新数据异常");
		}
		return data;
	}

	/**
	 * 修改用户资质信息,返回结果信息(map)
	 */
	@Override
	public Map<String, Object> updateCerInfo(SmsOauthpic smsOauthPic) throws Exception {
		Map<String, Object> data = new HashMap<String, Object>();

		if (StringUtils.isEmpty(smsOauthPic.getImgUrl())) {
			data.put("result", "fail");
			data.put("msg", "请上传图片");
			return data;
		}
		smsOauthPic.setUpdateDate(new Date());// 添加资质更新时间
		// smsAccountModel.setUpdateTime(curDate);// 更新客户信息更新时间
		// smsAccountModel.setOauthStatus(2);//认证状态，2：待认证 ，3：证件已认证(正常)，4：认证不通过

		int updateCer = accountMapper.updateCerInfo(smsOauthPic); // 添加用户信息
		int updateAcc = accountMapper.updateAccWithCer(smsOauthPic);// 更新客户状态
		if (updateCer > 0 && updateAcc > 0) {
			data.put("result", "success");
			data.put("msg", "更新成功");
			logger.debug("更新客户资质: 成功! 修改账户信息 -->{}条, 修改图片信息 -->{}条", updateAcc, updateCer);
		} else if (updateCer == 0 && updateAcc == 0) {
			data.put("result", "fail");
			data.put("msg", "更新失败");
			logger.debug("更新客户资质: 失败");
		} else {
			throw new RuntimeException("更改客户资质信息(重新提交):同步更新数据异常");
		}
		return data;
	}

	@Override
	public List<Account> getAllSubAccount() {
		return accountMapper.getAllSubAccount();
	}

	@Override
	public List<Account> getAllBindingSubAccount(String clientid) {
		return accountMapper.getAllBindingSubAccount(clientid);
	}

	@Transactional("message")
	@Override
	public boolean updateBindingSubAccountOfMessage(String clientid, Map<String, String> params) {
		List<Account> bindingList = accountMapper.getAllBindingSubAccount(clientid);// 已经绑定的子账户
		StringBuilder parstr = new StringBuilder();
		StringBuilder bindingParstr = new StringBuilder();
		String parentId = clientid;
		boolean bol = false;
		boolean bool = false;
		if (bindingList.size() > 0) {
			// 将老数据更新为未绑定
			for (int i = 0; i < bindingList.size(); i++) {
				if (bindingList.size() > 1) {
					if (i == 0) {
						parstr = parstr.append("'").append(bindingList.get(i).getClientid()).append("'").append(",");
					} else if (i == bindingList.size() - 1) {
						parstr = parstr.append("'").append(bindingList.get(i).getClientid()).append("'");
					} else {
						parstr = parstr.append("'").append(bindingList.get(i).getClientid()).append("'").append(",");
					}
				} else {
					parstr = parstr.append("'").append(bindingList.get(i).getClientid()).append("'");
				}
			}
			bol = accountMapper.updateBindingSubAccountNull(parstr.toString());
		} else {
			bol = true;
		}
		if (bol) {
			// 将新数据更新为已经绑定
			if (params.size() > 0) {
				params.remove("clientid");
				for (int i = 0; i < params.size(); i++) {
					if (i == 0 && StringUtils.isNotBlank(params.get("list[" + i + "][clientid]"))
							&& i != params.size() - 1) {
						bindingParstr = bindingParstr.append("'").append(params.get("list[" + i + "][clientid]"))
								.append("'").append(",");
					} else if (i == params.size() - 1
							&& StringUtils.isNotBlank(params.get("list[" + i + "][clientid]"))) {
						bindingParstr = bindingParstr.append("'").append(params.get("list[" + i + "][clientid]"))
								.append("'");
					} else {
						if (StringUtils.isNotBlank(params.get("list[" + i + "][clientid]"))) {
							bindingParstr = bindingParstr.append("'").append(params.get("list[" + i + "][clientid]"))
									.append("'").append(",");
						}
					}
				}
				if (StringUtils.isNotBlank(bindingParstr.toString())) {
					bool = accountMapper.updateBindingSubAccount(bindingParstr.toString(), parentId);
				} else {
					bool = true;
				}
			}
		} else {
			bool = true;
		}
		if (bool && bol) {
			return true;
		} else {
			return false;
		}
	}

	@Transactional("message")
	@Override
	public ResultVO updateWebPsd(JsmsClientInfoExt account) {
		account.setWebPassword(Encodes.decodeBase64String(account.getWebPassword()));
		int update = jsmsClientInfoExtService.updateByClientIdOfWeb(account);
		if (update > 0) {
			return ResultVO.successDefault();
		} else {
			return ResultVO.failure("密码修改失败!");
		}
	}

	public boolean updateExtValue(String clientId, Integer extValue) {
		boolean bool = false;
		boolean bool2 = false;
		List<Account> bindingList = null;
		StringBuilder parstr = new StringBuilder();
		if (extValue == 0) {
			bindingList = this.getAllBindingSubAccount(clientId);// 已经绑定的子账户
			if (bindingList.size() > 0) {
				// 将老数据更新为未绑定
				for (int i = 0; i < bindingList.size(); i++) {
					if (bindingList.size() > 1) {
						if (i == 0) {
							parstr = parstr.append("'").append(bindingList.get(i).getClientid()).append("'")
									.append(",");
						} else if (i == bindingList.size() - 1) {
							parstr = parstr.append("'").append(bindingList.get(i).getClientid()).append("'");
						} else {
							parstr = parstr.append("'").append(bindingList.get(i).getClientid()).append("'")
									.append(",");
						}
					} else {
						parstr = parstr.append("'").append(bindingList.get(i).getClientid()).append("'");
					}
				}
				bool2 = accountMapper.updateBindingSubAccountNull(parstr.toString());
			} else {
				bool2 = true;
			}
			if (bool2) {
				bool = accountMapper.updateExtValue(clientId, extValue);
			}
		} else {
			bool = accountMapper.updateExtValue(clientId, extValue);
		}
		return bool;
	}

	@Override
	public int getparentId(String clientId) {
		return accountMapper.getparentId(clientId);
	}

	public ResultVO customerAudit(String client_id, String oauth_status, String remark, Long userId, String agent_id) {
		ResultVO result = ResultVO.failure();

		Map<String, String> params = new HashMap<String, String>();
		params.put("admin_id", String.valueOf(userId));
		params.put("audit_type", "2"); // 认证类型：1-代理商认证 2-客户认证
		params.put("oauth_status", oauth_status);
		params.put("remark", remark);
		params.put("client_id", client_id);
		params.put("type", "1");
		params.put("userId", String.valueOf(userId));
		if (StringUtils.isBlank(agent_id)) {
			params.put("agent_id", "0");
		} else {
			params.put("agent_id", agent_id);
		}
		Map resultMap = null;

		try {
			resultMap = customerAuditService.audit(params);
		} catch (OperationException o) {
			o.printStackTrace();
			logger.error(o.getMessage(), o);
			return ResultVO.failure(o.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return ResultVO.failure();
		}

		if ("success".equals(resultMap.get("result"))) {
			result.setSuccess(true);
			result.setMsg((String) resultMap.get("msg"));
		} else {
			result.setSuccess(false);
			result.setMsg((String) resultMap.get("msg"));
		}
		return result;
	}

	public ResultVO agentAudit(String agent_id, String oauth_status, String remark, Long userId) {
		ResultVO result = ResultVO.failure();
		Map<String, String> params = new HashMap<String, String>();
		params.put("admin_id", String.valueOf(userId));
		params.put("audit_type", "1"); // 认证类型：1-代理商认证 2-客户认证
		params.put("oauth_status", oauth_status);
		params.put("remark", remark);
		params.put("agent_id", agent_id);
		params.put("type", "1");
		params.put("userId", String.valueOf(userId));
		Map resultMap = agentAuditService.audit(params);
		if ("success".equals(resultMap.get("result"))) {
			result.setSuccess(true);
			result.setMsg((String) resultMap.get("msg"));
		} else {
			result.setSuccess(false);
			result.setMsg((String) resultMap.get("msg"));
		}
		return result;
	}

	@Override
	@Transactional("message")
	public R saveClientBalanceAlarm(ClientBalanceAlarmPo clientBalanceAlarm) {
		if (clientBalanceAlarm == null || StringUtils.isBlank(clientBalanceAlarm.getClientid())) {
			return R.error("客户ID不能为空");
		}

		// if (clientBalanceAlarm.getYzmAlarmNumber() == null)
		// {
		// clientBalanceAlarm.setYzmAlarmNumber(500);
		// }
		//
		// if (clientBalanceAlarm.getTzAlarmNumber() == null)
		// {
		// clientBalanceAlarm.setTzAlarmNumber(500);
		// }
		//
		// if (clientBalanceAlarm.getYxAlarmNumber() == null)
		// {
		// clientBalanceAlarm.setYxAlarmNumber(500);
		// }
		//
		// if (clientBalanceAlarm.getGjAlarmAmount() == null)
		// {
		// clientBalanceAlarm.setGjAlarmAmount(new BigDecimal("10"));
		// }

		// 查询客户
		JsmsAccount jsmsAccount = jsmsAccountService.getByClientId(clientBalanceAlarm.getClientid());
		if (jsmsAccount == null) {
			return R.error("客户不存在");
		}

		if (StringUtils.isNotBlank(clientBalanceAlarm.getAlarmEmail())
				&& clientBalanceAlarm.getAlarmEmail().length() > 1000) {
			return R.error("客户余额提醒邮件最长1000");
		}

		if (StringUtils.isNotBlank(clientBalanceAlarm.getAlarmPhone())
				&& clientBalanceAlarm.getAlarmPhone().length() > 1000) {
			return R.error("客户余额提醒手机号最长1000");
		}

		int count;
		if (clientBalanceAlarm.getId() == null) {
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
			count = jsmsClientBalanceAlarmService.insertBatch(list);
			if (count !=0 && count != list.size())
			{
				count = 0;
			}
		} else {
			// 查询客户余额配置
			JsmsClientBalanceAlarm queryCba = new JsmsClientBalanceAlarm();
			queryCba.setClientid(clientBalanceAlarm.getClientid());
			List<JsmsClientBalanceAlarm> clientBalanceAlarms = jsmsClientBalanceAlarmService.findList(queryCba);
			if (Collections3.isEmpty(clientBalanceAlarms))
			{
				logger.debug("客户余额提醒设置修改失败，客户余额提醒设置项为空");
				return R.error("客户余额提醒设置修改失败, 客户余额提醒设置项为空");
			}

			logger.debug("客户余额提醒设置修改：余额提醒设置 {}  原始设置 {}", JSON.toJSONString(clientBalanceAlarm), JSON.toJSONString(clientBalanceAlarms));

			// 1. 修改验证码
			JsmsClientBalanceAlarm balanceAlarm = getJsmsClientBalanceAlarmByType(clientBalanceAlarms, ClientAlarmType.验证码.getValue().intValue());
			if (balanceAlarm == null)
			{
				logger.debug("客户余额提醒设置修改失败，验证码类型的余额提醒设置为空");
				throw new JsmsClientBalanceAlarmException("验证码类型的余额提醒设置修改失败");
			}

			if (!balanceAlarm.getAlarmPhone().equals(clientBalanceAlarm.getAlarmPhone())
					|| !balanceAlarm.getAlarmEmail().equals(clientBalanceAlarm.getAlarmEmail())
					|| !balanceAlarm.getAlarmNumber().equals(clientBalanceAlarm.getYzmAlarmNumber()))
			{
				balanceAlarm.setUpdateTime(Calendar.getInstance().getTime());
				balanceAlarm.setAlarmPhone(clientBalanceAlarm.getAlarmPhone());
				balanceAlarm.setAlarmEmail(clientBalanceAlarm.getAlarmEmail());
				balanceAlarm.setAlarmNumber(clientBalanceAlarm.getYzmAlarmNumber());
				balanceAlarm.setReminderNumber(1);
                balanceAlarm.setResetTime(balanceAlarm.getUpdateTime());

				logger.debug("客户余额提醒设置修改验证码: {}", JSON.toJSONString(balanceAlarm));
				count = jsmsClientBalanceAlarmService.updateSelective(balanceAlarm);
				if (count == 0)
				{
					logger.debug("客户余额提醒设置修改验证码失败，更新数据库条数为0");
					throw new JsmsClientBalanceAlarmException("验证码类型的余额提醒设置修改失败");
				}
			}

			// 2. 修改通知
			balanceAlarm = getJsmsClientBalanceAlarmByType(clientBalanceAlarms, ClientAlarmType.通知.getValue().intValue());
			if (balanceAlarm == null)
			{
				logger.debug("客户余额提醒设置修改失败，通知类型的余额提醒设置为空");
				throw new JsmsClientBalanceAlarmException("通知类型的余额提醒设置修改失败");
			}

			if (!balanceAlarm.getAlarmPhone().equals(clientBalanceAlarm.getAlarmPhone())
					|| !balanceAlarm.getAlarmEmail().equals(clientBalanceAlarm.getAlarmEmail())
					|| !balanceAlarm.getAlarmNumber().equals(clientBalanceAlarm.getTzAlarmNumber()))
			{
				balanceAlarm.setUpdateTime(Calendar.getInstance().getTime());
				balanceAlarm.setAlarmPhone(clientBalanceAlarm.getAlarmPhone());
				balanceAlarm.setAlarmEmail(clientBalanceAlarm.getAlarmEmail());
				balanceAlarm.setAlarmNumber(clientBalanceAlarm.getTzAlarmNumber());
				balanceAlarm.setReminderNumber(1);
                balanceAlarm.setResetTime(balanceAlarm.getUpdateTime());

				logger.debug("客户余额提醒设置修改通知: {}", JSON.toJSONString(balanceAlarm));
				count = jsmsClientBalanceAlarmService.updateSelective(balanceAlarm);
				if (count == 0)
				{
					logger.debug("客户余额提醒设置修改通知失败，更新数据库条数为0");
					throw new JsmsClientBalanceAlarmException("通知类型的余额提醒设置修改失败");
				}
			}

			// 3. 修改营销
			balanceAlarm = getJsmsClientBalanceAlarmByType(clientBalanceAlarms, ClientAlarmType.营销.getValue().intValue());
			if (balanceAlarm == null)
			{
				logger.debug("客户余额提醒设置修改失败，营销类型的余额提醒设置为空");
				throw new JsmsClientBalanceAlarmException("营销类型的余额提醒设置修改失败");
			}

			if (!balanceAlarm.getAlarmPhone().equals(clientBalanceAlarm.getAlarmPhone())
					|| !balanceAlarm.getAlarmEmail().equals(clientBalanceAlarm.getAlarmEmail())
					|| !balanceAlarm.getAlarmNumber().equals(clientBalanceAlarm.getYxAlarmNumber()))
			{
				balanceAlarm.setUpdateTime(Calendar.getInstance().getTime());
				balanceAlarm.setAlarmPhone(clientBalanceAlarm.getAlarmPhone());
				balanceAlarm.setAlarmEmail(clientBalanceAlarm.getAlarmEmail());
				balanceAlarm.setAlarmNumber(clientBalanceAlarm.getYxAlarmNumber());
				balanceAlarm.setReminderNumber(1);
                balanceAlarm.setResetTime(balanceAlarm.getUpdateTime());

				logger.debug("客户余额提醒设置修改营销: {}", JSON.toJSONString(balanceAlarm));
				count = jsmsClientBalanceAlarmService.updateSelective(balanceAlarm);

				if (count == 0)
				{
					logger.debug("客户余额提醒设置修改营销失败，更新数据库条数为0");
					throw new JsmsClientBalanceAlarmException("营销类型的余额提醒设置修改失败");
				}
			}

			// 4. 修改国际
			balanceAlarm = getJsmsClientBalanceAlarmByType(clientBalanceAlarms, ClientAlarmType.国际.getValue().intValue());
			if (balanceAlarm == null)
			{
				logger.debug("客户余额提醒设置修改失败，国际短信的余额提醒设置为空");
				throw new JsmsClientBalanceAlarmException("国际短信的余额提醒设置修改失败");
			}

			if (!balanceAlarm.getAlarmPhone().equals(clientBalanceAlarm.getAlarmPhone())
					|| !balanceAlarm.getAlarmEmail().equals(clientBalanceAlarm.getAlarmEmail())
					|| (balanceAlarm.getAlarmAmount().compareTo(clientBalanceAlarm.getGjAlarmAmount()) != 0))
			{
				balanceAlarm.setUpdateTime(Calendar.getInstance().getTime());
				balanceAlarm.setAlarmPhone(clientBalanceAlarm.getAlarmPhone());
				balanceAlarm.setAlarmEmail(clientBalanceAlarm.getAlarmEmail());
				balanceAlarm.setAlarmAmount(clientBalanceAlarm.getGjAlarmAmount());
				balanceAlarm.setReminderNumber(1);
				balanceAlarm.setResetTime(balanceAlarm.getUpdateTime());

				logger.debug("客户余额提醒设置修改国际短信: {}", JSON.toJSONString(balanceAlarm));
				count = jsmsClientBalanceAlarmService.updateSelective(balanceAlarm);

				if (count == 0)
				{
					logger.debug("客户余额提醒设置修改国际短信失败，更新数据库条数为0");
					throw new JsmsClientBalanceAlarmException("国际短信的余额提醒设置修改失败");
				}
			}

			count = 1;
		}

		return count == 0 ? R.error("客户余额提醒设置失败") : R.ok("客户余额提醒设置成功");
	}

	private JsmsClientBalanceAlarm getJsmsClientBalanceAlarmByType(List<JsmsClientBalanceAlarm> clientBalanceAlarms, int type)
	{
		JsmsClientBalanceAlarm result = null;
		for (JsmsClientBalanceAlarm clientBalanceAlarm : clientBalanceAlarms) {
			if (clientBalanceAlarm.getAlarmType() != null && clientBalanceAlarm.getAlarmType().intValue() == type)
			{
				result = clientBalanceAlarm;
				break;
			}
		}
		return result;
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
		jsmsClientBalanceAlarm.setReminderNumber(1);
		jsmsClientBalanceAlarm.setResetTime(Calendar.getInstance().getTime());
		jsmsClientBalanceAlarm.setCreateTime(jsmsClientBalanceAlarm.getResetTime());
		jsmsClientBalanceAlarm.setUpdateTime(jsmsClientBalanceAlarm.getResetTime());
		return jsmsClientBalanceAlarm;
	}

	public PageContainer queryCustomerStarLevel(Map<String, String> params) {
		Map<String, Object> objectMap = Maps.newHashMap();
		objectMap.putAll(params);
		PageContainer<Map<String, Object>> pageContainer = masterDao.getSearchPage("clientManage.queryCustomerStarLevel",
				"clientManage.queryCustomerStarLevelCount", objectMap);
		return pageContainer;
	}

	@Override
	public List<Map<String, Object>> queryAllCustomerStarLevel(Map<String, String> params) {
		Map<String, Object> objectMap = Maps.newHashMap();
		objectMap.putAll(params);
		List<Map<String, Object>> data = masterDao.getSearchList("clientManage.queryAllCustomerStarLevel", objectMap);
		return data;
	}

	@Override
	public AccountPo getAccountPoOfStarLevel(String clientId) {
		return accountMapper.getAccountPoOfStarLevel(clientId);
	}

	@Override
	public Set<String> findAllListByBelongSales(DataAuthorityCondition dataAuthorityCondition) {
		if (null == dataAuthorityCondition || null == dataAuthorityCondition.getIds() || dataAuthorityCondition.getIds().isEmpty()){
			logger.error("查询该销售下的所有clientid,销售集合参数为空{}", dataAuthorityCondition);
			throw new IllegalArgumentException("参数为空,请检查参数");
		}
		return accountMapper.findAllListByBelongSales(dataAuthorityCondition);
	}

	@Override
	public int getCountByBelongSale(long belongSale, int notStatus) {
		return this.accountMapper.getCountByBelongSale(belongSale, notStatus);
	}

	/**
	 * @param client_id 子账户id
	 * @param operator  操作者(为空则传-1)
	 * @Description: 判断子账户有没有赠送短信, 没有则赠送, 有则不赠送
	 * @Author: tanjiangqiang
	 * @Date: 2018/1/3 - 10:59
	 */
	@Override
	public void isGiveMessage(String client_id, Long operator) {
		JsmsAccount jsmsAccount = jsmsAccountService.getByClientId(client_id);

		// 判断子账户有没有赠送短信
		JsmsUserPropertyLog jsmsUserPropertyLog = jsmsUserPropertyLogMapper.getUserPropertyByClientidAndProperty(Constant.BESTOW_SMS, client_id);
		// 若子账户bestow_sms属性值为1,则已经赠送短信
		if (null != jsmsUserPropertyLog && Constant.BESTOW_SMS_VALUE.equals(jsmsUserPropertyLog.getValue())) {
			logger.info(client_id + "的bestow_sms属性为" + jsmsUserPropertyLog.getValue() + ",本次审核不再赠送短信");
		} else {
			// 赠送测试短信
			// Mod by lpjLiu: 生成订单ID
			List<Long> orderIds = new ArrayList<>();
			for (int j = 0; j < 3; j++) {
				orderIds.add(customerAuditService.getOemAgentOrderId());
			}
			JsmsOemDataConfig oemDataConfig = jsmsOemDataConfigMapper.getOemDataConfig(jsmsAccount.getAgentId());
			if (oemDataConfig == null) {
				logger.debug("代理商：{}------->没有对应的oem资料", jsmsAccount.getAgentId());
			} else {
				// 赠送测试短信
				jsmsAccountFinanceService.giveShortMessage(client_id, jsmsAccount.getPaytype(),
						jsmsAccount.getAgentId(), jsmsAccount.getName(), orderIds, oemDataConfig.getTestProductId(), oemDataConfig.getTestSmsNumber(), oemDataConfig.getId());
				// 判断是否存在bestow_sms属性,存在则更新,否则新增
				if (null == jsmsUserPropertyLog) {
					//新增子账户bestow_sms属性值为1
					this.insertValueByClientidAndProperty(client_id, operator);
				} else {
					//更新子账户bestow_sms属性值为1
					this.updateValueByClientidAndProperty(client_id, operator);
				}
			}
		}
	}

	/**
	 * @param clientid 子账户id
	 * @param operator 操作者(为空则传-1)
	 * @Description: 新增子账户bestow_sms属性为1(1为已赠送短信)
	 * @Author: tanjiangqiang
	 * @Date: 2018/1/3 - 10:03
	 */
	@Override
	public void insertValueByClientidAndProperty(String clientid, Long operator) {
		if (StringUtils.isBlank(clientid)) {
			throw new IllegalArgumentException("clientid错误,请检查");
		}
		if (null == operator) {
			operator = 0L;
		}
		JsmsUserPropertyLog jsmsUserPropertyLog = jsmsUserPropertyLogMapper.getUserPropertyByClientidAndProperty(Constant.BESTOW_SMS, clientid);
		if (null != jsmsUserPropertyLog) {
			logger.error("子账户bestow_sms属性已存在" + JsonUtil.toJson(jsmsUserPropertyLog));
			throw new JsmsUserPropertyLogException("子账户bestow_sms属性已存在");
		}
		JsmsUserPropertyLog insert = new JsmsUserPropertyLog();
		insert.setClientid(clientid);
		insert.setOperator(operator);
		insert.setProperty(Constant.BESTOW_SMS);
		insert.setValue(Constant.BESTOW_SMS_VALUE);
		Date now = new Date();
		insert.setCreateTime(now);
		insert.setEffectDate(now);
		insert.setRemark("子账户审核资质赠送短信(新增)");
		int n = jsmsUserPropertyLogMapper.insert(insert);
		if (n != 1) {
			throw new JsmsUserPropertyLogException("新增子账户bestow_sms属性失败");
		}
	}

	/**
	 * @param clientid 子账户id
	 * @param operator 操作者(为空则传-1)
	 * @Description: 根据子账户id更新子账户bestow_sms属性为1(1为已赠送短信)
	 * @Author: tanjiangqiang
	 * @Date: 2018/1/3 - 10:03
	 */
	@Override
	public void updateValueByClientidAndProperty(String clientid, Long operator){
		if (StringUtils.isBlank(clientid)){
			throw new IllegalArgumentException("clientid错误,请检查");
		}
		if (null == operator) {
			operator = 0L;
		}
		JsmsUserPropertyLog jsmsUserPropertyLog = jsmsUserPropertyLogMapper.getUserPropertyByClientidAndProperty(Constant.BESTOW_SMS, clientid);
		if (Constant.BESTOW_SMS_VALUE.equals(jsmsUserPropertyLog.getValue())){
			logger.error("子账户bestow_sms属性已修改"+JsonUtil.toJson(jsmsUserPropertyLog));
			throw new JsmsUserPropertyLogException("子账户bestow_sms属性已修改");
		}
		logger.info(clientid + "子账户bestow_sms属性为" + JsonUtil.toJson(jsmsUserPropertyLog));
		JsmsUserPropertyLog update = new JsmsUserPropertyLog();
		update.setValue(Constant.BESTOW_SMS_VALUE);
		update.setProperty(Constant.BESTOW_SMS);
		update.setOperator(operator);
		update.setClientid(clientid);
		int n = jsmsUserPropertyLogMapper.updateValueByClientidAndProperty(update);
		if (n != 1) {
			throw new JsmsUserPropertyLogException("更新子账户bestow_sms属性失败");
		}
		logger.info(clientid + "子账户bestow_sms属性更新为" + JsonUtil.toJson(update));
	}
}
