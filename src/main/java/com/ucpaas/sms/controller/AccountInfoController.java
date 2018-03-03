package com.ucpaas.sms.controller;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.jsmsframework.access.access.entity.JsmsClientFailReturn;
import com.jsmsframework.access.enums.RefundStateType;
import com.jsmsframework.access.service.JsmsClientFailReturnService;
import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.Result;
import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.common.entity.JsmsAgentClientParam;
import com.jsmsframework.common.enums.*;
import com.jsmsframework.common.service.JsmsAgentClientParamService;
import com.jsmsframework.finance.entity.JsmsAgentBalanceAlarm;
import com.jsmsframework.finance.entity.JsmsClientBalanceAlarm;
import com.jsmsframework.finance.service.JsmsAgentBalanceAlarmService;
import com.jsmsframework.finance.service.JsmsClientBalanceAlarmService;
import com.jsmsframework.order.entity.JsmsClientOrder;
import com.jsmsframework.order.entity.po.JsmsOemAgentPoolPo;
import com.jsmsframework.order.exception.JsmsOemAgentPoolException;
import com.jsmsframework.order.exception.JsmsOrderFinanceException;
import com.jsmsframework.order.service.JsmsClientOrderService;
import com.jsmsframework.product.entity.JsmsOemAgentProduct;
import com.jsmsframework.product.entity.JsmsOemProductInfo;
import com.jsmsframework.product.service.JsmsOemAgentProductService;
import com.jsmsframework.product.service.JsmsOemProductInfoService;
import com.jsmsframework.sale.credit.constant.SysConstant;
import com.jsmsframework.sale.credit.enums.OauthStatusType;
import com.jsmsframework.sale.credit.service.JsmsSaleCreditService;
import com.jsmsframework.user.entity.*;
import com.jsmsframework.user.service.JsmsAccountService;
import com.jsmsframework.user.service.JsmsAgentInfoService;
import com.jsmsframework.user.service.JsmsClientInfoExtService;
import com.jsmsframework.user.service.JsmsUserPropertyLogService;
import com.ucpaas.sms.common.entity.R;
import com.ucpaas.sms.common.util.*;
import com.ucpaas.sms.common.util.file.ExcelUtils;
import com.ucpaas.sms.common.util.file.FileUtils;
import com.ucpaas.sms.common.util.web.ServletUtils;
import com.ucpaas.sms.constant.LogConstant.LogType;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.entity.access.ClientFailReturn;
import com.ucpaas.sms.entity.message.Account;
import com.ucpaas.sms.entity.message.AccountGroup;
import com.ucpaas.sms.entity.message.AgentInfo;
import com.ucpaas.sms.entity.message.User;
import com.ucpaas.sms.entity.po.AccountPo;
import com.ucpaas.sms.entity.po.ClientBalanceAlarmPo;
import com.ucpaas.sms.entity.po.OemProductInfoPo;
import com.ucpaas.sms.enums.AgentType;
import com.ucpaas.sms.enums.LogEnum;
import com.ucpaas.sms.enums.SessionEnum;
import com.ucpaas.sms.exception.OperationException;
import com.ucpaas.sms.mapper.message.AccountMapper;
import com.ucpaas.sms.model.*;
import com.ucpaas.sms.po.ProductInfo;
import com.ucpaas.sms.po.RechargePo;
import com.ucpaas.sms.po.SaleEntityPo;
import com.ucpaas.sms.service.AgentInfoService;
import com.ucpaas.sms.service.UserService;
import com.ucpaas.sms.service.account.AccountGroupService;
import com.ucpaas.sms.service.account.AccountService;
import com.ucpaas.sms.service.account.ApplyRecordService;
import com.ucpaas.sms.service.admin.AuthorityService;
import com.ucpaas.sms.service.audit.AgentAuditService;
import com.ucpaas.sms.service.audit.CustomerAuditService;
import com.ucpaas.sms.service.common.LogService;
import com.ucpaas.sms.service.customer.CustomerManageService;
import com.ucpaas.sms.service.returnFail.ClientFailReturnService;
import com.ucpaas.sms.util.AgentUtils;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.HttpUtils;
import com.ucpaas.sms.util.RegexUtils;
import com.ucpaas.sms.util.beans.BeanUtil;
import com.ucpaas.sms.util.order.OemOrderIdGenerate;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("/accountInfo")
public class AccountInfoController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(AccountInfoController.class);

    private final ObjectMapper customMapper = new ObjectMapper();
    private final MappingJackson2JsonView view = new MappingJackson2JsonView();
	@Autowired
	private AgentAuditService agentAuditService;
	@Autowired
	private ApplyRecordService applyRecordService;
	@Autowired
	private AccountService accountService;
	@Autowired
	private AccountService accountInfoManageService;
	@Autowired
	private LogService logService;
	@Autowired
	private AccountGroupService accountGroupService;
	@Autowired
	private JsmsAgentBalanceAlarmService agentBalanceAlarmService;
	@Autowired
	private JsmsAgentInfoService agentInfoService;
	@Autowired
	private JsmsOemAgentProductService oemAgentProductService;
	@Autowired
	private JsmsOemProductInfoService oemProductInfoService;
	@Autowired
	private JsmsAgentClientParamService agentClientParamService;
	@Autowired
	private AgentInfoService agentInfoServiceCtrl;
	@Autowired
	private UserService userService;
	@Autowired
	private ClientFailReturnService clientFailReturnService;
	@Autowired
	private JsmsClientFailReturnService jsmsClientFailReturnService;
	@Autowired
	private JsmsUserPropertyLogService jsmsUserPropertyLogService;
	@Autowired
	private JsmsAccountService jsmsAccountService;
	@Autowired
	private JsmsAgentInfoService jsmsAgentInfoService;
	@Autowired
	private JsmsClientOrderService jsmsClientOrderService;
	@Autowired
	private JsmsSaleCreditService jsmsSaleCreditService;
	@Autowired
	private JsmsClientInfoExtService jsmsClientInfoExtService;
	@Autowired
	private JsmsClientBalanceAlarmService jsmsClientBalanceAlarmService;
	@Autowired
	private CustomerManageService customerManageService;
	@Autowired
	private AccountMapper accountMapper;
    @Autowired
    private AuthorityService authorityService;
	@Autowired
    private CustomerAuditService customerAuditService;

	@ApiOperation(value = "客户开户首页(直客开户)", notes = "客户开户", tags = "账户信息管理-客户开户", response = R.class)
	@GetMapping(path = { "/openaccount", "/client/addzk" })
	public ModelAndView openAccount(String type, ModelAndView mv, HttpSession session) {
		mv.setViewName("accountInfo/client/addzk");
		if (StringUtils.isBlank(type)) {
			type = "1";
		}
		mv.addObject("type", type);
		UserSession user = getUserFromSession(session);
		mv.addObject("menus", AgentUtils.hasMenuRight(user, "直客开户-zkkh", "子账户开户-zzhkh", "客户开户-khkh"));
		return mv;
	}

	@ApiOperation(value = "子账户开户", notes = "客户开户", tags = "客户开户-子账户开户", response = R.class)
	@GetMapping(path = { "/client/add" })
	public ModelAndView addZk(String type, ModelAndView mv, HttpSession session) {
		boolean sale = false;
		if (StringUtils.isBlank(type)) {
			type = "1";
		}
		mv.addObject("type", type);

		UserSession user = getUserFromSession(session);
		if(user.getRoles().size()>0){
			for(int i=0;i<user.getRoles().size();i++){
				if(user.getRoles().get(i).getRoleName().equals("销售人员")){
					sale = true;
				}
			}
		}
		if(sale){
			mv.setViewName("accountInfo/client/addSale");
		}else{
			mv.setViewName("accountInfo/client/add");
		}
		mv.addObject("menus", AgentUtils.hasMenuRight(user, "直客开户-zkkh", "直客开户-zkkh", "子账户开户-zzhkh", "客户开户-khkh"));
		return mv;
	}

	// @GetMapping("/openaccount/right")
	// @ResponseBody
	// public R openAccountRight(HttpSession session){
	// UserSession user = getUserFromSession(session);
	// return R.ok("获取客户开户子菜单权限成功", AgentUtils.hasMenuRight(user, "直客开户",
	// "代理商子客户开户", "代理商开户"));
	// }

	/**
	 * 代理管理- 直接跳至代理商管理
	 */
	@GetMapping("/agentmanage")
	public ModelAndView agentManage(ModelAndView mv, HttpSession session) {
		mv.addObject("max_export_num", ConfigUtils.max_export_excel_num);
		mv.setViewName("accountInfo/agent/list");

		UserSession user = getUserFromSession(session);
		mv.addObject("menus", AgentUtils.hasMenuRight(user, "客户管理-dlsgl", "客户申请-dlssq"));
		JsmsMenu jsmsMenu = authorityService.getJsmsMenu("客户管理");
		mv.addObject("jsmsMenu" , jsmsMenu);
		return mv;
	}

	/**
	 * 修改计费规则
	 */
	@GetMapping("/changeRule")
	public ModelAndView changeRule(String clientId, String paytype, ModelAndView mv) {
		mv.addObject("max_export_num", ConfigUtils.max_export_excel_num);
		mv.setViewName("accountInfo/changeRule");
		mv.addObject("clientId", clientId);
		String effectDate = "";
		Integer chargeRule = 0;
		JsmsUserPropertyLog lately = accountService.getLatelyChargeRuleByClientid(clientId);
		if (lately != null) {
			chargeRule = Integer.valueOf(lately.getValue());
			effectDate = new SimpleDateFormat("yyyy-MM-dd").format(lately.getEffectDate());
		} else { // 直客和非直客

			chargeRule = jsmsUserPropertyLogService.getChargeRuleByClientIdAndDate(clientId,
					DateTime.now().toString("yyyyMMdd"));
			if (chargeRule == null) {

				JsmsAccount jsmsAccount = jsmsAccountService.getByClientId(clientId);

				if (jsmsAccount.getAgentId() == null || jsmsAccount.getAgentId() == 0 || jsmsAccount.getAgentId() == 1
						|| jsmsAccount.getAgentId() == 2) { // 直客
					chargeRule = ChargeRuleType.成功量.getValue();
				} else { // 代理商子客户
					chargeRule = ChargeRuleType.提交量.getValue();
				}
			}

		}

		mv.addObject("paytype", paytype);
		mv.addObject("chargeRule", chargeRule);
		mv.addObject("effectDate", effectDate);
		return mv;
	}

	/**
	 * 客户资质认证
	 */
	@GetMapping("/clientauth")
	public ModelAndView clientauth(ModelAndView mv) {
		mv.addObject("max_export_num", ConfigUtils.max_export_excel_num);
		mv.setViewName("accountInfo/client/clientauth");
		return mv;
	}

	/**
	 * 代理商资质认证
	 */
	@GetMapping("/agentauth")
	public ModelAndView agentauth(ModelAndView mv) {
		mv.addObject("max_export_num", ConfigUtils.max_export_excel_num);
		mv.setViewName("accountInfo/agent/agentauth");
		return mv;
	}

	/**
	 * 客户管理- 直接跳至直客管理
	 */
	@GetMapping("/clientmanage")
	public ModelAndView clientManage(ModelAndView mv, HttpSession session) {
		mv.addObject("max_export_num", ConfigUtils.max_export_excel_num);
		mv.setViewName("accountInfo/directclient/list");

		UserSession user = getUserFromSession(session);
		mv.addObject("menus", AgentUtils.hasMenuRight(user, "直客管理-zkgl", "子账户管理-dlszkhgl", "客户组管理-khzgl"));
		JsmsMenu jsmsMenu = authorityService.getJsmsMenu("直客管理");
		mv.addObject("jsmsMenu" , jsmsMenu);
		return mv;
	}

	@RequestMapping(path = "/applyRecord/query", method = RequestMethod.GET)
	public ModelAndView query(ModelAndView mv, HttpSession session) {
		mv.setViewName("accountInfo/applyRecord/list");

		UserSession user = getUserFromSession(session);
		mv.addObject("menus", AgentUtils.hasMenuRight(user, "客户管理-dlsgl", "客户申请-dlssq"));
		return mv;
	}

	@RequestMapping(path = "/applyRecord/list", method = RequestMethod.POST)
	@ResponseBody
	public PageContainer list(String rows, String page, String condition, String applyStatus, String start_time_day,
			String end_time_day) {
		String pageRowCount = rows;
		String currentPage = page;
		Map<String, String> params = new HashMap<String, String>();
		params.put("pageRowCount", rows);
		params.put("currentPage", page);
		if (StringUtils.isEmpty(applyStatus)) {
			applyStatus = "0"; // 待受理
		}
		params.put("applyStatus", applyStatus);
		if (!StringUtils.isEmpty(end_time_day)) {
			end_time_day += " 23:59:59";
		}
		params.put("agentInfo", condition);
		params.put("start_time_day", start_time_day);
		params.put("end_time_day", end_time_day);
		PageContainer pageContainer = applyRecordService.query(params);
		return pageContainer;
	}

	@RequestMapping(path = "/applyRecord/acceptOrNot", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO acceptOrNot(String email, String status, String reason, String applyId, HttpSession session,
			HttpServletRequest request) {
		ResultVO result = ResultVO.failure();
		if ("1".equals(status)) {
			if (StringUtils.isBlank(reason)) {
				result.setMsg("原因不能为空");
				return result;
			} else if (reason.length() > 50) {
				result.setMsg("原因的长度不能大于50个字符");
				return result;
			}
		}
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		Map<String, String> params = new HashMap<String, String>();
		params.put("email", email);
		params.put("status", status);
		params.put("reason", reason);
		params.put("userId", userSession.getId().toString());
		params.put("pageUrl", request.getRequestURI());
		params.put("ip", HttpUtils.getIpAddress(request));
		params.put("applyId", applyId);

		Map resultMap = applyRecordService.acceptOrNot(params);
		String sendEmail = (String) resultMap.get("sendEmail");
		sendEmail = sendEmail == null ? "" : sendEmail;
		if ("success".equals(resultMap.get("result"))) {
			result.setSuccess(true);
			result.setMsg((String) resultMap.get("msg") + sendEmail);
		} else {
			result.setSuccess(false);
			result.setMsg((String) resultMap.get("msg") + sendEmail);
		}
		return result;
	}

	@RequestMapping(path = "/agent/query", method = RequestMethod.GET)
	public ModelAndView agentQuery(ModelAndView mv, HttpSession session) {
		mv.addObject("max_export_num", ConfigUtils.max_export_excel_num);

		UserSession user = getUserFromSession(session);
		mv.addObject("menus", AgentUtils.hasMenuRight(user, "客户管理-dlsgl", "客户申请-dlssq"));
		JsmsMenu jsmsMenu = authorityService.getJsmsMenu("客户管理");
		mv.addObject("jsmsMenu" , jsmsMenu);
		mv.setViewName("accountInfo/agent/list");
		return mv;
	}

	@RequestMapping(path = "/agent/exportExcel", method = RequestMethod.POST)
	public void agentQuery(String condition, String start_time_day, String end_time_day, String minAmount,
			String maxAmount, String agent_type, HttpServletRequest request, HttpServletResponse response) {
		String filePath = ConfigUtils.save_path + "/客户信息报表.xls";
		Map<String, String> params = new HashMap<String, String>();

		params.put("condition", condition);
		if (!StringUtils.isEmpty(end_time_day)) {
			end_time_day += " 23:59:59";
		}
		params.put("start_time_day", start_time_day);
		params.put("end_time_day", end_time_day);
		params.put("minAmount", minAmount);
		params.put("maxAmount", maxAmount);
		params.put("agent_type", agent_type);

		Excel excel = new Excel();
		excel.setPageRowCount(2000); // 设置每页excel显示2000行
		excel.setFilePath(filePath);
		excel.setTitle("客户信息报表");
		StringBuffer buffer = new StringBuffer("查询条件：");
		int count = 0;
		// 判断是否有该查询条件:用户信息
		if (condition != null)
			buffer.append("（").append(++count).append("）客户ID/客户名称/手机号码/销售名字/邮箱：").append(condition);
		// 判断是否有该查询条件:代理商类型
		if (params.get("agent_type") != null)
			buffer.append("（").append(++count).append("）客户类型：")
					.append(ColumnConverUtil.agentTypeToName(params.get("agent_type")));
		// 判断是否有该查询条件:开始时间
		if (start_time_day != null)
			buffer.append("（").append(++count).append("）起始时间：").append(start_time_day);
		// 判断是否有该查询条件:结束时间
		if (end_time_day != null)
			buffer.append("（").append(++count).append("）截止时间：").append(end_time_day);
		// 判断余额范围
		if (params.get("minAmount") != null)
			buffer.append("（").append(++count).append("）最低金额：").append(params.get("minAmount"));
		if (params.get("maxAmount") != null)
			buffer.append("（").append(++count).append("）最高金额：").append(params.get("maxAmount"));
		// 在表格对象中添加查询条件,无查询条件则不添加
		if (count > 0)
			excel.addRemark(buffer.toString());

		excel.addHeader(20, "客户ID", "agent_id");
		excel.addHeader(20, "客户名称", "agent_name");
		excel.addHeader(20, "手机号码", "mobile");
		excel.addHeader(20, "邮箱", "email");
		excel.addHeader(20, "客户状态", "status");
		excel.addHeader(20, "认证状态", "oauthStatus");
		excel.addHeader(20, "客户类型", "agent_type_name");
		excel.addHeader(20, "余额（元）", "balance");
		excel.addHeader(20, "归属销售", "userRealName");
		excel.addHeader(20, "注册时间", "create_time");

		// 权限控制
		UserSession userSession = getUserFromSession(request.getSession());
		params.put("userId", userSession.getId().toString());

		excel.setDataList(accountService.queryAll(params));
		if (ExcelUtils.exportExcel(excel)) {
			FileUtils.download(response, filePath);
			FileUtils.delete(filePath);
		} else {
			String fullContentType = "text/plain;charset=UTF-8";
			response.setContentType(fullContentType);
			try {
				response.getWriter().write("导出Excel文件失败，请联系管理员");
				response.getWriter().flush();
			} catch (IOException e) {
				logger.error("导出Excel文件失败", e);
			}
		}
	}

	@RequestMapping(path = "/agent/list", method = RequestMethod.POST)
	@ResponseBody
	public PageContainer agentList(String rows, String page, String condition, String start_time_day,
			String end_time_day, String minAmount, String maxAmount, String agent_type, HttpSession session) {
		String pageRowCount = rows;
		String currentPage = page;
		BigDecimal balance;
		BigDecimal creditBalance;
		Map<String, String> params = new HashMap<String, String>();
		params.put("pageRowCount", rows);
		params.put("currentPage", page);

		params.put("condition", condition);
		if (!StringUtils.isEmpty(end_time_day)) {
			end_time_day += " 23:59:59";
		}
		params.put("start_time_day", start_time_day);
		params.put("end_time_day", end_time_day);
		params.put("minAmount", minAmount);
		params.put("maxAmount", maxAmount);
		params.put("agent_type", agent_type);

		// 权限控制
		UserSession userSession = getUserFromSession(session);
		params.put("userId", userSession.getId().toString());

		PageContainer pageContainer = accountService.query(params);
		if (pageContainer.getList().size() > 0) {
			for (int i = 0; i < pageContainer.getList().size(); i++) {
				balance = new BigDecimal(String.valueOf(((Map) pageContainer.getList().get(i)).get("balance")));
				creditBalance = new BigDecimal(
						String.valueOf(((Map) pageContainer.getList().get(i)).get("credit_balance")));
				if (balance.intValue() >= 0) {
					((Map) pageContainer.getList().get(i)).put("useable_balance", balance.add(creditBalance));
				} else {
					((Map) pageContainer.getList().get(i)).put("useable_balance", creditBalance);
				}
			}
		}
		return pageContainer;
	}

	@RequestMapping(path = "/agent/updateStatus", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO agentUpdateStatus(String agentId, String status, HttpSession session, HttpServletRequest request) {
		ResultVO result = ResultVO.failure();
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		Map<String, String> params = new HashMap<String, String>();
		params.put("agentId", agentId);
		params.put("status", status);
		params.put("userId", userSession.getId().toString());
		params.put("pageUrl", request.getRequestURI());
		params.put("ip", HttpUtils.getIpAddress(request));

		Map resultMap = accountService.updateStatus(params);
		if ("success".equals(resultMap.get("result"))) {
			result.setSuccess(true);
			result.setMsg((String) resultMap.get("msg"));
		} else {
			result.setSuccess(false);
			result.setMsg((String) resultMap.get("msg"));
		}
		return result;
	}

	/**
	 * 代理商折扣设置
	 *
	 * @param mv
	 * @return
	 */
	@ApiOperation(value = "折扣主页", notes = "折扣主页,返回国际优惠比参数名称（OEM_GJ_SMS_DISCOUNT）<br/>返回代理商ID（agentId）<br/>返回代理商名称（agentName）", tags = "账户信息管理-折扣设置")
	@ApiImplicitParams({ @ApiImplicitParam(name = "agentId", value = "代理商ID", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "agentName", value = "代理商名称", dataType = "String", paramType = "query") })
	@RequestMapping(path = "/agent/discount", method = RequestMethod.GET)
	public ModelAndView discountPage(Integer agentId, String agentName, ModelAndView mv) {
		// 查询出国际短信的优惠比
		JsmsAgentClientParam agentClientParam = agentClientParamService.getByParamKey("OEM_GJ_SMS_DISCOUNT");
		mv.addObject("OEM_GJ_SMS_DISCOUNT", agentClientParam.getParamValue());
		mv.addObject("agentId", agentId);
		String agentName2 = agentInfoService.getByAgentId(agentId).getAgentName();
		mv.addObject("agentName", agentName2);
		mv.setViewName("accountInfo/agent/discount");
		return mv;
	}

	@ApiOperation(value = "折扣列表数据", notes = "折扣列表数据", tags = "账户信息管理-折扣设置", response = JsmsPage.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "page", value = "页码", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "rows", value = "行数", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "agentId", value = "代理商ID", required = true, dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "productName", value = "产品名称", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "productType", value = "产品类型", dataType = "int", paramType = "query") })
	@RequestMapping(path = "/agent/discount/list", method = RequestMethod.POST)
	@ResponseBody
	public JsmsPage discountPageList(JsmsPage page, Integer agentId, String productName, Integer productType) {
		Map<String, Object> params = Maps.newHashMap();
		params.put("agentId", agentId);
		params.put("productName", productName);
		params.put("productType", productType);

		// 状态必须时已上架
		params.put("status", 1);
		params.put("isShow", 1);
		page.setParams(params);
		if (agentId == null) {
			return null;
		}

		JsmsPage<OemProductInfoPo> result = new JsmsPage<>();

		JsmsPage<JsmsOemProductInfo> jsmsPage = oemProductInfoService.findList(page);

		if (!Collections3.isEmpty(jsmsPage.getData())) {
			List<OemProductInfoPo> pos = Lists.newArrayList();
			for (JsmsOemProductInfo jsmsOemProductInfo : jsmsPage.getData()) {
				OemProductInfoPo oemProductInfoPo = new OemProductInfoPo();
				BeanUtil.copyProperties(jsmsOemProductInfo, oemProductInfoPo);

				// 单价保留4位
				if (oemProductInfoPo.getUnitPrice() != null) {
					oemProductInfoPo.setUnitPrice(FmtUtils.roundDown(oemProductInfoPo.getUnitPrice(), 4));
				}

				JsmsOemAgentProduct queryOemAgentProduct = new JsmsOemAgentProduct();
				queryOemAgentProduct.setAgentId(agentId);
				queryOemAgentProduct.setProductId(jsmsOemProductInfo.getProductId());
				JsmsOemAgentProduct oemAgentProduct = oemAgentProductService
						.getByAgentIdAndProductId(queryOemAgentProduct);

				// 折后价保存4位
				if (oemAgentProduct != null && oemAgentProduct.getDiscountPrice() != null) {
					oemAgentProduct.setDiscountPrice(FmtUtils.roundDown(oemAgentProduct.getDiscountPrice(), 4));
				}

				oemProductInfoPo.setOemAgentProduct(oemAgentProduct);
				pos.add(oemProductInfoPo);
			}
			jsmsPage.setData(null);

			BeanUtil.copyProperties(jsmsPage, result);
			result.setData(pos);
		}

		return result;
	}

	/**
	 * 组织架构 - 添加部门保存
	 */
	@ApiOperation(value = "折扣设置保存", notes = "折扣设置保存<br/> 1. 请传入数组例如<br/> [<br/>" + "  {<br/>"
			+ "    \"adminId\": 0,<br/>" + "    \"agentId\": 0,<br/>" + "    \"discountPrice\": 0,<br/>"
			+ "    \"gjSmsDiscount\": 0,<br/>" + "    \"id\": 0,<br/>" + "    \"productId\": 0<br/>" + "  }<br/>"
			+ "] <br/>2. 普通短信的时候gjSmsDiscount不传 <br/> 3. 国际短信的时候discountPrice不传<br/> 4. 若是修改id必传", tags = "账户信息管理-折扣设置", response = R.class)
	@PostMapping("/agent/discount/save")
	@ResponseBody
	public R discountSave(@RequestBody List<JsmsOemAgentProduct> oemAgentProducts, HttpServletRequest request) {
		R r;
		try {
			// 验证对象的安全性
			r = agentInfoServiceCtrl.checkOemAgentProduct(oemAgentProducts);
			if (r == null) {
				UserSession userSession = getUserFromSession(request.getSession());
				for (JsmsOemAgentProduct oemAgentProduct : oemAgentProducts) {
					oemAgentProduct.setAdminId(userSession.getId());
				}
				r = agentInfoServiceCtrl.saveOemAgentProduct(oemAgentProducts, request.getRequestURI(),
						HttpUtils.getIpAddress(request));
			}
		} catch (OperationException e) {
			logger.error("折扣设置保存失败 消息{}", e);
			r = R.error(e.getMessage());
		} catch (Exception e) {
			logger.error("折扣设置保存失败 消息{}", e);
			r = R.error("折扣设置保存失败");
		}
		return r;
	}

	/**
	 * 品牌受理跳转页面
	 *
	 * @return
	 */
	@RequestMapping(path = "/applyRecord/jumpToBrandAcceptPage", method = RequestMethod.GET)
	public ModelAndView jumpToBrandAcceptPage(ModelAndView mv, HttpServletRequest request) {
		Map<String, String> params = ServletUtils.getFormData(request);
		String id = params.get("applyId");
		Map<String, Object> data = applyRecordService.getAgentApplyInfoById(id);
		mv.addObject("data", data);

		List<SaleEntityPo> saleList = applyRecordService.getSaleList();
		mv.addObject("saleList", saleList);
		mv.addObject("applyId", id);
		mv.setViewName("accountInfo/applyRecord/brandAccept");
		return mv;

	}

	/**
	 * 跳转到添加销售页面
	 *
	 * @param mv
	 * @return
	 */
	@RequestMapping(path = "/applyRecord/addSalePage", method = RequestMethod.GET)
	public ModelAndView addSalePage(ModelAndView mv) {
		mv.setViewName("accountInfo/applyRecord/addSalePage");
		return mv;
	}

	/**
	 * 品牌确认受理
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(path = "/applyRecord/brandConfirmAccept", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO brandConfirmAccept(HttpServletRequest request, HttpSession session) {
		Map<String, String> formData = ServletUtils.getFormData(request);
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());

		formData.put("userId", userSession.getId().toString());
		formData.put("pageUrl", request.getRequestURI());
		formData.put("ip", HttpUtils.getIpAddress(request));

		ResultVO result = ResultVO.failure();
		try {
			result = applyRecordService.brandConfirmAccept(formData);
		} catch (OperationException o) {
			o.printStackTrace();
			logger.error(o.getMessage(), o);
			return ResultVO.failure(o.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return ResultVO.failure();
		}

		return result;
	}

	@RequestMapping(path = "/applyRecord/oemConfirmAccept", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO oemConfirmAccept(HttpServletRequest request, HttpSession session) {

		Map<String, String> formData = ServletUtils.getFormData(request);
		System.out.println("formData---------------->" + formData.toString());

		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		formData.put("userId", userSession.getId().toString());
		formData.put("pageUrl", request.getRequestURI());
		formData.put("ip", HttpUtils.getIpAddress(request));

		String productId = formData.get("productId");
		String productNum = formData.get("productNum");
		if ("".equals(productId)) {
			formData.put("productId", null);
		}
		if ("".equals(productNum)) {
			formData.put("productNum", "0");
		}
		ResultVO result = ResultVO.failure();
		try {
			result = applyRecordService.oemConfirmAccept(formData);
		} catch (OperationException o) {
			o.printStackTrace();
			logger.error(o.getMessage(), o);
			return ResultVO.failure(o.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			return ResultVO.failure();
		}
		return result;

	}

	/**
	 * 新增加销售
	 *
	 * @param request
	 * @return
	 */
	@RequestMapping(path = "/applyRecord/addSale", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO addSale(HttpServletRequest request) {

		Map<String, String> formData = ServletUtils.getFormData(request);

		try {
			applyRecordService.addSale(formData);
		} catch (OperationException o) {
			o.printStackTrace();
			logger.error(o.getMessage(), o);
			return ResultVO.failure(o.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			logger.error("添加销售失败----------->" + e.getMessage());
			return ResultVO.failure(e.getMessage());
		}

		return ResultVO.successDefault();
	}

	/**
	 * 判断是否手机号是否已经用了
	 */
	@RequestMapping(path = "/applyRecord/queryByFlagAndmobile", method = RequestMethod.GET)
	@ResponseBody
	public Map queryByFlagAndmobile(HttpServletRequest request, HttpServletResponse response) throws Exception {
		String flag = request.getParameter("flag");
		Integer webId = null;
		if (flag.equals("oem")) {
			webId = 4;
		} else {
			webId = 2;
		}
		Map<String, Object> result = new HashMap<String, Object>();
		// 判断手机号是否存在
		List<User> mUser = userService.checkByMobile(request.getParameter("mobile"), webId.toString());
		if (null != mUser && !mUser.isEmpty()) {
			result.put("fail", "手机号已经被使用");
			return result;
		}
		result.put("success", "成功");
		return result;

	}

	/**
	 * oem受理跳转页面
	 *
	 * @param mv
	 * @return
	 */
	@RequestMapping(path = "/applyRecord/jumpToOemAcceptPage", method = RequestMethod.GET)
	public ModelAndView jumpToOemAcceptPage(ModelAndView mv, HttpServletRequest request) throws Exception {
		Map<String, String> params = ServletUtils.getFormData(request);
		String id = params.get("applyId");
		Map<String, Object> data = applyRecordService.getAgentApplyInfoById(id);
		mv.addObject("data", data);

		List<SaleEntityPo> saleList = applyRecordService.getSaleList();
		mv.addObject("saleList", saleList);
		mv.addObject("applyId", id);

		String oldSaleId = params.get("oldSaleId");
		String oldReason = params.get("oldReason");
		if (oldSaleId != null && !"".equals(oldSaleId)) {
			mv.addObject("oldSaleId", oldSaleId);
		}
		if (oldReason != null && !"".equals(oldReason)) {
			mv.addObject("oldReason", java.net.URLDecoder.decode(oldReason, "UTF-8"));
		}

		mv.setViewName("accountInfo/applyRecord/oemAccept");
		return mv;
	}

	/**
	 * 跳转到oem资料
	 *
	 * @param mv
	 * @return
	 */
	@RequestMapping(path = "/applyRecord/pageOemData", method = RequestMethod.GET)
	public ModelAndView pageOemData(ModelAndView mv, HttpServletRequest request) throws Exception {
		Map<String, String> formData = ServletUtils.getFormData(request);
		String saleId = formData.get("saleId");
		String reason = formData.get("reason");
		String applyId = formData.get("applyId");
		String email = formData.get("email");

		mv.addObject("saleId", saleId);

		if (reason == null || "".equals(reason)) {
			mv.addObject("reason", null);
		} else {
			mv.addObject("reason", java.net.URLDecoder.decode(reason, "UTF-8"));
		}
		mv.addObject("applyId", applyId);
		mv.addObject("email", email);

		// 测试产品列表
		List<ProductInfo> productInfoList = applyRecordService.getProductInfoListForOemData();
		mv.addObject("productInfoList", productInfoList);

		String smspImgUrl = ConfigUtils.smsp_img_url.endsWith("/")
				? ConfigUtils.smsp_img_url.substring(0, ConfigUtils.smsp_img_url.lastIndexOf("/"))
				: ConfigUtils.smsp_img_url;
		mv.addObject("smspImgUrl", smspImgUrl);

		mv.setViewName("accountInfo/applyRecord/oemData");
		return mv;
	}

	@RequestMapping(path = "/agent/view", method = RequestMethod.GET)
	public ModelAndView pageView(ModelAndView mv, Integer agentId) {
		Map data = accountService.queryAgentDetailInfo(agentId);

		Map<String, String> params = new HashMap<String, String>();
		params.put("agentId", agentId.toString());
		Map data1 = accountService.getOEMdataConfig(params);
		String smspImgUrl = ConfigUtils.smsp_img_url.endsWith("/")
				? ConfigUtils.smsp_img_url.substring(0, ConfigUtils.smsp_img_url.lastIndexOf("/"))
				: ConfigUtils.smsp_img_url;
        Map params1 = new HashMap<>();
        params1.put("agent_id", agentId);
        Map data2 = agentAuditService.view(params1);
        data.put("smsp_img_url", smspImgUrl);
        data.putAll(data1);
        if (null != data2) {
			data.put("img_url",data2.get("img_url"));
		} else {
			data.put("img_url", null);
		}
        List<SaleEntityPo> saleList = applyRecordService.getSaleList();
        mv.addObject("saleList", saleList);

		mv.addObject("data", data);
		mv.setViewName("accountInfo/agent/view");
		return mv;
	}

	@RequestMapping(path = "/agent/updateBelongSaleForAgent", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO updateBelongSaleForAgent(HttpServletRequest request) {
		Map<String, String> formData = ServletUtils.getFormData(request);
		Long oldUserId = Long.valueOf(formData.get("oldBelongSale"));
		Long newUserId = Long.valueOf(formData.get("belong_sale"));
		Integer agentId = Integer.valueOf(formData.get("agent_id"));
		ResultVO resultVO = ResultVO.failure();
		if (StringUtils.isEmpty(formData.get("belong_sale"))) {
			return resultVO.setMsg("归属销售不能为空");
		}
		try {
			com.jsmsframework.common.dto.R r = jsmsSaleCreditService.singleBelongSaleChaned(oldUserId, newUserId,
					agentId);
			if (Objects.equals(SysConstant.FAIL_CODE, r.getCode())) {
				return ResultVO.failure(r.getMsg());
			}
			resultVO = applyRecordService.updateBelongSaleForAgent(formData);
		} catch (OperationException o) {
			o.printStackTrace();
			logger.error(o.getMessage(), o);
			return ResultVO.failure(o.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			resultVO.setMsg("系统错误");
		}
		return resultVO;
	}

	@RequestMapping(path = "/client/updateBelongSaleForClient", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO updateBelongSaleForClient(HttpServletRequest request) {

		Map<String, String> formData = ServletUtils.getFormData(request);
		ResultVO resultVO = ResultVO.failure();
		if (StringUtils.isEmpty(formData.get("belong_sale"))) {
			return resultVO.setMsg("归属销售不能为空");
		}
		try {
			resultVO = applyRecordService.updateBelongSaleForClient(formData);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			resultVO.setMsg("系统错误");
		}
		return resultVO;
	}

	@RequestMapping(path = "/agent/updateAgentType", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO agentUpdateAgentType(String agentId, String agentType, HttpSession session,
			HttpServletRequest request) {
		ResultVO result = ResultVO.failure();
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		Map<String, String> params = new HashMap<String, String>();
		params.put("agentId", agentId);
		params.put("agentType", agentType);
		params.put("userId", userSession.getId().toString());
		params.put("pageUrl", request.getRequestURI());
		params.put("ip", HttpUtils.getIpAddress(request));

		Map resultMap = accountService.updateAgentType(params);
		if ("success".equals(resultMap.get("result"))) {
			result.setSuccess(true);
			result.setMsg((String) resultMap.get("msg"));
		} else {
			result.setSuccess(false);
			result.setMsg((String) resultMap.get("msg"));
		}
		return result;
	}

	@RequestMapping(path = "/oem/edit", method = RequestMethod.GET)
	public ModelAndView pageOemEdit(ModelAndView mv, Integer agentId) {
		Map data = accountService.queryAgentDetailInfo(agentId);

		Map<String, String> params = new HashMap<String, String>();
		params.put("agentId", agentId.toString());
		Map data1 = accountService.getOEMdataConfig(params);
		Map params1 = new HashMap<>();
		params1.put("agent_id", agentId);
		Map data2 = agentAuditService.view(params1);
		String smspImgUrl = ConfigUtils.smsp_img_url.endsWith("/")
				? ConfigUtils.smsp_img_url.substring(0, ConfigUtils.smsp_img_url.lastIndexOf("/"))
				: ConfigUtils.smsp_img_url;
		data.put("smsp_img_url", smspImgUrl);
		data.putAll(data1);
		if (null != data2) {
			data.put("img_url",data2.get("img_url"));
		} else {
			data.put("img_url", null);
		}
		mv.addObject("data", data);

		// 测试产品列表
		List<ProductInfo> productInfoList = applyRecordService.getProductInfoListForOemData();
		mv.addObject("productInfoList", productInfoList);

		List<SaleEntityPo> saleList = applyRecordService.getSaleList();
		mv.addObject("saleList", saleList);

		mv.setViewName("accountInfo/oem/edit");
		return mv;
	}

	@RequestMapping(path = "/oem/edit", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO oemEdit(HttpServletRequest request) {
		ResultVO result = ResultVO.failure();
		Map<String, String> params = new HashMap<>();

		for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
			String key = e.nextElement();
			String value = request.getParameter(key);
			params.put(key, value);
		}

		String productId = params.get("productId");
		String productNum = params.get("productNum");
		if ("".equals(productId)) {
			params.put("productId", null);
		}
		if ("".equals(productNum)) {
			params.put("productNum", "0");
		}
		Map resultMap = new HashMap<>();
		try {
			resultMap = accountService.updateAgentInfo(params);
		} catch (Exception e) {
			logger.error("更新OEM资料失败", e);
			result.setSuccess(false);
			result.setMsg(e.getMessage());
			return result;
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

	@RequestMapping(path = "/oem/view", method = RequestMethod.GET)
	public ModelAndView pageOemView(ModelAndView mv, Integer agentId) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("agentId", agentId.toString());
		Map data = accountService.getOEMdataConfig(params);
		String smspImgUrl = ConfigUtils.smsp_img_url.endsWith("/")
				? ConfigUtils.smsp_img_url.substring(0, ConfigUtils.smsp_img_url.lastIndexOf("/"))
				: ConfigUtils.smsp_img_url;
		data.put("smsp_img_url", smspImgUrl);
		mv.addObject("data", data);
		mv.setViewName("accountInfo/oem/view");
		return mv;
	}

	@RequestMapping(value = "/downloadFile")
	public ResponseEntity<byte[]> downloadFile(HttpServletRequest request, HttpSession session) throws IOException {

		Map<String, String> formData = ServletUtils.getFormData(request);
		String fName = formData.get("fileName");

		String fileName;
		File downloadFile;
		if ("sms-api".equals(fName)) {
			String path = request.getSession().getServletContext().getRealPath("/template/sms-api.docx");
			fileName = "SMS_API_DOCUMENT_V1.0.docx";
			downloadFile = new File(path);
		} else {
			String path = request.getSession().getServletContext().getRealPath("/template/sms-api.docx");
			fileName = "SMS_API_DOCUMENT_V1.0.docx";
			downloadFile = new File(path);
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDispositionFormData("attachment", new String(fileName.getBytes(), "utf-8"));

		return new ResponseEntity<byte[]>(org.apache.commons.io.FileUtils.readFileToByteArray(downloadFile), headers,
				HttpStatus.CREATED);
	}

	@RequestMapping(path = "/client/query", method = RequestMethod.GET)
	public ModelAndView clientQuery(ModelAndView mv, HttpSession session) {
		mv.addObject("max_export_num", ConfigUtils.max_export_excel_num);
		mv.setViewName("accountInfo/client/list");
		UserSession user = getUserFromSession(session);
		mv.addObject("menus", AgentUtils.hasMenuRight(user, "直客管理-zkgl", "子账户管理-dlszkhgl", "客户组管理-khzgl"));
		JsmsMenu jsmsMenu = authorityService.getJsmsMenu("子账户管理");
		mv.addObject("jsmsMenu" , jsmsMenu);
		return mv;
	}

	@RequestMapping(path = "/client/list", method = RequestMethod.POST)
	@ResponseBody
	public PageContainer clientList(String rows, String page, String condition, String start_time_day,
			String end_time_day, String paytype, String charge_rule, String extValue, String agent_type, String status,
			String starLevel, String flag, HttpSession session) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("pageRowCount", rows);
		params.put("currentPage", page);

		params.put("condition", condition);
		if (!StringUtils.isEmpty(end_time_day)) {
			end_time_day += " 23:59:59";
		}
		if (start_time_day != null && StringUtils.isNotBlank(start_time_day)) {
			params.put("start_time_day", start_time_day);
		}
		if (end_time_day != null && StringUtils.isNotBlank(end_time_day)) {
			params.put("end_time_day", end_time_day);
		}
		if (paytype != null && StringUtils.isNotBlank(paytype)) {
			params.put("paytype", paytype);
		}
		if (charge_rule != null && StringUtils.isNotBlank(charge_rule)) {
			params.put("charge_rule", charge_rule);
		}
		if (agent_type != null && StringUtils.isNotBlank(agent_type)) {
			params.put("agent_type", agent_type);
		}
		if (extValue != null && StringUtils.isNotBlank(extValue)) {
			params.put("extValue", extValue);
		}
		if (status != null && StringUtils.isNotBlank(status)) {
			params.put("status", status);
		}
		if (starLevel != null && StringUtils.isNotBlank(starLevel)) {
			params.put("starLevel", starLevel);
		}
		// 权限控制
		UserSession userSession = getUserFromSession(session);
		params.put("userId", userSession.getId().toString());

		String chargeRule = params.get("charge_rule");
		if (StringUtils.isNotBlank(chargeRule)) {
			params.put("property", "charge_rule");
			params.put("value", chargeRule);
		}
		PageContainer pageContainer = accountInfoManageService.queryClientInfo(params);
		return pageContainer;
	}

	@RequestMapping(path = "/client/orderRemain", method = RequestMethod.GET)
	public ModelAndView clientOrderRemain(ModelAndView mv, String clientId, String agent_type) {
		mv.addObject("clientId", clientId);
		mv.addObject("agent_type", agent_type);
		mv.setViewName("accountInfo/client/orderRemain");
		return mv;
	}

	@RequestMapping(path = "/client/orderRemain", method = RequestMethod.POST)
	@ResponseBody
	public PageContainer clientOrderRemain(@RequestParam Map map, HttpSession session) {

		map.put("orderByClause", "remain_quantity desc");

		return accountService.clientOrderRemain(map);
	}

	@RequestMapping(path = "/client/orderRemainIN", method = RequestMethod.GET)
	public ModelAndView clientOrderRemainIN(ModelAndView mv, String clientId, String agent_type) {
		mv.addObject("clientId", clientId);
		mv.addObject("agent_type", agent_type);
		mv.setViewName("accountInfo/client/orderRemainIN");
		return mv;
	}

	@RequestMapping(path = "/client/orderRemainIN", method = RequestMethod.POST)
	@ResponseBody
	public PageContainer clientOrderRemainIN(@RequestParam Map map, HttpSession session) {
		map.put("productType", "2");

		map.put("orderByClause", "remain_quantity desc");

		return accountService.clientOrderRemain(map);

	}

	@RequestMapping(path = "/client/view", method = RequestMethod.GET)
	public ModelAndView pageClientView(ModelAndView mv, String clientId) {
		AccountPo data = accountInfoManageService.getClientInfo(clientId);

		List<SaleEntityPo> saleList = applyRecordService.getSaleList();
		mv.addObject("saleList", saleList);
		Map params = new HashMap<>();
		params.put("client_id", clientId);
		params.put("type", "2");
		Map data1 = customerAuditService.view(params);
		mv.addObject("data", data);
		mv.addObject("smsp_img_url",data1.get("smsp_img_url"));
		mv.addObject("img_url",data1.get("img_url"));

		if(String.valueOf(data1.get("id_type")).equals("1")){
			mv.addObject("id_type","身份证");
		}else if(String.valueOf(data1.get("id_type")).equals("2")){
			mv.addObject("id_type","护照");
		}else if(String.valueOf(data1.get("id_type")).equals("3")){
			mv.addObject("id_type","组织机构证");
		}else if(String.valueOf(data1.get("id_type")).equals("4")){
			mv.addObject("id_type","税务登记证");
		}else if(String.valueOf(data1.get("id_type")).equals("5")){
			mv.addObject("id_type","营业执照");
		}else if(String.valueOf(data1.get("id_type")).equals("6")){
			mv.addObject("id_type","三证合一(企业)");
		}else if(String.valueOf(data1.get("id_type")).equals("7")){
			mv.addObject("id_type","四证合一(企业)");
		}else if(String.valueOf(data1.get("id_type")).equals("8")){
			mv.addObject("id_type","登记证书号");
		}
		mv.addObject("id_nbr",data1.get("id_nbr"));
		mv.setViewName("accountInfo/client/view");
		return mv;
	}

	/**
	 * 修改客户密码
	 */
	@PostMapping("/client/updatePsd")
	@ResponseBody
	public com.jsmsframework.common.dto.ResultVO clientUpdatePsd(@RequestParam Map<String, String> params,
			HttpServletRequest request) {
		String newPsd = params.get("newPsd");
		String confirmPsd = params.get("confirmPsd");
		String clientId = params.get("clientId");

		if (org.apache.commons.lang3.StringUtils.isBlank(clientId)) {
			return com.jsmsframework.common.dto.ResultVO.failure("参数缺失!");
		}
		if (org.apache.commons.lang3.StringUtils.isBlank(newPsd)) {
			return com.jsmsframework.common.dto.ResultVO.failure("密码不能为空");
		}
		if (!newPsd.equals(confirmPsd)) {
			return com.jsmsframework.common.dto.ResultVO.failure("两次密码不一致");
		}
		JsmsAccount account = new JsmsAccount();
		account.setClientid(clientId);
		account.setPassword(newPsd);
		return accountService.updatePsd(account);
	}

	/**
	 * 修改接口密码
	 */
	@PostMapping("/client/updateWebPsd")
	@ResponseBody
	public com.jsmsframework.common.dto.ResultVO clientupdateWebPsd(@RequestParam Map<String, String> params,
			HttpServletRequest request) {
		String newPsd = params.get("newPsd");
		String confirmPsd = params.get("confirmPsd");
		String clientId = params.get("clientId");

		if (org.apache.commons.lang3.StringUtils.isBlank(clientId)) {
			return com.jsmsframework.common.dto.ResultVO.failure("参数缺失!");
		}
		if (org.apache.commons.lang3.StringUtils.isBlank(newPsd)) {
			return com.jsmsframework.common.dto.ResultVO.failure("密码不能为空");
		}
		if (!newPsd.equals(confirmPsd)) {
			return com.jsmsframework.common.dto.ResultVO.failure("两次密码不一致");
		}
		JsmsClientInfoExt account = new JsmsClientInfoExt();
		account.setClientid(clientId);
		account.setWebPassword(newPsd);
		return accountService.updateWebPsd(account);
	}

	@GetMapping("/client/edit")
	public ModelAndView pageClientEdit(ModelAndView mv, String clientId) {
		int count = 0;
		AccountPo data = accountInfoManageService.getClientInfo(clientId);
		List<SaleEntityPo> saleList = applyRecordService.getSaleList();
		mv.addObject("saleList", saleList);

		List<AgentInfo> agentInfoList = null;
		if (StringUtils.isNotBlank(data.getAgentType())) {
			agentInfoList = accountService.findAgentInfoList(Integer.parseInt(data.getAgentType()));
		}
		count = accountService.getparentId(clientId);
		mv.addObject("count", count);
		mv.addObject("agentInfoList", agentInfoList);
		mv.addObject("data", data);
		Map params = new HashMap<>();
		params.put("client_id", clientId);
		params.put("type", "2");
		Map data1 = customerAuditService.view(params);
		mv.addObject("data", data);
		mv.addObject("smsp_img_url",data1.get("smsp_img_url"));
		mv.addObject("img_url",data1.get("img_url"));

		if(String.valueOf(data1.get("id_type")).equals("1")){
			mv.addObject("id_type","身份证");
		}else if(String.valueOf(data1.get("id_type")).equals("2")){
			mv.addObject("id_type","护照");
		}else if(String.valueOf(data1.get("id_type")).equals("3")){
			mv.addObject("id_type","组织机构证");
		}else if(String.valueOf(data1.get("id_type")).equals("4")){
			mv.addObject("id_type","税务登记证");
		}else if(String.valueOf(data1.get("id_type")).equals("5")){
			mv.addObject("id_type","营业执照");
		}else if(String.valueOf(data1.get("id_type")).equals("6")){
			mv.addObject("id_type","三证合一(企业)");
		}else if(String.valueOf(data1.get("id_type")).equals("7")){
			mv.addObject("id_type","四证合一(企业)");
		}else if(String.valueOf(data1.get("id_type")).equals("8")){
			mv.addObject("id_type","登记证书号");
		}
		mv.addObject("id_nbr",data1.get("id_nbr"));
		mv.setViewName("accountInfo/client/edit");
		return mv;
	}

	/**
	 * 修改客户-保存
	 */
	@PostMapping("/client/edit")
	@ResponseBody
	public R pageClientEdit(@RequestBody Account account, Model model, HttpServletRequest request) {
		R r;
		if (account == null) {
			return R.error("客户信息不能为空！");
		}
		if(null==account.getAccessSpeed()){
			account.setAccessSpeed(100);
		}

		// 验证对象的安全性
		String checkResult = accountService.checkClient(account);
		if (com.ucpaas.sms.common.util.StringUtils.isNotBlank(checkResult)) {
			return R.error(checkResult);
		}
		try {
			UserSession userSession = (UserSession) request.getSession()
					.getAttribute(SessionEnum.SESSION_USER.toString());
			Map<String, Object> result = accountService.updateClient(account, userSession.getId(),
					request.getRequestURI(), HttpUtils.getIpAddress(request));
			r = result.get("result").toString().equals("success") ? R.ok(result.get("msg").toString())
					: R.error(result.get("msg").toString());
		} catch (OperationException e) {
			logger.error("修改客户失败 客户ID{} 消息{}", account.getClientid(), e);
			r = R.error(e.getMessage());
		} catch (Exception e) {
			logger.error("修改客户失败 客户ID{} 消息{}", account.getClientid(), e);
			r = R.error("修改客户失败");
		}
		return r;
	}

	/**
	 * 修改任务状态：解冻，冻结，注销
	 *
	 * @return
	 */
	@RequestMapping(path = "/client/updateStatus", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO clientUpdateStatus(String clientId, String status, HttpSession session,
			HttpServletRequest request) {
		ResultVO result = ResultVO.failure();
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		logger.debug("客户ID：{};  状态：{}", clientId, status);

		try {
			doUpdateStatus(clientId, status, result);
		} catch (Exception e) {
			result.setMsg("系统操作超时");
			logger.debug("更新用户状态失败,参数 --->客户ID：{};  变更状态：{}  ; {}", clientId, status, e);
		}
		logService.add(LogType.update, LogEnum.账户信息管理.getValue(), userSession.getId(), request.getRequestURI(),
				HttpUtils.getIpAddress(request), "账户信息管理-客户管理：修改账户状态", clientId, status);
		return result;
	}

	@RequestMapping(path = "/client/exportExcel")
	public void clientExportExcel(String condition, String start_time_day, String end_time_day, String paytype,
			String agent_type, String charge_rule, String extValue, String status, String star_level,
			HttpServletRequest request, HttpServletResponse response) {
		Map<String, String> params = new HashMap<String, String>();

		params.put("condition", condition);
		if (!StringUtils.isEmpty(end_time_day)) {
			end_time_day += " 23:59:59";
		}
		params.put("start_time_day", start_time_day);
		params.put("end_time_day", end_time_day);
		params.put("paytype", paytype);
		params.put("agent_type", agent_type);
		if (status != null && StringUtils.isNotBlank(status)) {
			params.put("status", status);
		}
		if (star_level != null && StringUtils.isNotBlank(star_level)) {
			params.put("starLevel", star_level);
		}
		if (StringUtils.isNotBlank(extValue)) {
			params.put("extValue", extValue);
		}
		if (org.apache.commons.lang3.StringUtils.isNotBlank(charge_rule)) {
			params.put("property", "charge_rule");
			params.put("value", charge_rule);
		}
		String filePath = ConfigUtils.save_path + "/子账户信息报表.xls";

		Excel excel = new Excel();
		excel.setPageRowCount(2000); // 设置每页excel显示2000行
		excel.setFilePath(filePath);
		excel.setTitle("子账户信息报表");

		StringBuffer buffer = new StringBuffer("查询条件：");
		int count = 0;
		// 判断是否有该查询条件:用户信息
		if (StringUtils.isNotEmpty(condition))
			buffer.append("（").append(++count).append("）子账户ID/子账户名称/手机号码/销售名字/所属客户ID：").append(condition);
		// 判断是否有该查询条件:代理商类型

		if (StringUtils.isNotEmpty(params.get("agent_type")))
			buffer.append("（").append(++count).append("）所属客户类型：")
					.append(ColumnConverUtil.agentTypeToName(params.get("agent_type")));
		// 判断是否有该查询条件:代理商ID
		if (StringUtils.isNotEmpty(params.get("agent_info")))
			buffer.append("（").append(++count).append("）付费类型：").append(params.get("paytype"));
		// 判断是否有该查询条件:计费类型
		if (StringUtils.isNotEmpty(charge_rule)) {
			if (charge_rule.equals("0")) {
				buffer.append("（").append(++count).append("）计费规则：提交量");
			} else if (charge_rule.equals("1")) {
				buffer.append("（").append(++count).append("）计费规则：成功量");
			} else if (charge_rule.equals("2")) {
				buffer.append("（").append(++count).append("）计费规则：明确成功量");
			}
		}
		// 判断是否有该查询条件:是否支持子客户
		if (StringUtils.isNotBlank(extValue))
			if (extValue.equals("0")) {
				buffer.append("（").append(++count).append("）是否支持子客户：不支持");
			} else if (extValue.equals("1")) {
				buffer.append("（").append(++count).append("）是否支持子客户：支持");
			}
		// 判断是否有该查询条件:客户状态
		if (StringUtils.isNotBlank(status))
			if (status.equals("1")) {
				buffer.append("（").append(++count).append("）子账户状态：已启用");
			} else if (status.equals("5")) {
				buffer.append("（").append(++count).append("）子账户状态：已冻结");
			} else if (status.equals("6")) {
				buffer.append("（").append(++count).append("）子账户状态：已注销");
			} else if (status.equals("7")) {
				buffer.append("（").append(++count).append("）子账户状态：已锁定");
			}
		// 判断是否有该查询条件:客户星级
		if (StringUtils.isNotBlank(star_level))
			if (star_level.equals("1")) {
				buffer.append("（").append(++count).append("）客户等级：1星");
			} else if (star_level.equals("2")) {
				buffer.append("（").append(++count).append("）客户等级：2星");
			} else if (star_level.equals("3")) {
				buffer.append("（").append(++count).append("）客户等级：3星");
			} else if (star_level.equals("4")) {
				buffer.append("（").append(++count).append("）客户等级：4星");
			} else if (star_level.equals("5")) {
				buffer.append("（").append(++count).append("）客户等级：5星");
			}
		// 判断是否有该查询条件:开始时间
		if (StringUtils.isNotEmpty(start_time_day))
			buffer.append("（").append(++count).append("）起始时间：").append(start_time_day);
		// 判断是否有该查询条件:结束时间
		if (StringUtils.isNotEmpty(end_time_day))
			buffer.append("（").append(++count).append("）截止时间：").append(end_time_day);
		// 在表格对象中添加查询条件,无查询条件则不添加
		if (count > 0)
			excel.addRemark(buffer.toString());

		excel.addHeader(20, "子账户ID", "clientid");
		excel.addHeader(20, "子账户名称", "name");
		excel.addHeader(20, "客户等级", "starLevelStr");
		excel.addHeader(20, "手机号码", "mobile");
		excel.addHeader(20, "子账户状态", "statusName");
		excel.addHeader(20, "认证状态", "oauth_status");
		excel.addHeader(20, "所属客户ID", "agent_id");
		excel.addHeader(20, "所属客户类型", "agent_type_name");
		excel.addHeader(20, "归属销售", "userRealName");
		excel.addHeader(20, "计费规则", "valueStr");
		excel.addHeader(20, "付费类型", "paytypeDesc");
		excel.addHeader(20, "是否支持子客户", "extValueStr");
		excel.addHeader(20, "普通类型(条)", "normalType");
		excel.addHeader(20, "国际短信(元)", "interType");
		excel.addHeader(20, "创建时间", "createtime");

		// 权限控制
		UserSession userSession = getUserFromSession(request.getSession());
		params.put("userId", userSession.getId().toString());

		excel.setDataList(accountInfoManageService.queryAllClientInfo(params));
		if (ExcelUtils.exportExcel(excel)) {
			FileUtils.download(response, filePath);
			FileUtils.delete(filePath);
		} else {
			String fullContentType = "text/plain;charset=UTF-8";
			response.setContentType(fullContentType);
			try {
				response.getWriter().write("导出Excel文件失败，请联系管理员");
				response.getWriter().flush();
			} catch (IOException e) {
				logger.error("导出Excel文件失败", e);
			}
		}
	}

	/**
	 * 获取销售人员名称列表
	 */
	@RequestMapping("/getSaleList")
	@ResponseBody
	public List<SaleEntityPo> getSaleList() {

		List<SaleEntityPo> saleList = applyRecordService.getSaleList();
		return saleList;
	}

	// /**
	// * 添加客户-视图
	// */
	// @GetMapping("/client/add")
	// public ModelAndView addClientView(String type, ModelAndView mv,
	// HttpSession session) {
	// mv.setViewName("accountInfo/client/add");
	// if (StringUtils.isBlank(type)){
	// type = "";
	// }
	// mv.addObject("type", type);
	//
	// UserSession user = getUserFromSession(session);
	// mv.addObject("menus", AgentUtils.hasMenuRight(user, "直客开户", "代理商子客户开户",
	// "代理商开户"));
	// return mv;
	// }

	/**
	 * 添加客户-视图
	 */
	@GetMapping("/client/add/data")
	@ResponseBody
	public R addClientData(ModelAndView mv) {
		Map<String, Object> result = Maps.newHashMap();

		// 查询归属销售
		List<SaleEntityPo> saleList = applyRecordService.getSaleList();

		// 查询oem代理商，过滤冻结和注销的、注册状态为已认证、用户不为禁用的
		List<AgentInfo> agentInfoList = accountService.findAgentInfoList(5);

		result.put("saleList", saleList);
		result.put("agentInfoList", agentInfoList);
		return R.ok("成功", result);
	}

	@GetMapping("/client/add/data2")
	@ResponseBody
	public R addClientData2(ModelAndView mv,HttpSession session ) {
		Map<String, Object> result = Maps.newHashMap();
		// 查询归属销售
		List<SaleEntityPo> saleList = getSaleList();
		Long belongSale = null;
		// 查询oem代理商，过滤冻结和注销的、注册状态为已认证、用户不为禁用的
		List<AgentInfo> agentInfoList = accountService.findAgentInfoList2(5);
		List<AgentInfo> agentInfoSaleList = new ArrayList<>();
		UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		if(user.getRoles().size()>0){
			for(int i=0;i<user.getRoles().size();i++){
				if(user.getRoles().get(i).getRoleName().equals("销售人员")){
					saleList.clear();
					SaleEntityPo saleEntityPo = new SaleEntityPo();
					saleEntityPo.setId(String.valueOf(user.getId()));
					saleEntityPo.setName(user.getRealname());
					saleList.add(saleEntityPo);
					belongSale = user.getId();
				}
			}
		}
		if(belongSale != null){
        if(agentInfoList.size()>0){
        	for(int i=0;i<agentInfoList.size();i++){
        		if(agentInfoList.get(i).getBelongSale()!=null){
					if(agentInfoList.get(i).getBelongSale().equals(belongSale)){
						agentInfoSaleList.add(agentInfoList.get(i));
					}
				}
			}
		}
			result.put("agentInfoList", agentInfoSaleList);
		}else{
			result.put("agentInfoList", agentInfoList);
		}

		result.put("saleList", saleList);
		return R.ok("成功", result);
	}

	/**
	 * 添加客户-保存
	 */
	@ApiOperation(value = "代理商子客户、直客开户", notes = "代理商子客户、直客开户", tags = "账户信息管理-客户开户", response = R.class)
	@PostMapping("/client/add")
	@ResponseBody
	public R saveClient(@RequestBody Account account, Model model, HttpServletRequest request,HttpSession session) {
		R result;
		boolean sale = false;
		if (account == null) {
			return R.error("客户信息不能为空！");
		}
		//接入速度没填值时默认设置100
		if(null==account.getAccessSpeed()){
			account.setAccessSpeed(100);
		}
		//IP白名单没值，默认*
		if(null==account.getIp()){
			account.setIp("*");
		}
		UserSession user = getUserFromSession(session);
		if(user.getRoles().size()>0){
			for(int i=0;i<user.getRoles().size();i++){
				if(user.getRoles().get(i).getRoleName().equals("销售人员")){
					if(account.getAgentId()!=null){//直客除外
						if((StringUtils.isNotBlank(String.valueOf(account.getAgentId())))){
							JsmsClientInfoExt clientInfoExt = new JsmsClientInfoExt();
							clientInfoExt.setExtValue(0);
							sale = true;
							account.setNeedextend(0);
							account.setPaytype(0);
							account.setExtValue(0);
							account.setClientInfoExt(clientInfoExt);
						}
					}
				}
			}
		}
		try {
			// 验证对象的安全性
			if (!beanValidator(model, account)) {
				return R.error(model.asMap().get("message").toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			return R.error("参数错误");
		}

		try {
			result = accountService.addClient(account, getUserFromSession(request.getSession()).getId(),
					request.getRequestURI(), HttpUtils.getIpAddress(request),sale);
			//boolean isSuccess = result.getMsg().equals("success");

			/**
			 * 判断是否审核通过
			 */
//			if (account.getOauthStatus() != null) {
//				if (isSuccess && account.getOauthStatus().equals(3)) {
//					logger.debug("开始赠送短信给 {}-----------------------------------> start", account.getClientid());
//					try {
//						//赠送测试短信
//						//customerAuditService.giveShortMessage(account);
//						// Mod by lpjLiu: 生成订单ID
//						List<Long> orderIds = new ArrayList<>();
//						for (int j = 0; j < 3; j++) {
//							orderIds.add(customerAuditService.getOemAgentOrderId());
//						}
//						jsmsAccountFinanceService.giveShortMessage();
//					} catch (Exception e) {
//						logger.error("添加客户成功 客户ID {}, 赠送短信失败 --> {}", account.getClientid(), e);
//						e.printStackTrace();
//					}
//					logger.debug("赠送短信结束  ======================================> end");
//				}
//			}
			// r = isSuccess ? R.ok(result.get("msg").toString(), account) :
			// R.error(result.get("msg").toString());
		} catch (OperationException e) {
			logger.error("添加客户失败 客户ID {} 消息 {}", account.getClientid(), e);
			return R.error(e.getMessage());
		} catch (Exception e) {
			logger.error("添加客户失败 客户ID {} 消息 {}", account.getClientid(), e);
			return R.error("添加客户失败");
		}
		return result;
	}

	/**
	 * 直客管理
	 */
	@ApiOperation(value = "直客页面", notes = "直客页面", tags = "账户信息管理-直客管理", response = ModelAndView.class)
	@GetMapping("/directclient/query")
	public ModelAndView directclientQuery(ModelAndView mv, HttpSession session) {
		mv.addObject("max_export_num", ConfigUtils.max_export_excel_num);
		mv.setViewName("accountInfo/directclient/list");
		UserSession user = getUserFromSession(session);
		mv.addObject("menus", AgentUtils.hasMenuRight(user, "直客管理-zkgl", "子账户管理-dlszkhgl", "客户组管理-khzgl"));
		JsmsMenu jsmsMenu = authorityService.getJsmsMenu("直客管理");
		mv.addObject("jsmsMenu" , jsmsMenu);
		return mv;
	}

	/**
	 * 直客管理首页
	 */
	@ApiOperation(value = "直客页面列表查询", notes = "直客页面列表查询", tags = "账户信息管理-直客管理", response = Page.class)
	@PostMapping("/directclient/list")
	@ResponseBody
	public PageContainer directclientList(@RequestParam Map params, HttpSession session) {
		String end_time_day = "";
		Object obj = params.get("end_time_day");
		if (obj != null && StringUtils.isNotBlank(obj.toString())) {
			end_time_day += obj.toString() + " 23:59:59";
			params.put("end_time_day", end_time_day);
		}
		params.put("pageRowCount", params.get("rows"));
		params.put("currentPage", params.get("page"));
		String chargeRule = (String) params.get("charge_rule");
		if (org.apache.commons.lang3.StringUtils.isNotBlank(chargeRule)) {
			params.put("property", "charge_rule");
			params.put("value", chargeRule);
		}

		// 权限控制
		UserSession userSession = getUserFromSession(session);
		params.put("userId", userSession.getId().toString());
		PageContainer pageContainer = accountService.findDirectclientList(params);
		return pageContainer;
		// return accountService.findDirectclientList(page);
	}

	/**
	 * 直客管理明细
	 */
	@ApiOperation(value = "直客详情页面", notes = "直客详情页面", tags = "账户信息管理-直客管理", response = ModelAndView.class)
	@GetMapping("/directclient/view")
	public ModelAndView pageDirectclientView(ModelAndView mv, String clientId) {
		// 查询直客的详情信息
		AccountPo data = accountService.getDirectclientDetailInfo(clientId);
		Map params = new HashMap<>();
		params.put("client_id", clientId);
		params.put("type", "3");
		Map data1 = customerAuditService.view(params);
		mv.addObject("data", data);
		mv.addObject("smsp_img_url",data1.get("smsp_img_url"));
		mv.addObject("img_url",data1.get("img_url"));

		if(String.valueOf(data1.get("id_type")).equals("1")){
			mv.addObject("id_type","身份证");
		}else if(String.valueOf(data1.get("id_type")).equals("2")){
			mv.addObject("id_type","护照");
		}else if(String.valueOf(data1.get("id_type")).equals("3")){
			mv.addObject("id_type","组织机构证");
		}else if(String.valueOf(data1.get("id_type")).equals("4")){
			mv.addObject("id_type","税务登记证");
		}else if(String.valueOf(data1.get("id_type")).equals("5")){
			mv.addObject("id_type","营业执照");
		}else if(String.valueOf(data1.get("id_type")).equals("6")){
			mv.addObject("id_type","三证合一(企业)");
		}else if(String.valueOf(data1.get("id_type")).equals("7")){
			mv.addObject("id_type","四证合一(企业)");
		}else if(String.valueOf(data1.get("id_type")).equals("8")){
			mv.addObject("id_type","登记证书号");
		}
		mv.addObject("id_nbr",data1.get("id_nbr"));
		mv.addObject("data", data);
		mv.setViewName("accountInfo/directclient/view");

		return mv;
	}

	/**
	 * 直客管理明细
	 */
	@ApiOperation(value = "直客修改页面", notes = "直客修改页面", tags = "账户信息管理-直客管理", response = ModelAndView.class)
	@GetMapping("/directclient/edit")
	public ModelAndView pageDirectclientEdit(ModelAndView mv, String clientId) {
		int count = 0;
		// 查询归属销售
		List<SaleEntityPo> saleList = applyRecordService.getSaleList();
		mv.addObject("saleList", saleList);
		// 查询直客的详情信息
		AccountPo data = accountService.getDirectclientDetailInfo(clientId);
		count = accountService.getparentId(clientId);
		mv.addObject("count", count);
		Map params = new HashMap();
		params.put("client_id", clientId);
		params.put("type", "3");
		Map data1 = customerAuditService.view(params);
		mv.addObject("data", data);
		mv.addObject("smsp_img_url",data1.get("smsp_img_url"));
		mv.addObject("img_url",data1.get("img_url"));

		if(String.valueOf(data1.get("id_type")).equals("1")){
			mv.addObject("id_type","身份证");
		}else if(String.valueOf(data1.get("id_type")).equals("2")){
			mv.addObject("id_type","护照");
		}else if(String.valueOf(data1.get("id_type")).equals("3")){
			mv.addObject("id_type","组织机构证");
		}else if(String.valueOf(data1.get("id_type")).equals("4")){
			mv.addObject("id_type","税务登记证");
		}else if(String.valueOf(data1.get("id_type")).equals("5")){
			mv.addObject("id_type","营业执照");
		}else if(String.valueOf(data1.get("id_type")).equals("6")){
			mv.addObject("id_type","三证合一(企业)");
		}else if(String.valueOf(data1.get("id_type")).equals("7")){
			mv.addObject("id_type","四证合一(企业)");
		}else if(String.valueOf(data1.get("id_type")).equals("8")){
			mv.addObject("id_type","登记证书号");
		}
		mv.addObject("id_nbr",data1.get("id_nbr"));
		mv.addObject("data", data);
		mv.setViewName("accountInfo/directclient/edit");
		return mv;
	}

	/**
	 * 修改客户-保存
	 */
	@ApiOperation(value = "修改客户计费", notes = "修改客户计费，account对象，参数 clientid,chargeRule,effectDate", tags = "账户信息管理-客户管理", response = R.class)
	@PostMapping("/client/edit/chargerule")
	@ResponseBody
	public R pageDirectclientEditChargeRule(@RequestBody Account account, Model model, HttpServletRequest request) {
		R r;
		if (account == null) {
			return R.error("信息不能为空！");
		}

		try {
			UserSession userSession = (UserSession) request.getSession()
					.getAttribute(SessionEnum.SESSION_USER.toString());

			r = accountService.updateClienttChargeRule(account, userSession.getId(), request.getRequestURI(),
					HttpUtils.getIpAddress(request));
		} catch (OperationException e) {
			logger.error("修改客户计费失败 客户ID{} 消息{}", account.getClientid(), e);
			r = R.error(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("修改客户计费失败 客户ID{} 消息{}", account.getClientid(), e);
			r = R.error("修改客户计费失败");
		}
		return r;
	}

	/**
	 * 修改客户-保存
	 */
	@ApiOperation(value = "直客修改保存", notes = "直客修改保存", tags = "账户信息管理-直客管理", response = R.class)
	@PostMapping("/directclient/edit")
	@ResponseBody
	public R pageDirectclientEdit(@RequestBody Account account, Model model, HttpServletRequest request) {
		R r;
		if (account == null) {
			return R.error("直客信息不能为空！");
		}
		// 验证对象的安全性
		String checkResult = accountService.checkDirectclient(account);
		if (StringUtils.isNotBlank(checkResult)) {
			return R.error(checkResult);
		}
		try {
			// 如果是不支持更新为支持
			UserSession userSession = (UserSession) request.getSession()
					.getAttribute(SessionEnum.SESSION_USER.toString());
			Map<String, Object> result = accountService.updateClient(account, userSession.getId(),
					request.getRequestURI(), HttpUtils.getIpAddress(request));
			r = result.get("result").toString().equals("success") ? R.ok(result.get("msg").toString())
					: R.error(result.get("msg").toString());
		} catch (OperationException e) {
			logger.error("修改直客失败 客户ID{} 消息{}", account.getClientid(), e);
			r = R.error(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("修改直客失败 客户ID{} 消息{}", account.getClientid(), e);
			r = R.error("修改直客失败");
		}
		return r;
	}

	/**
	 * 直客管理：修改状态：解冻，冻结，注销
	 */
	@ApiOperation(value = "直客状态更新", notes = "直客管理：修改状态：解冻，冻结，注销", tags = "账户信息管理-直客管理", response = ResultVO.class)
	@PostMapping("/directclient/updateStatus")
	@ResponseBody
	public ResultVO directclientUpdateStatus(String clientId, String status, HttpSession session,
			HttpServletRequest request) {
		ResultVO result = ResultVO.failure();
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		logger.debug("直客客户ID：{};  状态：{}", clientId, status);

		try {
			doUpdateStatus(clientId, status, result);
		} catch (Exception e) {
			result.setMsg("系统操作超时");
			logger.debug("更新直客状态失败,参数 --->客户ID：{};  变更状态：{}  ; {}", clientId, status, e);
		}
		logService.add(LogType.update, LogEnum.账户信息管理.getValue(), userSession.getId(), request.getRequestURI(),
				HttpUtils.getIpAddress(request), "账户信息管理-直客管理：修改状态", clientId, status);
		return result;
	}

	private void doUpdateStatus(String clientId, String status, ResultVO result) {
		if (clientId != null && NumberUtils.isDigits(status)) {
			Map resultMap = accountService.updateClientStatus(clientId, Integer.parseInt(status));
			if ("success".equals(resultMap.get("result"))) {
				result.setSuccess(true);
				result.setMsg((String) resultMap.get("msg"));
			} else {
				result.setSuccess(false);
				result.setMsg((String) resultMap.get("msg"));
			}
		}
	}

	@ApiOperation(value = "直客导出", notes = "直客导出", tags = "账户信息管理-直客管理", produces = "application/octet-stream")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "condition", value = "客户ID/客户名称/手机号码/销售名字", dataType = "String", paramType = "form"),
			@ApiImplicitParam(name = "paytype", value = "付费类型", dataType = "String", paramType = "form"),
			@ApiImplicitParam(name = "start_time_day", value = "起始时间", dataType = "String", paramType = "form"),
			@ApiImplicitParam(name = "end_time_day", value = "截止时间", dataType = "String", paramType = "form") })
	@PostMapping(path = "/directclient/exportExcel")
	public void directclientExportExcel(@ApiParam(name = "params", hidden = true) @RequestParam Map params,
			HttpServletResponse response, HttpSession session) {
		String end_time_day = "";
		Object obj = params.get("end_time_day");
		if (obj != null && com.ucpaas.sms.common.util.StringUtils.isNotBlank(obj.toString())) {
			end_time_day += obj.toString() + " 23:59:59";
			params.put("end_time_day", end_time_day);
		}
		String chargeRule = (String) params.get("charge_rule");
		if (org.apache.commons.lang3.StringUtils.isNotBlank(chargeRule)) {
			params.put("property", "charge_rule");
			params.put("value", chargeRule);
		}
		String filePath = ConfigUtils.save_path + "/直客信息列表.xls";

		Excel excel = new Excel();
		excel.setPageRowCount(2000); // 设置每页excel显示2000行
		excel.setFilePath(filePath);
		excel.setTitle("直客信息列表");

		StringBuffer buffer = new StringBuffer("查询条件：");
		int count = 0;
		// 判断是否有该查询条件:综合条件
		if (params.get("condition") != null)
			buffer.append("（").append(++count).append("）" + "客户ID/客户名称/手机号码/销售名字：").append(params.get("condition"));
		// 判断是否有该查询条件:客户星级
		String startLevel = params.get("star_level").toString();
		if (StringUtils.isNotBlank(startLevel)) {
			buffer.append("（").append(++count).append("）客户等级：" + startLevel + "星");
		}
		// 判断是否有该查询条件:客户状态
		String status = params.get("status").toString();
		if (StringUtils.isNotBlank(status)) {
			String accountStatus = AccountStatusEnum.getDescByValue(Integer.valueOf(status));
			buffer.append("（").append(++count).append("）客户状态：").append("注册完成".equals(accountStatus) ? "已启用" : accountStatus);
		}
		String extValue = (String) params.get("extValue");
		// 判断是否有该查询条件:是否支持子客户
		if (StringUtils.isNotBlank(extValue)) {
			buffer.append("（").append(++count).append("）是否支持子客户：").append(SupportEnum.getDescByValue(Integer.valueOf(extValue)));
		}
		// 判断是否有该查询条件:计费类型
		String charge_rule = (String) params.get("charge_rule");
		if (StringUtils.isNotEmpty(charge_rule)) {
			buffer.append("（").append(++count).append("）计费规则：").append(ChargeRuleType.getDescByValue(Integer.valueOf(charge_rule)));
		}
		// 判断是否有该查询条件:付费类型
		if (params.get("paytype") != null)
			buffer.append("（").append(++count).append("）付费类型：").append(
					com.ucpaas.sms.common.util.ColumnConverUtil.paytypeToName(params.get("paytype").toString()));
		// 判断是否有该查询条件:开始时间
		if (params.get("start_time_day") != null)
			buffer.append("（").append(++count).append("）起始时间：").append(params.get("start_time_day"));
		// 判断是否有该查询条件:结束时间
		if (end_time_day != null)
			buffer.append("（").append(++count).append("）截止时间：").append(end_time_day);
		// 在表格对象中添加查询条件,无查询条件则不添加
		if (count > 0)
			excel.addRemark(buffer.toString());

		excel.addHeader(20, "客户ID", "clientid");
		excel.addHeader(20, "客户名称", "name");
		excel.addHeader(20, "客户等级", "starLevelStr");
		excel.addHeader(20, "手机号码", "mobile");
		excel.addHeader(20, "客户状态", "statusName");
		excel.addHeader(20, "认证状态", "oauthStatusDesc");
		excel.addHeader(20, "归属销售", "userRealName");
		excel.addHeader(20, "短信协议类型", "smsfromDesc");
		excel.addHeader(20, "短信类型", "smstypeDesc");
		excel.addHeader(20, "计费规则", "valueStr");
		excel.addHeader(20, "付费类型", "paytypeDesc");
		excel.addHeader(20, "是否支持子账户", "extValueStr");
		excel.addHeader(20, "创建时间", "createtime");

		// 权限控制
		UserSession userSession = getUserFromSession(session);
		params.put("userId", userSession.getId().toString());

		excel.setDataList(accountService.findAllDirectclientList(params));

		if (com.ucpaas.sms.common.util.file.ExcelUtils.exportExcel(excel)) {
			com.ucpaas.sms.common.util.file.FileUtils.download(filePath, response);
			com.ucpaas.sms.common.util.file.FileUtils.delete(filePath);
		} else {
			String fullContentType = "text/plain;charset=UTF-8";
			response.setContentType(fullContentType);
			try {
				response.getWriter().write("导出Excel文件失败，请联系管理员");
				response.getWriter().flush();
			} catch (IOException e) {
				logger.error("导出Excel文件失败", e);
			}
		}
	}

	/**
	 * 客户组管理
	 */
	@ApiOperation(value = "客户组主页", notes = "客户组主页，返回路径为page/accountInfo/clientgroup/list", tags = "账户信息管理-客户组管理")
	@GetMapping("/clientgroup/query")
	public ModelAndView clientGroupQuery(ModelAndView mv, HttpSession session) {
		mv.addObject("max_export_num", ConfigUtils.max_export_excel_num);
		mv.setViewName("accountInfo/clientgroup/list");
		UserSession user = getUserFromSession(session);
		mv.addObject("menus", AgentUtils.hasMenuRight(user, "直客管理-zkgl", "子账户管理-dlszkhgl", "客户组管理-khzgl"));
		return mv;
	}

	/**
	 * 客户组管理列表
	 */
	@ApiOperation(value = "客户组列表", notes = "客户组管理查询列表", tags = "账户信息管理-客户组管理", response = Page.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "rows", value = "每页面显示行数", dataType = "String", paramType = "form"),
			@ApiImplicitParam(name = "page", value = "页码", dataType = "String", paramType = "form"),
			@ApiImplicitParam(name = "accountGname", value = "客户组名称", dataType = "String", paramType = "form") })
	@PostMapping("/clientgroup/list")
	@ResponseBody
	public Page clientGroupList(@ApiParam(name = "params", hidden = true) @RequestParam Map params) {
		Page<AccountGroup> page = AgentUtils.buildPageByParams(params);
		page.getParams().put("accountGname", params.get("accountGname"));
		return accountGroupService.queryList(page);
	}

	@JsonIgnoreProperties({ "id", "password", "status", "agentId", "oauthStatus", "oauth", "mobile", "email",
			"province", "city", "area", "address", "realname", "clientLevel", "clientType", "needreport", "needmo",
			"needaudit", "createtime", "ip", "deliveryurl", "mourl", "nodenum", "paytype", "needextend", "signextend",
			"beSale", "agentOwned", "smstype", "smsfrom", "isoverratecharge", "uptime", "spnum", "getreportInterval",
			"getreportMaxsiz e", "moip", "moport", "nodeid", "identify", "accessSpeed", "noticeurl", "extendSize",
			"clientAscription", "extendtype", "extendport", "signportlen", "remarks", "belongSale", "updatetime",
			"userPriceList", "oauthDate", "lastUpdateTime", "rowNum", "orderBy" })
	private static class AccountFilter extends Account {
	}

	/**
	 * 客户组管理 - 根据客户类型获取客户列表
	 */
	@ApiOperation(value = "获取客户列表", notes = "根据客户类型获取客户列表", tags = "账户信息管理-客户组管理", response = R.class)
	@ApiImplicitParam(name = "type", value = "客户类型", paramType = "query", dataType = "int")
	@GetMapping("/clientgroup/clientsbytype")
	public View clientGroupGetClientsByType(Integer type, Model model) {
		customMapper.addMixIn(Account.class, AccountFilter.class);
		customMapper.setTimeZone(TimeZone.getDefault());
		view.setObjectMapper(customMapper);

		List<Account> accounts = accountGroupService.getClientInfoList(type);

		model.addAllAttributes(R.ok("获取数据成功", accounts).asMap());
		return view;
	}

	/**
	 * 客户组管理 - 添加页面
	 */
	@ApiOperation(value = "添加页面", notes = "添加页面，返回路径为page/accountInfo/clientgroup/add", tags = "账户信息管理-客户组管理")
	@GetMapping("/clientgroup/add")
	public ModelAndView clientGroupAdd(ModelAndView mv) {
		mv.setViewName("accountInfo/clientgroup/add");
		return mv;
	}

	/**
	 * 客户组管理 - 添加保存
	 */
	@ApiOperation(value = "添加保存", notes = "客户组管理添加保存", tags = "账户信息管理-客户组管理", response = R.class)
	@PostMapping("/clientgroup/add")
	@ResponseBody
	public R clientGroupAdd(@RequestBody AccountGroup accountGroup, HttpSession session) {
		R r;

		try {
			// 验证对象的安全性
			r = accountGroupService.checkAccountGroup(accountGroup, true);
			if (r == null) {
				UserSession userSession = getUserFromSession(session);
				accountGroup.setCreateId(userSession.getId());
				r = accountGroupService.addAccountGroup(accountGroup);
			}
		} catch (OperationException e) {
			logger.error("添加客户组失败 消息{}", e);
			r = R.error(e.getMessage());
		} catch (Exception e) {
			logger.error("添加客户组失败 消息{}", e);
			r = R.error("添加客户组失败");
		}
		return r;
	}

	/**
	 * 客户组管理 - 编辑页面
	 */
	@ApiOperation(value = "编辑页面", notes = "客户组管理编辑页面, 返回路径为page/accountInfo/clientgroup/edit", tags = "账户信息管理-客户组管理")
	@GetMapping("/clientgroup/edit")
	public ModelAndView clientGroupEdit(ModelAndView mv) {
		mv.setViewName("accountInfo/clientgroup/edit");
		return mv;
	}

	/**
	 * 客户组管理 - 编辑页面数据
	 */
	@ApiOperation(value = "编辑页面数据", notes = "客户组管理编辑页面数据", tags = "账户信息管理-客户组管理", response = R.class)
	@ApiImplicitParam(name = "accountGid", value = "客户组Id", paramType = "query", required = true, dataType = "int")
	@GetMapping("/clientgroup/edit/data")
	public View clientGroupEditData(Integer accountGid, Model model) {
		customMapper.addMixIn(Account.class, AccountFilter.class);
		customMapper.setTimeZone(TimeZone.getDefault());
		view.setObjectMapper(customMapper);

		if (accountGid == null) {
			model.addAllAttributes(R.ok("客户组ID不能为空").asMap());
			return view;
		}

		Map<String, Object> data = Maps.newHashMap();
		AccountGroup accountGroup = accountGroupService.getAccountGroup(accountGid);
		data.put("accountGroup", accountGroup);
		data.put("clients", accountGroupService.getClientInfoList(accountGroup.getType()));

		model.addAllAttributes(R.ok("获取数据成功", data).asMap());
		return view;
	}

	/**
	 * 客户组管理 - 编辑保存
	 */
	@ApiOperation(value = "编辑保存", notes = "客户组管理编辑保存", tags = "账户信息管理-客户组管理", response = R.class)
	@PostMapping("/clientgroup/edit")
	@ResponseBody
	public R clientGroupEdit(@RequestBody AccountGroup accountGroup, HttpServletRequest request) {
		R r;
		try {
			// 验证对象的安全性
			r = accountGroupService.checkAccountGroup(accountGroup, false);
			if (r == null) {
				r = accountGroupService.modAccountGroup(accountGroup);
			}
		} catch (OperationException e) {
			logger.error("修改客户组失败  消息{}", e);
			r = R.error(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("修改客户组失败 消息{}", e);
			r = R.error("修改客户组失败");
		}
		return r;
	}

	/**
	 * 客户组管理 - 删除
	 */
	@ApiOperation(value = "删除客户组", notes = "客户组管理删除", tags = "账户信息管理-客户组管理", response = R.class)
	@PostMapping("/clientgroup/delete/{id}")
	@ResponseBody
	public R clientGroupDel(@ApiParam(value = "客户组ID", required = true) @PathVariable("id") Integer id,
			HttpServletRequest request) {
		R r;
		try {
			if (id == null) {
				return R.error("客户组ID不能为空");
			}

			// 验证对象的安全性
			r = accountGroupService.delAccountGroup(id);
		} catch (OperationException e) {
			logger.error("删除客户组失败  消息{}", e);
			r = R.error(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("删除客户组失败 消息{}", e);
			r = R.error("删除客户组失败");
		}
		return r;
	}

	@ApiOperation(value = "查询代理商可用额度提醒", notes = "查询代理商的“可用额度”、“告警手机号码”", tags = "账户信息管理-客户管理", response = Result.class)
	@GetMapping("/agentBalanceAlarm/get/{agentId}")
	@ResponseBody
	public Result<JsmsAgentBalanceAlarm> agentBalanceAlarmGet(@PathVariable("agentId") Integer agentId) {
		JsmsAgentBalanceAlarm agentBalanceAlarm = agentBalanceAlarmService.getByAgentId(agentId);
		if (agentBalanceAlarm == null) {
			JsmsAgentInfo agentInfo = agentInfoService.getByAgentId(agentId);
			agentBalanceAlarm = new JsmsAgentBalanceAlarm();
			agentBalanceAlarm.setAgentId(agentId);
			agentBalanceAlarm.setAlarmAmount(BigDecimal.ZERO);
			agentBalanceAlarm.setReminderNumber(0);
			agentBalanceAlarm.setAlarmPhone(agentInfo.getMobile());
		}
		return new Result<>(true, CodeEnum.SUCCESS, agentBalanceAlarm, "操作成功");
	}

	@ApiOperation(value = "更新代理商可用额度提醒", notes = "更新代理商的“可用额度”、“告警手机号码”", tags = "账户信息管理-客户管理", response = Result.class)
	@PostMapping("/agentBalanceAlarm/post")
	@ResponseBody
	public Result<JsmsAgentBalanceAlarm> agentBalanceAlarmEdit(Integer agentId, String alarmPhone, String alarmAmount) {

		JsmsAgentBalanceAlarm agentBalanceAlarm = new JsmsAgentBalanceAlarm();
		// 校验代理商ID
		if (agentId == null)
			return new Result<>(false, CodeEnum.FAIL, null, "代理商ID不正确");

		// 校验告警阈值
		try {
			agentBalanceAlarm.setAlarmAmount(new BigDecimal(alarmAmount));
			if (agentBalanceAlarm.getAlarmAmount().compareTo(BigDecimal.ZERO) == -1) {
				return new Result<>(false, CodeEnum.FAIL, null, "告警阀值不能为负数");
			}
			if (agentBalanceAlarm.getAlarmAmount().compareTo(new BigDecimal("1000000")) == 1) {
				return new Result<>(false, CodeEnum.FAIL, null, "告警阀值不能大于100W");
			}
		} catch (Exception e) {
			return new Result<>(false, CodeEnum.FAIL, null, "告警阀值只能为数字");
		}

		// 校验手机号码
		if (StringUtils.isEmpty(alarmPhone)) {
			return new Result<>(false, CodeEnum.FAIL, null, "接收告警手机号不能为空");
		}

		if (alarmPhone.length() > 1000) {
			return new Result<>(false, CodeEnum.FAIL, null, "手机号字符总长度不能超过1000");
		}
		String[] phoneArr = alarmPhone.split(",");
		/*
		 * if(phoneArr.length > 2){ return new Result<>(false, CodeEnum.FAIL,
		 * null, "最多只能设置两个手机号"); }
		 */
		for (String s : phoneArr) {
			if (!RegexUtils.isMobile(s)) {
				return new Result<>(false, CodeEnum.FAIL, null, "手机号码格式错误");
			}
		}

		agentBalanceAlarm.setAgentId(Integer.valueOf(agentId));
		agentBalanceAlarm.setAlarmPhone(alarmPhone);
		agentBalanceAlarm.setUpdateTime(new Date());
		int i = agentBalanceAlarmService.insertOrUpdate(agentBalanceAlarm);
		return new Result<>(true, CodeEnum.SUCCESS, null, "操作成功");
	}
	@PostMapping("/client/checkAgentInfo")
	@ResponseBody
	public R checkAgentInfo(String agentId) {
		AgentInfo checkAgentInfo = AgentUtils.queryAgentInfoByAgentId(agentId);
		if (null == checkAgentInfo) {
			return R.error("未查询到代理商相关信息");
		}
		if (!OauthStatusEnum.证件已认证.getValue().equals(checkAgentInfo.getOauthStatus())) {
			return R.error("代理商还未认证！");
		}else{
			return R.ok("代理商已认证！");
		}

	}

	@RequestMapping(value = "/qualification/save", method = RequestMethod.GET)
	public ModelAndView autoTemplate(ModelAndView mv, String agentId, String clientid, String type,HttpSession session ) {
		AgentInfo agentInfo = null;
		Account account = null;
		if (StringUtils.isNotBlank(agentId)) {// 代理商
			agentInfo = agentInfoServiceCtrl.getByAgentId(Integer.parseInt(agentId));
			// 代理商资质回显
			Map params = new HashMap<>();
			params.put("agent_id", agentId);
			Map agentAudit = agentAuditService.view(params);

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if (agentInfo != null) {
				mv.addObject("agentId", agentInfo.getAgentId());
				mv.addObject("agentName", agentInfo.getAgentName());
				mv.addObject("mobile", agentInfo.getMobile());
				mv.addObject("address", agentInfo.getAddress());
				if (agentInfo.getCreateTime() != null) {
					mv.addObject("createTime", formatter.format(agentInfo.getCreateTime()));
				}
				mv.addObject("remark", agentInfo.getRemark());
				String imgUrl = (String) (agentAudit == null ? "" : agentAudit.get("img_url") == null ? "" : agentAudit.get("img_url"));
				mv.addObject("imgUrl", imgUrl);
				mv.addObject("decodeImgUrl", SecurityUtils.decodeDes3(imgUrl));
				mv.addObject("idType", agentAudit == null ? "" : agentAudit.get("id_type") == null ? "" : agentAudit.get("id_type"));
				mv.addObject("idNbr", agentAudit == null ? "" : agentAudit.get("id_nbr") == null ? "" : agentAudit.get("id_nbr"));
				mv.addObject("oauthType", "2");
				mv.addObject("type", "1");
			}
			mv.setViewName("accountInfo/agent/agentauth");
		}
		if (StringUtils.isNotBlank(clientid)) {// 直客,代理商子客户
			account = accountService.getAccount(clientid);
			// 子账户资质回显
			Map params = new HashMap<>();
			params.put("client_id", clientid);
			params.put("type", "2");
			Map<String, Object> customerAudit = customerAuditService.view(params);

			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if (account != null) {
				mv.addObject("agentId", account.getAgentId());
				mv.addObject("agentName", account.getName());
				mv.addObject("mobile", account.getMobile());
				mv.addObject("address", account.getAddress());
				mv.addObject("realname", account.getRealname());
				mv.addObject("clientId", clientid);
				if (account.getCreatetime() != null) {
					mv.addObject("createTime", formatter.format(account.getCreatetime()));
				}
				mv.addObject("remark", account.getRemarks());
				String imgUrl = (String) (customerAudit == null ? "" : customerAudit.get("img_url") == null ? "" : customerAudit.get("img_url"));
				mv.addObject("imgUrl", imgUrl);
                mv.addObject("decodeImgUrl", SecurityUtils.decodeDes3(imgUrl));
				mv.addObject("idType", customerAudit == null ? "" : customerAudit.get("id_type") == null ? "" : customerAudit.get("id_type"));
				mv.addObject("idNbr", customerAudit == null ? "" : customerAudit.get("id_nbr") == null ? "" : customerAudit.get("id_nbr"));
				mv.addObject("oauthType", account.getClientType());
				if (StringUtils.isNotBlank(type)) {
					mv.addObject("isZK", "1");
				}
				mv.addObject("type", "2");
			}
			mv.setViewName("accountInfo/client/clientauth");
		}
		String smspImgUrl = ConfigUtils.smsp_img_url.endsWith("/")
				? ConfigUtils.smsp_img_url.substring(0, ConfigUtils.smsp_img_url.lastIndexOf("/"))
				: ConfigUtils.smsp_img_url;
		mv.addObject("smsp_img_url", smspImgUrl);
		return mv;
	}

	/**
	 * @Description: 上传账户资质
	 * @return: Map<String,Object>
	 */
	@RequestMapping(value = "/qualification/save", method = RequestMethod.POST)
	@ResponseBody
	public R addAccountCer(String idNbr, String imgurl, String agentId, String clientId, String realName, String idType,
			String clientType, String iszK, HttpServletRequest request, HttpSession session) {
		R r;
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        boolean isSale = false;
        if(userSession.getRoles().size()>0){
            for(int i=0;i<userSession.getRoles().size();i++){
                if(userSession.getRoles().get(i).getRoleName().equals("销售人员")){
                    isSale = true;
                }
            }
        }
		Account account = new Account();
		SmsOauthpic info = new SmsOauthpic();
		UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		if ((StringUtils.isBlank(agentId) && StringUtils.isBlank(clientId)) || StringUtils.isBlank(clientType)) {
			r = R.error("参数不全！");
			return r;
		}
		if (StringUtils.isBlank(idNbr)) {
			r = R.error("证件号码不能为空！");
			return r;
		}
		if (StringUtils.isBlank(imgurl)) {
			r = R.error("证件图片不能为空！");
			return r;
		}
		if (StringUtils.isBlank(idType)) {
			r = R.error("证件类型不能为空！");
			return r;
		}
		if (StringUtils.isBlank(realName)) {
			r = R.error("代理商名称不能为空！");
			return r;
		}
		info.setIdNbr(idNbr);
		info.setImgUrl(imgurl);
		info.setRealName(realName);
		info.setIdType(idType);
		info.setClientType(clientType);
		if (StringUtils.isNotBlank(clientId) && StringUtils.isBlank(iszK)) {// 代理商子客户
			info.setClientId(clientId);
			info.setOauthType("2");
			account = accountService.getAccount(clientId);
		} else if (StringUtils.isNotBlank(clientId) && StringUtils.isNotBlank(iszK)) {// 直客
			info.setClientId(clientId);
			info.setOauthType("2");
			info.setReason("zk");
		}
		if(isSale){//销售上传资质默认是待审核
            info.setOauthStatus(String.valueOf(OauthStatusType.待认证.getValue()));
        }else{
            info.setOauthStatus(String.valueOf(OauthStatusType.证件已认证.getValue()));// 在运营平台上的上传资质都是认证通过的
        }
		Long userId = userSession.getId();
		try {
			if (StringUtils.isBlank(agentId)) {
				info.setAgentId("0");
			} else {
				info.setAgentId(String.valueOf(agentId));
			}
			Map<String, Object> data = accountService.addCerInfo(info, userId); // 上传资质信息
			r = "success".equals(data.get("result").toString()) ? R.ok(data.get("msg").toString(), data.get("datePath"))
					: R.error(data.get("msg").toString());
		} catch (Exception e) {
			logger.error("客户资质添加失败, clientId{}, 信息{}", info.getClientId(), e);
			r = R.error("服务器异常,正在检修中...");
		}
		return r;
	}

	// 管理子账户页面
	@RequestMapping(value = "/subclient/list", method = RequestMethod.GET)
	public ModelAndView subclient(ModelAndView mv, String agentId, String clientid, String type) {
		mv.setViewName("accountInfo/directclient/subAccount");
		return mv;
	}

	/**
	 * 查询所有的未绑定的子账户和当前客户已经绑定的子账户
	 */
	@RequestMapping("/query/subAccount")
	@ResponseBody
	public Map<String, List<Account>> getAccounts(HttpSession session, HttpServletRequest request, String clientid) {
		Map<String, List<Account>> data = new HashMap<>();
		List<Account> bindingList = accountService.getAllBindingSubAccount(clientid);// 已经绑定的子账户
		List<Account> list = accountService.getAllSubAccount();// 未绑定的子账户
		data.put("list", list);
		data.put("bindingList", bindingList);
		return data;
	}

	/**
	 * 绑定子账户
	 */
	@RequestMapping("/saveSubAccount")
	@ResponseBody
	public Map<String, String> saveSubAccount(HttpSession session, HttpServletRequest request, String clientid,
			@RequestParam Map<String, String> params) {
		boolean bool = false;
		Map<String, String> map = new HashMap<>();
		try {
			bool = accountService.updateBindingSubAccountOfMessage(clientid, params);
		} catch (Exception e) {
			map.put("code", "500");
			map.put("msg", "操作子客户异常");
			return map;
		}
		if (bool) {
			map.put("code", "200");
			map.put("msg", "操作子客户成功");
			return map;
		} else {
			map.put("code", "500");
			map.put("msg", "操作子客户失败");
			return map;
		}

	}

	/**
	 * 失败待返还
	 */
	@GetMapping("/{clientid}/returnFail")
	public ModelAndView returnFail(ModelAndView mv, @PathVariable String clientid, @RequestParam String name,
			@RequestParam String agentType) {
		if (org.apache.commons.lang3.StringUtils.isNotBlank(name)) {
			try {
				name = URLDecoder.decode(name, "UTF-8");
				mv.addObject("name", name);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}
		mv.setViewName("accountInfo/client/returnFail");
		mv.addObject("clientid", clientid);
		mv.addObject("agentType", agentType);
		return mv;
	}

	/**
	 * 失败待返还
	 */
	@PostMapping("/{clientid}/returnFail")
	@ResponseBody
	public PageContainer returnFail(@PathVariable String clientid, @RequestParam Map params, HttpSession session) {

		params.put("orderByClause", "date DESC, id DESC");
		PageContainer pageContainer = clientFailReturnService.queryList(clientid, params);

		return pageContainer;
	}

	/**
	 * 失败待返还
	 */
	@PostMapping("/returnFail/confirmReturn")
	@ResponseBody
	public com.jsmsframework.common.dto.ResultVO returnFail(@RequestBody SendFailReturnPo sendFailReturnPo,
			HttpSession session) {
		if (sendFailReturnPo.getFailReturnList().size() < 1) {
			return com.jsmsframework.common.dto.ResultVO.failure("请选择记录...");
		}
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());

		Map<Integer, String> failReturnMap = new HashMap<>();
		for (FailReturnPo failReturn : sendFailReturnPo.getFailReturnList()) {
			// for (SendFailReturnPo.FailReturn failReturn
			// :sendFailReturnPo.getFailReturnList()) {
			failReturnMap.put(failReturn.getId(), failReturn.getSubId());
		}

		Set<Integer> ids1 = new HashSet<>(failReturnMap.keySet());
		List<JsmsClientFailReturn> clientFailReturnList = jsmsClientFailReturnService.getByIds(ids1);
		Date now = new Date();

		JsmsAccount jsmsAccount = jsmsAccountService.getByClientId(sendFailReturnPo.getClientId());
		JsmsAgentInfo jsmsAgentInfo = jsmsAgentInfoService.getByAgentId(jsmsAccount.getAgentId());
		int update = 0;
		int totalReturn = 0;
		int actualReturn = 0;
		for (JsmsClientFailReturn clientFailReturn : clientFailReturnList) {
			totalReturn += clientFailReturn.getReturnNumber();
		}
		com.jsmsframework.common.dto.ResultVO resultVO = null;
		try {
			if (AgentType.品牌代理商.getValue().equals(jsmsAgentInfo.getAgentType())
					|| AgentType.销售代理商.getValue().equals(jsmsAgentInfo.getAgentType())) {
				Set<Long> subIds = new HashSet<>();
				for (String s : failReturnMap.values()) {
					subIds.add(Long.valueOf(s));
				}
				Map<String, Long> orderIdMap = new HashMap<>();
				List<JsmsClientOrder> clientOrders = jsmsClientOrderService.getBySubIds(subIds);
				for (JsmsClientOrder clientOrder : clientOrders) {
					orderIdMap.put(clientOrder.getSubId(), clientOrder.getOrderId());
				}
				for (JsmsClientFailReturn clientFailReturn : clientFailReturnList) {
					if (!RefundStateType.未退费.getValue().equals(clientFailReturn.getRefundState())) {
						continue;
					}

					// add by lpjLiu 20180109
					ClientFailReturn qc = new ClientFailReturn();
					qc.setClientid(clientFailReturn.getClientid());
					qc.setSmstype(clientFailReturn.getSmstype());
					qc.setSubId(clientFailReturn.getSubId());
					qc.setDate(clientFailReturn.getDate());

					List<ClientFailReturn> list = clientFailReturnService.findList(qc);
					if (Collections3.isEmpty(list))
					{
						resultVO =  com.jsmsframework.common.dto.ResultVO.failure("返还失败");
					}

					int returnTotal = 0;
					List<Integer> waitUpdateFailIds = new ArrayList<>();
					for (ClientFailReturn failReturn : list) {
						returnTotal += failReturn.getReturnNumber();
						waitUpdateFailIds.add(failReturn.getId());

						if (!failReturn.getId().equals(clientFailReturn.getId()))
						{
							totalReturn += failReturn.getReturnNumber();
						}
					}
					clientFailReturn.setReturnNumber(returnTotal);

					resultVO = clientFailReturnService.returnSendFail(clientFailReturn, waitUpdateFailIds,
							orderIdMap.get(clientFailReturn.getSubId()), userSession.getId(), now);
					if (resultVO.isSuccess()) {
						//actualReturn += clientFailReturn.getReturnNumber();
						actualReturn += returnTotal;
						update++;
					}
				}
			} else if (AgentType.OEM代理商.getValue().equals(jsmsAgentInfo.getAgentType())) {
				Long orderId = null;
				Long orderNo = null;
				for (JsmsClientFailReturn clientFailReturn : clientFailReturnList) {
					if (!RefundStateType.未退费.getValue().equals(clientFailReturn.getRefundState())) {
						continue;
					}
					orderId = OemOrderIdGenerate.getOemClientOrderId();
					if (orderNo == null) {
						orderNo = orderId;
					}

					// add by lpjLiu 20180109
					ClientFailReturn qc = new ClientFailReturn();
					qc.setClientid(clientFailReturn.getClientid());
					qc.setSmstype(clientFailReturn.getSmstype());
					qc.setSubId(clientFailReturn.getSubId());
					qc.setDate(clientFailReturn.getDate());

					List<ClientFailReturn> list = clientFailReturnService.findList(qc);
					if (Collections3.isEmpty(list))
					{
						resultVO =  com.jsmsframework.common.dto.ResultVO.failure("返还失败");
					}

					int returnTotal = 0;
					List<Integer> waitUpdateFailIds = new ArrayList<>();
					for (ClientFailReturn failReturn : list) {
						returnTotal += failReturn.getReturnNumber();
						waitUpdateFailIds.add(failReturn.getId());

						if (!failReturn.getId().equals(clientFailReturn.getId()))
						{
							totalReturn += failReturn.getReturnNumber();
						}
					}
					clientFailReturn.setReturnNumber(returnTotal);

					resultVO = clientFailReturnService.returnOemSendFail(clientFailReturn, waitUpdateFailIds, jsmsAgentInfo.getAgentId(),
							orderId, orderNo, userSession.getId(), now);
					if (resultVO.isSuccess()) {
						// actualReturn += clientFailReturn.getReturnNumber();
						actualReturn += returnTotal;
						update++;
					}
				}
			}
		} catch (JsmsOrderFinanceException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		int size = clientFailReturnList.size();
		if (size == 1 || size == update) {
			return resultVO;
		} else {
			return com.jsmsframework.common.dto.ResultVO
					.failure("需要返还" + totalReturn + "条</br>实际返还" + actualReturn + "条");
		}
	}

	/**
	 * 客户管理-代理商子客户管理：余额提醒
	 */
	@ApiOperation(value = "余额提醒", notes = "余额提醒", tags = "账户信息管理-客户管理-子账户管理", response = R.class)
	@GetMapping("/client/balancealarm/{clientid}")
	@ResponseBody
	public R getClientBalanceAlarm(@PathVariable("clientid") String clientId) {
		R r;
		try {
			if (StringUtils.isBlank(clientId)) {
				return R.error("客户ID不能为空");
			}

			// 查询客户
			JsmsAccount jsmsAccount = jsmsAccountService.getByClientId(clientId);
			if (jsmsAccount == null) {
				return R.error("客户不存在");
			}

			// 查询客户余额配置
			JsmsClientBalanceAlarm queryCba = new JsmsClientBalanceAlarm();
			queryCba.setClientid(clientId);
			List<JsmsClientBalanceAlarm> clientBalanceAlarms = jsmsClientBalanceAlarmService.findList(queryCba);

			ClientBalanceAlarmPo clientBalanceAlarmPo = null;
			if (!Collections3.isEmpty(clientBalanceAlarms))
			{
				clientBalanceAlarmPo = new ClientBalanceAlarmPo();
				clientBalanceAlarmPo.setId(clientBalanceAlarms.get(0).getId());
				clientBalanceAlarmPo.setClientid(clientBalanceAlarms.get(0).getClientid());
				clientBalanceAlarmPo.setAlarmEmail(clientBalanceAlarms.get(0).getAlarmEmail());
				clientBalanceAlarmPo.setAlarmPhone(clientBalanceAlarms.get(0).getAlarmPhone());
				for (JsmsClientBalanceAlarm clientBalanceAlarm : clientBalanceAlarms) {
					if (clientBalanceAlarm.getAlarmType().intValue() == ClientAlarmType.验证码.getValue().intValue())
					{
						clientBalanceAlarmPo.setYzmAlarmNumber(clientBalanceAlarm.getAlarmNumber());
					}

					if (clientBalanceAlarm.getAlarmType().intValue() == ClientAlarmType.通知.getValue().intValue())
					{
						clientBalanceAlarmPo.setTzAlarmNumber(clientBalanceAlarm.getAlarmNumber());
					}

					if (clientBalanceAlarm.getAlarmType().intValue() == ClientAlarmType.营销.getValue().intValue())
					{
						clientBalanceAlarmPo.setYxAlarmNumber(clientBalanceAlarm.getAlarmNumber());
					}

					if (clientBalanceAlarm.getAlarmType().intValue() == ClientAlarmType.国际.getValue().intValue())
					{
						clientBalanceAlarmPo.setGjAlarmAmount(clientBalanceAlarm.getAlarmAmount());
					}
				}
			}

			Map<String, Object> result = new HashMap<>();
			result.put("account", jsmsAccount);
			result.put("balanceAlarm", clientBalanceAlarmPo);

			r = R.ok("获取客户余额提醒设置成功", result);

		} catch (Exception e) {
			r = R.error("获取客户余额提醒设置失败");
			logger.debug("获取客户余额提醒设置失败, 参数 --->客户ID：{};  错误 {}", clientId, e);
		}

		return r;
	}

	/**
	 * 客户管理-代理商子客户管理：余额提醒保存
	 */
	@ApiOperation(value = "余额提醒保存", notes = "余额提醒保存", tags = "账户信息管理-客户管理-子账户管理", response = R.class)
	@PostMapping("/client/balancealarm/save")
	@ResponseBody
	public R saveClientBalanceAlarm(@RequestBody ClientBalanceAlarmPo clientBalanceAlarm) {
		R r;
		try {
			logger.debug("客户余额提醒设置：{}", JSON.toJSONString(clientBalanceAlarm));
			r = accountService.saveClientBalanceAlarm(clientBalanceAlarm);
		} catch (Exception e) {
			r = R.error("客户余额提醒设置失败");
			logger.debug("客户余额提醒设置失败, 参数：{};  错误 {}", JSON.toJSONString(clientBalanceAlarm), e);
		}
		return r;
	}
	/**
	 * 客户管理-代理商子客户管理：oem余额提醒保存页面
	 */
	@ApiOperation(value = "oem余额提醒页面", notes = "子账户管理：oem余额提醒保存页面", tags = "子账户管理-oem余额提醒")
	@GetMapping("/client/balancealarm/view")
	public ModelAndView clientBalanceAlarmView(ModelAndView mv) {
		mv.setViewName("/accountInfo/client/oemBalanceAlarm");
		return mv;
	}

	@GetMapping(path = { "/customerStarLevel/list" })
	public ModelAndView customerStarLevel(ModelAndView mv, HttpSession session) {
		mv.addObject("max_export_num", ConfigUtils.max_export_excel_num);
		mv.setViewName("accountInfo/customerStarLevel/list");
		return mv;
	}

	@PostMapping(path = { "/customerStarLevel/list" })
	@ResponseBody
	public PageContainer getCustomerStarLevelList(@RequestParam Map<String, String> params) {
		params.put("currentPage", params.get("page"));
		params.put("pageRowCount", params.get("rows"));
		PageContainer pageContainer = accountService.queryCustomerStarLevel(params);
		return pageContainer;
	}

	@RequestMapping(path = "/customerStarLevel/exportExcel")
	public void customerStarLevelExcel(String customerID, String customerName, String status, String star_level,
			HttpServletRequest request, HttpServletResponse response) {
		Map<String, String> params = new HashMap<String, String>();

		if (status != null && StringUtils.isNotBlank(status)) {
			params.put("status", status);
		}
		if (star_level != null && StringUtils.isNotBlank(star_level)) {
			params.put("starLevel", star_level);
		}
		if (customerID != null && StringUtils.isNotBlank(customerID)) {
			params.put("customerID", customerID);
		}
		if (customerName != null && StringUtils.isNotBlank(customerName)) {
			params.put("customerName", customerName);
		}
		String filePath = ConfigUtils.save_path + "/客户信息报表.xls";

		Excel excel = new Excel();
		excel.setPageRowCount(2000); // 设置每页excel显示2000行
		excel.setFilePath(filePath);
		excel.setTitle("客户信息报表");

		StringBuffer buffer = new StringBuffer("查询条件：");
		int count = 0;
		// 判断是否有该查询条件:客户ID
		if (StringUtils.isNotEmpty(customerID))
			buffer.append("（").append(++count).append("）客户ID：").append(params.get("customerID"));
		// 判断是否有该查询条件:客户名称
		if (StringUtils.isNotEmpty(customerName))
			buffer.append("（").append(++count).append("）客户名称：").append(params.get("customerName"));
		// 判断是否有该查询条件:客户类型
		if (StringUtils.isNotEmpty(status)) {
			if (status.equals("1")) {
				buffer.append("（").append(++count).append("）客户类型：代理商子客户");
			} else if (status.equals("2")) {
				buffer.append("（").append(++count).append("）客户类型：直客");
			}
		}
		// 判断是否有该查询条件:客户星级
		if (StringUtils.isNotBlank(star_level))
			if (star_level.equals("1")) {
				buffer.append("（").append(++count).append("）客户等级：1星");
			} else if (star_level.equals("2")) {
				buffer.append("（").append(++count).append("）客户等级：2星");
			} else if (star_level.equals("3")) {
				buffer.append("（").append(++count).append("）客户等级：3星");
			} else if (star_level.equals("4")) {
				buffer.append("（").append(++count).append("）客户等级：4星");
			} else if (star_level.equals("5")) {
				buffer.append("（").append(++count).append("）客户等级：5星");
			}
		// 在表格对象中添加查询条件,无查询条件则不添加
		if (count > 0)
			excel.addRemark(buffer.toString());

		excel.addHeader(20, "客户ID", "clientid");
		excel.addHeader(20, "客户名称", "name");
		excel.addHeader(20, "客户等级", "starLevelStr");
		excel.addHeader(20, "客户类型", "agentIdStr");
		excel.addHeader(20, "创建时间", "createtimeStr");
		excel.setDataList(accountService.queryAllCustomerStarLevel(params));
		if (ExcelUtils.exportExcel(excel)) {
			FileUtils.download(response, filePath);
			FileUtils.delete(filePath);
		} else {
			String fullContentType = "text/plain;charset=UTF-8";
			response.setContentType(fullContentType);
			try {
				response.getWriter().write("导出Excel文件失败，请联系管理员");
				response.getWriter().flush();
			} catch (IOException e) {
				logger.error("导出Excel文件失败", e);
			}
		}
	}

	/*
	 * @PostMapping(path = {"/customerStarLevel/getAccountPo"})
	 *
	 * @ResponseBody public AccountPo getAccountPo(String clientid,String
	 * starLevel) { AccountPo accountPO = new AccountPo(); Account
	 * po=accountService.getAccount(clientid);
	 * accountPO.setClientid(po.getClientid()); accountPO.setName(po.getName());
	 * accountPO.setStarLeve(Integer.parseInt(starLevel)); return accountPO; }
	 */

	@GetMapping(path = { "/customerStarLevel/getAccountPoOfStarLevel" })
	public ModelAndView getAccountPoOfStarLevel(ModelAndView mv, HttpServletRequest request, String clientid) {
		if (StringUtils.isNotBlank(clientid)) {
			AccountPo accountPo = accountService.getAccountPoOfStarLevel(clientid);
			if (accountPo != null) {
				mv.addObject("clientid", accountPo.getClientid());
				mv.addObject("name", accountPo.getName());
				mv.addObject("starLevel", accountPo.getStarLevel());
			}
		}
		mv.setViewName("accountInfo/customerStarLevel/setLevel");
		return mv;
	}

    @PostMapping (path = {"/customerStarLevel/updateAccountPoOfStarLevel"})
    @ResponseBody
    public ResultVO updateAccountPoOfStarLevel(String clientid,String starLevel) {
        int count =0;
        ResultVO result = ResultVO.failure();
        JsmsClientInfoExt model = new JsmsClientInfoExt();
        model.setClientid(clientid);
        model.setStarLevel(Integer.parseInt(starLevel));
        count = jsmsClientInfoExtService.updateSelective(model);
        if(count>0){
            result.setCode(200);
            result.setMsg("设置星级成功!");
        }else{
            result.setCode(500);
            result.setMsg("设置星级失败!");
        }
        return result;
    }


    /**
     * 代理商购买短信-获取产品列表信息页面
     */
    @GetMapping(path = {"/oem/productsView"})
    public ModelAndView productsView(ModelAndView mv, HttpSession session, String agentId) {
        mv.addObject("agentId", agentId);
        mv.setViewName("accountInfo/oem/productsView");
        return mv;
    }
    /**
     * 代理商资客户管理-子客户短息充值页面
     */
    @GetMapping(path = {"/oem/agentpoolsView"})
    public ModelAndView agentpoolsView(ModelAndView mv, HttpSession session, String clientId) {
        mv.addObject("clientId", clientId);
        mv.setViewName("accountInfo/oem/agentpoolsView");
        return mv;
    }
    /**
     * @param
     * @Description: 给子客户充值
     * @Author: tanjiangqiang
     * @Date: 2017/11/15 - 19:54
     */
    @PostMapping("/client/recharge")
    @ResponseBody
    public R chargeSave(@RequestBody RechargePo po) {
        R r = null;
        try {
            if (null == po) {
                return R.error("请求参数为空");
            }
            List<JsmsOemAgentPoolPo> poolPos = po.getPoolPos();
            Iterator<JsmsOemAgentPoolPo> poolPoIterator = poolPos.iterator();
            while (poolPoIterator.hasNext()){
                JsmsOemAgentPoolPo jsmsOemAgentPoolPo = poolPoIterator.next();
                Integer updateNum = jsmsOemAgentPoolPo.getUpdateNum();
                if(null == updateNum || updateNum <= 0){
                    poolPoIterator.remove();
                }
            }
            if (poolPos.isEmpty()) {
                return R.error("请选择购买产品和充值数量");
            }
            String clientId = po.getClientId();
            if (StringUtils.isBlank(clientId)) {
                return R.error("请选择要充值的客户");
            }
            AgentInfo agentInfo = AgentUtils.queryAgentInfoByAgentId(poolPos.get(0).getAgentId().toString());
            if (null == agentInfo) {
                return R.error("未查询到代理商相关信息");
            }
            if (!OauthStatusEnum.证件已认证.getValue().equals(agentInfo.getOauthStatus())) {
                return R.error("代理商还未认证！");
            }
            r = customerManageService.oemClientRecharge(poolPos, clientId);
        } catch (JsmsOemAgentPoolException e) {
            logger.error("客户充值失败,  信息{}", e);
            R.error(e.getMessage());
        } catch (Exception e) {
            logger.error("客户充值失败,  信息{}", e);
            R.error("服务器异常,正在检修中...");
        }
        return r;
    }
}
