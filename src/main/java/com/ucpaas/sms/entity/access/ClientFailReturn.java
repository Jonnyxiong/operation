package com.ucpaas.sms.entity.access;

import java.util.Date;
import java.math.BigDecimal;

/**
 * @description 客户失败返回清单表
 * @author lpjLiu
 * @date 2017-10-11
 */
public class ClientFailReturn {

    // 主键, 自增长
    private Integer id;
    // 用户账号,关联t_sms_account表中clientid字段
    private String clientid;
    // 付费类型, 0:预付费, 1:后付费
    private Integer paytype;
    // 计费规则，0：提交量，1：成功量，2：明确成功量
    private Integer chargeRule;
    // 短信类型, 0:通知短信, 4:验证码短信, 5:营销短信, 6:告警短信, 7:USSD, 8:闪信
    private Integer smstype;
    // 扣费关联id, 1: 品牌代理和销售代理,对应t_sms_client_order表中的sub_id字段; 2:OEM代理,对应t_sms_oem_client_pool表中client_pool_id字段。针对代理商预付费用户
    private String subId;
    // 产品类型, 0:行业, 1:营销, 2:国际, 3:验证码, 4:通知, 7:USSD, 8:闪信, 9:挂机短信, 其中0、1、3、4为普通短信，2为国际短信
    private Integer productType;
    // 对应运营商, 0:全网, 1:移动, 2:联通, 3:电信, 4:国际 。针对代理商预付费用户
    private Integer operatorCode;
    // 适用区域, 0:全国, 1:国际 针对代理商预付费用户
    private Integer areaCode;
    // 到期时间, 针对代理商预付费用户
    private Date dueTime;
    // 普通短信单价，单位：元，预付费用户为sub_id对应的单价，后付费用户为（发送日期+短信类型）对应的单价
    private BigDecimal unitPrice;
    // 提交成功数量(状态:1)
    private Integer submitsuccess;
    // 提交失败数量(状态:4)
    private Integer submitfail;
    // 明确失败数量(状态:6)
    private Integer reportfail;
    // 返还条数
    private Integer returnNumber;
    // 所属销售，关联t_sms_access_0表中belong_sale字段
    private Long belongSale;
    // 退费状态, 0:未退费, 1:已退费
    private Integer refundState;
    // 统计的数据时间(天)
    private Integer date;
    // 创建时间，信息保存时间
    private Date createTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id ;
    }

    public String getClientid() {
        return clientid;
    }

    public void setClientid(String clientid) {
        this.clientid = clientid ;
    }

    public Integer getPaytype() {
        return paytype;
    }

    public void setPaytype(Integer paytype) {
        this.paytype = paytype ;
    }

    public Integer getChargeRule() {
        return chargeRule;
    }

    public void setChargeRule(Integer chargeRule) {
        this.chargeRule = chargeRule ;
    }

    public Integer getSmstype() {
        return smstype;
    }

    public void setSmstype(Integer smstype) {
        this.smstype = smstype ;
    }

    public String getSubId() {
        return subId;
    }

    public void setSubId(String subId) {
        this.subId = subId ;
    }

    public Integer getProductType() {
        return productType;
    }

    public void setProductType(Integer productType) {
        this.productType = productType ;
    }

    public Integer getOperatorCode() {
        return operatorCode;
    }

    public void setOperatorCode(Integer operatorCode) {
        this.operatorCode = operatorCode ;
    }

    public Integer getAreaCode() {
        return areaCode;
    }

    public void setAreaCode(Integer areaCode) {
        this.areaCode = areaCode ;
    }

    public Date getDueTime() {
        return dueTime;
    }

    public void setDueTime(Date dueTime) {
        this.dueTime = dueTime ;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice ;
    }

    public Integer getSubmitsuccess() {
        return submitsuccess;
    }

    public void setSubmitsuccess(Integer submitsuccess) {
        this.submitsuccess = submitsuccess ;
    }

    public Integer getSubmitfail() {
        return submitfail;
    }

    public void setSubmitfail(Integer submitfail) {
        this.submitfail = submitfail ;
    }

    public Integer getReportfail() {
        return reportfail;
    }

    public void setReportfail(Integer reportfail) {
        this.reportfail = reportfail ;
    }

    public Integer getReturnNumber() {
        return returnNumber;
    }

    public void setReturnNumber(Integer returnNumber) {
        this.returnNumber = returnNumber ;
    }

    public Integer getRefundState() {
        return refundState;
    }

    public Long getBelongSale() {
        return belongSale;
    }

    public void setBelongSale(Long belongSale) {
        this.belongSale = belongSale;
    }

    public void setRefundState(Integer refundState) {
        this.refundState = refundState ;
    }

    public Integer getDate() {
        return date;
    }

    public void setDate(Integer date) {
        this.date = date ;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime ;
    }

}