package com.ucpaas.sms.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucpaas.sms.entity.message.UserRole;
import com.ucpaas.sms.mapper.message.UserRoleMapper;
import com.ucpaas.sms.model.Page;

/**
 * @description 用户角色关系表
 * @author huangwenjie
 * @date 2017-07-15
 */
@Service
public class UserRoleServiceImpl implements UserRoleService{

    @Autowired
    private UserRoleMapper userRoleMapper;
    
    @Override
	@Transactional("message")
    public int insert(UserRole model) {
        return this.userRoleMapper.insert(model);
    }

    @Override
	@Transactional("message")
    public int insertBatch(List<UserRole> modelList) {
        return this.userRoleMapper.insertBatch(modelList);
    } 

	@Override
	@Transactional("message")
    public int update(UserRole model) {
		UserRole old = this.userRoleMapper.getByRuId(model.getRuId());
		if(old == null){
			return 0;
		}
		return this.userRoleMapper.update(model); 
    }

    @Override
	@Transactional("message")
    public int updateSelective(UserRole model) {
		UserRole old = this.userRoleMapper.getByRuId(model.getRuId());
		if(old != null)
        	return this.userRoleMapper.updateSelective(model);
		return 0;
    }

    @Override
	@Transactional("message")
    public UserRole getByRuId(Integer ruId) {
        UserRole model = this.userRoleMapper.getByRuId(ruId);
		return model;
    }

    @Override
	@Transactional("message")
    public Page queryList(Page page) {
        List<UserRole> list = this.userRoleMapper.queryList(page);
        page.setData(list);
        return page;
    }

    @Override
	@Transactional("message")
    public int count(Map<String,Object> params) {
		return this.userRoleMapper.count(params);
    }
    
}
