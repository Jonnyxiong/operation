package com.ucpaas.sms.service;

import com.ucpaas.sms.model.PageContainer;

import java.util.List;
import java.util.Map;

public interface CustomerSendRecordService {



	List<Map<String, Object>> queryAccountByBelongSale(Map<String, Object> objectMap);


	PageContainer queryAll(Map<String, Object> params);


	PageContainer querySmsSendRecord(Map<String, Object> params);


	List<Map<String, Object>> querySmsRecord4Excel(Map<String, Object> params);


}
