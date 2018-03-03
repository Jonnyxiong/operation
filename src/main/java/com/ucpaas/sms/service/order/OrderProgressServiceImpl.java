package com.ucpaas.sms.service.order;

import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.entity.message.OrderProgress;
import com.ucpaas.sms.entity.po.OrderProgressPo;
import com.ucpaas.sms.mapper.message.OrderProgressMapper;
import com.ucpaas.sms.model.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


/**
 * @title 
 * @description 
 * @author jevoncode
 * @date 2017-06-20
 */
@Service
public class OrderProgressServiceImpl implements OrderProgressService{

    @Autowired
    private OrderProgressMapper orderProgressMapper;
    
    @Override
	@Transactional("message")
    public int insert(OrderProgress model) {
        return this.orderProgressMapper.insert(model);
    }

    @Override
	@Transactional("message")
    public ResultVO insertBatch(List<OrderProgress> modelList) {
        this.orderProgressMapper.insertBatch(modelList);
		return ResultVO.successDefault();
    }

    @Override
	@Transactional("message")
    public ResultVO delete(Long id) {
		OrderProgress model = this.orderProgressMapper.getById(id);
		if(model != null)
        	this.orderProgressMapper.delete(id);
		return ResultVO.successDefault();
    }

	@Override
	@Transactional("message")
    public ResultVO update(OrderProgress model) {
		OrderProgress old = this.orderProgressMapper.getById(Long.valueOf(model.getId().toString()));
		if(old == null){
			return ResultVO.failure();
		}
		this.orderProgressMapper.update(model);
		OrderProgress newModel = this.orderProgressMapper.getById(Long.valueOf(model.getId().toString()));
		return ResultVO.successDefault(newModel);
    }

    @Override
	@Transactional("message")
    public ResultVO updateSelective(OrderProgress model) {
		OrderProgress old = this.orderProgressMapper.getById(Long.valueOf(model.getId().toString()));
		if(old != null)
        	this.orderProgressMapper.updateSelective(model);
		return ResultVO.successDefault();
    }

    @Override
	@Transactional("message")
    public ResultVO getById(Long id) {
        OrderProgress model = this.orderProgressMapper.getById(id);
		return ResultVO.successDefault(model);
    }

    @Override
	@Transactional("message")
    public ResultVO queryList(Page<OrderProgress> page) {
        List<OrderProgress> list = this.orderProgressMapper.queryList(page);
        return ResultVO.successDefault(list);
    }

    @Override
    public List<OrderProgressPo> queryOrderProgressList(String orderId) {
        List<OrderProgressPo> list = this.orderProgressMapper.queryOrderProgressList(orderId);
        return list;
    }

    @Override
	@Transactional("message")
    public ResultVO count(Map<String,Object> params) {
		return ResultVO.successDefault(this.orderProgressMapper.count(params));
    }
    
}
