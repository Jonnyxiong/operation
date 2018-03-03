package com.ucpaas.sms.service;

import java.util.List;
import java.util.Map;

import com.ucpaas.sms.entity.message.UserRole;
import com.ucpaas.sms.model.Page;

/**
 * @description 用户角色关系表
 * @author huangwenjie
 * @date 2017-07-15
 */
public interface UserRoleService {

    public int insert(UserRole model);
    
    public int insertBatch(List<UserRole> modelList);
    
    
    public int update(UserRole model);
    
    public int updateSelective(UserRole model);
    
    public UserRole getByRuId(Integer ruId);
    
    public Page queryList(Page page);
    
    public int count(Map<String,Object> params);
    
}
