package com.ucpaas.sms.dto;

import com.jsmsframework.common.enums.OperatorType;
import com.jsmsframework.common.enums.SMSType;
import com.ucpaas.sms.entity.record.RecordConsumeStat;
import com.ucpaas.sms.enums.PayType;
import com.ucpaas.sms.util.CheckNullUtils;
import com.ucpaas.sms.util.ByZeroUtil;

import java.math.BigDecimal;

/**
 * Created by dylan on 2017/7/26.
 * 通道消耗报表
 */
public class ChannelConsumeVO extends RecordConsumeStat {
//    // 主键，自增长
//    private Integer id;
//    // 通道号，关联t_sms_channel表中cid字段
//    private Integer channelid;
    /**
     * 归属商务，关联t_sms_user表中id字段
     */
    private String belongBusinessStr;
    /**
     * 部门名称
     */
    private String departmentStr;
    /**
     * 短信类型，0：通知短信，4：验证码短信，5：营销短信，6：告警短信，7：USSD，8：闪信
     */
    private String smstypeStr;
    /**
     * 付费类型，0：预付费，1：后付费
     */
    private String paytypeStr;
//    // 通道说明
//    private String remark;
    /**
     * 运营商类型，0：全网，1：移动，2：联通，3：电信，4：国际
     */
    private String operatorstypeStr;
    /**
     * 通道单价，单位：厘 todo 不能转成元
     */
    private String costpriceStr;
    /**
     * 通道单价总和，单位：元
     */
    private BigDecimal costpriceTotal;
    /**
     * 通道总成本，单位：厘 todo 转成元
     */
    private String costtotalStr;
    /**
     * 销售收入，单位：厘，客户侧 todo 转成元
     */
    private String saletotalStr;

    /**
     * 总发送量（条）(1+2+3+5+6)
     */
    private Long sendTotal;
    /**
     * 通道计费数（条）（1+2+3）
     */
    private Long chargeTotal;
    /**
     * 成功率 % (3/1+2+3+5+6)
     * (3/sendTotal)
     */
    private String successRate;

    /**
     * 成功量 (2+3)
     */
    private Long successTotal;
    /**
     * 失败条数 (5+6)
     */
    private Long failTotal;
    /**
     * 毛利 (销售收入-通道成本)
     */
    private String profit;

    /**
     * 毛利率 % (毛利/销售收入)
     */
    private String profitRate;

    public ChannelConsumeVO() {
    }

    public ChannelConsumeVO(String belongBusinessStr, String departmentStr, String smstypeStr, String paytypeStr, String operatorstypeStr, String costpriceStr, String costtotalStr, String saletotalStr, Long sendTotal, Long chargeTotal, String successRate, Long successTotal, Long failTotal, String profit, String profitRate) {
        this.belongBusinessStr = belongBusinessStr;
        this.departmentStr = departmentStr;
        this.smstypeStr = smstypeStr;
        this.paytypeStr = paytypeStr;
        this.operatorstypeStr = operatorstypeStr;
        this.costpriceStr = costpriceStr;
        this.costtotalStr = costtotalStr;
        this.saletotalStr = saletotalStr;
        this.sendTotal = sendTotal;
        this.chargeTotal = chargeTotal;
        this.successRate = successRate;
        this.successTotal = successTotal;
        this.failTotal = failTotal;
        this.profit = profit;
        this.profitRate = profitRate;
    }

    public String getBelongBusinessStr() {
        return belongBusinessStr;
    }

    public void setBelongBusinessStr(String belongBusinessStr) {
        this.belongBusinessStr = belongBusinessStr;
    }

    public String getDepartmentStr() {
        return departmentStr;
    }

    public void setDepartmentStr(String departmentStr) {
        this.departmentStr = departmentStr;
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

    public String getOperatorstypeStr() {
        return OperatorType.getDescByValue(getOperatorstype());
    }

    public void setOperatorstypeStr(String operatorstypeStr) {
        this.operatorstypeStr = operatorstypeStr;
    }

    /**
     * 通道单价，单位：厘 todo 不能转成元
     */
    public String getCostpriceStr() {
        if (getCostprice() != null) {
            costpriceStr = getCostprice().setScale(4, BigDecimal.ROUND_DOWN).toString();
        } else {
            costpriceStr = "0.0000";
        }
        return costpriceStr;
    }

    public void setCostpriceStr(String costpriceStr) {
        this.costpriceStr = costpriceStr;
    }

    /**
     * 通道总成本，单位：厘 todo 转成元
     */
    public String getCosttotalStr() {
        if (getCosttotal() != null) {
            costtotalStr = getCosttotal().divide(BigDecimal.valueOf(1000)).setScale(2, BigDecimal.ROUND_DOWN).toString();
        } else {
            costtotalStr = "0.00";
        }
        return costtotalStr;
    }

    public void setCosttotalStr(String costtotalStr) {
        this.costtotalStr = costtotalStr;
    }

    /**
     * 销售收入，单位：厘，客户侧 todo 转成元
     */
    public String getSaletotalStr() {
        if (getSaletotal() != null) {
            saletotalStr = getSaletotal().divide(BigDecimal.valueOf(1000)).setScale(2, BigDecimal.ROUND_DOWN).toString();
        } else {
            saletotalStr = "0.0";
        }
        return saletotalStr;
    }

    public void setSaletotalStr(String saletotalStr) {
        this.saletotalStr = saletotalStr;
    }

    /**
     * 总发送量（条）(1+2+3+5+6)
     */
    public Long getSendTotal() {
        if(sendTotal != null){
            return sendTotal;
        }
        long sendTotal = getSubmitsuccess() + getSubretsuccess() + getReportsuccess() + getSubretfail() + getReportfail();
        return sendTotal;
    }

    public void setSendTotal(Long sendTotal) {
        this.sendTotal = sendTotal;
    }
    /**
     * 通道计费数（条）（1+2+3）
     */
    public Long getChargeTotal() {
        if(chargeTotal != null){
            return chargeTotal;
        }
        long temp = getSubmitsuccess() + getSubretsuccess() + getReportsuccess();
        chargeTotal = temp;
        return chargeTotal;
    }

    public void setChargeTotal(Long chargeTotal) {
        this.chargeTotal = chargeTotal;
    }

    /**
     * 成功率 % (3/1+2+3+5+6)
     * (3/sendTotal)
     */
    public String getSuccessRate() {
        if(ByZeroUtil.isDivisorZero(getSendTotal())){
            return "-";
        }
        BigDecimal rate = new BigDecimal(getReportsuccess()).
                divide(ByZeroUtil.divideExcludeZero(getSendTotal()), 5, BigDecimal.ROUND_DOWN)
                .multiply(new BigDecimal("100"));

        return new StringBuilder(rate.setScale(1, BigDecimal.ROUND_DOWN).toString()).append("%").toString();
    }

    public void setSuccessRate(String successRate) {
        this.successRate = successRate;
    }

    /**
     * 成功量 (2+3)
     */
    public Long getSuccessTotal() {
        if(successTotal != null){
            return successTotal;
        }
        long successTotal = getReportsuccess() + getSubretsuccess();
        return successTotal;
    }

    public void setSuccessTotal(Long successTotal) {
        this.successTotal = successTotal;
    }

    /**
     * 失败条数 (5+6)
     */
    public Long getFailTotal() {
        if(failTotal != null){
            return failTotal;
        }
        long failTotal = getSubretfail() + getReportfail();
        return failTotal;
    }

    public void setFailTotal(Long failTotal) {
        this.failTotal = failTotal;
    }

    /**
     * 毛利 (销售收入-通道成本)
     */
    public String getProfit() {
        profit = CheckNullUtils.check(getSaletotal()).subtract(CheckNullUtils.check(getCosttotal())).divide(BigDecimal.valueOf(1000)).setScale(2,BigDecimal.ROUND_DOWN).toString();
        return profit;
    }

    public void setProfit(String profit) {
        this.profit = profit;
    }

    /**
     * 毛利率 % (毛利/销售收入)
     */
    public String getProfitRate() {

        if(ByZeroUtil.isDivisorZero(getSaletotal())){
            return "-";
        }
        BigDecimal rate = getSaletotal().subtract(getCosttotal())
                .divide( ByZeroUtil.divideExcludeZero(getSaletotal()),5,BigDecimal.ROUND_DOWN)
                .multiply(new BigDecimal("100"));
        profitRate = new StringBuilder(rate.setScale(1,BigDecimal.ROUND_DOWN).toString()).append("%").toString();
        return profitRate;
    }

    public void setProfitRate(String profitRate) {
        this.profitRate = profitRate;
    }

    public static ChannelConsumeVO init(){
        return new ChannelConsumeVO(null,null, null,null,null, null, null,null,0L,0L, null,0L, 0L,null,null) ;
    }

    public BigDecimal getCostpriceTotal() {
        return costpriceTotal;
    }

    public void setCostpriceTotal(BigDecimal costpriceTotal) {
        this.costpriceTotal = costpriceTotal;
    }
}
