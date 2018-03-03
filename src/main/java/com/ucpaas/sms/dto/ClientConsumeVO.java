package com.ucpaas.sms.dto;

import com.jsmsframework.common.enums.ChargeRuleType;
import com.ucpaas.sms.entity.access.AccessSendStat;
import com.ucpaas.sms.enums.AgentType;
import com.ucpaas.sms.enums.PayType;
import com.jsmsframework.common.enums.SMSType;
import com.ucpaas.sms.util.ByZeroUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by dylan on 2017/7/26.
 * 客户消耗报表
 */
public class ClientConsumeVO extends AccessSendStat{

    /**
     * 统计的数据时间（天）格式化
     */
    private String dateStr;
    /**
     * 客户名称
     */
    private String clientName;
    /**
     * 客户组名称
     */
    private String accountGname;
    /**
     * 部门名称
     */
    private String departmentStr;
    /**
     * 代理商名称
     */
    private String agentName;
    /**
     * 代理商类型
     */
    private Integer agentType;
    private String agentTypeStr;
    /**
     * 归属销售, 关联t_sms_user表中的id字段
     */
    private String belongSaleStr;
    /**
     * 短信类型, 0:通知短信,4:验证码短信,5:营销短信,6:告警短信,7:USSD,8:闪信
     */
    private String smstypeStr;
    /**
     * 付费类型, 0:预付费,1:后付费
     */
    private String paytypeStr;
    /**
     * 通道总成本(单位:元), 通道侧数据 todo 单位转换
     */
    private String costfeeStr;
    /**
     * 客户总消费(单位:元) todo 单位转换
     */
    private String salefeeStr;
    /**
     * 创建时间, 信息保存时间
     */
    private String createtimeStr;
    /**
     * 客户注册时间
     */
    private Date clientCreateTime;
    /**
     * 客户注册时间
     */
    private String clientCreateTimeStr;
    /**
     * 提交条数
     * (0+1+3+4+5+6+7+8+9+10)
     */
    private Long submitTotal;
    /**
     * 计费条数
     * (1+3+4+6)
     */
    private Long chargeTotal;

    /**
     * 成功率 % (3/1+3+6)
     */
    private String successRate;
    /**
     * 成功率 % (3/0+1+3+4+5+6+7+8+9+10)
     */
    private String successRate2Dot3;

    /**
     * 失败条数 (4+6)
     */
    private Long failTotal;

    /**
     * 拦截条数 (5+7+8+9+10)
     */
    private Long interceptTotal;

    /**
     * 毛利 (消费金额-通道成本)
     */
    private String profit;

    /**
     * 毛利率 % (毛利/消费金额)
     */
    private String profitRate;
    public String getDateStr() {
        if(getDate() != null){
            if (getDate() < 99991231 && getDate() > 10000101) {
                dateStr = new StringBuilder(getDate().toString().substring(0, 4))
                        .append("-")
                        .append(getDate().toString().substring(4, 6))
                        .append("-")
                        .append(getDate().toString().substring(6))
                        .toString();
            }else if(getDate() < 999912 && getDate() > 100001){
                dateStr = new StringBuilder(getDate().toString().substring(0,4))
                        .append("-")
                        .append(getDate().toString().substring(4))
                        .toString();
            }
        }
        return dateStr;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }


//    public Integer getAccountGroupType() {
//        return accountGroupType;
//    }
//
//    public void setAccountGroupType(AccountType accountType) {
//        this.accountGroupType = accountType.getValue();
//    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getAccountGname() {
        if(!StringUtils.isEmpty(accountGname)){
            return accountGname;
        }else{
            return clientName;
        }
    }

    public void setAccountGname(String accountGname) {
        this.accountGname = accountGname;
    }

    public String getDepartmentStr() {
        return departmentStr;
    }

    public void setDepartmentStr(String departmentStr) {
        this.departmentStr = departmentStr;
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

//    public void setAccountGroupType(Integer accountGroupType) {
//        this.accountGroupType = accountGroupType;
//    }

    public String getAgentTypeStr() {
        return AgentType.getDescByValue(this.agentType);
    }

    public void setAgentTypeStr(String agentTypeStr) {
        this.agentTypeStr = agentTypeStr;
    }

    public String getBelongSaleStr() {
        return belongSaleStr;
    }

    public void setBelongSaleStr(String belongSaleStr) {
        this.belongSaleStr = belongSaleStr;
    }

    public String getSmstypeStr() {

        return SMSType.getDescByValue(getSmstype());
    }

    public void setSmstypeStr(String smstypeStr) {
        this.smstypeStr = smstypeStr;
    }

    public String getPaytypeStr() {
        
        return PayType.getDescByValue(getPaytype());
    }

    public void setPaytypeStr(String paytypeStr) {
        this.paytypeStr = paytypeStr;
    }

    public String getCostfeeStr() {
        if(getCostfee() != null){
            costfeeStr = getCostfee().divide(BigDecimal.valueOf(1000)).setScale(2,BigDecimal.ROUND_DOWN).toString();
        }else {
            costfeeStr = "0.00";
        }
        return costfeeStr;
    }

    public void setCostfeeStr(String costfeeStr) {
        this.costfeeStr = costfeeStr;
    }

    public String getSalefeeStr() {
        if(getSalefee() != null){
            salefeeStr = getSalefee().divide(BigDecimal.valueOf(1000)).setScale(2,BigDecimal.ROUND_DOWN).toString();
        }else {
            salefeeStr = "0.00";
        }
        return salefeeStr;
    }

    public void setSalefeeStr(String salefeeStr) {
        this.salefeeStr = salefeeStr;
    }

    public String getCreatetimeStr() {
        if (getCreatetime() != null){
//            createtimeStr = DateFormat.getDateTimeInstance().format(getCreatetime());
            createtimeStr = new DateTime(this.getCreatetime()).toString("yyyy-MM-dd HH:mm:ss");
        }else{
            createtimeStr = "-";
        }
        return createtimeStr;
    }

    public void setCreatetimeStr(String createtimeStr) {
        this.createtimeStr = createtimeStr;
    }

    public Date getClientCreateTime() {
        return clientCreateTime;
    }

    public void setClientCreateTime(Date clientCreateTime) {
        this.clientCreateTime = clientCreateTime;
    }

    public String getClientCreateTimeStr() {
        if (this.clientCreateTime != null){
//            clientCreateTimeStr = DateFormat.getDateTimeInstance().format(this.clientCreateTime);
            clientCreateTimeStr = new DateTime(this.clientCreateTime).toString("yyyy-MM-dd HH:mm:ss");
        }else{
            clientCreateTimeStr = "-";
        }
        return clientCreateTimeStr;
    }

    public void setClientCreateTimeStr(String clientCreateTimeStr) {
        this.clientCreateTimeStr = clientCreateTimeStr;
    }

    /**
     * 提交条数
     * (0+1+3+4+5+6+7+8+9+10)
     */
    public Long getSubmitTotal() {
        if(submitTotal != null){
            return submitTotal;
        }
        long submitTotal = 0;
        submitTotal = getNotsend() + getSubmitsuccess() +getReportsuccess() + getSubmitfail() + getSubretfail() + getReportfail() + getAuditfail() + getRecvintercept() + getSendintercept() + getOverrateintercept();
        return submitTotal;
    }

    public void setSubmitTotal(Long submitTotal) {
        this.submitTotal = submitTotal;
    }

    /**
     * 子客户 计费条数(1+3+4+6)
     * 直户 计费条数(1+3)
     */
    public Long getChargeTotal() {
        if(chargeTotal != null){
            return chargeTotal;
        }

        long status1 = getSubmitsuccess().longValue();
        long status3 = getReportsuccess().longValue();
        long status4 = getSubmitfail().longValue();
        long status6 = getReportfail().longValue();

        //计费条数计算
        if(getChargeRule() != null && getChargeRule().equals(ChargeRuleType.成功量.getValue())){
            chargeTotal = status1 + status3;

        }else if (getChargeRule() != null && getChargeRule().equals(ChargeRuleType.提交量.getValue())){
            chargeTotal = status1+status3+status4+status6;

        }else if (getChargeRule() != null && getChargeRule().equals(ChargeRuleType.明确成功量.getValue())){
            chargeTotal = status3;

        }else{ //历史数据没有计费规则则按直客的“计费规则”是成功量，代理商子客户的“计费规则”是提交量。
            if(getChargeRule() != null && getAgentId()==null||getAgentId()==0||getAgentId()==1||getAgentId()==2) {  //直客
                chargeTotal = status1 + status3;
            }else{ //代理商子客户
                chargeTotal = status1+status3+status4+status6;
            }
        }

        return chargeTotal;
    }

    public void setChargeTotal(Long chargeTotal) {
        this.chargeTotal = chargeTotal;
    }
    /**
     * 成功率 % (3/1+3+6)
     */
    public String getSuccessRate() {
        int temp = getSubmitsuccess() + getReportsuccess() + getReportfail();
        if(ByZeroUtil.isDivisorZero(temp)){
            return "-";
        }
        BigDecimal rate = new BigDecimal(getReportsuccess()).
                divide(ByZeroUtil.divideExcludeZero(temp),5,BigDecimal.ROUND_DOWN)
                .multiply(new BigDecimal("100"));

        successRate = new StringBuilder(rate.setScale(1,BigDecimal.ROUND_DOWN).toString()).append("%").toString();
        return successRate;
    }

    public void setSuccessRate(String successRate) {
        this.successRate = successRate;
    }
    /**
     * 成功率 % (3/0+1+3+4+5+6+7+8+9+10)
     */
    public String getSuccessRate2Dot3() {
        long temp = getSubmitTotal();
        if(ByZeroUtil.isDivisorZero(temp)){
            return "-";
        }
        BigDecimal rate = new BigDecimal(getReportsuccess()).
                divide(ByZeroUtil.divideExcludeZero(temp),5,BigDecimal.ROUND_DOWN)
                .multiply(new BigDecimal("100"));

        successRate2Dot3 = new StringBuilder(rate.setScale(1,BigDecimal.ROUND_DOWN).toString()).append("%").toString();
        return successRate2Dot3;
    }

    public void setSuccessRate2Dot3(String successRate2Dot3) {
        this.successRate2Dot3 = successRate2Dot3;
    }

    /**
     * 失败条数 (4+6)
     */
    public Long getFailTotal() {
        if(failTotal != null){
            return failTotal;
        }
        long failTotal = getSubmitfail() + getReportfail();
        return failTotal;
    }

    public void setFailTotal(Long failTotal) {
        this.failTotal = failTotal;
    }
    /**
     * 拦截条数 (5+7+8+9+10)
     */
    public Long getInterceptTotal() {
        if(interceptTotal != null){
            return interceptTotal;
        }
        long interceptTotal =  getSubretfail()  + getAuditfail() + getRecvintercept() + getSendintercept() + getOverrateintercept();
        return interceptTotal;
    }

    public void setInterceptTotal(Long interceptTotal) {
        this.interceptTotal = interceptTotal;
    }
    /**
     * 毛利 (消费金额-通道成本)
     */
    public String getProfit() {
        profit = getSalefee().subtract(getCostfee()).divide(BigDecimal.valueOf(1000)).setScale(2,BigDecimal.ROUND_DOWN).toString();
        return profit;
    }

    public void setProfit(String profit) {
        this.profit = profit;
    }
    /**
     * 毛利率 % (毛利/消费金额)
     */
    public String getProfitRate() {
        if(ByZeroUtil.isDivisorZero(getSalefee())){
            return "-";
        }
        BigDecimal rate = getSalefee().subtract(getCostfee())
                .divide( ByZeroUtil.divideExcludeZero(getSalefee()),5,BigDecimal.ROUND_DOWN)
                .multiply(new BigDecimal("100"));
        profitRate = new StringBuilder(rate.setScale(1,BigDecimal.ROUND_DOWN).toString()).append("%").toString();
        return profitRate;
    }

    public void setProfitRate(String profitRate) {
        this.profitRate = profitRate;
    }

    public ClientConsumeVO() {
    }

    public ClientConsumeVO(Integer id, Integer departmentId, Integer agentId, Integer accountGid, String clientid, Long belongSale, Integer smstype, Integer paytype, Integer notsend, Integer submitsuccess, Integer reportsuccess, Integer submitfail, Integer subretfail, Integer reportfail, Integer auditfail, Integer recvintercept, Integer sendintercept, Integer overrateintercept, BigDecimal costfee, BigDecimal salefee, Integer stattype, Integer date, Date createtime, String clientName, String accountGname, String departmentStr, String agentName, Integer agentType, String agentTypeStr, String belongSaleStr, String smstypeStr, String paytypeStr, String costfeeStr, String salefeeStr, String createtimeStr, Date clientCreateTime, String clientCreateTimeStr, Long submitTotal, Long chargeTotal, String successRate, Long failTotal, Long interceptTotal, String profit, String profitRate) {
        super(id, departmentId, agentId, accountGid, clientid, belongSale, smstype, paytype, notsend, submitsuccess, reportsuccess, submitfail, subretfail, reportfail, auditfail, recvintercept, sendintercept, overrateintercept, costfee, salefee, stattype, date, createtime);
        this.clientName = clientName;
        this.accountGname = accountGname;
        this.departmentStr = departmentStr;
        this.agentName = agentName;
        this.agentType = agentType;
        this.agentTypeStr = agentTypeStr;
        this.belongSaleStr = belongSaleStr;
        this.smstypeStr = smstypeStr;
        this.paytypeStr = paytypeStr;
        this.costfeeStr = costfeeStr;
        this.salefeeStr = salefeeStr;
        this.createtimeStr = createtimeStr;
        this.clientCreateTime = clientCreateTime;
        this.clientCreateTimeStr = clientCreateTimeStr;
        this.submitTotal = submitTotal;
        this.chargeTotal = chargeTotal;
        this.successRate = successRate;
        this.failTotal = failTotal;
        this.interceptTotal = interceptTotal;
        this.profit = profit;
        this.profitRate = profitRate;
    }

    public static ClientConsumeVO init(){

        return new ClientConsumeVO(null, null, null, null,null,null, null,
                null,0,0,0,0,0,0,0,0,
                0,0,BigDecimal.ZERO, BigDecimal.ZERO, null,null,null,null,
                null,null,null,null,null,null,null,null,
                null,null,null,null,null,0L,0L,null,
                0L, 0L,null,null);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("departmentStr", departmentStr)
                .append("agentName", agentName)
                .append("agentType", agentType)
                .append("belongSaleStr", belongSaleStr)
                .append("smstypeStr", smstypeStr)
                .append("paytypeStr", paytypeStr)
                .append("costfeeStr", costfeeStr)
                .append("salefeeStr", salefeeStr)
                .append("createtimeStr", createtimeStr)
                .append("clientCreateTime", clientCreateTime)
                .append("submitTotal", submitTotal)
                .append("chargeTotal", chargeTotal)
                .append("successRate", successRate)
                .append("failTotal", failTotal)
                .append("interceptTotal", interceptTotal)
                .append("profit", profit)
                .append("profitRate", profitRate)
                .toString();
    }


}
