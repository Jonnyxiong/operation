package com.ucpaas.sms.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.ucpaas.sms.constant.DbConstant.DbType;

@Repository
public class CommonDao {
	
	@Autowired
	private MessageMasterDao messageMasterDao;
	
	@Autowired
	private AccessMasterDao accessMasterDao;
	
	@Autowired
	private AccessSlaveDao accessSlaveDao;
	
	@Autowired
	private RecordMasterDao recordMasterDao;

	@Autowired
	private RecordSlaveDao recordSlaveDao;
	
//	@Autowired
//	private StatsMasterDao statsMasterDao;
	

	public BaseDao getDao(DbType dbType) {
		switch (dbType) {
		case ucpaas_message:
			return messageMasterDao;
		case ucpaas_message_access_master:
			return accessMasterDao;
		case ucpaas_message_access_slave:
			return accessSlaveDao;
		case ucpaas_message_record_master:
			return recordMasterDao;
		case ucpaas_message_record_slave:
			return recordSlaveDao;
//		case ucpaas_message_stats_master:
//			return statsMasterDao;
		default:
			return null;
		}
	}

	public MessageMasterDao getMessageMasterDao() {
		return messageMasterDao;
	}

	public AccessMasterDao getAccessMasterDao() {
		return accessMasterDao;
	}
	
	public AccessSlaveDao getAccessSlaveDao() {
		return accessSlaveDao;
	}

	public RecordMasterDao getRecordMasterDao() {
		return recordMasterDao;
	}
	
	public RecordSlaveDao getRecordSlaveDao() {
		return recordSlaveDao;
	}
	
//	public StatsMasterDao getMonitorMasterDao() {
//		return statsMasterDao;
//	}
	
	
	
	

}
