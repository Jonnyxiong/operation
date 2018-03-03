package com.ucpaas.sms.service;

import com.alibaba.fastjson.JSON;
import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.entity.JsmsEmailAlarmSetting;
import com.jsmsframework.common.entity.JsmsSendEmailList;
import com.jsmsframework.common.enums.EmailType;
import com.jsmsframework.common.enums.StatusEnum;
import com.jsmsframework.common.enums.WebIdNew;
import com.jsmsframework.common.service.JsmsEmailAlarmSettingService;
import com.jsmsframework.common.service.JsmsSendEmailListService;
import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.entity.message.Mailprop;
import com.ucpaas.sms.util.ConfigUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.internet.MimeMessage;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Add by lpjLiu 20171206
 */
@Service
public class TaskAlarmSettingServiceImpl implements TaskAlarmSettingService {

	private static Logger logger = LoggerFactory.getLogger(TaskAlarmSettingService.class);

	@Autowired
	private JsmsEmailAlarmSettingService jsmsEmailAlarmSettingService;

	@Autowired
	private JsmsSendEmailListService jsmsSendEmailListService;

	@Autowired
	private MailpropService mailpropService;

	private Boolean alarmEmailIsError(String alarmEmail) {
		if (StringUtils.isBlank(alarmEmail)) {
			return true;
		}

		String[] arr = alarmEmail.split(",");
		if (arr == null) {
			return true;
		}

		Boolean isError = false;
		if (arr.length > 1) {
			for (String s : arr) {
				if (!isEmail(arr[0])) {
					isError = true;
					break;
				}
			}
		} else {
			if (!isEmail(arr[0])) {
				isError = true;
			}
		}

		return isError;
	}

	private Boolean serverIsError(String server) {
		if (StringUtils.isBlank(server)) {
			return true;
		}

		String[] arr = server.split(":");
		if (arr == null || arr.length != 2) {
			return true;
		}

		Integer port = null;
		try {
			port = Integer.parseInt(arr[1]);
		} catch (Exception e) {
			logger.error("邮箱服务器的端口错误：{}", e);
		}

		if (port == null) {
			return true;
		}

		if (port > 65535) {
			return true;
		}

		return false;
	}

	private static final String REGEX_EMAIL = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";

	/**
	 * 校验邮箱
	 *
	 * @param email
	 * @return 校验通过返回true，否则返回false
	 */
	private boolean isEmail(String email) {
		return Pattern.matches(REGEX_EMAIL, email);
	}

	@Override
	public R sendEmailListInfo(Long id) {
		if (id == null) {
			return R.error("发送邮箱服务器id不能为空");
		}

		JsmsSendEmailList sendEmailList = jsmsSendEmailListService.getById(id.intValue());
		if (sendEmailList == null) {
			return R.error("发送邮箱服务器不存在");
		}

		return R.ok("获取发送邮箱服务器详情成功", sendEmailList);
	}

	@Override
	public R findSendEmailList() {
		JsmsPage<JsmsSendEmailList> page = new JsmsPage();
		page.setPage(0);
		page.setRows(-1);
		page.setParams(new HashMap<String, Object>());
		JsmsPage<JsmsSendEmailList> page1 = jsmsSendEmailListService.queryList(page);
		return R.ok("获取发送邮箱服务器列表成功", page1.getData());
	}

	private R checkSendEmail(JsmsSendEmailList sendEmailList) {
		if (sendEmailList == null) {
			return R.error("发送邮箱服务器不能为空");
		}

		if (StringUtils.isBlank(sendEmailList.getAlarmName()) || sendEmailList.getAlarmName().length() > 50) {
			return R.error("发送邮箱服务器用途长度必须是1至50");
		}

		if (sendEmailList.getMailboxType() == null
				|| EmailType.getInstanceByValue(sendEmailList.getMailboxType()) == null) {
			return R.error("发送邮箱服务器的邮箱类型不正确");
		}

		if (StringUtils.isBlank(sendEmailList.getIncomingMailServer())
				|| sendEmailList.getIncomingMailServer().length() > 100
				|| serverIsError(sendEmailList.getIncomingMailServer())) {
			return R.error("发送邮箱服务器的收件服务器配置不正确");
		}

		if (StringUtils.isBlank(sendEmailList.getOutgoingMailServer())
				|| sendEmailList.getOutgoingMailServer().length() > 100
				|| serverIsError(sendEmailList.getOutgoingMailServer())) {
			return R.error("发送邮箱服务器的发件服务器配置不正确");
		}

		if (StringUtils.isBlank(sendEmailList.getEmailAddress()) || sendEmailList.getEmailAddress().length() > 100
				|| !isEmail(sendEmailList.getEmailAddress())) {
			return R.error("发送邮箱服务器的邮箱地址配置不正确");
		}

		if (StringUtils.isBlank(sendEmailList.getEmailUsername()) || sendEmailList.getEmailUsername().length() > 50) {
			return R.error("发送邮箱服务器的用户名长度必须是1至50");
		}

		if (StringUtils.isBlank(sendEmailList.getEmailPassword()) || sendEmailList.getEmailPassword().length() > 50) {
			return R.error("发送邮箱服务器的密码长度必须是1至50");
		}

		if (sendEmailList.getWebId() == null || WebIdNew.getInstanceByValue(sendEmailList.getWebId()) == null) {
			return R.error("发送邮箱服务器的应用系统配置不正确");
		}

		return null;
	}

	@Override
	@Transactional("message")
	public R addSendEmailList(JsmsSendEmailList sendEmailList) {
		R r = checkSendEmail(sendEmailList);
		if (r != null) {
			return r;
		}

		logger.debug("添加发送邮箱服务器 {}", JSON.toJSONString(sendEmailList));

		// 检测是否已经添加过相同类型名称，相同平台的邮箱配置
		Map<String, Object> params = new HashMap<>();
		params.put("alarmName", sendEmailList.getAlarmName());
		params.put("webId", sendEmailList.getWebId());
		int count = jsmsSendEmailListService.count(params);
		if (count > 0) {
			logger.debug("已存在相同用途、相同应用系统的发送邮箱服务器");
			return R.error("已存在相同用途、相同应用系统的发送邮箱服务器");
		}

		sendEmailList.setStatus(StatusEnum.STATUS_ENABLE.getValue());

		Date date = Calendar.getInstance().getTime();
		sendEmailList.setCreateTime(date);
		sendEmailList.setUpdateTime(date);
		int addCount = jsmsSendEmailListService.insert(sendEmailList);
		return addCount > 0 ? R.ok("添加发送邮箱服务器成功") : R.error("添加发送邮箱服务器失败");
	}

	@Override
	@Transactional("message")
	public R editSendEmailList(JsmsSendEmailList sendEmailList) {
		R r = checkSendEmail(sendEmailList);
		if (r != null) {
			return r;
		}

		if (sendEmailList.getId() == null) {
			return R.error("发送邮箱服务器的ID不能为空");
		}

		logger.debug("编辑发送邮箱服务器 {}", JSON.toJSONString(sendEmailList));

		// 检测 除了本身，其它已存在的邮箱服务器配置是否存在相同的
		Map<String, Object> params = new HashMap<>();
		params.put("id", sendEmailList.getId());
		params.put("alarmName", sendEmailList.getAlarmName());
		params.put("webId", sendEmailList.getWebId());
		int count = jsmsSendEmailListService.countForEdit(params);
		if (count > 0) {
			logger.debug("已存在相同用途、相同应用系统的发送邮箱服务器");
			return R.error("已存在相同用途、相同应用系统的发送邮箱服务器");
		}

		Date date = Calendar.getInstance().getTime();
		sendEmailList.setUpdateTime(date);

		int modCount = jsmsSendEmailListService.updateSelective(sendEmailList);
		return modCount > 0 ? R.ok("编辑发送邮箱服务器成功") : R.error("编辑发送邮箱服务器失败");
	}

	@Override
	@Transactional("message")
	public R disableSendEmailList(Long id) {
		if (id == null) {
			return R.error("发送邮箱服务器ID不能为空");
		}

		JsmsSendEmailList sendEmailList = jsmsSendEmailListService.getById(id.intValue());
		if (sendEmailList == null) {
			return R.error("发送邮箱服务器不存在");
		}

		if (sendEmailList.getStatus().intValue() == StatusEnum.STATUS_DISABLE.getValue().intValue()) {
			return R.ok("发送邮箱服务器禁用成功");
		}

		JsmsSendEmailList newSendEmailList = new JsmsSendEmailList();
		newSendEmailList.setId(id.intValue());
		newSendEmailList.setStatus(StatusEnum.STATUS_DISABLE.getValue());

		logger.debug("发送邮箱服务器禁用: {}", JSON.toJSONString(newSendEmailList));
		int count = jsmsSendEmailListService.updateSelective(newSendEmailList);
		return count == 0 ? R.error("发送邮箱服务器禁用失败") : R.ok("发送邮箱服务器禁用成功");
	}

	@Override
	@Transactional("message")
	public R enableSendEmailList(Long id) {
		if (id == null) {
			return R.error("发送邮箱服务器ID不能为空");
		}

		JsmsSendEmailList sendEmailList = jsmsSendEmailListService.getById(id.intValue());
		if (sendEmailList == null) {
			return R.error("发送邮箱服务器不存在");
		}

		if (sendEmailList.getStatus().intValue() == StatusEnum.STATUS_ENABLE.getValue().intValue()) {
			return R.ok("发送邮箱服务器启用成功");
		}

		JsmsSendEmailList newSendEmailList = new JsmsSendEmailList();
		newSendEmailList.setId(id.intValue());
		newSendEmailList.setStatus(StatusEnum.STATUS_ENABLE.getValue());

		logger.debug("发送邮箱服务器启用: {}", JSON.toJSONString(newSendEmailList));
		int count = jsmsSendEmailListService.updateSelective(newSendEmailList);
		return count == 0 ? R.error("发送邮箱服务器启用失败") : R.ok("发送邮箱服务器启用成功");
	}

	@Override
	public R testSendEmailList(JsmsSendEmailList sendEmailList) {
		R r = checkSendEmail(sendEmailList);
		if (r != null) {
			return r;
		}

		String[] server = sendEmailList.getOutgoingMailServer().split(":");

		JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
		javaMailSender.setHost(server[0]);
		javaMailSender.setPort(Integer.parseInt(server[1]));
		javaMailSender.setUsername(sendEmailList.getEmailUsername());
		javaMailSender.setPassword(sendEmailList.getEmailPassword());

		Properties properties = new Properties();
		properties.setProperty("mail.smtp.auth", "true");
		properties.setProperty("mail.smtp.timeout", "25000");
		properties.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		javaMailSender.setJavaMailProperties(properties);

		Mailprop mailprop = null;
		mailprop = mailpropService.getById(100017l); // 品牌代理商邮件
		String vUrl = ConfigUtils.agent_site_url;// 获取代理商服务器站点地址

		String body = mailprop.getText();
		body = body.replace("vUrl", vUrl);
		body = body.replace("vemail", sendEmailList.getEmailAddress());
		body = body.replace("password", "xxxx");

		try {
			MimeMessage msg = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(msg, false, "utf-8");
			helper.setFrom(sendEmailList.getEmailUsername());
			helper.setTo(sendEmailList.getEmailAddress().split(","));
			helper.setSubject(mailprop.getSubject());
			helper.setText(body, true);
			javaMailSender.send(msg);

			r = R.ok("测试发送邮件成功");
		} catch (Throwable e) {
			logger.error("测试发送邮件失败 {}", e);
			r = R.error("测试发送邮件失败");
		}
		return r;
	}

	private R checkEmailAlarmSetting(JsmsEmailAlarmSetting emailAlarmSetting) {
		if (emailAlarmSetting == null) {
			return R.error("邮件提醒设置不能为空");
		}

		if (StringUtils.isBlank(emailAlarmSetting.getAlarmName()) || emailAlarmSetting.getAlarmName().length() > 50) {
			return R.error("邮件提醒设置用途长度必须是1至50");
		}

		if (StringUtils.isBlank(emailAlarmSetting.getAlarmEmail()) || emailAlarmSetting.getAlarmEmail().length() > 2000
				|| alarmEmailIsError(emailAlarmSetting.getAlarmEmail())) {
			return R.error("邮件提醒设置的提醒邮箱配置不正确");
		}

		if (emailAlarmSetting.getWebId() == null || WebIdNew.getInstanceByValue(emailAlarmSetting.getWebId()) == null) {
			return R.error("邮件提醒设置的应用系统配置不正确");
		}

		return null;
	}

	@Override
	public R emailAlarmSettingInfo(Long id) {
		if (id == null) {
			return R.error("邮件提醒设置ID不能为空");
		}

		JsmsEmailAlarmSetting emailAlarmSetting = jsmsEmailAlarmSettingService.getById(id.intValue());
		if (emailAlarmSetting == null) {
			return R.error("邮件提醒设置不存在");
		}

		return R.ok("获取邮件提醒设置详情成功", emailAlarmSetting);
	}

	@Override
	public R findEmailAlarmSettingList() {
		JsmsPage<JsmsEmailAlarmSetting> page = new JsmsPage();
		page.setPage(0);
		page.setRows(-1);
		page.setParams(new HashMap<String, Object>());
		JsmsPage<JsmsEmailAlarmSetting> page1 = jsmsEmailAlarmSettingService.queryList(page);
		return R.ok("获取邮件提醒设置列表成功", page1.getData());
	}

	@Override
	@Transactional("message")
	public R addEmailAlarmSetting(JsmsEmailAlarmSetting emailAlarmSetting) {
		R r = checkEmailAlarmSetting(emailAlarmSetting);
		if (r != null) {
			return r;
		}

		logger.debug("添加邮件提醒设置 {}", JSON.toJSONString(emailAlarmSetting));

		// 检测是否已经添加过相同类型名称，相同平台的邮箱配置
		Map<String, Object> params = new HashMap<>();
		params.put("alarmName", emailAlarmSetting.getAlarmName());
		params.put("webId", emailAlarmSetting.getWebId());
		int count = jsmsEmailAlarmSettingService.count(params);
		if (count > 0) {
			logger.debug("已存在相同用途、相同应用系统的邮件提醒设置");
			return R.error("已存在相同用途、相同应用系统的邮件提醒设置");
		}

		emailAlarmSetting.setStatus(StatusEnum.STATUS_ENABLE.getValue());

		Date date = Calendar.getInstance().getTime();
		emailAlarmSetting.setCreateTime(date);
		emailAlarmSetting.setUpdateTime(date);
		int addCount = jsmsEmailAlarmSettingService.insert(emailAlarmSetting);
		return addCount > 0 ? R.ok("添加邮件提醒设置成功") : R.error("添加邮件提醒设置失败");
	}

	@Override
	@Transactional("message")
	public R editEmailAlarmSetting(JsmsEmailAlarmSetting emailAlarmSetting) {
		R r = checkEmailAlarmSetting(emailAlarmSetting);
		if (r != null) {
			return r;
		}

		if (emailAlarmSetting.getId() == null) {
			return R.error("邮件提醒设置的ID不能为空");
		}

		logger.debug("编辑邮件提醒设置 {}", JSON.toJSONString(emailAlarmSetting));

		// 检测 除了本身，其它已存在的邮箱服务器配置是否存在相同的
		Map<String, Object> params = new HashMap<>();
		params.put("id", emailAlarmSetting.getId());
		params.put("alarmName", emailAlarmSetting.getAlarmName());
		params.put("webId", emailAlarmSetting.getWebId());
		int count = jsmsEmailAlarmSettingService.countForEdit(params);
		if (count > 0) {
			logger.debug("已存在相同用途、相同应用系统的邮件提醒设置");
			return R.error("已存在相同用途、相同应用系统的邮件提醒设置");
		}

		Date date = Calendar.getInstance().getTime();
		emailAlarmSetting.setUpdateTime(date);

		int modCount = jsmsEmailAlarmSettingService.updateSelective(emailAlarmSetting);
		return modCount > 0 ? R.ok("编辑邮件提醒设置成功") : R.error("编辑邮件提醒设置失败");
	}

	@Override
	@Transactional("message")
	public R disableEmailAlarmSetting(Long id) {
		if (id == null) {
			return R.error("邮件提醒设置ID不能为空");
		}

		JsmsEmailAlarmSetting emailAlarmSetting = jsmsEmailAlarmSettingService.getById(id.intValue());
		if (emailAlarmSetting == null) {
			return R.error("邮件提醒设置不存在");
		}

		if (emailAlarmSetting.getStatus().intValue() == StatusEnum.STATUS_DISABLE.getValue().intValue()) {
			return R.ok("邮件提醒设置禁用成功");
		}

		JsmsEmailAlarmSetting update = new JsmsEmailAlarmSetting();
		update.setId(id.intValue());
		update.setStatus(StatusEnum.STATUS_DISABLE.getValue());

		logger.debug("邮件提醒设置禁用: {}", JSON.toJSONString(update));
		int count = jsmsEmailAlarmSettingService.updateSelective(update);
		return count == 0 ? R.error("邮件提醒设置禁用失败") : R.ok("邮件提醒设置禁用成功");
	}

	@Override
	@Transactional("message")
	public R enableEmailAlarmSetting(Long id) {
		if (id == null) {
			return R.error("邮件提醒设置ID不能为空");
		}

		JsmsEmailAlarmSetting emailAlarmSetting = jsmsEmailAlarmSettingService.getById(id.intValue());
		if (emailAlarmSetting == null) {
			return R.error("邮件提醒设置不存在");
		}

		if (emailAlarmSetting.getStatus().intValue() == StatusEnum.STATUS_ENABLE.getValue().intValue()) {
			return R.ok("邮件提醒设置启用成功");
		}

		JsmsEmailAlarmSetting update = new JsmsEmailAlarmSetting();
		update.setId(id.intValue());
		update.setStatus(StatusEnum.STATUS_ENABLE.getValue());

		logger.debug("邮件提醒设置启用: {}", JSON.toJSONString(update));
		int count = jsmsEmailAlarmSettingService.updateSelective(update);
		return count == 0 ? R.error("邮件提醒设置启用失败") : R.ok("邮件提醒设置启用成功");
	}
}
