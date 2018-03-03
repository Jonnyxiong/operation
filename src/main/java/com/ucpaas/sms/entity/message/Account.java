package com.ucpaas.sms.entity.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.jsmsframework.finance.entity.JsmsUserPriceLog;
import com.jsmsframework.user.entity.JsmsClientInfoExt;
import com.ucpaas.sms.common.entity.BaseEntity;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

/**
 * 子账号Entity
 * 
 * @author lpjLiu
 * @version 2017-07-12
 */
@ApiModel(value = "客户")
public class Account extends BaseEntity {

	private String id;
	private String clientid; // 用户帐号（子帐号）
	private String password; // 用户密码
	private String name; // 用户名称
	private Integer status; // 1：注册完成，5：冻结，6：注销，7：锁定
	private Integer agentId; // 代理商id（关联t_sms_agent_info表中agent_id字段）
	private Integer oauthStatus; // 认证状态，2：待认证 ，3：证件已认证(正常)，4：认证不通过
	private Date oauthDate; // 认证时间
	private String mobile; // 手机号码
	private String email; // 邮箱地址
	private String province; // 省
	private String city; // 市
	private String area; // 区县
	private String address; // 个人地址/公司地址
	private String realname; // 个人姓名/公司名称
	private Integer clientLevel; // 用户等级，1：普通客户（6－8位用户扩展），2：中小企业大型企业（4－5位用户扩展），3：大型企业（2－3位用户扩展）
	private Integer clientType; // 用户类型，1：个人用户，2：企业用户
	private Integer needreport; // 是否需要状态报告，0：不需要，1：需要简单状态报告，2：需要透传状态报告,3:用户来拉取状态报告
	private Integer needmo; // 是否需要上行，0：不需要，1：SMSP推送 3.用户主动拉取
	private Integer needaudit; // 是否需要审核，0：不需要，1：营销需要，2：全部需要，3：关键字审核
	private Date createtime; // 创建时间
	private String ip; // 验证IP（可以有多个，用逗号分隔：192.168.0.*，*，192.168.0.0/16
	private String deliveryurl; // 状态报告回调地址
	private String mourl; // 上行回调地址
	private Integer nodenum; // 连接节点数
	private Integer paytype; // 付费类型，0：预付费，1：后付费
	private Integer needextend; // 是否支持自扩展，0：不支持，1：支持
	private Integer signextend; // 是否支持签名对应签名端口，0：不支持，1：支持
	private Long belongSale; // 所属销售，关联t_sms_user表中id字段
	private Integer agentOwned; // 是否代理商自有用户帐号，0：否，1：是
	private Integer smstype; // 短信类型，0：通知短信，4：验证码短信，5：营销短信（适用于标准协议）
	@ApiModelProperty(value = "客户接入使用协议类型", name = "客户接入使用协议类型")
	private Integer smsfrom; // 客户接入使用协议类型，2为SMPP协议，3为CMPP协议，4为SGIP协议，5为SMGP协议，6为HTTPS协议
	private Integer httpProtocolType; // HTTPS协议具体分类，0为https json；1为https get/post；2为https tx-json
	private Integer isoverratecharge; // 是否超频计费，0：不需要，1：需要
	private Date updatetime; // 更新时间
	private String spnum; // 提供给客户的sp号
	private Integer getreportInterval; // 拉取状态报告的最小时间间隔，单位秒
	private Integer getreportMaxsize; // 单个请求拉取状态报告的最大条数
	private String moip; // SGIP协议接入客户提供的上行IP
	private String moport; // SGIP协议接入客户提供的上行端口
	private Long nodeid; // 提供给SGIP协议接入客户的节点编码
	private Integer identify; // 标识，取值范围[0，9]，对应access流水表中的表名序号
	private Integer accessSpeed; // 客户接入速度，取值范围[1,10000]
	private String noticeurl; // 通知回调地址，适用于http协议模板短信接入客户
	private Integer extendSize; // 支持自扩展位数
	private Integer clientAscription; // 用户归属，0：阿里，1：代理商，2：云平台
	private Integer extendtype; // 用户端口扩展类型
	private String extendport; // 用户端口
	private Integer signportlen; // 签名端口长度
	private String remarks; // 备注
	private JsmsClientInfoExt clientInfoExt;
	//是否支持子账户0:不支持1:支持
	private Integer extValue;
	private Integer size;
	// Add by lpjLiu 20171012 v2.3.0 v5.15.0
	private Integer chargeRule;	//计费规则，0：提交量，1：成功量，2：明确成功量;
	private Date effectDate; // 生效时间

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}
	public Integer getExtValue() {
		return extValue;
	}

	public void setExtValue(Integer extValue) {
		this.extValue = extValue;
	}

	private List<JsmsUserPriceLog> userPriceList;

	public Account() {
		super();
	}

	public Account(String id) {
		this.setId(id);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getClientid() {
		return clientid;
	}

	public void setClientid(String clientid) {
		this.clientid = clientid;
	}

	@Length(min = 1, max = 12, message = "客户密码长度必须介于 1 和 12 之间")
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Length(min = 1, max = 50, message = "客户名称长度必须介于 1 和 50 之间")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getAgentId() {
		return agentId;
	}

	public void setAgentId(Integer agentId) {
		this.agentId = agentId;
	}

	public Integer getOauthStatus() {
		return oauthStatus;
	}

	public void setOauthStatus(Integer oauthStatus) {
		this.oauthStatus = oauthStatus;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone ="GMT+8")
	public Date getOauthDate() {
		return oauthDate;
	}

	public void setOauthDate(Date oauthDate) {
		this.oauthDate = oauthDate;
	}

	@Length(min = 0, max = 20, message = "手机号码长度必须介于 0 和 20 之间")
	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	@Length(min = 0, max = 100, message = "邮箱地址长度必须介于 0 和 100 之间")
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Length(min = 0, max = 60, message = "省长度必须介于 0 和 60 之间")
	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	@Length(min = 0, max = 60, message = "市长度必须介于 0 和 60 之间")
	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	@Length(min = 0, max = 60, message = "区县长度必须介于 0 和 60 之间")
	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	@Length(min = 0, max = 200, message = "客户地址长度必须介于 0 和 200 之间")
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Length(min = 1, max = 60, message = "客户名称长度必须介于 1 和 60 之间")
	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public Integer getClientLevel() {
		return clientLevel;
	}

	public void setClientLevel(Integer clientLevel) {
		this.clientLevel = clientLevel;
	}

	@NotNull(message = "客户类型不能为空")
	public Integer getClientType() {
		return clientType;
	}

	public void setClientType(Integer clientType) {
		this.clientType = clientType;
	}

	@NotNull(message = "状态报告获取方式不能为空")
	public Integer getNeedreport() {
		return needreport;
	}

	public void setNeedreport(Integer needreport) {
		this.needreport = needreport;
	}

	@NotNull(message = "上行获取方式不能为空")
	public Integer getNeedmo() {
		return needmo;
	}

	public void setNeedmo(Integer needmo) {
		this.needmo = needmo;
	}

	@NotNull(message = "是否审核不能为空")
	public Integer getNeedaudit() {
		return needaudit;
	}

	public void setNeedaudit(Integer needaudit) {
		this.needaudit = needaudit;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone ="GMT+8")
	public Date getCreatetime() {
		return createtime;
	}

	public void setCreatetime(Date createtime) {
		this.createtime = createtime;
	}

	@Length(min = 0, max = 100, message = "IP地址长度必须介于 1 和 512 之间")
	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	@Length(min = 0, max = 100, message = "状态报告回调地址长度必须介于 0 和 100 之间")
	public String getDeliveryurl() {
		return deliveryurl;
	}

	public void setDeliveryurl(String deliveryurl) {
		this.deliveryurl = deliveryurl;
	}

	@Length(min = 0, max = 100, message = "上行回调地址长度必须介于 0 和 100 之间")
	public String getMourl() {
		return mourl;
	}

	public void setMourl(String mourl) {
		this.mourl = mourl;
	}

	public Integer getNodenum() {
		return nodenum;
	}

	public void setNodenum(Integer nodenum) {
		this.nodenum = nodenum;
	}

	@NotNull(message = "付费类型不能为空")
	public Integer getPaytype() {
		return paytype;
	}

	public void setPaytype(Integer paytype) {
		this.paytype = paytype;
	}

	@NotNull(message = "自扩展不能为空")
	public Integer getNeedextend() {
		return needextend;
	}

	public void setNeedextend(Integer needextend) {
		this.needextend = needextend;
	}

	public Integer getSignextend() {
		return signextend;
	}

	public void setSignextend(Integer signextend) {
		this.signextend = signextend;
	}

	@NotNull(message = "归属销售不能为空")
	public Long getBelongSale() {
		return belongSale;
	}

	public void setBelongSale(Long belongSale) {
		this.belongSale = belongSale;
	}

	public Integer getAgentOwned() {
		return agentOwned;
	}

	public void setAgentOwned(Integer agentOwned) {
		this.agentOwned = agentOwned;
	}

	public Integer getSmstype() {
		return smstype;
	}

	public void setSmstype(Integer smstype) {
		this.smstype = smstype;
	}

	@NotNull(message = "客户接入使用协议类型不能为空")
	public Integer getSmsfrom() {
		return smsfrom;
	}

	public void setSmsfrom(Integer smsfrom) {
		this.smsfrom = smsfrom;
	}

	public Integer getHttpProtocolType() {
		return httpProtocolType;
	}

	public void setHttpProtocolType(Integer httpProtocolType) {
		this.httpProtocolType = httpProtocolType;
	}

	public Integer getIsoverratecharge() {
		return isoverratecharge;
	}

	public void setIsoverratecharge(Integer isoverratecharge) {
		this.isoverratecharge = isoverratecharge;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone ="GMT+8")
	public Date getUpdatetime() {
		return updatetime;
	}

	public void setUpdatetime(Date updatetime) {
		this.updatetime = updatetime;
	}

	public String getSpnum() {
		return spnum;
	}

	public void setSpnum(String spnum) {
		this.spnum = spnum;
	}

	public Integer getGetreportInterval() {
		return getreportInterval;
	}

	public void setGetreportInterval(Integer getreportInterval) {
		this.getreportInterval = getreportInterval;
	}

	public Integer getGetreportMaxsize() {
		return getreportMaxsize;
	}

	public void setGetreportMaxsize(Integer getreportMaxsize) {
		this.getreportMaxsize = getreportMaxsize;
	}

	@Length(min = 0, max = 100, message = "SGIP协议接入客户提供的上行IP长度必须介于 0 和 100 之间")
	public String getMoip() {
		return moip;
	}

	public void setMoip(String moip) {
		this.moip = moip;
	}

	@Length(min = 0, max = 11, message = "SGIP协议接入客户提供的上行端口长度必须介于 0 和 11 之间")
	public String getMoport() {
		return moport;
	}

	public void setMoport(String moport) {
		this.moport = moport;
	}

	public Long getNodeid() {
		return nodeid;
	}

	public void setNodeid(Long nodeid) {
		this.nodeid = nodeid;
	}

	public Integer getIdentify() {
		return identify;
	}

	public void setIdentify(Integer identify) {
		this.identify = identify;
	}

	@NotNull(message = "客户接入速度不能为空")
	public Integer getAccessSpeed() {
		return accessSpeed;
	}

	public void setAccessSpeed(Integer accessSpeed) {
		this.accessSpeed = accessSpeed;
	}

	public String getNoticeurl() {
		return noticeurl;
	}

	public void setNoticeurl(String noticeurl) {
		this.noticeurl = noticeurl;
	}

	public Integer getExtendSize() {
		return extendSize;
	}

	public void setExtendSize(Integer extendSize) {
		this.extendSize = extendSize;
	}

	public Integer getClientAscription() {
		return clientAscription;
	}

	public void setClientAscription(Integer clientAscription) {
		this.clientAscription = clientAscription;
	}

	public Integer getExtendtype() {
		return extendtype;
	}

	public void setExtendtype(Integer extendtype) {
		this.extendtype = extendtype;
	}

	public String getExtendport() {
		return extendport;
	}

	public void setExtendport(String extendport) {
		this.extendport = extendport;
	}

	public Integer getSignportlen() {
		return signportlen;
	}

	public void setSignportlen(Integer signportlen) {
		this.signportlen = signportlen;
	}

	@Length(min = 0, max = 200, message = "备注长度必须介于 0 和 200 之间")
	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public List<JsmsUserPriceLog> getUserPriceList() {
		return userPriceList;
	}

	public void setUserPriceList(List<JsmsUserPriceLog> userPriceList) {
		this.userPriceList = userPriceList;
	}

	public JsmsClientInfoExt getClientInfoExt() {
		return clientInfoExt;
	}

	public void setClientInfoExt(JsmsClientInfoExt clientInfoExt) {
		this.clientInfoExt = clientInfoExt;
	}

	public Integer getChargeRule() {
		return chargeRule;
	}

	public void setChargeRule(Integer chargeRule) {
		this.chargeRule = chargeRule;
	}

	public Date getEffectDate() {
		return effectDate;
	}

	public void setEffectDate(Date effectDate) {
		this.effectDate = effectDate;
	}
}