package com.ucpaas.sms.model;

import java.io.Serializable;

/**
 * Created by dylan on 2017/10/20.
 */
public class FailReturnPo implements Serializable{
    private String subId;
    private Integer id;

    public FailReturnPo() {
    }

    public String getSubId() {
        return subId;
    }

    public void setSubId(String subId) {
        this.subId = subId;
    }

    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
}
