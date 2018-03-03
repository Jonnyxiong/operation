package com.ucpaas.sms.service.product;

import com.alibaba.fastjson.JSON;
import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.common.entity.JsmsAgentClientParam;
import com.jsmsframework.common.service.JsmsAgentClientParamService;
import com.jsmsframework.common.util.BeanUtil;
import com.jsmsframework.order.entity.JsmsOemAgentOrder;
import com.jsmsframework.order.product.service.JsmsOEMAgentOrderProductService;
import com.jsmsframework.product.entity.JsmsOemAgentProduct;
import com.jsmsframework.product.service.JsmsOemAgentProductService;
import com.jsmsframework.product.service.JsmsOemProductInfoService;
import com.jsmsframework.user.entity.JsmsAgentInfo;
import com.jsmsframework.user.service.JsmsAgentInfoService;
import com.ucpaas.sms.common.entity.R;
import com.ucpaas.sms.constant.LogConstant.LogType;
import com.ucpaas.sms.dao.MessageMasterDao;
import com.ucpaas.sms.dto.OemProductInfoVO;
import com.ucpaas.sms.dto.PurchaseOrder;
import com.ucpaas.sms.entity.po.ProductInfoPo;
import com.ucpaas.sms.enums.LogEnum;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.po.PlaceOrderParam;
import com.ucpaas.sms.service.common.LogService;
import com.ucpaas.sms.service.customer.CustomerManageServiceImpl;
import com.ucpaas.sms.util.PageConvertUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 用户短信通道管理-短信通道配置
 * 
 * @author zenglb
 */
@Service
@Transactional
public class ProductServiceImpl implements ProductService {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(ProductServiceImpl.class);

	@Autowired
	private MessageMasterDao masterDao;
	@Autowired
	private LogService logService;
	@Autowired
	private JsmsOemProductInfoService jsmsOemProductInfoService;
	@Autowired
	private JsmsAgentClientParamService jsmsAgentClientParamService;
	@Autowired
	private JsmsAgentInfoService jsmsAgentInfoService;
	@Autowired
	private JsmsOEMAgentOrderProductService jsmsOEMAgentOrderProductService;
	@Autowired
	private JsmsOemAgentProductService jsmsOemAgentProductService;

	@Override
	public PageContainer query(Map<String, String> params) {
		return masterDao.getSearchPage("product.query", "product.queryCount",
				params);
	}
	
	

	@Override
	public PageContainer cusSetingRecord(Map<String, String> params) {
		return masterDao.getSearchPage("product.cusSetingRecord", "product.cusSetingRecordCount",
				params);
	}



	@Override
	public Map<String, Object> getProductDetailByProductId(int id) {
		return masterDao.getOneInfo("product.getProductDetailByProductId", id);
	}

	@Override
	public Map<String, Object> addData(Map<String, String> params) {

		LOGGER.debug("创建产品包：" + params);
		Long userId = Long.valueOf(params.get("userId"));
		String pageUrl = params.get("pageUrl");
		String ip = params.get("ip"); 
		Map<String, Object> data = new HashMap<String, Object>();

		Map<String, Object> map = new HashMap<>();
		map.putAll(params);
		map.put("create_time", new Date());
		map.put("creator",userId);

		//判断是否有产品代码了
		int num = masterDao.getOneInfo("product.queryProductNumByCode", map);
		if(num > 0){
			data.put("result", "fail");
			data.put("msg", "产品代码已经存在，请重新填写！");
			return data;
		}
		
		int i = masterDao.insert("product.insertProduct", map);
		if (i > 0) {
			data.put("result", "success");
			data.put("msg", "添加成功");
		} else {
			data.put("result", "fail");
			data.put("msg", "添加失败");
		}
		logService.add(LogType.add, LogEnum.产品管理.getValue(), userId, pageUrl, ip,
				"产品管理-产品包列表：创建产品包", params, data);
//		logService.add(LogType.add, LogEnum.产品管理.getValue(),
//				"产品管理-产品包列表：创建产品包", params, data);

		return data;
	}

	@Override
	public Map<String, Object> save(Map<String, String> params) {

		LOGGER.debug("修改产品包：" + params);

		Long userId = Long.valueOf(params.get("userId"));
		String pageUrl = params.get("pageUrl");
		String ip = params.get("ip"); 
		Map<String, Object> map = new HashMap<>();
		map.putAll(params);
		map.put("update_time", new Date());
		map.put("updator", userId);

		Map<String, Object> data = new HashMap<String, Object>();
		
		Map<String,Object> productDetail = masterDao.getOneInfo("product.getProductDetailByProductId", params);
		String oldProductCode = (String) productDetail.get("product_code");
		if(!oldProductCode.equals(params.get("product_code"))){
			//如果产品编号变化了，需要判断是否重复的
			
			//判断是否有产品代码了
			int num = masterDao.getOneInfo("product.queryProductNumByCode", map);
			if(num > 0){
				data.put("result", "fail");
				data.put("msg", "产品代码已经存在，请重新填写！");
				return data;
			}
		}
		
		
		int i = masterDao.update("product.updateProductInfo", map);
		if (i > 0) {
			data.put("result", "success");
			data.put("msg", "修改成功");
		} else {
			data.put("result", "fail");
			data.put("msg", "修改失败");
		}

//		logService.add(LogType.update, LogEnum.产品管理.getValue(),
//				"产品管理-产品包列表：修改产品包", params, data);
		logService.add(LogType.update, LogEnum.产品管理.getValue(),userId, pageUrl, ip,
				"产品管理-产品包列表：修改产品包", params, data);
		return data;

	}

	@Override
	public Map<String, Object> updateStatus(Map<String, String> params) {
		LOGGER.debug("修改产品包状态，上架/下架：" + params);
		Long userId = Long.valueOf(params.get("userId"));
		String pageUrl = params.get("pageUrl");
		String ip = params.get("ip"); 
		Map<String, Object> data = new HashMap<String, Object>();
		
		Map<String, Object> map = new HashMap<>();
		map.putAll(params);
		map.put("update_time", new Date());
		map.put("updator",userId);
		
		int i = masterDao.update("product.updateStatus", map);
		if (i > 0) {
			data.put("result", "success");
			data.put("msg", "修改成功");
		} else {
			data.put("result", "fail");
			data.put("msg", "修改失败");
		}
//		logService.add(LogType.update, LogEnum.产品管理.getValue(),
//				"产品管理-产品包列表：修改产品包状态", params, data);
		logService.add(LogType.update, LogEnum.产品管理.getValue(),userId, pageUrl, ip,
				"产品管理-产品包列表：修改产品包状态", params, data);

		return data;
	}

	@Override
	public Map<String, Object> getCustomerInfo(
			Map<String, String> params) {
		return masterDao.getOneInfo("product.getCustomerInfo", params);
	}

	@Override
	public PageContainer getAgentProductInfo(Map<String, String> params) {
		return masterDao.getSearchPage("product.getAgentProductInfo",
				"product.getAgentProductInfoCount", params);
	}

	@Override
	public Map<String, Object> updateDiscountInfo(Map<String, String> params) {

		LOGGER.debug("产品包折扣率，添加/修改：" + params);

		Long userId = Long.valueOf(params.get("userId"));
		String pageUrl = params.get("pageUrl");
		String ip = params.get("ip"); 
		Map<String, Object> data = new HashMap<String, Object>();
		Date date = new Date();

		// 先判断有没有存在，没有新增，有则就修改
		Map<String, Object> params1 = new HashMap<>();
		params1.putAll(params);

		Map<String, Object> check = masterDao.getOneInfo(
				"product.checkDiscountInfo", params);
		if (check != null) {
			// 更新折扣
			params1.put("updator", userId);
			params1.put("update_time", date);

			int i = masterDao.update("product.updateDiscountInfo", params1);
			if (i > 0) {
				data.put("result", "success");
				data.put("msg", "修改成功");
			} else {
				data.put("result", "fail");
				data.put("msg", "修改失败");
			}
//			logService.add(LogType.update, LogEnum.产品管理.getValue(),
//					"产品管理-特殊客户设置：修改产品包折扣率", params, data);
			logService.add(LogType.update, LogEnum.产品管理.getValue(),userId, pageUrl, ip,
					"产品管理-特殊客户设置：修改产品包折扣率", params, data);

		} else {
			// 插入折扣信息
			params1.put("create_time", date);
			int i = masterDao.insert("product.insertDiscountInfo", params1);
			if (i > 0) {
				data.put("result", "success");
				data.put("msg", "修改成功");
			} else {
				data.put("result", "fail");
				data.put("msg", "修改失败");
			}
//			logService.add(LogType.add, LogEnum.产品管理.getValue(),
//					"产品管理-特殊客户设置：添加产品包折扣率", params, data);
			logService.add(LogType.add, LogEnum.产品管理.getValue(),userId, pageUrl, ip,
					"产品管理-特殊客户设置：添加产品包折扣率", params, data);
		}

		return data;
	}
	
	@Override
	public Map<String, Object> updateDiscountInfoBatch(PlaceOrderParam placeOrderParam,String userId, String pageUrl, String ip) {
		Map<String,Object> data = new HashMap<>();
		
		Date date = new Date();
		String clientid = placeOrderParam.getClientid();
		String agentId = placeOrderParam.getAgentId();
		List<ProductInfoPo> productInfoList = placeOrderParam.getProductInfoPoList();
		for(ProductInfoPo po : productInfoList){
			this.updateDiscountInfoSingle(clientid, agentId, po.getProduct_id(), po.getProduct_price_dis(), po.getGn_discount_price(), date,userId, pageUrl, ip);
		}
		
		data.put("result", "success");
		data.put("msg", "修改成功");
		
		return data;
	}
	
	private void updateDiscountInfoSingle(String clientid,String agentId,
			Integer product_id,BigDecimal product_price_dis,BigDecimal gn_discount_price,Date date,String userId,String pageUrl, String ip){
		
		Map<String,Object> params = new HashMap<>();
		params.put("client_id", clientid);
		params.put("agent_id", agentId);
		params.put("product_id", product_id);
		params.put("price_discount", product_price_dis);
		params.put("gn_discount_price", gn_discount_price);

		Map<String, Object> check = masterDao.getOneInfo("product.checkDiscountInfo", params);
		if (check != null) {
			// 更新折扣
			params.put("updator", userId);
			params.put("update_time", date);

			masterDao.update("product.updateDiscountInfo", params);
			logService.add(LogType.update, LogEnum.产品管理.getValue(), Long.parseLong(userId), pageUrl, ip,
					"产品管理-品牌特殊客户设置：修改产品折扣", JSON.toJSONString(params));
		} else {
			// 插入折扣信息
			params.put("create_time", date);
			params.put("update_time", date);
			params.put("updator", userId);
			masterDao.insert("product.insertDiscountInfo", params);
			logService.add(LogType.add, LogEnum.产品管理.getValue(), Long.parseLong(userId), pageUrl, ip,
					"产品管理-品牌特殊客户设置：添加产品折扣", JSON.toJSONString(params));
			
		}
	}

	// 国际短信价格费率查询
	@Override
	public PageContainer queryInterSmsPriceInfo(Map<String, String> params) {
		return masterDao.getSearchPage("product.queryInterSmsPrice",
				"product.queryInterSmsPriceCount", params);
	}

	// 删除国际费率表里的某一信息
	@Override
	public Map<String, Object> deleteMsg(int id, Long userId, String pageUrl, String ip) {
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("id", id);
		int i = masterDao.delete("product.deleteInterSmsPriceMsg", params);
		if (i > 0) {
			data.put("result", "success");
			data.put("msg", "删除成功");
		} else {
			data.put("result", "fail");
			data.put("msg", "删除失败");
		}
//		logService.add(LogType.delete, LogEnum.产品管理.getValue(),
//				"产品管理-国际短信价格费率配置：删除信息", params, data);
		logService.add(LogType.delete, LogEnum.产品管理.getValue(), userId, pageUrl, ip,
				"产品管理-国际短信价格费率配置：删除信息", params, data);
		return data;
	}

	// 通过ID查询某一国际价格费率的信息
	@Override
	public Map<String, Object> getInterSmsPriceById(int id) {
		return masterDao.getOneInfo("product.getInterSmsPriceById", id);
	}

	/**
	 * 修改国际短信价格
	 */
	@Override
	public Map<String, Object> updateInterSmsPrice(Map<String, String> params) {
		LOGGER.debug("修改某一国际短信价格配制：" + params);
		// 判断国际代码是否重复
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String,Object> interSmsPrice = masterDao.getOneInfo("product.getInterSmsPriceById", params);
		int oldPrefix = (int) interSmsPrice.get("prefix");
		String prefix = params.get("prefix");
		if(prefix != null && Integer.parseInt(prefix) != oldPrefix){
			//如果产品编号变化了，需要判断是否重复的
			int num = masterDao.getOneInfo("product.queryInterSMSNumByCode", params);
			if(num > 0){
				data.put("result", "fail");
				data.put("msg", "国际代码已经存在，请重新填写！");
				return data;
			}
		}
		int i = masterDao.update("product.updateInterSmsPriceInfo", params);
		if (i > 0) {
			data.put("result", "success");
			data.put("msg", "修改成功");
		} else {
			data.put("result", "fail");
			data.put("msg", "修改失败，请重新提交");
		}
//		logService.add(LogType.update, LogEnum.产品管理.getValue(),
//				"产品管理-国际短信价格费率配置：修改信息", params, data);
		Long userId = Long.valueOf(params.get("userId"));
		String pageUrl = params.get("pageUrl");
		String ip = params.get("ip"); 
		logService.add(LogType.update, LogEnum.产品管理.getValue(), userId, pageUrl, ip,
				"产品管理-国际短信价格费率配置：修改信息", params, data);

		return data;
	}

	/**
	 * 获取品牌订单中返点抵充比例
	 */
	@Override
	public List<Map<String, Object>> getAgentRebate(Map<String, String> params) {
		return masterDao.getSearchList("product.getAgentRebate", params);
	}

	/**
	 * 修改|添加|删除 品牌订单中返点抵充比例
	 */
	@Override
	public Map<String, Object> addOrUpdateRebate(Map<String, String> params) {
		Long userId = Long.valueOf(params.get("userId"));
		String pageUrl = params.get("pageUrl");
		String ip = params.get("ip"); 
		Map<String,Object> data = new HashMap<String,Object>();
		String action = params.get("edit_action");
//		params.put("updator", AuthorityUtils.getLoginUserId().toString());
		params.put("updator", userId.toString());
		if(action != null && "add".equals(action)){
			int insert = masterDao.insert("product.insertAgentRebate", params);
			data.put("result", insert > 0 ? "success" :"fail");
			data.put("msg", insert > 0 ? "添加成功" :"添加失败");
//			logService.add(LogType.add, LogEnum.产品管理.getValue(),
//					"产品管理- 品牌订单中返点抵充比例：添加返点比例", params, data);
			logService.add(LogType.add, LogEnum.产品管理.getValue(),userId, pageUrl, ip, 
					"产品管理- 品牌订单中返点抵充比例：添加返点比例", params, data);
		}else if(action != null && "update".equals(action)){
			int update = masterDao.update("product.updateAgentRebate", params);
			data.put("result", update > 0 ? "success" :"fail");
			data.put("msg", update > 0 ? "修改成功" :"修改失败");
//			logService.add(LogType.update, LogEnum.产品管理.getValue(),
//					"产品管理- 品牌订单中返点抵充比例：修改返点比例", params, data);
			logService.add(LogType.update, LogEnum.产品管理.getValue(),userId, pageUrl, ip,
					"产品管理- 品牌订单中返点抵充比例：修改返点比例", params, data);
		}else if(action != null && "delete".equals(action)){
			int delete = masterDao.delete("product.deleteAgentRebate", params);
			if(delete > 1){
				throw new RuntimeException("删除代理商返点比例异常:一次只能删除一条记录,当前删除记录数----->"+delete);
			}else{
				data.put("result", delete > 0 ? "success" :"fail");
				data.put("msg", delete > 0 ? "删除成功" :"删除失败");
//				logService.add(LogType.delete, LogEnum.产品管理.getValue(),
//						"产品管理- 品牌订单中返点抵充比例：删除返点比例", params, data);
				logService.add(LogType.delete, LogEnum.产品管理.getValue(),userId, pageUrl, ip,
						"产品管理- 品牌订单中返点抵充比例：删除返点比例", params, data);
			}
		}
		return data;
	}

	/**
	 * 添加国际短信
	 */
	@Override
	public Map<String, Object> addInterSmsPrice(Map<String, String> params) {
		LOGGER.debug("修改某一国际短信价格配制：" + params);
		Long userId = Long.valueOf(params.get("userId"));
		String pageUrl = params.get("pageUrl");
		String ip = params.get("ip"); 
		
		Map<String, Object> data = new HashMap<String, Object>();
		// 判断国际代码是否重复
		int num = masterDao.getOneInfo("product.queryInterSMSNumByCode", params);
		if(num > 0){
			data.put("result", "fail");
			data.put("msg", "国际代码已经存在，请重新填写！");
			return data;
		}
		int i = masterDao.insert("product.addInterSmsPrice", params);
		if (i > 0) {
			data.put("result", "success");
			data.put("msg", "添加成功");
		} else {
			data.put("result", "fail");
			data.put("msg", "添加失败，请重新提交");
		}
//		logService.add(LogType.add, LogEnum.产品管理.getValue(),
//				"产品管理-国际短信价格费率配置：添加国际短信价格", params, data);
		logService.add(LogType.add, LogEnum.产品管理.getValue(), userId, pageUrl, ip,
				"产品管理-国际短信价格费率配置：添加国际短信价格", params, data);

		return data;
	}

	/**
	 * 导出国际短信价格
	 */
	@Override
	public List<Map<String, Object>> exportInterSmsPrice(Map<String, String> params) {
		return masterDao.getSearchList("product.exportInterSmsPrice", params);
	}


	@Override
	public PageContainer querySmsPurchaseProduct(Map<String, String> params) {
		params.put("status","1");
		params.put("isShow","1");
		params.put("dueTimeAfter", DateTime.now().toString("yyyy-MM-dd HH:mm:ss"));
		JsmsPage jsmsPage = PageConvertUtil.paramToPage(params);
		jsmsOemProductInfoService.queryList(jsmsPage);
		List<OemProductInfoVO> result = new ArrayList();
		int rowNum = 1;
		JsmsAgentClientParam oem_gj_sms_discount = jsmsAgentClientParamService.getByParamKey("OEM_GJ_SMS_DISCOUNT");

		JsmsOemAgentProduct tempParam = new JsmsOemAgentProduct();

		for ( Object object : jsmsPage.getData()) {
			OemProductInfoVO temp = new OemProductInfoVO();
			BeanUtil.copyProperties(object,temp);
			temp.setRowNum((jsmsPage.getPage() - 1)*jsmsPage.getRows() + rowNum);
			temp.setOEM_GJ_SMS_DISCOUNT(oem_gj_sms_discount.getParamValue());
			tempParam.setAgentId(Integer.parseInt(params.get("agentId")));
			tempParam.setProductId(temp.getProductId());
			JsmsOemAgentProduct oemAgentProduct = jsmsOemAgentProductService.getByAgentIdAndProductId(tempParam);
			if(oemAgentProduct != null){
				temp.setDiscountPrice(oemAgentProduct.getDiscountPrice());
				temp.setGjSmsDiscount(oemAgentProduct.getGjSmsDiscount());
			}
			result.add(temp);
			++rowNum;
		}
		jsmsPage.setData(result);
		return PageConvertUtil.pageToContainer(jsmsPage);
	}


	@Override
	@Transactional
	public ResultVO confirmPurchaseOrder(List<PurchaseOrder> purchaseOrderList, Integer agentId, Long adminId) {

		JsmsAgentInfo agentInfo = jsmsAgentInfoService.getByAgentId(agentId);

		if (null == agentInfo){
			return ResultVO.failure("未查询到代理商相关信息");
		}

		if(agentInfo == null && !agentInfo.getOauthStatus().equals(3)){
			return ResultVO.failure("代理商尚未认证！请先认证...");
		}

		JsmsOemAgentOrder jsmsOemAgentOrder = new JsmsOemAgentOrder();
		Long orderId = CustomerManageServiceImpl.getClientOrderId();
		jsmsOemAgentOrder.setOrderId(orderId);
		jsmsOemAgentOrder.setOrderNo(orderId);
		boolean newOrderId = false;

		ResultVO resultVO = null;
		for (PurchaseOrder purchaseOrder : purchaseOrderList) {
			if(newOrderId){
				jsmsOemAgentOrder.setOrderId(CustomerManageServiceImpl.getClientOrderId());
			}
			newOrderId = true;
			resultVO = jsmsOEMAgentOrderProductService.purchaseOrder(agentInfo.getAgentId(),agentInfo.getRebateUseRadio(), purchaseOrder.getProductId(), purchaseOrder.getPurchaseNum(), adminId, jsmsOemAgentOrder);
		}

		return resultVO;
	}

}
