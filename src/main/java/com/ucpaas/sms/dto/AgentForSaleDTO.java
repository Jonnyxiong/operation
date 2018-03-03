package com.ucpaas.sms.dto;

import com.jsmsframework.common.enums.*;
import com.jsmsframework.user.entity.JsmsAgentInfo;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Created by Don on 2017/11/4.
 */
public class AgentForSaleDTO extends JsmsAgentInfo {

    private String agentTypeName;
    private String statusName;
    private String belongSaleName;
    // 账户余额（元）
    private BigDecimal balance;
    // 历史授信额度，单位：元
    private BigDecimal creditBalance;
    //历史授信回款额度，单位：元
    private BigDecimal historyPayment;
    //授信余额，单位：元
    private BigDecimal currentCredit;
    //未回款授信额度，单位：元
    private BigDecimal noBackPayment;


    public String getAgentTypeName() {
       agentTypeName=AgentType.getDescByValue(getAgentType());
        return agentTypeName;
    }

    public void setAgentTypeName(String agentTypeName) {
        this.agentTypeName = agentTypeName;
    }

    public String getStatusName() {
        if(Objects.equals(getStatus(), AgentStatus.注册完成.getValue())){
            statusName="已启用";
        }else{
            statusName=AgentStatus.getDescByValue(getStatus().toString());
        }

        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getBelongSaleName() {
        return belongSaleName;
    }

    public void setBelongSaleName(String belongSaleName) {
        this.belongSaleName = belongSaleName;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getCreditBalance() {
        return creditBalance;
    }

    public void setCreditBalance(BigDecimal creditBalance) {
        this.creditBalance = creditBalance;
    }

    public BigDecimal getHistoryPayment() {
        return historyPayment;
    }

    public void setHistoryPayment(BigDecimal historyPayment) {
        this.historyPayment = historyPayment;
    }

    public BigDecimal getCurrentCredit() {
        return currentCredit;
    }

    public void setCurrentCredit(BigDecimal currentCredit) {
        this.currentCredit = currentCredit;
    }

    public BigDecimal getNoBackPayment() {
        return noBackPayment;
    }

    public void setNoBackPayment(BigDecimal noBackPayment) {
        this.noBackPayment = noBackPayment;
    }
}

