package com.ucpaas.sms.service;

import java.util.List;
import java.util.Map;

import com.ucpaas.sms.entity.message.AgentAccount;
import com.ucpaas.sms.model.Page;

/**
 * @description 代理商帐户表
 * @author huangwenjie
 * @date 2017-07-15
 */
public interface AgentAccountService {

    public int insert(AgentAccount model);
    
    public int insertBatch(List<AgentAccount> modelList);
    
    
    public int update(AgentAccount model);
    
    public int updateSelective(AgentAccount model);
    
    public AgentAccount getByAgentId(Integer agentId);
    
    public Page queryList(Page page);
    
    public int count(Map<String,Object> params);
    
}
