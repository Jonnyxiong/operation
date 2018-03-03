package com.ucpaas.sms.service.email;

import javax.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Email业务
 * 
 * @author xiejiaan
 */
@Service
public class EmailServiceImpl implements EmailService {
	private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);
	@Autowired
	private JavaMailSender javaMailSender;
	/**
	 * 发送者
	 */
	private static final String from = "admin@ucpaas.com";

	@Override
	public boolean sendTextEmail(String to, String subject, String body) {
		logger.debug("发送文本格式的Email【开始】：to={}, subject={}, body={}", to, subject, body);
		try {
			SimpleMailMessage msg = new SimpleMailMessage();
			msg.setFrom(from);
			msg.setTo(to.split(","));
			msg.setSubject(subject);
			msg.setText(body);
			javaMailSender.send(msg);

			logger.debug("发送文本格式的Email【成功】：to={}, subject={}, body={}", to, subject, body);
			return true;
		} catch (Throwable e) {
			logger.error("发送文本格式的Email【失败】：to=" + to + ", subject=" + subject + ", body=" + body, e);
		}
		return false;
	}

	@Override
	public boolean sendHtmlEmail(String to, String subject, String body) {
		logger.debug("发送html格式的Email【开始】：to={}, subject={}, body={}", to, subject, body);
		try {
			MimeMessage msg = javaMailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(msg, false, "utf-8");
			helper.setFrom(from);
			helper.setTo(to.split(","));
			helper.setSubject(subject);
			helper.setText(body, true);
			javaMailSender.send(msg);

			logger.debug("发送html格式的Email【成功】：to={}, subject={}, body={}", to, subject, body);
			return true;
		} catch (Throwable e) {
			logger.error("发送html格式的Email【失败】：to=" + to + ", subject=" + subject + ", body=" + body, e);
		}
		return false;
	}

}
