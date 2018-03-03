package com.ucpaas.sms.entity.message;

import java.util.Date;

import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ucpaas.sms.common.entity.BaseEntity;
import com.ucpaas.sms.validate.number.CanNullNumberRange;
import com.ucpaas.sms.validate.number.NotNullNumberRange;

/**
 * Created by lpjLiu on 2017/6/20.
 */
public class Resource extends BaseEntity {
	private static final long serialVersionUID = 1L;

	private String resourceId; // '资源编号，规则：T+4位序号+YYYYMMDD，T表示通道，序号范围[0000，9999]，同一天按顺序往下递增，YYYYMMDD表示年月日，如：T000020170614',
	private String channelId; // '通道号，关联t_sms_channel表中cid字段',
	private String channelType; // '通道类型，多个类型间以＂,＂分隔，0：三网合一，1：移动，2：电信，3：联通，4：全网',
	private String smsType; // '发送类型',
	private String contentProp; // '发送内容属性',
	private String belongBusiness; // '归属商务，关联t_sms_user表中id字段',
	private String belongBusinessName;
	private String directConnect; // '是否直连，0：直连，1：过平台',
	private String extendSize; // '扩展位，=0：无特殊要求；>0：要求位长',
	private String signType; // '签名类型，多个类型间以＂,＂分隔，0：自定义，1：报备签名，2：固签',
	private String purchasePrice; // '采购价格，单位：元/条',
	private String rate; // '速度（TPS），单位：条/秒',
	private String minimumGuarantee; // '是否保底，单位：万条/月',
	private String isCredit; // '是否授信，0：否，1：是',
	private String payType; // '结算方式',
	private String invoiceType; // '发票类型，0：不要票，1：专票，2：普票',
	private String mtIp; // '接入IP',
	private String protocolType; // '接入协议，0：HTTP1：CMPP，2：SMGP，3：SGIP，4：SMPP',
	private String supplier; // '供应商',
	private String contact; // '联系人',
	private String mobile; // '手机号码',
	private String isAudit; // '是否需要上级审核，0：否，1：是',
	private Date onlineDate; // '待接入时间',
	private String state; // '资源状态，0：待接入，1：已接入，2：待审批，3：撤销',
	private String remark; // '备注',
	private String auditorId; // '审批人ID，关联t_sms_user表中id字段',
	private String operatorId; // '提交人ID，关联t_sms_user表中id字段',
	private Date createTime; // '创建时间',
	private Date updateTime; // '修改时间'

	public Resource() {

	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}

	@CanNullNumberRange(min = 1, max = Integer.MAX_VALUE, message = "通道必须是数字，取值范围为1至2147483647")
	public String getChannelId() {
		return channelId;
	}

	public void setChannelId(String channelId) {
		this.channelId = channelId;
	}

	@NotEmpty(message = "通道类型不能为空")
	@Length(max = 30, message = "通道类型不能超过30个字符")
	public String getChannelType() {
		return channelType;
	}

	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}

	@NotEmpty(message = "发送类型不能为空")
	@Length(max = 50, message = "发送类型不能超过50个字符")
	public String getSmsType() {
		return smsType;
	}

	public void setSmsType(String smsType) {
		this.smsType = smsType;
	}

	@NotEmpty(message = "发送内容属性不能为空")
	@Length(max = 100, message = "发送内容属性不能超过100个字符")
	public String getContentProp() {
		return contentProp;
	}

	public void setContentProp(String contentProp) {
		this.contentProp = contentProp;
	}

	@NotNullNumberRange(min = 1, max = Integer.MAX_VALUE, message = "归属商务不能为空")
	public String getBelongBusiness() {
		return belongBusiness;
	}

	public void setBelongBusiness(String belongBusiness) {
		this.belongBusiness = belongBusiness;
	}

	@NotNullNumberRange(min = 0, max = Integer.MAX_VALUE, message = "是否直连不能为空")
	public String getDirectConnect() {
		return directConnect;
	}

	public void setDirectConnect(String directConnect) {
		this.directConnect = directConnect;
	}

	@NotNullNumberRange(min = 0, max = 12, message = "扩展位不能为空且必须是数字，取值范围为0至12")
	public String getExtendSize() {
		return extendSize;
	}

	public void setExtendSize(String extendSize) {
		this.extendSize = extendSize;
	}

	@NotEmpty(message = "签名类型不能为空")
	@Length(max = 30, message = "签名类型不能超过30个字符")
	public String getSignType() {
		return signType;
	}

	public void setSignType(String signType) {
		this.signType = signType;
	}

	@NotEmpty(message = "采购价格不能为空")
	@Length(max = 50, message = "采购价格不能超过50个字符")
	public String getPurchasePrice() {
		return purchasePrice;
	}

	public void setPurchasePrice(String purchasePrice) {
		this.purchasePrice = purchasePrice;
	}

	@NotNullNumberRange(min = 0, max = 3000, message = "速率不能为空且必须是数字，取值范围为0至3000")
	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	@NotEmpty(message = "是否保底不能为空")
	@Length(max = 60, message = "是否保底不能超过60个字符")
	public String getMinimumGuarantee() {
		return minimumGuarantee;
	}

	public void setMinimumGuarantee(String minimumGuarantee) {
		this.minimumGuarantee = minimumGuarantee;
	}

	@NotNullNumberRange(min = 0, max = Integer.MAX_VALUE, message = "是否授信不能为空")
	public String getIsCredit() {
		return isCredit;
	}

	public void setIsCredit(String isCredit) {
		this.isCredit = isCredit;
	}

	@NotEmpty(message = "结算方式不能为空")
	@Length(max = 50, message = "结算方式不能超过50个字符")
	public String getPayType() {
		return payType;
	}

	public void setPayType(String payType) {
		this.payType = payType;
	}

	@NotNullNumberRange(min = 0, max = Integer.MAX_VALUE, message = "发票类型不能为空")
	public String getInvoiceType() {
		return invoiceType;
	}

	public void setInvoiceType(String invoiceType) {
		this.invoiceType = invoiceType;
	}

	@NotEmpty(message = "接入IP不能为空")
	@Length(max = 100, message = "接入IP不能超过100个字符")
	public String getMtIp() {
		return mtIp;
	}

	public void setMtIp(String mtIp) {
		this.mtIp = mtIp;
	}

	@NotNullNumberRange(min = 0, max = Integer.MAX_VALUE, message = "接入协议不能为空")
	public String getProtocolType() {
		return protocolType;
	}

	public void setProtocolType(String protocolType) {
		this.protocolType = protocolType;
	}

	@NotEmpty(message = "供应商不能为空")
	@Length(max = 50, message = "供应商不能超过50个字符")
	public String getSupplier() {
		return supplier;
	}

	public void setSupplier(String supplier) {
		this.supplier = supplier;
	}

	@NotEmpty(message = "联系人不能为空")
	@Length(max = 50, message = "联系人不能超过50个字符")
	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	@NotEmpty(message = "手机号码不能为空")
	@Length(max = 11, message = "手机号码不能超过11个字符")
	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	@NotNullNumberRange(min = 0, max = Integer.MAX_VALUE, message = "是否需要上级审核不能为空且必须是数字，取值范围为1至2147483647")
	public String getIsAudit() {
		return isAudit;
	}

	public void setIsAudit(String isAudit) {
		this.isAudit = isAudit;
	}

	@JsonFormat(pattern = "yyyy-MM-dd", timezone ="GMT+8")
	public Date getOnlineDate() {
		return onlineDate;
	}

	public void setOnlineDate(Date onlineDate) {
		this.onlineDate = onlineDate;
	}

	@NotNullNumberRange(min = 0, max = Integer.MAX_VALUE, message = "资源状态不能为空且必须是数字，取值范围为1至2147483647")
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Length(max = 50, message = "备注不能超过50个字符")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@CanNullNumberRange(min = 1, max = Integer.MAX_VALUE, message = "审批人必须是数字，取值范围为1至2147483647")
	public String getAuditorId() {
		return auditorId;
	}

	public void setAuditorId(String auditorId) {
		this.auditorId = auditorId;
	}

	public String getOperatorId() {
		return operatorId;
	}

	public void setOperatorId(String operatorId) {
		this.operatorId = operatorId;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	
	public String getBelongBusinessName() {
		return belongBusinessName;
	}

	public void setBelongBusinessName(String belongBusinessName) {
		this.belongBusinessName = belongBusinessName;
	}
}
