package com.ucpaas.sms.enums;

/**
 * Created by lpjLiu on 2017/7/10. 短信类型
 */
public enum DataAuthority {
	仅看自己数据(0, "仅看自己数据"), 所在部门及下级部门(1, "所在部门及下级部门");

	private Integer value;
	private String desc;

	DataAuthority(Integer value, String desc) {
		this.value = value;
		this.desc = desc;
	}

	public Integer getValue() {
		return value;
	}

	public String getDesc() {
		return desc;
	}

	public static String getDescByValue(int value) {
		String result = null;
		for (DataAuthority s : DataAuthority.values()) {
			if (value == s.getValue()) {
				result = s.getDesc();
				break;
			}
		}
		return result;
	}
}
