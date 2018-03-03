package com.ucpaas.sms.service.statistic.impl;

import com.jsmsframework.access.access.entity.JsmsClientOperationStatistics;
import com.jsmsframework.access.service.JsmsClientOperationStatisticsService;
import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.enums.Code;
import com.jsmsframework.common.enums.OperatorType;
import com.jsmsframework.common.enums.SmsTypeEnum;
import com.jsmsframework.user.entity.JsmsAccount;
import com.jsmsframework.user.entity.JsmsUser;
import com.jsmsframework.user.service.JsmsAccountService;
import com.jsmsframework.user.service.JsmsUserService;
import com.ucpaas.sms.common.util.Collections3;
import com.ucpaas.sms.common.util.DateUtils;
import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.entity.po.JsmsClientOperationStatisticsPo;
import com.ucpaas.sms.enums.ClientOperation;
import com.ucpaas.sms.service.statistic.ClientOperationStatisticsService;
import com.ucpaas.sms.util.RegexUtils;
import com.ucpaas.sms.util.beans.BeanUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * created by xiaoqingwen on 2018/1/11 10:19 客户运营分析统计
 */
@Service
public class ClientOperationStatisticsServiceImpl implements ClientOperationStatisticsService {

	@Autowired
	private JsmsClientOperationStatisticsService clientOperationStatisticsService;

	@Autowired
	private JsmsAccountService accountService;

	@Autowired
	private JsmsUserService userService;

	@Override
	public R checkSearchCondition(JsmsClientOperationStatisticsPo clientOperationStatistics) {

		if (clientOperationStatistics == null) {
			// 默认填充一个条件
			clientOperationStatistics = new JsmsClientOperationStatisticsPo();
			clientOperationStatistics
					.setBeginTime(String.valueOf(DateUtils.formatDate(Calendar.getInstance().getTime(), "yyyyMM")));
			clientOperationStatistics.setEndTime(clientOperationStatistics.getBeginTime());
			clientOperationStatistics.setOrderByValue(ClientOperation.OrderBy.投诉率由高到低.getValue().toString());
			clientOperationStatistics.setOrderBy(ClientOperation.OrderBy.投诉率由高到低.getDesc());
			return R.ok("当前对象为空，初始化条件", clientOperationStatistics);
		}

		if (clientOperationStatistics.getSmstype() != null
				&& StringUtils.isBlank(SmsTypeEnum.getDescByValue(clientOperationStatistics.getSmstype()))) {
			return R.error(Code.SYS_ERR, "短信类型条件错误");
		}

		if (clientOperationStatistics.getOperatorstype() != null
				&& StringUtils.isBlank(OperatorType.getDescByValue(clientOperationStatistics.getOperatorstype()))) {
			return R.error(Code.SYS_ERR, "运营商类型条件错误");
		}

		if (org.apache.commons.lang3.StringUtils.isNotBlank(clientOperationStatistics.getBeginTime())
				&& !RegexUtils.checkYYYYMM(clientOperationStatistics.getBeginTime().toString())) {
			return R.error(Code.SYS_ERR, "开始日期条件错误");
		}

		if (org.apache.commons.lang3.StringUtils.isNotBlank(clientOperationStatistics.getEndTime())
				&& !RegexUtils.checkYYYYMM(clientOperationStatistics.getEndTime().toString())) {
			return R.error(Code.SYS_ERR, "结束日期条件错误");
		}

		if (clientOperationStatistics.getOrderBy() != null
				&& (!RegexUtils.checkNumber(clientOperationStatistics.getOrderBy())
						|| StringUtils.isBlank(ClientOperation.OrderBy
								.getDescByValue(Integer.parseInt(clientOperationStatistics.getOrderBy()))))) {
			return R.error(Code.SYS_ERR, "排序条件错误");
		}

		if (clientOperationStatistics.getOrderBy() == null)
		{
			clientOperationStatistics.setOrderByValue(ClientOperation.OrderBy.投诉率由高到低.getValue().toString());
			clientOperationStatistics.setOrderBy(ClientOperation.OrderBy.投诉率由高到低.getDesc());
		} else {
			clientOperationStatistics.setOrderByValue(clientOperationStatistics.getOrderBy());
			clientOperationStatistics.setOrderBy(ClientOperation.OrderBy.getDescByValue(Integer.parseInt(clientOperationStatistics.getOrderBy())));
		}

		return null;
	}

	private Map<String, Object> convertParams(JsmsClientOperationStatisticsPo po) {
		Map<String, Object> params = new HashMap<>();

		//应倪大佬要求:当提交条数和明确成功条数同时为0的时候，整条数据不显示
		params.put("submitTotalAndReportsuccess","submitTotalAndReportsuccess");
		if (StringUtils.isNotBlank(po.getClientId())) {
			params.put("clientId", po.getClientId());
		}

		if (po.getBelongSale() != null) {
			params.put("belongSale", po.getBelongSale());
		}

		if (po.getSmstype() != null) {
			params.put("smstype", po.getSmstype().toString());
		}

		if (StringUtils.isNotBlank(po.getOrderBy())) {
			params.put("orderBy", po.getOrderBy());
		}

		if (po.getOperatorstype() != null) {
			params.put("operatorstype", po.getOperatorstype());
		}

		if (po.getBeginTime() != null) {
			params.put("beginTime", po.getBeginTime());
		}

		if (po.getBeginTime() != null) {
			params.put("endTime", po.getEndTime());
		}

		return params;
	}

	private JsmsUser getUser(Long belongSale, List<JsmsUser> users) {
		JsmsUser result = null;
		if (belongSale == null || Collections3.isEmpty(users)) {
			return result;
		}

		for (JsmsUser user : users) {
			if (belongSale.equals(user.getId())) {
				result = new JsmsUser();
				result.setId(user.getId());
				result.setRealname(user.getRealname());
				break;
			}
		}

		return result;
	}

	private JsmsAccount getAccount(String clientId, List<JsmsAccount> accounts) {
		JsmsAccount result = null;
		if (StringUtils.isBlank(clientId) || Collections3.isEmpty(accounts)) {
			return result;
		}

		for (JsmsAccount client : accounts) {
			if (clientId.equals(client.getClientid())) {
				result = new JsmsAccount();
				result.setClientid(client.getClientid());
				result.setName(client.getName());
				break;
			}
		}

		return result;
	}

	@Override
	public List<JsmsClientOperationStatisticsPo> findList(JsmsClientOperationStatisticsPo clientOperationStatistics) {
		List<JsmsClientOperationStatistics> list = this.clientOperationStatisticsService
				.findList(convertParams(clientOperationStatistics));
		if (Collections3.isEmpty(list)) {
			return new ArrayList<>();
		}

		List<JsmsAccount> accounts = getJsmsAccounts(list);
		List<JsmsUser> users = getJsmsUsers(list);
		List<JsmsClientOperationStatisticsPo> pos = buildPoList(list, accounts, users);

		return pos;
	}

	private List<JsmsClientOperationStatisticsPo> buildPoList(List<JsmsClientOperationStatistics> list,
			List<JsmsAccount> accounts, List<JsmsUser> users) {
		List<JsmsClientOperationStatisticsPo> pos = new ArrayList<>();

		JsmsClientOperationStatisticsPo po;
		for (JsmsClientOperationStatistics jsmsClientOperationStatistics : list) {
			po = new JsmsClientOperationStatisticsPo();
			BeanUtil.copyProperties(jsmsClientOperationStatistics, po);
			String date = String.valueOf(po.getDate());
			//处理时间
			if(org.apache.commons.lang3.StringUtils.isNotBlank(date) && date.length()==6){
				po.setDateStr(date.substring(0,4)+"/"+date.substring(4));
			}else{
				po.setDateStr(date);
			}
			JsmsUser user = getUser(jsmsClientOperationStatistics.getBelongSale(), users);
			po.setUser(user);

			JsmsAccount client = getAccount(jsmsClientOperationStatistics.getClientId(), accounts);
			po.setClient(client);
			//处理单价(厘-->元)
			BigDecimal salefee = jsmsClientOperationStatistics.getSalefee();
			po.setSalefee(salefee.divide(BigDecimal.valueOf(1000)));
			pos.add(po);
		}
		return pos;
	}

	private List<JsmsUser> getJsmsUsers(List<JsmsClientOperationStatistics> list) {
		List<JsmsUser> users = null;
		Set<Long> userIds = new HashSet<>();
		for (JsmsClientOperationStatistics operationStatistics : list) {
			if (userIds.contains(operationStatistics.getBelongSale())) {
				continue;
			}

			userIds.add(operationStatistics.getBelongSale());
		}

		if (!Collections3.isEmpty(userIds)) {
			users = userService.getByIds(userIds);
		}
		return users;
	}

	private List<JsmsAccount> getJsmsAccounts(List<JsmsClientOperationStatistics> list) {
		List<JsmsAccount> accounts = null;
		Set<String> clientIds = new HashSet<>();
		for (JsmsClientOperationStatistics operationStatistics : list) {
			if (clientIds.contains(operationStatistics.getClientId())) {
				continue;
			}

			clientIds.add(operationStatistics.getClientId());
		}

		if (!Collections3.isEmpty(clientIds)) {
			// 查询客户
			accounts = accountService.getByIds(clientIds);
		}
		return accounts;
	}

	@Override
	public JsmsPage<JsmsClientOperationStatisticsPo> queryPage(
			JsmsClientOperationStatisticsPo clientOperationStatistics) {
		JsmsPage<JsmsClientOperationStatistics> jsmsPage = new JsmsPage<>();
		jsmsPage.setRows(clientOperationStatistics.getRows());
		jsmsPage.setPage(clientOperationStatistics.getPage());
		jsmsPage.setParams(convertParams(clientOperationStatistics));
		jsmsPage.setOrderByClause(clientOperationStatistics.getOrderBy()+",id");
		JsmsPage<JsmsClientOperationStatistics> wait = clientOperationStatisticsService.queryList(jsmsPage);

		List<JsmsClientOperationStatistics> list = wait.getData();
		if (Collections3.isEmpty(list)) {
			JsmsPage<JsmsClientOperationStatisticsPo> result = new JsmsPage<>();
			BeanUtil.copyProperties(wait, result);
			return result;
		}

		JsmsPage<JsmsClientOperationStatisticsPo> result = new JsmsPage<>();
		BeanUtil.copyProperties(wait, result);

		List<JsmsAccount> accounts = getJsmsAccounts(list);
		List<JsmsUser> users = getJsmsUsers(list);
		List<JsmsClientOperationStatisticsPo> pos = buildPoList(list, accounts, users);
		//将部分数据保留小数点后4位,不需要四舍五入
		if(pos!=null && pos.size()>0){
			List<JsmsClientOperationStatisticsPo> ll=new ArrayList<>();
			for (int i=0;i<pos.size();i++){
				JsmsClientOperationStatisticsPo jsmsClientOperationStatisticsPo = pos.get(i);
				jsmsClientOperationStatisticsPo.setSendSuccessRatioStr(jsmsClientOperationStatisticsPo.getSendSuccessRatio().setScale(4, BigDecimal.ROUND_DOWN).toString());
				jsmsClientOperationStatisticsPo.setComplaintRatioStr(jsmsClientOperationStatisticsPo.getComplaintRatio().setScale(4, BigDecimal.ROUND_DOWN).toString());
				jsmsClientOperationStatisticsPo.setComplaintCoefficientStr(jsmsClientOperationStatisticsPo.getComplaintCoefficient().setScale(4, BigDecimal.ROUND_DOWN).toString());
				jsmsClientOperationStatisticsPo.setComplaintDifferenceStr(jsmsClientOperationStatisticsPo.getComplaintDifference().setScale(4, BigDecimal.ROUND_DOWN).toString());
				jsmsClientOperationStatisticsPo.setSalefeeStr(jsmsClientOperationStatisticsPo.getSalefee().setScale(4, BigDecimal.ROUND_DOWN).toString());
				ll.add(jsmsClientOperationStatisticsPo);
			}
			result.setData(ll);
		}else{
			//数据不存在
			result.setData(pos);
		}
		return result;
	}

	@Override
	public List<Map<String, Object>> ListToMap(List<JsmsClientOperationStatisticsPo> list) {

		List<Map<String, Object>> result = new ArrayList<>();
		if (Collections3.isEmpty(list)) {
			return result;
		}

		Map<String, Object> map = null;
		for (int i = 0; i < list.size(); i++) {
			JsmsClientOperationStatisticsPo po = list.get(i);
			map = new HashMap<>();
			map.put("clientId", po.getClientId());
			map.put("clientName", po.getClient() == null ? "" : po.getClient().getName());
			map.put("operatorstype", OperatorType.getDescByValue(po.getOperatorstype()));
			map.put("smstype", SmsTypeEnum.getDescByValue(po.getSmstype()));
			map.put("submitTotal", po.getSubmitTotal());
			map.put("reportsuccess", po.getReportsuccess());
			map.put("sendSuccessRatio",
					po.getSendSuccessRatio().setScale(4, BigDecimal.ROUND_DOWN).toString() + "%");
			map.put("complaintNumber", po.getComplaintNumber());
			map.put("complaintRatio", po.getComplaintRatio().setScale(4, BigDecimal.ROUND_DOWN).toString() + "%");
			map.put("complaintCoefficient",
					po.getComplaintCoefficient().setScale(4, BigDecimal.ROUND_DOWN).toString());
			map.put("complaintDifference",
					po.getComplaintDifference().setScale(4, BigDecimal.ROUND_DOWN).toString());
			map.put("salefee", po.getSalefee().setScale(4, BigDecimal.ROUND_DOWN).toString());
			map.put("belongSale", po.getUser() == null ? "" : po.getUser().getRealname());
			map.put("date", po.getDate());
			map.put("dateStr",po.getDateStr());
			result.add(map);
		}

		return result;
	}
}
