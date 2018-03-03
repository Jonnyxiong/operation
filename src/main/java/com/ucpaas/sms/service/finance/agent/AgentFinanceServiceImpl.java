package com.ucpaas.sms.service.finance.agent;

import com.jsmsframework.common.dto.JsmsPage;
import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.constant.LogConstant.LogType;
import com.ucpaas.sms.dao.MessageMasterDao;
import com.ucpaas.sms.enums.LogEnum;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.common.LogService;
import com.ucpaas.sms.service.product.ProductServiceImpl;
import com.ucpaas.sms.util.CommonUtil;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.ucpaas.sms.entity.po.JsmsOnlinePaymentPo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ucpaas.sms.mapper.message.OnlinePaymentMapper;

import java.beans.PropertyDescriptor;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Transactional
public class AgentFinanceServiceImpl implements AgentFinanceService{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);

	@Autowired
	private MessageMasterDao masterDao;
	@Autowired
	private LogService logService;

	@Autowired
	private OnlinePaymentMapper onlinePaymentMapper;

	
	@Override
	public PageContainer query(Map<String, String> params) {

		PageContainer page=new PageContainer();

		page=masterDao.getSearchPage("agentFinance.queryAgentFinance", "agentFinance.queryAgentFinanceCount", params);

		List<Map<String, Object>> lists=page.getList();

		if(null!=params.get("create_time")){


		Map<String,Object> totals=masterDao.getOneInfo("agentFinance.queryAgentFinanceTotal",params);

		if( totals !=null){

			Double balance_total=Double.parseDouble(String.valueOf(totals.get("balance_total")==null?"0.00":totals.get("balance_total")));
			Double commission_income_total=Double.parseDouble(String.valueOf(totals.get("commission_income_total")==null?"0.00":totals.get("commission_income_total")));
			Double rebate_income_total=Double.parseDouble(String.valueOf(totals.get("rebate_income_total")==null?"0.00":totals.get("rebate_income_total")));
			Double deposit_total=Double.parseDouble(String.valueOf(totals.get("deposit_total")==null?"0.00":totals.get("deposit_total")));
//			Double amount_total= Double.parseDouble(String.valueOf(totals.get("amount_total")==null?"0.00":totals.get("amount_total"))) ;


		for (Map<String, Object> map:lists) {
		Map<String,Object> sparam=new HashedMap();
		sparam.put("agent_id",map.get("agent_id"));
		sparam.put("create_time",params.get("create_time"));
			Double ba=Double.parseDouble(String.valueOf(map.get("balance")==null?"0.00":map.get("balance")))*1000000;//当前余额
			Double com=Double.parseDouble(String.valueOf(map.get("commission_income")==null?"0.00":map.get("commission_income")))*1000000;	//当前佣金
			Double dep=Double.parseDouble(String.valueOf(map.get("deposit")==null?"0.00":map.get("deposit")))*1000000;	//当前押金
			Double reb=Double.parseDouble(String.valueOf(map.get("rebate_income")==null?"0.00":map.get("rebate_income")))*1000000;	//当前返点
		//1.根据时间对比余额收支明细表
		List<Map<String,Object>> balances=masterDao.getSearchList("finance.queryBalancebyagentIdAndDate",sparam);
		if(balances.size()>0 &&  !balances.isEmpty()){
			for (Map<String, Object> balance : balances) {

				Double bam=Double.parseDouble(String.valueOf(balance.get("amount")==null?"0.00":balance.get("amount")))*1000000;	//历史金额
				Double bahis=Double.parseDouble(String.valueOf(balance.get("balance")==null?"0.00":balance.get("balance")))*1000000; //历史余额

				map.put("balance",bahis/1000000);
				balance_total -=ba/1000000;
				balance_total +=bahis/1000000;
				break;

			}

		}else{
			map.put("balance","0.00");
			balance_total -=ba/1000000;


		}
		// 2.根据时间对比佣金收支明细表

			List<Map<String,Object>> commissions=masterDao.getSearchList("finance.queryCommissionbyagentIdAndDate",sparam);
			if(commissions.size()>0 &&  !commissions.isEmpty()){
				for (Map<String, Object> commission : commissions) {

					Double comb=Double.parseDouble(String.valueOf(commission.get("amount")==null?"0.00":commission.get("amount")))*1000000;	//历史金额
					Double combhis=Double.parseDouble(String.valueOf(commission.get("balance")==null?"0.00":commission.get("balance")))*1000000;	//历史余额

					map.put("commission_income",combhis/1000000);
					balance_total -=com/1000000;
					balance_total +=combhis/1000000;
					break;
				}

			}else{
				map.put("commission_income","0.00");
				balance_total -=com/1000000;
			}

		// 3.根据时间对比押金表

			List<Map<String,Object>> deposits=masterDao.getSearchList("finance.queryDepositbyagentIdAndDate",sparam);
			if(deposits.size()>0 &&  !deposits.isEmpty()){
				for (Map<String, Object> deposit : deposits) {

					Double deph=Double.parseDouble(String.valueOf(deposit.get("amount")==null?"0.00":deposit.get("amount")))*1000000;	//历史金额
					Double dephis=Double.parseDouble(String.valueOf(deposit.get("balance")==null?"0.00":deposit.get("balance")))*1000000;	//历史余额

					map.put("deposit",dephis/1000000);
					balance_total -=dep/1000000;
					balance_total +=dephis/1000000;
					break;

				}

			}else{
				map.put("deposit","0.00");
				balance_total -=dep/1000000;
			}
		//4.根据时间对比返点表

			List<Map<String,Object>> rebates=masterDao.getSearchList("finance.queryRebatebyagentIdAndDate",sparam);
			if(rebates.size()>0 &&  !rebates.isEmpty()){
				for (Map<String, Object> rebate : rebates) {

					Double rebh=Double.parseDouble(String.valueOf(rebate.get("amount")==null?"0.00":rebate.get("amount")))*1000000;	//历史金额
					Double rebhis=Double.parseDouble(String.valueOf(rebate.get("balance")==null?"0.00":rebate.get("balance")))*1000000;	//历史余额

					map.put("rebate_income",rebhis/1000000);
					balance_total -=reb/1000000;
					balance_total +=rebhis/1000000;
					break;

				}

			}else{
				map.put("rebate_income","0.00");
				balance_total -=reb/1000000;
			}





		}

		totals.put("balance_total",balance_total.toString());
		totals.put("commission_income_total",commission_income_total.toString());
		totals.put("rebate_income_total",rebate_income_total.toString());
		totals.put("deposit_total",deposit_total.toString());

		page.setBalanceTotal(balance_total);
		page.setCommissionTotal(commission_income_total);
		page.setDepositTotal(deposit_total);
		page.setRebateTotal(rebate_income_total);
		}
		}
		return page;
	}

	@Override
	public Map<String, Object> sumTotal(Map<String, String> params) {


		params.put("limit","");
//		page.setPageRowCount(999999999);

		List<Map<String,Object>> lists=masterDao.getSearchList("agentFinance.queryAgentFinance", params);

		Map<String, Object> totals = masterDao.getOneInfo("agentFinance.queryAgentFinanceTotal", params);


		if(null!=params.get("create_time")) {


			if (totals !=null) {

				Double balance_total = Double.parseDouble(String.valueOf(totals.get("balance_total") == null ? "0.00" : totals.get("balance_total")));
				Double commission_income_total = Double.parseDouble(String.valueOf(totals.get("commission_income_total") == null ? "0.00" : totals.get("commission_income_total")));
				Double rebate_income_total = Double.parseDouble(String.valueOf(totals.get("rebate_income_total") == null ? "0.00" : totals.get("rebate_income_total")));
				Double deposit_total = Double.parseDouble(String.valueOf(totals.get("deposit_total") == null ? "0.00" : totals.get("deposit_total")));


				for (Map<String, Object> map : lists) {
					Map<String, Object> sparam = new HashedMap();
					sparam.put("agent_id", map.get("agent_id"));
					sparam.put("create_time", params.get("create_time"));

					Double ba=Double.parseDouble(String.valueOf(map.get("balance")==null?"0.00":map.get("balance")))*1000000;//当前余额
					Double com=Double.parseDouble(String.valueOf(map.get("commission_income")==null?"0.00":map.get("commission_income")))*1000000;	//当前佣金
					Double dep=Double.parseDouble(String.valueOf(map.get("deposit")==null?"0.00":map.get("deposit")))*1000000;	//当前押金
					Double reb=Double.parseDouble(String.valueOf(map.get("rebate_income")==null?"0.00":map.get("rebate_income")))*1000000;	//当前返点
					//1.根据时间对比余额收支明细表
					List<Map<String,Object>> balances=masterDao.getSearchList("finance.queryBalancebyagentIdAndDate",sparam);




					if(balances.size()>0 &&  !balances.isEmpty()){
						for (Map<String, Object> balance : balances) {

							Double bam=Double.parseDouble(String.valueOf(balance.get("amount")==null?"0.00":balance.get("amount")))*1000000;	//历史金额
							Double bahis=Double.parseDouble(String.valueOf(balance.get("balance")==null?"0.00":balance.get("balance")))*1000000; //历史余额

							map.put("balance",bahis/1000000);
							balance_total -=ba/1000000;
							balance_total +=bahis/1000000;
							break;



						}

					}else{
						map.put("balance","0.00");
						balance_total -=ba/1000000;

					}
					// 2.根据时间对比佣金收支明细表

					List<Map<String,Object>> commissions=masterDao.getSearchList("finance.queryCommissionbyagentIdAndDate",sparam);
					if(commissions.size()>0 &&  !commissions.isEmpty()){
						for (Map<String, Object> commission : commissions) {

							Double comb=Double.parseDouble(String.valueOf(commission.get("amount")==null?"0.00":commission.get("amount")))*1000000;	//历史金额
							Double combhis=Double.parseDouble(String.valueOf(commission.get("balance")==null?"0.00":commission.get("balance")))*1000000;	//历史余额

							map.put("commission_income",combhis/1000000);
							commission_income_total -=com/1000000;
							commission_income_total +=combhis/1000000;
							break;


						}

					}else{
						map.put("commission_income","0.00");
						commission_income_total -=com/1000000;
					}

					// 3.根据时间对比押金表

					List<Map<String,Object>> deposits=masterDao.getSearchList("finance.queryDepositbyagentIdAndDate",sparam);
					if(deposits.size()>0 &&  !deposits.isEmpty()){
						for (Map<String, Object> deposit : deposits) {

							Double deph=Double.parseDouble(String.valueOf(deposit.get("amount")==null?"0.00":deposit.get("amount")))*1000000;	//历史金额
							Double dephis=Double.parseDouble(String.valueOf(deposit.get("balance")==null?"0.00":deposit.get("balance")))*1000000;	//历史余额

							map.put("deposit",dephis/1000000);
							deposit_total -=dep/1000000;
							deposit_total +=dephis/1000000;
							break;


						}

					}else{
						map.put("deposit","0.00");
						deposit_total -=dep/1000000;
					}
					//4.根据时间对比返点表

					List<Map<String,Object>> rebates=masterDao.getSearchList("finance.queryRebatebyagentIdAndDate",sparam);
					if(rebates.size()>0 &&  !rebates.isEmpty()){
						for (Map<String, Object> rebate : rebates) {

							Double rebh=Double.parseDouble(String.valueOf(rebate.get("amount")==null?"0.00":rebate.get("amount")))*1000000;	//历史金额
							Double rebhis=Double.parseDouble(String.valueOf(rebate.get("balance")==null?"0.00":rebate.get("balance")))*1000000;	//历史余额

							map.put("rebate_income",rebhis/1000000);
							rebate_income_total -=reb/1000000;
							rebate_income_total +=rebhis/1000000;
							break;

						}

					}else{
						map.put("rebate_income","0.00");
						rebate_income_total -=reb/1000000;
					}





				}
				if(lists.size()>0){
					totals.put("balance_total", balance_total.toString());
					totals.put("commission_income_total", commission_income_total.toString());
					totals.put("rebate_income_total", rebate_income_total.toString());
					totals.put("deposit_total", deposit_total.toString());
				}else{
					totals.put("balance_total", "0.00");
					totals.put("commission_income_total", "0.00");
					totals.put("rebate_income_total", "0.00");
					totals.put("deposit_total","0.00");
				}





			}else {

				Map<String,Object> total=new HashedMap();

				total.put("balance_total", "0.00");
				total.put("commission_income_total", "0.00");
				total.put("rebate_income_total", "0.00");
				total.put("deposit_total","0.00");
				return total;
			}

		}



		return totals;
	}


	@Override
	public List<Map<String, Object>> queryAll(Map<String, String> params) {

		List<Map<String, Object>> alldata=masterDao.getSearchList("agentFinance.queryALLAgentFinance", params);
		Map<String,Object> totals=masterDao.getOneInfo("agentFinance.queryAgentFinanceTotal",params);
		if(null!=params.get("create_time")){
		if(totals !=null){


			Double balance_total=Double.parseDouble(String.valueOf(totals.get("balance_total")==null?"0.00":totals.get("balance_total")));
			Double commission_income_total=Double.parseDouble(String.valueOf(totals.get("commission_income_total")==null?"0.00":totals.get("commission_income_total")));
			Double rebate_income_total=Double.parseDouble(String.valueOf(totals.get("rebate_income_total")==null?"0.00":totals.get("rebate_income_total")));
			Double deposit_total=Double.parseDouble(String.valueOf(totals.get("deposit_total")==null?"0.00":totals.get("deposit_total")));

			for (Map<String, Object> map:alldata) {
				Map<String,Object> sparam=new HashedMap();
				sparam.put("agent_id",map.get("agent_id"));
				sparam.put("create_time",params.get("create_time"));
				Double ba=Double.parseDouble(String.valueOf(map.get("balance")==null?"0.00":map.get("balance")))*1000000;//当前余额
				Double com=Double.parseDouble(String.valueOf(map.get("commission_income")==null?"0.00":map.get("commission_income")))*1000000;	//当前佣金
				Double dep=Double.parseDouble(String.valueOf(map.get("deposit")==null?"0.00":map.get("deposit")))*1000000;	//当前押金
				Double reb=Double.parseDouble(String.valueOf(map.get("rebate_income")==null?"0.00":map.get("rebate_income")))*1000000;	//当前返点
				//1.根据时间对比余额收支明细表
				List<Map<String,Object>> balances=masterDao.getSearchList("finance.queryBalancebyagentIdAndDate",sparam);
				if(balances.size()>0 &&  !balances.isEmpty()){
					for (Map<String, Object> balance : balances) {

						Double bam=Double.parseDouble(String.valueOf(balance.get("amount")==null?"0.00":balance.get("amount")))*1000000;	//历史金额
						Double bahis=Double.parseDouble(String.valueOf(balance.get("balance")==null?"0.00":balance.get("balance")))*1000000; //历史余额

						map.put("balance",bahis/1000000);
						balance_total -=ba/1000000;
						balance_total +=bahis/1000000;
						break;



					}

				}else{
					map.put("balance","0.00");
					balance_total -=ba/1000000;

				}
				// 2.根据时间对比佣金收支明细表

				List<Map<String,Object>> commissions=masterDao.getSearchList("finance.queryCommissionbyagentIdAndDate",sparam);
				if(commissions.size()>0 &&  !commissions.isEmpty()){
					for (Map<String, Object> commission : commissions) {

						Double comb=Double.parseDouble(String.valueOf(commission.get("amount")==null?"0.00":commission.get("amount")))*1000000;	//历史金额
						Double combhis=Double.parseDouble(String.valueOf(commission.get("balance")==null?"0.00":commission.get("balance")))*1000000;	//历史余额

						map.put("commission_income",combhis/1000000);
						balance_total -=com/1000000;
						balance_total +=combhis/1000000;
						break;


					}

				}else{
					map.put("commission_income","0.00");
					balance_total -=com/1000000;
				}

				// 3.根据时间对比押金表

				List<Map<String,Object>> deposits=masterDao.getSearchList("finance.queryDepositbyagentIdAndDate",sparam);
				if(deposits.size()>0 &&  !deposits.isEmpty()){
					for (Map<String, Object> deposit : deposits) {

						Double deph=Double.parseDouble(String.valueOf(deposit.get("amount")==null?"0.00":deposit.get("amount")))*1000000;	//历史金额
						Double dephis=Double.parseDouble(String.valueOf(deposit.get("balance")==null?"0.00":deposit.get("balance")))*1000000;	//历史余额

						map.put("deposit",dephis/1000000);
						balance_total -=dep/1000000;
						balance_total +=dephis/1000000;
						break;


					}

				}else{
					map.put("deposit","0.00");
					balance_total -=dep/1000000;
				}
				//4.根据时间对比返点表

				List<Map<String,Object>> rebates=masterDao.getSearchList("finance.queryRebatebyagentIdAndDate",sparam);
				if(rebates.size()>0 &&  !rebates.isEmpty()){
					for (Map<String, Object> rebate : rebates) {

						Double rebh=Double.parseDouble(String.valueOf(rebate.get("amount")==null?"0.00":rebate.get("amount")))*1000000;	//历史金额
						Double rebhis=Double.parseDouble(String.valueOf(rebate.get("balance")==null?"0.00":rebate.get("balance")))*1000000;	//历史余额

						map.put("rebate_income",rebhis/1000000);
						balance_total -=reb/1000000;
						balance_total +=rebhis/1000000;
						break;

					}

				}else{
					map.put("rebate_income","0.00");
					balance_total -=reb/1000000;
				}





			}

		}
		}

		return alldata;
	}

	@Override
	public Map<String, Object> getAgentInfoByAgentID(int agent_id) {
		return masterDao.getOneInfo("agentFinance.getAgentInfoByAgentID", agent_id);
	}

	/**
	 * 余额操作,修改余额 并添加余额账单信息
	 */
	@Override
	public Map<String, Object> balanceAction(Map<String, String> params) {
		Map<String, Object> data = new HashMap<String, Object>();
		String operateType = params.get("operateType");// 充值 退款 赠送 扣减
		if(params.get("operateAmount") == null || params.get("operateAmount") == ""){
			data.put("result", "fail");
			data.put("msg", "请输入金额");
			return data;
		}
		if(params.get("agentType") != null && "1".equals(params.get("agentType")) && params.get("clientId") == null){
			data.put("result", "fail");
			data.put("msg", "<b>请选择待"+operateType+ "客户</b>");
			return data;
		}
		// 判断客户状态
		if(params.get("agentType") != null && "1".equals(params.get("agentType")) ){
			Map<String,Object> clientInfo = masterDao.getOneInfo("agentFinance.getClientById", params);
			if(clientInfo != null && !clientInfo.get("status").equals(1)){
				data.put("result", "fail");
				data.put("msg", "<b>待"+operateType+ "客户状态："+clientInfo.get("status_name")+"</b>");
				return data;
			}
			if(clientInfo != null &&  !clientInfo.get("oauth_status").equals(3)){
				data.put("result", "fail");
				data.put("msg", "<b>待"+operateType+ "客户认证状态："+clientInfo.get("oauth_status_name")+"</b>");
				return data;
			}
			
		
		}
		int paymentType = -1;// 业务类型，0：充值，1：扣减，2：佣金转余额，3：购买产品包，4：退款，5：赠送
		int financialType = -1;//0：入账，1：出账
		LOGGER.debug("代理商余额{}：{}",operateType,params);
		int i = -1;
		if(operateType != null && "充值".equals(operateType)){	//0 余额充值
			paymentType = 0;
			financialType = 0;
			i = masterDao.update("agentFinance.updateAgentBalance", params);
		}else if(operateType != null && "赠送".equals(operateType)){	//5 余额赠送
			paymentType = 5;
			financialType = 0;
			i = masterDao.update("agentFinance.updateAgentBalance", params);
		}else if(operateType != null && "扣减".equals(operateType)){	//1 余额扣减 
			paymentType = 1;
			financialType = 1;
			params.put("operateAmount","-" + params.get("operateAmount"));
			i = masterDao.update("agentFinance.updateAgentBalance", params);
		}else if(operateType != null && "退款".equals(operateType)){	//4 余额退款
			Map<String,Object> agentAccount = masterDao.getOneInfo("agentFinance.getAgentAccout", params);
			BigDecimal decimal =  (BigDecimal) agentAccount.get("balance");
			double balance = decimal != null ? decimal.doubleValue()  : -1;
			BigDecimal balance1=BigDecimal.valueOf(balance*1000000);
			BigDecimal operateAmount=BigDecimal.valueOf(Double.parseDouble(params.get("operateAmount"))*1000000);
			if(balance1.compareTo(operateAmount)==1|| balance1.compareTo(operateAmount)==0){
				paymentType = 4;
				financialType = 1;
				params.put("balance","" + balance);
				params.put("operateAmount","-" + params.get("operateAmount"));
				i = masterDao.update("agentFinance.updateAgentBalance", params);
			}else{
				data.put("result", "fail");
				data.put("msg","余额不足");
				return data;
			}
		}
		// 添加余额账单信息
		if (i > 0) {
			//生成流水记录
			Map<String,Object> insertParams = new HashMap<>();
			insertParams.putAll(params);
			insertParams.put("payment_type", paymentType);//业务类型，0：充值，1：扣减，2：佣金转余额，3：购买产品包，4：退款，5：赠送
			insertParams.put("financial_type", financialType);//财务类型，0：入账，1：出账',
			insertParams.put("admin_id",params.get("userId"));
			int insertNum = masterDao.insert("agentFinance.insertAgentBalanceBill", insertParams);
			if(insertNum > 0){
				data.put("result", "success");
				data.put("msg",operateType + "成功");
			}else{
				throw new RuntimeException("添加余额账单信息失败");
			}
		} else {
			data.put("result", "fail");
			data.put("msg",operateType + "失败");
		}
		


		Long userId = Long.valueOf(params.get("userId"));
		String pageUrl = params.get("pageUrl");
		String ip = params.get("ip"); 
		logService.add(LogType.update, LogEnum.财务管理.getValue(), userId, pageUrl, ip, "财务管理-客户财务-余额操作:" + operateType, params, data);

		return data;
	}

	/**
	 * 获取代理商的指定的条件的客户 
	 */
	@Override
	public List<Map<String,Object>> getClientByCondition(Map<String, String> params) {
		return masterDao.getSearchList("agentFinance.getClientByCondition", params);
	}


	/**
	 * 获取代理商的指定的条件的客户
	 */
	@Override
	public Map<String,Object> getClientByCondition1(Map<String, String> params) {
		return masterDao.getOneInfo("agentFinance.getClientByCondition1", params);
	}
	@Override
	public JsmsPage initOnlinePayMent(JsmsPage page) {
		List<JsmsOnlinePaymentPo> ls=onlinePaymentMapper.queryListPo(page);

		page.setData(ls);
		return page;

	}
	@Override
	public JsmsPage initOnlinePayMentParam(Map<String, String> params) {
		JsmsPage jpage=new JsmsPage();
		Map<String, String> paramPo=new HashMap<>();
		if(!StringUtils.isBlank(params.get("rows"))) {
			jpage.setRows(Integer.parseInt(params.get("rows")));
		}
		if(!StringUtils.isBlank(params.get("page")) ){
			jpage.setPage(Integer.parseInt(params.get("page")));
		}

		if(!StringUtils.isBlank(params.get("idOrAmount"))) {
			paramPo.put("idOrAmount",params.get("idOrAmount"));
		}
		if(!StringUtils.isBlank(params.get("idOrNameOrSaler"))) {
			paramPo.put("idOrNameOrSaler",params.get("idOrNameOrSaler"));
		}

		if(!StringUtils.isBlank(params.get("payTimeStart"))) {
			paramPo.put("payTimeStart",params.get("payTimeStart"));
		}
		if(!StringUtils.isBlank(params.get("payTimeEnd"))) {
			paramPo.put("payTimeEnd",params.get("payTimeEnd"));
		}
		if(!StringUtils.isBlank(params.get("paymentState"))) {
			paramPo.put("paymentState",params.get("paymentState"));
			if(params.get("paymentState").toString().equals("2")){
				paramPo.put("paymentState",null);
				paramPo.put("paymentStates", "2,5,6");
			}
		}
		if(!StringUtils.isBlank(params.get("paymentMode"))) {
			paramPo.put("paymentMode", params.get("paymentMode"));
		}
		jpage.setOrderByClause("create_time desc");
		jpage.setParams(paramPo);
		return jpage;

	}
	/**
	 * 押金操作,修改押金 并添加押金信息
	 */
	@Override
	public Map<String, Object> depositAction(Map<String, String> params) {
		Map<String, Object> data = new HashMap<String, Object>();
		if(params.get("operateAmount") == null || params.get("operateAmount") == ""){
			data.put("result", "fail");
			data.put("msg", "请输入金额");
			return data;
		}
		String operateType = params.get("operateType");// 充值 退款 扣减
		int paymentType = -1; // 业务类型,0:押金充值,1押金扣减,2:押金退款
		int financialType = -1;//财务类型,0:入账,1:出账
		LOGGER.debug("代理商押金{}：{}",operateType,params);
		int i = -1;
		if(operateType != null && "充值".equals(operateType)){	//0 押金充值
			paymentType = 0;
			financialType = 0;
			i = masterDao.update("agentFinance.updateAgentDeposit", params);
		}else if(operateType != null && "扣减".equals(operateType)){	//1 押金扣减 
			Map<String,Object> agentAccount = masterDao.getOneInfo("agentFinance.getAgentAccout", params);
			BigDecimal decimal =  (BigDecimal) agentAccount.get("deposit");
			double deposit = decimal != null ? decimal.doubleValue() : -1;
			BigDecimal deposit1=BigDecimal.valueOf(deposit*1000000);
			BigDecimal operateAmount=BigDecimal.valueOf(Double.parseDouble(params.get("operateAmount"))*1000000);
			if(deposit1.compareTo(operateAmount)==1|| deposit1.compareTo(operateAmount)==0){
				paymentType = 1;
				financialType = 1;
				params.put("operateAmount","-" + params.get("operateAmount"));
				i = masterDao.update("agentFinance.updateAgentDeposit", params);
			}else{
				data.put("result", "fail");
				data.put("msg","押金不足");
				return data;
			}
		}else if(operateType != null && "退款".equals(operateType)){	//2 押金退款
			Map<String,Object> agentAccount = masterDao.getOneInfo("agentFinance.getAgentAccout", params);
			BigDecimal decimal =  (BigDecimal) agentAccount.get("deposit");
			double deposit = decimal != null ? decimal.doubleValue() : -1;
			BigDecimal deposit1=BigDecimal.valueOf(deposit*1000000);
			BigDecimal operateAmount=BigDecimal.valueOf(Double.parseDouble(params.get("operateAmount"))*1000000);
			if(deposit1.compareTo(operateAmount)==1|| deposit1.compareTo(operateAmount)==0){
				paymentType = 2;
				financialType = 1;
				params.put("deposit","" + deposit);
				params.put("operateAmount","-" + params.get("operateAmount"));
				i = masterDao.update("agentFinance.updateAgentDeposit", params);
			}else{
				data.put("result", "fail");
				data.put("msg","押金不足");
				return data;
			}
		}
		// 添加押金账单信息
		if (i > 0) {
			//生成流水记录
			Map<String,Object> insertParams = new HashMap<>();
			insertParams.putAll(params);
			insertParams.put("payment_type", paymentType);//支付类型，0：运营转入，2：佣金转入，3：订单支付，4：转结算账户',
			insertParams.put("financial_type", financialType);//财务类型，0：入账，1：出账',
			insertParams.put("admin_id", params.get("userId"));
			int insertNum = masterDao.insert("agentFinance.insertAgentDepositBill", insertParams);
			if(insertNum > 0){
				data.put("result", "success");
				data.put("msg",operateType + "成功");
			}else{
				throw new RuntimeException("添加押金账单信息失败");
			}
		} else {
			data.put("result", "fail");
			data.put("msg",operateType + "失败");
		}
		
//		logService.add(LogType.update,LogEnum.财务管理.getValue(), "财务管理-代理商财务-押金操作：" + operateType, params, data);
		Long userId = Long.valueOf(params.get("userId"));
		String pageUrl = params.get("pageUrl");
		String ip = params.get("ip"); 
		logService.add(LogType.update, LogEnum.财务管理.getValue(), userId, pageUrl, ip, "财务管理-客户财务-押金操作：" + operateType, params, data);

		return data;
	}
	@Override
	public JsmsPage initOnlinePayMent4Export(JsmsPage page) {
		List<JsmsOnlinePaymentPo> ls=onlinePaymentMapper.queryListPo(page);

		List<Map> ls2=new ArrayList<>();
		if(ls!=null)
			for(JsmsOnlinePaymentPo jsmsOnlinePaymentPo:ls){
				ls2.add(getValue(jsmsOnlinePaymentPo));
			}
		page.setData(ls2);
		return page;

	}
	private  Map getValue(Object obj)
	{
		Map<String, Object> params = new HashMap<String, Object>(0);
		try {
			PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
			PropertyDescriptor[] descriptors = propertyUtilsBean.getPropertyDescriptors(obj);
			for (int i = 0; i < descriptors.length; i++) {
				String name = descriptors[i].getName();
				if (!"class".equals(name)) {

					params.put(name, propertyUtilsBean.getNestedProperty(obj, name));
					if(name.equals("paymentState")){
						if(params.get("paymentState").toString().equals("0")){
							params.put("paymentState","待支付");
						}else if(params.get("paymentState").toString().equals("1")){
							params.put("paymentState","支付已提交");
						}else if(params.get("paymentState").toString().equals("3")){
							params.put("paymentState","支付失败");
						}else if(params.get("paymentState").toString().equals("4")){
							params.put("paymentState","支付已取消");
						}else if(params.get("paymentState").toString().equals("2")){
							params.put("paymentState","支付成功");
						}
					}
					if(name.equals("paymentMode")){
						if(params.get("paymentMode").toString().equals("0")){
							params.put("paymentMode","支付宝");
						}else if (params.get("paymentMode").toString().equals("1")){
							params.put("paymentMode","微信");
						}
					}
					if(name.equals("payTime")&&!CommonUtil.isObjectEmptyString(params.get("payTime"))){
						SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
						String startTime = sdf.format(params.get("payTime"));
						params.put("payTime",startTime);
					}

				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return params;
	}


	/**
	 * 授信操作,修改授信额
	 */
	@Override
	public Map<String, Object> createAction(Map<String, String> params) {
		Map<String, Object> data = new HashMap<String, Object>();
		if(params.get("operateAmount") == null || "".equals(params.get("operateAmount"))){
			data.put("result", "fail");
			data.put("msg", "请输入金额");
			return data;
		}
		int i = -1;
		Map<String,Object> agentAccount = masterDao.getOneInfo("agentFinance.getAgentAccout", params);
		BigDecimal decimal =  (BigDecimal) agentAccount.get("balance");
		double balance = decimal != null ? decimal.doubleValue() : -1;
		BigDecimal balance1=BigDecimal.valueOf(balance*1000000);
		BigDecimal operateAmount=BigDecimal.valueOf(Double.parseDouble(params.get("operateAmount"))*1000000);
		if(balance1.add(operateAmount) .compareTo(BigDecimal.ZERO)!=-1){
			params.put("balance","" + balance);
			i = masterDao.update("agentFinance.updateAgentCredit", params);
		}else{
			data.put("result", "fail");
			data.put("msg","授信额不抵余额负债!");
			return data;
		}
		// 添加押金账单信息
		if (i > 0) {
			//生成流水记录
			Map<String,Object> insertParams = new HashMap<>();
			insertParams.putAll(params);
			insertParams.put("payment_type", 0); // 业务类型,0:授信
			insertParams.put("admin_id", params.get("userId"));
			int insertNum = masterDao.insert("agentFinance.insertAgentCreditRecord", insertParams);
			if(insertNum > 0){
				data.put("result", "success");
				data.put("msg","修改成功");
			}else{
				throw new RuntimeException("添加授信记录信息失败");
			}
		} else {
			data.put("result", "fail");
			data.put("msg","修改失败");
		}
		
//		logService.add(LogType.update,LogEnum.财务管理.getValue(), "财务管理-代理商财务-授信操作：修改授信额", params, data);
		Long userId = Long.valueOf(params.get("userId"));
		String pageUrl = params.get("pageUrl");
		String ip = params.get("ip"); 
		logService.add(LogType.update, LogEnum.财务管理.getValue(), userId, pageUrl, ip, "财务管理-客户财务-授信操作：修改授信额", params, data);

		return data;
	}
	
	
	

}
