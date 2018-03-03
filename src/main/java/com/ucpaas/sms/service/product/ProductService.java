package com.ucpaas.sms.service.product;

import java.util.List;
import java.util.Map;

import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.dto.PurchaseOrder;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.po.PlaceOrderParam;

/**
 * 用户短信通道管理-短信通道配置
 * 
 * @author zenglb
 */
public interface ProductService {
	PageContainer query(Map<String, String> params);
	
	PageContainer cusSetingRecord(Map<String, String> params);
	
	Map<String, Object> getProductDetailByProductId(int id);
	
	Map<String, Object> addData(Map<String, String> params);
	
	Map<String, Object> updateStatus(Map<String, String> params);
	
	Map<String, Object> save(Map<String, String> params);
	
	Map<String, Object> getCustomerInfo(Map<String, String> params);
	
	PageContainer getAgentProductInfo(Map<String, String> params);
	
	Map<String, Object> updateDiscountInfo(Map<String, String> params);
	
	Map<String,Object> updateDiscountInfoBatch(PlaceOrderParam placeOrderParam,String userId, String pageUrl, String ip);
	
	public PageContainer queryInterSmsPriceInfo(Map<String, String> params);

	Map<String, Object> deleteMsg(int id, Long userId, String pageUrl, String ip);
	
	public Map<String, Object> getInterSmsPriceById(int id);
	
	public Map<String, Object> updateInterSmsPrice(Map<String, String> params);

	/**
	 * @Description: 获取品牌订单中返点抵充比例
	 * @author: Niu.T 
	 * @date: 2016年12月5日    下午5:51:31
	 * @param formData
	 * @return List<Map<String,Object>>
	 */
	List<Map<String, Object>> getAgentRebate(Map<String, String> params);

	/**
	 * @Description: 修改品牌订单中返点抵充比例
	 * @author: Niu.T 
	 * @date: 2016年12月5日    下午8:46:56
	 * @param params
	 * @return Map<String,Object>
	 */
	Map<String, Object> addOrUpdateRebate(Map<String, String> params);

	/**
	 * @Description: 添加国际短信价格
	 * @author: Niu.T 
	 * @date: 2016年12月6日    上午11:22:43
	 * @param formData
	 * @return Map<String,Object>
	 */
	Map<String, Object> addInterSmsPrice(Map<String, String> params);

	/**
	 * @Description: 导出国际短信价格
	 * @author: Niu.T 
	 * @date: 2016年12月12日    下午6:50:09
	 * @param params
	 * @return List<Map<String,Object>>
	 */
	List<Map<String, Object>> exportInterSmsPrice(Map<String, String> params);


	/**
	 * @Title: querySmsPurchaseProduct
	 * @Description: 查询产品信息
	 * @param params
	 * @return
	 * @return: PageContainer
	 */
	PageContainer querySmsPurchaseProduct(Map<String, String> params);


	ResultVO confirmPurchaseOrder(List<PurchaseOrder> purchaseOrderList, Integer agentId, Long adminId);

}
