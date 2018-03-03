package com.ucpaas.sms.service.finance.order;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.dto.PurchaseOrderVO;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.model.PageContainer;

public interface OrderInfoService {
	
	PageContainer query(Map<String, String> params);

	/**
	  * @Description: 查询品牌代理商订单(数据权限控制)
	  * @Author: tanjiangqiang
	  * @Date: 2017/12/16 - 17:01
	  * @param params 查询参数
	  * @param userSession 用户登录信息
	  *
	  */
	PageContainer queryOrder(Map params, UserSession userSession);

	/**
	 * @Description: 获取登录用户可查看的客户id
	 * @Author: tanjiangqiang
	 * @Date: 2017/12/16 - 14:23
	 * @param userSession 用户信息
	 *
	 */
	Set<String> getClientids(UserSession userSession);
	
	Map<String,Object> queryOrderInfoBySubId(long orderId);

	/**
	 * @Description: 查询OEM代理商订单信息
	 * @author: Niu.T 
	 * @date: 2016年11月25日    下午3:14:33
	 * @param params
	 * @return PageContainer
	 */
	PageContainer queryAgentOrder(Map<String, String> params);

	/**
	 * @param params      查询参数
	 * @param userSession 用户信息
	 * @Description: 查询OEM代理商订单信息(权限控制)
	 * @Author: tanjiangqiang
	 * @Date: 2017/12/18 - 11:15
	 */
	PageContainer queryOEMAgentOrder(Map<String, Object> params, UserSession userSession);

	/**
	 * @Description: 查询OEM客户订单信息
	 * @author: Niu.T 
	 * @date: 2016年11月25日    下午3:15:38
	 * @param params
	 * @return PageContainer
	 */
	PageContainer queryClientOrder(Map<String, String> params);

	/**
	 * @param params      查询条件
	 * @param userSession 用户信息
	 * @Description: 查询OEM客户订单信息(数据权限控制)
	 * @Author: tanjiangqiang
	 * @Date: 2017/12/18 - 12:20
	 */
	PageContainer queryOEMClientOrder(Map<String, Object> params, UserSession userSession);

	/**
	 * @Description: 根据订单id查询oem代理商订单
	 * @author: Niu.T 
	 * @date: 2016年11月25日    下午5:06:29
	 * @param valueOf
	 * @return Map<String,Object>
	 */
	Map<String, Object> getOEMAgentOrderById(Long orderId);
	/**
	 * @Description: 根据订单id查询oem客户订单TODO
	 * @author: Niu.T 
	 * @date: 2016年11月25日    下午5:06:29
	 * @param valueOf
	 * @return Map<String,Object>
	 */
	Map<String, Object> getOEMClientOrderById(Long orderId);

	/**
	 * @Description: 导出品牌订单信息 Excel
	 * @author: Niu.T 
	 * @date: 2016年12月7日    下午12:21:52
	 * @param params
	 * @return List<Map<String,Object>>
	 */
	List<Map<String, Object>> exportOrderExcel(Map<String, String> params);

	/**
	  * @Description: 导出品牌订单信息 Excel(数据权限控制)
	  * @Author: tanjiangqiang
	  * @Date: 2017/12/16 - 17:00
	  * @param params 查询参数
	  * @param userSession 登录用户信息
	  *
	  */
	List<Map<String, Object>> exportBrandOrderExcel(Map params, UserSession userSession);

	/**
	 * @Description: 导出OEM代理商订单
	 * @author: Niu.T 
	 * @date: 2016年12月7日    下午2:13:17
	 * @param params
	 * @return List<Map<String,Object>>
	 */
	List<Map<String, Object>> exportAgentOrder(Map<String, Object> params);

	/**
	 * @param params      导出条件
	 * @param userSession 用户信息
	 * @Description: 导出OEM代理商订单(数据权限控制)
	 * @Author: tanjiangqiang
	 * @Date: 2017/12/18 - 11:54
	 */
	List<Map<String, Object>> exportOEMAgentOrder(Map<String, Object> params, UserSession userSession);

	/**
	 * @Description: 导出OEM客户订单
	 * @author: Niu.T 
	 * @date: 2016年12月7日    下午2:13:08
	 * @param params
	 * @return List<Map<String,Object>>
	 */
	List<Map<String, Object>> exportClientOrder(Map<String, Object> params);

	/**
	 * @param params      查询条件
	 * @param userSession 用户信息
	 * @Description: 导出OEM客户订单(数据权限控制)
	 * @Author: tanjiangqiang
	 * @Date: 2017/12/18 - 12:35
	 */
	List<Map<String, Object>> exportOEMClientOrder(Map<String, Object> params, UserSession userSession);
	
	/**
	 * @Title: confirmBuy
	 * @Description: 对于销售代理商来说，订单支付在运营平台
	 * @param params:订单id：sub_id
	 * @return: Map<String,Object>
	 */
	Map<String,Object> confirmBuy(Map<String, String> params);
	Map<String,Object> jsmsConfirmBuy(Map<String, String> params,Long adminId);

	/**
	 * 品牌订单信息列表的总计行
	 * @param params
	 * @return
	 */
	Map total(Map<String, String> params);

	/**
	  * @Description: 品牌订单信息总计行(数据权限控制)
	  * @Author: tanjiangqiang
	  * @Date: 2017/12/16 - 16:05
	  * @param params 查询条件
	  * @param userSession 登录用户信息
	  *
	  */
	Map totalOrder(Map params, UserSession userSession);

	ResultVO createOrderAndBuy(PurchaseOrderVO purchaseOrderList,Long adminId);

}
