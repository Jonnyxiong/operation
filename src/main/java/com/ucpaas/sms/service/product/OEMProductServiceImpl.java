package com.ucpaas.sms.service.product;

import com.ucpaas.sms.constant.LogConstant.LogType;
import com.ucpaas.sms.dao.MessageMasterDao;
import com.ucpaas.sms.enums.LogEnum;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.common.LogService;
import com.ucpaas.sms.service.common.OEMCommonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: OEMProductServiceImpl
 * @Description: OEM产品管理
 * @author: Niu.T
 * @date: 2016年11月24日  下午5:47:02
 */
@Service
@Transactional
public class OEMProductServiceImpl implements OEMProductService {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(OEMProductServiceImpl.class);

	@Autowired
	private MessageMasterDao masterDao;
	@Autowired
	private LogService logService;
	@Autowired
	private OEMCommonService oemCommonService;

	/**
	 * 查询OEM产品
	 */
	@Override
	public PageContainer queryProduct(Map<String, String> params) {
		return masterDao.getSearchPage("oemProduct.queryProduct", "oemProduct.queryProductCount",params);
	}
	/**
	 * 添加OEM产品
	 */
	@Override
	public Map<String, Object> addProduct(Map<String, String> params) {
		return commonActioin(params,"添加OEM产品包",1);
	}
	/**
	 * 修改OEM产品
	 */
	@Override
	public Map<String, Object> editProduct(Map<String, String> params) {
		return commonActioin(params,"修改OEM产品包",2);
	}
	/**
	 * 根据产品id查询产品详情
	 */
	@Override
	public Map<String, Object> getProductDetail(int id) {
		Map<String,Object> data = masterDao.getOneInfo("oemProduct.getProductDetail", id);
		if(data != null && 2 == (int)data.get("product_type")){
			data.putAll(oemCommonService.getOneAgentParam("OEM_GJ_SMS_DISCOUNT"));
		}
		return data;
	}
	/**
	 * 修改产品状态: 上架 | 下架
	 */
	@Override
	@Deprecated
	public Map<String, Object> updateStatus(Map<String, String> params) {
		LOGGER.debug("修改产品包状态，上架/下架：" + params);
		Map<String, Object> data = new HashMap<String, Object>();
		
		Map<String, Object> map = new HashMap<>();
		map.putAll(params);
//		map.put("updator", AuthorityUtils.getLoginUserId().toString());
		int i = masterDao.update("oemProduct.updateStatus", map);
		if (i > 0) {
			data.put("result", "success");
			data.put("msg", "修改成功");
		} else {
			data.put("result", "fail");
			data.put("msg", "修改失败");
		}
//		logService.add(LogType.update, LogEnum.产品管理.getValue(),
//				"产品管理-产品包列表：修改产品包状态", params, data);
		Long userId = Long.valueOf(params.get("userId"));
		String pageUrl = params.get("pageUrl");
		String ip = params.get("ip"); 
		logService.add(LogType.update, LogEnum.产品管理.getValue(), userId, pageUrl, ip,
				"产品管理-OEM产品包管理-OEM产品包管理：修改产品包状态", params, data);
		return data;
	}
	/**
	 * @Description: 添加|修改 产品包公用方法
	 * @author: Niu.T 
	 * @date: 2016年11月25日    上午11:04:55
	 * @param params
	 * @param actionName
	 * @param actionType
	 * @return Map<String,Object>
	 */
	@Deprecated
	private Map<String,Object> commonActioin(Map<String, String> params,String actionName,int actionType){
		LOGGER.debug(actionName + params);
		Map<String, Object> data = new HashMap<String, Object>();
		params.put("due_time", params.get("due_time")+" 23:59:59");
		//判断是否有产品代码了
		int num = masterDao.getOneInfo("oemProduct.queryProductNumByCode", params);
		if(num > 0){
			data.put("result", "fail");
			data.put("msg", "产品代码已经存在，请重新填写！");
			return data;
		}
		int i = 0;
		String desc = "添加产品包";
		Long userId = Long.valueOf(params.get("userId"));
		if(actionType == 1){ // 添加 
//			params.put("isShow", "1");
			params.put("creator", userId.toString());
			params.put("status", "0");
			i = masterDao.insert("oemProduct.addProduct", params);
		}else if(actionType == 2){ // 修改
			desc = "修改产品包";
			params.put("updator", userId.toString());
			i = masterDao.update("oemProduct.editProduct", params);
		}
		if (i > 0) {
			data.put("result", "success");
			data.put("msg", actionName + "成功");
		} else {
			data.put("result", "fail");
			data.put("msg", actionName + "失败");
		}
//		logService.add(LogType.add, LogEnum.产品管理.getValue(),
//				"产品管理-产品包列表：" + actionName, params, data);

		String pageUrl = params.get("pageUrl");
		String ip = params.get("ip"); 
		logService.add(actionType == 1 ? LogType.add : LogType.update, LogEnum.产品管理.getValue(), userId, pageUrl, ip,
				"产品管理-OEM产品包管理-OEM产品包管理："+desc+"：" + actionName, params, data);

		return data;
	}
	/**
	 * 获取OEM代理商属性配置
	 */
	@Override
	public List<Map<String, Object>> getAgentRebate(Map<String, String> params) {

		return masterDao.getSearchList("oemProduct.getAgentRebate", params);
	}
	
	
	
}
