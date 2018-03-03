package com.ucpaas.sms.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucpaas.sms.entity.message.Mailprop;
import com.ucpaas.sms.mapper.message.MailpropMapper;
import com.ucpaas.sms.model.Page;

/**
 * @description 邮件配置表
 * @author huangwenjie
 * @date 2017-07-15
 */
@Service
public class MailpropServiceImpl implements MailpropService{

    @Autowired
    private MailpropMapper mailpropMapper;
    
    @Override
	@Transactional("message")
    public int insert(Mailprop model) {
        return this.mailpropMapper.insert(model);
    }

    @Override
	@Transactional("message")
    public int insertBatch(List<Mailprop> modelList) {
        return this.mailpropMapper.insertBatch(modelList);
    }

	@Override
	@Transactional("message")
    public int update(Mailprop model) {
		Mailprop old = this.mailpropMapper.getById(model.getId());
		if(old == null){
			return 0;
		}
		return this.mailpropMapper.update(model); 
    }

    @Override
	@Transactional("message")
    public int updateSelective(Mailprop model) {
		Mailprop old = this.mailpropMapper.getById(model.getId());
		if(old != null)
        	return this.mailpropMapper.updateSelective(model);
		return 0;
    }

    @Override
	@Transactional("message")
    public Mailprop getById(Long id) {
        Mailprop model = this.mailpropMapper.getById(id);
		return model;
    }

    @Override
	@Transactional("message")
    public Page queryList(Page page) {
        List<Mailprop> list = this.mailpropMapper.queryList(page);
        page.setData(list);
        return page;
    }

    @Override
	@Transactional("message")
    public int count(Map<String,Object> params) {
		return this.mailpropMapper.count(params);
    }
    
}
