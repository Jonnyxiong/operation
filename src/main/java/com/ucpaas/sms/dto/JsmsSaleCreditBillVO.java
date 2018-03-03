package com.ucpaas.sms.dto;

import com.jsmsframework.finance.entity.JsmsSaleCreditBill;

import java.math.BigDecimal;

/**
 * Created by xiongfenglin on 2017/11/7.
 *
 * @author: xiongfenglin
 */
public class JsmsSaleCreditBillVO extends JsmsSaleCreditBill{
    private String realName;//销售名称
    private String mobile;//手机号码
    private String email;//邮箱
    private BigDecimal financialHistoryCreditPrice;//合计-历史授信额度
    private BigDecimal saleHistoryCreditPrice;//合计-销售历史售出额度
    private BigDecimal agentHistoryPaymentPrice;//合计-历史回款额度
    private BigDecimal currentCreditPrice;//合计-授信余额
    private BigDecimal noBackPaymentPrice;//合计-未回款额度
    private String financialHistoryCreditStr;//历史授信额度
    private String saleHistoryCreditStr;//销售历史售出额度
    private String agentHistoryPaymentStr;//历史回款额度
    private String currentCreditStr;//授信余额
    private String noBackPaymentStr;//未回款额度
    private String adminName;//操作者
    private String amountStr;//账单金额
    private String  businessTypeStr;//业务类型
    private String  financialTypeStr;//财务类型
    private String createTimeStr;//创建时间
    private String agentName;//代理商名称
    private Integer agentId;//代理商ID
    private String agentTypeStr;//代理商类型

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public Integer getAgentId() {
        return agentId;
    }

    public void setAgentId(Integer agentId) {
        this.agentId = agentId;
    }

    public String getAgentTypeStr() {
        return agentTypeStr;
    }

    public void setAgentTypeStr(String agentTypeStr) {
        this.agentTypeStr = agentTypeStr;
    }

    public String getCreateTimeStr() {
        return createTimeStr;
    }

    public void setCreateTimeStr(String createTimeStr) {
        this.createTimeStr = createTimeStr;
    }

    public String getBusinessTypeStr() {
        return businessTypeStr;
    }

    public void setBusinessTypeStr(String businessTypeStr) {
        this.businessTypeStr = businessTypeStr;
    }

    public String getFinancialTypeStr() {
        return financialTypeStr;
    }

    public void setFinancialTypeStr(String financialTypeStr) {
        this.financialTypeStr = financialTypeStr;
    }

    public String getAmountStr() {
        return amountStr;
    }

    public void setAmountStr(String amountStr) {
        this.amountStr = amountStr;
    }

    public String getAdminName() {
        return adminName;
    }

    public void setAdminName(String adminName) {
        this.adminName = adminName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getFinancialHistoryCreditPrice() {
        return financialHistoryCreditPrice;
    }

    public void setFinancialHistoryCreditPrice(BigDecimal financialHistoryCreditPrice) {
        this.financialHistoryCreditPrice = financialHistoryCreditPrice;
    }

    public BigDecimal getSaleHistoryCreditPrice() {
        return saleHistoryCreditPrice;
    }

    public void setSaleHistoryCreditPrice(BigDecimal saleHistoryCreditPrice) {
        this.saleHistoryCreditPrice = saleHistoryCreditPrice;
    }

    public BigDecimal getAgentHistoryPaymentPrice() {
        return agentHistoryPaymentPrice;
    }

    public void setAgentHistoryPaymentPrice(BigDecimal agentHistoryPaymentPrice) {
        this.agentHistoryPaymentPrice = agentHistoryPaymentPrice;
    }

    public BigDecimal getCurrentCreditPrice() {
        return currentCreditPrice;
    }

    public void setCurrentCreditPrice(BigDecimal currentCreditPrice) {
        this.currentCreditPrice = currentCreditPrice;
    }

    public BigDecimal getNoBackPaymentPrice() {
        return noBackPaymentPrice;
    }

    public void setNoBackPaymentPrice(BigDecimal noBackPaymentPrice) {
        this.noBackPaymentPrice = noBackPaymentPrice;
    }

    public String getFinancialHistoryCreditStr() {
        return financialHistoryCreditStr;
    }

    public void setFinancialHistoryCreditStr(String financialHistoryCreditStr) {
        this.financialHistoryCreditStr = financialHistoryCreditStr;
    }

    public String getSaleHistoryCreditStr() {
        return saleHistoryCreditStr;
    }

    public void setSaleHistoryCreditStr(String saleHistoryCreditStr) {
        this.saleHistoryCreditStr = saleHistoryCreditStr;
    }

    public String getAgentHistoryPaymentStr() {
        return agentHistoryPaymentStr;
    }

    public void setAgentHistoryPaymentStr(String agentHistoryPaymentStr) {
        this.agentHistoryPaymentStr = agentHistoryPaymentStr;
    }

    public String getCurrentCreditStr() {
        return currentCreditStr;
    }

    public void setCurrentCreditStr(String currentCreditStr) {
        this.currentCreditStr = currentCreditStr;
    }

    public String getNoBackPaymentStr() {
        return noBackPaymentStr;
    }

    public void setNoBackPaymentStr(String noBackPaymentStr) {
        this.noBackPaymentStr = noBackPaymentStr;
    }
}
