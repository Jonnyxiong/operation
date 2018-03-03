package com.ucpaas.sms.dto;

import com.jsmsframework.common.enums.*;
import com.ucpaas.sms.entity.access.ClientFailReturn;
import com.ucpaas.sms.enums.PayType;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.math.BigDecimal;

/**
 * Created by dylan on 2017/10/12.
 */
public class ClientFailReturnVO extends ClientFailReturn{

    // 付费类型, 0:预付费, 1:后付费
    private String paytypeStr;
    // 计费规则，0：提交量，1：成功量，2：明确成功量
    private String chargeRuleStr;
    // 短信类型, 0:通知短信, 4:验证码短信, 5:营销短信, 6:告警短信, 7:USSD, 8:闪信
    private String smstypeStr;
    // 产品类型, 0:行业, 1:营销, 2:国际, 3:验证码, 4:通知, 7:USSD, 8:闪信, 9:挂机短信, 其中0、1、3、4为普通短信，2为国际短信
    private String productTypeStr;
    // 对应运营商, 0:全网, 1:移动, 2:联通, 3:电信, 4:国际 。针对代理商预付费用户
    private String operatorCodeStr;
    // 适用区域, 0:全国, 1:国际 针对代理商预付费用户
    private String areaCodeStr;
    // 到期时间, 针对代理商预付费用户
    private String dueTimeStr;
    // 普通短信单价，单位：元，预付费用户为sub_id对应的单价，后付费用户为（发送日期+短信类型）对应的单价
    private String unitPriceStr;
    // 提交成功数量(状态:1)
//    private Integer submitsuccess;
    // 提交失败数量(状态:4)
//    private Integer submitfail;
    // 明确失败数量(状态:6)
//    private Integer reportfail;
    private Integer failTotal;
    // 返还条数
//    private Integer returnNumber;
    // 退费状态, 0:未退费, 1:已退费
//    private Integer refundState;
    // 统计的数据时间(天)
    private String dateStr;

    // 付费类型, 0:预付费, 1:后付费
    public String getPaytypeStr() {
        paytypeStr = PayType.getDescByValue(getPaytype());
        return paytypeStr;
    }

    public void setPaytypeStr(String paytypeStr) {
        this.paytypeStr = paytypeStr;
    }

    public String getChargeRuleStr() {
        chargeRuleStr = ChargeRuleType.getDescByValue(getChargeRule());
        return chargeRuleStr;
    }

    public void setChargeRuleStr(String chargeRuleStr) {
        this.chargeRuleStr = chargeRuleStr;
    }

    public String getSmstypeStr() {
        smstypeStr = SmsTypeEnum.getDescByValue(getSmstype());
        return smstypeStr;
    }

    public void setSmstypeStr(String smstypeStr) {
        this.smstypeStr = smstypeStr;
    }

    public String getProductTypeStr() {
        productTypeStr = ProductType.getDescByValue(getProductType());
        return productTypeStr;
    }

    public void setProductTypeStr(String productTypeStr) {
        this.productTypeStr = productTypeStr;
    }

    public String getOperatorCodeStr() {
        operatorCodeStr = OperatorType.getDescByValue(getOperatorCode());
        return operatorCodeStr;
    }

    public void setOperatorCodeStr(String operatorCodeStr) {
        this.operatorCodeStr = operatorCodeStr;
    }

    public String getAreaCodeStr() {
        areaCodeStr = AreaCodeEnum.getDescByValue(getAreaCode());
        return areaCodeStr;
    }

    public void setAreaCodeStr(String areaCodeStr) {
        this.areaCodeStr = areaCodeStr;
    }

    public String getDueTimeStr() {
        dueTimeStr = DateFormatUtils.format(getDueTime(), "yyyy-MM-dd");
        return dueTimeStr;
    }

    public void setDueTimeStr(String dueTimeStr) {
        this.dueTimeStr = dueTimeStr;
    }

    public String getUnitPriceStr() {
        if(getUnitPrice() != null){
            unitPriceStr = getUnitPrice().setScale(4, BigDecimal.ROUND_DOWN).toString();
        }
        return unitPriceStr;
    }

    public void setUnitPriceStr(String unitPriceStr) {
        this.unitPriceStr = unitPriceStr;
    }

    public Integer getFailTotal() {
        failTotal = getReportfail() + getSubmitfail();
        return failTotal;
    }

    public void setFailTotal(Integer failTotal) {
        this.failTotal = failTotal;
    }

    public String getDateStr() {

        if(getDate() != null){
            StringBuilder stringBuilder = new StringBuilder(getDate().toString());
            stringBuilder = stringBuilder.insert(4, "-");
            stringBuilder = stringBuilder.insert(7, "-");
            dateStr = stringBuilder.toString();
        }
        return dateStr;
    }

    public void setDateStr(String dateStr) {
        this.dateStr = dateStr;
    }

}
