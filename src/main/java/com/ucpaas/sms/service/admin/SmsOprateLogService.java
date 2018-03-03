package com.ucpaas.sms.service.admin;

import java.util.List;
import java.util.Map;

import com.ucpaas.sms.model.PageContainer;

public interface SmsOprateLogService {

	PageContainer query(Map<String, String> formData);

	List<Map<String, Object>> queryModuleList();

}
