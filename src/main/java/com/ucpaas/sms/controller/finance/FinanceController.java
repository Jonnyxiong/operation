package com.ucpaas.sms.controller.finance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsmsframework.common.constant.SysConstant;
import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.common.enums.PaymentType;
import com.jsmsframework.common.enums.ProductType;
import com.jsmsframework.finance.entity.JsmsAgentAccount;
import com.jsmsframework.finance.entity.JsmsOnlinePayment;
import com.jsmsframework.finance.service.*;
import com.jsmsframework.order.entity.JsmsClientOrder;
import com.jsmsframework.order.entity.JsmsOemAgentPool;
import com.jsmsframework.order.entity.po.JsmsClientOrderPo;
import com.jsmsframework.order.entity.po.JsmsOemAgentPoolPo;
import com.jsmsframework.order.exception.JsmsOrderFinanceException;
import com.jsmsframework.order.service.JsmsClientOrderService;
import com.jsmsframework.order.service.JsmsOemAgentPoolService;
import com.jsmsframework.order.service.JsmsOrderFinanceService;
import com.jsmsframework.sale.credit.service.JsmsSaleCreditService;
import com.jsmsframework.user.entity.JsmsAccount;
import com.jsmsframework.user.entity.JsmsAgentInfo;
import com.jsmsframework.user.entity.JsmsMenu;
import com.jsmsframework.user.entity.JsmsUser;
import com.jsmsframework.user.service.JsmsAccountService;
import com.jsmsframework.user.service.JsmsAgentInfoService;
import com.jsmsframework.user.service.JsmsUserService;
import com.ucpaas.sms.common.util.Collections3;
import com.ucpaas.sms.common.util.DateUtils;
import com.ucpaas.sms.common.util.FmtUtils;
import com.ucpaas.sms.common.util.MathUtils;
import com.ucpaas.sms.common.util.file.ExcelUtils;
import com.ucpaas.sms.common.util.file.FileUtils;
import com.ucpaas.sms.constant.LogConstant;
import com.ucpaas.sms.constant.RechargeSMSConstant;
import com.ucpaas.sms.controller.BaseController;
import com.ucpaas.sms.dao.MessageMasterDao;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.enums.LogEnum;
import com.ucpaas.sms.enums.SessionEnum;
import com.ucpaas.sms.model.Excel;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.admin.AuthorityService;
import com.ucpaas.sms.service.audit.CustomerAuditService;
import com.ucpaas.sms.service.common.LogService;
import com.ucpaas.sms.service.finance.agent.AgentFinanceService;
import com.ucpaas.sms.util.*;
import com.ucpaas.sms.util.beans.BeanUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by lpjLiu on 2017/5/16. 财务信息
 */
@Controller
@RequestMapping("/finance")
public class FinanceController extends BaseController{

	private static Logger logger = LoggerFactory.getLogger(FinanceController.class);

	private final String FAIL="fail";

	private final String SUCCESS="success";
	private final ObjectMapper customMapper = new ObjectMapper();
	private final MappingJackson2JsonView view = new MappingJackson2JsonView();

	@Autowired
	private AgentFinanceService agentFinanceService;

	@Autowired
	private JsmsOemAgentPoolService oemAgentPoolService;

	@Autowired
	private JsmsAccountService jsmsAccountService;

	@Autowired
	private JsmsClientOrderService jsmsClientOrderService;

	@Autowired
	private JsmsOrderFinanceService jsmsOrderFinanceService;

	@Autowired
	private CustomerAuditService customerAuditService;

	@Autowired
	private JsmsAgentBalanceBillService jsmsAgentBalanceBillService;
	@Autowired
	private JsmsAgentAccountService jsmsAgentAccountService;

	@Autowired
	private JsmsSaleCreditBillService jsmsSaleCreditBillService;

	@Autowired
	private JsmsSaleCreditAccountService jsmsSaleCreditAccountService;

	@Autowired
	private JsmsAgentInfoService jsmsAgentInfoService;
	@Autowired
	private MessageMasterDao masterDao;

	@Autowired
	private JsmsUserService jsmsUserService;
	@Autowired
	private JsmsSaleCreditService jsmsSaleCreditService;

	@Autowired
	private JsmsOnlinePaymentService jsmsOnlinePaymentService;

	@Autowired
	private AuthorityService authorityService;

	@Autowired
	private LogService logService;

	private static Map queryFase= new HashMap<String,Object>();




	/**
	 *代理商账单首页
	 */
	@RequestMapping("/agentFinance")
	public String view(Model model, HttpSession session,String tab) {
		model.addAttribute("max_export_num", ConfigUtils.max_export_excel_num);
		UserSession user = getUserFromSession(session);
		model.addAttribute("menus", AgentUtils.hasMenuRight(user, "客户财务-dlscw", "历史财务-lssj","余额账单-yezd", "佣金账单-yjzd", "返点账单-fdzd", "押金账单-yajinzd", "授信账单-sxzd"));

		JsmsMenu jsmsMenu = authorityService.getJsmsMenu("客户财务");
		model.addAttribute("jsmsMenu", jsmsMenu);

		return "finance/info/list";
	}
	@RequestMapping("/onlinepay/pay")
	public String onlinePay(Model model, HttpSession session,String tab) {
		model.addAttribute("max_export_num", ConfigUtils.max_export_excel_num);
		UserSession user = getUserFromSession(session);
		model.addAttribute("menus", AgentUtils.hasMenuRight(user, "代理商财务-dlscw", "历史财务-lssj","余额账单-yezd", "佣金账单-yjzd", "返点账单-fdzd", "押金账单-yajinzd", "授信账单-sxzd"));
//		model.addAttribute("menus",)

		JsmsMenu jsmsMenu = authorityService.getJsmsMenu("在线支付订单");
		model.addAttribute("jsmsMenu" , jsmsMenu);
		return "finance/onlinepay/pay";
	}
	/*
	* 订单列表
	 */
	@RequestMapping("/onlinepay/pay/list")
    @ResponseBody
	public R onlinePayList(HttpSession session,@RequestParam Map<String,String> params) {
		JsmsPage jpage=agentFinanceService.initOnlinePayMentParam(params);
		agentFinanceService.initOnlinePayMent(jpage);
		PageContainer container = PageConvertUtil.pageToContainer(jpage);
		return R.ok("获取客户授信列表成功",container);
	}

    /*
     *修改说明
     */
    @RequestMapping("/onlinepay/pay/editDes")
    @ResponseBody
    public R editDes(HttpSession session,HttpServletRequest request,String paymentId,String description) {
        JsmsOnlinePayment jsmsOnlinePayment=jsmsOnlinePaymentService.getByPaymentId(paymentId);
        jsmsOnlinePayment.setDescription(description);
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		jsmsOnlinePayment.setAdminId(userSession.getId());
        jsmsOnlinePaymentService.update(jsmsOnlinePayment);
		logService.add(LogConstant.LogType.update, LogEnum.在线支付.getValue(),userSession.getId(),request.getRequestURI(),HttpUtils.getIpAddress(request),"财务管理-在线支付订单-修改订单说明",paymentId,description);
        return R.ok("修改说明成功");
    }
    /*
     *已解决
     */
    @RequestMapping("/onlinepay/pay/solve")
    @ResponseBody
    public R solve(HttpSession session,String paymentId,HttpServletRequest request) {
        JsmsOnlinePayment jsmsOnlinePayment=jsmsOnlinePaymentService.getByPaymentId(paymentId);
        jsmsOnlinePayment.setRemark("已人工解决");
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		jsmsOnlinePayment.setAdminId(userSession.getId());
        jsmsOnlinePaymentService.update(jsmsOnlinePayment);
		logService.add(LogConstant.LogType.update, LogEnum.在线支付.getValue(),userSession.getId(),request.getRequestURI(),HttpUtils.getIpAddress(request),"财务管理-在线支付订单-人工解决操作",paymentId);
        return R.ok("已解决处理成功");
    }
    /*
    *修改订单
     */
    @RequestMapping("/onlinepay/pay/editPay")
    @ResponseBody
    public R editPay(HttpSession session,HttpServletRequest request,String paymentId,String description,Integer paymentState) {
        JsmsOnlinePayment jsmsOnlinePayment=jsmsOnlinePaymentService.getByPaymentId(paymentId);
        if(jsmsOnlinePayment.getPaymentState()!=2&&paymentState==2){
            jsmsOnlinePayment.setPaymentState(2);
            Map params=new HashMap();
            params.put("agent_id",jsmsOnlinePayment.getAgentId().toString());
            params.put("operateAmount",jsmsOnlinePayment.getPaymentAmount().toString());
            params.put("remark","充值");
            params.put("clientId","");
            params.put("operateType","充值");

            balanceSave(params,session,
                    request);
        }
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		jsmsOnlinePayment.setAdminId(userSession.getId());
        jsmsOnlinePayment.setDescription(description);
        jsmsOnlinePaymentService.update(jsmsOnlinePayment);
		logService.add(LogConstant.LogType.update, LogEnum.在线支付.getValue(),userSession.getId(),request.getRequestURI(),HttpUtils.getIpAddress(request),"财务管理-在线支付订单-人工解决操作",paymentId,description,paymentState);
        return R.ok("修改订单成功");
    }


	@RequestMapping("/onlinepay/pay/export")
	public void onlinePayExport(@RequestParam Map<String,String> params, HttpServletRequest request,
								   HttpServletResponse response) {

		JsmsPage jpage=agentFinanceService.initOnlinePayMentParam(params);
        jpage.setRows(Integer.MAX_VALUE);
        jpage.setMaxQueryLimit(Integer.MAX_VALUE);
		agentFinanceService.initOnlinePayMent4Export(jpage);


		String fileName = "在线支付订单信息";

		StringBuffer buffer = new StringBuffer();
		buffer.append("查询条件 -> ");


		String agentTypeName = "";




		String filePath = ConfigUtils.save_path + "/" + fileName + "_" + DateTime.now().toString("yyyyMMddHHmmss")
				+ ".xls";


		Excel excel = new Excel();
		excel.setFilePath(filePath);
		excel.setTitle(fileName);
		excel.addRemark(buffer.toString());
		excel.addHeader(20, "支付订单号", "paymentId");
		excel.addHeader(30, "金额（元）", "paymentAmount");
		excel.addHeader(30, "支付方式", "paymentMode");
		excel.addHeader(30, "支付流水号", "flowId");
        excel.addHeader(30, "支付时间", "payTime");
		excel.addHeader(30, "支付状态", "paymentState");
        excel.addHeader(30, "客户ID", "agentId");
        excel.addHeader(30, "客户名称", "agentName");
		excel.addHeader(30, "归属销售", "saler");
		excel.addHeader(30, "操作者", "operation");
		excel.addHeader(30, "备注", "remark");
        excel.addHeader(30, "订单说明：", "description");
		excel.setDataList(jpage.getData());

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
	 * 代理商历史财务查询
	 */
	@RequestMapping("/hisAgentFinance")
	public String hisview(Model model, HttpSession session,String tab) {
		model.addAttribute("max_export_num", ConfigUtils.max_export_excel_num);
		UserSession user = getUserFromSession(session);

		model.addAttribute("menus", AgentUtils.hasMenuRight(user, "客户财务-dlscw", "历史财务-lssj","余额账单-yezd", "佣金账单-yjzd", "返点账单-fdzd", "押金账单-yajinzd", "授信账单-sxzd"));
		return "finance/info/hislist";
	}

	@RequestMapping("/agentFinance/list")
	@ResponseBody
	public PageContainer agentFinanceList(String rows, String page, String condition, String agentType,
			String create_time) throws ParseException {
		Map<String, String> params = new HashMap<>();
		params.put("pageRowCount", rows);
		params.put("currentPage", page);

		// 代理商ID/代理商名称/手机号码/邮箱
		params.put("agentInfo", condition);
		params.put("agentType", agentType);

		return agentFinanceService.query(params);
	}

	@RequestMapping("/agentFinance/listhis")
	@ResponseBody
	public PageContainer agentFinanceListhis(String rows, String page, String condition, String agentType,
			String create_time) throws ParseException {
		Map<String, String> params = new HashMap<>();
		params.put("pageRowCount", rows);
		params.put("currentPage", page);

		// 代理商ID/代理商名称/手机号码/邮箱
		params.put("agentInfo", condition);
		params.put("agentType", agentType);

		if (StringUtils.isBlank(create_time)) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);

			Date nowTime = new Date();
			create_time = sdf.format(nowTime);
			params.put("create_time", create_time);
		} else {
			params.put("create_time", create_time);
		}
        PageContainer pc=agentFinanceService.query(params);
		return pc;
	}

	/**
	 * 品牌代理商回退
	 */
	@RequestMapping("/agentReback")
	public String reback(Model model) {
		model.addAttribute("max_export_num", ConfigUtils.max_export_excel_num);
		return "finance/info/agentReback";
	}

	/**
	 * OEM代理商回退
	 */
	@RequestMapping("/oemReback")
	public String rebackoem(Model model) {
		model.addAttribute("max_export_num", ConfigUtils.max_export_excel_num);
		return "finance/info/oemReback";
	}
	/**
	 * 余额账单-查询数据
	 */
	@RequestMapping("/agentFinance/total")
	@ResponseBody
	public ResultVO balanceTotal(String condition, String agentType, String create_time) {
		ResultVO resultVO = ResultVO.failure();
		Map<String, String> params = new HashMap<>();
		// 业务单号/代理商ID/订单号
		// 代理商ID/代理商名称/手机号码/邮箱
		params.put("agentInfo", condition);
		params.put("agentType", agentType);

		if (StringUtils.isBlank(create_time)) {

		} else {
			params.put("create_time", create_time);
		}

		Map data = agentFinanceService.sumTotal(params);
		resultVO.setSuccess(true);
		resultVO.setData(data);
		return resultVO;
	}

	/**
	 * 余额账单-查询数据
	 */
	@RequestMapping("/agentFinance/total1")
	@ResponseBody
	public ResultVO balanceTotal1(String condition, String agentType, String create_time) {
		ResultVO resultVO = ResultVO.failure();
		Map<String, String> params = new HashMap<>();
		// 业务单号/代理商ID/订单号
		// 代理商ID/代理商名称/手机号码/邮箱
		params.put("agentInfo", condition);
		params.put("agentType", agentType);

		if (StringUtils.isBlank(create_time)) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.CHINA);

			Date nowTime = new Date();
			create_time = sdf.format(nowTime);
			params.put("create_time", create_time);
		} else {
			params.put("create_time", create_time);
		}
		Map<String, Object> data=new HashMap<String, Object>();
		System.out.println(params.toString());
		if(queryFase.containsKey(params.toString())) {
			data = (Map)queryFase.get(params.toString());
		}else {
			data = agentFinanceService.sumTotal(params);
			queryFase.put(params.toString(),data);
		}
		resultVO.setSuccess(true);
		resultVO.setData(data);
		return resultVO;
	}

	@RequestMapping("/agentFinance/export")
	public void agentFinanceExport(String condition, String agentType, String create_time, HttpServletRequest request,
			HttpServletResponse response) {

		String fileName = "";

		// condition=new String(condition,"utf-8");

		// byte[] b= new byte[0];//用tomcat的格式（iso-8859-1）方式去读。
		// try {
		// b = condition.getBytes("ISO-8859-1");
		// } catch (UnsupportedEncodingException e) {
		// e.printStackTrace();
		// }
		// String str=newString(b,"utf-8");
		Map<String, String> params = new HashMap<String, String>();
		StringBuffer buffer = new StringBuffer();
		buffer.append("查询条件 -> ");
		if (StringUtils.isNotBlank(condition)) {
			params.put("agentInfo", condition);
			buffer.append("客户ID/客户名称/手机号码/邮箱 ：").append(condition).append(";");

		}

		String agentTypeName = "";
		params.put("agentType", agentType);
		if (null == agentType || agentType.isEmpty()) {
			agentTypeName = "全部";
		} else if ("1".equals(agentType)) {
			agentTypeName = "销售代理商";
		} else if ("2".equals(agentType)) {
			agentTypeName = "品牌代理商";
		} else if ("3".equals(agentType)) {
			agentTypeName = "资源合作商";
		} else if ("4".equals(agentType)) {
			agentTypeName = "代理商和资源合作";
		} else if ("5".equals(agentType)) {
			agentTypeName = "OEM代理商";
		}
		buffer.append("客户类型 ：").append(agentTypeName).append(";");
		if (StringUtils.isNotBlank(create_time)) {
			params.put("create_time", create_time);
			buffer.append(" 时间 ：").append(create_time).append(";");
			fileName = "客户财务-历史记录";
		} else {
			fileName = "客户财务";
		}

		String filePath = ConfigUtils.save_path + "/" + fileName + "_" + DateTime.now().toString("yyyyMMddHHmmss")
				+ ".xls";
		// if(create_time==null){
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm",
		// Locale.CHINA);
		//
		// Date nowTime = new Date();
		// create_time=sdf.format(nowTime);
		// params.put("create_time",create_time);
		// }else{
		// params.put("create_time",create_time);
		// }

		Excel excel = new Excel();
		excel.setFilePath(filePath);
		excel.setTitle(fileName);
		excel.addRemark(buffer.toString());
		excel.addHeader(20, "客户ID", "agent_id");
		excel.addHeader(30, "客户名称", "agent_name");
		excel.addHeader(30, "客户类型", "agentType");
		excel.addHeader(30, "邮箱", "email");
		excel.addHeader(20, "手机号码", "mobile");
		excel.addHeader(30, "余额（元）", "balance");
		excel.addHeader(30, "佣金剩余（元）", "commission_income");
		excel.addHeader(30, "返点剩余（元）", "rebate_income");
		excel.addHeader(30, "押金（元）", "deposit");

		// 导出加上总计
		Map<String, Object> totalData = new HashMap(4);
		if (StringUtils.isNotBlank(create_time)) {
			// 客户财务-历史记录
		} else {
			// 客户财务
			Map sumTotal = agentFinanceService.sumTotal(params);
			totalData.put("agent_id", null);
			totalData.put("agent_name", null);
			totalData.put("agentType", null);
			totalData.put("email", null);
			totalData.put("mobile", "合计");
			totalData.put("balance", sumTotal.get("balance_total"));
			totalData.put("commission_income", sumTotal.get("commission_income_total"));
			totalData.put("rebate_income", sumTotal.get("rebate_income_total"));
			totalData.put("deposit", sumTotal.get("deposit_total"));
		}
		excel.setDataList(agentFinanceService.queryAll(params));
		if (!totalData.isEmpty()){
			excel.getDataList().add(totalData);
		}

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

	/*@RequestMapping("/creditOperate")
	public String credit(Integer agentId, Model model) {
		Map<String, Object> data = agentFinanceService.getAgentInfoByAgentID(agentId);
		model.addAttribute("data", data);
		return "finance/info/credit";
	}

	@RequestMapping("/creditOperate/edit")
	@ResponseBody
	public ResultVO creditSave(@RequestParam Map<String, String> params, HttpSession session,
			HttpServletRequest request) {
		ResultVO result = ResultVO.failure();

		// 需要用户Id
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		params.put("userId", userSession.getId().toString());
		params.put("pageUrl", request.getRequestURI());
		params.put("ip", HttpUtils.getIpAddress(request));

		Map data = null;
		try {
			data = agentFinanceService.createAction(params);
		} catch (Exception e) {
			data = new HashMap<String, Object>();
			data.put("result", "fail");
			data.put("msg", "添加授信记录信息失败");
			logger.debug("添加授信记录信息失败 ---------> 运行错误信息", e.getMessage());
		}

		result.setSuccess("success".equals(data.get("result").toString()));
		result.setMsg(data.get("msg").toString());
		return result;
	}*/

	@RequestMapping("/depositOperate")
	public String deposit(String operate, Integer agentId, Model model) {
		try {
			operate = URLDecoder.decode(operate, "utf-8");
			logger.debug("押金操作类型 ---> {}", operate);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		Map<String, Object> data = agentFinanceService.getAgentInfoByAgentID(agentId);
		data.put("operateType", operate);
		model.addAttribute("data", data);
		return "finance/info/deposit";
	}

	@RequestMapping("/depositOperate/edit")
	@ResponseBody
	public ResultVO depositSave(@RequestParam Map<String, String> params, HttpSession session,
			HttpServletRequest request) {
		ResultVO result = ResultVO.failure();

		// 需要用户Id
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		params.put("userId", userSession.getId().toString());
		params.put("pageUrl", request.getRequestURI());
		params.put("ip", HttpUtils.getIpAddress(request));

		Map data = null;
		try {
			data = agentFinanceService.depositAction(params);
		} catch (Exception e) {
			data = new HashMap<String, Object>();
			data.put("result", "fail");
			data.put("msg", "押金操作失败");
			logger.debug("押金" + params.get("operateType") + "操作失败---------> 运行错误信息", e.getMessage());
		}

		result.setSuccess("success".equals(data.get("result").toString()));
		result.setMsg(data.get("msg").toString());
		return result;
	}

	@RequestMapping("/balanceOperate")
	public String balance(String operate, Integer agentId, Model model) {
		try {
			operate = URLDecoder.decode(operate, "utf-8");
			logger.debug("余额操作类型 ---> {}", operate);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		Map<String, Object> data = agentFinanceService.getAgentInfoByAgentID(agentId);
		data.put("operateType", operate);
		model.addAttribute("data", data);
		return "finance/info/balance";
	}

	@RequestMapping("/balanceOperate/search")
	@ResponseBody
	public ResultVO balanceSearchClient(@RequestParam Map<String, String> params, HttpSession session,
			HttpServletRequest request) {
		ResultVO result = ResultVO.failure();
		Map<String, Object> data = agentFinanceService.getClientByCondition1(params);
		if (null == data || data.size() == 0) {
			result.setFail(true);

			result.setMsg("客户ID不存在或没有关联关系");
			// data.put("msg", );
			return result;
		}
		if (!"3".equals(data.get("oauth_status").toString())) {
			result.setFail(true);
			result.setMsg("该ID客户未认证通过");
			return result;
		}
		if (!"1".equals(data.get("status").toString())) {
			result.setFail(true);
			result.setMsg("该ID客户状态异常");
			return result;
		}
		result.setSuccess(true);
		// data.put("result", "success");
		result.setMsg("该ID客户存在");
		return result;
	}

	@RequestMapping("/balanceOperate/edit")
	@ResponseBody
	public ResultVO balanceSave(@RequestParam Map<String, String> params, HttpSession session,
			HttpServletRequest request) {
		ResultVO result = ResultVO.failure();

		// 需要用户Id
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		params.put("userId", userSession.getId().toString());
		params.put("pageUrl", request.getRequestURI());
		params.put("ip", HttpUtils.getIpAddress(request));

		Map data = null;
		try {
			//data = agentFinanceService.balanceAction(params);
		//	data=this.agentBalanceForOpreation(params);
			data=jsmsSaleCreditService.agentBalanceForOpreation(params);
			//充值成功，发送短信至归属销售
			if(Objects.equals(data.get("result"),SUCCESS) && Objects.equals(PaymentType.充值.getDesc(),params.get("operateType"))){
				List<String> params1=new ArrayList<>();
				JsmsAgentInfo agent=jsmsAgentInfoService.getByAgentId(Integer.valueOf(params.get("agent_id")));
				JsmsAgentAccount agentNew = jsmsAgentAccountService.getByAgentId(agent.getAgentId());
				JsmsUser sale=jsmsUserService.getById2(agent.getBelongSale());
				if(sale==null){
					data.put("result", SysConstant.FAIL);
					data.put("msg", "归属销售不存在，无法发送充值短信！");
					logger.error("归属销售不存在，无法发送充值短信");
					result.setFail("fail".equals(data.get("result").toString()));
					result.setMsg(data.get("msg").toString());
					return result;
				}
				params1.add(0,sale.getRealname());
				params1.add(1,agent.getAgentId()+"-"+agent.getAgentName());
				params1.add(2,String.valueOf(agentNew.getBalance().add(agentNew.getCurrentCredit())));
				String mobile=sale.getMobile();
				String result1= null;
				try {
					result1 = SendSMSUtil.sendRechargeSMS(RechargeSMSConstant.client_sms_recharge_template,mobile,params1);
					logger.debug("充值成功调用发送短信接口,返回内容={}",result1);
				} catch (Exception e) {
					logger.error(agent.getAgentId() + "客户充值余额成功,发送短信至归属销售失败");
				}

			}

		} catch (Exception e) {
			data = new HashMap<String, Object>();
			data.put("result", SysConstant.FAIL);
			data.put("msg", "余额操作失败");
			logger.debug("余额" + params.get("operateType") + "操作失败---------> 运行错误信息", e.getMessage());
		}

		result.setSuccess("success".equals(data.get("result").toString()));
		result.setMsg(data.get("msg").toString());
		return result;
	}



	@ApiOperation(value = "获取代理商池列表", notes = "获取代理商池列表", tags = "财务管理-回退条数", response = R.class)
	@ApiImplicitParam(name = "agentId", value = "代理商ID", paramType = "query", required = true, dataType = "int")
	@GetMapping("/returnqty/agentpools")
	@ResponseBody
	public R agentPools(Integer agentId) {
		if (agentId == null) {
			return R.error("代理商ID不能为空");
		}

		// 查询代理商池
		JsmsOemAgentPool queryPool = new JsmsOemAgentPool();
		queryPool.setAgentId(agentId);
		List<JsmsOemAgentPool> list = oemAgentPoolService.findList(queryPool);
		if (Collections3.isEmpty(list)) {
			return R.ok("获取代理商池成功", new ArrayList<>());
		}

		// 过滤掉不处理的
		List<JsmsOemAgentPoolPo> poolPos = new ArrayList<>();
		for (JsmsOemAgentPool pool : list) {
			// 不支持国际短信的回退, 已停用的不可回退, 数量为0的不会退
			if (pool.getProductType().intValue() == ProductType.国际.getValue().intValue() || pool.getStatus() == 1
					|| pool.getRemainNumber() <= 0) {
				continue;
			}

			JsmsOemAgentPoolPo po = new JsmsOemAgentPoolPo();
			BeanUtil.copyProperties(pool, po);
			poolPos.add(po);
		}

		if (Collections3.isEmpty(poolPos)) {
			return R.ok("获取代理商池成功", new ArrayList<>());
		}

		// 合并重复数据
		Set<Long> ids = new HashSet<>();
		for (JsmsOemAgentPoolPo poolPo : poolPos) {
			String date = DateUtils.formatDate(poolPo.getDueTime(), "yyyyMMdd");
			String unitPrice = FmtUtils.roundDownAsString(poolPo.getUnitPrice(), 4);

			// 设置多条记录
			poolPo.setMultiRecord(new HashMap<Long, String>());

			poolPo.getMultiRecord().put(poolPo.getAgentPoolId(),
					poolPo.getProductType().intValue() == ProductType.国际.getValue()
							? poolPo.getRemainAmount().toString() : poolPo.getRemainNumber().toString());

			// 已合并过的ID跳过
			if (ids.contains(poolPo.getAgentPoolId())) {
				continue;
			}

			// 查询是否存在相同的
			for (JsmsOemAgentPoolPo pool : poolPos) {
				if (pool.getAgentPoolId().intValue() == poolPo.getAgentPoolId().intValue()) {
					continue;
				}

				String date1 = DateUtils.formatDate(pool.getDueTime(), "yyyyMMdd");
				String unitPrice1 = FmtUtils.roundDownAsString(pool.getUnitPrice(), 4);

				// 若存在相同的记录，合并
				if (pool.getProductType().intValue() == poolPo.getProductType().intValue()
						&& pool.getOperatorCode().intValue() == poolPo.getOperatorCode().intValue()
						&& pool.getAreaCode().intValue() == poolPo.getAreaCode().intValue() && date1.equals(date)
						&& unitPrice1.equals(unitPrice)) {

					// 若是国际
					if (poolPo.getProductType().intValue() == ProductType.国际.getValue()) {
						Double price = MathUtils.add(poolPo.getRemainAmount().doubleValue(),
								pool.getRemainAmount().doubleValue());
						poolPo.setRemainAmount(FmtUtils.roundDown(new BigDecimal(price.toString()), 4));
						poolPo.getMultiRecord().put(pool.getAgentPoolId(), pool.getRemainAmount().toString());
					} else {
						poolPo.setRemainNumber(poolPo.getRemainNumber() + pool.getRemainNumber());
						poolPo.getMultiRecord().put(pool.getAgentPoolId(), pool.getRemainNumber().toString());
					}

					ids.add(pool.getAgentPoolId());
				}
			}
		}

		Iterator iterator = poolPos.iterator();
		while (iterator.hasNext()) {
			JsmsOemAgentPoolPo poolPo = (JsmsOemAgentPoolPo) iterator.next();
			if (ids.contains(poolPo.getAgentPoolId())) {
				iterator.remove();
			}
		}

		return R.ok("获取代理商池成功", poolPos);
	}

	@ApiOperation(value = "代理商池回退", notes = "代理商池回退", tags = "财务管理-回退条数", response = R.class)
	@PostMapping("/returnqty/agentpools/return")
	@ResponseBody
	public R agentPoolsReturn(@RequestBody List<JsmsOemAgentPoolPo> oemAgentPoolPos, HttpServletRequest request) {
		if (oemAgentPoolPos == null || oemAgentPoolPos.isEmpty()) {
			return R.error("参数为空");
		}

		// 生成订单编号
		int count = 0;
		for (JsmsOemAgentPoolPo oemAgentPoolPo : oemAgentPoolPos) {
			count += oemAgentPoolPo.getMultiRecord().size();
		}

		List<Long> orderIds = new ArrayList<>();
		for (int i = 0; i < count + 1; i++) {
			orderIds.add(customerAuditService.getOemAgentOrderId());
		}

		R r;
		try {
			UserSession userSession = (UserSession) request.getSession()
					.getAttribute(SessionEnum.SESSION_USER.toString());
			r = jsmsOrderFinanceService.agentPoolReturnQuantity(oemAgentPoolPos, userSession.getId(),
					request.getRequestURI(), HttpUtils.getIpAddress(request), orderIds);
		} catch (JsmsOrderFinanceException e) {
			logger.error("==代理商订单回退条数失败 {}", e);
			r = R.error(e.getMessage());
		} catch (Exception e1) {
			logger.error("==代理商池回退条数失败 {}", e1);
			r = R.error("回退失败，请联系管理员");
		}
		return r;
	}

	@JsonIgnoreProperties({ "sid", "status", "agentId", "oauthStatus", "oauthDate", "mobile", "email", "province",
			"city", "area", "address", "realname", "clientLevel", "clientType", "needreport", "needmo", "needaudit",
			"createtime", "ip", "deliveryurl", "mourl", "nodenum", "paytype", "needextend", "signextend", "belongSale",
			"agentOwned", "remarks", "smstype", "smsfrom", "isoverratecharge", "updatetime", "spnum",
			"getreportInterval", "getreportMaxsize", "signportlen", "moip", "moport", "nodeid", "identify",
			"accessSpeed", "noticeurl", "extendSize", "clientAscription", "extendtype", "extendport" })
	private static class JsmsAccountFilter extends JsmsAccount {
	}

	@ApiOperation(value = "获取客户列表", notes = "获取客户列表", tags = "财务管理-回退条数", response = R.class)
	@ApiImplicitParam(name = "agentId", value = "代理商ID", paramType = "query", required = true, dataType = "int")
	@GetMapping("/returnqty/clients")
	public View clients(Integer agentId, Model model) {
		customMapper.addMixIn(JsmsAccount.class, JsmsAccountFilter.class);
		customMapper.setTimeZone(TimeZone.getDefault());
		view.setObjectMapper(customMapper);

		if (agentId == null) {
			model.addAllAttributes(R.error("代理商ID不能为空").asMap());
			return view;
		}

		model.addAllAttributes(R.ok("获取数据成功", jsmsAccountService.findListForReturnQuantity(agentId)).asMap());

		return view;
	}

	@ApiOperation(value = "获取订单列表", notes = "获取订单列表", tags = "财务管理-回退条数", response = R.class)
	@PostMapping("/returnqty/agentorders")
	@ResponseBody
	public R agentOrders(@RequestBody JsmsClientOrder clientOrder) {
		if (clientOrder == null) {
			return R.error("参数为空");
		}

		if (clientOrder.getAgentId() == null) {
			return R.error("代理商ID为空");
		}

		if (clientOrder.getClientId() == null) {
			return R.error("客户ID为空");
		}

		clientOrder.setOrderType(null);
		clientOrder.setStatus(1);

		// 查询列表，过滤掉国际短信的订单和剩余数量为0的订单
		List<JsmsClientOrder> clientOrders = jsmsClientOrderService.findReturnOrderList(clientOrder);
		Iterator iterator = clientOrders.iterator();
		while (iterator.hasNext()) {
			JsmsClientOrder poolPo = (JsmsClientOrder) iterator.next();
			if (poolPo.getProductType().intValue() == ProductType.国际.getValue().intValue()
					|| poolPo.getRemainQuantity().compareTo(BigDecimal.ZERO) <= 0) {
				iterator.remove();
			}
		}

		List<JsmsClientOrderPo> pos = new ArrayList<>();
		for (JsmsClientOrder order : clientOrders) {
			JsmsClientOrderPo po = new JsmsClientOrderPo();
			BeanUtil.copyProperties(order, po);
			pos.add(po);
		}
		return R.ok("获取代理商客户订单成功", pos);
	}

	@ApiOperation(value = "品牌代理商订单回退", notes = "品牌代理商订单回退", tags = "财务管理-回退条数", response = R.class)
	@PostMapping("/returnqty/agentorders/return")
	@ResponseBody
	public R agentOrdersReturn(@RequestBody List<JsmsClientOrderPo> jsmsClientOrderPos, HttpServletRequest request) {
		if (jsmsClientOrderPos == null || jsmsClientOrderPos.isEmpty()) {
			return R.error("参数为空");
		}

		R r;
		try {
			UserSession userSession = (UserSession) request.getSession()
					.getAttribute(SessionEnum.SESSION_USER.toString());
			r = jsmsOrderFinanceService.agentOrderReturnQuantity(jsmsClientOrderPos, userSession.getId(),
					request.getRequestURI(), HttpUtils.getIpAddress(request));
		} catch (JsmsOrderFinanceException e) {
			logger.error("==订单回退条数失败 {}", e);
			r = R.error(e.getMessage());
		} catch (Exception e1) {
			logger.error("==订单回退条数失败 {}", e1);
			r = R.error("回退订单失败，请联系管理员");
		}
		return r;
	}
}