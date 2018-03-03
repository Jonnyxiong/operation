package com.ucpaas.sms.dto;

import java.math.BigDecimal;

public class ClientConsumeVO2Point3 {


    //计费规则
    private Integer chargeRule;
    //计费规则
    private String chargeRuleStr;
    //客户类型
    private String clientTypeStr;
    //部门
    private Integer departmentId;
    private String departmentStr;
    //客户ID
    private String clientid;
    //客户名称
    private String accountGname;
    //所属代理商ID
    private Integer agentId;
    //代理商名称
    private String agentName;
    //代理商类型
    private Integer agentType;
    //代理商类型
    private String agentTypeStr;
    // 归属销售, 关联t_sms_user表中的id字段
    private Long belongSale;
    //归属销售
    private String belongSaleStr;
    // 付费类型, 0:预付费,1:后付费
    private Integer paytype;
    //付费类型
    private String paytypeStr;
    // 短信类型, 0:通知短信,4:验证码短信,5:营销短信,6:告警短信,7:USSD,8:闪信
    private Integer smstype;
    //短信类型
    private String smstypeStr;
    //提交条数（条）
    // (0+1+3+4+5+6+7+8+9+10)
    private long submitTotal;
    //计费条数（条） 根据计费规则计算
    private long chargeTotal;
    //成功率（%）
    //(3/(0+1+3+4+5+6+7+8+9+10))
    private String successRate;
    //成功条数（条） 明确成功数量(状态:3)
    private long reportsuccess;
    //未知条数（条） 提交成功数量(状态:1)
    private long submitsuccess;
    //失败条数（条）  (4+6)
    private long failTotal;
    //待发送条数（条） 未发送数量(状态:0)
    private long notsend;
    //拦截条数（条） (5+7+8+9+10)
    private long interceptTotal;

    //消费金额（元） 客户总消耗
    private BigDecimal salefee;
    //消费金额（元） 客户总消耗
    private String salefeeStr;
    //通道成本（元） 通道总成本
    private BigDecimal costfee;
    //通道成本（元） 通道总成本
    private String costfeeStr;
    //毛利 （元） (消费金额-通道成本)
    private String profit;
    //毛利率（%） (毛利/消费金额)
    private String profitRate;
    //未返还条数 未返还条数=需返还条数-已返还条数
    private int pendingReturnNumber;
    //未返还计费 未返还条数=需返还条数-已返还条数
    private BigDecimal pendingReturnAmount;
    //未返还计费 未返还条数=需返还条数-已返还条数
    private String pendingReturnAmountStr;
    // 运营商类型
    private Integer operatorstype;
    //数据种类 普通短信/国际短信
    private String operatorstypeStr;
    // 提交失败数量(状态:4)
    private int submitfail;
    // 发送失败数量(状态:5)
    private int subretfail;
    // 明确失败数量(状态:6)
    private int reportfail;
    // 审核不通过数量(状态:7)
    private int auditfail;
    // 网关接收拦截数量(状态:8)
    private int recvintercept;
    // 网关发送拦截数量(状态:9)
    private int sendintercept;
    // 超频拦截数量(状态:10)
    private int overrateintercept;

    public int getSubretfail() {
        return subretfail;
    }

    public void setSubretfail(int subretfail) {
        this.subretfail = subretfail;
    }

    public int getReportfail() {
        return reportfail;
    }

    public void setReportfail(int reportfail) {
        this.reportfail = reportfail;
    }

    public int getAuditfail() {
        return auditfail;
    }

    public void setAuditfail(int auditfail) {
        this.auditfail = auditfail;
    }

    public int getRecvintercept() {
        return recvintercept;
    }

    public void setRecvintercept(int recvintercept) {
        this.recvintercept = recvintercept;
    }

    public int getSendintercept() {
        return sendintercept;
    }

    public void setSendintercept(int sendintercept) {
        this.sendintercept = sendintercept;
    }

    public int getOverrateintercept() {
        return overrateintercept;
    }

    public void setOverrateintercept(int overrateintercept) {
        this.overrateintercept = overrateintercept;
    }

    public int getSubmitfail() {
        return submitfail;
    }

    public void setSubmitfail(int submitfail) {
        this.submitfail = submitfail;
    }

    public BigDecimal getSalefee() {
        return salefee;
    }

    public void setSalefee(BigDecimal salefee) {
        this.salefee = salefee;
    }

    public BigDecimal getCostfee() {
        return costfee;
    }

    public void setCostfee(BigDecimal costfee) {
        this.costfee = costfee;
    }

    public String getChargeRuleStr() {
        return chargeRuleStr;
    }

    public void setChargeRuleStr(String chargeRuleStr) {
        this.chargeRuleStr = chargeRuleStr;
    }

    public String getClientTypeStr() {
        return clientTypeStr;
    }

    public void setClientTypeStr(String clientTypeStr) {
        this.clientTypeStr = clientTypeStr;
    }

    public Integer getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentStr() {
        return departmentStr;
    }

    public void setDepartmentStr(String departmentStr) {
        this.departmentStr = departmentStr;
    }

    public String getClientid() {
        return clientid;
    }

    public void setClientid(String clientid) {
        this.clientid = clientid;
    }

    public String getAccountGname() {
        return accountGname;
    }

    public void setAccountGname(String accountGname) {
        this.accountGname = accountGname;
    }

    public Integer getAgentId() {
        return agentId;
    }

    public void setAgentId(Integer agentId) {
        this.agentId = agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAgentTypeStr() {
        return agentTypeStr;
    }

    public void setAgentTypeStr(String agentTypeStr) {
        this.agentTypeStr = agentTypeStr;
    }

    public Long getBelongSale() {
        return belongSale;
    }

    public void setBelongSale(Long belongSale) {
        this.belongSale = belongSale;
    }

    public String getBelongSaleStr() {
        return belongSaleStr;
    }

    public void setBelongSaleStr(String belongSaleStr) {
        this.belongSaleStr = belongSaleStr;
    }

    public String getPaytypeStr() {
        return paytypeStr;
    }

    public void setPaytypeStr(String paytypeStr) {
        this.paytypeStr = paytypeStr;
    }

    public Integer getSmstype() {
        return smstype;
    }

    public void setSmstype(Integer smstype) {
        this.smstype = smstype;
    }

    public String getSmstypeStr() {
        return smstypeStr;
    }

    public void setSmstypeStr(String smstypeStr) {
        this.smstypeStr = smstypeStr;
    }

    public long getSubmitTotal() {
        return submitTotal;
    }

    public void setSubmitTotal(long submitTotal) {
        this.submitTotal = submitTotal;
    }

    public long getChargeTotal() {
        return chargeTotal;
    }

    public void setChargeTotal(long chargeTotal) {
        this.chargeTotal = chargeTotal;
    }

    public String getSuccessRate() {
        return successRate;
    }

    public void setSuccessRate(String successRate) {
        this.successRate = successRate;
    }

    public long getReportsuccess() {
        return reportsuccess;
    }

    public void setReportsuccess(long reportsuccess) {
        this.reportsuccess = reportsuccess;
    }

    public long getSubmitsuccess() {
        return submitsuccess;
    }

    public void setSubmitsuccess(long submitsuccess) {
        this.submitsuccess = submitsuccess;
    }

    public long getFailTotal() {
        return failTotal;
    }

    public void setFailTotal(long failTotal) {
        this.failTotal = failTotal;
    }

    public long getNotsend() {
        return notsend;
    }

    public void setNotsend(long notsend) {
        this.notsend = notsend;
    }

    public long getInterceptTotal() {
        return interceptTotal;
    }

    public void setInterceptTotal(long interceptTotal) {
        this.interceptTotal = interceptTotal;
    }

    public String getSalefeeStr() {

        if(salefeeStr!=null&&!salefeeStr.equals("")) {
            return salefeeStr;
        }else{
            salefeeStr = salefee.setScale(2,BigDecimal.ROUND_HALF_UP).toString();
        }
        return salefeeStr;
    }

    public void setSalefeeStr(String salefeeStr) {
        this.salefeeStr = salefeeStr;
    }

    public String getCostfeeStr() {
        if(costfeeStr!=null&&!costfeeStr.equals("")) {
            return costfeeStr;
        }else{
            costfeeStr = costfee.setScale(2,BigDecimal.ROUND_HALF_UP).toString();
        }
        return costfeeStr;
    }

    public void setCostfeeStr(String costfeeStr) {
        this.costfeeStr = costfeeStr;
    }

    public String getProfit() {
        return profit;
    }

    public void setProfit(String profit) {
        this.profit = profit;
    }

    public String getProfitRate() {
        return profitRate;
    }

    public void setProfitRate(String profitRate) {
        this.profitRate = profitRate;
    }

    public int getPendingReturnNumber() {
        return pendingReturnNumber;
    }

    public void setPendingReturnNumber(int pendingReturnNumber) {
        this.pendingReturnNumber = pendingReturnNumber;
    }

    public Integer getOperatorstype() {
        return operatorstype;
    }

    public void setOperatorstype(Integer operatorstype) {
        this.operatorstype = operatorstype;
    }

    public String getOperatorstypeStr() {
        return operatorstypeStr;
    }

    public void setOperatorstypeStr(String operatorstypeStr) {
        this.operatorstypeStr = operatorstypeStr;
    }

    public void setAgentType(Integer agentType) {
        this.agentType = agentType;
    }

    public Integer getAgentType() {
        return agentType;
    }

    public Integer getPaytype() {
        return paytype;
    }

    public void setPaytype(Integer paytype) {
        this.paytype = paytype;
    }

    public Integer getChargeRule() {
        return chargeRule;
    }

    public void setChargeRule(Integer chargeRule) {
        this.chargeRule = chargeRule;
    }

    public BigDecimal getPendingReturnAmount() {
        return pendingReturnAmount;
    }

    public void setPendingReturnAmount(BigDecimal pendingReturnAmount) {
        this.pendingReturnAmount = pendingReturnAmount;
    }

    public String getPendingReturnAmountStr() {
        if(pendingReturnAmountStr!=null&&!pendingReturnAmountStr.equals("")) {
            return pendingReturnAmountStr;
        }else{
            pendingReturnAmountStr = pendingReturnAmount.setScale(2,BigDecimal.ROUND_HALF_UP).toString();
        }
        return pendingReturnAmountStr;
    }

    public void setPendingReturnAmountStr(String pendingReturnAmountStr) {
        this.pendingReturnAmountStr = pendingReturnAmountStr;
    }
}
