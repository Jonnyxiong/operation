package com.ucpaas.sms.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucpaas.sms.entity.message.User;
import com.ucpaas.sms.mapper.message.UserMapper;
import com.ucpaas.sms.model.Page;

/**
 * @description 用户表
 * @author huangwenjie
 * @date 2017-07-15
 */
@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserMapper userMapper;
    
    @Override
	@Transactional("message")
    public int insert(User model) {
        return this.userMapper.insert(model);
    }

    @Override
	@Transactional("message")
    public int insertBatch(List<User> modelList) {
        return this.userMapper.insertBatch(modelList);
    }
 

	@Override
	@Transactional("message")
    public int update(User model) {
		User old = this.userMapper.getById(model.getId());
		if(old == null){
			return 0;
		}
		return this.userMapper.update(model); 
    }

    @Override
	@Transactional("message")
    public int updateSelective(User model) {
		User old = this.userMapper.getById(model.getId());
		if(old != null)
        	return this.userMapper.updateSelective(model);
		return 0;
    }

    @Override
	@Transactional("message")
    public User getById(Long id) {
        User model = this.userMapper.getById(id);
		return model;
    }

    @Override
	@Transactional("message")
    public Page queryList(Page page) {
        List<User> list = this.userMapper.queryList(page);
        page.setData(list);
        return page;
    }

    @Override
	@Transactional("message")
    public int count(Map<String,Object> params) {
		return this.userMapper.count(params);
    }

	@Override
	public List<User> checkByMobile(String mobile, String webId) {
		 List<User> model = this.userMapper.checkByMobile(mobile, webId);
			return model;
	}
    
}
