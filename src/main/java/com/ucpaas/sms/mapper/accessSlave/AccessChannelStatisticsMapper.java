package com.ucpaas.sms.mapper.accessSlave;


import com.ucpaas.sms.entity.access.AccessChannelStatistics;
import com.ucpaas.sms.model.Page;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AccessChannelStatisticsMapper{

	int insert(AccessChannelStatistics accessChannelStatistics);
	
	int insertBatch(List<AccessChannelStatistics> accessChannelStatisticsList);
	
	int delete(Long id);
	
	int update(AccessChannelStatistics accessChannelStatistics);
	
	int updateSelective(AccessChannelStatistics accessChannelStatistics);
	
	AccessChannelStatistics getById(Long id);
	
	List<AccessChannelStatistics> queryList(Page<AccessChannelStatistics> page);
	
	int count(AccessChannelStatistics accessChannelStatistics);

	List<AccessChannelStatistics> queryListByClientids(Page<AccessChannelStatistics> params);

	List<AccessChannelStatistics> querySumByClientids(Page<AccessChannelStatistics> params);

}