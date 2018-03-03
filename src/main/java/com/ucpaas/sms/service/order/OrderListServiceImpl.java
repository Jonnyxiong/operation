package com.ucpaas.sms.service.order;

import java.util.*;

import com.jsmsframework.common.dto.ResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.domain.message.OrderDomain;
import com.ucpaas.sms.entity.message.*;
import com.ucpaas.sms.entity.po.OrderListPo;
import com.ucpaas.sms.enums.OrderProgressDescEnum;
import com.ucpaas.sms.enums.OrderStatusEnum;
import com.ucpaas.sms.enums.ResourceStatus;
import com.ucpaas.sms.exception.OperationException;
import com.ucpaas.sms.exception.OrderException;
import com.ucpaas.sms.mapper.message.OrderListMapper;
import com.ucpaas.sms.model.Page;
import com.ucpaas.sms.util.JacksonUtil;
import com.ucpaas.sms.util.enums.StaticInitVariable;
import com.ucpaas.sms.util.rest.utils.DateUtil;

/**
 * @title
 * @description
 * @author jevoncode
 * @date 2017-06-20
 */
@Service
public class OrderListServiceImpl implements OrderListService {

	private static Logger logger = LoggerFactory.getLogger(OrderListServiceImpl.class);

	@Autowired
	private OrderListMapper orderListMapper;

	@Autowired
	private OrderProgressService orderProgressService;

	@Autowired
	private ResourceService resourceService;

	@Autowired
	private OrderRefResourceService orderRefResourceService;

	@Autowired
	private OrderRefDemandService orderRefDemandService;

	@Override
	@Transactional("message")
	public ResultVO insert(OrderList model) {

		Date now = new Date();
		String orderId = this.getDemandOrderId();
		// 订单状态,0：待配单,1：待匹配,2：待审批（二级审核）,3：退单,4：撤单,5：匹配成功,6：寻资源,7：待审核（销售总监）,8：待审核（订单运营）
		model.setState(Integer.valueOf("0"));
		model.setCreateTime(now);
		model.setUpdateTime(now);
		model.setOrderId(orderId);

		// 生成订单
		int i = this.orderListMapper.insert(model);
		if (i <= 0) {
			throw new OperationException("生成订单失败");
		}

		// 生成进度流水(提交订单)
		OrderProgress progress1 = new OrderProgress();
		progress1.setOrderId(orderId);
		progress1.setOrderProgress(OrderProgressDescEnum.提交订单.toString());
		progress1.setState(Integer.valueOf("0")); // 订单状态,0：待配单,1：待匹配,2：待审批（二级审核）,3：退单,4：撤单,5：匹配成功,6：寻资源,7：待审核（销售总监）,8：待审核（订单运营）
		progress1.setIsAudit(Integer.valueOf("0"));
		progress1.setOperatorId(model.getOperatorId());
		progress1.setCreateTime(now);
		int j = orderProgressService.insert(progress1);
		if (j <= 0) {
			throw new OperationException("生成进度‘提交订单’流水失败");
		}

		return ResultVO.successDefault();
	}

	private synchronized String getDemandOrderId() {
		Date date = new Date();
		int num = 0;

		String orderIdSuf = DateUtil.dateToStr(date, "yyyyMMdd");
		if (orderIdSuf.equals(StaticInitVariable.DEMAND_ORDERID_SUF)) {
			num = StaticInitVariable.DEMAND_ORDER_NUM;
			StaticInitVariable.DEMAND_ORDER_NUM = num + 1;
		} else {
			StaticInitVariable.DEMAND_ORDERID_SUF = orderIdSuf;
			num = 0;
			StaticInitVariable.DEMAND_ORDER_NUM = num + 1;
		}

		// 拼接id
		String orderIdStr = "D" + StringUtils.addZeroForNum(num, 4, "0") + orderIdSuf;
		return orderIdStr;
	}

	@Override
	@Transactional("message")
	public ResultVO insertBatch(List<OrderList> modelList) {
		this.orderListMapper.insertBatch(modelList);
		return ResultVO.successDefault();
	}

	@Override
	@Transactional("message")
	public ResultVO delete(String id) {
		OrderList model = this.orderListMapper.getById(id);
		if (model != null)
			this.orderListMapper.delete(id);
		return ResultVO.successDefault();
	}

	@Override
	@Transactional("message")
	public ResultVO update(OrderList model) {
		OrderList old = this.orderListMapper.getById(model.getOrderId());
		if (old == null) {
			return ResultVO.failure();
		}
		this.orderListMapper.update(model);
		OrderList newModel = this.orderListMapper.getById(model.getOrderId());
		return ResultVO.successDefault(newModel);
	}

	/**
	 * 根据主键编辑订单
	 * 
	 * @param model
	 * @return
	 */
	@Override
	@Transactional("message")
	public ResultVO updateSelective(OrderList model) {
		OrderList old = this.orderListMapper.getById(model.getOrderId());
		int oldState = old.getState();
		if (oldState != 3) {
			return ResultVO.failure("该订单的状态不是退单,不能编辑");
		}

		Date now = new Date();
		OrderDomain orderDomain = new OrderDomain();
		BeanUtils.copyProperties(model, orderDomain);
		orderDomain.setLastUpdateTime(model.getUpdateTime());
		orderDomain.setUpdateTime(now);
		orderDomain.setState(OrderStatusEnum.待配单.getValue());
		if (old != null) {
			int i = this.orderListMapper.updateSelective(orderDomain);
			if (i == 0) {
				return ResultVO.failure("该订单已经更新,请重新操作");
			}
		}

		// 生成进度流水(提交订单)
		OrderProgress progress1 = new OrderProgress();
		progress1.setOrderId(old.getOrderId());
		progress1.setOrderProgress(OrderProgressDescEnum.编辑订单.toString());
		progress1.setState(model.getState()); // 订单状态,0：待配单,1：待匹配,2：待审批（二级审核）,3：退单,4：撤单,5：匹配成功,6：寻资源,7：待审核（销售总监）,8：待审核（订单运营）
		progress1.setIsAudit(Integer.valueOf("0"));// 此进度是否由审核人操作,0：否,1：是
		progress1.setOperatorId(model.getOperatorId());
		progress1.setCreateTime(now);
		int j = orderProgressService.insert(progress1);
		if (j <= 0) {
			throw new OperationException("生成进度‘提交订单’流水失败");
		}

		return ResultVO.successDefault();
	}

	/**
	 * 都什么鬼代码
	 */
	@Override
	@Transactional("message")
	@Deprecated
	public ResultVO updateOrderState(OrderList param) {
		Date now = new Date();
		OrderList model = new OrderList();
		model.setOrderId(param.getOrderId());
		model.setState(param.getState());
		model.setCreateTime(param.getUpdateTime());
		model.setUpdateTime(now);
		int i = this.orderListMapper.updateSelective(model);
		if (i <= 0) {
			return ResultVO.failure("该订单已经更新,请重新操作");
		}

		if (i > 0) {
			// 生成进度流水
			OrderProgress progress1 = new OrderProgress();
			progress1.setOrderId(param.getOrderId());
			progress1.setOrderProgress(backOrderProgressDesc(param.getState()));
			progress1.setState(param.getState());
			progress1.setIsAudit(Integer.valueOf("0"));
			progress1.setOperatorId(param.getOperatorId());
			progress1.setCreateTime(now);
			int j = orderProgressService.insert(progress1);
			if (j <= 0) {
				throw new OperationException("生成进度‘撤单’流水失败");
			}
		}

		// TODO 撤单需要处理资源需求的状态

		return ResultVO.successDefault("更新成功");
	}

	private String backOrderProgressDesc(int state) {
		String str = null;
		if (state == 4) {
			str = "撤单";
		} else {
			str = "其他";
		}
		return str;
	}

	@Override
	@Transactional("message")
	public ResultVO getById(String id) {
		OrderList model = this.orderListMapper.getById(id);
		return ResultVO.successDefault(model);
	}

	@Override
	public OrderListPo getPoById(String orderId) {
		OrderListPo po = this.orderListMapper.getPoById(orderId);
		return po;
	}

	@Override
	@Transactional("message")
	public ResultVO queryList(Page<OrderList> page) {
		List<OrderList> list = this.orderListMapper.queryList(page);
		page.setData(list);
		return ResultVO.successDefault(page);
	}

	@Override
	public Page queryListNew(Page<OrderListPo> page) {
		List<OrderListPo> list = this.orderListMapper.queryListNew(page);
		page.setData(list);
		return page;
	}

	@Override
	public Page queryListAndDemanState(Page<OrderListPo> page) {
		List<OrderListPo> list = this.orderListMapper.queryListNew(page);
		for (OrderListPo po : list) {
			String orderId = po.getOrderId();
			Demand demand = resourceService.getDemandByOrderId(orderId);
			if (demand != null) {
				po.setDemandState(demand.getState());
			}
		}
		page.setData(list);
		return page;
	}

	@Override
	@Transactional("message")
	public ResultVO count(Map<String, Object> params) {
		return ResultVO.successDefault(this.orderListMapper.count(params));
	}

	@Override
	@Transactional("message")
	public ResultVO daipeidan(String orderId, String[] resourceIds, Long operatorId) {
		logger.debug("待配单操作,orderId={},resourceIds={},operatorId={}", orderId, Arrays.toString(resourceIds),
				operatorId);
		ResultVO resultVO = new ResultVO();
		// 1.获取订单信息
		OrderListPo po = getPoById(orderId);
		if (po == null) {
			throw new OrderException("订单不存在");
		}
		// if(resourceIds==null||resourceIds.length==0){
		// throw new OrderException("请选择资源");
		// }

		Date now = new Date();
		logger.debug("原订单信息order={}", JacksonUtil.toJSON(po));
		// 2.修改订单状态
		OrderDomain order = new OrderDomain();
		BeanUtils.copyProperties(po, order);
		order.goToStatus(OrderStatusEnum.待配单);
		order.setUpdateTime(now);
		order.setLastUpdateTime(po.getUpdateTime());
		logger.debug("新订单信息order={}", JacksonUtil.toJSON(order));
		int updateRows = orderListMapper.updateSelective(order);
		if (updateRows == 0) {
			throw new OrderException("订单已被更新过了，请刷新页面后重新操作");
		}

		// 3.清除原关联资源的关联信息
		int deleteRows = orderRefResourceService.deleteByOrderId(orderId);
		logger.debug("删除原关联信息{}条", deleteRows);

		// 4.新增关联信息
		if (resourceIds != null) {
			List<OrderRefResource> refs = new ArrayList<OrderRefResource>();
			for (String resourceId : resourceIds) {
				if (resourceService.getResource(resourceId) == null)
					throw new OrderException("资源不存在，请刷新页面后重试");
				OrderRefResource ref = new OrderRefResource();
				ref.setOrderId(orderId);
				ref.setResourceId(resourceId);
				ref.setCreateTime(now);
				refs.add(ref);
			}
			if (refs != null && refs.size() > 0)
				orderRefResourceService.insertBatch(refs);
			logger.debug("新增关联信息{}", JacksonUtil.toJSON(refs));
		}

		// 5.记录进度
		OrderProgress progress = new OrderProgress();
		progress.setOrderId(order.getOrderId());
		progress.setOrderProgress(order.getStateName());
		progress.setState(order.getState());
		progress.setIsAudit(0);
		progress.setOperatorId(operatorId);
		progress.setCreateTime(now);
		if (po.getState().equals(OrderStatusEnum.寻资源.getValue()))
			progress.setIsShow(0); // 页面是否显示进度，0：否，1：是 从寻资源到待配单，的状态不显示
		logger.debug("记录进度信息{}", JacksonUtil.toJSON(progress));
		orderProgressService.insert(progress);

		resultVO.setSuccess(true);
		resultVO.setMsg("操作成功！");
		logger.info("待配单操作成功");
		return resultVO;
	}

	@Override
	@Transactional("message")
	public ResultVO daipipei(String orderId, String[] resourceIds, String remark, Long operatorId) {
		logger.debug("待匹配操作,orderId={},resourceIds={},operatorId={}", orderId, Arrays.toString(resourceIds),
				operatorId);
		ResultVO resultVO = new ResultVO();
		// 1.获取订单信息
		OrderListPo po = getPoById(orderId);
		if (po == null) {
			throw new OrderException("订单不存在");
		}

		Date now = new Date();
		logger.debug("原订单信息order={}", JacksonUtil.toJSON(po));
		// 2.修改订单状态
		OrderDomain order = new OrderDomain();
		BeanUtils.copyProperties(po, order);
		order.goToStatus(OrderStatusEnum.待匹配);
		order.setUpdateTime(now);
		order.setLastUpdateTime(po.getUpdateTime());
		logger.debug("新订单信息order={}", JacksonUtil.toJSON(order));
		int updateRows = orderListMapper.updateSelective(order);
		if (updateRows == 0) {
			throw new OrderException("订单已被更新过了，请刷新页面后重新操作");
		}

		// 3.清除原关联资源的关联信息
		int deleteRows = orderRefResourceService.deleteByOrderId(orderId);
		logger.debug("删除原关联信息{}条", deleteRows);

		// 4.新增关联信息
		if (resourceIds != null) {
			List<OrderRefResource> refs = new ArrayList<OrderRefResource>();
			for (String resourceId : resourceIds) {
				if (resourceService.getResource(resourceId) == null)
					throw new OrderException("资源不存在，请刷新页面后重试");
				OrderRefResource ref = new OrderRefResource();
				ref.setOrderId(orderId);
				ref.setResourceId(resourceId);
				ref.setCreateTime(now);
				refs.add(ref);
			}
			if (refs != null && refs.size() > 0)
				orderRefResourceService.insertBatch(refs);
			logger.debug("新增关联信息{}", JacksonUtil.toJSON(refs));
		}

		// 5.记录进度
		OrderProgress progress = new OrderProgress();
		progress.setOrderId(order.getOrderId());
		progress.setOrderProgress(order.getStateName());
		progress.setState(order.getState());
		progress.setIsAudit(0);
		progress.setOperatorId(operatorId);
		progress.setCreateTime(now);
		progress.setRemark(remark);
		logger.debug("记录进度信息{}", JacksonUtil.toJSON(progress));
		orderProgressService.insert(progress);

		resultVO.setSuccess(true);
		resultVO.setMsg("操作成功！");
		logger.info("待匹配操作成功");
		return resultVO;
	}

	@Override
	@Transactional("message")
	public ResultVO pipeichenggong(String orderId, String[] resourceIds, String remark, Long operatorId) {
		logger.debug("匹配成功操作,orderId={},resourceIds={},operatorId={}", orderId, Arrays.toString(resourceIds),
				operatorId);
		ResultVO resultVO = new ResultVO();
		// 1.获取订单信息
		OrderListPo po = getPoById(orderId);
		if (po == null) {
			throw new OrderException("订单不存在");
		}
		if (resourceIds == null || resourceIds.length == 0) {
			throw new OrderException("请选择资源");
		}

		logger.debug("原订单信息order={}", JacksonUtil.toJSON(po));
		Date now = new Date();
		// 2.修改订单状态
		OrderDomain order = new OrderDomain();
		BeanUtils.copyProperties(po, order);
		order.goToStatus(OrderStatusEnum.匹配成功);
		order.setUpdateTime(now);
		order.setLastUpdateTime(po.getUpdateTime());
		logger.debug("新订单信息order={}", JacksonUtil.toJSON(order));
		int updateRows = orderListMapper.updateSelective(order);
		if (updateRows == 0) {
			throw new OrderException("订单已被更新过了，请刷新页面后重新操作");
		}

		// 3.清除原关联资源的关联信息
		int deleteRows = orderRefResourceService.deleteByOrderId(orderId);
		logger.debug("删除原关联信息{}条", deleteRows);

		// 4.新增关联信息
		List<OrderRefResource> refs = new ArrayList<OrderRefResource>();
		for (String resourceId : resourceIds) {
			Resource resource = resourceService.getResource(resourceId);
			if (resource == null)
				throw new OrderException("资源不存在，请刷新页面后重试");
			if (resource.getState() == null || !resource.getState().equals(ResourceStatus.已接入.getValue().toString()))
				throw new OrderException("待接入资源不能进行匹配");
			OrderRefResource ref = new OrderRefResource();
			ref.setOrderId(orderId);
			ref.setResourceId(resourceId);
			ref.setCreateTime(now);
			refs.add(ref);
		}
		if (refs != null && refs.size() > 0)
			orderRefResourceService.insertBatch(refs);
		logger.debug("新增关联信息{}", JacksonUtil.toJSON(refs));

		// 5.记录进度
		OrderProgress progress = new OrderProgress();
		progress.setOrderId(order.getOrderId());
		progress.setOrderProgress(order.getStateName());
		progress.setState(order.getState());
		progress.setIsAudit(0);
		progress.setOperatorId(operatorId);
		progress.setCreateTime(now);
		progress.setRemark(remark);
		logger.debug("记录进度信息{}", JacksonUtil.toJSON(progress));
		orderProgressService.insert(progress);

		resultVO.setSuccess(true);
		resultVO.setMsg("操作成功！");
		logger.info("匹配成功操作成功");
		return resultVO;
	}

	@Override
	@Transactional("message")
	public ResultVO huitui(String orderId, String remark, Long operatorId) {
		logger.debug("订单回退操作,orderId={},operatorId={}", orderId, operatorId);
		ResultVO resultVO = new ResultVO();
		// 1.获取订单信息
		OrderListPo po = getPoById(orderId);
		if (po == null) {
			throw new OrderException("订单不存在");
		}

		Date now = new Date();
		logger.debug("原订单信息order={}", JacksonUtil.toJSON(po));
		// 2.修改订单状态
		OrderDomain order = new OrderDomain();
		BeanUtils.copyProperties(po, order);
		order.goToStatus(OrderStatusEnum.退单);
		order.setUpdateTime(now);
		order.setLastUpdateTime(po.getUpdateTime());
		logger.debug("新订单信息order={}", JacksonUtil.toJSON(order));
		int updateRows = orderListMapper.updateSelective(order);
		if (updateRows == 0) {
			throw new OrderException("订单已被更新过了，请刷新页面后重新操作");
		}

		// 3.清除原关联资源的关联信息
		int deleteRows = orderRefResourceService.deleteByOrderId(orderId);
		logger.debug("删除原关联信息{}条", deleteRows);

		// 4.记录进度
		OrderProgress progress = new OrderProgress();
		progress.setOrderId(order.getOrderId());
		progress.setOrderProgress(order.getStateName());
		progress.setState(order.getState());
		progress.setIsAudit(0);
		progress.setOperatorId(operatorId);
		progress.setCreateTime(now);
		progress.setRemark(remark);
		logger.debug("记录进度信息{}", JacksonUtil.toJSON(progress));
		orderProgressService.insert(progress);

		resultVO.setSuccess(true);
		resultVO.setMsg("操作成功！");
		logger.info("订单回退操作成功");
		return resultVO;
	}

	@Override
	@Transactional("message")
	public ResultVO chedan(String orderId, Long operatorId) {
		logger.debug("撤单操作,orderId={},operatorId={}", orderId, operatorId);
		ResultVO resultVO = new ResultVO();
		// 1.获取订单信息
		OrderListPo po = getPoById(orderId);
		if (po == null) {
			throw new OrderException("订单不存在");
		}

		Date now = new Date();
		logger.debug("原订单信息order={}", JacksonUtil.toJSON(po));
		// 2.修改订单状态
		OrderDomain order = new OrderDomain();
		BeanUtils.copyProperties(po, order);
		order.goToStatus(OrderStatusEnum.撤单);
		order.setUpdateTime(now);
		order.setLastUpdateTime(po.getUpdateTime());
		logger.debug("新订单信息order={}", JacksonUtil.toJSON(order));
		int updateRows = orderListMapper.updateSelective(order);
		if (updateRows == 0) {
			throw new OrderException("订单已被更新过了，请刷新页面后重新操作");
		}

		// 3.清除原关联资源的关联信息
		int deleteRows = orderRefResourceService.deleteByOrderId(orderId);
		logger.debug("删除原关联资源信息{}条", deleteRows);

		// 4.关闭需求关联
		Page page = new Page<>();
		page.getParams().put("orderId", orderId);
		List<OrderRefDemand> demanrefs = orderRefDemandService.queryList(page).getData();
		logger.debug("需求关联情况{}", JacksonUtil.toJSON(demanrefs));
		int updateDemanRefRows = 0;
		for (OrderRefDemand ref : demanrefs) {
			updateDemanRefRows += orderRefDemandService.delete(ref.getId());
		}
		logger.debug("关闭原关联需求信息{}条", updateDemanRefRows);

		// 5.设置需求状态
		for (OrderRefDemand ref : demanrefs) {
			logger.debug("将需求置为撤单状态,demand{}", ref.getDemandId());
			resourceService.chedan(ref.getDemandId());
		}

		// 6.记录进度
		OrderProgress progress = new OrderProgress();
		progress.setOrderId(order.getOrderId());
		progress.setOrderProgress(order.getStateName());
		progress.setState(order.getState());
		progress.setIsAudit(0);
		progress.setOperatorId(operatorId);
		progress.setCreateTime(now);
		logger.debug("记录进度信息{}", JacksonUtil.toJSON(progress));
		orderProgressService.insert(progress);

		resultVO.setSuccess(true);
		resultVO.setMsg("操作成功！");
		logger.info("订单回退操作成功");
		return resultVO;
	}

	@Override
	@Transactional("message")
	public ResultVO faqixuqiu(String orderId, String remark, Long operatorId) {
		logger.debug("发起需求操作,orderId={},operatorId={}", orderId, operatorId);
		ResultVO resultVO = new ResultVO();
		// 1.获取订单信息
		OrderListPo po = getPoById(orderId);
		if (po == null) {
			throw new OrderException("订单不存在");
		}

		Date now = new Date();
		logger.debug("原订单信息order={}", JacksonUtil.toJSON(po));
		// 2.修改订单状态
		OrderDomain order = new OrderDomain();
		BeanUtils.copyProperties(po, order);
		order.goToStatus(OrderStatusEnum.寻资源);
		order.setUpdateTime(now);
		order.setLastUpdateTime(po.getUpdateTime());
		logger.debug("新订单信息order={}", JacksonUtil.toJSON(order));
		int updateRows = orderListMapper.updateSelective(order);
		if (updateRows == 0) {
			throw new OrderException("订单已被更新过了，请刷新页面后重新操作");
		}

		// 3.清除原关联资源的关联信息
		int deleteRows = orderRefResourceService.deleteByOrderId(orderId);
		logger.debug("删除原关联资源信息{}条", deleteRows);

		// 4.关闭原需求关联
		Page page = new Page<>();
		page.getParams().put("orderId", orderId);
		List<OrderRefDemand> demanrefs = orderRefDemandService.queryList(page).getData();
		logger.debug("需求关联情况{}", JacksonUtil.toJSON(demanrefs));
		int updateDemanRefRows = 0;
		for (OrderRefDemand ref : demanrefs) {
			updateDemanRefRows += orderRefDemandService.delete(ref.getId());
		}
		logger.debug("关闭原关联需求信息{}条", updateDemanRefRows);

		// 5.设置需求状态
		Demand demand = new Demand();
		demand.setIndustryType(order.getIndustryType());
		demand.setSmsType(order.getSmsType());
		demand.setExpectNumber(order.getExpectNumber());
		demand.setMinimumGuarantee(order.getMinimumGuarantee());
		demand.setChannelType(order.getChannelType());
		demand.setDirectConnect(order.getDirectConnect().toString());
		demand.setExtendSize(order.getExtendSize().toString());
		demand.setRate(order.getRate() == null ? null : order.getRate().toString());
		demand.setSignType(order.getSignType());
		demand.setContentTemplate(order.getContentTemplate());
		demand.setSalePrice(order.getSalePrice());
		demand.setOnlineDate(order.getOnlineDate());
		demand.setState("0");
		demand.setRemark(remark);
		demand.setOperatorId(operatorId.toString());
		demand.setCreateTime(now);
		demand.setUpdateTime(now);
		resourceService.addDemand(demand);
		logger.debug("新增需求{}", JacksonUtil.toJSON(demand));
		OrderRefDemand orderRefDemand = new OrderRefDemand();
		orderRefDemand.setDemandId(demand.getDemandId());
		orderRefDemand.setOrderId(orderId);
		orderRefDemand.setCreateTime(now);
		orderRefDemand.setUpdateTime(now);
		orderRefDemand.setState(1);
		orderRefDemandService.insert(orderRefDemand);
		logger.debug("新增需求关联信息{}", JacksonUtil.toJSON(orderRefDemand));

		// 6.记录进度
		OrderProgress progress = new OrderProgress();
		progress.setOrderId(order.getOrderId());
		progress.setOrderProgress(order.getStateName());
		progress.setState(order.getState());
		progress.setIsAudit(0);
		progress.setOperatorId(operatorId);
		progress.setCreateTime(now);
		progress.setRemark(remark);
		logger.debug("记录进度信息{}", JacksonUtil.toJSON(progress));
		orderProgressService.insert(progress);

		resultVO.setSuccess(true);
		resultVO.setMsg("操作成功！");
		logger.info("发起需求操作成功");
		return resultVO;
	}

}
