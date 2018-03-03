package com.ucpaas.sms.enums;

public enum AgentType {
	/*
	 * 代理商类型
	 * 1:销售代理商,2:品牌代理商,3:资源合作商,4:代理商和资源合作,5:OEM代理商 配置tb_sms_params.param_type=agent_type',
	 */
	销售代理商("销售代理商", 1), 品牌代理商("品牌代理商", 2), 资源合作商("资源合作商", 3), 代理商和资源合作("代理商和资源合作", 4),OEM代理商("OEM代理商", 5);

	// 成员变量
	private String name;
	private Integer value;

	AgentType(String name, Integer value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Integer getValue() {
		return value;
	}

	public static String getDescByValue(Integer value) {

		if(value == null){ return null;}
		String result = null;
		for (AgentType s : AgentType.values()) {
			if (value == s.getValue()) {
				result = s.getName();
				break;
			}
		}
		return result;
	}

	@Override
	public String toString() {
		return name;
	}
}
