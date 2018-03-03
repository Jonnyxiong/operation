package com.ucpaas.sms.constant;

/**
 * 预警常量
 * 
 * @author xiejiaan
 */
public class RechargeSMSConstant {

	/**
	 * 销售充值成功短信提醒模板
	 */
	public static final String client_sms_recharge_template = "【云之讯】您好，{销售名称}。你的客户{代理商ID-代理商名称}，已完成充值，当前可用额度为{可用额度}，如若不足请提出申请。";


	/**
	 * 短信类型
	 */
	public enum SmsType {
		NOTIFY("0", "通知短信"),
		VERIFICATION_CODE("4", "验证码短信"),
		MARKETING("5", "营销短信"),
		USSD("7", "USSD短信"),
		FLASH("8", "闪信短信");

		private String value;
		private String desc;

		private SmsType(String value, String desc) {
			this.value = value;
			this.desc = desc;
		}

		public String getValue() {
			return value;
		}

		public String getDesc() {
			return desc;
		}

		@Override
		public String toString(){
			return this.value;
		}
	}
}
