package com.ucpaas.sms.service.order;

import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.entity.message.OrderList;
import com.ucpaas.sms.entity.po.OrderListPo;
import com.ucpaas.sms.model.Page;

import java.util.List;
import java.util.Map;


public interface OrderListService {


    /**
     * 生成订单
     * @param model
     * @return
     */
    public ResultVO insert(OrderList model);
    
    public ResultVO insertBatch(List<OrderList> modelList);
    
    public ResultVO delete(String id);

    /**
     * 根据主键，更新订单全部属性
     * @param model
     * @return
     */
    public ResultVO update(OrderList model);

    /**
     *根据主键编辑订单
     * @param model
     * @return
     */
    public ResultVO updateSelective(OrderList model);

    /**
     * 修改订单的状态
     * @param param
     */
    @Deprecated
    public ResultVO updateOrderState(OrderList param);

    
    public ResultVO getById(String orderId);

    /**
     * 根据主键查询
     * @param orderId
     * @return
     */
    public OrderListPo getPoById(String orderId);
    
    public ResultVO queryList(Page<OrderList> page);

    /**
     * 查询订单列表
     * @param page
     * @return
     */
    public Page queryListNew(Page<OrderListPo> page);
    
    public ResultVO count(Map<String, Object> params);

	Page queryListAndDemanState(Page<OrderListPo> page);


	ResultVO pipeichenggong(String orderId, String[] resourceIds, String remark, Long operatorId);
	
	
	ResultVO huitui(String orderId, String remark, Long operatorId);

	ResultVO daipipei(String orderId, String[] resourceIds, String remark, Long operatorId);

	ResultVO chedan(String orderId, Long operatorId);

	ResultVO faqixuqiu(String orderId, String remark, Long operatorId);

	ResultVO daipeidan(String orderId, String[] resourceIds, Long operatorId);




}
