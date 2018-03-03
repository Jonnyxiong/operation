package com.ucpaas.sms.dto;

import com.jsmsframework.common.enums.AreaCodeEnum;
import com.jsmsframework.common.enums.OperatorType;
import com.jsmsframework.common.enums.ProductType;
import com.jsmsframework.order.entity.JsmsOemClientOrder;
import com.jsmsframework.order.enums.OEMClientOrderType;
import com.ucpaas.sms.util.IntConvertDateUntil;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by dylan on 2017/8/17.
 */
public class OemClientOrderVO extends JsmsOemClientOrder implements Serializable {
    // 用户账号,关联t_sms_account表中clientid字段
    private String clientName;
    // 产品类型，0：行业，1：营销，2：国际
    private String productTypeStr;
    //对应运营商，0：全网，1：移动，2：联通，3：电信，4：国际 所有订单类型时都有值
    private String operatorCodeStr;
    //适用区域，0：全国，1：国际 所有订单类型时都有值
    private String areaCodeStr;
    // 订单类型，1：OEM代理商分发，2：OEM代理商回退
    private String orderTypeStr;
    // 普通订单短信条数，单位：条
    private String orderNumberStr;
    // 普通订单短信单价，单位：元，普通短信时有值
    private String unitPriceStr;
    // 到期时间
    private String dueTimeStr;
    // 创建时间
    private String createTimeStr;
    // 消费日期
    private String consumerDateStr;
    // 操作者, 对应t_sms_user表中id字段
    private String operatorStr;

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
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

    public String getOrderTypeStr() {
        orderTypeStr = OEMClientOrderType.getDescByValue(getOrderType());
        return orderTypeStr;
    }

    public void setOrderTypeStr(String orderTypeStr) {
        this.orderTypeStr = orderTypeStr;
    }

    public String getOrderNumberStr() {
        if (OEMClientOrderType.短信失败返还.getValue().equals(getOrderType())
                && !ProductType.国际.getValue().equals(getProductType())) {
            orderNumberStr = new StringBuilder("+").append(getOrderNumber()).append("条").toString();
        }
        return orderNumberStr;
    }

    public void setOrderNumberStr(String orderNumberStr) {
        this.orderNumberStr = orderNumberStr;
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
        if (getDueTime() != null) {
            dueTimeStr = DateFormatUtils.format(getDueTime(), "yyyy-MM-dd");
        }
        return dueTimeStr;
    }

    public void setDueTimeStr(String dueTimeStr) {
        this.dueTimeStr = dueTimeStr;
    }

    public String getCreateTimeStr() {
        if (getCreateTime() != null) {
            createTimeStr = DateFormatUtils.format(getCreateTime(), "yyyy-MM-dd HH:mm");
        }
        return createTimeStr;
    }

    public void setCreateTimeStr(String createTimeStr) {
        this.createTimeStr = createTimeStr;
    }

    public String getConsumerDateStr() {
        if (getConsumerDate() != null) {
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


}
