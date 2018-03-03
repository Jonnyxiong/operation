package com.ucpaas.sms.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucpaas.sms.entity.message.AgentAccount;
import com.ucpaas.sms.mapper.message.AgentAccountMapper;
import com.ucpaas.sms.model.Page;

/**
 * @description 代理商帐户表
 * @author huangwenjie
 * @date 2017-07-15
 */
@Service
public class AgentAccountServiceImpl implements AgentAccountService{

    @Autowired
    private AgentAccountMapper agentAccountMapper;
    
    @Override
	@Transactional("message")
    public int insert(AgentAccount model) {
        return this.agentAccountMapper.insert(model);
    }

    @Override
	@Transactional("message")
    public int insertBatch(List<AgentAccount> modelList) {
        return this.agentAccountMapper.insertBatch(modelList);
    }
 

	@Override
	@Transactional("message")
    public int update(AgentAccount model) {
		AgentAccount old = this.agentAccountMapper.getByAgentId(model.getAgentId());
		if(old == null){
			return 0;
		}
		return this.agentAccountMapper.update(model); 
    }

    @Override
	@Transactional("message")
    public int updateSelective(AgentAccount model) {
		AgentAccount old = this.agentAccountMapper.getByAgentId(model.getAgentId());
		if(old != null)
        	return this.agentAccountMapper.updateSelective(model);
		return 0;
    }

    @Override 
	@Transactional("message") 
    public AgentAccount getByAgentId(Integer agentId) {
        AgentAccount model = this.agentAccountMapper.getByAgentId(agentId);
		return model;
    }

    @Override
	@Transactional("message")
    public Page queryList(Page page) {
        List<AgentAccount> list = this.agentAccountMapper.queryList(page);
        page.setData(list);
        return page;
    }

    @Override
	@Transactional("message")
    public int count(Map<String,Object> params) {
		return this.agentAccountMapper.count(params);
    }
    
}
