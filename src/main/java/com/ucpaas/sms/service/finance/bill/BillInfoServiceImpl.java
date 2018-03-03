package com.ucpaas.sms.service.finance.bill;

import com.jsmsframework.user.entity.JsmsAccount;
import com.jsmsframework.user.entity.JsmsUser;
import com.jsmsframework.user.service.JsmsAccountService;
import com.jsmsframework.user.service.JsmsUserService;
import com.ucpaas.sms.common.util.Collections3;
import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.dao.MessageMasterDao;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.entity.po.DataAuthorityCondition;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.AgentInfoService;
import com.ucpaas.sms.util.AgentUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class BillInfoServiceImpl implements BillInfoService {

	@Autowired
	private MessageMasterDao masterDao;

	@Autowired
	private JsmsAccountService accountService;

	@Autowired
	private AgentInfoService agentInfoService;

	@Autowired
	private JsmsUserService userService;

	@Override
	public PageContainer queryBalance(Map<String, Object> params) {
		PageContainer pageContainer = masterDao.getSearchPage("finance.queryBalance", "finance.queryBalanceCount",
				params);
		dealBlanceList(pageContainer.getList());
		return pageContainer;
	}


	@Override
	public PageContainer queryBalanceBill(Map<String, Object> params, UserSession userSession) {
		//获取用户的数据权限
		DataAuthorityCondition dataAuthorityCondition = AgentUtils.getDataAuthorityCondition(userSession.getId(), true, false);
		//Set<Integer> agentIds = agentInfoService.findOEMAgentIdByBelongSales(dataAuthorityCondition);
		Set<Integer> agentIds = agentInfoService.findAgentIdByBelongSales(dataAuthorityCondition);
		if (null == agentIds || agentIds.isEmpty()) {
			return new PageContainer();
		}
		params.put("agentIds", agentIds);
		return this.queryBalance(params);
	}


	@Override
	public PageContainer queryCommission(Map<String, String> params) {
		return masterDao.getSearchPage("finance.queryCommission", "finance.queryCommissionCount", params);
	}

	@Override
	public List<Map<String, Object>> queryCommission4Excel(Map<String, String> params) {
		return masterDao.selectList("finance.queryCommission4Excel", params);
	}

	@Override
	public List<Map<String, Object>> queryBalance4Excel(Map<String, Object> params) {
		List<Map<String, Object>> lists = masterDao.selectList("finance.queryBalance4Excel", params);
		dealBlanceList(lists);
		return lists;
	}

	@Override
	public List<Map<String, Object>> queryBalance4Excel(Map<String, Object> params,UserSession userSession) {
		//获取用户的数据权限
		DataAuthorityCondition dataAuthorityCondition = AgentUtils.getDataAuthorityCondition(userSession.getId(), true, false);
		Set<Integer> agentIds = agentInfoService.findAgentIdByBelongSales(dataAuthorityCondition);
		params.put("agentIds", agentIds);
		if (null == agentIds || agentIds.isEmpty()) {
			new ArrayList<>(1);
		}
		return this.queryBalance4Excel(params);
	}


	/**
	 * 查询返点账单
	 */
	@Override
	public PageContainer queryRebate(Map<String, String> params) {
		return masterDao.getSearchPage("finance.queryRebate", "finance.queryRebateCount", params);
	}

	/**
	 * 查询授信记录
	 */
	@Override
	public PageContainer queryCredit(Map<String, String> params) {
		return masterDao.getSearchPage("finance.queryCredit", "finance.queryCreditCount", params);
	}

	/**
	 * 查询押金账单
	 */
	@Override
	public PageContainer queryDeposit(Map<String, String> params) {
		PageContainer data = masterDao.getSearchPage("finance.queryDeposit", "finance.queryDepositCount", params);
		return data;
	}

	/**
	 * 导出返点账单,查询满足条件的所有数据
	 */
	@Override
	public List<Map<String, Object>> exportRebateExcel(Map<String, String> params) {
		return masterDao.selectList("finance.exportRebateExcel", params);
	}

	/**
	 * 导出押金账单,查询满足条件的所有数据
	 */
	@Override
	public List<Map<String, Object>> exportDepositExcel(Map<String, String> params) {
		return masterDao.selectList("finance.exportDepositExcel", params);
	}

	/**
	 * 导出授信记录,查询满足条件的所有数据
	 */
	@Override
	public List<Map<String, Object>> exportCreditExcel(Map<String, String> params) {
		return masterDao.selectList("finance.exportCreditExcel", params);
	}

	@Override
	public Map balanceTotal(Map<String, Object> params) {
		Map totalData = masterDao.getOneInfo("finance.balanceTotal", params);
		if (totalData == null || totalData.get("sum_amount") == null) {
			totalData = new HashMap<>();
			totalData.put("sum_amount", "0");
			return totalData;
		}
		return totalData;
	}

	@Override
	public Map balanceBillTotal(Map<String, Object> params, UserSession userSession) {
		//获取用户的数据权限
		DataAuthorityCondition dataAuthorityCondition = AgentUtils.getDataAuthorityCondition(userSession.getId(), true, false);
		Set<Integer> agentIds = agentInfoService.findAgentIdByBelongSales(dataAuthorityCondition);
		params.put("agentIds", agentIds);
		if (null == agentIds || agentIds.isEmpty()) {
			return new HashMap(1);
		}
		return this.balanceTotal(params);
	}

	/**
	 * SQL中删除连表语句 单独查询客户和用户帐号
	 */
	private void dealBlanceList(List<Map<String, Object>> lists) {
		if (Collections3.isEmpty(lists)) {
			return;
		}

		Set<String> clientIds = new HashSet<>();
		Set<Long> userIds = new HashSet<>();
		Object obj;
		for (Map<String, Object> list : lists) {
			obj = list.get("client_id");
			String clientId = obj == null ? "" : obj.toString();
			if (StringUtils.isNotBlank(clientId) && !clientId.equals("null")) {
				clientIds.add(clientId);
			}

			// 业务类型
			obj = list.get("payment_type");
			Integer paymentType = obj == null ? null : Integer.parseInt(obj.toString());

			// 获取管理员
			obj = list.get("admin_id");
			Long adminId = obj == null ? null : Long.parseLong(obj.toString());
			if (adminId == null || paymentType.intValue() == 8 || adminId.intValue() == 0) {
				continue;
			}

			userIds.add(adminId);
		}

		// 批量查询帐号
		Map<String, JsmsAccount> accountMap = new HashMap<>();
		if (clientIds.size() > 0) {
			List<JsmsAccount> accounts = accountService.getByIds(clientIds);
			for (JsmsAccount account : accounts) {
				accountMap.put(account.getClientid(), account);
			}
		}

		// 批量查询用户
		Map<Long, JsmsUser> userMap = new HashMap<>();
		if (userIds.size() > 0) {
			List<JsmsUser> users = userService.getByIds(userIds);
			for (JsmsUser user : users) {
				userMap.put(user.getId(), user);
			}
		}

		for (Map<String, Object> list : lists) {
			// 财务类型
			obj = list.get("financial_type");
			Integer financialType = obj == null ? null : Integer.parseInt(obj.toString());
			if (financialType.intValue() == 0) {
				list.put("financial_type_name", "入账");
			} else {
				list.put("financial_type_name", "出账");
			}

			// 业务类型
			obj = list.get("payment_type");
			Integer paymentType = obj == null ? null : Integer.parseInt(obj.toString());
			if (paymentType != null) {
				switch (paymentType) {
				case 0:
					list.put("payment_type_name", "充值");
					break;
				case 1:
					list.put("payment_type_name", "扣减");
					break;
				case 2:
					list.put("payment_type_name", "佣金转余额");
					break;
				case 3:
					list.put("payment_type_name", "购买产品包");
					break;
				case 4:
					list.put("payment_type_name", "退款");
					break;
				case 5:
					list.put("payment_type_name", "赠送");
					break;
				case 6:
					list.put("payment_type_name", "后付费客户消耗");
					break;
				case 7:
					list.put("payment_type_name", "回退条数");
					break;
				case 8:
					list.put("payment_type_name", "后付费客户失败返还");
					break;
				case 9:
					list.put("payment_type_name", "增加授信");
					break;
				case 10:
					list.put("payment_type_name", "降低授信");
					break;
				case 11:
					list.put("payment_type_name", "在线支付");
					break;
				default:
					list.put("payment_type_name", "");
					break;
				}
			}

			// 获取客户Id
			obj = list.get("client_id");
			String clientId = obj == null ? "" : obj.toString();

			String name = "";
			if (accountMap.size() > 0) {
				JsmsAccount account = accountMap.get(clientId);
				if (account != null) {
					name = account.getName();
				}
			}
			list.put("name", name);

			// 获取管理员
			obj = list.get("admin_id");
			Long adminId = obj == null ? null : Long.valueOf(obj.toString());

			String userName = "";
			if (paymentType != null && adminId != null) {
				if (paymentType.intValue() == 8 || adminId.intValue() == 0) {
					userName = "系统";
				} else {
					if (userMap.size() > 0) {
						JsmsUser user = userMap.get(adminId);
						if (user != null) {
							userName = user.getRealname();
						}
					}
				}
			}
			list.put("admin_id", userName);
			if(list.get("payment_type")!=null&&list.get("payment_type").toString().equals("11")){
				list.put("order_id",list.get("paymentId"));
			}
		}
	}

}
