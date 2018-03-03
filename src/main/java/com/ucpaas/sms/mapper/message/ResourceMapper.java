package com.ucpaas.sms.mapper.message;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ucpaas.sms.entity.message.Resource;
import com.ucpaas.sms.model.Page;

@Repository
public interface ResourceMapper {
	Resource get(String id);

	List<Resource> findAllList(Map params);

	Integer getCount(Map params);

	List<Resource> findList(Map params);

	int insert(Resource entity);

	int update(Resource entity);

	int updateStatus(Resource resource);

	/**
	 * 获取当天最大的ID序列，可能为空
	 * 
	 */
	int getMaxIdSeq();

	int getChannelCount(String channelId);

	List<Resource> findResourceByIds(List<String> ids);

	int getOpenChannelCountByChannelId(String channelId);
	
	List<Resource> queryList(Page page);
}