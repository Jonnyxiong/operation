package com.ucpaas.sms.service.audit;

import java.util.Map;



import org.springframework.scheduling.annotation.Async;

import com.ucpaas.sms.entity.Template;
import com.ucpaas.sms.model.PageContainer;
/**
 * @ClassName: SmsTemplateService
 * @Description: 短信模板审核 service
 * @author: Niu.T
 * @date: 2016年12月28日  上午9:32:50
 */
public interface SmsTemplateService {
	/**
	 * @Description: 查询短信模板
	 * @author: Niu.T 
	 * @date: 2016年12月28日    上午9:32:45
	 * @param params
	 * @return PageContainer
	 */
	PageContainer query(Map<String, String> params);
	/**
	 * @Description: 查看模板信息详情
	 * @author: Niu.T 
	 * @date: 2016年12月28日    上午9:33:12
	 * @param params
	 * @return Map<String,Object>
	 */
	Map<String, Object> view(Map<String, String> params); 
	/**
	 * @Description: 模板审核
	 * @author: Niu.T 
	 * @date: 2016年12月29日    上午10:55:44
	 * @param params
	 * @return Map<String,Object>
	 */
	Map<String, Object> audit(Map<String, String> params); 
	
	/**
	 * @Description: 录入数据(转审结果)
	 * @author: Niu.T 
	 * @date: 2016年12月29日    下午3:37:09
	 * @param params
	 * @return Map<String,Object>
	 */
	Map<String,Object> auditTransfer(Map<String,String> params);
	
    /**
     * @Description 审核结果通知
     * @return
     * @author wangwei
     * @date 2017年3月6日 下午4:52:01
     */
    @Async
    void auditNotice(String clientId,Template template);

}
