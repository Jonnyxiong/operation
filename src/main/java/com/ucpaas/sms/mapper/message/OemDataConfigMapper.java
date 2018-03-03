package com.ucpaas.sms.mapper.message;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ucpaas.sms.model.Page;

/**
 * @description OEM资料配置
 * @author huangwenjie
 * @date 2017-07-13
 */
@Repository
public interface OemDataConfigMapper{

	int insert(OemDataConfig model);
	
	int insertBatch(List<OemDataConfig> modelList);
	
	
	int update(OemDataConfig model);
	
	int updateSelective(OemDataConfig model);
	
	OemDataConfig getById(Integer id);
	
	List<OemDataConfig> queryList(Page<OemDataConfig> page);
	
	int count(Map<String,Object> params);

}