package com.ucpaas.sms.mapper.message;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ucpaas.sms.entity.message.UserRole;
import com.ucpaas.sms.model.Page;

/**
 * @description 用户角色关系表
 * @author huangwenjie
 * @date 2017-07-15
 */
@Repository
public interface UserRoleMapper{

	int insert(UserRole model);
	
	int insertBatch(List<UserRole> modelList);
	
	int update(UserRole model);
	
	int updateSelective(UserRole model);
	
	UserRole getByRuId(Integer ruId);
	
	List<UserRole> queryList(Page<UserRole> page);
	
	int count(Map<String,Object> params);

}