package com.ucpaas.sms.dto;

import java.math.BigDecimal;
import java.util.List;

import com.jsmsframework.finance.entity.JsmsUserPriceLog;
import com.jsmsframework.user.entity.JsmsClientInfoExt;
import com.ucpaas.sms.entity.message.AgentInfo;
import com.ucpaas.sms.enums.AgentType;

public class AgentRequest extends AgentInfo {
	// 邮箱
	private String email;
	// 域名
	private String domainName;
	// 版权文件
	private String copyrightText;
	// 标签页LOGO图,尺寸48*48
	private String tabLogoUrl;
	// LOGO图,尺寸41*43
	private String logoUrl;
	// 公司名称图,尺寸184*30
	private String companyLogoUrl;
	// 接口文档,文件 <= 2M
	private String apiDocumentUrl;
	// FAQ文档,文件 <= 2M
	private String fAQDocumentUrl;
	// 导航栏背景色,16进制颜色码 如:天蓝色为#87CEEB
	private String navigationBackcolor;
	// 导航栏文字颜色,16进制颜色码 如:天蓝色为#87CEEB
	private String navigationTextColor;
	// 行业短信折扣率
	private BigDecimal hySmsDiscount;
	// 营销短信折扣率
	private BigDecimal yxSmsDiscount;
	// 国际短信折扣率
	private BigDecimal gjSmsDiscount;
	// 测试短信产品id号，关联t_sms_oem_product_info表中product_id字段

	private Integer testProductId;
	// 赠送的测试短信条数
	private Integer testSmsNumber;
	//登入密码
	private String password;
	//-------------------------代理商开户之资质认证----------------------------------
	//证件类型
	private String idType;
	//证件号码
	private String idNbr;
	//证件图片
	private String imgUrl;
	//-----------------------代理商开户之开通子账户------------------------------------
	//是否支持子账户0:不支持1:支持
	private Integer extValue;
	private Integer smstype; // 短信类型，0：通知短信，4：验证码短信，5：营销短信（适用于标准协议）
	private String moip; // SGIP协议接入客户提供的上行IP
	private String moport; // SGIP协议接入客户提供的上行端口
	private Long nodeid; // 提供给SGIP协议接入客户的节点编码
	private Integer extendSize; // 支持自扩展位数
	//用户名称
	private String name;
	// 付费方式，0：预付费，1：后付费
	private Integer paytype;
	//计费规则，0：提交量，1：成功量，2：明确成功量;
	private Integer chargeRule;
	//使用对象
	private Integer agentOwned; // 是否代理商自有用户帐号，0：否，1：是
	//短信协议
	private Integer smsfrom; // 客户接入使用协议类型，2为SMPP协议，3为CMPP协议，4为SGIP协议，5为SMGP协议，6为HTTPS协议
	//http子协议
	private Integer httpProtocolType; // HTTPS协议具体分类，0为https json；1为https get/post；2为https tx-json
	// 状态报告获取方式
	private Integer needreport; // 是否需要状态报告，0：不需要，1：需要简单状态报告，2：需要透传状态报告,3:用户来拉取状态报告
	//上行获取方式
	private Integer needmo; // 是否需要上行，0：不需要，1：SMSP推送 3.用户主动拉取
	//上行回调地址
	private String mourl; // 上行回调地址
	//状态报告回调地址
	private String deliveryurl; // 状态报告回调地址
	//模板审核通知回调地址
	private String noticeurl; // 通知回调地址，适用于http协议模板短信接入客户
	//IP白名单
	private String ip; // 验证IP（可以有多个，用逗号分隔：192.168.0.*，*，192.168.0.0/16
	//自扩展
	private Integer needextend; // 是否支持自扩展，0：不支持，1：支持

	public Integer getExtValue() {
		return extValue;
	}

	public void setExtValue(Integer extValue) {
		this.extValue = extValue;
	}

	public Integer getSmstype() {
		return smstype;
	}

	public void setSmstype(Integer smstype) {
		this.smstype = smstype;
	}

	public String getMoip() {
		return moip;
	}

	public void setMoip(String moip) {
		this.moip = moip;
	}

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

	public Integer getExtendSize() {
		return extendSize;
	}

	public void setExtendSize(Integer extendSize) {
		this.extendSize = extendSize;
	}

	private List<JsmsUserPriceLog> userPriceList;

	public List<JsmsUserPriceLog> getUserPriceList() {
		return userPriceList;
	}

	public void setUserPriceList(List<JsmsUserPriceLog> userPriceList) {
		this.userPriceList = userPriceList;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Integer getChargeRule() {
		return chargeRule;
	}

	public void setChargeRule(Integer chargeRule) {
		this.chargeRule = chargeRule;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getImgUrl() {
		return imgUrl;
	}
	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}
	public String getIdType() {
		return idType;
	}
	public void setIdType(String idType) {
		this.idType = idType;
	}
	public String getIdNbr() {
		return idNbr;
	}
	public void setIdNbr(String idNbr) {
		this.idNbr = idNbr;
	}

	public Integer getPaytype() {
		return paytype;
	}

	public void setPaytype(Integer paytype) {
		this.paytype = paytype;
	}

	public Integer getAgentOwned() {
		return agentOwned;
	}

	public void setAgentOwned(Integer agentOwned) {
		this.agentOwned = agentOwned;
	}

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

	public Integer getNeedreport() {
		return needreport;
	}

	public void setNeedreport(Integer needreport) {
		this.needreport = needreport;
	}

	public Integer getNeedmo() {
		return needmo;
	}

	public void setNeedmo(Integer needmo) {
		this.needmo = needmo;
	}

	public String getMourl() {
		return mourl;
	}

	public void setMourl(String mourl) {
		this.mourl = mourl;
	}

	public String getDeliveryurl() {
		return deliveryurl;
	}

	public void setDeliveryurl(String deliveryurl) {
		this.deliveryurl = deliveryurl;
	}

	public String getNoticeurl() {
		return noticeurl;
	}

	public void setNoticeurl(String noticeurl) {
		this.noticeurl = noticeurl;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public Integer getNeedextend() {
		return needextend;
	}

	public void setNeedextend(Integer needextend) {
		this.needextend = needextend;
	}
	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	public String getCopyrightText() {
		return copyrightText;
	}

	public void setCopyrightText(String copyrightText) {
		this.copyrightText = copyrightText;
	}

	public String getTabLogoUrl() {
		return tabLogoUrl;
	}

	public void setTabLogoUrl(String tabLogoUrl) {
		this.tabLogoUrl = tabLogoUrl;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public String getCompanyLogoUrl() {
		return companyLogoUrl;
	}

	public void setCompanyLogoUrl(String companyLogoUrl) {
		this.companyLogoUrl = companyLogoUrl;
	}

	public String getApiDocumentUrl() {
		return apiDocumentUrl;
	}

	public void setApiDocumentUrl(String apiDocumentUrl) {
		this.apiDocumentUrl = apiDocumentUrl;
	}

	public String getfAQDocumentUrl() {
		return fAQDocumentUrl;
	}

	public void setfAQDocumentUrl(String fAQDocumentUrl) {
		this.fAQDocumentUrl = fAQDocumentUrl;
	}

	public String getNavigationBackcolor() {
		return navigationBackcolor;
	}

	public void setNavigationBackcolor(String navigationBackcolor) {
		this.navigationBackcolor = navigationBackcolor;
	}

	public String getNavigationTextColor() {
		return navigationTextColor;
	}

	public void setNavigationTextColor(String navigationTextColor) {
		this.navigationTextColor = navigationTextColor;
	}

	public BigDecimal getHySmsDiscount() {
		return hySmsDiscount;
	}

	public void setHySmsDiscount(BigDecimal hySmsDiscount) {
		this.hySmsDiscount = hySmsDiscount;
	}

	public BigDecimal getYxSmsDiscount() {
		return yxSmsDiscount;
	}

	public void setYxSmsDiscount(BigDecimal yxSmsDiscount) {
		this.yxSmsDiscount = yxSmsDiscount;
	}

	public BigDecimal getGjSmsDiscount() {
		return gjSmsDiscount;
	}

	public void setGjSmsDiscount(BigDecimal gjSmsDiscount) {
		this.gjSmsDiscount = gjSmsDiscount;
	}

	public Integer getTestProductId() {
		return testProductId;
	}

	public void setTestProductId(Integer testProductId) {
		this.testProductId = testProductId;
	}

	public Integer getTestSmsNumber() {
		return testSmsNumber;
	}

	public void setTestSmsNumber(Integer testSmsNumber) {
		this.testSmsNumber = testSmsNumber;
	}

	
	
	public boolean isOEM(){
		return getAgentType().intValue() == AgentType.OEM代理商.getValue().intValue();
	}
	public boolean isBrand(){
		return  !isOEM();
	}
}
