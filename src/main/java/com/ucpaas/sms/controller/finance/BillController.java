package com.ucpaas.sms.controller.finance;

import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.finance.enums.PaymentType;
import com.ucpaas.sms.common.util.ColumnConverUtil;
import com.ucpaas.sms.common.util.FmtUtils;
import com.ucpaas.sms.common.util.file.ExcelUtils;
import com.ucpaas.sms.common.util.file.FileUtils;
import com.ucpaas.sms.controller.BaseController;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.model.Excel;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.finance.bill.BillInfoService;
import com.ucpaas.sms.util.AgentUtils;
import com.ucpaas.sms.util.ConfigUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.omg.CORBA.OBJ_ADAPTER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lpjLiu on 2017/5/16. 账单信息
 */
@Controller
@RequestMapping("/finance/bill")
public class BillController extends BaseController{

	private static Logger logger = LoggerFactory.getLogger(BillController.class);

	@Autowired
	private BillInfoService billInfoService;

	/**
	 * 账单首页
	 */
	@RequestMapping("/balanceBill")
	public String view(Model model, HttpSession session,String tab) {
		model.addAttribute("max_export_num", ConfigUtils.max_export_excel_num);
		UserSession user = getUserFromSession(session);
		model.addAttribute("tab",tab);
		model.addAttribute("menus", AgentUtils.hasMenuRight(user, "客户财务-dlscw", "历史财务-lssj","余额账单-yezd", "佣金账单-yjzd", "返点账单-fdzd", "押金账单-yajinzd", "授信账单-sxzd"));
		return "finance/bill/list";
	}


	/**
	 * 余额账单-查询数据
	 */
	@RequestMapping("/balance/list")
	@ResponseBody
	public PageContainer balanceList(String rows, String page, String condition, String financialType, String agentType,
			String paymentType, String start_time_day, String end_time_day, HttpSession session) {
		Map<String, Object> params = new HashMap<>();
		params.put("pageRowCount", rows);
		params.put("currentPage", page);

		// 业务单号/代理商ID/订单号
		params.put("balance_text", condition);

		if (StringUtils.isNotBlank(financialType)) {
			params.put("financial_type", financialType);
		}

		if (StringUtils.isNotBlank(agentType)) {
			params.put("agent_type", agentType);
		}

		if (StringUtils.isNotBlank(paymentType)) {
			params.put("payment_type", paymentType);
		}

		if (StringUtils.isNotBlank(start_time_day)) {
			params.put("start_time_day", start_time_day);
		}

		if (StringUtils.isNotBlank(end_time_day)) {
			params.put("end_time_day", end_time_day);
		}

		UserSession userSession = getUserFromSession(session);

//		PageContainer<Map<String, Object>> pageContainer = billInfoService.queryBalance(params);
		PageContainer<Map<String, Object>> pageContainer = billInfoService.queryBalanceBill(params,userSession);
		for (Map<String, Object> map : pageContainer.getList()) {
			String amount = map.get("amount").toString();
			map.put("amount", FmtUtils.roundDownAsString(new BigDecimal(amount), 4));

			String balance = map.get("balance").toString();
			map.put("balance", FmtUtils.roundDownAsString(new BigDecimal(balance), 4));
		}
		return pageContainer;
	}

	/**
	 * 余额账单-查询数据
	 */
	@RequestMapping("/balance/total")
	@ResponseBody
	public ResultVO balanceTotal(String condition, String financialType, String agentType, String paymentType,
								 String start_time_day, String end_time_day, HttpSession session) {
		ResultVO resultVO = ResultVO.failure();
		Map<String, Object> params = new HashMap<>();
		// 业务单号/代理商ID/订单号
		params.put("balance_text", condition);

		if (StringUtils.isNotBlank(financialType)) {
			params.put("financial_type", financialType);
		}

		if (StringUtils.isNotBlank(agentType)) {
			params.put("agent_type", agentType);
		}

		if (StringUtils.isNotBlank(paymentType)) {
			params.put("payment_type", paymentType);
		}

		if (StringUtils.isNotBlank(start_time_day)) {
			params.put("start_time_day", start_time_day);
		}

		if (StringUtils.isNotBlank(end_time_day)) {
			params.put("end_time_day", end_time_day);
		}

		UserSession userSession = getUserFromSession(session);
//		Map data = billInfoService.balanceTotal(params);
		Map data = billInfoService.balanceBillTotal(params,userSession);
		resultVO.setSuccess(true);
		resultVO.setData(data);
		return resultVO;
	}

	/**
	 * 余额账单-导出数据
	 */
	@RequestMapping("/balance/export")
	public void balanceExport(String condition, String financialType, String agentType, String paymentType,
			String start_time_day, String end_time_day, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
		String filePath = ConfigUtils.save_path + "/余额账单_" + DateTime.now().toString("yyyyMMddHHmmss") + ".xls";

		Map<String, Object> params = new HashMap<String, Object>();
		StringBuffer buffer = new StringBuffer();
		buffer.append("查询条件 -> ");
		if (StringUtils.isNotBlank(condition)) {
			params.put("balance_text", condition);
			buffer.append("业务单号/客户ID/订单编号 ：").append(condition).append(";");
		}

		if (StringUtils.isNotBlank(financialType)) {
			params.put("financial_type", financialType);
			buffer.append("财务类型 ：").append(ColumnConverUtil.financialTypeToName(financialType)).append(";");
		}

		if (StringUtils.isNotBlank(agentType)) {
			params.put("agent_type", agentType);
			buffer.append("客户类型 ：").append(ColumnConverUtil.agentTypeToName(agentType)).append(";");
		}

		if (StringUtils.isNotBlank(paymentType)) {
			params.put("payment_type", paymentType);
//			buffer.append("业务类型 ：").append(ColumnConverUtil.businessTypeToName(paymentType)).append(";");
			buffer.append("业务类型 ：").append(PaymentType.getDescByValue(Integer.valueOf(paymentType))).append(";");
		}

		if (StringUtils.isNotBlank(start_time_day)) {
			params.put("start_time_day", start_time_day);
			buffer.append("  开始时间：").append(start_time_day).append(";");
		}

		if (StringUtils.isNotBlank(end_time_day)) {
			params.put("end_time_day", end_time_day);
			buffer.append("  结束时间：").append(end_time_day);
		}

		Excel excel = new Excel();
		excel.setFilePath(filePath);
		excel.setTitle("余额账单");
		excel.addRemark(buffer.toString());
		excel.addHeader(20, "业务单号", "id");
		excel.addHeader(20, "业务类型", "payment_type_name");
		excel.addHeader(20, "财务类型", "financial_type_name");
		excel.addHeader(20, "金额（元）", "amount");
		excel.addHeader(20, "余额剩余（元）", "balance");
		excel.addHeader(20, "订单编号", "order_id");
		excel.addHeader(20, "客户ID", "agent_id");
		excel.addHeader(20, "子账户ID", "client_id");
		excel.addHeader(20, "子账户名称", "name");
		excel.addHeader(20, "客户类型", "agent_type_name");
		excel.addHeader(20, "操作者", "admin_id");
		excel.addHeader(20, "备注", "remark");
		excel.addHeader(20, "生成时间", "create_time");

		UserSession userSession = getUserFromSession(session);
		Map totalData = billInfoService.balanceBillTotal(params, userSession);
		totalData.put("id", "总计");
		totalData.put("amount",
				((BigDecimal) totalData.get("sum_amount")).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
		totalData.put("payment_type_name", "-");
		totalData.put("financial_type_name", "-");
		totalData.put("balance", "-");
		totalData.put("order_id", "-");
		totalData.put("agent_id", "-");
		totalData.put("client_id", "-");
		totalData.put("agent_type_name", "-");
		totalData.put("admin_id", "-");
		totalData.put("remark", "-");
		totalData.put("create_time", "-");
		List<Map<String, Object>> data = billInfoService.queryBalance4Excel(params,userSession);
		for (Map<String, Object> map : data) {
			String _paymentType = map.get("payment_type").toString();
			if (!"3".equals(_paymentType) && !"7".equals(_paymentType)) {
				map.put("order_id", "-");
			}
			if ("11".equals(_paymentType)) {
				map.put("order_id", map.get("paymentId"));
			}

			String amount = map.get("amount").toString();
			amount = FmtUtils.roundDownAsString(new BigDecimal(amount), 4);
			if ("1".equals(map.get("financial_type").toString())) {
				map.put("amount", new BigDecimal(amount).multiply(new BigDecimal(-1)));
			}else {
				map.put("amount", amount);
			}

			String balance = map.get("balance").toString();
			map.put("balance", FmtUtils.roundDownAsString(new BigDecimal(balance), 4));
		}
		data.add(totalData);
		excel.setDataList(data);

		if (ExcelUtils.exportExcel(excel, true)) {
			FileUtils.download(response, filePath);
			FileUtils.delete(filePath);
		} else {
			response.setContentType("text/plain;charset=UTF-8");
			try {
				response.getWriter().write("导出Excel文件失败，请联系管理员");
				response.getWriter().flush();
			} catch (IOException e) {
				logger.error("导出Excel文件失败", e);
			}
		}
	}

	/**
	 * 佣金账单-查询数据
	 */
	@RequestMapping("/commission/list")
	@ResponseBody
	public PageContainer commissionList(String rows, String page, String condition, String financialType,
			String agentType, String start_time_day, String end_time_day) {
		Map<String, String> params = new HashMap<>();
		params.put("pageRowCount", rows);
		params.put("currentPage", page);

		// 业务单号/代理商ID
		params.put("commission_text", condition);

		if (StringUtils.isNotBlank(financialType)) {
			params.put("financial_type", financialType);
		}

		if (StringUtils.isNotBlank(agentType)) {
			params.put("agent_type", agentType);
		}

		if (StringUtils.isNotBlank(start_time_day)) {
			params.put("start_time_day", start_time_day);
		}

		if (StringUtils.isNotBlank(end_time_day)) {
			params.put("end_time_day", end_time_day);
		}

		return billInfoService.queryCommission(params);
	}

	/**
	 * 佣金账单-导出数据
	 */
	@RequestMapping("/commission/export")
	public void commissionExport(String condition, String financialType, String agentType, String start_time_day,
			String end_time_day, HttpServletRequest request, HttpServletResponse response) {
		String filePath = ConfigUtils.save_path + "/佣金账单_" + DateTime.now().toString("yyyyMMddHHmmss") + ".xls";

		Map<String, String> params = new HashMap<String, String>();
		StringBuffer buffer = new StringBuffer();
		buffer.append("查询条件 -> ");
		if (StringUtils.isNotBlank(condition)) {
			params.put("commission_text", condition);
			buffer.append("业务单号/代理商ID ：").append(condition).append(";");
		}

		if (StringUtils.isNotBlank(financialType)) {
			params.put("financial_type", financialType);
			buffer.append("财务类型 ：").append(ColumnConverUtil.financialTypeToName(financialType)).append(";");
		}

		if (StringUtils.isNotBlank(agentType)) {
			params.put("agent_type", agentType);
			buffer.append("代理商类型 ：").append(ColumnConverUtil.agentTypeToName(agentType)).append(";");
		}

		if (StringUtils.isNotBlank(start_time_day)) {
			params.put("start_time_day", start_time_day);
			buffer.append("  开始时间：").append(start_time_day).append(";");
		}

		if (StringUtils.isNotBlank(end_time_day)) {
			params.put("end_time_day", end_time_day);
			buffer.append("  结束时间：").append(end_time_day);
		}

		Excel excel = new Excel();
		excel.setFilePath(filePath);
		excel.setTitle("代理商佣金账单");
		excel.addRemark(buffer.toString());
		excel.addHeader(20, "业务单号", "id");
		excel.addHeader(20, "业务类型", "payment_type");
		excel.addHeader(20, "财务类型", "financial_type");
		excel.addHeader(20, "金额（元）", "amount");
		excel.addHeader(20, "剩余佣金（元）", "balance");
		excel.addHeader(20, "代理商ID", "agent_id");
		excel.addHeader(20, "代理商类型", "agent_type_name");
		excel.addHeader(20, "备注", "remark");
		excel.addHeader(20, "生成时间", "create_time");

		excel.setDataList(billInfoService.queryCommission4Excel(params));

		if (ExcelUtils.exportExcel(excel)) {
			FileUtils.download(response, filePath);
			FileUtils.delete(filePath);
		} else {
			response.setContentType("text/plain;charset=UTF-8");
			try {
				response.getWriter().write("导出Excel文件失败，请联系管理员");
				response.getWriter().flush();
			} catch (IOException e) {
				logger.error("导出Excel文件失败", e);
			}
		}
	}

	/**
	 * 返点账单-查询数据
	 */
	@RequestMapping("/rebate/list")
	@ResponseBody
	public PageContainer rebateList(String rows, String page, String condition, String financialType, String agentType,
			String start_time_day, String end_time_day) {
		Map<String, String> params = new HashMap<>();
		params.put("pageRowCount", rows);
		params.put("currentPage", page);

		// 业务单号/代理商ID
		params.put("rebate_text", condition);

		if (StringUtils.isNotBlank(financialType)) {
			params.put("financial_type", financialType);
		}

		if (StringUtils.isNotBlank(agentType)) {
			params.put("agent_type", agentType);
		}

		if (StringUtils.isNotBlank(start_time_day)) {
			params.put("start_time_day", start_time_day);
		}

		if (StringUtils.isNotBlank(end_time_day)) {
			params.put("end_time_day", end_time_day);
		}

		return billInfoService.queryRebate(params);
	}

	/**
	 * 返点账单-导出数据
	 */
	@RequestMapping("/rebate/export")
	public void rebateExport(String condition, String financialType, String agentType, String start_time_day,
			String end_time_day, HttpServletRequest request, HttpServletResponse response) {
		String filePath = ConfigUtils.save_path + "/返点账单_" + DateTime.now().toString("yyyyMMddHHmmss") + ".xls";

		Map<String, String> params = new HashMap<String, String>();
		StringBuffer buffer = new StringBuffer();
		buffer.append("查询条件 -> ");
		if (StringUtils.isNotBlank(condition)) {
			params.put("rebate_text", condition);
			buffer.append("业务单号/代理商ID ：").append(condition).append(";");
		}

		if (StringUtils.isNotBlank(financialType)) {
			params.put("financial_type", financialType);
			buffer.append("财务类型 ：").append(ColumnConverUtil.financialTypeToName(financialType)).append(";");
		}

		if (StringUtils.isNotBlank(agentType)) {
			params.put("agent_type", agentType);
			buffer.append("代理商类型 ：").append(ColumnConverUtil.agentTypeToName(agentType)).append(";");
		}

		if (StringUtils.isNotBlank(start_time_day)) {
			params.put("start_time_day", start_time_day);
			buffer.append("  开始时间：").append(start_time_day).append(";");
		}

		if (StringUtils.isNotBlank(end_time_day)) {
			params.put("end_time_day", end_time_day);
			buffer.append("  结束时间：").append(end_time_day);
		}

		Excel excel = new Excel();
		excel.setFilePath(filePath);
		excel.setTitle("代理商返点账单");
		excel.addRemark(buffer.toString());
		excel.addHeader(20, "业务单号", "id");
		excel.addHeader(20, "业务类型", "payment_type_name");
		excel.addHeader(20, "财务类型", "financial_type_name");
		excel.addHeader(20, "金额（元）", "amount");
		excel.addHeader(20, "返点剩余（元）", "balance");
		excel.addHeader(20, "订单编号", "order_id");
		excel.addHeader(20, "代理商ID", "agent_id");
		excel.addHeader(20, "代理商类型", "agent_type_name");
		excel.addHeader(20, "生成时间", "create_time");

		excel.setDataList(billInfoService.exportRebateExcel(params));

		if (ExcelUtils.exportExcel(excel)) {
			FileUtils.download(response, filePath);
			FileUtils.delete(filePath);
		} else {
			response.setContentType("text/plain;charset=UTF-8");
			try {
				response.getWriter().write("导出Excel文件失败，请联系管理员");
				response.getWriter().flush();
			} catch (IOException e) {
				logger.error("导出Excel文件失败", e);
			}
		}
	}

	/**
	 * 押金账单-查询数据
	 */
	@RequestMapping("/deposit/list")
	@ResponseBody
	public PageContainer depositList(String rows, String page, String condition, String financialType, String agentType,
			String start_time_day, String end_time_day) {
		Map<String, String> params = new HashMap<>();
		params.put("pageRowCount", rows);
		params.put("currentPage", page);

		// 业务单号/代理商ID
		params.put("deposit_text", condition);

		if (StringUtils.isNotBlank(financialType)) {
			params.put("financial_type", financialType);
		}

		if (StringUtils.isNotBlank(agentType)) {
			params.put("agent_type", agentType);
		}

		if (StringUtils.isNotBlank(start_time_day)) {
			params.put("start_time_day", start_time_day);
		}

		if (StringUtils.isNotBlank(end_time_day)) {
			params.put("end_time_day", end_time_day);
		}

		return billInfoService.queryDeposit(params);
	}

	/**
	 * 押金账单-导出数据
	 */
	@RequestMapping("/deposit/export")
	public void depositExport(String condition, String financialType, String agentType, String start_time_day,
			String end_time_day, HttpServletRequest request, HttpServletResponse response) {
		String filePath = ConfigUtils.save_path + "/押金账单_" + DateTime.now().toString("yyyyMMddHHmmss") + ".xls";

		Map<String, String> params = new HashMap<String, String>();
		StringBuffer buffer = new StringBuffer();
		buffer.append("查询条件 -> ");
		if (StringUtils.isNotBlank(condition)) {
			params.put("deposit_text", condition);
			buffer.append("业务单号/代理商ID ：").append(condition).append(";");
		}

		if (StringUtils.isNotBlank(financialType)) {
			params.put("financial_type", financialType);
			buffer.append("财务类型 ：").append(ColumnConverUtil.financialTypeToName(financialType)).append(";");
		}

		if (StringUtils.isNotBlank(agentType)) {
			params.put("agent_type", agentType);
			buffer.append("代理商类型 ：").append(ColumnConverUtil.agentTypeToName(agentType)).append(";");
		}

		if (StringUtils.isNotBlank(start_time_day)) {
			params.put("start_time_day", start_time_day);
			buffer.append("  开始时间：").append(start_time_day).append(";");
		}

		if (StringUtils.isNotBlank(end_time_day)) {
			params.put("end_time_day", end_time_day);
			buffer.append("  结束时间：").append(end_time_day);
		}

		Excel excel = new Excel();
		excel.setFilePath(filePath);
		excel.setTitle("代理商押金账单");
		excel.addRemark(buffer.toString());
		excel.addHeader(20, "业务单号", "id");
		excel.addHeader(20, "业务类型", "payment_type_name");
		excel.addHeader(20, "财务类型", "financial_type_name");
		excel.addHeader(20, "金额（元）", "amount");
		excel.addHeader(20, "剩余押金（元）", "balance");
		excel.addHeader(20, "代理商ID", "agent_id");
		excel.addHeader(20, "代理商类型", "agent_type_name");
		excel.addHeader(20, "操作者", "admin_id");
		excel.addHeader(20, "备注", "remark");
		excel.addHeader(20, "生成时间", "create_time");

		excel.setDataList(billInfoService.exportDepositExcel(params));

		if (ExcelUtils.exportExcel(excel)) {
			FileUtils.download(response, filePath);
			FileUtils.delete(filePath);
		} else {
			response.setContentType("text/plain;charset=UTF-8");
			try {
				response.getWriter().write("导出Excel文件失败，请联系管理员");
				response.getWriter().flush();
			} catch (IOException e) {
				logger.error("导出Excel文件失败", e);
			}
		}
	}

	/**
	 * 授信账单-查询数据
	 */
	@RequestMapping("/credit/list")
	@ResponseBody
	public PageContainer creditList(String rows, String page, String condition, String financialType, String agentType,
			String start_time_day, String end_time_day) {
		Map<String, String> params = new HashMap<>();
		params.put("pageRowCount", rows);
		params.put("currentPage", page);

		// 业务单号/代理商ID
		params.put("credit_text", condition);

		if (StringUtils.isNotBlank(financialType)) {
			params.put("financial_type", financialType);
		}

		if (StringUtils.isNotBlank(agentType)) {
			params.put("agent_type", agentType);
		}

		if (StringUtils.isNotBlank(start_time_day)) {
			params.put("start_time_day", start_time_day);
		}

		if (StringUtils.isNotBlank(end_time_day)) {
			params.put("end_time_day", end_time_day);
		}

		return billInfoService.queryCredit(params);
	}

	/**
	 * 授信账单-导出数据
	 */
	@RequestMapping("/credit/export")
	public void creditExport(String condition, String financialType, String agentType, String start_time_day,
			String end_time_day, HttpServletRequest request, HttpServletResponse response) {
		String filePath = ConfigUtils.save_path + "/授信记录_" + DateTime.now().toString("yyyyMMddHHmmss") + ".xls";

		Map<String, String> params = new HashMap<String, String>();
		StringBuffer buffer = new StringBuffer();
		buffer.append("查询条件 -> ");
		if (StringUtils.isNotBlank(condition)) {
			params.put("credit_text", condition);
			buffer.append("业务单号/代理商ID ：").append(condition).append(";");
		}

		if (StringUtils.isNotBlank(financialType)) {
			params.put("financial_type", financialType);
			buffer.append("财务类型 ：").append(ColumnConverUtil.financialTypeToName(financialType)).append(";");
		}

		if (StringUtils.isNotBlank(agentType)) {
			params.put("agent_type", agentType);
			buffer.append("代理商类型 ：").append(ColumnConverUtil.agentTypeToName(agentType)).append(";");
		}

		if (StringUtils.isNotBlank(start_time_day)) {
			params.put("start_time_day", start_time_day);
			buffer.append("  开始时间：").append(start_time_day).append(";");
		}

		if (StringUtils.isNotBlank(end_time_day)) {
			params.put("end_time_day", end_time_day);
			buffer.append("  结束时间：").append(end_time_day);
		}

		Excel excel = new Excel();
		excel.setFilePath(filePath);
		excel.setTitle("代理商授信记录");
		excel.addRemark(buffer.toString());
		excel.addHeader(20, "业务单号", "id");
		excel.addHeader(20, "业务类型", "payment_type_name");
		excel.addHeader(20, "金额（元）", "amount");
		excel.addHeader(20, "代理商ID", "agent_id");
		excel.addHeader(20, "代理商类型", "agent_type_name");
		excel.addHeader(20, "操作者", "admin_id");
		excel.addHeader(20, "备注", "remark");
		excel.addHeader(20, "生成时间", "create_time");

		excel.setDataList(billInfoService.exportCreditExcel(params));

		if (ExcelUtils.exportExcel(excel)) {
			FileUtils.download(response, filePath);
			FileUtils.delete(filePath);
		} else {
			response.setContentType("text/plain;charset=UTF-8");
			try {
				response.getWriter().write("导出Excel文件失败，请联系管理员");
				response.getWriter().flush();
			} catch (IOException e) {
				logger.error("导出Excel文件失败", e);
			}
		}
	}
}
