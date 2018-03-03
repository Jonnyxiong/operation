package com.ucpaas.sms.enums;

/**
 * Created by xiaoqingwen on 2018/01/18. 通道运营的排序
 */
public enum ChannelOperationEnum {
	//按低消完成率由高到低-->1
	低消完成率由高到低(1,"按低消完成率由高到低"),
	//按低消完成率由低到高-->2
	低消完成率由低到高(2,"按低消完成率由低到高"),
	//按投诉率由高到低-->3
	投诉率由高到低(3,"按投诉率由高到低"),
	//按投诉率由低到高-->4
	投诉率由低到高(4,"按投诉率由低到高"),
	//按投诉差异值由高到低-->5
	投诉差异值由高到低(5,"按投诉差异值由高到低"),
	//按投诉差异值由低到高-->6
	投诉差异值由低到高(6,"按投诉差异值由低到高");
	private Integer value;
	private String desc;
	ChannelOperationEnum(Integer value, String desc) {
		this.value = value;
		this.desc = desc;
	}

	public Integer getValue() {
		return value;
	}
	public String getDesc() {
		return desc;
	}

}
