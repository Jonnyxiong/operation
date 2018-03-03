package com.ucpaas.sms.mapper.message;


import com.ucpaas.sms.entity.message.Channel;
import com.ucpaas.sms.model.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ChannelMapper{

	int insert(Channel channel);
	
	int insertBatch(List<Channel> channelList);
	
	int delete(Integer id);
	
	int update(Channel channel);
	
	int updateSelective(Channel channel);
	
	Channel getById(Integer id);

	List<Channel> getByChannelIds(@Param("channelIds")Set<Integer> channelIds);

	List<Channel> queryList(Page<Channel> page);
	
	int count(Channel channel);

}