package com.ucpaas.sms.service.common;

public interface CommonSeqService {

	String getOrAddId(String prefix);

	boolean updateClientIdStatus(String clientId);
}
