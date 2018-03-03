package com.ucpaas.sms.service.product;

import java.util.List;
import java.util.Map;

import com.ucpaas.sms.model.PageContainer;

/**
 * @ClassName: OEMProductService
 * @Description: 4.3.1 新增OEM 产品管理
 * @author: Niu.T
 * @date: 2016年11月24日  下午5:46:16
 */
public interface OEMProductService {
	/**
	 * @Description: 查询OEM产品包
	 * @author: Niu.T 
	 * @date: 2016年11月24日    上午11:03:15
	 * @param params
	 * @return PageContainer
	 */
	PageContainer queryProduct(Map<String, String> params);
	/**
	 * @Description: 添加OEM产品
	 * @author: Niu.T 
	 * @date: 2016年11月24日    下午5:46:52
	 * @param params
	 * @return Map<String,Object>
	 */
	Map<String, Object> addProduct(Map<String, String> params);
	/**
	 * @Description: 修改OEM产品
	 * @author: Niu.T 
	 * @date: 2016年11月25日    上午10:24:46
	 * @param params
	 * @return Map<String,Object>
	 */
	Map<String, Object> editProduct(Map<String, String> params);
	/**
	 * @Description: 根据产品id查询产品详情
	 * @author: Niu.T 
	 * @date: 2016年11月25日    上午9:27:49
	 * @param id
	 * @return Map<String,Object>
	 */
	Map<String, Object> getProductDetail(int id);
	/**
	 * @Description: 修改产品状态 / 上架 | 下架
	 * @author: Niu.T 
	 * @date: 2016年11月25日    上午11:06:21
	 * @param params
	 * @return Map<String,Object>
	 */
	Map<String, Object> updateStatus(Map<String, String> params);
	/**
	 * @Description: 产品管理 / 获取OEM代理属性配置 
	 * @author: Niu.T 
	 * @date: 2016年11月28日    下午5:06:57
	 * @return List<String,Object>
	 */
	List<Map<String,Object>> getAgentRebate(Map<String, String> params);
	
}
