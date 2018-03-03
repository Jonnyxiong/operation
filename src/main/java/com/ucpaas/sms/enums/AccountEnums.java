package com.ucpaas.sms.enums;

public class AccountEnums {

	public enum Status {
		未激活(0, "未激活"), 已启用(1, "已启用"), 已冻结(5, "已冻结"), 已注销(6, "已注销"), 已锁定(7, "已锁定");
		private Integer value;
		private String desc;

		Status(Integer value, String desc) {
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
			if (value == null) {
				return null;
			}
			String result = null;
			for (Status s : Status.values()) {
				if (value == s.getValue()) {
					result = s.getDesc();
					break;
				}
			}
			return result;
		}

		public static Status getInstanceByValue(Integer value) {
			for (Status status : Status.values()) {
				if (status.getValue().equals(value)) {
					return status;
				}
			}
			return null;
		}
	}
}
