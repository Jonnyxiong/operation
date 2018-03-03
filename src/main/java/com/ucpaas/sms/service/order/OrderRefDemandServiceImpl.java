package com.ucpaas.sms.service.order;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucpaas.sms.entity.message.OrderRefDemand;
import com.ucpaas.sms.mapper.message.OrderRefDemandMapper;
import com.ucpaas.sms.model.Page;

/**
 * @description 订单资源关联表
 * @author huangwenjie
 * @date 2017-06-29
 */
@Service
public class OrderRefDemandServiceImpl implements OrderRefDemandService{

    @Autowired
    private OrderRefDemandMapper orderRefDemandMapper;
    
    @Override
	@Transactional("message")
    public int insert(OrderRefDemand model) {
        return this.orderRefDemandMapper.insert(model);
    }

    @Override
	@Transactional("message")
    public int insertBatch(List<OrderRefDemand> modelList) {
        return this.orderRefDemandMapper.insertBatch(modelList);
    }

    @Override
	@Transactional("message")
    public int delete(Integer id) {
		OrderRefDemand model = this.orderRefDemandMapper.getById(id);
		if(model != null)
        	return this.orderRefDemandMapper.delete(id);
		return 0;
    }

	@Override
	@Transactional("message")
    public int update(OrderRefDemand model) {
		OrderRefDemand old = this.orderRefDemandMapper.getById(model.getId());
		if(old == null){
			return 0;
		}
		return this.orderRefDemandMapper.update(model); 
    }

    @Override
	@Transactional("message")
    public int updateSelective(OrderRefDemand model) {
		OrderRefDemand old = this.orderRefDemandMapper.getById(model.getId());
		if(old != null)
        	return this.orderRefDemandMapper.updateSelective(model);
		return 0;
    }

    @Override
	@Transactional("message")
    public OrderRefDemand getById(Integer id) {
        OrderRefDemand model = this.orderRefDemandMapper.getById(id);
		return model;
    }

    @Override
	@Transactional("message")
    public Page queryList(Page page) {
        List<OrderRefDemand> list = this.orderRefDemandMapper.queryList(page);
        page.setData(list);
        return page;
    }

    @Override
	@Transactional("message")
    public int count(Map<String,Object> params) {
		return this.orderRefDemandMapper.count(params);
    }
    
}
