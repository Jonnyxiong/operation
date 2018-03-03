package com.ucpaas.sms.service.order;

import java.util.List;
import java.util.Map;

import com.ucpaas.sms.entity.message.OrderRefResource;
import com.ucpaas.sms.model.Page;

/**
 * @description 订单资源关联表
 * @author huangwenjie
 * @date 2017-06-29
 */
public interface OrderRefResourceService {

    public int insert(OrderRefResource model);
    
    public int insertBatch(List<OrderRefResource> modelList);
    
    public int delete(Integer id);
    
    public int update(OrderRefResource model);
    
    public int updateSelective(OrderRefResource model);
    
    public OrderRefResource getById(Integer id);
    
    public Page queryList(Page page);
    
    public int count(Map<String,Object> params);

	public int deleteByOrderId(String orderId);
    
}
