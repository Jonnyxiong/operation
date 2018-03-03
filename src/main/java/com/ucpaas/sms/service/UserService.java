package com.ucpaas.sms.service;

import java.util.List;
import java.util.Map;

import com.ucpaas.sms.entity.message.User;
import com.ucpaas.sms.model.Page;

/**
 * @description 用户表
 * @author huangwenjie
 * @date 2017-07-15
 */
public interface UserService {

    public int insert(User model);
    
    public int insertBatch(List<User> modelList);
    
    public int update(User model);
    
    public int updateSelective(User model);
    
    public User getById(Long id);
    
    public Page queryList(Page page);
    
    public List<User> checkByMobile(String mobile, String webId);
    
    public int count(Map<String,Object> params);
    
}
