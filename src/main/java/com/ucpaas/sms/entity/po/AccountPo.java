package com.ucpaas.sms.entity.po;

import org.apache.ibatis.type.Alias;

import com.ucpaas.sms.entity.message.Account;

/**
 * Created by lpjLiu on 2017/7/13.
 */
@Alias("AccountPo")
public class AccountPo extends Account {

    private String paytypeDesc;
    private String userRealName;

    private String statusName;
    private String oauthStatusDesc;

    private String smsfromDesc;
    private String smstypeDesc;

    private String clientTypeDesc;
    private String httpProtocolTypeStr;

    private String needAuditDesc;

    private String agentType;
    private String agentTypeDesc;
    private String needreportDesc;

    private String needextendDesc;
    private String needmoDesc;
    private String extValueStr;
    private String valueStr;
    private Integer flag;
    private Integer ext_value;
    private Integer client_type;


    //计费规则
    private Integer chargeRule;
    //计费规则
    private String chargeRuleStr;

    //下次计费规则
    private Integer nextChargeRule;
    //下次计费规则
    private String nextChargeRuleStr;
    //星级
    private String starLevelStr;
    //星级
    private Integer starLevel;

    private String createtimeStr;
    private String agentIdStr;

    public String getCreatetimeStr() {
        return createtimeStr;
    }

    public void setCreatetimeStr(String createtimeStr) {
        this.createtimeStr = createtimeStr;
    }

    public String getAgentIdStr() {
        return agentIdStr;
    }

    public void setAgentIdStr(String agentIdStr) {
        this.agentIdStr = agentIdStr;
    }

    public String getStarLevelStr() {
        return starLevelStr;
    }

    public void setStarLevelStr(String starLevelStr) {
        this.starLevelStr = starLevelStr;
    }

    public Integer getStarLevel() {
        return starLevel;
    }

    public void setStarLevel(Integer starLevel) {
        this.starLevel = starLevel;
    }

    public Integer getNextChargeRule() {
        return nextChargeRule;
    }

    public void setNextChargeRule(Integer nextChargeRule) {
        this.nextChargeRule = nextChargeRule;
    }

    public String getNextChargeRuleStr() {
        return nextChargeRuleStr;
    }

    public void setNextChargeRuleStr(String nextChargeRuleStr) {
        this.nextChargeRuleStr = nextChargeRuleStr;
    }

    @Override
    public Integer getChargeRule() {
        return chargeRule;
    }

    @Override
    public void setChargeRule(Integer chargeRule) {
        this.chargeRule = chargeRule;
    }

    public String getChargeRuleStr() {
        return chargeRuleStr;
    }

    public void setChargeRuleStr(String chargeRuleStr) {
        this.chargeRuleStr = chargeRuleStr;
    }

    public Integer getClient_type() {
        return client_type;
    }

    public void setClient_type(Integer client_type) {
        this.client_type = client_type;
    }

    public Integer getExt_value() {
        return ext_value;
    }

    public void setExt_value(Integer ext_value) {
        this.ext_value = ext_value;
    }

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        this.flag = flag;
    }

    public String getPaytypeDesc() {
        return paytypeDesc;
    }

    public void setPaytypeDesc(String paytypeDesc) {
        this.paytypeDesc = paytypeDesc;
    }

    public String getUserRealName() {
        return userRealName;
    }

    public void setUserRealName(String userRealName) {
        this.userRealName = userRealName;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
    }

    public String getOauthStatusDesc() {
        return oauthStatusDesc;
    }

    public void setOauthStatusDesc(String oauthStatusDesc) {
        this.oauthStatusDesc = oauthStatusDesc;
    }

    public String getSmsfromDesc() {
        return smsfromDesc;
    }

    public void setSmsfromDesc(String smsfromDesc) {
        this.smsfromDesc = smsfromDesc;
    }

    public String getSmstypeDesc() {
        return smstypeDesc;
    }

    public void setSmstypeDesc(String smstypeDesc) {
        this.smstypeDesc = smstypeDesc;
    }

    public String getClientTypeDesc() {
        return clientTypeDesc;
    }

    public void setClientTypeDesc(String clientTypeDesc) {
        this.clientTypeDesc = clientTypeDesc;
    }

    public String getNeedAuditDesc() {
        return needAuditDesc;
    }

    public void setNeedAuditDesc(String needAuditDesc) {
        this.needAuditDesc = needAuditDesc;
    }

    public String getAgentType() {
        return agentType;
    }

    public void setAgentType(String agentType) {
        this.agentType = agentType;
    }

    public String getAgentTypeDesc() {
        return agentTypeDesc;
    }

    public void setAgentTypeDesc(String agentTypeDesc) {
        this.agentTypeDesc = agentTypeDesc;
    }

    public String getNeedreportDesc() {
        return needreportDesc;
    }

    public void setNeedreportDesc(String needreportDesc) {
        this.needreportDesc = needreportDesc;
    }

    public String getNeedextendDesc() {
        return needextendDesc;
    }

    public void setNeedextendDesc(String needextendDesc) {
        this.needextendDesc = needextendDesc;
    }

    public String getNeedmoDesc() {
        return needmoDesc;
    }

    public void setNeedmoDesc(String needmoDesc) {
        this.needmoDesc = needmoDesc;
    }

    public String getHttpProtocolTypeStr() {
        //HTTPS协议具体分类，0为https json；1为https get/post；2为https tx-json
        if (getHttpProtocolType() == null) {

        } else if (getHttpProtocolType().equals(0)) {
            httpProtocolTypeStr = "https json";
        } else if (getHttpProtocolType().equals(1)) {
            httpProtocolTypeStr = "https get/post";
        } else if (getHttpProtocolType().equals(2)) {
            httpProtocolTypeStr = "https tx-json";
        } else if (getHttpProtocolType().equals(3)) {
            httpProtocolTypeStr = "https get/post jd";
        } else if (getHttpProtocolType().equals(4)) {
            httpProtocolTypeStr = "webservice jd";
        }
        return httpProtocolTypeStr;
    }

    public String getExtValueStr() {
        return extValueStr;
    }

    public void setExtValueStr(String extValueStr) {
        this.extValueStr = extValueStr;
    }

    public void setHttpProtocolTypeStr(String httpProtocolTypeStr) {

        this.httpProtocolTypeStr = httpProtocolTypeStr;
    }

    public String getValueStr() {
        return valueStr;
    }

    public void setValueStr(String valueStr) {
        this.valueStr = valueStr;
    }
}
