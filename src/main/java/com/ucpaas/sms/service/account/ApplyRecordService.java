package com.ucpaas.sms.service.account;

import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.po.ProductInfo;
import com.ucpaas.sms.po.SaleEntityPo;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: ApplyRecordService
 * @Description: SMSP运营平台:账户信息管理  - > 代理商申请记录
 * @author: Niu.T 
 * @date: 2016年9月6日 上午11:16:51
 */
public interface ApplyRecordService {
	/**
	 * @Description: 分页查询代理商信息数据  
	 * @author: Niu.T 
	 * @date: 2016年9月6日 上午11:18:51
	 * @param params
	 * @return: PageContainer
	 */
	PageContainer query(Map<String, String> params);
	/**  
	 * @Description: 账户信息管理 - > 受理代理商与否
	 * @author: Niu.T 
	 * @date: 2016年9月6日 下午4:02:42  
	 * @param params
	 * @return: Map<String,Object>
	 */
	Map<String, Object> acceptOrNot(Map<String, String> params);

	/**
	 * 获取销售实体
	 * @return
	 */
	List<SaleEntityPo> getSaleList();

	/**  
	 * 获取代理商的申请信息
	 * @param id
	 * @return
	 */
	Map<String,Object> getAgentApplyInfoById(String id);
 
	/**
	 *oem资源获取测试产品
	 * @return
	 */
	List<ProductInfo> getProductInfoListForOemData();


	/**
	 * 添加销售
	 * @param params
	 */
	void addSale(Map<String,String> params);


	/**
	 * 品牌确认受理
	 * @param params
	 * @return
	 */
	ResultVO brandConfirmAccept(Map<String,String> params);

	/**
	 * oem确认受理
	 * @param params
	 * @return 
	 */
	ResultVO oemConfirmAccept(Map<String,String> params);


	/**
	 * 更新代理商归属销售
	 * @param params
	 */
	ResultVO updateBelongSaleForAgent(Map<String,String> params);

	/**
	 * 更新客户归属销售
	 * @param params
	 */
	ResultVO updateBelongSaleForClient(Map<String,String> params);

}
