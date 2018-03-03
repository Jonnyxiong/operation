package com.ucpaas.sms.enums;

/**
 * Created by lpjLiu on 2018/01/11. 账户运营的排序
 */
public class ClientOperation {

	public enum OrderBy {
		投诉率由高到低(1, "complaint_ratio desc"),
		投诉率由低到高(2, "complaint_ratio asc"),
		明确成功条数由高到低(3, "reportsuccess desc"),
		明确成功条数低到高(4, "reportsuccess asc");

		private Integer value;
		private String desc;

		OrderBy(Integer value, String desc) {
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
			for (OrderBy s : OrderBy.values()) {
				if (value == s.getValue()) {
					result = s.getDesc();
					break;
				}
			}
			return result;
		}

		public static OrderBy getEnumByValue(Integer value) {
			if(value == null){ return null;}

			OrderBy e = null;
			for (OrderBy result : OrderBy.values()) {
				if (value == result.getValue()) {
					e = result;
					break;
				}
			}
			return e;
		}
	}


}
