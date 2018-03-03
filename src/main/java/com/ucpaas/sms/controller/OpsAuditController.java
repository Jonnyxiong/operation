package com.ucpaas.sms.controller;

import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.enums.SessionEnum;
import com.ucpaas.sms.exception.OperationException;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.audit.AgentAuditService;
import com.ucpaas.sms.service.audit.CustomerAuditService;
import com.ucpaas.sms.service.audit.SmsTemplateService;
import com.ucpaas.sms.util.AgentUtils;
import com.ucpaas.sms.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/opsAudit")
public class OpsAuditController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(OpsAuditController.class);

	@Autowired
	private AgentAuditService agentAuditService;
	@Autowired
	private CustomerAuditService customerAuditService;
	@Autowired
	private SmsTemplateService smsTemplateService;

	@RequestMapping(path = "/agent/query", method = RequestMethod.GET)
	public ModelAndView agentQuery(ModelAndView mv, HttpSession session) {
		mv.setViewName("opsAudit/agent/list");

		UserSession user = getUserFromSession(session);
		mv.addObject("menus", AgentUtils.hasMenuRight(user, "客户资质审核-dlszzsh", "子账户资质审核-khzzsh", "短信模板审核-dxmbsh"));
		return mv;
	}

	@RequestMapping(path = "/agent/list", method = RequestMethod.POST)
	@ResponseBody
	public PageContainer list(String rows, String page, String condition, String oauth_status, String start_time,
			String end_time) {
		String pageRowCount = rows;
		String currentPage = page;
		Map<String, String> params = new HashMap<String, String>();
		params.put("pageRowCount", rows);
		params.put("currentPage", page);
		params.put("text", condition);
		params.put("oauth_status", oauth_status);
		params.put("start_time", start_time);
		params.put("end_time", end_time);

		PageContainer pageContainer = agentAuditService.query(params);
		return pageContainer;
	}

	@RequestMapping(path = "/agent/view", method = RequestMethod.GET)
	public ModelAndView agentView(ModelAndView mv, String agentId,String type) {
		Map params = new HashMap<>();
		params.put("agent_id", agentId);
		params.put("type", type);
		Map data = agentAuditService.view(params);
		mv.addObject("data", data);
		mv.setViewName("opsAudit/agent/view");
		return mv;
	}

	@RequestMapping(path = "/agent/audit")
	@ResponseBody
	public ResultVO agentAudit(String agent_id, String oauth_status, String remark, HttpSession session,
							   HttpServletRequest request) {
		ResultVO result = ResultVO.failure();
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		Long userId = userSession.getId();
		Map<String, String> params = new HashMap<String, String>();
		params.put("admin_id", String.valueOf(userId));
		params.put("audit_type", "1"); // 认证类型：1-代理商认证 2-客户认证
		params.put("oauth_status", oauth_status);
		params.put("remark", remark);
		params.put("agent_id", agent_id);

		params.put("userId", userSession.getId().toString());
		params.put("pageUrl", request.getRequestURI());
		params.put("ip", HttpUtils.getIpAddress(request));

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

	@RequestMapping(path = "/customer/query", method = RequestMethod.GET)
	public ModelAndView customerQuery(ModelAndView mv, HttpSession session) {
		mv.setViewName("opsAudit/customer/list");

		UserSession user = getUserFromSession(session);
		mv.addObject("menus", AgentUtils.hasMenuRight(user, "客户资质审核-dlszzsh", "子账户资质审核-khzzsh", "短信模板审核-dxmbsh"));
		return mv;
	}

	@RequestMapping(path = "/customer/list", method = RequestMethod.POST)
	@ResponseBody
	public PageContainer customerList(String rows, String page, String condition, String oauth_status,
			String start_time, String end_time) {
		String pageRowCount = rows;
		String currentPage = page;
		Map<String, String> params = new HashMap<String, String>();
		params.put("pageRowCount", rows);
		params.put("currentPage", page);
		params.put("text", condition);
		params.put("oauth_status", oauth_status);
		params.put("start_time", start_time);
		params.put("end_time", end_time);

		PageContainer pageContainer = customerAuditService.query(params);
		return pageContainer;
	}

	@RequestMapping(path = "/customer/view", method = RequestMethod.GET)
	public ModelAndView customerView(ModelAndView mv, String clientId,String type) {
		Map params = new HashMap<>();
		Map data = new HashMap<>();
		params.put("client_id", clientId);
		params.put("type", type);
		/*if(StringUtils.isNotBlank(type)&&("2").equals(type)){//代理商子客户
			 data = customerAuditService.viewOfAccount(params);
		}else if(StringUtils.isNotBlank(type)&&("3").equals(type)){//直客
			data = customerAuditService.viewOfAccount(params);
		}
		else{//代理商*/
			data = customerAuditService.view(params);
		//}
		mv.addObject("data", data);
		mv.setViewName("opsAudit/customer/view");
		return mv;
	}

	@RequestMapping(path = "/customer/audit")
	@ResponseBody
	public ResultVO customerAudit(String client_id, String oauth_status, String remark, HttpSession session,
			HttpServletRequest request) {
		ResultVO result = ResultVO.failure();
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		Long userId = userSession.getId();
		Map<String, String> params = new HashMap<String, String>();
		params.put("admin_id", String.valueOf(userId));
		params.put("audit_type", "2"); // 认证类型：1-代理商认证 2-客户认证
		params.put("oauth_status", oauth_status);
		params.put("remark", remark);
		params.put("client_id", client_id);

		params.put("userId", userSession.getId().toString());
		params.put("pageUrl", request.getRequestURI());
		params.put("ip", HttpUtils.getIpAddress(request));

		Map resultMap = null;

		try {
			resultMap = customerAuditService.audit(params);
		} catch (OperationException o) {
			o.printStackTrace();
			logger.error(o.getMessage(),o);
			return ResultVO.failure(o.getMessage());
		}catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage(),e);
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

	@RequestMapping(path = "/smsTemplate/query", method = RequestMethod.GET)
	public ModelAndView smsTemplateQuery(ModelAndView mv, HttpSession session) {
		mv.setViewName("opsAudit/smsTemplate/list");

		UserSession user = getUserFromSession(session);
		mv.addObject("menus", AgentUtils.hasMenuRight(user, "客户资质审核-dlszzsh", "子账户资质审核-khzzsh", "短信模板审核-dxmbsh"));
		return mv;
	}

	@RequestMapping(path = "/smsTemplate/list", method = RequestMethod.POST)
	@ResponseBody
	public PageContainer smsTemplateList(String rows, String page, String condition, String oauth_status) {
		String pageRowCount = rows;
		String currentPage = page;
		Map<String, String> params = new HashMap<String, String>();
		params.put("pageRowCount", rows);
		params.put("currentPage", page);
		params.put("text", condition);
		params.put("oauth_status", oauth_status);

		PageContainer pageContainer = smsTemplateService.query(params);
		return pageContainer;
	}

	@RequestMapping(path = "/smsTemplate/isSessionValid")
	@ResponseBody
	public boolean isSessionValid() {
		return true;
	}

	@RequestMapping(path = "/smsTemplate/view")
	public ModelAndView smsTemplateView(ModelAndView mv, String template_id, String state) {
		Map params = new HashMap<>();
		params.put("template_id", template_id);
		params.put("state", state);
		Map data = smsTemplateService.view(params);
		if (!"1".equals(state)) {
			data.put("channel_tempid", "");
			data.put("channelid", "");
			data.put("result", "");
			data.put("remark", "");
		}
		if (data != null) {
			data.put("state", params != null ? params.get("state") : null);
		}
		mv.addObject("data", data);
		mv.setViewName("opsAudit/smsTemplate/view");
		return mv;
	}

	@RequestMapping(path = "/smsTemplate/auditTransfer")
	@ResponseBody
	public ResultVO smsTemplateAuditTransfer(String template_id,String last_time,String channel_tempid, String channelid, String result, String remark,String check_status,
			HttpSession session, HttpServletRequest request) {
		ResultVO resultVO = ResultVO.failure();
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		Map<String, String> params = new HashMap<String, String>();
		params.put("channel_tempid", channel_tempid);
		params.put("channelid", channelid);
		params.put("result", result);
		params.put("remark", remark);
		params.put("template_id", template_id);
		params.put("last_time", last_time);
		params.put("check_status", check_status);

		params.put("admin_id", userSession.getId().toString());
		params.put("userId", userSession.getId().toString());
		params.put("pageUrl", request.getRequestURI());
		params.put("ip", HttpUtils.getIpAddress(request));

		Map resultMap = smsTemplateService.auditTransfer(params);
		if ("success".equals(resultMap.get("result"))) {
			resultVO.setSuccess(true);
			resultVO.setMsg((String) resultMap.get("msg"));
		} else {
			resultVO.setSuccess(false);
			resultVO.setMsg((String) resultMap.get("msg"));
		}
		return resultVO;
	}

	@RequestMapping(path = "/smsTemplate/audit") 
	public ModelAndView smsTemplateAudit(ModelAndView mv, String template_id, String state) {
		Map params = new HashMap<>();
		params.put("template_id", template_id);
		params.put("state", state);
		Map data = smsTemplateService.view(params);
		data.putAll(params);
		mv.addObject("data", data);
		mv.setViewName("opsAudit/smsTemplate/audit");
		return mv;
	}
	
	@RequestMapping(path = "/smsTemplate/auditAction")
	@ResponseBody
	public ResultVO smsTemplateAuditAction(String content,String check_status,String template_id, String sms_type, String type, String last_time,
			HttpSession session, HttpServletRequest request) {
		ResultVO resultVO = ResultVO.failure();
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		Map<String, String> params = new HashMap<String, String>();
		params.put("content", content);
		params.put("check_status", check_status);
		params.put("template_id", template_id);
		params.put("sms_type", sms_type);
		params.put("type", type);
		params.put("last_time", last_time);
		
		

		params.put("admin_id", userSession.getId().toString());
		params.put("userId", userSession.getId().toString());
		params.put("pageUrl", request.getRequestURI());
		params.put("ip", HttpUtils.getIpAddress(request));

		Map resultMap = smsTemplateService.audit(params);
		if ("success".equals(resultMap.get("result"))) {
			resultVO.setSuccess(true);
			resultVO.setMsg((String) resultMap.get("msg"));
		} else {
			resultVO.setSuccess(false);
			resultVO.setMsg((String) resultMap.get("msg"));
		}
		return resultVO;
	}
	
	@RequestMapping(path = "/smsTemplate/viewOriginal")
	@ResponseBody
	public ResultVO smsTemplateViewOriginal(String template_id,String flag) {
		ResultVO resultVO = ResultVO.failure();
		Map<String, String> params = new HashMap<String, String>();
		params.put("template_id", template_id);
		params.put("flag", flag);
		
		Map data = smsTemplateService.view(params);
		resultVO.setSuccess(true);
		resultVO.setData(data); 
		return resultVO;
	}

}
