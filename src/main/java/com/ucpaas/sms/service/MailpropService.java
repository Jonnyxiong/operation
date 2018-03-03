package com.ucpaas.sms.service;

import java.util.List;
import java.util.Map;

import com.ucpaas.sms.entity.message.Mailprop;
import com.ucpaas.sms.model.Page;

/**
 * @description 邮件配置表
 * @author huangwenjie
 * @date 2017-07-15
 */
public interface MailpropService {

	public int insert(Mailprop model);

	public int insertBatch(List<Mailprop> modelList);

	public int update(Mailprop model);

	public int updateSelective(Mailprop model);

	public Mailprop getById(Long id);

	public Page queryList(Page page);

	public int count(Map<String, Object> params);

}
