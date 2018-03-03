package com.ucpaas.sms.entity.po;

import com.jsmsframework.access.access.entity.JsmsClientOperationStatistics;
import com.jsmsframework.common.enums.OperatorType;
import com.jsmsframework.common.enums.SmsTypeEnum;
import com.jsmsframework.user.entity.JsmsAccount;
import com.jsmsframework.user.entity.JsmsUser;

public class JsmsClientOperationStatisticsPo extends JsmsClientOperationStatistics {
	private JsmsAccount client;
	private JsmsUser user;

	//统计时间类型转成String
	private String beginTime;
	//统计时间类型转成String
	private String endTime;

	private String orderByValue;
	private String orderBy;
	private Integer rows;
	private Integer page;
	//统计时间,字符串格式
	private String dateStr;
	// 发送成功率,字符串格式
	private String sendSuccessRatioStr;
	// 投诉率,字符串格式
	private String complaintRatioStr;
	// 投诉系数,字符串格式
	private String complaintCoefficientStr;
	// 投诉差异值,字符串格式
	private String complaintDifferenceStr;
	// 销售单价,字符串格式
	private String salefeeStr;

	public String getSalefeeStr() {
		return salefeeStr;
	}

	public void setSalefeeStr(String salefeeStr) {
		this.salefeeStr = salefeeStr;
	}

	public String getSendSuccessRatioStr() {
		return sendSuccessRatioStr;
	}

	public void setSendSuccessRatioStr(String sendSuccessRatioStr) {
		this.sendSuccessRatioStr = sendSuccessRatioStr;
	}

	public String getComplaintRatioStr() {
		return complaintRatioStr;
	}

	public void setComplaintRatioStr(String complaintRatioStr) {
		this.complaintRatioStr = complaintRatioStr;
	}

	public String getComplaintCoefficientStr() {
		return complaintCoefficientStr;
	}

	public void setComplaintCoefficientStr(String complaintCoefficientStr) {
		this.complaintCoefficientStr = complaintCoefficientStr;
	}

	public String getComplaintDifferenceStr() {
		return complaintDifferenceStr;
	}

	public void setComplaintDifferenceStr(String complaintDifferenceStr) {
		this.complaintDifferenceStr = complaintDifferenceStr;
	}

	public String getDateStr() {
		return dateStr;
	}

	public void setDateStr(String dateStr) {
		this.dateStr = dateStr;
	}

	public String getSmstypeDesc() {
		return this.getSmstype() == null ? "" : SmsTypeEnum.getDescByValue(this.getSmstype());
	}


	public String getOperatorstypeDesc() {
		return this.getOperatorstype() == null ? "" : OperatorType.getDescByValue(this.getOperatorstype());
	}


	public JsmsAccount getClient() {
		return client;
	}

	public void setClient(JsmsAccount client) {
		this.client = client;
	}

	public JsmsUser getUser() {
		return user;
	}

	public void setUser(JsmsUser user) {
		this.user = user;
	}

	public String getOrderBy() {
		return orderBy;
	}

	public void setOrderBy(String orderBy) {
		this.orderBy = orderBy;
	}

	public Integer getRows() {
		return rows;
	}

	public void setRows(Integer rows) {
		this.rows = rows;
	}

	public Integer getPage() {
		return page;
	}

	public void setPage(Integer page) {
		this.page = page;
	}

	public String getOrderByValue() {
		return orderByValue;
	}

	public String getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(String beginTime) {
		this.beginTime = beginTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public void setOrderByValue(String orderByValue) {
		this.orderByValue = orderByValue;
	}
}