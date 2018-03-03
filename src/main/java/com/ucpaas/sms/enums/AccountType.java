package com.ucpaas.sms.enums;

/**
 * Created by lpjLiu on 2017/7/10. 短信类型
 */
public enum AccountType {
	代理商子客户(0, "代理商子客户"),直客(1, "直客");

	private Integer value;
	private String desc;

	private AccountType(Integer value, String desc) {
		this.value = value;
		this.desc = desc;
	}

	public Integer getValue() {
		return value;
	}

	public String getDesc() {
		return desc;
	}

	public static String getDescByValue(Integer value) {
		if(value == null){ return null;}

		String result = null;
		for (AccountType s : AccountType.values()) {
			if (value == s.getValue()) {
				result = s.getDesc();
				break;
			}
		}
		return result;
	}
}
