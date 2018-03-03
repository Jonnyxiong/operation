package com.ucpaas.sms.service;

import com.jsmsframework.user.entity.JsmsAccount;
import com.jsmsframework.user.service.JsmsAccountService;
import com.ucpaas.sms.dao.AccessMasterDao;
import com.ucpaas.sms.dao.MessageMasterDao;
import com.ucpaas.sms.model.PageContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@Transactional
public class CustomerSendRecordServiceImpl implements CustomerSendRecordService{
	@Autowired
	private JsmsAccountService jsmsAccountService;
	//	@Autowired
//	private CustomerSendRecordDao customerSendRecordDao;
	@Autowired
	private AccessMasterDao accessDao ;
	@Autowired
	private MessageMasterDao masterDao;
	@Override
	public PageContainer querySmsSendRecord(Map<String, Object> params) {
		String start_time = (String) params.get("start_time");
		String end_time = (String) params.get("end_time");
		//List<String> tableList = new ArrayList<>();

		String obj = (String) params.get("clientid");
		//拿到了对应的实体类
		JsmsAccount jsmsAccount = jsmsAccountService.getByClientId(obj);
		Integer identify = jsmsAccount.getIdentify();

		String data = "20170904";
		String table = "t_sms_access_"+identify+"_"+data ;

		return accessDao.getSearchPage("customer.querySmsSendRecordOem", "customer.queryAll", params);



	}
	@Override
	public List<Map<String, Object>> queryAccountByBelongSale(Map<String, Object> params) {
		return masterDao.getSearchList("customer.queryAccountByBelongSale", params);
	}
	@Override
	public PageContainer queryAll(Map<String, Object> params) {
		String obj = (String) params.get("account_id");
		JsmsAccount jsmsAccount = jsmsAccountService.getByClientId(obj);
		if(jsmsAccount == null){
			return null;
		}
		Integer identify = jsmsAccount.getIdentify();
		String d = (String) params.get("data");
		String data  = d.replaceAll("-", "");
		String table = "t_sms_access_"+identify+"_"+ data;
		params.put("table", table);
		PageContainer searchPage = accessDao.getSearchPage("customer.queryCustomerSendRecord", "customer.queryCustomerSendRecordCount", params);
		return searchPage;
	}
	@Override
	public List<Map<String, Object>> querySmsRecord4Excel(Map<String, Object> params) {
		// TODO Auto-generated method stub
		String obj = (String) params.get("account_id");
		JsmsAccount jsmsAccount = jsmsAccountService.getByClientId(obj);
		Integer identify = jsmsAccount.getIdentify();
		String d = (String) params.get("data");
		String data  = d.replaceAll("-", "");
//		Date now = new Date(); 
//		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
//		String date = dateFormat.format(now);
		String table = "t_sms_access_"+identify+"_"+ d;
		params.put("table", table);
//		Object start_time = params.get("start_time").toString();
//		Object end_time = params.get("end_time").toString();
		List<Map<String, Object>> list = accessDao.getSearchList("customer.querySmsRecord4Excell", params);
//		List<String> tableListTemp_old = commonService.getExistTable(DbTablePrefix.T_SMS_ACCESS_YYYYMMDD, start_time,
//				end_time);
//		List<String> tableListTemp_new = commonService.getExistTable(DbTablePrefix.T_SMS_ACCESS_YYYYMMDD, start_time,
//				end_time, params.get("clientid").toString());
//		
		return list;
	}
}
