package com.ucpaas.sms.dto;

import com.jsmsframework.common.enums.OperatorType;
import com.jsmsframework.common.enums.SMSType;
import com.ucpaas.sms.entity.access.AccessChannelStatistics;
import com.ucpaas.sms.enums.PayType;
import com.ucpaas.sms.util.CheckNullUtils;
import com.ucpaas.sms.util.ByZeroUtil;

import java.math.BigDecimal;

/**
 * Created by dylan on 2017/7/28.
 */
public class AccessStatisticVO extends AccessChannelStatistics {

    /**
     * 统计的数据时间（天）格式化
     */
    private String dateStr;
    /**
     * 短信类型, 0:通知短信,4:验证码短信,5:营销短信,6:告警短信,7:USSD,8:闪信
     */
    private String smstypeStr;
    /**
     * 付费类型, 0:预付费,1:后付费
     */
    private String paytypeStr;

    /**
     * 成功率（%）(3/1+3+6)
     */
    private String successRate;

    /**
     * 成功量 (2+3)
     */
    private Long successTotal;;
    /**
     * 客户总消费(单位:元) todo 单位转换
     */
    private String salefeeStr;

    /**
     * 毛利 (消费金额-通道成本)
     */
    private String profit;

    /**
     * 毛利率 % (毛利/消费金额)
     */
    private String profitRate;
    /**
     * 通道计费数（条）（1+2+3）（通道侧）
     */
    private Integer recordChargeTotal;
    /**
     * 通道单价，单位：厘
     */
    private BigDecimal costprice;
    /**
     * 通道单价，单位：厘 todo 不能转成元
     */
    private String costpriceStr;
    /**
     * 通道侧 总成本(单位：厘)
     */
    private BigDecimal recordCosttotal;
    /**
     * 通道侧 总成本(单位：元) // todo 转成元
     */
    private String recordCosttotalStr;
    /**
     * 运营商类型，0：全网，1：移动，2：联通，3：电信，4：国际
     */
    private String operatorstypeStr;
    /**
     * 归属商务，关联t_sms_user表中id字段
     */
    private Long belongBusiness;
    private String belongBusinessStr;

    public AccessStatisticVO() {
    }

    public AccessStatisticVO(String dateStr, String smstypeStr, String paytypeStr, String successRate, Long successTotal, String salefeeStr, String profit, String profitRate, Integer recordChargeTotal, BigDecimal costprice, String costpriceStr, BigDecimal recordCosttotal, String recordCosttotalStr, String operatorstypeStr, Long belongBusiness, String belongBusinessStr) {
        this.dateStr = dateStr;
        this.smstypeStr = smstypeStr;
        this.paytypeStr = paytypeStr;
        this.successRate = successRate;
        this.successTotal = successTotal;
        this.salefeeStr = salefeeStr;
        this.profit = profit;
        this.profitRate = profitRate;
        this.recordChargeTotal = recordChargeTotal;
        this.costprice = costprice;
        this.costpriceStr = costpriceStr;
        this.recordCosttotal = recordCosttotal;
        this.recordCosttotalStr = recordCosttotalStr;
        this.operatorstypeStr = operatorstypeStr;
        this.belongBusiness = belongBusiness;
        this.belongBusinessStr = belongBusinessStr;
    }

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
    /**
     * 成功率 % (3/1+3+6)
     */
    public String getSuccessRate() {
        int temp = CheckNullUtils.check(getSubmitsuccess()) + CheckNullUtils.check(getReportsuccess()) + CheckNullUtils.check(getReportfail());
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
     * 成功量 (2+3)
     */
    public Long getSuccessTotal() {
        if(successTotal != null){
            return successTotal;
        }
        long successTotal = getSubmitsuccess() + getReportsuccess();
        return successTotal;
    }

    public void setSuccessTotal(Long successTotal) {
        this.successTotal = successTotal;
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
    /**
     * 毛利 (消费金额-通道成本)
     */
    public String getProfit() {
        profit = CheckNullUtils.check(getSalefee()).subtract(CheckNullUtils.check(getRecordCosttotal())).divide(BigDecimal.valueOf(1000)).setScale(2,BigDecimal.ROUND_DOWN).toString();
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
        BigDecimal rate = getSalefee().subtract(getRecordCosttotal())
                .divide( ByZeroUtil.divideExcludeZero(getSalefee()),5,BigDecimal.ROUND_DOWN)
                .multiply(new BigDecimal("100"));
        profitRate = new StringBuilder(rate.setScale(1,BigDecimal.ROUND_DOWN).toString()).append("%").toString();
        return profitRate;
    }

    public void setProfitRate(String profitRate) {
        this.profitRate = profitRate;
    }
    /**
     * 通道计费数（条）（1+2+3）（通道侧）
     */
    public Integer getRecordChargeTotal() {
        if (recordChargeTotal == null){
            recordChargeTotal = 0;
        }
        return recordChargeTotal;
    }

    public void setRecordChargeTotal(Integer recordChargeTotal) {
        this.recordChargeTotal = recordChargeTotal;
    }

    /**
     * 通道单价，单位：厘
     */
    public BigDecimal getCostprice() {
        if(costprice == null){
            costprice = BigDecimal.ZERO;
        }
        return costprice;
    }

    public void setCostprice(BigDecimal costprice) {
        this.costprice = costprice;
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
     * 通道侧 总成本(单位：厘)
     */
    public BigDecimal getRecordCosttotal() {
        if(recordCosttotal == null){
            recordCosttotal = BigDecimal.ZERO;
        }
        return recordCosttotal;
    }

    public void setRecordCosttotal(BigDecimal recordCosttotal) {
        this.recordCosttotal = recordCosttotal;
    }
    /**
     * 通道侧 总成本(单位：元) // todo 转成元
     */
    public String getRecordCosttotalStr() {
        if(recordCosttotalStr == null){
            recordCosttotalStr = getRecordCosttotal().divide(BigDecimal.valueOf(1000)).setScale(2, BigDecimal.ROUND_DOWN).toString();
        }
        return recordCosttotalStr;
    }

    public void setRecordCosttotalStr(String recordCosttotalStr) {
        this.recordCosttotalStr = recordCosttotalStr;
    }

    public String getOperatorstypeStr() {
        return OperatorType.getDescByValue(getOperatorstype());
    }

    public void setOperatorstypeStr(String operatorstypeStr) {
        this.operatorstypeStr = operatorstypeStr;
    }

    public Long getBelongBusiness() {
        return belongBusiness;
    }

    public void setBelongBusiness(Long belongBusiness) {
        this.belongBusiness = belongBusiness;
    }

    public String getBelongBusinessStr() {
        return belongBusinessStr;
    }

    public void setBelongBusinessStr(String belongBusinessStr) {
        this.belongBusinessStr = belongBusinessStr;
    }

    public static AccessStatisticVO init(){
        return new AccessStatisticVO(null, null, null,null, 0L, null, null, null, 0,BigDecimal.ZERO,null, BigDecimal.ZERO,null,null,0L, null);
    }
}
