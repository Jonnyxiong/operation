package com.ucpaas.sms.enums;

public enum OrderStatusEnum {
	/*
	 * 订单状态
	 */
	待配单("待配单", 0), 待匹配("待匹配", 1), 待审批("待审批（二级审核）", 2), 退单("退单", 3), 撤单("撤单", 4), 匹配成功("匹配成功", 5), 寻资源("寻资源",
			6), 待审核_销售总监("待审核（销售总监）", 7), 待审核_订单运营("待审核（订单运营）", 8);

	// 成员变量
	private String name;
	private Integer value;

	OrderStatusEnum(String name, Integer value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public Integer getValue() {
		return value;
	}

	@Override
	public String toString() {
		return name;
	}

}
