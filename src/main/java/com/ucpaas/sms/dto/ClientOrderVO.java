package com.ucpaas.sms.dto;

import com.jsmsframework.common.enums.AreaCodeEnum;
import com.jsmsframework.common.enums.OperatorType;
import com.jsmsframework.common.enums.ProductType;
import com.jsmsframework.order.entity.JsmsClientOrder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Created by dylan on 2017/8/17.
 */
public class ClientOrderVO extends JsmsClientOrder implements Serializable{

    private String subIdStr;
    private String orderTypeStr;

    private String productTypeStr;

    private String operatorCodeStr;

    private String areaCodeStr;

    private String productCode;
    private Integer agentType;

    /**
     * 产品总售价
     */
    private String salePriceStr;
    /**
     * 产品总成本价
     */
    private String productCostStr;

    private String quantityStr;

    private String remainQuantityStr;

    private String statusStr;

    private String activePeriodStr;

    private String endTimeStr;

    private String createTimeStr;

    public String getSubIdStr() {
        if(getSubId() != null){
            subIdStr = getSubId().toString();
        }
        return subIdStr;
    }

    public void setSubIdStr(String subIdStr) {
        this.subIdStr = subIdStr;
    }

    public String getOrderTypeStr() {
        if(getOrderType() == null){
        }else if (getOrderType().equals(0)){
            orderTypeStr = "客户购买";
        }else if (getOrderType().equals(1)){
            orderTypeStr = "代理商代买";
        }else if (getOrderType().equals(2)){
            orderTypeStr = "运营代买";
        }
        return orderTypeStr;
    }

    public void setOrderTypeStr(String orderTypeStr) {
        this.orderTypeStr = orderTypeStr;
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

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getSalePriceStr() {
        if(getSalePrice() != null){
            salePriceStr = getSalePrice().setScale(4, BigDecimal.ROUND_DOWN).toString();
        }
        return salePriceStr;
    }

    public void setSalePriceStr(String salePriceStr) {
        this.salePriceStr = salePriceStr;
    }

    public String getProductCostStr() {
        if(getProductCost() != null){
            productCostStr = getProductCost().setScale(4, BigDecimal.ROUND_DOWN).toString();
        }
        return productCostStr;
    }

    public void setProductCostStr(String productCostStr) {
        this.productCostStr = productCostStr;
    }

    public String getQuantityStr() {
        if(getProductType() == null || getQuantity() == null){
        }else if(getProductType().equals(2)){
            quantityStr = new StringBuilder(getQuantity().setScale(4,BigDecimal.ROUND_DOWN).toString()).append("元").toString();
        }else{
            quantityStr = new StringBuilder(getQuantity().setScale(0,BigDecimal.ROUND_DOWN).toString()).append("条").toString();
        }
        return quantityStr;
    }

    public void setQuantityStr(String quantityStr) {
        this.quantityStr = quantityStr;
    }

    public String getRemainQuantityStr() {
        if(getRemainQuantity() == null || getRemainQuantity() == null){
        }else if(getProductType().equals(2)){
            remainQuantityStr = new StringBuilder(getRemainQuantity().setScale(4,BigDecimal.ROUND_DOWN).toString()).append("元").toString();
        }else{
            remainQuantityStr = new StringBuilder(getRemainQuantity().setScale(0,BigDecimal.ROUND_DOWN).toString()).append("条").toString();
        }
        return remainQuantityStr;
    }

    public void setRemainQuantityStr(String remainQuantityStr) {
        this.remainQuantityStr = remainQuantityStr;
    }

    public String getStatusStr() {
        //状态， 0：待审核，1：订单生效，2：订单完成（订单中数量已用完），3：订单失败（代理商余额不足）,4：订单取消（客户取消订单）
        if (getStatus() == null) {
        }else if (getStatus().equals(0)){
            statusStr = "待审核";
        }else if (getStatus().equals(1)){
            statusStr = "订单生效";
        }else if (getStatus().equals(2)){
            statusStr = "订单完成";
        }else if (getStatus().equals(3)){
            statusStr = "订单失败";
        }else if (getStatus().equals(4)){
            statusStr = "订单取消";
        }
        return statusStr;
    }

    public void setStatusStr(String statusStr) {
        this.statusStr = statusStr;
    }

    public String getActivePeriodStr() {
        if(getActivePeriod() == null){
            activePeriodStr = "无限期";
        }else if(getActivePeriod().equals(0)){
            activePeriodStr = "无限期";
        }else{
            activePeriodStr = new StringBuilder(getActivePeriod().toString()).append("天").toString();
//            activePeriodStr = getActivePeriod() + "天";
        }
        return activePeriodStr;
    }

    public void setActivePeriodStr(String activePeriodStr) {
        this.activePeriodStr = activePeriodStr;
    }

    public String getEndTimeStr() {
        if(getEndTime() != null){
            endTimeStr = DateFormatUtils.format(getEndTime(), "yyyy-MM-dd HH:mm:ss");
        }
        return endTimeStr;
    }

    public void setEndTimeStr(String endTimeStr) {
        this.endTimeStr = endTimeStr;
    }

    public String getCreateTimeStr() {
        if(getCreateTime() != null){
            createTimeStr = DateFormatUtils.format(getCreateTime(), "yyyy-MM-dd HH:mm:ss");
        }
        return createTimeStr;
    }

    public void setCreateTimeStr(String createTimeStr) {
        this.createTimeStr = createTimeStr;
    }

    public Integer getAgentType() {
        return agentType;
    }

    public void setAgentType(Integer agentType) {
        this.agentType = agentType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("orderTypeStr", orderTypeStr)
                .append("productTypeStr", productTypeStr)
                .append("operatorCodeStr", operatorCodeStr)
                .append("areaCodeStr", areaCodeStr)
                .append("productCode", productCode)
                .append("salePriceStr", salePriceStr)
                .append("productCostStr", productCostStr)
                .append("quantityStr", quantityStr)
                .append("remainQuantityStr", remainQuantityStr)
                .append("statusStr", statusStr)
                .append("activePeriodStr", activePeriodStr)
                .append("endTimeStr", endTimeStr)
                .append("createTimeStr", createTimeStr)
                .toString();
    }
}
