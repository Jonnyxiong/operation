package com.ucpaas.sms.dto;

import com.jsmsframework.common.enums.AreaCodeEnum;
import com.jsmsframework.common.enums.OperatorType;
import com.jsmsframework.common.enums.ProductType;
import com.jsmsframework.finance.entity.JsmsClientConsumerList;
import com.ucpaas.sms.util.IntConvertDateUntil;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by dylan on 2017/10/17.
 */
public class ClientConsumerListVO extends JsmsClientConsumerList implements Serializable{
    // 用户账号,关联t_sms_account表中clientid字段
    private String clientName;
    // 操作烈性, 0:短信失败返还
    private String operateTypeStr;
    // 产品类型, 0:行业, 1:营销, 2:国际, 3:验证码, 4:通知, 7:USSD, 8:闪信, 9:挂机短信 ;其中0、1、3、4为普通短信, 2为国际短信
    private String productTypeStr;
    // 对应运营商, 0:全网, 1:移动, 2:联通, 3:电信, 4:国际
    private String operatorCodeStr;
    // 使用区域, 0:全国, 1:国际
    private String areaCodeStr;
    // 短信单价, 单位:元
    private String unitPriceStr;
    // 到期时间
    private String dueTimeStr;
    // 短信条数, 单价:条
    private String smsNumberStr;
    // 消费日期
    private String consumerDateStr;
    // 操作者, 对应t_sms_user表中id字段
    private String operatorStr;
    // 操作时间
    private String operateTimeStr;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getOperateTypeStr() {
        operateTypeStr = OperateType.getDescByValue(getOperateType());
        return operateTypeStr;
    }

    public void setOperateTypeStr(String operateTypeStr) {
        this.operateTypeStr = operateTypeStr;
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

    public String getUnitPriceStr() {
        if (getUnitPrice() != null) {
            unitPriceStr = getUnitPrice().setScale(4, BigDecimal.ROUND_DOWN).toString();
        }
        return unitPriceStr;
    }

    public void setUnitPriceStr(String unitPriceStr) {
        this.unitPriceStr = unitPriceStr;
    }

    public String getDueTimeStr() {
        if(getDueTime() != null) {
            dueTimeStr = DateFormatUtils.format(getDueTime(), "yyyy-MM-dd");
        }
        return dueTimeStr;
    }

    public void setDueTimeStr(String dueTimeStr) {
        this.dueTimeStr = dueTimeStr;
    }

    public String getSmsNumberStr() {
        if(OperateType.短信失败返还.getValue().equals(getOperateType())
                && !ProductType.国际.getValue().equals(getProductType())){
            smsNumberStr = new StringBuilder("+").append(getSmsNumber()).append("条").toString();
        }
        return smsNumberStr;
    }

    public void setSmsNumberStr(String smsNumberStr) {
        this.smsNumberStr = smsNumberStr;
    }

    public String getConsumerDateStr() {
        if (getConsumerDate() != null){
            consumerDateStr = IntConvertDateUntil.convertToString(getConsumerDate());
        }
        return consumerDateStr;
    }

    public void setConsumerDateStr(String consumerDateStr) {
        this.consumerDateStr = consumerDateStr;
    }

    public String getOperatorStr() {
        return operatorStr;
    }

    public void setOperatorStr(String operatorStr) {
        this.operatorStr = operatorStr;
    }

    public String getOperateTimeStr() {
        if(getOperateTime() != null){
            operateTimeStr = DateFormatUtils.format(getOperateTime(), "yyyy-MM-dd HH:mm");
        }
        return operateTimeStr;
    }

    public void setOperateTimeStr(String operateTimeStr) {
        this.operateTimeStr = operateTimeStr;
    }


    private enum OperateType {

        短信失败返还(0, "短信失败返还");

        private Integer value;
        private String desc;

        OperateType(Integer value, String desc) {
            this.value = value;
            this.desc = desc;
        }

        public Integer getValue() {
            return value;
        }

        public String getDesc() {
            return desc;
        }

        public static String getDescByValue(Integer value) {
            if (value == null) {
                return null;
            }
            String result = null;
            for (OperateType s : OperateType.values()) {
                if (value == s.getValue()) {
                    result = s.getDesc();
                    break;
                }
            }
            return result;
        }
    }

}
