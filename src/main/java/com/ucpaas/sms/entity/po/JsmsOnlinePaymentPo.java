package com.ucpaas.sms.entity.po;

import com.jsmsframework.finance.entity.JsmsOnlinePayment;
import com.ucpaas.sms.entity.message.Account;
import org.apache.ibatis.type.Alias;

import java.util.Date;

/**
 * Created by lpjLiu on 2017/7/13.
 */
public class JsmsOnlinePaymentPo extends JsmsOnlinePayment {

    private String agentName;
    private String saler;
    private Date payTimeEnd;
    private Date payTimeStart;
    private String idOrAmount;
    private String idOrNameOrSaler;
    private String operation;


    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }

    public Date getPayTimeEnd() {
        return payTimeEnd;
    }

    public void setPayTimeEnd(Date payTimeEnd) {
        this.payTimeEnd = payTimeEnd;
    }

    public Date getPayTimeStart() {
        return payTimeStart;
    }

    public void setPayTimeStart(Date payTimeStart) {
        this.payTimeStart = payTimeStart;
    }

    public String getIdOrAmount() {
        return idOrAmount;
    }

    public void setIdOrAmount(String idOrAmount) {
        this.idOrAmount = idOrAmount;
    }

    public String getIdOrNameOrSaler() {
        return idOrNameOrSaler;
    }

    public void setIdOrNameOrSaler(String idOrNameOrSaler) {
        this.idOrNameOrSaler = idOrNameOrSaler;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getSaler() {
        return saler;
    }

    public void setSaler(String saler) {
        this.saler = saler;
    }

}
