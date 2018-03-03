package com.ucpaas.sms.service.order;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucpaas.sms.entity.message.OrderRefResource;
import com.ucpaas.sms.mapper.message.OrderRefResourceMapper;
import com.ucpaas.sms.model.Page;

/**
 * @description 订单资源关联表
 * @author huangwenjie
 * @date 2017-06-29
 */
@Service
public class OrderRefResourceServiceImpl implements OrderRefResourceService{

    @Autowired
    private OrderRefResourceMapper orderRefResourceMapper;
    
    @Override
	@Transactional("message")
    public int insert(OrderRefResource model) {
        return this.orderRefResourceMapper.insert(model);
    }

    @Override
	@Transactional("message")
    public int insertBatch(List<OrderRefResource> modelList) {
        return this.orderRefResourceMapper.insertBatch(modelList);
    }

    @Override
	@Transactional("message")
    public int delete(Integer id) {
		OrderRefResource model = this.orderRefResourceMapper.getById(id);
		if(model != null)
        	return this.orderRefResourceMapper.delete(id);
		return 0;
    }

	@Override
	@Transactional("message")
    public int update(OrderRefResource model) {
		OrderRefResource old = this.orderRefResourceMapper.getById(model.getId());
		if(old == null){
			return 0;
		}
		return this.orderRefResourceMapper.update(model); 
    }

    @Override
	@Transactional("message")
    public int updateSelective(OrderRefResource model) {
		OrderRefResource old = this.orderRefResourceMapper.getById(model.getId());
		if(old != null)
        	return this.orderRefResourceMapper.updateSelective(model);
		return 0;
    }

    @Override
	@Transactional("message")
    public OrderRefResource getById(Integer id) {
        OrderRefResource model = this.orderRefResourceMapper.getById(id);
		return model;
    }

    @Override
	@Transactional("message")
    public Page queryList(Page page) {
        List<OrderRefResource> list = this.orderRefResourceMapper.queryList(page);
        page.setData(list);
        return page;
    }

    @Override
	@Transactional("message")
    public int count(Map<String,Object> params) {
		return this.orderRefResourceMapper.count(params);
    }

	@Override
	@Transactional("message")
	public int deleteByOrderId(String orderId) {
		Page<OrderRefResource> page = new Page<>();
		page.setPageSize(999);
		page.getParams().put("orderId", orderId);
        List<OrderRefResource> list = this.orderRefResourceMapper.queryList(page);
        int rows = 0;
		for(OrderRefResource orderRefResource:list){
			rows+=delete(orderRefResource.getId());
		}
		return rows;
	}
    
}
