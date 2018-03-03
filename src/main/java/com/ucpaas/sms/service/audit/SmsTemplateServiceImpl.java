package com.ucpaas.sms.service.audit;

import com.ucpaas.sms.constant.LogConstant.LogType;
import com.ucpaas.sms.dao.MessageMasterDao;
import com.ucpaas.sms.entity.CallbackResponse;
import com.ucpaas.sms.entity.Template;
import com.ucpaas.sms.enums.LogEnum;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.common.LogService;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.HttpUtils;
import com.ucpaas.sms.util.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@Transactional
public class SmsTemplateServiceImpl implements SmsTemplateService {

	private static Logger logger = LoggerFactory.getLogger(SmsTemplateServiceImpl.class);

	@Autowired
	private MessageMasterDao masterDao;
	@Autowired
	private LogService logService;

	/**
	 * 查询短信模板
	 */
	@Override
	public PageContainer query(Map<String, String> params) {
		return masterDao.getSearchPage("smsTemplate.query", "smsTemplate.queryCount", params);
	}

	/**
	 * 审核短信模板
	 */
	@Override
	public Map<String, Object> audit(Map<String, String> params) {
		Map<String, Object> result = new HashMap<String, Object>();
		String content = params.get("content");
		// var reg = /^([^\{\}]*\{[^\{\}]*\}[^\{\}]*){0,4}[^\{\}]*$/;
		if (!(content != null && content.matches("^([^\\{\\}]*\\{[^\\{\\}]*\\}[^\\{\\}]*){0,4}[^\\{\\}]*$"))) {
			result.put("result", "fail");
			result.put("msg", "请使用正确的模板参数格式");
			return result;
		}
		params.put("update_time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		int updateNum = commonAudit(params, result);

		if (updateNum <= 0) {
			return result;
		}

		//  推送通知
		int templateId = Integer.parseInt(params.get("template_id"));
		Map<String, Integer> queryMap = new HashMap<String, Integer>();
		queryMap.put("templateId", templateId);
		Template template = masterDao.getOneInfo("smsTemplate.queryTemplateTemporary", queryMap);
		if (null != template) {
			final String tmpClientId = template.getClientId();
			final Template tmpTemplate = template;
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					auditNotice(tmpClientId, tmpTemplate);
				}
			});
			t.start();
		}

		Long userId = Long.valueOf(params.get("userId"));
		String pageUrl = params.get("pageUrl");
		String ip = params.get("ip");
		// logService.add(LogType.update, LogEnum.审核管理.getValue(),
		// "审核管理管理- 短信模板审核：审核", params, result);
		logService.add(LogType.update, LogEnum.审核管理.getValue(), userId, pageUrl, ip, "审核管理管理- 短信模板审核：审核", params,
				result);

		return result;
	}

	/**
	 * 查看模板的详细信息(根据模板id)
	 */
	@Override
	public Map<String, Object> view(Map<String, String> params) {
		Map<String, Object> data = new HashMap<String, Object>();
		if ("true".equals(params.get("flag"))) {
			data = masterDao.getOneInfo("smsTemplate.getOldTemplateInfo", params);
		} else {
			if ("1".equals(params.get("state"))) {
				data = masterDao.getOneInfo("smsTemplate.getDetailInfo", params);
			} else {
				data = masterDao.getOneInfo("smsTemplate.getTemplateInfo", params);
			}
		}
		return data;
	}

	/**
	 * 录入数据(转审结果)
	 */
	@Override
	public Map<String, Object> auditTransfer(Map<String, String> params) {
		Map<String, Object> result = new HashMap<String, Object>();
		params.put("update_time", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
		int updateNum = commonAudit(params, result);
		if (updateNum <= 0) {
			return result;
		}
		int insertNum = masterDao.insert("smsTemplate.insertAuditTransfer", params);

		if (updateNum != insertNum) {
			throw new RuntimeException("录入数据(转审结果) : 添加的数据条数和修改的模板数不对应");
		}

		//  推送通知
		int templateId = Integer.parseInt(params.get("template_id"));
		Map<String, Integer> queryMap = new HashMap<String, Integer>();
		queryMap.put("templateId", templateId);
		Template template = masterDao.getOneInfo("smsTemplate.queryTemplateTemporary", queryMap);
		if (null != template) {
			final String tmpClientId = template.getClientId();
			final Template tmpTemplate = template;
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					auditNotice(tmpClientId, tmpTemplate);
				}
			});
			t.start();
		}

		// logService.add(LogType.update, LogEnum.审核管理.getValue(),
		// "审核管理管理- 短信模板审核：录入转审结果", params, result);

		Long userId = Long.valueOf(params.get("userId"));
		String pageUrl = params.get("pageUrl");
		String ip = params.get("ip");
		logService.add(LogType.update, LogEnum.审核管理.getValue(), userId, pageUrl, ip, "审核管理管理- 短信模板审核：录入转审结果", params,
				result);
		return result;

	}

	/**
	 * @Description: 模板审核
	 * @author: Niu.T
	 * @date: 2016年12月29日 下午3:41:35
	 * @param params
	 * @param result
	 */
	private int commonAudit(Map<String, String> params, Map<String, Object> result) {
		params.put("update_type", "3");

		int num1 = masterDao.update("smsTemplate.updateTempStatus", params);
		if (num1 == 0) {

			result.put("result", "none");
			result.put("msg", "数据已被其他人员修改,请刷新后再试...");
			return num1;
		}
		int num2 = masterDao.update("smsTemplate.updateStatus", params);
		if (num1 != num2) {
			throw new RuntimeException("修改模板 : 原始模板和临时模板数据没有同步更新");
		}
		if (num1 > 0) {
			result.put("result", "success");
			result.put("msg", "操作成功");
		} else {
			result.put("result", "fail");
			result.put("msg", "操作失败");
		}
		return num1;
	}

	@Override
	@Async
	public void auditNotice(String clientId, com.ucpaas.sms.entity.Template template) {
		Map<String, Object> request = new HashMap<String, Object>();
		request.put("event", 1);
		request.put("msg", "审核结果通知");

		Map<String, String> paraMap = new HashMap<String, String>();
		paraMap.put("clientId", clientId);

		String noticeUrl = masterDao.getOneInfo("smsTemplate.queryNoticeUrl", paraMap);
		if (StringUtils.isBlank(noticeUrl)) {
			return;
		}
		request.put("data", template);
		try {
			int times = 10; //10次
			int period = 30; //30秒
			try{
				times = Integer.valueOf(ConfigUtils.template_authorize_time);
			}catch (Exception e) {
				 times = 10; //10次
			}
			try{
				period = Integer.valueOf(ConfigUtils.template_authorize_period);
			}catch (Exception e) {
				period = 30; //30秒
			}
			for (int i = 0; i < times; i++) {
//				logger.info("Begin pushing notice, noticeUrl:{}, times:{}", noticeUrl, i + 1);
				String body = JsonUtils.toJson(request);
				logger.info("模板审核推送通知(第"+(i+1)+"次),url="+noticeUrl+",body="+body);
				// 响应超时判断
				String response = "";
				if (noticeUrl.startsWith("https")) {
					response = HttpUtils.doJsonPost(noticeUrl,body, true);
				} else {
					response = HttpUtils.doJsonPost(noticeUrl,body, false);
				}
				try{
					logger.info("模板审核推送通知(第"+(i+1)+"次),response="+JsonUtils.toJson(response));
				}catch (Exception e) {
				}
				
				if ("error".equals(response)) {
					// httpPost() 异常, 可能连接超时或者响应超时
					logger.info("模板审核推送通知(第"+(i+1)+"次),请求失败,等待"+period+"秒后再请求");
					Thread.sleep(period*1000);
					continue;
				}

				try {
					CallbackResponse resp = JsonUtils.toObject(response, CallbackResponse.class);
					if (null != resp && "0".equals(resp.getCode())) {
						break;
					}else{
						logger.info("模板审核推送通知(第"+(i+1)+"次),请求失败,等待"+period+"秒后再请求");
						Thread.sleep(period*1000);
					}
				} catch (Exception e) {
					// unexpected response format.
					logger.error("unexcepted response format, result:{}", response);
					logger.info("模板审核推送通知(第"+(i+1)+"次),请求失败,等待"+period+"秒后再请求");
					Thread.sleep(period*1000);
					continue;
				} 
			}
		} catch (Exception e) {
			logger.error("模板审核推送通知失败", e);
			return;
		}
	}

}
