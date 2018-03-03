package com.ucpaas.sms.entity.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.ibatis.type.Alias;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Date;

@Alias("OrderList")
public class OrderList {

    // 订单号，规则：D+4位序号+YYYYMMDD，D表示短信，序号范围[0000，9999]，同一天按顺序往下递增，YYYYMMDD表示年月日，如：D000020170614
    private String orderId;
    // 公司名称
    @NotEmpty(message="公司名称不能为空")
    @Length(max = 50,message = "公司名称不能超过50个字符")
    private String companyName;
    // 所属行业
    @NotEmpty(message = "请选择所属行业")
    private String industryType;
    // 归属销售，关联t_sms_user表中id字段
    @NotNull(message = "请选择归属销售")
    private Long belongSale;
    // 发送类型
    @NotEmpty(message = "请选择发送类型")
    private String smsType;
    // 预计量，单位：条/月
    @NotEmpty(message = "请选择预计量")
    private String expectNumber;
    // 是否保底，单位：万条/月
    @NotEmpty(message = "请选择是否保底")
    @Pattern(regexp="^\\d{1,10}$",message="保底数量只能填写10位以内数字")
    private String minimumGuarantee;
    // 通道类型，多个类型间以＂,＂分隔，0：三网合一，1：移动，2：电信，3：联通，4：全网
    @NotEmpty(message = "请选择通道类型")
    private String channelType;
    // 是否直连，0：直连，1：第三方
    @NotNull(message = "请选择是否直连")
    private Integer directConnect;
    // 扩展位，=0：无特殊要求；>0：要求位长
    @NotNull(message = "请选择扩展位")
    @Max(value = 12,message = "扩展位数不能超过12")
    private Integer extendSize;
    // 速度（TPS），单位：条/秒
    @Max(value = 3000,message = "发送速率不能超过3000TPS")
    private Integer rate;
    // 签名类型，多个类型间以＂,＂分隔，0：自定义，1：报备签名，2：固签
    @NotEmpty(message = "请选择签名类型")
    private String signType;
    // 发送内容模板
    @NotEmpty(message = "请填写发送模板内容")
    @Length(max = 100,message = "模板内容不能超过100个字符")
    private String contentTemplate;
    // 结算方式
    @NotEmpty(message = "请选择结算方式")
    @Length(max = 50,message = "结算方式不能超过50个字符")
    private String payType;
    // 销售价格，单位：元/条
    @NotEmpty(message = "请填写销售价格")
    @Length(max = 50,message = "销售价格不能超过50个字符")
    private String salePrice;
    // 发票类型，0：不要票，1：专票，2：普票
    @NotNull(message = "请选择发票类型")
    private Integer invoiceType;
    // 要求上线时间
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date onlineDate;
    // 是否需要上级审核，0：否，1：是
    private Integer isAudit;
    // 订单状态，0：待配单，1：待匹配，2：待审批（二级审核），3：退单，4：撤单，5：匹配成功，6：寻资源，7：待审核（销售总监），8：待审核（订单运营）

    private Integer state;
    // 备注
    private String remark;
    // 审批人ID，关联t_sms_user表中id字段

    private Long auditorId;
    // 提交人ID，关联t_sms_user表中id字段

    private Long operatorId;
    // 创建时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    // 修改时间
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateTime;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getIndustryType() {
        return industryType;
    }

    public void setIndustryType(String industryType) {
        this.industryType = industryType;
    }

    public Long getBelongSale() {
        return belongSale;
    }

    public void setBelongSale(Long belongSale) {
        this.belongSale = belongSale;
    }

    public String getSmsType() {
        return smsType;
    }

    public void setSmsType(String smsType) {
        this.smsType = smsType;
    }

    public String getExpectNumber() {
        return expectNumber;
    }

    public void setExpectNumber(String expectNumber) {
        this.expectNumber = expectNumber;
    }

    public String getMinimumGuarantee() {
        return minimumGuarantee;
    }

    public void setMinimumGuarantee(String minimumGuarantee) {
        this.minimumGuarantee = minimumGuarantee;
    }

    public String getChannelType() {
        return channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public Integer getDirectConnect() {
        return directConnect;
    }

    public void setDirectConnect(Integer directConnect) {
        this.directConnect = directConnect;
    }

    public Integer getExtendSize() {
        return extendSize;
    }

    public void setExtendSize(Integer extendSize) {
        this.extendSize = extendSize;
    }

    public Integer getRate() {
        return rate;
    }

    public void setRate(Integer rate) {
        this.rate = rate;
    }

    public String getSignType() {
        return signType;
    }

    public void setSignType(String signType) {
        this.signType = signType;
    }

    public String getContentTemplate() {
        return contentTemplate;
    }

    public void setContentTemplate(String contentTemplate) {
        this.contentTemplate = contentTemplate;
    }

    public String getPayType() {
        return payType;
    }

    public void setPayType(String payType) {
        this.payType = payType;
    }

    public String getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(String salePrice) {
        this.salePrice = salePrice;
    }

    public Integer getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(Integer invoiceType) {
        this.invoiceType = invoiceType;
    }

    public Date getOnlineDate() {
        return onlineDate;
    }

    public void setOnlineDate(Date onlineDate) {
        this.onlineDate = onlineDate;
    }

    public Integer getIsAudit() {
        return isAudit;
    }

    public void setIsAudit(Integer isAudit) {
        this.isAudit = isAudit;
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

    public Long getAuditorId() {
        return auditorId;
    }

    public void setAuditorId(Long auditorId) {
        this.auditorId = auditorId;
    }

    public Long getOperatorId() {
        return operatorId;
    }

    public void setOperatorId(Long operatorId) {
        this.operatorId = operatorId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

}