package com.ucpaas.sms.mapper.recordSlave;


import com.ucpaas.sms.dto.ChannelConsumeVO;
import com.ucpaas.sms.entity.record.RecordConsumeStat;
import com.ucpaas.sms.model.Page;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RecordConsumeStatMapper{

	int insert(RecordConsumeStat recordConsumeStat);
	
	int insertBatch(List<RecordConsumeStat> recordConsumeStatList);
	
	int delete(Long id);
	
	int update(RecordConsumeStat recordConsumeStat);
	
	int updateSelective(RecordConsumeStat recordConsumeStat);
	
	RecordConsumeStat getById(Long id);
	
	List<RecordConsumeStat> queryList(Page<RecordConsumeStat> page);

	List<RecordConsumeStat> querySumList(Page<RecordConsumeStat> page);

	List<ChannelConsumeVO> queryTop(Map params);

	int count(RecordConsumeStat recordConsumeStat);

}