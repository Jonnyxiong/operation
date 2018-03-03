package com.ucpaas.sms.util;


import com.jsmsframework.common.enums.WebIdNew;
import com.ucpaas.sms.common.util.DateUtils;
import com.ucpaas.sms.service.audit.CustomerAuditService;
import com.ucpaas.sms.service.common.CommonService;
import com.ucpaas.sms.util.enums.StaticInitVariable;
import com.ucpaas.sms.util.rest.utils.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.FileInputStream;
import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 系统配置工具类
 * 
 * @author xiejiaan
 */
@Component
public class ConfigUtils {
	private static final Logger logger = LoggerFactory.getLogger(ConfigUtils.class);
	
	@Autowired
	private CommonService commonService;

	@Autowired
	private CustomerAuditService customerAuditService;
	
	public static String system_version;
//	/**
//	 * 运行环境：development（开发）、devtest（开发测试）、test（测试）、production（线上）
//	 */
//	public static String spring_profiles_active;
	/**
	 * 当前平台的web_id
	 */
	public static String web_id;
	/**
	 * 是否自动登录
	 */
	public static boolean is_auto_login;

	/**
	 * 配置文件路径
	 */
	public static String config_file_path;

	/**
	 * UEdiotr配置文件路径
	 */
	public static String ueditor_config_file_path;
	
	/**
	 * smsp-access短信请求URL
	 */
	public static String smsp_access_url;
	
	/**
	 * smsp-access短信请求clientid
	 */
	public static String smsp_access_clientid;
	
	/**
	 * smsp-access短信请求password
	 */
	public static String smsp_access_password;
	
	/**
	 * 重置密码路径
	 */
	public static String smap_resetpwd_url;
	
	/**
	 * 公用的代理商服务器站点地址
	 */
	public static String agent_site_url;

	/**
	 * oem代理商服务器站点地址
	 */
	public static String oem_agent_site_url;

	/**
	 * 公用的代理商服务器站点地址
	 */
	public static String client_site_url;
	/**
	 * 页面允许导出Excel最大记录数
	 */
	public static String max_export_excel_num;
	/**
	 * <pre>
	 * 接口地址：刷新前台缓存信息
	 * 
	 * 主账号key=main:[sid]
	 * 应用key=app:[appSid]
	 * 子账户key=client:[clientNumber]
	 * 白名单key=wl:[appSid]
	 * 短信模板key=tl:[templateId]
	 * </pre>
	 */
	public static String interface_url_flush;

	/**
	 * 接口地址：app审核通过后，分配短信号码
	 */
	public static String interface_url_getMsgNbr;

	/**
	 * rest接口的域名
	 */
	public static String rest_domain;
	/**
	 * rest接口的版本
	 */
	public static String rest_version;
	/**
	 * 前台站点的域名
	 */
	public static String ucpaas_domain;
	/**
	 * 文件本地保存路径
	 */
	public static String save_path;
	/**
	 * 平台标志 ,用于区分客户OEM平台
	 */
	public static String platform_order_identify;
	/**
	 * 图片服务器地址
	 */
	public static String smsp_img_url;
	/**
	 * 模板审核结果推送频率 ,单位秒
	 */
	public static String template_authorize_period;
	/**
	 * 模板审核结果推送次数
	 */
	public static String template_authorize_time;

	/**
	 * 模板审核结果推送超时,单位秒
	 */
	public static String template_authorize_timeout;

	/**
	 * 运营平台下单标识为4
	 */
	public static String platform_oem_agent_order_identify;

	public static String environmentFlag;

	public static String client_oauth_pic;




	/**
	 * 初始化
	 */
	@PostConstruct
	public void init() {
		String path = ConfigUtils.class.getClassLoader().getResource("").getPath() ;
//		spring_profiles_active = System.getProperty("spring.profiles.active");
		config_file_path = path + "system.properties";

		initValue();
		logger.info("\n\n-------------------------【smsp-operation_v{}，服务器启动】\n加载配置文件：\n{}\n", system_version,
				config_file_path);

		this.initAgentIdPre();

		this.initResourceId();
		this.initDemandId();

		this.initDemandOrderIdSuf();
		this.initAgentOrderIdPreForOem();
		this.initClientOrderIdPreForOem();
		this.initAgentInvoiceId();
	}

	/**
	 * 初始化配置项的值
	 */
	private void initValue() {
		Field[] fields = ConfigUtils.class.getFields();
		Object fieldValue = null;
		String name = null, value = null, tmp = null;
		Class<?> type = null;
		Pattern pattern = Pattern.compile("\\$\\{(.+?)\\}");
		Matcher matcher = null;
		try {
			Properties properties = new Properties();
			properties.load(new FileInputStream(config_file_path));

			for (Field field : fields) {
				name = field.getName();
				value = properties.getProperty(name);
				if (StringUtils.isNotBlank(value)) {
					matcher = pattern.matcher(value);
					while (matcher.find()) {
						tmp = properties.getProperty(matcher.group(1));
						if (StringUtils.isBlank(tmp)) {
							logger.error("配置{}存在其它配置{}，请检查您的配置文件", name, matcher.group(1));
						}
						value = value.replace(matcher.group(0), tmp);
					}

					type = field.getType();
					if (String.class.equals(type)) {
						fieldValue = value;
					} else if (Integer.class.equals(type)) {
						fieldValue = Integer.valueOf(value);
					} else if (Boolean.class.equals(type)) {
						fieldValue = Boolean.valueOf(value);
					} else {
						fieldValue = value;
					}
					field.set(this, fieldValue);
				}
				logger.info("加载配置：{}={}", name, field.get(this));
			}
		} catch (Throwable e) {
			logger.error("初始化配置项的值失败：" + name + "=" + value, e);
		}
	}

	/**
	 * 初始化代理商订单id前缀
	 */
	private void initAgentIdPre(){
		
		Date date = new Date();
		int num = 0;
		String agentIdPre = DateUtil.dateToStr(date, "yyyyMM");
		
		String numStr = commonService.getMostAgentNumForMonth(agentIdPre);
		if(numStr == null){
			num = 1;
		}else{
			num = Integer.valueOf(numStr) + 1;
		}
		StaticInitVariable.AGENTID_PRE = agentIdPre;
		StaticInitVariable.AGENT_NUM = num;
	}


	/**
	 * 初始化资源Id
	 */
	private void initResourceId() {
		int count = commonService.getMaxResourceIdSeq();
		if (count == -1 || "".equals(StaticInitVariable.RESOURCE_ID_SUFFIX)){
			StaticInitVariable.RESOURCE_ID_SUFFIX = DateUtil.dateToStr(Calendar.getInstance().getTime(), 2);
		}
		StaticInitVariable.RESOURCE_ID_SEQ = count;
	}

	/**
	 * 初始化需求Id
	 */
	private void initDemandId() {
		int count = commonService.getMaxDemandIdSeq();
		if (count == -1 || "".equals(StaticInitVariable.DEMAND_ID_SUFFIX)){
			StaticInitVariable.DEMAND_ID_SUFFIX = DateUtil.dateToStr(Calendar.getInstance().getTime(), 2);
		}
		StaticInitVariable.DEMAND_ID_SEQ = count;
	}

	/**
	 * 初始化需求订单id前缀
	 */
	private void initDemandOrderIdSuf(){
		Date date = new Date();
		int num = 0;

		String orderIdSuf = DateUtil.dateToStr(date, "yyyyMMdd");
		String numStr = commonService.getMostDemandOrderNumForDay(orderIdSuf);
		if(numStr == null){
			num = 0;
		}else{
			num = Integer.valueOf(numStr) + 1;
		}

		StaticInitVariable.DEMAND_ORDER_NUM = num;
		StaticInitVariable.DEMAND_ORDERID_SUF = orderIdSuf;
	}
	private void initAgentOrderIdPreForOem(){

		Date date = new Date();
		int num = 0;
		//后面的1代表代理商下单
		String orderIdPre = DateUtil.dateToStr(date,"yyMMdd") + DateUtil.dateToStr(date, "HHmm")+ ConfigUtils.platform_oem_agent_order_identify;//运营平台下单标识为4
		String numStr =  customerAuditService.getOemAgentOrderTheMostNumForMinute(orderIdPre);
		if(numStr == null){
			num = 1;
		}else{
			num = Integer.valueOf(numStr) + 1;
		}

		StaticInitVariable.OEM_AGENT_ORDERID_PRE = orderIdPre;
		StaticInitVariable.OEM_AGENT_ORDER_NUM = num;
	}

	private void initClientOrderIdPreForOem(){

		Date date = new Date();
		int num = 0;
		//后面的1代表代理商下单
		String orderIdPre = DateUtil.dateToStr(date,"yyMMdd") + DateUtil.dateToStr(date, "HHmm")+ ConfigUtils.platform_oem_agent_order_identify;// oem订单标识3
		String numStr =  customerAuditService.getOemClientOrderTheMostNumForMinute(orderIdPre);
		if(numStr == null){
			num = 1;
		}else{
			num = Integer.valueOf(numStr) + 1;
		}

		StaticInitVariable.OEM_CLIENT_ORDERID_PRE = orderIdPre;
		StaticInitVariable.OEM_CLIENT_ORDER_NUM = num;
	}

	private void initAgentInvoiceId(){
		Date date = new Date();
		int num = 0;

		//后面的1代表代理商下单
		String idPre = "I"+ DateUtils.formatDate(date, "yyyyMMdd") + "000"+ WebIdNew.运营平台.getValue();
		String numStr =  commonService.getMaxInvoiceIdSeq(idPre);
		if(numStr == null){
			num = 1;
		}else{
			num = Integer.valueOf(numStr) + 1;
		}

		StaticInitVariable.AGENT_INVOICE_ID_PRE = idPre;
		StaticInitVariable.AGENT_INVOICE_ID_NUM = num;
	}

}
