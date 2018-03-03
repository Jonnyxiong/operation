package com.ucpaas.sms.service.order;

import java.util.List;
import java.util.Map;

import com.ucpaas.sms.entity.message.Demand;
import com.ucpaas.sms.entity.message.Resource;
import com.ucpaas.sms.model.Page;
import com.ucpaas.sms.model.PageContainer;

/**
 * Created by lpjLiu on 2017/6/20.
 */
public interface ResourceService {

	/**
	 * 根据ID查询资源
	 * 
	 * @param id
	 * @return
	 */
	Resource getResource(String id);

	/**
	 * 查询资源进行分页
	 * 
	 * @param params
	 * @return
	 */
	PageContainer<Resource> findResourceList(Map params);

	/**
	 * 添加资源
	 * 
	 * @param resource
	 * @return
	 */
	int addResource(Resource resource);

	/**
	 * 编辑资源
	 * 
	 * @param resource
	 * @return
	 */
	int editResource(Resource resource);

	/**
	 * 更新资源状态
	 * 
	 * @param resource
	 * @return
	 */
	int updateResourceStatus(Resource resource);

	/**
	 * 通道是否已使用
	 * 
	 * @param channelId
	 * @return
	 */
	boolean hasUsedChannel(String channelId);

	/**
	 * 添加资源需求
	 *
	 * @param demand
	 * @return
	 */
	int addDemand(Demand demand);

	/**
	 * 更新资源需求状态
	 *
	 * @param demandId
	 * @return
	 */
	int chedan(String demandId);

	/**
	 * 根据ID查询资源需求
	 */
	Demand getDemand(String id);

	/**
	 * 获取资源需求列表
	 * 
	 */
	PageContainer<Demand> findDemandList(Map params);

	/**
	 * 资源需求设置为无资源
	 * 
	 * @param demand
	 * @return
	 */
	boolean doSetNoResourceForDemand(Demand demand);

	List<Resource> findResourceListByDemandId(String demandId);

	/**
	 * 有资源处理
	 * 
	 * @return
	 */
	boolean doSetHasResourceForDemand(Map<String, Object> params);

	/**
	 * 已接入资源列表
	 * 
	 * @param filterList
	 *            查询的结果过滤掉此列表的资源，无可以传空
	 * @return
	 */
	List<Resource> findHasInResourceList(List<Resource> filterList);

	/**
	 * 未接入资源列表
	 * 
	 * @param filterList
	 *            查询的结果过滤掉此列表的资源，无可以传空
	 * @return
	 */
	List<Resource> findWaitInResourceList(List<Resource> filterList);

	boolean channelIsOpen(String channelId);

	Demand getDemandByOrderId(String orderId);

	/**
	 * 分页查询资源列表
	 * @param page
	 * @return
	 */
	Page queryList(Page page);
}
