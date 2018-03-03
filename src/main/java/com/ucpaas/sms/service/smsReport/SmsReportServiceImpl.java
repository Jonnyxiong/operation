package com.ucpaas.sms.service.smsReport;

import com.ucpaas.sms.dao.AccessSlaveDao;
import com.ucpaas.sms.dao.MessageMasterDao;
import com.ucpaas.sms.model.PageContainer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SmsReportServiceImpl implements SmsReportService {

	@Autowired
	private MessageMasterDao masterDao;
	@Autowired
	private AccessSlaveDao messageStatSlaveDao;

	/**
	 * 传统短信(标准短信)统计
	 */
	@Override
	public PageContainer stdSmsQuery(Map<String, String> params) {

		String text = params.get("text");
		//销售人员用户id列表
		List<Long> saleIdList = new ArrayList<>();
		if(text != null && !"".equals(text)){
			//查询销售名称对应的id
			List<Map<String, Object>> saleIdMapList = masterDao.getSearchList("smsReport.getUserIdByUserName", params);
			if(saleIdMapList != null && saleIdMapList.size() != 0){
				for(Map<String, Object> map : saleIdMapList){
					saleIdList.add(Long.valueOf(map.get("id").toString()));
				}
			}
		}

		Map<String,Object> paramsObj = new HashMap<>();
		paramsObj.putAll(params);
		List<Integer> product_types = new ArrayList<>();
		product_types.add(0);
		product_types.add(1);
		product_types.add(2);
		product_types.add(9);
		paramsObj.put("product_types", product_types); // 按产品类型统计 0：行业，1：营销，2：国际，7：USSD，8：闪信，9：挂机短信
		if(saleIdList.size() != 0){
			paramsObj.put("saleIdList",saleIdList);
		}
		PageContainer dataPage = messageStatSlaveDao.getSearchPage("smsReportCommon.smsQuery",
				"smsReportCommon.smsQueryCount", paramsObj);

		List<Map<String, Object>> dataList = dataPage.getList();
		if (dataList !=null && dataList.size() > 0) {

			List<String> idList = new ArrayList<>();
			List<Long> saleList = new ArrayList<>();
			for (Map<String, Object> map : dataList) {
				idList.add((String) map.get("clientid"));
				if(map.get("belong_sale") != null){
					saleList.add((Long) map.get("belong_sale"));
				}
			}
			List<Map<String, Object>> clientList = masterDao.getSearchList("smsReport.getClientAssociatedInfo", idList);
			for (Map<String, Object> map : dataList) {
				for (Map<String, Object> info : clientList) {
					if (map.get("clientid").equals(info.get("clientid"))) {
						map.putAll(info);
					}
				}
			}

			if(saleList.size() != 0){
				List<Map<String, Object>> saleInfoMapList = masterDao.getSearchList("smsReport.getUserInfoById", saleList);
				for(Map<String, Object> map : dataList){
					for(Map<String, Object> saleInfo : saleInfoMapList){
						if(map.get("belong_sale") != null){
							if(map.get("belong_sale").equals(saleInfo.get("id"))){
								map.put("belong_sale_name",saleInfo.get("realname"));
							}
						}
					}
				}
			}

			dataPage.setList(dataList);
		}
		return dataPage;
	}

	/**
	 * 传统短信(标准短信)统计 (用于导出Excel)
	 */
	@Override
	public List<Map<String, Object>> stdSmsExcel(Map<String, String> params) {

		String text = params.get("text");
		//销售人员用户id列表
		List<Long> saleIdList = new ArrayList<>();
		if(text != null && !"".equals(text)){
			//查询销售名称对应的id
			List<Map<String, Object>> saleIdMapList = masterDao.getSearchList("smsReport.getUserIdByUserName", params);
			if(saleIdMapList != null && saleIdMapList.size() != 0){
				for(Map<String, Object> map : saleIdMapList){
					saleIdList.add(Long.valueOf(map.get("id").toString()));
				}
			}
		}

		Map<String,Object> paramsObj = new HashMap<>();
		paramsObj.putAll(params);
		List<Integer> product_types = new ArrayList<>();
		product_types.add(0);
		product_types.add(1);
		product_types.add(2);
		product_types.add(9);
		paramsObj.put("product_types", product_types); // 按产品类型统计 0：行业，1：营销，2：国际，7：USSD，8：闪信，9：挂机短信
		if(saleIdList.size() != 0){
			paramsObj.put("saleIdList",saleIdList);
		}

		List<Map<String, Object>> dataList = messageStatSlaveDao.getSearchList("smsReportCommon.smsExcel", paramsObj);
		if (dataList.size() > 0) {
			List<String> idList = new ArrayList<>();
			List<Long> saleList = new ArrayList<>();
			for (Map<String, Object> map : dataList) {
				idList.add((String) map.get("clientid"));
				if(map.get("belong_sale") != null){
					saleList.add((Long) map.get("belong_sale"));
				}

			}
			List<Map<String, Object>> clientList = masterDao.getSearchList("smsReport.getClientAssociatedInfo", idList);
			for (Map<String, Object> map : dataList) {
				for (Map<String, Object> info : clientList) {
					if (map.get("clientid").equals(info.get("clientid"))) {
						map.putAll(info);
					}
				}
			}

			if(saleList.size() != 0){
				List<Map<String, Object>> saleInfoMapList = masterDao.getSearchList("smsReport.getUserInfoById", saleList);
				for(Map<String, Object> map : dataList){
					for(Map<String, Object> saleInfo : saleInfoMapList){
						if(map.get("belong_sale") != null){
							if(map.get("belong_sale").equals(saleInfo.get("id"))){
								map.put("belong_sale_name",saleInfo.get("realname"));
							}
						}
					}
				}
			}
		}
		return dataList;
	}

	/**
	 * 闪信统计
	 */
	@Override
	public PageContainer flashSmsQuery(Map<String, String> params) {

		String text = params.get("text");
		//销售人员用户id列表
		List<Long> saleIdList = new ArrayList<>();
		if(text != null && !"".equals(text)){
			//查询销售名称对应的id
			List<Map<String, Object>> saleIdMapList = masterDao.getSearchList("smsReport.getUserIdByUserName", params);
			if(saleIdMapList != null && saleIdMapList.size() != 0){
				for(Map<String, Object> map : saleIdMapList){
					saleIdList.add(Long.valueOf(map.get("id").toString()));
				}
			}
		}

		Map<String,Object> paramsObj = new HashMap<>();
		paramsObj.putAll(params);
		List<Integer> product_types = new ArrayList<>();
		product_types.add(8);
		paramsObj.put("product_types", product_types); // 按产品类型统计 0：行业，1：营销，2：国际，7：USSD，8：闪信，9：挂机短信
		if(saleIdList.size() != 0){
			paramsObj.put("saleIdList",saleIdList);
		}

		PageContainer dataPage = messageStatSlaveDao.getSearchPage("smsReportCommon.smsQuery",
				"smsReportCommon.smsQueryCount", paramsObj);
		List<Map<String, Object>> dataList = dataPage.getList();

		if (dataList.size() > 0) {
			List<String> idList = new ArrayList<>();
			List<Long> saleList = new ArrayList<>();
			for (Map<String, Object> map : dataList) {
				idList.add((String) map.get("clientid"));
				if(map.get("belong_sale") != null){
					saleList.add((Long) map.get("belong_sale"));
				}
			}
			List<Map<String, Object>> clientList = masterDao.getSearchList("smsReport.getClientAssociatedInfo", idList);
			for (Map<String, Object> map : dataList) {
				for (Map<String, Object> info : clientList) {
					if (map.get("clientid").equals(info.get("clientid"))) {
						map.putAll(info);
					}
				}
			}

			if(saleList.size() != 0){
				List<Map<String, Object>> saleInfoMapList = masterDao.getSearchList("smsReport.getUserInfoById", saleList);
				for(Map<String, Object> map : dataList){
					for(Map<String, Object> saleInfo : saleInfoMapList){
						if(map.get("belong_sale") != null){
							if(map.get("belong_sale").equals(saleInfo.get("id"))){
								map.put("belong_sale_name",saleInfo.get("realname"));
							}
						}
					}
				}
			}

			dataPage.setList(dataList);
		}
		return dataPage;
	}

	/**
	 * 闪信统计(用于导出Excel)
	 */
	@Override
	public List<Map<String, Object>> flashSmsExcel(Map<String, String> params) {

		String text = params.get("text");
		//销售人员用户id列表
		List<Long> saleIdList = new ArrayList<>();
		if(text != null && !"".equals(text)){
			//查询销售名称对应的id
			List<Map<String, Object>> saleIdMapList = masterDao.getSearchList("smsReport.getUserIdByUserName", params);
			if(saleIdMapList != null && saleIdMapList.size() != 0){
				for(Map<String, Object> map : saleIdMapList){
					saleIdList.add(Long.valueOf(map.get("id").toString()));
				}
			}
		}

		Map<String,Object> paramsObj = new HashMap<>();
		paramsObj.putAll(params);
		List<Integer> product_types = new ArrayList<>();
		product_types.add(8);
		paramsObj.put("product_types", product_types); // 按产品类型统计 0：行业，1：营销，2：国际，7：USSD，8：闪信，9：挂机短信
		if(saleIdList.size() != 0){
			paramsObj.put("saleIdList",saleIdList);
		}

		List<Map<String, Object>> dataList = messageStatSlaveDao.getSearchList("smsReportCommon.smsExcel", paramsObj);

		if (dataList.size() > 0) {
			List<String> idList = new ArrayList<String>();
			List<Long> saleList = new ArrayList<>();
			for (Map<String, Object> map : dataList) {
				idList.add((String) map.get("clientid"));
				if(map.get("belong_sale") != null){
					saleList.add((Long) map.get("belong_sale"));
				}
			}


			List<Map<String, Object>> clientList = masterDao.getSearchList("smsReport.getClientAssociatedInfo", idList);
			for (Map<String, Object> map : dataList) {
				for (Map<String, Object> info : clientList) {
					if (map.get("clientid").equals(info.get("clientid"))) {
						map.putAll(info);
					}
				}
			}

			if(saleList.size() != 0){
				List<Map<String, Object>> saleInfoMapList = masterDao.getSearchList("smsReport.getUserInfoById", saleList);
				for(Map<String, Object> map : dataList){
					for(Map<String, Object> saleInfo : saleInfoMapList){
						if(map.get("belong_sale") != null){
							if(map.get("belong_sale").equals(saleInfo.get("id"))){
								map.put("belong_sale_name",saleInfo.get("realname"));
							}
						}
					}
				}
			}

		}
		return dataList;
	}

	/**
	 * USSD统计
	 */
	@Override
	public PageContainer ussdSmsQuery(Map<String, String> params) {

		String text = params.get("text");
		//销售人员用户id列表
		List<Long> saleIdList = new ArrayList<>();
		if(text != null && !"".equals(text)){
			//查询销售名称对应的id
			List<Map<String, Object>> saleIdMapList = masterDao.getSearchList("smsReport.getUserIdByUserName", params);
			if(saleIdMapList != null && saleIdMapList.size() != 0){
				for(Map<String, Object> map : saleIdMapList){
					saleIdList.add(Long.valueOf(map.get("id").toString()));
				}
			}
		}

		Map<String,Object> paramsObj = new HashMap<>();
		paramsObj.putAll(params);
		List<Integer> product_types = new ArrayList<>();
		product_types.add(7);
		paramsObj.put("product_types", product_types); // 按产品类型统计 0：行业，1：营销，2：国际，7：USSD，8：闪信，9：挂机短信

		if(saleIdList.size() != 0){
			paramsObj.put("saleIdList",saleIdList);
		}


		PageContainer dataPage = messageStatSlaveDao.getSearchPage("smsReportCommon.smsQuery",
				"smsReportCommon.smsQueryCount", paramsObj);
		List<Map<String, Object>> dataList = dataPage.getList();

		if (dataList !=null && dataList.size() > 0) {
			List<String> idList = new ArrayList<>();
			List<Long> saleList = new ArrayList<>();
			for (Map<String, Object> map : dataList) {
				idList.add((String) map.get("clientid"));
				if(map.get("belong_sale") != null){
					saleList.add((Long) map.get("belong_sale"));
				}
			}
			List<Map<String, Object>> clientList = masterDao.getSearchList("smsReport.getClientAssociatedInfo", idList);
			for (Map<String, Object> map : dataList) {
				for (Map<String, Object> info : clientList) {
					if (map.get("clientid").equals(info.get("clientid"))) {
						map.putAll(info);
					}
				}
			}

			if(saleList.size() != 0){
				List<Map<String, Object>> saleInfoMapList = masterDao.getSearchList("smsReport.getUserInfoById", saleList);
				for(Map<String, Object> map : dataList){
					for(Map<String, Object> saleInfo : saleInfoMapList){
						if(map.get("belong_sale") != null){
							if(map.get("belong_sale").equals(saleInfo.get("id"))){
								map.put("belong_sale_name",saleInfo.get("realname"));
							}
						}
					}
				}
			}

			dataPage.setList(dataList);
		}
		return dataPage;
	}

	/**
	 * USSD统计(用于导出Excel)
	 */
	@Override
	public List<Map<String, Object>> ussdSmsExcel(Map<String, String> params) {

		String text = params.get("text");
		//销售人员用户id列表
		List<Long> saleIdList = new ArrayList<>();
		if(text != null && !"".equals(text)){
			//查询销售名称对应的id
			List<Map<String, Object>> saleIdMapList = masterDao.getSearchList("smsReport.getUserIdByUserName", params);
			if(saleIdMapList != null && saleIdMapList.size() != 0){
				for(Map<String, Object> map : saleIdMapList){
					saleIdList.add(Long.valueOf(map.get("id").toString()));
				}
			}
		}

		Map<String,Object> paramsObj = new HashMap<>();
		paramsObj.putAll(params);
		List<Integer> product_types = new ArrayList<>();
		product_types.add(7);
		paramsObj.put("product_types", product_types); // 按产品类型统计 0：行业，1：营销，2：国际，7：USSD，8：闪信，9：挂机短信

		if(saleIdList.size() != 0){
			paramsObj.put("saleIdList",saleIdList);
		}

		List<Map<String, Object>> dataList = messageStatSlaveDao.getSearchList("smsReportCommon.smsExcel", paramsObj);
		if (dataList.size() > 0) {
			List<String> idList = new ArrayList<>();
			List<Long> saleList = new ArrayList<>();
			for (Map<String, Object> map : dataList) {
				idList.add((String) map.get("clientid"));
				if(map.get("belong_sale") != null){
					saleList.add((Long) map.get("belong_sale"));
				}
			}

			List<Map<String, Object>> clientList = masterDao.getSearchList("smsReport.getClientAssociatedInfo", idList);
			for (Map<String, Object> map : dataList) {
				for (Map<String, Object> info : clientList) {
					if (map.get("clientid").equals(info.get("clientid"))) {
						map.putAll(info);
					}
				}
			}

			if(saleList.size() != 0){
				List<Map<String, Object>> saleInfoMapList = masterDao.getSearchList("smsReport.getUserInfoById", saleList);
				for(Map<String, Object> map : dataList){
					for(Map<String, Object> saleInfo : saleInfoMapList){
						if(map.get("belong_sale") != null){
							if(map.get("belong_sale").equals(saleInfo.get("id"))){
								map.put("belong_sale_name",saleInfo.get("realname"));
							}
						}
					}
				}
			}


		}
		return dataList;
	}

	@Override
	public Map stdSmsTotal(Map<String, String> params) {
		Map<String,Object> paramsObj = new HashMap<>();
		paramsObj.putAll(params);
		List<Integer> product_types = new ArrayList<>();
		product_types.add(0);
		product_types.add(1);
		product_types.add(2);
		product_types.add(9);
		paramsObj.put("product_types", product_types); // 按产品类型统计 0：行业，1：营销，2：国际，7：USSD，8：闪信，9：挂机短信 
		Map data = messageStatSlaveDao.getOneInfo("smsReportCommon.smsTotal", paramsObj);
		if (data == null) {
			data = new HashMap<>();
			data.put("cusSubTotal", "0");
			data.put("successTotal", "0");
			data.put("failTotal", "0");
			data.put("chargetotal", "0");
			data.put("notsend", "0");
			data.put("submitsuccess", "0");
			data.put("salefee", "0");
			data.put("productfee", "0");
			data.put("costfee", "0");
		}
		return data;
	}

	@Override
	public Map smsTotal(Map<String, Object> paramsObj) {
		//销售人员用户id列表
		Object text = paramsObj.get("text");
		List<Long> saleIdList = new ArrayList<>();
		if(text != null && StringUtils.isNotBlank(text.toString())){
			//查询销售名称对应的id
			List<Map<String, Object>> saleIdMapList = masterDao.getSearchList("smsReport.getUserIdByUserName", paramsObj);
			if(saleIdMapList != null && saleIdMapList.size() > 0){
				for(Map<String, Object> map : saleIdMapList){
					saleIdList.add(Long.valueOf(map.get("id").toString()));
				}
			}
		}
		if(saleIdList.size() != 0){
			paramsObj.put("saleIdList",saleIdList);
		}

		Map data = messageStatSlaveDao.getOneInfo("smsReportCommon.smsTotal", paramsObj);
		if (data == null) {
			data = new HashMap<>();
			data.put("cusSubTotal", "0");
			data.put("successTotal", "0");
			data.put("failTotal", "0");
			data.put("chargetotal", "0");
			data.put("notsend", "0");
			data.put("submitsuccess", "0");
			data.put("salefee", "0");
			data.put("productfee", "0");
			data.put("costfee", "0");
		}
		return data;
	}
 

}
