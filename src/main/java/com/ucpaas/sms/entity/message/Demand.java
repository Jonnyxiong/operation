package com.ucpaas.sms.entity.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ucpaas.sms.common.entity.BaseEntity;
import com.ucpaas.sms.validate.number.NotNullNumberRange;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.Date;

/**
 * Created by lpjLiu on 2017/6/20.
 */
public class Demand extends BaseEntity {
	private static final long serialVersionUID = 1L;

	private String demandId; // '需求编号，规则：X+4位序号+YYYYMMDD，X表示寻资源，序号范围[0000，9999]，同一天按顺序往下递增，YYYYMMDD表示年月日，如：X00002017061',
	private String industryType; // '所属行业'
	private String smsType; // '发送类型',
	private String expectNumber; // '预计量，单位：条/月',
	private String minimumGuarantee; // '是否保底，单位：万条/月',
	private String channelType; // '通道类型，多个类型间以＂,＂分隔，0：三网合一，1：移动，2：电信，3：联通，4：全网',
	private String directConnect; // '是否直连，0：直连，1：第三方'
	private String extendSize; // '扩展位，=0：无特殊要求；>0：要求位长',
	private String rate; // '速度（TPS），单位：条/秒',
	private String signType; // '签名类型，多个类型间以＂,＂分隔，0：自定义，1：报备签名，2：固签',
	private String contentTemplate; // '发送内容模板',
	private String salePrice; // '销售价格，单位：元/条',
	private Date onlineDate; // '要求上线时间',
	private String state; // '需求状态，0：寻资源，1：已供应，2：无资源，3：撤单',
	private String remark; // '备注',
	private String operatorId; // '提交人ID，关联t_sms_user表中id字段',
	private Date createTime; // '创建时间',
	private Date updateTime; // '修改时间'

	public Demand() {

	}

	public String getDemandId() {
		return demandId;
	}

	public void setDemandId(String demandId) {
		this.demandId = demandId;
	}

	@NotEmpty(message = "所属行业不能为空")
	@Length(max = 60, message = "所属行业不能超过60个字符")
	public String getIndustryType() {
		return industryType;
	}

	public void setIndustryType(String industryType) {
		this.industryType = industryType;
	}

	@NotEmpty(message = "发送类型不能为空")
	@Length(max = 60, message = "发送类型不能超过60个字符")
	public String getSmsType() {
		return smsType;
	}

	public void setSmsType(String smsType) {
		this.smsType = smsType;
	}

	@NotEmpty(message = "预计量不能为空")
	@Length(max = 60, message = "预计量不能超过60个字符")
	public String getExpectNumber() {
		return expectNumber;
	}

	public void setExpectNumber(String expectNumber) {
		this.expectNumber = expectNumber;
	}

	@NotEmpty(message = "是否保底不能为空")
	@Length(max = 60, message = "是否保底不能超过60个字符")
	public String getMinimumGuarantee() {
		return minimumGuarantee;
	}

	public void setMinimumGuarantee(String minimumGuarantee) {
		this.minimumGuarantee = minimumGuarantee;
	}

	@NotEmpty(message = "通道类型不能为空")
	@Length(max = 30, message = "通道类型不能超过30个字符")
	public String getChannelType() {
		return channelType;
	}

	public void setChannelType(String channelType) {
		this.channelType = channelType;
	}

	@NotNullNumberRange(min = 0, max = Integer.MAX_VALUE, message = "是否直连不能为空且必须是数字，取值范围为1至2147483647")
	public String getDirectConnect() {
		return directConnect;
	}

	public void setDirectConnect(String directConnect) {
		this.directConnect = directConnect;
	}

	@NotNullNumberRange(min = 0, max = Integer.MAX_VALUE, message = "扩展位不能为空且必须是数字，取值范围为1至2147483647")
	public String getExtendSize() {
		return extendSize;
	}

	public void setExtendSize(String extendSize) {
		this.extendSize = extendSize;
	}

	@NotNullNumberRange(min = 0, max = Integer.MAX_VALUE, message = "速度不能为空且必须是数字，取值范围为1至2147483647")
	public String getRate() {
		return rate;
	}

	public void setRate(String rate) {
		this.rate = rate;
	}

	@NotEmpty(message = "签名类型不能为空")
	@Length(max = 30, message = "签名类型不能超过30个字符")
	public String getSignType() {
		return signType;
	}

	public void setSignType(String signType) {
		this.signType = signType;
	}

	@NotEmpty(message = "发送内容属性不能为空")
	@Length(max = 1000, message = "发送内容属性不能超过1000个字符")
	public String getContentTemplate() {
		return contentTemplate;
	}

	public void setContentTemplate(String contentTemplate) {
		this.contentTemplate = contentTemplate;
	}

	@NotEmpty(message = "销售价格不能为空")
	@Length(max = 100, message = "销售价格不能超过100个字符")
	public String getSalePrice() {
		return salePrice;
	}

	public void setSalePrice(String salePrice) {
		this.salePrice = salePrice;
	}

	@JsonFormat(pattern = "yyyy-MM-dd", timezone ="GMT+8")
	public Date getOnlineDate() {
		return onlineDate;
	}

	public void setOnlineDate(Date onlineDate) {
		this.onlineDate = onlineDate;
	}

	@NotNullNumberRange(min = 0, max = Integer.MAX_VALUE, message = "资源需求状态不能为空且必须是数字，取值范围为1至2147483647")
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@Length(max = 100, message = "备注不能超过100个字符")
	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
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
}
