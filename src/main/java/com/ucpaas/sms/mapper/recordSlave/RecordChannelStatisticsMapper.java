package com.ucpaas.sms.mapper.recordSlave;


import com.ucpaas.sms.entity.record.RecordChannelStatistics;
import com.ucpaas.sms.model.Page;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RecordChannelStatisticsMapper{

	int insert(RecordChannelStatistics recordChannelStatistics);
	
	int insertBatch(List<RecordChannelStatistics> recordChannelStatisticsList);
	
	int delete(Long id);
	
	int update(RecordChannelStatistics recordChannelStatistics);
	
	int updateSelective(RecordChannelStatistics recordChannelStatistics);
	
	RecordChannelStatistics getById(Long id);
	
	List<RecordChannelStatistics> queryList(Page<RecordChannelStatistics> page);
	
	int count(RecordChannelStatistics recordChannelStatistics);

	List<RecordChannelStatistics> queryListByClientids(Map<String, Object> params);

	List<RecordChannelStatistics> querySumByClientids(Map<String, Object> params);

}