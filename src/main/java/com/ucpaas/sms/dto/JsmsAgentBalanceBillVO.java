package com.ucpaas.sms.dto;

import com.jsmsframework.finance.entity.JsmsAgentBalanceBill;

import java.math.BigDecimal;

/**
 * Created by xiongfenglin on 2017/11/7.
 *
 * @author: xiongfenglin
 */
public class JsmsAgentBalanceBillVO extends JsmsAgentBalanceBill{
    private String agentName;//代理商名称
    private Integer agentType;//代理商类型
    private String agentTypeStr;
    private String status;//代理商状态
    private String statusStr;
    private String mobile;//手机号码
    private String email;//邮箱
    private String realName;//归属销售
    private BigDecimal balancePrice;//合计-余额
    private BigDecimal creditBalancePrice;//合计-历史授信额度
    private BigDecimal historyPaymentPrice;//合计-历史授信回款额度
    private BigDecimal currentCreditPrice;//合计-授信余额
    private BigDecimal noBackPaymentPrice;//合计-未回款额度
    private String balanceStr;//余额
    private String creditBalanceStr;//历史授信额度
    private String historyPaymentStr;//历史授信回款额度
    private String currentCreditStr;//授信余额
    private String noBackPaymentStr;//未回款额度

    public String getBalanceStr() {
        return balanceStr;
    }

    public void setBalanceStr(String balanceStr) {
        this.balanceStr = balanceStr;
    }

    public String getCreditBalanceStr() {
        return creditBalanceStr;
    }

    public void setCreditBalanceStr(String creditBalanceStr) {
        this.creditBalanceStr = creditBalanceStr;
    }

    public String getHistoryPaymentStr() {
        return historyPaymentStr;
    }

    public void setHistoryPaymentStr(String historyPaymentStr) {
        this.historyPaymentStr = historyPaymentStr;
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

    public BigDecimal getBalancePrice() {
        return balancePrice;
    }

    public void setBalancePrice(BigDecimal balancePrice) {
        this.balancePrice = balancePrice;
    }

    public BigDecimal getCreditBalancePrice() {
        return creditBalancePrice;
    }

    public void setCreditBalancePrice(BigDecimal creditBalancePrice) {
        this.creditBalancePrice = creditBalancePrice;
    }

    public BigDecimal getHistoryPaymentPrice() {
        return historyPaymentPrice;
    }

    public void setHistoryPaymentPrice(BigDecimal historyPaymentPrice) {
        this.historyPaymentPrice = historyPaymentPrice;
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

    public String getRealName() {
        return realName;
    }
    public void setRealName(String realName) {
        this.realName = realName;
    }
    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public Integer getAgentType() {
        return agentType;
    }

    public void setAgentType(Integer agentType) {
        this.agentType = agentType;
    }

    public String getAgentTypeStr() {
        return agentTypeStr;
    }

    public void setAgentTypeStr(String agentTypeStr) {
        this.agentTypeStr = agentTypeStr;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatusStr() {
        return statusStr;
    }

    public void setStatusStr(String statusStr) {
        this.statusStr = statusStr;
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
}
