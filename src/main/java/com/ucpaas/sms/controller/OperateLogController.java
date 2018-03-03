package com.ucpaas.sms.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.admin.SmsOprateLogService;

@Controller
@RequestMapping("/smsOprateLog")
public class OperateLogController {

	private static Logger logger = LoggerFactory.getLogger(OperateLogController.class);

	@Autowired
	private SmsOprateLogService smsOprateLogService;

	@RequestMapping(path = "/query", method = RequestMethod.GET)
	public ModelAndView page(ModelAndView mv) {
		String start_time, end_time;
		DateTime dt = DateTime.now();
		start_time = dt.minusHours(1).toString("yyyy-MM-dd HH:mm:ss");
		end_time = dt.toString("yyyy-MM-dd HH:mm:ss");
		mv.addObject("start_time", start_time);
		mv.addObject("end_time", end_time);
		mv.setViewName("smsOprateLog/list");
		return mv;
	}

	@RequestMapping(path = "/list", method = RequestMethod.POST)
	@ResponseBody
	public PageContainer query(String rows, String page, String text, String realname, String email, //
			String module, String pageUrl, String ip, String opDesc, String start_time, String end_time, //
			String opType) {

		String pageRowCount = rows;
		String currentPage = page;
		Map<String, String> params = new HashMap<>();
		params.put("pageRowCount", rows);
		params.put("currentPage", page);
		params.put("text", text);
		params.put("realname", realname);
		params.put("email", email);
		params.put("module", module);
		params.put("pageUrl", pageUrl);
		params.put("ip", ip);
		params.put("opDesc", opDesc);
		if (StringUtils.isEmpty(start_time) && StringUtils.isEmpty(end_time)) {
			DateTime dt = DateTime.now();
			start_time = dt.minusHours(1).toString("yyyy-MM-dd HH:mm:ss");
			end_time = dt.toString("yyyy-MM-dd HH:mm:ss");
		}
		params.put("start_time", start_time);
		params.put("end_time", end_time);

		PageContainer pageData = smsOprateLogService.query(params);
		return pageData;
	}

	/** 获取模块名称 */
	@RequestMapping(value = "/modules", method = RequestMethod.GET)
	@ResponseBody
	public List<Map<String, Object>> listModules() {
		return smsOprateLogService.queryModuleList();
	}

}
