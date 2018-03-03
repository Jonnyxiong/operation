package com.ucpaas.sms.service.finance.bill;

import java.util.List;
import java.util.Map;

import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.model.PageContainer;

/**
 * （运营平台）余额账单

 */
public interface BillInfoService {
	
	PageContainer queryBalance(Map<String, Object> params);

	/**
	  * @Description: 查询余额账单(数据权限控制)
	  * @Author: tanjiangqiang
	  * @Date: 2017/12/25 - 10:25
	  * @param params 查询参数
	  * @param userSession 用户信息
	  *
	  */
	PageContainer queryBalanceBill(Map<String, Object> params, UserSession userSession);
	
	PageContainer queryCommission(Map<String, String> params);
	
	List<Map<String, Object>> queryCommission4Excel(Map<String, String> params);
	
	List<Map<String, Object>> queryBalance4Excel(Map<String, Object> params);

	/**
	  * @Description: 导出余额账单(数据权限控制)
	  * @Author: tanjiangqiang
	  * @Date: 2017/12/25 - 10:58
	  * @param params 查询条件
	  * @param userSession 用户信息
	  *
	  */
	List<Map<String, Object>> queryBalance4Excel(Map<String, Object> params,UserSession userSession);

	/**
	 * @Description: 查询返点账单
	 * @author: Niu.T 
	 * @date: 2016年11月23日    下午4:32:07
	 * @param params
	 * @return PageContainer
	 */
	PageContainer queryRebate(Map<String, String> params);

	/**
	 * @Description: 查询授信记录
	 * @author: Niu.T 
	 * @date: 2016年11月23日    下午4:32:19
	 * @param params
	 * @return PageContainer
	 */
	PageContainer queryCredit(Map<String, String> params);

	/**
	 * @Description: 查询押金账单
	 * @author: Niu.T 
	 * @date: 2016年11月23日    下午4:32:25
	 * @param params
	 * @return PageContainer
	 */
	PageContainer queryDeposit(Map<String, String> params);

	/**
	 * @Description: 导出返点账单,查询满足条件的所有数据
	 * @author: Niu.T 
	 * @date: 2016年12月7日    上午11:32:13
	 * @param params
	 * @return List<Map<String,Object>>
	 */
	List<Map<String, Object>> exportRebateExcel(Map<String, String> params);

	/**
	 * @Description: 导出押金账单,查询满足条件的所有数据
	 * @author: Niu.T 
	 * @date: 2016年12月7日    上午11:51:38
	 * @param params
	 * @return List<Map<String,Object>>
	 */
	List<Map<String, Object>> exportDepositExcel(Map<String, String> params);

	/**
	 * @Description: 导出授信记录,查询满足条件的所有数据
	 * @author: Niu.T 
	 * @date: 2016年12月7日    上午11:51:49
	 * @param params
	 * @return List<Map<String,Object>>
	 */
	List<Map<String, Object>> exportCreditExcel(Map<String, String> params);

	/**
	 * 查询余额账单的总计
	 * @param params
	 * @return
	 */
	Map balanceTotal(Map<String, Object> params);

	/**
	  * @Description: 查询余额账单的总计(数据权限控制)
	  * @Author: tanjiangqiang
	  * @Date: 2017/12/25 - 10:50
	  * @param params 查询条件
	  * @param userSession 用户信息
	  *
	  */
	Map balanceBillTotal(Map<String, Object> params, UserSession userSession);

}
