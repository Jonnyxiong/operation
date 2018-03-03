package com.ucpaas.sms.service.audit;

import java.util.Map;

import com.ucpaas.sms.model.PageContainer;

public interface AgentAuditService {
	
	PageContainer query(Map<String, String> params);
	
	Map<String, Object> view(Map<String, String> params); 
	
	Map<String, Object> audit(Map<String, String> params);

}
