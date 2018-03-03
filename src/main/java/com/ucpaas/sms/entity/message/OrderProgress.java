package com.ucpaas.sms.entity.message;

import org.apache.ibatis.type.Alias;

import java.util.Date;

@Alias("OrderProgress")
public class OrderProgress {

	// 序号，自增长
	private Integer id;
	// 订单号，关联t_sms_order_list表中demand_id字段

	private String orderId;
	// 进度描述
	private String orderProgress;

	// 订单状态，0：待配单，1：待匹配，2：待审批（二级审核），3：退单，4：撤单，5：匹配成功，6：寻资源，7：待审核（销售总监），8：待审核（订单运营）
	private Integer state;

	// 备注
	private String remark;

	// 操作者ID，关联t_sms_user表中id字段
	private Long operatorId;

	// 此进度是否由审核人操作，0：否，1：是
	private Integer isAudit;

	// 页面是否显示进度，0：否，1：是
	private Integer isShow;

	// 创建时间
	private Date createTime;

	public Integer getIsShow() {
		return isShow;
	}

	public void setIsShow(Integer isShow) {
		this.isShow = isShow;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getOrderProgress() {
		return orderProgress;
	}

	public void setOrderProgress(String orderProgress) {
		this.orderProgress = orderProgress;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Long getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(Long operatorId) {
		this.operatorId = operatorId;
	}

	public Integer getIsAudit() {
		return isAudit;
	}

	public void setIsAudit(Integer isAudit) {
		this.isAudit = isAudit;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

}