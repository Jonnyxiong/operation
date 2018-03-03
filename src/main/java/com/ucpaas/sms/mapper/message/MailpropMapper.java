package com.ucpaas.sms.mapper.message;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Repository;

import com.ucpaas.sms.entity.message.Mailprop;
import com.ucpaas.sms.model.Page;

/**
 * @description 邮件配置表
 * @author huangwenjie
 * @date 2017-07-15
 */
@Repository
public interface MailpropMapper{

	int insert(Mailprop model);
	
	int insertBatch(List<Mailprop> modelList);
	
	int update(Mailprop model);
	
	int updateSelective(Mailprop model);
	
	Mailprop getById(Long id);
	
	List<Mailprop> queryList(Page<Mailprop> page);
	
	int count(Map<String,Object> params);

}