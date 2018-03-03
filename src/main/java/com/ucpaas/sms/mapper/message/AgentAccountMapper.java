package com.ucpaas.sms.mapper.message;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ucpaas.sms.entity.message.AgentAccount;
import com.ucpaas.sms.model.Page;

/**
 * @description 代理商帐户表
 * @author huangwenjie
 * @date 2017-07-15
 */
@Repository
public interface AgentAccountMapper{

	int insert(AgentAccount model);
	
	int insertBatch(List<AgentAccount> modelList);
	
	int update(AgentAccount model);
	
	int updateSelective(AgentAccount model);
	
	AgentAccount getByAgentId(Integer agentId);
	
	List<AgentAccount> queryList(Page<AgentAccount> page);
	
	int count(Map<String,Object> params);

}