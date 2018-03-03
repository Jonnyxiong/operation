package com.ucpaas.sms.service.order;

import java.util.List;
import java.util.Map;

import com.ucpaas.sms.entity.message.OrderRefDemand;
import com.ucpaas.sms.model.Page;

/**
 * @description 订单资源关联表
 * @author huangwenjie
 * @date 2017-06-29
 */
public interface OrderRefDemandService {

    public int insert(OrderRefDemand model);
    
    public int insertBatch(List<OrderRefDemand> modelList);
    
    public int delete(Integer id);
    
    public int update(OrderRefDemand model);
    
    public int updateSelective(OrderRefDemand model);
    
    public OrderRefDemand getById(Integer id);
    
    public Page queryList(Page page);
    
    public int count(Map<String,Object> params);
    
}
