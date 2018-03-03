package com.ucpaas.sms.service.order;

import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.entity.message.OrderProgress;
import com.ucpaas.sms.entity.po.OrderProgressPo;
import com.ucpaas.sms.model.Page;

import java.util.List;
import java.util.Map;

public interface OrderProgressService {

    /**
     * 生成订单进度
     * @param model
     * @return
     */
    public int insert(OrderProgress model);
    
    public ResultVO insertBatch(List<OrderProgress> modelList);
    
    public ResultVO delete(Long id);
    
    public ResultVO update(OrderProgress model);

    public ResultVO updateSelective(OrderProgress model);
    
    public ResultVO getById(Long id);
    
    public ResultVO queryList(Page<OrderProgress> page);

    public List<OrderProgressPo> queryOrderProgressList(String orderId);
    
    public ResultVO count(Map<String, Object> params);
    
}
