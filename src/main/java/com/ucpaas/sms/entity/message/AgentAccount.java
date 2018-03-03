package com.ucpaas.sms.entity.message;

import java.math.BigDecimal;

import org.apache.ibatis.type.Alias;

/**
 * @description 代理商帐户表
 * @author huangwenjie
 * @date 2017-07-15
 */
@Alias("AgentAccount")
public class AgentAccount {
    
    // 代理商id
    private Integer agentId;
    // 账户余额（元）
    private BigDecimal balance;
    // 信用额度（元）,0为不可透支,>0为可透支金额
    private BigDecimal creditBalance;
    // 佣金收入（元）
    private BigDecimal commissionIncome;
    // 返点收入（元）
    private BigDecimal rebateIncome;
    // 押金（元）
    private BigDecimal deposit;
    // 历史充值总额（元）
    private BigDecimal accumulatedRecharge;
    // 累计消费总额（元）
    private BigDecimal accumulatedConsume;
    // 历史累计佣金收入（元）
    private BigDecimal accumulatedIncome;
    // 累计返点收入（元）
    private BigDecimal accumulatedRebateIncome;
    // 累计返点支出（元）
    private BigDecimal accumulatedRebatePay;
    
    public Integer getAgentId() {
        return agentId;
    }
    
    public void setAgentId(Integer agentId) {
        this.agentId = agentId ;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public void setBalance(BigDecimal balance) {
        this.balance = balance ;
    }
    
    public BigDecimal getCreditBalance() {
        return creditBalance;
    }
    
    public void setCreditBalance(BigDecimal creditBalance) {
        this.creditBalance = creditBalance ;
    }
    
    public BigDecimal getCommissionIncome() {
        return commissionIncome;
    }
    
    public void setCommissionIncome(BigDecimal commissionIncome) {
        this.commissionIncome = commissionIncome ;
    }
    
    public BigDecimal getRebateIncome() {
        return rebateIncome;
    }
    
    public void setRebateIncome(BigDecimal rebateIncome) {
        this.rebateIncome = rebateIncome ;
    }
    
    public BigDecimal getDeposit() {
        return deposit;
    }
    
    public void setDeposit(BigDecimal deposit) {
        this.deposit = deposit ;
    }
    
    public BigDecimal getAccumulatedRecharge() {
        return accumulatedRecharge;
    }
    
    public void setAccumulatedRecharge(BigDecimal accumulatedRecharge) {
        this.accumulatedRecharge = accumulatedRecharge ;
    }
    
    public BigDecimal getAccumulatedConsume() {
        return accumulatedConsume;
    }
    
    public void setAccumulatedConsume(BigDecimal accumulatedConsume) {
        this.accumulatedConsume = accumulatedConsume ;
    }
    
    public BigDecimal getAccumulatedIncome() {
        return accumulatedIncome;
    }
    
    public void setAccumulatedIncome(BigDecimal accumulatedIncome) {
        this.accumulatedIncome = accumulatedIncome ;
    }
    
    public BigDecimal getAccumulatedRebateIncome() {
        return accumulatedRebateIncome;
    }
    
    public void setAccumulatedRebateIncome(BigDecimal accumulatedRebateIncome) {
        this.accumulatedRebateIncome = accumulatedRebateIncome ;
    }
    
    public BigDecimal getAccumulatedRebatePay() {
        return accumulatedRebatePay;
    }
    
    public void setAccumulatedRebatePay(BigDecimal accumulatedRebatePay) {
        this.accumulatedRebatePay = accumulatedRebatePay ;
    }
    
}