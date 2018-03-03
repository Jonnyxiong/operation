package com.ucpaas.sms.service.smsReport;

import java.util.List;
import java.util.Map;

import com.ucpaas.sms.model.PageContainer;

/**
 * 运营平台-代理商客户短信报表
 * 
 * @author oylx
 */
public interface SmsReportService {

	/**
	 * 查询
	 * 
	 * @param params
	 * @return
	 */
	PageContainer stdSmsQuery(Map<String, String> params);
	
	/**
	 * 查询(用于Excel导出)
	 * 
	 * @param params
	 * @return
	 */
	List<Map<String, Object>> stdSmsExcel(Map<String, String> params);

	/**
	 * @Description: 闪信统计
	 * @author: Niu.T 
	 * @date: 2017年1月3日    上午10:53:37
	 * @param params
	 * @return PageContainer
	 */
	PageContainer flashSmsQuery(Map<String, String> params);
	
	/**
	 * @Description: 闪信统计(用于导出Excel)
	 * @author: Niu.T 
	 * @date: 2017年1月3日    上午10:53:56
	 * @param params
	 * @return List<Map<String,Object>>
	 */
	List<Map<String, Object>> flashSmsExcel(Map<String, String> params);

	/**
	 * @Description: ussd统计
	 * @author: Niu.T 
	 * @date: 2017年1月3日    上午10:54:40
	 * @param params
	 * @return PageContainer
	 */
	PageContainer ussdSmsQuery(Map<String, String> params);
	
	/**
	 * @Description: ussd统计(用于导出Excel)
	 * @author: Niu.T 
	 * @date: 2017年1月3日    上午10:54:59
	 * @param params
	 * @return List<Map<String,Object>>
	 */
	List<Map<String, Object>> ussdSmsExcel(Map<String, String> params);

	/**
	 * 传统短信-查询 总计栏
	 * @param params
	 * @return
	 */
	Map stdSmsTotal(Map<String, String> params); 
	
	
	/**
	 * 公共短信-查询 总计栏
	 * @param params
	 * @return
	 */
	Map smsTotal(Map<String, Object> paramsObj);
	
	
}
