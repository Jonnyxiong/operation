package com.ucpaas.sms.entity.access;

import org.apache.ibatis.type.Alias;
import org.omg.PortableInterceptor.INACTIVE;

import java.math.BigDecimal;
import java.util.Date;

@Alias("AccessSendStat")
public class AccessSendStat {
    // 主键, 自增长
    private Integer id;
    // 部门id(一级部门), 关联t_sms_department表中department_id字段
    private Integer departmentId;
    // 代理商id, 关联t_sms_agent_info表中agent_id字段
    private Integer agentId;
    // 客户组id, 关联t_sms_account_group表中的account_gid字段
    private Integer accountGid;
    // 用户账户(子账号), 关联t_sms_account表中clientid字段
    private String clientid;
    // 归属销售, 关联t_sms_user表中的id字段
    private Long belongSale;
    // 短信类型, 0:通知短信,4:验证码短信,5:营销短信,6:告警短信,7:USSD,8:闪信
    private Integer smstype;
    // 付费类型, 0:预付费,1:后付费
    private Integer paytype;
    //计费规则，0：提交量， 1：成功量， 2：明确成功量
    private Integer chargeRule;
    // 未发送数量(状态:0)
    private Integer notsend;
    // 提交成功数量(状态:1)
    private Integer submitsuccess;
    // 明确成功数量(状态:3)
    private Integer reportsuccess;
    // 提交失败数量(状态:4)
    private Integer submitfail;
    // 发送失败数量(状态:5)
    private Integer subretfail;
    // 明确失败数量(状态:6)
    private Integer reportfail;
    // 审核不通过数量(状态:7)
    private Integer auditfail;
    // 网关接收拦截数量(状态:8)
    private Integer recvintercept;
    // 网关发送拦截数量(状态:9)
    private Integer sendintercept;
    // 超频拦截数量(状态:10)
    private Integer overrateintercept;
    // 通道总成本(单位:厘), 通道侧数据
    private BigDecimal costfee;
    // 客户总消耗(单位:厘)
    private BigDecimal salefee;
    //需返还总条数
    private Integer returnTotalNumber;
    //需返还总金额
    private BigDecimal returnTotalAmount;
    // 统计类型, 0:每日,2:每月
    private Integer stattype;
    // 统计的数据时间(天)
    private Integer date;
    // 创建时间, 信息保存时间
    private Date createtime;
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id ;
    }
    
    public Integer getDepartmentId() {
        return departmentId;
    }
    
    public void setDepartmentId(Integer departmentId) {
        this.departmentId = departmentId ;
    }
    
    public Integer getAgentId() {
        return agentId;
    }
    
    public void setAgentId(Integer agentId) {
        this.agentId = agentId ;
    }
    
    public Integer getAccountGid() {
        return accountGid;
    }
    
    public void setAccountGid(Integer accountGid) {
        this.accountGid = accountGid ;
    }
    
    public String getClientid() {
        return clientid;
    }
    
    public void setClientid(String clientid) {
        this.clientid = clientid ;
    }
    
    public Long getBelongSale() {
        return belongSale;
    }
    
    public void setBelongSale(Long belongSale) {
        this.belongSale = belongSale ;
    }
    
    public Integer getSmstype() {
        return smstype;
    }
    
    public void setSmstype(Integer smstype) {
        this.smstype = smstype ;
    }
    
    public Integer getPaytype() {
        return paytype;
    }
    
    public void setPaytype(Integer paytype) {
        this.paytype = paytype ;
    }
    
    public Integer getNotsend() {
        return notsend;
    }
    
    public void setNotsend(Integer notsend) {
        this.notsend = notsend ;
    }
    
    public Integer getSubmitsuccess() {
        return submitsuccess;
    }
    
    public void setSubmitsuccess(Integer submitsuccess) {
        this.submitsuccess = submitsuccess ;
    }
    
    public Integer getReportsuccess() {
        return reportsuccess;
    }
    
    public void setReportsuccess(Integer reportsuccess) {
        this.reportsuccess = reportsuccess ;
    }
    
    public Integer getSubmitfail() {
        return submitfail;
    }
    
    public void setSubmitfail(Integer submitfail) {
        this.submitfail = submitfail ;
    }
    
    public Integer getSubretfail() {
        return subretfail;
    }
    
    public void setSubretfail(Integer subretfail) {
        this.subretfail = subretfail ;
    }
    
    public Integer getReportfail() {
        return reportfail;
    }
    
    public void setReportfail(Integer reportfail) {
        this.reportfail = reportfail ;
    }
    
    public Integer getAuditfail() {
        return auditfail;
    }
    
    public void setAuditfail(Integer auditfail) {
        this.auditfail = auditfail ;
    }
    
    public Integer getRecvintercept() {
        return recvintercept;
    }
    
    public void setRecvintercept(Integer recvintercept) {
        this.recvintercept = recvintercept ;
    }
    
    public Integer getSendintercept() {
        return sendintercept;
    }
    
    public void setSendintercept(Integer sendintercept) {
        this.sendintercept = sendintercept ;
    }
    
    public Integer getOverrateintercept() {
        return overrateintercept;
    }
    
    public void setOverrateintercept(Integer overrateintercept) {
        this.overrateintercept = overrateintercept ;
    }
    
    public BigDecimal getCostfee() {
        return costfee;
    }
    
    public void setCostfee(BigDecimal costfee) {
        this.costfee = costfee ;
    }
    
    public BigDecimal getSalefee() {
        return salefee;
    }
    
    public void setSalefee(BigDecimal salefee) {
        this.salefee = salefee ;
    }
    
    public Integer getStattype() {
        return stattype;
    }
    
    public void setStattype(Integer stattype) {
        this.stattype = stattype ;
    }
    
    public Integer getDate() {
        return date;
    }
    
    public void setDate(Integer date) {
        this.date = date ;
    }
    
    public Date getCreatetime() {
        return createtime;
    }
    
    public void setCreatetime(Date createtime) {
        this.createtime = createtime ;
    }

    public AccessSendStat() {
    }

    public AccessSendStat(Integer id, Integer departmentId, Integer agentId, Integer accountGid, String clientid, Long belongSale, Integer smstype, Integer paytype, Integer notsend, Integer submitsuccess, Integer reportsuccess, Integer submitfail, Integer subretfail, Integer reportfail, Integer auditfail, Integer recvintercept, Integer sendintercept, Integer overrateintercept, BigDecimal costfee, BigDecimal salefee, Integer stattype, Integer date, Date createtime) {
        this.id = id;
        this.departmentId = departmentId;
        this.agentId = agentId;
        this.accountGid = accountGid;
        this.clientid = clientid;
        this.belongSale = belongSale;
        this.smstype = smstype;
        this.paytype = paytype;
        this.notsend = notsend;
        this.submitsuccess = submitsuccess;
        this.reportsuccess = reportsuccess;
        this.submitfail = submitfail;
        this.subretfail = subretfail;
        this.reportfail = reportfail;
        this.auditfail = auditfail;
        this.recvintercept = recvintercept;
        this.sendintercept = sendintercept;
        this.overrateintercept = overrateintercept;
        this.costfee = costfee;
        this.salefee = salefee;
        this.stattype = stattype;
        this.date = date;
        this.createtime = createtime;
    }

    public static AccessSendStat init(){

        return new AccessSendStat(-1, -1, -1,-1, "", -1L, -1, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0,0, BigDecimal.ZERO, BigDecimal.ZERO, 0, 0, null) ;
    }

    public Integer getChargeRule() {
        return chargeRule;
    }

    public void setChargeRule(Integer chargeRule) {
        this.chargeRule = chargeRule;
    }

    public Integer getReturnTotalNumber() {
        return returnTotalNumber;
    }

    public void setReturnTotalNumber(Integer returnTotalNumber) {
        this.returnTotalNumber = returnTotalNumber;
    }

    public BigDecimal getReturnTotalAmount() {
        return returnTotalAmount;
    }

    public void setReturnTotalAmount(BigDecimal returnTotalAmount) {
        this.returnTotalAmount = returnTotalAmount;
    }
}