package com.ucpaas.sms.service.finance.order;

import com.hankcs.hanlp.classification.features.IFeatureWeighter;
import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.Result;
import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.common.enums.ClientOrderType;
import com.jsmsframework.common.util.BeanUtil;
import com.jsmsframework.common.util.JsonUtil;
import com.jsmsframework.order.entity.JsmsClientOrder;
import com.jsmsframework.order.product.exception.JsmsClientOrderProductException;
import com.jsmsframework.order.product.service.JsmsClientOrderProductService;
import com.jsmsframework.order.service.JsmsClientOrderService;
import com.jsmsframework.product.entity.JsmsProductInfo;
import com.jsmsframework.product.service.JsmsProductInfoService;
import com.jsmsframework.user.entity.JsmsAgentInfo;
import com.jsmsframework.user.exception.JsmsUserException;
import com.jsmsframework.user.service.JsmsAgentInfoService;
import com.ucpaas.sms.constant.LogConstant.LogType;
import com.ucpaas.sms.dao.MessageMasterDao;
import com.ucpaas.sms.dto.ClientOrderVO;
import com.ucpaas.sms.dto.PurchaseOrder;
import com.ucpaas.sms.dto.PurchaseOrderVO;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.entity.po.DataAuthorityCondition;
import com.ucpaas.sms.enums.LogEnum;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.AgentInfoService;
import com.ucpaas.sms.service.account.AccountService;
import com.ucpaas.sms.service.admin.AuthorityService;
import com.ucpaas.sms.service.common.LogService;
import com.ucpaas.sms.util.AgentUtils;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.PageConvertUtil;
import com.ucpaas.sms.util.order.OrderIdGenerate;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;

@Service
public class OrderInfoServiceImpl implements OrderInfoService{

	private static final Logger LOGGER = LoggerFactory.getLogger(OrderInfoServiceImpl.class);

	@Autowired
	private MessageMasterDao masterDao;
	@Autowired
	private LogService logService;
	@Autowired
	private JsmsClientOrderService jsmsClientOrderService;
	@Autowired
	private JsmsProductInfoService jsmsProductInfoService;
	@Autowired
	private JsmsAgentInfoService jsmsAgentInfoService;
	@Autowired
	private JsmsClientOrderProductService jsmsClientOrderProductService;
	@Autowired
	private AuthorityService authorityService;
	@Autowired
	private AccountService accountService;
	@Autowired
	private AgentInfoService agentInfoService;


	@Override
	public PageContainer query(Map params) {
		if(StringUtils.isNotBlank((String) params.get("condition"))){
			List<JsmsProductInfo> jsmsProductInfos = jsmsProductInfoService.getByProductCode((String) params.get("condition"));
			if(!jsmsProductInfos.isEmpty()){
				Set<Integer> productIds = new HashSet();
				for (JsmsProductInfo jsmsProductInfo : jsmsProductInfos) {
					productIds.add(jsmsProductInfo.getProductId());
				}
				params.put("productIds", productIds);
			}
		}

        JsmsPage<ClientOrderVO> page = PageConvertUtil.paramToPage(params);
		jsmsClientOrderService.queryList(page);

		Set<Integer> productIds = new HashSet();
		Set<Integer> agentIds = new HashSet();
		List resultList = new ArrayList<>();
		for(JsmsClientOrder  jsmsClientOrder: page.getData()){
			ClientOrderVO clientOrder = new ClientOrderVO();
			BeanUtil.copyProperties(jsmsClientOrder,clientOrder);
			resultList.add(clientOrder);
			productIds.add(clientOrder.getProductId());
			agentIds.add(clientOrder.getAgentId());
		}
        page.setData(resultList);
		List<JsmsProductInfo> jsmsProductInfos = new ArrayList<>();
		if(!productIds.isEmpty()){
			jsmsProductInfos = jsmsProductInfoService.getByProductIds(productIds);
        }
        List<JsmsAgentInfo> agentInfos = new ArrayList<>();
        if(!agentIds.isEmpty()){
            agentInfos = jsmsAgentInfoService.getByAgentIds(agentIds);
        }

        Map<Integer, String> productInfoMap = new HashMap<>();
        for (JsmsProductInfo info : jsmsProductInfos) {
            productInfoMap.put(info.getProductId(), info.getProductCode());
        }
        Map<Integer, Integer> agentInfoMap = new HashMap<>();
        for (JsmsAgentInfo info : agentInfos) {
            agentInfoMap.put(info.getAgentId(), info.getAgentType());
        }
        for (ClientOrderVO temp : page.getData()) {
            if (productInfoMap.get(temp.getProductId()) != null) {
                temp.setProductCode(productInfoMap.get(temp.getProductId()));
            }
            if (agentInfoMap.get(temp.getAgentId()) != null) {
                temp.setAgentType(agentInfoMap.get(temp.getAgentId()));
            }
        }
        return PageConvertUtil.pageToContainer(page);
//        return pageContainer;
//        return masterDao.getSearchPage("OrderInfo.queryOrderInfo", "OrderInfo.queryOrderInfoCount", params);
	}

	@Override
	public PageContainer queryOrder(Map params, UserSession userSession) {
		Set<String> clientids = this.getClientids(userSession);
		//返回的客户id为空,说明该用户或下级部门没有归属客户
		if (null == clientids || clientids.isEmpty()){
			//直接返回空列表
			return new PageContainer();
		}
		params.put("clientids", clientids);
		return this.query(params);
	}

	/**
	  * @Description: 获取登录用户可查看的客户id
	  * @Author: tanjiangqiang
	  * @Date: 2017/12/16 - 14:23
	  * @param userSession 用户信息
	  *
	  */
	@Override
	public Set<String> getClientids(UserSession userSession) {
		Set<String> clientids = new HashSet<>();
		Long userId = userSession.getId();
		if (null == userId) {
			LOGGER.error("session为空{}", userSession);
			new JsmsUserException("请先登录");
		}
		DataAuthorityCondition dataAuthorityCondition = AgentUtils.getDataAuthorityCondition(userId, true, false);
		clientids =  accountService.findAllListByBelongSales(dataAuthorityCondition);
		return clientids;
	}

	@Override
	public Map<String, Object> queryOrderInfoBySubId(long orderId) {
		return masterDao.getOneInfo("OrderInfo.queryOrderInfoBySubId", orderId);
	}

	/**
	 * 查询OEM代理商订单信息
	 */
	@Override
	public PageContainer queryAgentOrder(Map<String, String> params) {
		return masterDao.getSearchPage("oemOrder.queryAgentOrder", "oemOrder.queryAgentOrderCount", params);
	}

	/**
	 * 查询OEM代理商订单信息(数据权限控制)
	 */
	@Override
	public PageContainer queryOEMAgentOrder(Map<String, Object> params, UserSession userSession) {
		//获取用户的数据权限
		DataAuthorityCondition dataAuthorityCondition = AgentUtils.getDataAuthorityCondition(userSession.getId(), true, false);
		Set<Integer> agentIds = agentInfoService.findOEMAgentIdByBelongSales(dataAuthorityCondition);
		params.put("agentIds", agentIds);
		if (null == agentIds || agentIds.isEmpty()){
			return new PageContainer();
		}
		return masterDao.getSearchPage("oemOrder.queryAgentOrder", "oemOrder.queryAgentOrderCount", params);
	}

	/**
	 * 查询OEM客户订单信息
	 */
	@Override
	public PageContainer queryClientOrder(Map<String, String> params) {
		return masterDao.getSearchPage("oemOrder.queryClientOrder", "oemOrder.queryClientOrderCount", params);
	}

	/**
	 * @param params      查询条件
	 * @param userSession 用户信息
	 * @Description: 查询OEM客户订单信息(数据权限控制)
	 * @Author: tanjiangqiang
	 * @Date: 2017/12/18 - 12:20
	 */
	@Override
	public PageContainer queryOEMClientOrder(Map<String, Object> params, UserSession userSession) {
		Set<String> clientids = this.getClientids(userSession);
		params.put("clientids", clientids);
		// 可查看的clientids为空,直接返回空列表
		if (null == clientids || clientids.isEmpty()) {
			return new PageContainer();
		}
		return masterDao.getSearchPage("oemOrder.queryClientOrder", "oemOrder.queryClientOrderCount", params);
	}

	/**
	 * 根据订单id查询oem代理商订单
	 */
	@Override
	public Map<String, Object> getOEMAgentOrderById(Long orderId) {
		return masterDao.getOneInfo("oemOrder.getOEMAgentOrderById", orderId);
	}

	/**
	 * 根据订单id查询oem客户订单
	 */
	@Override
	public Map<String, Object> getOEMClientOrderById(Long orderId) {
		return masterDao.getOneInfo("oemOrder.getOEMClientOrderById", orderId);
	}

	/**
	 * 导出品牌订单信息
	 */
	@Override
	public List<Map<String, Object>> exportOrderExcel(Map<String, String> params) {
        int pageRowCount = Integer.parseInt(ConfigUtils.max_export_excel_num) + 1;
        params.put("pageRowCount", String.valueOf(pageRowCount));
        List entityList = query(params).getEntityList();
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object o : entityList) {
            try {
                Map describe = BeanUtils.describe(o);
                result.add(describe);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return result;
//		return masterDao.getSearchList("OrderInfo.exportOrderExcel", params);
	}


	@Override
	public List<Map<String, Object>> exportBrandOrderExcel(Map params, UserSession userSession) {
		int pageRowCount = Integer.parseInt(ConfigUtils.max_export_excel_num) + 1;
		params.put("pageRowCount", String.valueOf(pageRowCount));
//		List entityList = query(params).getEntityList();
		List entityList = this.queryOrder(params,userSession).getEntityList();
		List<Map<String, Object>> result = new ArrayList<>();
		for (Object o : entityList) {
			try {
				Map describe = BeanUtils.describe(o);
				result.add(describe);
			}  catch (Exception e) {
				e.printStackTrace();
				LOGGER.error("对象转换失败{}",o);
				throw new IllegalArgumentException("服务器正在检修...");
			}
		}
		return result;
//		return masterDao.getSearchList("OrderInfo.exportOrderExcel", params);
	}

	/**
	 * 导出OEM代理商订单
	 */
	@Override
	public List<Map<String, Object>> exportAgentOrder(Map<String, Object> params) {
		List<Map<String, Object>> datas = masterDao.getSearchList("oemOrder.exportAgentOrder", params);

		for(Map<String, Object> data:datas){
			String unit_price_str = data.get("unit_price").toString();
			if(!unit_price_str.equals("-")){
				BigDecimal unit_price = new BigDecimal(unit_price_str);
				data.put("unit_price", unit_price.setScale(4, BigDecimal.ROUND_HALF_UP).toString());
			}
			String order_amount_str = data.get("order_amount").toString();
			if(!order_amount_str.equals("-")){
				BigDecimal order_amount = new BigDecimal(order_amount_str);
				data.put("order_amount", order_amount.setScale(4, BigDecimal.ROUND_HALF_UP).toString());
			}
		}
		return datas;
	}

	/**
	 * @param params      导出条件
	 * @param userSession 用户信息
	 * @Description: 导出OEM代理商订单(数据权限控制)
	 * @Author: tanjiangqiang
	 * @Date: 2017/12/18 - 11:54
	 */
	@Override
	public List<Map<String, Object>> exportOEMAgentOrder(Map<String, Object> params, UserSession userSession) {
		DataAuthorityCondition dataAuthorityCondition = AgentUtils.getDataAuthorityCondition(userSession.getId(), true,false);
		Set<Integer> agentIds = agentInfoService.findOEMAgentIdByBelongSales(dataAuthorityCondition);
		params.put("agentIds",agentIds);
		if (null == agentIds || agentIds.isEmpty()) {
			return new ArrayList<>();
		}
		return this.exportAgentOrder(params);
	}



	/**
	 * 导出OEM代理商订单
	 */
	@Override
	public List<Map<String, Object>> exportClientOrder(Map<String, Object> params) {
		List<Map<String,Object>> datas= masterDao.getSearchList("oemOrder.exportClientOrder", params);
		for(Map<String,Object> data:datas){
			if(!"2".equals(data.get("product_type"))){ //非国际短信 是 数量乘以单价
				String number = data.get("order_number").equals("-")?"0":data.get("order_number").toString();
				String unitPrice = data.get("unit_price").equals("-")?"0":data.get("unit_price").toString();
				data.put("order_price", new BigDecimal(number).multiply(new BigDecimal(unitPrice)));
			}


			String unit_price_str = data.get("unit_price").toString();
			if(!unit_price_str.equals("-")){
				BigDecimal unit_price = new BigDecimal(unit_price_str);
				data.put("unit_price", unit_price.setScale(4, BigDecimal.ROUND_HALF_UP).toString());
			}
			String order_price_str = data.get("order_price").toString();
			if(!order_price_str.equals("-")){
				BigDecimal order_price = new BigDecimal(order_price_str);
				data.put("order_price", order_price.setScale(4, BigDecimal.ROUND_HALF_UP).toString());
			}
		}
		return datas;
	}

	/**
	 * @param params      查询条件
	 * @param userSession 用户信息
	 * @Description: 导出OEM客户订单(数据权限控制)
	 * @Author: tanjiangqiang
	 * @Date: 2017/12/18 - 12:35
	 */
	@Override
	public List<Map<String, Object>> exportOEMClientOrder(Map<String, Object> params, UserSession userSession) {
		Set<String> clientids = this.getClientids(userSession);
		params.put("clientids", clientids);
		// 可查看的clientids为空,直接返回空列表
		if (null == clientids || clientids.isEmpty()) {
			return new ArrayList<>();
		}
		return this.exportClientOrder(params);
	}

	/**
	 * 销售代理商支付
	 */
	@Override
	@Deprecated
    @Transactional
	public Map<String, Object> confirmBuy(Map<String, String> params) {

		LOGGER.debug("进入confirmBuy方法,参数：{}",JSONObject.valueToString(params));

		Map<String,Object> data = new HashMap<>();

		Long sub_id = Long.valueOf(params.get("sub_id"));
		Map<String,Object> orderDetail = masterDao.getOneInfo("finance.getOrderDetailBySubId", sub_id);

		String client_id = (String) orderDetail.get("client_id");
		Integer agent_id = (Integer) orderDetail.get("agent_id");
		Integer product_id = (Integer) orderDetail.get("product_id");
		Integer active_period = (Integer) orderDetail.get("active_period");
		Integer status = (Integer) orderDetail.get("status");
		Long order_id = (Long) orderDetail.get("order_id");
		Integer product_type = (Integer) orderDetail.get("product_type");
		BigDecimal sale_price = null;

		//如果是国际产品，总数量即为总金额

		if(product_type == 2){
			sale_price = (BigDecimal) orderDetail.get("quantity");
		}else{
			sale_price = (BigDecimal) orderDetail.get("sale_price");
		}

		//检查订单的条件
		StringBuffer sb_balance = new StringBuffer("");
		boolean flag = this.check(data, product_id, agent_id, sale_price,status,sub_id,sb_balance);
		if(flag == false){
			LOGGER.debug("离开confirmBuy方法,结果：{}",JSONObject.valueToString(data));
			return data;
		}

		//减少余额
		this.reduceBalance(agent_id, sale_price);

		//订单生效（生效时间、到期时间=生效时间+有效时间、订单状态）
		this.updateOrderInfoForComplete(active_period, sub_id, data);

		//插入余额账单表（t_sms_agent_balance_bill）
//		Long admin_id = AuthorityUtils.getLoginUserId();
//		this.insertAgentBalanceBill(agent_id, sale_price, order_id, admin_id, client_id,sb_balance);

		data.put("result", "success");
		data.put("msg", "支付成功");

		LOGGER.debug("离开confirmBuy方法,结果：{}",JSONObject.valueToString(data));
//		logService.add(LogType.update,LogEnum.财务管理.getValue(), "财务管理-品牌订单信息-销售代理商订单支付:", params, data);
		Long userId = Long.valueOf(params.get("userId"));
		String pageUrl = params.get("pageUrl");
		String ip = params.get("ip");
		logService.add(LogType.update,LogEnum.财务管理.getValue(), userId, pageUrl, ip, "财务管理-品牌订单信息-销售代理商订单支付:", params, data);

		return data;
	}

	@Override
	@Transactional("message")
	public Map<String, Object> jsmsConfirmBuy(Map<String, String> params,Long adminId) {
		Long subId = Long.valueOf(params.get("sub_id"));
		List<Long> subIds = new ArrayList<>();
		subIds.add(subId);
        ResultVO resultVO = jsmsClientOrderProductService.confirmBuy(subIds, adminId);

        String pageUrl = params.get("pageUrl");
		String ip = params.get("ip");
        Map<String,Object> data = new HashMap<>();
        data.put("result", resultVO.isSuccess()?"success":"fail");
        data.put("msg", resultVO.getMsg());
		logService.add(LogType.update,LogEnum.财务管理.getValue(), adminId, pageUrl, ip, "财务管理-品牌订单信息-销售代理商订单支付:", params, data);

		return data;
	}

		//检查订单是否符合购买的条件
	private boolean check(Map<String,Object> data,Integer product_id,Integer agent_id,BigDecimal sale_price,Integer status,Long sub_id,StringBuffer sb_balance){

		boolean flag = true;
		if(!status.equals(0)){
			String statusName = new String();
			data.put("result", "fail");
			switch (status) {
			case 1:
				statusName = "订单已经生效，无需重复支付";
				break;
			case 2:
				statusName = "订单已经完成，无需重复支付";
				break;
			case 3:
				statusName = "失败订单，无法支付";
				break;
			case 4:
				statusName = "订单已被取消，无法支付";
				break;

			default:
				break;
			}
			data.put("msg",statusName);
			return false;
		}

		//先判断产品有没有下架
		Map<String,Object> productMap = masterDao.getOneInfo("product.getProductDetailByProductId", product_id);
		if(productMap == null){
			data.put("result", "fail");
			data.put("msg", "商品："+product_id+"已经被删除，订单失败");

			//更新订单的状态，未订单失败
			Map<String,Object> params = new HashMap<>();
			params.put("sub_id", sub_id);
			params.put("status", 3);//订单失败
			masterDao.update("finance.orderFail", params);
			return false;
		}

		Integer productStatus = (Integer) productMap.get("status");
		String product_name = (String) productMap.get("product_name");
		//产品已经下架
		if(productStatus == 2){
			data.put("result", "fail");
			data.put("msg", "商品："+product_name+"已经下架，订单失败");

			//更新订单的状态，未订单失败
			Map<String,Object> params = new HashMap<>();
			params.put("sub_id", sub_id);
			params.put("status", 3);//订单失败
			masterDao.update("finance.orderFail", params);
			return false;
		}

		//判断代理商是否已经取消代理商品,如果取消代理了，则订单也为失败
		Map<String,Object> queryNumParams = new HashMap<>();
		queryNumParams.put("agent_id", agent_id);
		queryNumParams.put("product_id", product_id);
		int num = masterDao.getOneInfo("finance.queryCountAgentProduct", queryNumParams);
		if(num > 0){

			data.put("result", "fail");
			data.put("msg", "商品："+product_name+"已经被取消代理，订单失败");

			//更新订单的状态，未订单失败
			Map<String,Object> params = new HashMap<>();
			params.put("sub_id", sub_id);
			params.put("status", 3);//订单失败
			masterDao.update("finance.orderFail", params);

			return false;
		}

		//判断余额是否充足 getAgentAccountByAgentId
		Map<String,Object> agentAccount = masterDao.getOneInfo("finance.getAgentAccountByAgentId", agent_id);
		BigDecimal balance = (BigDecimal) agentAccount.get("balance");
		if(balance.compareTo(sale_price) == -1){
			data.put("result", "fail");
			data.put("msg", "余额不足，请充值");

			return false;
		}

		BigDecimal remain_balance = balance.subtract(sale_price);
		sb_balance.append(remain_balance.toString());

		return flag;
	}

	//减少余额
	private void reduceBalance(Integer agent_id,BigDecimal sale_price){
		//余额减少
		Map<String,Object> reduceBalanceParams = new HashMap<>();
		reduceBalanceParams.put("agent_id", agent_id);
		reduceBalanceParams.put("amount", sale_price);
		masterDao.update("finance.reduceBalance", reduceBalanceParams);
	}


	//更新订单状态信息
	private void updateOrderInfoForComplete(Integer active_period,Long sub_id,Map<String,Object> data){

		DateTime dt_now = new DateTime();
		Date endTime = null;

		if(active_period != 0){
			endTime = dt_now.plusDays(active_period).toDate();
		}else{
			DateTime dt = new DateTime(9999,1,1,0,0,0,0);
			endTime = dt.toDate();
		}

		Map<String,Object> orderDetail = masterDao.getOneInfo("finance.getBySubId", sub_id);

		//订单完成，更新状态信息
		Map<String,Object> completeSqlParams = new HashMap<>();
		if (!orderDetail.get("product_type").equals(2)){
			BigDecimal salePrice = (BigDecimal) orderDetail.get("sale_price");
			BigDecimal quantity = (BigDecimal) orderDetail.get("quantity");
			completeSqlParams.put("unit_price",salePrice.divide(quantity,5,BigDecimal.ROUND_HALF_UP));//普通短信单价（折后价） = 产品总销售价 / 剩余数量
		}

		completeSqlParams.put("sub_id", sub_id);
		completeSqlParams.put("effective_time", dt_now.toDate());
		completeSqlParams.put("end_time", endTime);
		completeSqlParams.put("status", 1);//订单生效
		completeSqlParams.put("update_time", dt_now.toDate());

		masterDao.update("finance.updateOrderInfoForComplete", completeSqlParams);

		data.put("result", "sucess");
		data.put("msg", "购买成功");
	}

	//生成余额账单表
	private void insertAgentBalanceBill(Integer agent_id,BigDecimal amount,Long order_id,Long admin_id,String client_id,StringBuffer sb_balance){

		Map<String,Object> agentBalanceBillParams = new HashMap<>();
		agentBalanceBillParams.put("agent_id", agent_id);
		agentBalanceBillParams.put("operateAmount", amount);
		agentBalanceBillParams.put("balance", sb_balance.toString());
		agentBalanceBillParams.put("payment_type", 3); //0：运营转入，2：佣金转入，3：订单支付，4：转结算账户
		agentBalanceBillParams.put("financial_type", 1); //财务类型，0：入账，1：出账

		agentBalanceBillParams.put("create_time", new Date());
		agentBalanceBillParams.put("order_id", order_id);
		agentBalanceBillParams.put("admin_id", admin_id);
		agentBalanceBillParams.put("client_id", client_id);

		agentBalanceBillParams.put("remark", null);

		masterDao.insert("agentFinance.insertAgentBalanceBill", agentBalanceBillParams);

	}

	@Override
	public Map total(Map params) {

		if(StringUtils.isNotBlank((String) params.get("condition"))){
			List<JsmsProductInfo> jsmsProductInfos = jsmsProductInfoService.getByProductCode((String) params.get("condition"));
			if(!jsmsProductInfos.isEmpty()){
				Set<Integer> productIds = new HashSet();
				for (JsmsProductInfo jsmsProductInfo : jsmsProductInfos) {
					productIds.add(jsmsProductInfo.getProductId());
				}
				params.put("productIds", productIds);
			}
		}

		Map totalData= masterDao.getOneInfo("OrderInfo.total", params);
		if(totalData==null){
			totalData = new HashMap<>();
			totalData.put("sale_price", BigDecimal.ZERO);
			totalData.put("product_cost", BigDecimal.ZERO);
			totalData.put("quantity", "0条/0元");
			totalData.put("remain_quantity", "0条/0元");
			return totalData;
		}
		String quantityT = totalData.get("quantity_t")==null?"0条":totalData.get("quantity_t").toString()+"条";
		String quantityY = totalData.get("quantity_y")==null?"0元":totalData.get("quantity_y").toString()+"元";
		String remainQuantityT = totalData.get("remain_quantity_t")==null?"0条":totalData.get("remain_quantity_t").toString()+"条";
		String remainQuantityY = totalData.get("remain_quantity_y")==null?"0元":totalData.get("remain_quantity_y").toString()+"元";
		totalData.put("quantity", quantityT+"/"+quantityY);
		totalData.put("remain_quantity", remainQuantityT+"/"+remainQuantityY);
		return totalData;
	}

	@Override
	public Map totalOrder(Map params, UserSession userSession) {
		Set<String> clientids = this.getClientids(userSession);
		if (null == clientids || clientids.isEmpty()) {
			return new HashMap();
		}
		params.put("clientids", clientids);
		return this.total(params);
	}



//	//处理到期时间
//	private void handleEndTime(Map<String, Object> map){
//		
//		String effective_time =  (String) map.get("effective_time");
//		int active_period = (int) map.get("active_period");
//		if(effective_time == null){
//			map.put("end_time", null);
//			if(active_period == 0){
//				map.put("active_period", "无限期");
//			}
//		}else{
//			if(active_period == 0){
//				map.put("end_time", "9999-01-01 00:00:00");
//				map.put("active_period", "无限期");
//			}else{
//				DateTimeFormatter format1 = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
//				DateTime startDateTime = DateTime.parse(effective_time, format1);
//				startDateTime = startDateTime.plusDays(active_period);
//				map.put("end_time", startDateTime.toString("yyyy-MM-dd HH:mm:ss"));
//			}
//		}
//	}


	@Override
    @Transactional("message")
	public ResultVO createOrderAndBuy(PurchaseOrderVO purchaseOrderList,Long adminId) {
		List<String> productCodes = new ArrayList<>();
		List<Integer> purchaseNums = new ArrayList<>();
		List<Long> orderIds = new ArrayList<>();
		for (PurchaseOrder purchaseOrder : purchaseOrderList.getPurchaseList()) {
			productCodes.add(purchaseOrder.getProductCode());
			purchaseNums.add(purchaseOrder.getPurchaseNum().intValue());
			orderIds.add(OrderIdGenerate.GENERATE.getOrderId());
		}
        Result result = jsmsClientOrderProductService.createOrder(productCodes, purchaseNums, orderIds, ClientOrderType.运营代买,purchaseOrderList.getAgentId(), purchaseOrderList.getClientId());
		if (result.isFail()){
            return ResultVO.failure(result.getMsg());
        }
        List<JsmsClientOrder> clientOrders = (List<JsmsClientOrder>) result.getData();
        if(clientOrders.size() != orderIds.size()){
            throw new JsmsClientOrderProductException("订单购买失败!请稍后再试...");
        }
        ResultVO resultVO = jsmsClientOrderProductService.confirmBuyOrder(orderIds,purchaseOrderList.getAgentId(),adminId);
        if(resultVO.isFail()){
            throw new JsmsClientOrderProductException(resultVO.getMsg());
        }

        return resultVO;
	}
}
