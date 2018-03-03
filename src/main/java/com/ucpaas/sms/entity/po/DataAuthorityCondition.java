package com.ucpaas.sms.entity.po;

import java.util.Date;
import java.util.List;

/**
 * Created by lpjLiu on 2017/7/28.
 */
public class DataAuthorityCondition {

	// 需要查询归属销售或归属商务为空的数据
	private Boolean needQuerySaleIsNullData;

	// 用户Id列表
	private List<Long> ids;
	//查询用的开始时间
	private Date startTime;
	//查询用的结束时间
	private Date endTime;

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public DataAuthorityCondition() {
		this.needQuerySaleIsNullData = false;
	}

	public DataAuthorityCondition(List<Long> ids) {
		this.needQuerySaleIsNullData = false;
		this.ids = ids;
	}

	public DataAuthorityCondition(Boolean needQuerySaleIsNullData, List<Long> ids) {
		this.needQuerySaleIsNullData = needQuerySaleIsNullData;
		this.ids = ids;
	}

	public Boolean isNeedQuerySaleIsNullData() {
		return needQuerySaleIsNullData;
	}

	public void setNeedQuerySaleIsNullData(Boolean needQuerySaleIsNullData) {
		this.needQuerySaleIsNullData = needQuerySaleIsNullData;
	}

	public List<Long> getIds() {
		return ids;
	}

	public void setIds(List<Long> ids) {
		this.ids = ids;
	}
}
