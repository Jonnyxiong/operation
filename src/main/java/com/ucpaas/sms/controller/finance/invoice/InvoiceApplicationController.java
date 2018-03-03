package com.ucpaas.sms.controller.finance.invoice;

import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.enums.Code;
import com.jsmsframework.common.enums.invoice.InvoiceStatusEnum;
import com.jsmsframework.finance.entity.JsmsAgentInvoiceList;
import com.jsmsframework.user.entity.JsmsAgentInfo;
import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.entity.po.JsmsAgentInvoiceListPo;
import com.ucpaas.sms.enums.SessionEnum;
import com.ucpaas.sms.service.finance.invoice.AgentInvoiceService;
import com.ucpaas.sms.util.AgentUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/finance/invoice")
public class InvoiceApplicationController {
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	@Autowired
	private AgentInvoiceService agentInvoiceService;


	@ApiOperation(value = "发票申请主页面", notes = "发票申请主页面", tags = "发票管理-发票申请", response = R.class)
	@GetMapping("/app")
	public ModelAndView view(ModelAndView mv, HttpSession session) {
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		mv.addObject("menus", AgentUtils.hasMenuRight(userSession, "发票申请-fpsq", "申请记录-sqjl", "发票信息-fpxx", "发票审核-fpsh"));
		mv.setViewName("finance/invoice/application/add");
		return mv;
	}

	@ApiOperation(value = "普票弹框页面", notes = "普票弹框页面", tags = "发票管理-发票申请", response = R.class)
	@GetMapping("/normal/add")
	public ModelAndView normalAdd(ModelAndView mv, HttpSession session) {
		mv.setViewName("finance/invoice/application/normaladd");
		return mv;
	}

	@ApiOperation(value = "增票弹框页面", notes = "增票弹框页面", tags = "发票管理-发票申请", response = R.class)
	@GetMapping("/vat/add")
	public ModelAndView vatAdd(ModelAndView mv, HttpSession session) {
		mv.setViewName("finance/invoice/application/vatadd");
		return mv;
	}


	@ApiOperation(value = "获取OEM代理商", notes = "获取OEM代理商", tags = "发票管理-发票申请", response = R.class)
	@GetMapping("/app/agentlist")
	@ResponseBody
	public R agentList(HttpSession session) {
		R r;
		try {
			UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
			List<JsmsAgentInfo> agentInfos = agentInvoiceService.getAgentList(userSession.getId());
			r = R.ok("获取客户信息成功", agentInfos);
		} catch (Exception e) {
			logger.debug("获取客户信息失败{}", e);
			r = R.error(Code.SYS_ERR, e.getMessage());
		}

		return r;
	}

	@ApiOperation(value = "获取发票信息配置", notes = "获取发票信息配置", tags = "发票管理-发票申请", response = R.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "agentId", value = "代理商ID", dataType = "int", paramType = "query"),
			@ApiImplicitParam(name = "invoiceType", value = "发票类型", dataType = "int", paramType = "query") })
	@GetMapping("/app/invoiceconfig")
	@ResponseBody
	public R invoiceConfig(Integer agentId, Integer invoiceType, HttpSession session) {
		R r;
		try {
			r = agentInvoiceService.getInvoiceConfig(agentId, invoiceType);
		} catch (Exception e) {
			logger.debug("获取发票信息配置失败{}", e);
			r = R.error(Code.SYS_ERR, e.getMessage());
		}

		return r;
	}

	@ApiOperation(value = "获取可开票金额", notes = "获取可开票金额", tags = "发票管理-发票申请", response = R.class)
	@GetMapping("/app/canamount/{agentId}")
	@ResponseBody
	public R canAmount(@PathVariable("agentId") Integer agentId) {
		R r;
		try {

			if (agentId == null) {
				r = R.error(Code.SYS_ERR, "客户ID不能为空");
				return r;
			}

			BigDecimal decimal = agentInvoiceService.getCanInvoiceAmount(agentId);
			r = R.ok("获取可开票金额成功", decimal);
		} catch (Exception e) {
			logger.debug("获取可开票金额失败{}", e);
			r = R.error(Code.SYS_ERR, e.getMessage());
		}

		return r;
	}

	@ApiOperation(value = "发票申请保存", notes = "发票申请保存<br/>"
			+ " 申请id：invoiceId，<br/>"
			+ "代理商id：agentId，<br/>"
			+ " 发票金额：invoiceAmount，<br/>"
			+ "开票主体：1：个人2：企业 invoiceBody，<br/>"
			+ " 发票类型，1：普通发票（电子）2：增值税专票 invoiceType，<br/>"
			+ " 电子邮箱：email，<br/>"
			+ " 发票抬头： invoiceHead，<br/>"
			+ " 统一社会信用代码：creditCode，<br/>"
			+ " 开户银行： bank，<br/>"
			+ " 开户账号：bankAccount，<br/>"
			+ " 公司注册地址：companyRegAddr，<br/>"
			+ " 公司固定电话：telphone，<br/>"
			+ " 收件人：toName，<br/>"
			+ " 收件人手机：toPhone，<br/>"
			+ " 收件人地址,省市区以英文逗号“,”分隔：toAddr，<br/>"
			+ " 收件人详细地址：toAddrDetail，<br/>"
			+ " 收件人qq：toQq，<br/>"
			+ " 快递公司，关联t_sms_dict表中的参数类型为“express”的键值：expressCompany，<br/>"
			+ " 快递单号：expressOrder，<br/>"
			+ " 状态，0：待审核，1：已取消，2：审核不通过，3：开票中，4：已邮寄，5：已返还： status，<br/>"
			+ " 审核不通过原因：auditFailCause，<br/>"
			+ " 备注： remark，", tags = "发票管理-发票申请", response = R.class)
	@PostMapping("/app/save")
	@ResponseBody
	public R application(@RequestBody JsmsAgentInvoiceList invoiceList, HttpSession session) {
		R r;
		try {
			UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());

			if (invoiceList == null) {
				r = R.error(Code.SYS_ERR, "发票信息不能为空");
				return r;
			}

			// 设置操作者
			invoiceList.setOperator(userSession.getId());

			r = agentInvoiceService.applicationInvoice(invoiceList);
		} catch (Exception e) {
			logger.debug("发票申请异常{}", e);
			r = R.error(Code.SYS_ERR, e.getMessage());
		}

		return r;
	}

	@ApiOperation(value = "发票返还页面", notes = "发票返还页面", tags = "发票管理-返还发票", response = R.class)
	@GetMapping("/retuan/add")
	public ModelAndView returnInvoiceView(ModelAndView mv, HttpSession session) {
		mv.setViewName("finance/invoice/return/add");
		return mv;
	}

	@ApiOperation(value = "发票返还获取已邮寄列表", notes = "发票返还获取已邮寄列表", tags = "发票管理-返还发票", response = R.class)
	@ApiImplicitParams({
			@ApiImplicitParam(name = "agentId", value = "客户ID", dataType = "String", paramType = "form"),
			@ApiImplicitParam(name = "condition", value = "发票抬头/申请ID", dataType = "String", paramType = "form"),
			@ApiImplicitParam(name = "invoiceType", value = "发票类型", dataType = "int", paramType = "form"),
			@ApiImplicitParam(name = "startTime", value = "起始时间", dataType = "String", paramType = "form"),
			@ApiImplicitParam(name = "endTime", value = "截止时间", dataType = "String", paramType = "form") })
	@PostMapping("/retuan/invoicelist")
	@ResponseBody
	public R invoiceList(@ApiParam(name = "params", hidden = true) @RequestParam Map params) {
		R r;
		try {
			if (params == null || null == params.get("agentId")) {
				r = R.error(Code.SYS_ERR, "客户ID不能为空");
				return r;
			}

			List<String> keys = new ArrayList<>();
			for (Object o : params.keySet()) {
				if(params.get(o) == null || StringUtils.isBlank(params.get(o).toString()))
				{
					keys.add(o.toString());
				}
			}
			for (String s : keys) {
				params.remove(s);
			}

			params.put("status", InvoiceStatusEnum.已邮寄.getValue().toString());

			List<JsmsAgentInvoiceList> invoiceLists = agentInvoiceService.findInvoiceList4ReturnInvoice(params);

			r = R.ok("获取已邮寄发票列表成功", invoiceLists);
		} catch (Exception e) {
			logger.debug("获取已邮寄发票列表失败{}", e);
			r = R.error(Code.SYS_ERR, e.getMessage());
		}

		return r;
	}

	@ApiOperation(value = "获取已开票初始化金额", notes = "获取已开票初始化金额", tags = "发票管理-返还发票", response = R.class)
	@GetMapping("/retuan/hastakeinit/{agentId}")
	@ResponseBody
	public R hasTakeInit(@PathVariable("agentId") Integer agentId) {
		R r;
		try {
			if (agentId == null) {
				r = R.error(Code.SYS_ERR, "客户ID不能为空");
				return r;
			}

			BigDecimal decimal = agentInvoiceService.getHasTakeInvoiceInitAmount(agentId);
			r = R.ok("获取已开票初始化金额成功", decimal);
		} catch (Exception e) {
			logger.debug("获取已开票初始化失败{}", e);
			r = R.error(Code.SYS_ERR, e.getMessage());
		}

		return r;
	}

	@ApiOperation(value = "获取可退款金额", notes = "获取可退款金额", tags = "发票管理-返还发票", response = R.class)
	@GetMapping("/canbackmoney/{agentId}")
	@ResponseBody
	public R canBackMoney(@PathVariable("agentId") Integer agentId) {
		R r;
		try {
			if (agentId == null) {
				r = R.error(Code.SYS_ERR, "客户ID不能为空");
				return r;
			}

			BigDecimal decimal = agentInvoiceService.getCanBackAmount(agentId);
			r = R.ok("获取可退款金额成功", decimal);
		} catch (Exception e) {
			logger.debug("获取可退款金额失败{}", e);
			r = R.error(Code.SYS_ERR, e.getMessage());
		}

		return r;
	}

	@ApiOperation(value = "返还发票保存", notes = "返还发票保存<br/>"
			+ " 申请id：invoiceId，<br/>"
			+ " 代理商id：agentId，<br/>"
			+ " 是否已开票（Booblen）：openInvoice，<br/>"
			+ " 开票主体：1：个人2：企业 invoiceBody，<br/>"
			+ " 发票类型，1：普通发票（电子）2：增值税专票 invoiceType，<br/>"
			+ " 发票抬头： invoiceHead，<br/>"
			+ " 发票金额： invoiceAmount，<br/>"
			+ " 返还发票申请ID列表：returnInvoiceIds", tags = "发票管理-返还发票", response = R.class)
	@PostMapping("/retuan/save")
	@ResponseBody
	public R returnInvoice(@RequestBody JsmsAgentInvoiceListPo invoiceList, HttpSession session) {
		R r;
		try {
			UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());

			if (invoiceList == null) {
				r = R.error(Code.SYS_ERR, "发票信息不能为空");
				return r;
			}

			// 设置操作者
			invoiceList.setOperator(userSession.getId());

			r = agentInvoiceService.returnInvoice(invoiceList);
		} catch (Exception e) {
			logger.debug("返还发票异常{}", e);
			r = R.error(Code.SYS_ERR, e.getMessage());
		}

		return r;
	}
}
