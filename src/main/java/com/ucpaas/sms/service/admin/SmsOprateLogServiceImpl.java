package com.ucpaas.sms.service.admin;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucpaas.sms.dao.MessageMasterDao;
import com.ucpaas.sms.model.PageContainer;
@Service
@Transactional
public class SmsOprateLogServiceImpl implements SmsOprateLogService{
	@Autowired
	private MessageMasterDao masterDao;
	@Override
	public PageContainer query(Map<String, String> params) {
		return masterDao.getSearchPage("smsOprateLog.query", "smsOprateLog.queryCount", params);
	}
	@Override
	public List<Map<String, Object>> queryModuleList() {
		return masterDao.getSearchList("smsOprateLog.queryModuleList", null);
	}
	
}
