package com.ucpaas.sms.dto;

import com.jsmsframework.order.entity.JsmsOemClientPool;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by xiongfenglin on 2017/10/30.
 *
 * @author: xiongfenglin
 */
public class JsmsOemClientPoolDTO extends JsmsOemClientPool{
    private Date endTime;
    private Double remainQuantity;
    private BigDecimal remainAmount;

    @Override
    public BigDecimal getRemainAmount() {
        return remainAmount;
    }

    @Override
    public void setRemainAmount(BigDecimal remainAmount) {
        this.remainAmount = remainAmount;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Double getRemainQuantity() {
        return remainQuantity;
    }

    public void setRemainQuantity(Double remainQuantity) {
        this.remainQuantity = remainQuantity;
    }
}
