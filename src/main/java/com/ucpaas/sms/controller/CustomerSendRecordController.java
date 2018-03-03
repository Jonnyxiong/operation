package com.ucpaas.sms.controller;

import com.jsmsframework.common.entity.JsmsSystemErrorDesc;
import com.jsmsframework.common.service.JsmsSystemErrorDescService;
import com.jsmsframework.user.entity.JsmsAccount;
import com.jsmsframework.user.service.JsmsAccountService;
import com.ucpaas.sms.annotation.JSON;
import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.common.util.file.ExcelUtils;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.enums.SessionEnum;
import com.ucpaas.sms.model.Excel;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.CustomerSendRecordService;
import com.ucpaas.sms.util.AgentUtils;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.FileUtils;
import com.ucpaas.sms.util.JsonUtils;
import com.ucpaas.sms.util.rest.utils.DateUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * 客户发送记录
 *
 */
@Controller
@RequestMapping("/customersendrecord")
public class CustomerSendRecordController extends BaseController {
	private static final Logger logger = LoggerFactory.getLogger(CustomerSendRecordController.class);
	String Error = "请输入同一天日期";

	@Autowired
	private CustomerSendRecordService customerSendRecordService;

	@Autowired
	private JsmsAccountService jsmsAccountService;
	@Autowired
	private JsmsSystemErrorDescService jsmsSystemErrorDescService;
	@RequestMapping(path = "/view", method = RequestMethod.GET)
	public ModelAndView query(ModelAndView mv) {
		mv.addObject("max_export_num", ConfigUtils.max_export_excel_num);
		mv.setViewName("customersendrecord/list");
		return mv;
	}

	/**
	 *
	 * 查询所有的客户
	 */
	@RequestMapping("/customersendrecordAccounts")
	@JSON(type = JsmsAccount.class, include="clientid,name")
	public List<JsmsAccount> getAccounts(HttpSession session, HttpServletRequest request) {
		UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		Map<String,Object> params = new HashMap<String,Object>();
		params.put("dataAuthorityCondition",AgentUtils.getDataAuthorityCondition(user.getId(), true,false));
		List<JsmsAccount> data = jsmsAccountService.getLikeIdAndName(params);// 查询所用用户为当前登陆用户
		return data;
	}

	/**
	 * 查询所用的
	 *
	 * @throws ParseException
	 */
	@RequestMapping(path = "/customersendrecordQuery", method = RequestMethod.POST)
	@ResponseBody
	public String autoTemplateQuery(HttpSession session, HttpServletRequest request,
									String send_status,String send_content,String account_id,
									String start_time_day,String end_time_day,
									String page,String rows,String phone
	) throws ParseException {
		JsmsSystemErrorDesc jsmsSystemErrorDesc = new JsmsSystemErrorDesc();
		Map<String, Object> params = new HashMap<String, Object>();
		if (StringUtils.isNotBlank(send_status)) {
			params.put("send_status", send_status);
		}
		// 手机号码
		if (StringUtils.isNotBlank(phone)) {
			params.put("phone", phone);
		}
		// 短信内容
		if (StringUtils.isNotBlank(send_content)) {
			params.put("content", send_content);
		}
		if (StringUtils.isNotBlank(account_id)) {
			params.put("account_id", account_id);
		}
		if (StringUtils.isNotBlank(page)) {
			params.put("currentPage", page);
		}
		if (StringUtils.isNotBlank(rows)) {
			params.put("pageRowCount", rows);
		}
		// 创建时间：开始时间
		params.put("createStartTime", start_time_day);
		params.put("createEndTime", end_time_day);
		if (StringUtils.isBlank(end_time_day)) {
			if (StringUtils.isBlank(start_time_day) || StringUtils.isBlank(end_time_day)) {
				DateTime dt = DateTime.now();
				start_time_day = dt.minusMinutes(3).withMillisOfDay(0).toString("yyyy-MM-dd HH:mm:ss");
				end_time_day = dt.toString("yyyy-MM-dd HH:mm:ss");
				params.put("createStartTime", start_time_day);
				params.put("createEndTime", end_time_day);
				String dataStr = dt.toString("yyyyMMdd");
				params.put("data", dataStr);
			}
			PageContainer pageContainer = customerSendRecordService.queryAll(params);
			return JsonUtils.toJson(pageContainer);
		}
		SimpleDateFormat dateFormater = new SimpleDateFormat("yyyy-MM-dd");
		Date dat = dateFormater.parse(end_time_day);
		String dataStr = dateFormater.format(dat);
		params.put("data", dataStr);
		PageContainer pageContainer = customerSendRecordService.queryAll(params);
		if(pageContainer.getList().size()>0){
			for(int i =0;i<pageContainer.getList().size();i++){
				if(String.valueOf(((Map)(pageContainer.getList().get(i))).get("state")).equals("0")){
					((Map)(pageContainer.getList().get(i))).put("status","发送中");
					((Map)(pageContainer.getList().get(i))).put("errorcode_name","-");
				}else if(String.valueOf(((Map)(pageContainer.getList().get(i))).get("state")).equals("1")){
					((Map)(pageContainer.getList().get(i))).put("status","未知");
					((Map)(pageContainer.getList().get(i))).put("errorcode_name","-");
				}else if(String.valueOf(((Map)(pageContainer.getList().get(i))).get("state")).equals("3")){
					((Map)(pageContainer.getList().get(i))).put("status","发送成功");
					((Map)(pageContainer.getList().get(i))).put("errorcode_name","-");
				}else if(String.valueOf(((Map)(pageContainer.getList().get(i))).get("state")).equals("4")){
					((Map)(pageContainer.getList().get(i))).put("status","发送失败");
					((Map)(pageContainer.getList().get(i))).put("errorcode_name","其它错误"+((Map)(pageContainer.getList().get(i))).get("submit"));
				}else if(String.valueOf(((Map)(pageContainer.getList().get(i))).get("state")).equals("6")){
					((Map)(pageContainer.getList().get(i))).put("status","发送失败");
					((Map)(pageContainer.getList().get(i))).put("errorcode_name","其它错误"+((Map)(pageContainer.getList().get(i))).get("report"));
				}else{
					((Map)(pageContainer.getList().get(i))).put("status","拦截");
					String errorcode = ((Map)(pageContainer.getList().get(i))).get("errorcode").toString();
					if(com.ucpaas.sms.common.util.StringUtils.isNotBlank(errorcode)){
						jsmsSystemErrorDesc =jsmsSystemErrorDescService.getBySyscode(errorcode.substring(0,7));
						if(jsmsSystemErrorDesc!=null){
							((Map)(pageContainer.getList().get(i))).put("errorcode_name",errorcode.substring(0,7)+"-"+jsmsSystemErrorDesc.getClientSideNote());
						}else{
							((Map)(pageContainer.getList().get(i))).put("errorcode_name",errorcode.substring(0,7)+"-"+"原因不明");
						}
					}else{
						((Map)(pageContainer.getList().get(i))).put("errorcode_name"," ");
					}
				}
			}
		}
		return JsonUtils.toJson(pageContainer);
	}


	/*
	 * 批量导出报表
	 *
	 */
	@RequestMapping(value = "/exportRecord")
	@ResponseBody
	public Map exportRecord(HttpServletRequest request, HttpServletResponse response, HttpSession session,
							@RequestParam Map<String, Object> params) throws Exception {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put("success", false);
		result.put("msg", "生成报表失败");
		try {


			Map<String, Object> objectMap = new HashMap<>();
			StringBuilder parstr = new StringBuilder();

			Object obj = new Object();
			obj = request.getParameter("send_status");
			if (obj.equals("1")) {
				params.put("send_status", "1");
			}
			if (obj.equals("0")) {
				params.put("send_status", "0");
			}
			if (obj.equals("3")) {
				params.put("send_status", "3");
			}
			if (obj.equals("55")) {

				parstr = parstr.append("'").append(5).append("'").append(",");
				parstr = parstr.append("'").append(7).append("'").append(",");
				parstr = parstr.append("'").append(8).append("'").append(",");
				parstr = parstr.append("'").append(9).append("'").append(",");
				parstr = parstr.append("'").append(10).append("'");
				params.put("send_status", parstr.toString());
			}
			if (obj.equals("46")) {
				parstr = parstr.append("'").append(4).append("'").append(",");
				parstr = parstr.append("'").append(6).append("'");
				params.put("send_status", parstr.toString());
			}
			// 手机号码
			obj = request.getParameter("phone");
			if (obj != null) {
				objectMap.put("phone", obj);
			}
			// 短信内容
			obj = request.getParameter("send_content");
			if (obj != null) {
//				objectMap.put("content", obj);
				params.put("content", obj);
			}
			obj = request.getParameter("account_id");
			if (obj != null) {
				objectMap.put("clientid", obj);
			}


			// SmsAccountModelPo accountModel = (SmsAccountModelPo)
			// session.getAttribute(SessionEnum.LOGIN_USER_INFO);
			// UserSession accountModel = (UserSession)
			// session.getAttribute(SessionEnum.SESSION_USER.toString());
			// if (accountModel == null) {
			// result.put("success", false);
			// result.put("msg", "非法请求");
			// return result;
			// }
			// String clientId = accountModel.getClientId();
			String clientId = request.getParameter("account_id");

			// Map<String, String> params = ServletUtil.getFormData(request);
			// Map<String, String> params = new HashMap<String, String>();
			params.put("clientid", clientId);

//			Map<String, Object> objectMap = new HashMap<>();
			String start_time = Objects.toString(request.getParameter("start_time_day"), "");
			String end_time = Objects.toString(request.getParameter("end_time_day"), "");

			params.put("createStartTime", start_time);
			params.put("createEndTime", end_time);

			StringBuffer fileName = new StringBuffer();
			fileName.append("短信记录-");
			if (end_time == null || "".equals(end_time)) {
				// 设置默认时间
				if (start_time == null || start_time.equals("") || end_time == null || end_time.equals("")) {
					DateTime dt = DateTime.now();
					start_time = dt.minusMinutes(3).toString("yyyy-MM-dd HH:mm:ss");
					start_time = dt.withMillisOfDay(0).toString("yyyy-MM-dd HH:mm:ss");
					end_time = dt.toString("yyyy-MM-dd HH:mm:ss");
					params.put("createStartTime", start_time);
					params.put("createEndTime", end_time);
				}
			}

			Date endTime = DateUtil.stringToDate(end_time, "yyyy-MM-dd HH:mm:ss");
			fileName.append(DateUtil.dateToStr(endTime, "yyyyMMddHHmmss"));
			fileName.append(".xls");

			SimpleDateFormat dateFormater = new SimpleDateFormat("yyyyMMdd");
			// Date dat = dateFormater.parse(end_time);
			String dataStr = dateFormater.format(endTime);
			params.put("data", dataStr);

			// String text =
			// Objects.toString(request.getParameter("end_time_day"), "");
			String filePath = ConfigUtils.save_path + "/" + clientId + "/" + fileName.toString();

			Excel excel = new Excel();
			excel.setFilePath(filePath);
			excel.setTitle("短信记录");
			excel.addHeader(20, "手机号", "phone");
			excel.addHeader(20, "发送内容", "content");
			excel.addHeader(20, "发送状态", "status");
			excel.addHeader(20, "状态码", "errorcode_name");
			excel.addHeader(20, "发送时间", "sendTime");
			excel.addHeader(20, "计费条数", "charge_num");

			Map<String, Object> p = new HashMap<String, Object>();
			p.putAll(params);

			// if (!isOem) {
			// buildSMSType(p);
			// }
			// p.put("isOem", ServletUtil.isOem(accountModel));

			List<Map<String, Object>> list = customerSendRecordService.querySmsRecord4Excel(params);
			excel.setDataList(list);

			if (ExcelUtils.exportExcel(excel)) {
				FileUtils.download(filePath, response);
				FileUtils.delete(filePath);
			}
		} catch (Exception e) {
			response.setContentType("text/plain;charset=UTF-8");
			try {
				response.getWriter().write("导出Excel文件失败，请联系管理员");
				response.getWriter().flush();
			} catch (IOException e1) {
				logger.error("导出Excel文件失败", e1);
			}
		}
		return result;

	}

}
