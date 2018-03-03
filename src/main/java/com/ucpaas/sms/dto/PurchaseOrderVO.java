package com.ucpaas.sms.dto;

import java.io.Serializable;
import java.util.List;

/**
 * Created by dylan on 2017/8/20.
 */
public class PurchaseOrderVO implements Serializable {

    /**
     * 客户id
     */
    private String clientId;
    /**
     * 代理商id
     */
    private Integer agentId;

    private List<PurchaseOrder> purchaseList;


    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public List<PurchaseOrder> getPurchaseList() {
        return purchaseList;
    }

    public Integer getAgentId() {
        return agentId;
    }

    public void setAgentId(Integer agentId) {
        this.agentId = agentId;
    }

    public void setPurchaseList(List<PurchaseOrder> purchaseList) {
        this.purchaseList = purchaseList;
    }
}
