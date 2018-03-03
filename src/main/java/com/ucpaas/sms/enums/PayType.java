package com.ucpaas.sms.enums;

/**
 * 短信报表中付费类型
 */
public enum PayType {
	/**
	 * 预付费
	 */
	PREPAY(0,"预付费"),
	/**
	 * 后付费
	 */
	POSTPAY(1,"后付费"),
	/**
	 * 日合计数据的paytype
	 */
	DAILY(-1,"日合计数据"),
	/**
	 * 月合计数据的paytype
	 */
	MONTYLY(-1,"月合计数据");
	
	Integer value;
	String desc;

	
	private PayType(Integer value, String desc) {

		this.value = value;
		this.desc= desc;
	}

	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public String getDesc() {
		return desc;
	}

	public static String getDescByValue(Integer value) {
		if(value == null){ return null;}

		String result = null;
		for (PayType p : PayType.values()) {
			if (value == p.getValue()) {
				result = p.getDesc();
				break;
			}
		}
		return result;
	}
}
