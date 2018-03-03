package com.ucpaas.sms.constant;

/**
 * 常量类
 */
public class Constant {

	public static final String LOGIN_USER_ID = "LOGIN_USER_ID";
	public static final String LOGIN_USER_NAME = "LOGIN_USER_NAME";
	public static final String LOGIN_USER_INFO = "LOGIN_USER_INFO";
	// 短信发送最大手机号码数
	public static final int SMS_SEND_MAX_NUM = 100;
	public static final String sms_experience_template = "【云之讯】亲爱的用户，您的短信验证码为[%s]";
	public static final String SMS_VERIFY_CODE = "SMS_VERIFY_CODE";

	// 订单前缀
	public static String ORDERID_PRE = "";

	// 同一分钟内的第几笔订单
	public static int ORDER_NUM = 1;

	/** 通知重试次数 MAX_RETRY_TIMES=3 */
	public static int MAX_RETRY_TIMES = 3;

	public static String CHARGE_RULE = "charge_rule";

	/**
	 * 判断是否赠送短信的属性key
	 */
	public static String BESTOW_SMS = "bestow_sms";

	/**
	 * 判断是否赠送短信的属性value(1为已赠送)
	 */
	public static String BESTOW_SMS_VALUE = "1";
}
