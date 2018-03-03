package com.ucpaas.sms.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dylan on 2017/10/16.
 */
public class SendFailReturnPo implements Serializable{

    private String clientId;

    private List<FailReturnPo> failReturnList;

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public List<FailReturnPo> getFailReturnList() {
        return failReturnList;
    }

    public void setFailReturnList(List<FailReturnPo> failReturnList) {
        this.failReturnList = failReturnList;
    }




}
