package com.ucpaas.sms.entity.po;

import com.jsmsframework.record.record.entity.JsmsChannelOperationStatistics;

import java.math.BigDecimal;

/**
 * created by xiaoqingwen on 2018/1/10 20:40
 * 通道分析统计表
 */
public class JsmsChannelOperationStatisticsExt extends JsmsChannelOperationStatistics {

    //通道名称
    private String channelname;
    //用户实际名称
    private String realname;
    //月份的String格式
    private String dateStr;
    //运营商类型0：全网1：移动2：联通3：电信4：国际
    private String operatorstypeStr;
    //通道所属类型，1：自有，2：直连，3：第三方
    private String ownerTypeStr;
    //发送成功率,字符串格式
    private String sendSuccessRatioStr;
    //低消完成率,字符串格式
    private String lowConsumeRatioStr;
    // 投诉率,字符串格式
    private String complaintRatioStr;
    // 投诉系数,字符串格式
    private String complaintCoefficientStr;
    // 投诉差异值,字符串格式
    private String complaintDifferenceStr;
    // 成本价,字符串格式
    private String costpriceStr;

    public String getComplaintRatioStr() {
        return complaintRatioStr;
    }

    public void setComplaintRatioStr(String complaintRatioStr) {
        this.complaintRatioStr = complaintRatioStr;
    }

    public String getComplaintCoefficientStr() {
        return complaintCoefficientStr;
    }

    public void setComplaintCoefficientStr(String complaintCoefficientStr) {
        this.complaintCoefficientStr = complaintCoefficientStr;
    }

    public String getComplaintDifferenceStr() {
        return complaintDifferenceStr;
    }

    public void setComplaintDifferenceStr(String complaintDifferenceStr) {
        this.complaintDifferenceStr = complaintDifferenceStr;
    }

    public String getCostpriceStr() {
        return costpriceStr;
    }

    public void setCostpriceStr(String costpriceStr) {
        this.costpriceStr = costpriceStr;
    }

    public String getSendSuccessRatioStr() {
        return sendSuccessRatioStr;
    }

    public void setSendSuccessRatioStr(String sendSuccessRatioStr) {
        this.sendSuccessRatioStr = sendSuccessRatioStr;
    }

    public String getLowConsumeRatioStr() {
        return lowConsumeRatioStr;
    }

    public void setLowConsumeRatioStr(String lowConsumeRatioStr) {
        this.lowConsumeRatioStr = lowConsumeRatioStr;
    }

    public String getOperatorstypeStr() {
        return operatorstypeStr;
    }

    public void setOperatorstypeStr(String operatorstypeStr) {
        this.operatorstypeStr = operatorstypeStr;
    }

    public String getOwnerTypeStr() {
        return ownerTypeStr;
    }

    public void setOwnerTypeStr(String ownerTypeStr) {
        this.ownerTypeStr = ownerTypeStr;
    }

    public String getDateStr() {
        return dateStr;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

    public String getChannelname() {
        return channelname;
    }

    public void setChannelname(String channelname) {
        this.channelname = channelname;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }
}
