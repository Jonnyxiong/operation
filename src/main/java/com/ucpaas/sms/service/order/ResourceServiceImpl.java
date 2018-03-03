package com.ucpaas.sms.service.order;

import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.dto.message.ResourceDTO;
import com.ucpaas.sms.entity.message.Demand;
import com.ucpaas.sms.entity.message.OrderRefResource;
import com.ucpaas.sms.entity.message.Resource;
import com.ucpaas.sms.enums.DemandStatus;
import com.ucpaas.sms.enums.ResourceStatus;
import com.ucpaas.sms.exception.OperationException;
import com.ucpaas.sms.mapper.message.DemandMapper;
import com.ucpaas.sms.mapper.message.OrderListMapper;
import com.ucpaas.sms.mapper.message.OrderProgressMapper;
import com.ucpaas.sms.mapper.message.ResourceMapper;
import com.ucpaas.sms.model.Page;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.admin.AdminService;
import com.ucpaas.sms.util.AgentUtils;
import com.ucpaas.sms.util.enums.StaticInitVariable;
import com.ucpaas.sms.util.rest.utils.DateUtil;

/**
 * Created by lpjLiu on 2017/6/20.
 */
@Service
public class ResourceServiceImpl implements ResourceService {
	private static final Logger logger = LoggerFactory.getLogger(ResourceServiceImpl.class);

	@Autowired
	private ResourceMapper resourceMapper;

	@Autowired
	private DemandMapper demandMapper;

	@Autowired
	private OrderListMapper orderListMapper;

	@Autowired
	private OrderProgressMapper orderProgressMapper;

	@Autowired
	private OrderRefResourceService orderRefResourceService;

	@Autowired
	private OrderListService orderListService;
	@Autowired
	private AdminService adminService;

	@Override
	public Resource getResource(String id) {
		return resourceMapper.get(id);
	}

	@Override
	public PageContainer<Resource> findResourceList(Map params) {
		Integer totalCount = resourceMapper.getCount(params);
		if (totalCount == null)
			totalCount = 0;

		PageContainer<Resource> p = new PageContainer<>();
		p.setTotalCount(totalCount);

		AgentUtils.buildPageLimitParams(params, totalCount, p);

		List<Resource> list = resourceMapper.findList(params);
		if (list == null) {
			list = new ArrayList<>();
		} else {
			int count = 0;
			for (Resource resource : list) {
				count++;
				resource.setRowNum(String.valueOf(count));
			}
		}
		p.setEntityList(list);
		return p;
	}

	@Override
	@Transactional("message")
	public int addResource(Resource resource) {
		if (resource == null) {
			return 0;
		}

		// 生产订单
		resource.setResourceId(getResourceId());
		resource.setCreateTime(Calendar.getInstance().getTime());
		resource.setUpdateTime(resource.getCreateTime());
		return resourceMapper.insert(resource);
	}

	@Override
	@Transactional("message")
	public int editResource(Resource resource) {
		if (resource == null) {
			return 0;
		}

		resource.setUpdateTime(Calendar.getInstance().getTime());
		return resourceMapper.update(resource);
	}

	@Override
	@Transactional("message")
	public int updateResourceStatus(Resource resource) {
		if (resource == null) {
			return 0;
		}
		Resource sourceResource = resourceMapper.get(resource.getResourceId());
		if (sourceResource.getState().equals(resource.getState())) {
			throw new OperationException("资源状态已更新，不可重复更新");
		}
		resource.setUpdateTime(Calendar.getInstance().getTime());
		return resourceMapper.updateStatus(resource);
	}

	private synchronized String getResourceId() {
		StringBuilder id = new StringBuilder(StaticInitVariable.RESOURCE_ID_PRE);

		// 获取后缀
		String suffix = DateUtil.dateToStr(Calendar.getInstance().getTime(), 2);
		if (suffix.equals(StaticInitVariable.RESOURCE_ID_SUFFIX)) {
			StaticInitVariable.RESOURCE_ID_SEQ = StaticInitVariable.RESOURCE_ID_SEQ + 1;
		} else {
			StaticInitVariable.RESOURCE_ID_SUFFIX = suffix;
			StaticInitVariable.RESOURCE_ID_SEQ = 0;
		}

		id.append(StringUtils.addZeroForNum(StaticInitVariable.RESOURCE_ID_SEQ, StaticInitVariable.RESOURCE_ID_SEQ_LEN,
				"0"));
		id.append(StaticInitVariable.RESOURCE_ID_SUFFIX);

		// 拼接AgentId
		return id.toString();
	}

	public synchronized String getDemandId() {
		StringBuilder id = new StringBuilder(StaticInitVariable.DEMAND_ID_PRE);

		// 获取后缀
		String suffix = DateUtil.dateToStr(Calendar.getInstance().getTime(), 2);
		if (suffix.equals(StaticInitVariable.DEMAND_ID_SUFFIX)) {
			StaticInitVariable.DEMAND_ID_SEQ = StaticInitVariable.DEMAND_ID_SEQ + 1;
		} else {
			StaticInitVariable.DEMAND_ID_SUFFIX = suffix;
			StaticInitVariable.DEMAND_ID_SEQ = 0;
		}

		id.append(
				StringUtils.addZeroForNum(StaticInitVariable.DEMAND_ID_SEQ, StaticInitVariable.DEMAND_ID_SEQ_LEN, "0"));
		id.append(StaticInitVariable.DEMAND_ID_SUFFIX);

		// 拼接AgentId
		return id.toString();
	}

	@Override
	public boolean hasUsedChannel(String channelId) {
		return resourceMapper.getChannelCount(channelId) > 0;
	}

	@Override
	@Transactional("message")
	public int addDemand(Demand demand) {
		if (demand == null) {
			return 0;
		}

		demand.setDemandId(getDemandId());
		demand.setCreateTime(Calendar.getInstance().getTime());
		demand.setUpdateTime(demand.getCreateTime());
		return demandMapper.insert(demand);
	}

	@Override
	@Transactional("message")
	public int chedan(String demandId) {
		if (StringUtils.isBlank(demandId)) {
			throw new OperationException("更新资源需求状态的参数为空");
		}

		Demand demand = demandMapper.get(demandId);
		if (demand == null) {
			throw new OperationException("待更新资源需求不存在");
		}

		if (StringUtils.isBlank(demand.getState())) {
			throw new OperationException("待更新资源需求数据异常");
		}

		if (DemandStatus.寻资源.toString().equals(demand.getState())) {
			demand.setState(DemandStatus.撤单.toString());
			demand.setLastUpdateTime(demand.getUpdateTime());
			demand.setUpdateTime(Calendar.getInstance().getTime());

			int count = demandMapper.updateStatus(demand);
			if (count <= 0) {
				throw new OperationException("资源需求状态更新失败");
			}
			return count;
		} else {
			return 0;
		}
	}

	@Override
	public Demand getDemand(String id) {
		return demandMapper.get(id);
	}

	@Override
	public PageContainer<Demand> findDemandList(Map params) {
		Integer totalCount = demandMapper.getCount(params);
		if (totalCount == null)
			totalCount = 0;

		PageContainer<Demand> p = new PageContainer<>();
		p.setTotalCount(totalCount);

		AgentUtils.buildPageLimitParams(params, totalCount, p);

		List<Demand> list = demandMapper.findList(params);
		if (list == null) {
			list = new ArrayList<>();
		} else {
			int count = 0;
			for (Demand demand : list) {
				count++;
				demand.setRowNum(String.valueOf(count));
			}
		}
		p.setEntityList(list);
		return p;
	}

	@Override
	@Transactional("message")
	public boolean doSetNoResourceForDemand(Demand demand) {
		// 判断当前需求是否寻资源
		Demand sourceDemand = demandMapper.get(demand.getDemandId());
		if (!sourceDemand.getState().equals(DemandStatus.寻资源.toString())) {
			throw new OperationException("资源需求(" + demand.getDemandId() + ")当前状态不是寻资源");
		}

		Date now = Calendar.getInstance().getTime();

		// 设置需求无资源
		demand.setState(DemandStatus.无资源.toString());
		demand.setUpdateTime(now);
		int updateDemand = demandMapper.updateStatus(demand);
		if (updateDemand <= 0) {
			throw new OperationException("资源需求状态更新为无资源失败");
		}

		// 查询出关联信息订单
		String orderId = demandMapper.getOrderRefById(demand.getDemandId());
		if (StringUtils.isBlank(orderId)) {
			throw new OperationException("资源需求(" + demand.getDemandId() + ")与订单的关联信息错误");
		}

		// 设置订单需求关联状态关闭
		int closeCount = demandMapper.closeOrderRefById(demand.getDemandId());
		if (closeCount <= 0) {
			throw new OperationException("订单与需求关联关闭失败");
		}

		// 设置订单待配单, 改为调用订单提供的方法
		orderListService.daipeidan(orderId, null, Long.parseLong(demand.getOperatorId()));

		// 设置订单待配单
		/*
		 * OrderList orderList = new OrderList(); orderList.setOrderId(orderId);
		 * orderList.setUpdateTime(now);
		 * orderList.setState(Constant.OrderStatus.待配单.getValue()); int
		 * updateOrder = orderListMapper.updateSelective(orderList); if
		 * (updateOrder <= 0) { throw new OperationException("订单状态更新为待配单失败"); }
		 */

		// 生成进度流水(待配单)
		/*
		 * OrderProgress progress = new OrderProgress();
		 * progress.setOrderId(orderId);
		 * progress.setOrderProgress(OrderProgressDescEnum.待配单.toString());
		 * progress.setIsAudit(Integer.valueOf("0"));
		 * progress.setOperatorId(Long.parseLong(demand.getOperatorId()));
		 * progress.setCreateTime(Calendar.getInstance().getTime()); int
		 * insertOrderProgress = this.orderProgressMapper.insert(progress); if
		 * (insertOrderProgress <= 0) { throw new
		 * OperationException("订单进度生成失败"); }
		 */
		return true;
	}

	@Override
	public List<Resource> findResourceListByDemandId(String demandId) {
		List<String> ids = demandMapper.findResourceById(demandId);
		if (ids != null && ids.size() > 0) {
			return resourceMapper.findResourceByIds(ids);
		} else {
			return Lists.newArrayList();
		}
	}

	@Override
	@Transactional("message")
	public boolean doSetHasResourceForDemand(Map<String, Object> params) {
		String demandId = null;
		String operatorId = null;
		List<String> resourceIds = null;

		Object obj;
		obj = params.get("demandId");
		if (obj == null || "".equals(obj.toString())) {
			throw new OperationException("资源需求ID为空");
		}
		demandId = obj.toString();

		obj = params.get("resourceIds");
		if (obj == null) {
			throw new OperationException("选择资源为空");
		}
		resourceIds = (ArrayList<String>) obj;
		if (resourceIds.size() <= 0) {
			throw new OperationException("选择资源为空");
		}

		obj = params.get("operatorId");
		if (obj == null) {
			throw new OperationException("操作员为空");
		}
		operatorId = obj.toString();

		// 判断当前需求是否寻资源
		Demand sourceDemand = demandMapper.get(demandId);
		if (!sourceDemand.getState().equals(DemandStatus.寻资源.toString())) {
			throw new OperationException("资源需求(" + sourceDemand.getDemandId() + ")当前状态不是寻资源");
		}

		Date now = Calendar.getInstance().getTime();

		// 设置需求已供应
		Demand demand = new Demand();
		demand.setDemandId(demandId);
		demand.setState(DemandStatus.已供应.toString());
		demand.setUpdateTime(now);
		int updateDemand = demandMapper.updateStatus(demand);
		if (updateDemand <= 0) {
			throw new OperationException("资源需求状态更新为已供应失败");
		}

		// 批量插入需求-资源关联信息
		params.put("now", now);
		int dCount = demandMapper.batchInsertResourceRef(params);
		if (dCount != resourceIds.size()) {
			throw new OperationException("需求与资源关联添加失败");
		}

		// 查询出关联信息订单
		String orderId = demandMapper.getOrderRefById(demand.getDemandId());
		if (StringUtils.isBlank(orderId)) {
			throw new OperationException("资源需求(" + demand.getDemandId() + ")与订单的关联信息错误");
		}

		// 改为调用订单的方法 设置订单待配单
		orderListService.daipeidan(orderId, resourceIds.toArray(new String[resourceIds.size()]),
				Long.parseLong(operatorId));

		/*
		 * OrderList orderList = new OrderList(); orderList.setOrderId(orderId);
		 * orderList.setState(Constant.OrderStatus.待配单.getValue());
		 * orderList.setUpdateTime(now); int updateOrder =
		 * orderListMapper.updateSelective(orderList); if (updateOrder <= 0) {
		 * throw new OperationException("订单状态更新为待配单失败"); }
		 *
		 * // 批量插入订单-资源的关联信息 params.put("orderId", orderId); int oCount =
		 * orderListMapper.batchInsertResourceRef(params); if (oCount !=
		 * resourceIds.size()) { throw new OperationException("订单与资源关联添加失败"); }
		 */

		return true;
	}

	@Override
	public List<Resource> findHasInResourceList(List<Resource> filterList) {
		return getResources(filterList, ResourceStatus.已接入.toString());
	}

	@Override
	public List<Resource> findWaitInResourceList(List<Resource> filterList) {
		return getResources(filterList, ResourceStatus.待接入.toString());
	}

	private List<Resource> getResources(List<Resource> filterList, String status) {
		Map params = Maps.newHashMap();
		params.put("state", status);

		List<Resource> list = resourceMapper.findAllList(params);

		if (filterList == null || filterList.size() <= 0) {
			return list;
		}

		// 返回列表删除掉需要过滤的
		Set<String> sets = Sets.newHashSet();
		for (Resource resource : filterList) {
			sets.add(resource.getResourceId());
		}
		Iterator<Resource> iterator = list.iterator();
		while (iterator.hasNext()) {
			Resource resource = iterator.next();
			if (sets.contains(resource.getResourceId())) {
				iterator.remove();
			}
		}
		return list;
	}

	@Override
	public boolean channelIsOpen(String channelId) {
		return resourceMapper.getOpenChannelCountByChannelId(channelId) > 0;
	}

	@Override
	public Demand getDemandByOrderId(String orderId) {
		return demandMapper.getDemandByOrderId(orderId);
	}

	@Override
	public Page queryList(Page page) {
		String orderId = (String) page.getParams().get("orderId");
		if (StringUtils.isNotEmpty(orderId)) {
			List<OrderRefResource> refs = orderRefResourceService.queryList(page).getData();
			List<String> resourceIds = new ArrayList<>();
			if (refs != null && refs.size() > 0) {
				for (OrderRefResource ref : refs) {
					resourceIds.add(ref.getResourceId());
				}
			}
			if (resourceIds.size() == 0)
				resourceIds.add("-999");
			page.getParams().put("resourceIds", resourceIds);
		}

		List<Resource> list = resourceMapper.queryList(page);
		List<ResourceDTO> dtos = new ArrayList<>();
		for (Resource r : list) {
			ResourceDTO dto = new ResourceDTO();
			BeanUtils.copyProperties(r, dto);
			Long userId = 0l;
			try {
				userId = Long.valueOf(dto.getBelongBusiness());
				Map<String, Object> user = this.adminService.getAdmin(userId);
				dto.setBelongBusinessName((String) user.get("realname"));
			} catch (Exception e) {
				logger.error("查询归属销售失败", e);
			}
			dtos.add(dto);
		}
		page.setData(dtos);
		return page;
	}
}
