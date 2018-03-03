package com.ucpaas.sms.mapper.message;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.ucpaas.sms.entity.message.ClientIdSequence;

@Repository
public interface ClientIdSequenceMapper {

	ClientIdSequence getUnusedRandom(ClientIdSequence clientIdSequence);

	int lock(String clientId);

	String getMax(@Param("clientidType") Integer clientidType, @Param("prefix") String prefix);

	int batchAdd(@Param("list")List<String> list, @Param("clientIdType") Integer clientIdType);

	int updateStatus(String clientId);

}