package com.ucpaas.sms.entity.po;

import com.ucpaas.sms.entity.message.OrderProgress;

/**
 * Created by huangzaizheng on 2017/6/21.
 */
public class OrderProgressPo extends OrderProgress {

    private String operatorName;

    private String stateName;

    public String getOperatorName() {
        return operatorName;
    }

    public void setOperatorName(String operatorName) {
        this.operatorName = operatorName;
    }


    public String getStateName() {
        return stateName;
    }

    public void setStateName(String stateName) {
        this.stateName = stateName;
    }
}
