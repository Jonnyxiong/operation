package com.ucpaas.sms.po;

import com.jsmsframework.order.entity.po.JsmsOemAgentPoolPo;

import java.util.List;

/**
 *
 * @outhor tanjiangqiang
 * @create 2017-11-21 20:01
 */
public class RechargePo {

    // 短信充值所需参数
    private List<JsmsOemAgentPoolPo> poolPos;
    // 客户id
    private String clientId;

    public List<JsmsOemAgentPoolPo> getPoolPos() {
        return poolPos;
    }

    public void setPoolPos(List<JsmsOemAgentPoolPo> poolPos) {
        this.poolPos = poolPos;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}