package com.ucpaas.sms.service.account;

import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.common.util.DateUtils;
import com.ucpaas.sms.common.util.MD5;
import com.ucpaas.sms.common.util.SecurityUtils;
import com.ucpaas.sms.constant.LogConstant.LogType;
import com.ucpaas.sms.dao.MessageMasterDao;
import com.ucpaas.sms.enums.LogEnum;
import com.ucpaas.sms.exception.OperationException;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.po.ProductInfo;
import com.ucpaas.sms.po.SaleEntityPo;
import com.ucpaas.sms.service.common.LogService;
import com.ucpaas.sms.service.email.EmailService;
import com.ucpaas.sms.util.AgentIdUtil;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.JacksonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @ClassName: ApplyRecordServiceImpl  
 * @Description: SMSP运营平台:账户信息管理  - > 代理商申请记录
 * @author: Niu.T 
 * @date: 2016年9月6日 上午11:15:43
 */
@Service
@Transactional
public class ApplyRecordServiceImpl implements ApplyRecordService {

	private static final Logger logger = LoggerFactory.getLogger(ApplyRecordServiceImpl.class);

	@Autowired
	private MessageMasterDao masterDao;
	@Autowired
	private EmailService emailService;
	@Autowired
	private LogService logService;

	/**
	 * 分页查询的数据
	 */
	@Override
	public PageContainer query(Map<String, String> params) {
		return masterDao.getSearchPage("agentApplyRecord.query", "agentApplyRecord.queryCount", params);
	}
	
	/**
	 * 账户信息管理 - > 受理代理商与否
	 */
	@Override
	public Map<String, Object> acceptOrNot(Map<String, String> params) {
		Map<String,Object> data = new HashMap<String,Object>();
		int i = masterDao.getOneInfo("agentApplyRecord.checkAgentApplyStatusById", params);
		if(i < 1){
			data.put("result", "fail");
			data.put("msg", "操作失败！该记录已经被处理");
			return data;
		}
		
		int updateNum = masterDao.update("agentApplyRecord.acceptOrNot", params);
		Long userId = Long.valueOf(params.get("userId"));
		String pageUrl = params.get("pageUrl");
		String ip = params.get("ip");
		if(updateNum > 0){
			if(params.get("status").equals("2")){//申请状态,0:待受理,1:受理不通过,2:已受理
				String password = UUID.randomUUID().toString().replace("-", "").substring(4,12);
				params.put("password", password);
				sumbitRegisterContent(params);
				String vUrl = ConfigUtils.agent_site_url;//获取代理商服务器站点地址
				Map<String,Object> mail = masterDao.getOneInfo("operation.common.querySmsMailprop", 100017);// 获取邮箱模板,100009为代理商申请受理模板,100017为代理商申请受理并直接生成账号和密码的邮件
				// 发送开户邮件到邮箱
				String body = (String) mail.get("text");
				body = body.replace("vUrl", vUrl);
				body = body.replace("vemail", params.get("email"));
				body = body.replace("password", password);
				boolean sendEmail = emailService.sendHtmlEmail(params.get("email"), (String)mail.get("subject"), body);
				data.put("sendEmail", sendEmail ? "邮件已发送！": "邮件发送失败！");
				data.put("result", "success");
				data.put("msg", "操作成功。");
//				logService.add(LogType.update,LogEnum.账户信息管理.getValue(),"账户信息管理-代理商申请记录：受理代理申请",params, data);
				logService.add(LogType.update,  LogEnum.账户信息管理.getValue(), userId, pageUrl, ip,"账户信息管理-代理商申请记录：受理代理申请",params, data);
			}else if(params.get("status").equals("1")){//不受理,发送邮件
				Map<String,Object> mail = masterDao.getOneInfo("operation.common.querySmsMailprop", 100010);// 获取邮箱模板,100010为代理商申请失败模板.暂定申请失败不发邮件
				String body = (String) mail.get("text");
				body = body.replace("vreason", params.get("reason"));
				boolean sendEmail = emailService.sendHtmlEmail(params.get("email"), (String)mail.get("subject"), body);
				data.put("sendEmail", sendEmail ? "邮件已发送！": "邮件发送失败！"); 
				data.put("result", "success");
				data.put("msg", "操作成功。");
//				logService.add(LogType.update,LogEnum.账户信息管理.getValue(),"账户信息管理-代理商申请记录：不受理代理申请",params, data);
				logService.add(LogType.update,  LogEnum.账户信息管理.getValue(), userId, pageUrl, ip,"账户信息管理-代理商申请记录：不受理代理申请",params, data);
			}
		}else{
			data.put("result", "fail");
			data.put("msg", "操作失败！");
		}
		return data;
	}

	/**
	 * 获取代理商的申请信息
	 * @param id
	 * @return
	 */
	@Override
	public Map<String, Object> getAgentApplyInfoById(String id) {
		return masterDao.getOneInfo("agentApplyRecord.getAgentApplyInfoById",id);
	}

	@Override
	public List<SaleEntityPo> getSaleList() {

		//查询角色名都为‘销售人员’的角色id
		String saleRoleId = masterDao.getOneInfo("agentApplyRecord.getSaleRoleId",null);
		if(saleRoleId == null){
			throw new OperationException("查找销售人员失败！");
		}
		Map<String,Object> roleIdParams = new HashMap<>();
		roleIdParams.put("roleId",saleRoleId);

		List<String> userIdList = new ArrayList<>();
		List<Map<String,Object>> userIdMapList = masterDao.getSearchList("agentApplyRecord.getUserIdFromUserRoleByRoleId",roleIdParams);
		for(Map<String,Object> map : userIdMapList){
			String userId = map.get("user_id").toString();
			userIdList.add(userId);
		}
		List<SaleEntityPo> saleList = new ArrayList<>();
		Map<String,Object> params = new HashMap<>();
		params.put("userIdList",userIdList);
		List<Map<String,Object>> saleMapList = masterDao.getSearchList("agentApplyRecord.getSaleInfoByUserId",params);
		for(Map<String,Object> map:saleMapList){
			SaleEntityPo saleEntityPo = new SaleEntityPo();
			saleEntityPo.setId(map.get("id").toString());
			saleEntityPo.setName(map.get("realname").toString());
			saleList.add(saleEntityPo);
		}

		return saleList;
	}

	@Override
	public List<ProductInfo> getProductInfoListForOemData() {

		Map<String,Object> params = new HashMap<>();
		params.put("productType",0); //产品类型，0：行业，1：营销，2：国际，7：USSD，8：闪信，9：挂机短信，其中0和1为普通短信
		params.put("operatorCode",0); //产品类型，0：行业，1：营销，2：国际，7：USSD，8：闪信，9：挂机短信，其中0和1为普通短信
		params.put("areaCode",0); //产品类型，0：行业，1：营销，2：国际，7：USSD，8：闪信，9：挂机短信，其中0和1为普通短信
		params.put("status",1); //状态，0：待上架，1：已上架，2：已下架

		List<Map<String, Object>> productInfoMapList = masterDao.getSearchList("agentApplyRecord.getProductInfo", params);
		List<ProductInfo> ProductInfoList = new ArrayList<>();
		for(Map<String,Object> map : productInfoMapList){
			ProductInfo productInfo = new ProductInfo();
			productInfo.setProduct_id(Integer.valueOf(map.get("productId").toString()));
			Date dueTime = (Date) map.get("due_time");
			String dueTimeStr = "";
			if(dueTime!=null)
				dueTimeStr = DateUtils.formatDate(dueTime, "yyyy-MM-dd");
			productInfo.setProduct_name(map.get("productName").toString()+" "+dueTimeStr);
			ProductInfoList.add(productInfo);
		}
		return ProductInfoList;
	}

	/**
	 * 添加销售
	 * @param params
	 */
	@Override
	public void addSale(Map<String, String> params) {

		logger.debug("添加销售,方法--->{},参数--->{}================","addSale",params.toString());

		//判断姓名、手机号码、邮箱是否已经存在

		//判断姓名是否存在
		Map<String,Object> userNameParams = new HashMap<>();
		userNameParams.put("username",params.get("username"));
		Integer count1 = masterDao.getOneInfo("agentApplyRecord.getCountForUserInfo", userNameParams);
		if(count1 > 0){
			throw new OperationException("用户名称已经存在，请重新输入！");
		}

		//判断手机号码是否存在
		Map<String,Object> mobileParams = new HashMap<>();
		mobileParams.put("mobile",params.get("mobile"));
		Integer count2 = masterDao.getOneInfo("agentApplyRecord.getCountForUserInfo", mobileParams);
		if(count2 > 0){
			throw new OperationException("手机号码已经存在，请重新输入！");
		}

		//判断邮箱是否存在
		Map<String,Object> emailParams = new HashMap<>();
		emailParams.put("email",params.get("email"));
		Integer count3 = masterDao.getOneInfo("agentApplyRecord.getCountForUserInfo", emailParams);
		if(count3 > 0){
			throw new OperationException("邮箱已经存在，请重新输入！");
		}

		Map<String,Object> user = new HashMap<>();
		{
			Date now = new Date();
			user.put("id",null);
			user.put("sid",null);
			user.put("username",params.get("username"));
			user.put("email",params.get("email"));

			String password = SecurityUtils.encryptMD5(params.get("mobile"));
			user.put("password", MD5.md5(password));

			user.put("user_type",1);//用户类型 1:系统管理员
			user.put("status",1); //用户状态：0: 禁用 1:正常
			user.put("mobile",params.get("mobile"));
			user.put("realname",params.get("username"));
			user.put("create_date",now);

			user.put("update_date",now);
			user.put("login_times",0);
			user.put("web_id",3); //web应用ID：1短信调度系统 2代理商平台 3运营平台 4OEM代理商平台
		}

		//生成销售人员用户，并返回用户id
		int i = masterDao.insert("agentApplyRecord.insertUserForSale",user);
		if(i == 0){
			throw new OperationException("生成销售人员失败！");
		}

		//查询角色名都为‘销售人员’的角色id
		String saleRoleId = masterDao.getOneInfo("agentApplyRecord.getSaleRoleId",null);

		//绑定关系
		Map<String,Object> userRole = new HashMap<>();
		userRole.put("role_id",saleRoleId);
		userRole.put("user_id",user.get("id"));

		int j = masterDao.insert("agentApplyRecord.insertUserRole",userRole);
		if(j == 0){
			throw new OperationException("绑定关系失败！");
		}

	}

	/**
	 * @Description: 注册代理商信息
	 * @author: Niu.T 
	 * @date: 2016年12月30日    下午6:01:43
	 * @param params
	 * @return int
	 */
	public int sumbitRegisterContent(Map<String, String> params) {
		Map<String,Object> agentApply = this.masterDao.getOneInfo("agentCommon.queryAgentApplyById", params);
		Date date = new Date();
		//插入t_sms_user表
		int insertUser = this.insertSmsUserInfoForRegister(params, agentApply, date);
		if(insertUser <= 0){
			throw new OperationException("插入t_sms_user表失败");
		}

		int agent_id = AgentIdUtil.getAgentId();
		params.put("agent_id",agent_id+"");//保存代理商id，生成oem资料需要用到

		//插入t_sms_agent_info表
		agentApply.put("agent_type",params.get("agent_type"));
		if (params.get("belongSale") != null){
			agentApply.put("belong_sale", params.get("belongSale"));
		}
		int insertAgent = this.insertSmsAgentInfoForRegister(agent_id, agentApply,date);
		if(insertAgent <= 0){
			throw new OperationException("插入t_sms_agent_info表失败");
		}

		//插入t_sms_agent_account表
		int insertAgentAccount = this.insertSmsAgentAccountForRegister(agent_id);
		if(insertAgentAccount <= 0){
			throw new OperationException("插入t_sms_agent_account表失败");
		}

		//给用户赋代理商的角色t_sms_user_role
		int createUserRole = this.createUserRole((long)agentApply.get("userId"),params.get("agent_type"));
		if(createUserRole <= 0){
			throw new OperationException("给用户赋代理商的角色失败");
		}

		logger.debug("\r\n代理商受理 -> 向4张表添加的数据条数: \r\n\t ①t_sms_user --> {} \r\n\t ②t_sms_agent_info --> {}\r\n\t ③t_sms_agent_account --> {} \r\n\t ④t_sms_user_role --> {}",
				insertUser,insertAgent,insertAgentAccount,createUserRole);

		return insertUser;
	}
		
	/**
	 * @Description: 向t_sms_user表添加用户信息
	 * @author: Niu.T 
	 * @date: 2016年12月30日    下午6:02:09
	 * @param params
	 * @param agentApply
	 * @param date
	 * @return int
	 */
	public int insertSmsUserInfoForRegister(Map<String, String> params,Map<String,Object> agentApply,Date date){

		String email = params.get("email");

		Map<String,Object> emailParams = new HashMap<>();
		emailParams.put("email",email);

		int emailAgentNum = this.masterDao.getOneInfo("agentCommon.querySmsUserCountByEmail",emailParams);
		if(emailAgentNum > 0){
			throw new OperationException("操作失败，邮箱已经存在");
		}

		Map<String,Object> smsUserParams = new HashMap<String,Object>();
		String sid = SecurityUtils.generateSid();

		String agent_type = params.get("agent_type");
		String web_id = null;
		if("2".equals(agent_type)){
			web_id = "2";
		}else {
			web_id = "4";
		}

		String password = SecurityUtils.encryptMD5(params.get("password"));
		smsUserParams.put("sid", sid);
		smsUserParams.put("email", params.get("email"));
		smsUserParams.put("password", MD5.md5(password));	// 初始密码(随机八位数字和字母组合)
		smsUserParams.put("user_type", "1");
		smsUserParams.put("status", "1");
		smsUserParams.put("mobile", agentApply.get("mobile"));
		smsUserParams.put("realname", agentApply.get("realname"));
		smsUserParams.put("create_date", date);
		smsUserParams.put("update_date", date);
		smsUserParams.put("login_times", 0);
		smsUserParams.put("web_id", web_id);		//web应用ID：1短信调度系统 2代理商平台 3运营平台
		int insert = this.masterDao.insert("agentCommon.insertSmsUser", smsUserParams);
		agentApply.put("userId", smsUserParams.get("id"));
		if(insert <= 0){
			logger.debug("代理商受理 / 添加用户信息失败(t_sms_user), 参数  --> {} \r\n\t insert结果 --> {}",smsUserParams,insert);
		}
		return insert;
	}
	
	
	
	//插入t_sms_agent_info表
	public int insertSmsAgentInfoForRegister(int agent_id,Map<String,Object> agentApply,Date date){
		
		Map<String,Object> smsAgentInfoParmas = new HashMap<String,Object>();
		smsAgentInfoParmas.put("agent_id", agent_id);
		smsAgentInfoParmas.put("admin_id", agentApply.get("userId"));
		smsAgentInfoParmas.put("agent_name", agentApply.get("realname"));
		smsAgentInfoParmas.put("shorter_name", agentApply.get("realname"));
		smsAgentInfoParmas.put("agent_type", agentApply.get("agent_type"));
		smsAgentInfoParmas.put("status", 1);
		smsAgentInfoParmas.put("oauth_status", 2);
		smsAgentInfoParmas.put("oauth_date", null);
		smsAgentInfoParmas.put("address", agentApply.get("address"));
		smsAgentInfoParmas.put("company", agentApply.get("company"));
		smsAgentInfoParmas.put("company_nbr", null);
		smsAgentInfoParmas.put("mobile", agentApply.get("mobile"));
		smsAgentInfoParmas.put("create_time", date);
		smsAgentInfoParmas.put("update_time", null);
		smsAgentInfoParmas.put("remark", null);

		if (agentApply.get("belong_sale") != null){
			smsAgentInfoParmas.put("belong_sale", agentApply.get("belong_sale"));
		}
		int insert = this.masterDao.insert("agentCommon.insertSmsAgentInfo", smsAgentInfoParmas);
		if(insert <= 0){
			logger.debug("代理商受理 / 添加代理商信息失败(t_sms_agent_info), 参数  --> {} \r\n\t insert结果 --> {}",smsAgentInfoParmas,insert);
		}
		return insert;
	}
	
	public int insertSmsAgentAccountForRegister(int agent_id){
		Map<String,Object> smsAgentAccountParms = new HashMap<String,Object>();
		smsAgentAccountParms.put("agent_id", agent_id);
		smsAgentAccountParms.put("balance", 0);
		smsAgentAccountParms.put("credit_balance", 0);
		smsAgentAccountParms.put("accumulated_income", 0);
		smsAgentAccountParms.put("commission_income", 0);
		smsAgentAccountParms.put("accumulated_recharge", 0);
		int insert = this.masterDao.insert("agentCommon.insertSmsAgentAccount", smsAgentAccountParms);
		if(insert <= 0){
			logger.debug("代理商受理 / 给代理商添加账户信息失败(t_sms_agent_account), 参数  --> {} \r\n\t insert结果 --> {}",smsAgentAccountParms,insert);
		}
		return insert;
	}
	
	private int createUserRole(Long userId,String agent_type){
		//代理商的角色id为3
		int roleId = 3;
		if("2".equals(agent_type)){
			//品牌代理商
			roleId = 3;
		}else{
			//oem代理商
			roleId = this.masterDao.getOneInfo("agentCommon.queryOemRoleId",null);
		}

		Map<String,Object> userRoleParams = new HashMap<String,Object>();
		userRoleParams.put("role_id", roleId);
		userRoleParams.put("user_id", userId);
		int insert = this.masterDao.insert("agentCommon.insertSmsUserRole", userRoleParams);
		if(insert <= 0){
			logger.debug("代理商受理 / 给代理商生成账号分配菜单(t_sms_user_role)失败, 参数  --> {} \r\n\t insert结果 --> {}",userRoleParams,insert);
		}
		return insert;
	} 
	
	/**
	 * 品牌确认受理
	 * @param params
	 * @return
	 */
	@Override
	@Transactional
	public ResultVO brandConfirmAccept(Map<String, String> params) {

		logger.debug("品牌确认受理,方法--->{},参数--->{}================","brandConfirmAccept",params.toString());
		ResultVO resultVO = ResultVO.failure();

		Map<String,Object> data = new HashMap<String,Object>();
		int i = masterDao.getOneInfo("agentApplyRecord.checkAgentApplyStatusById", params);
		if(i == 0){
			throw new OperationException("操作失败！该记录已经被处理");
		}

		params.put("status","2");//申请状态,0:待受理,1:受理不通过,2:已受理
		int updateNum = masterDao.update("agentApplyRecord.updateApplyInfoById", params);
		if(updateNum == 0){
			throw new OperationException("更新申请表失败！");
		}

		Long userId = Long.valueOf(params.get("userId"));
		String pageUrl = params.get("pageUrl");
		String ip = params.get("ip");
		String password = UUID.randomUUID().toString().replace("-", "").substring(4,12);
		params.put("password", password);

		//生成用户、生成代理商、生成代理商账户、管理代理商角色
		params.put("agent_type","2");//代理商类型,1:销售代理商,2:品牌代理商,3:资源合作商,4:代理商和资源合作,5:OEM代理商
		sumbitRegisterContent(params);

		String vUrl = ConfigUtils.agent_site_url;//获取代理商服务器站点地址
		Map<String,Object> mail = masterDao.getOneInfo("operation.common.querySmsMailprop", 100017);// 获取邮箱模板,100009为代理商申请受理模板,100017为代理商申请受理并直接生成账号和密码的邮件
		// 发送开户邮件到邮箱
		String body = (String) mail.get("text");
		body = body.replace("vUrl", vUrl);
		body = body.replace("vemail", params.get("email"));
		body = body.replace("password", password);
		boolean sendEmail = emailService.sendHtmlEmail(params.get("email"), (String)mail.get("subject"), body);
		if(sendEmail == false){
			resultVO.setMsg("受理成功！发送邮件失败！");
		}else{
			resultVO.setMsg("受理成功！发送邮件成功！");
		}
		resultVO.setSuccess(true);
		logService.add(LogType.update,  LogEnum.账户信息管理.getValue(), userId, pageUrl, ip,"账户信息管理-代理商申请记录：受理代理申请",params, data);
		logger.debug("品牌确认受理,返回{}",JacksonUtil.toJSON(resultVO));
		return resultVO;
	}


	/**
	 * oem确认受理
	 * @param params
	 * @return
	 */
	@Override
	@Transactional
	public ResultVO oemConfirmAccept(Map<String, String> params) {
		logger.debug("oem确认受理,方法--->{},参数--->{}================","oemConfirmAccept",params.toString());
		ResultVO resultVO = ResultVO.failure();

		Map<String,Object> data = new HashMap<String,Object>();
		int i = masterDao.getOneInfo("agentApplyRecord.checkAgentApplyStatusById", params);
		if(i == 0){
			throw new OperationException("操作失败！该记录已经被处理");
		}

		params.put("status","2");//申请状态,0:待受理,1:受理不通过,2:已受理
		int updateNum = masterDao.update("agentApplyRecord.updateApplyInfoById", params);
		if(updateNum == 0){
			throw new OperationException("更新申请表失败！");
		}

		Long userId = Long.valueOf(params.get("userId"));
		String pageUrl = params.get("pageUrl");
		String ip = params.get("ip");
		String password = UUID.randomUUID().toString().replace("-", "").substring(4,12);
		params.put("password", password);

		StringBuilder agentIdSb = new StringBuilder("");
		//生成用户、生成代理商、生成代理商账户、管理代理商角色
		params.put("agent_type","5");//代理商类型,1:销售代理商,2:品牌代理商,3:资源合作商,4:代理商和资源合作,5:OEM代理商
		sumbitRegisterContent(params);

		//添加oem资料
		addOemDataConfig(params);

//		String vUrl = ConfigUtils.agent_site_url;//获取代理商服务器站点地址
		String vUrl = ConfigUtils.oem_agent_site_url;//oem代理商站点地址

		Map<String,Object> mail = masterDao.getOneInfo("operation.common.querySmsMailprop", 100022);// 100022为代理商申请oem受理并直接生成账号和密码的邮件
		// 发送开户邮件到邮箱
		String body = (String) mail.get("text");
		body = body.replace("vUrl", vUrl);
		body = body.replace("vemail", params.get("email"));
		body = body.replace("password", password);
		boolean sendEmail = emailService.sendHtmlEmail(params.get("email"), (String)mail.get("subject"), body);
		if(sendEmail == false){
			resultVO.setMsg("受理成功！发送邮件失败！");
		}else{
			resultVO.setMsg("受理成功！发送邮件成功！");
		}
		resultVO.setSuccess(true);

		logService.add(LogType.update,  LogEnum.账户信息管理.getValue(), userId, pageUrl, ip,"账户信息管理-代理商申请记录：受理代理申请",params, data);
		logger.debug("oem确认受理,返回{}",JacksonUtil.toJSON(resultVO));
		return resultVO;

	}


	/**
	 * 添加oem资料
	 * @param params
	 */
	private void addOemDataConfig(Map<String, String> params){

		Map<String,Object> dataParams = new HashMap<>();

		dataParams.put("id",null);
		dataParams.put("domain_name",params.get("domain"));
		dataParams.put("agent_id",params.get("agent_id"));
		dataParams.put("copyright_text",params.get("copyright"));

		dataParams.put("tab_logo_url",params.get("h_tab_logo"));
		dataParams.put("logo_url",params.get("h_logo"));
		dataParams.put("company_logo_url",params.get("h_company_logo"));
		dataParams.put("api_document_url",params.get("h_api_document"));
		dataParams.put("FAQ_document_url",params.get("h_FAQ_document"));

		dataParams.put("navigation_backcolor",params.get("nav_backcolor"));
		dataParams.put("navigation_text_color",params.get("nav_text_color"));
		dataParams.put("hy_sms_discount",params.get("hy_discount"));
		dataParams.put("yx_sms_discount",params.get("yx_discount"));
		dataParams.put("gj_sms_discount",params.get("gj_discount"));
		dataParams.put("test_product_id",params.get("productId"));
		dataParams.put("test_sms_number",params.get("productNum"));
		String domain = params.get("domain");
		if(domain!=null&&!domain.equals("oemclient.sms.ucpaas.com")){
			List<Map<String,Object>> dcs = masterDao.selectList("agentManage.getOEMDataConfigByDomain", params);
			String agentId = params.get("agent_id")==null?"":params.get("agent_id").toString();
			if(dcs!=null&&dcs.size()>0){
				for(Map<String,Object> d:dcs){
					String dbAgentId =d.get("agent_id")!=null?d.get("agent_id").toString():""; 
					if(!dbAgentId.equals(agentId))
						throw new OperationException("域名已存在，请修改后再提交。代理商id="+d.get("agent_id")+"已使用该域名");
				}
			}
		}
		

		int i = masterDao.insert("agentApplyRecord.insertOemDataConfig",dataParams);
		if(i <=0 ){
			throw new OperationException("添加oem资料失败！");
		}
	}


	@Override
	public ResultVO updateBelongSaleForAgent(Map<String, String> params) {

		//首先更新代理商的销售归属
		int i = masterDao.update("agentManage.updateBelongSaleForAgent",params);
		if(i <= 0){
			return ResultVO.failure("更新代理商归属销售失败！");
		}

		//更细客户的销售归属
        int update = masterDao.update("clientManage.updateBelongSaleForClient", params);
		logger.debug("更新代理商归属销售时, 同时修改其名下的客户 ---> update = {}",update);

        return ResultVO.successDefault();

	}

	@Override
	public ResultVO updateBelongSaleForClient(Map<String, String> params) {

		int i = masterDao.update("clientManage.updateBelongSaleForClient",params);
		if( i<= 0){
			return ResultVO.failure("更新客户归属销售失败！");
		}
		return ResultVO.successDefault();
	}


}
