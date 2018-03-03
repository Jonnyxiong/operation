package com.ucpaas.sms.dto;

import com.jsmsframework.finance.entity.JsmsAgentInvoiceList;

import java.io.Serializable;

/**
 * 发票审核页面交互
 *
 * @outhor tanjiangqiang
 * @create 2018-01-23 17:38
 */
public class InvoiceAuditVO extends JsmsAgentInvoiceList implements Serializable {

    /**
     * 客户名称
     */
    private String agentName;
    /**
     * 归属销售名称
     */
    private String belongSaleName;
    /**
     * 发票类型(名称)
     */
    private String invoiceTypeName;
    /**
     * 开票主体(名称)
     */
    private String invoiceBodyName;
    /**
     * 操作者人(名称)
     */
    private String operatorName;
    /**
     * 申请人(名称)
     */
    private String applicantName;
    /**
     * 审核人(名称)
     */
    private String auditorName;
    /**
     * 申请状态(名称)
     */
    private String statusName;

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getBelongSaleName() {
        return belongSaleName;
    }

    public void setBelongSaleName(String belongSaleName) {
        this.belongSaleName = belongSaleName;
    }

    public String getInvoiceTypeName() {
        return invoiceTypeName;
    }

    public void setInvoiceTypeName(String invoiceTypeName) {
        this.invoiceTypeName = invoiceTypeName;
    }

    public String getInvoiceBodyName() {
        return invoiceBodyName;
    }

    public void setInvoiceBodyName(String invoiceBodyName) {
        this.invoiceBodyName = invoiceBodyName;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getAuditorName() {
        return auditorName;
    }

    public void setAuditorName(String auditorName) {
        this.auditorName = auditorName;
    }
}