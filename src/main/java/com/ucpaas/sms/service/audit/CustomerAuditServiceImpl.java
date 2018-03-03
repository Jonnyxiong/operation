package com.ucpaas.sms.service.audit;

import com.jsmsframework.common.enums.OauthStatusEnum;
import com.ucpaas.sms.common.util.SecurityUtils;
import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.constant.LogConstant.LogType;
import com.ucpaas.sms.dao.MessageMasterDao;
import com.ucpaas.sms.entity.message.Account;
import com.ucpaas.sms.enums.LogEnum;
import com.ucpaas.sms.exception.OperationException;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.account.AccountService;
import com.ucpaas.sms.service.common.LogService;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.order.OemOrderIdGenerate;
import com.ucpaas.sms.util.enums.StaticInitVariable;
import com.ucpaas.sms.util.rest.utils.DateUtil;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional
public class CustomerAuditServiceImpl implements CustomerAuditService {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CustomerAuditServiceImpl.class);
	
	@Autowired
	private MessageMasterDao masterDao;
	@Autowired
	private LogService logService;
	@Autowired
	private AccountService accountService;
	
	@Override
	public PageContainer query(Map<String, String> params) {
		return masterDao.getSearchPage("customerAudit.query", "customerAudit.queryCount", params);
	}

	@Override
	public Map<String, Object> audit(Map<String, String> params) {
		Map<String, Object> result = new HashMap<String, Object>();
		if(StringUtils.isBlank(params.get("type"))) {
			int i = masterDao.getOneInfo("customerAudit.checkCustomerOauthStatus", params);
			if (i < 1) {
				result.put("result", "fail");
				result.put("msg", "审核失败！该客户资质已经被审核");
				return result;
			}
		}
		int num = masterDao.update("customerAudit.updateStatus", params);
		if(num <= 0){
			throw new OperationException("审核资质认证失败");
		}

		Long userId = Long.valueOf(params.get("userId"));
		String pageUrl = params.get("pageUrl");
		String ip = params.get("ip");

		//测试短信赠送
		String client_id = params.get("client_id");

		// 只有认证成功才赠送短信
		String oauth_status = params.get("oauth_status");
		if (OauthStatusEnum.证件已认证.getValue().toString().equals(oauth_status)) {
            // 判断子账户有没有赠送短信, 没有则赠送, 有则不赠送
            accountService.isGiveMessage(client_id,userId);
        }

		result.put("result", "success");
		result.put("msg", "审核资质认证完成");
		int j = masterDao.insert("customerAudit.insertAuditLog", params);

		if(j<=0){
			throw new OperationException("审核资质认证失败");
		}

		logService.add(LogType.update,  LogEnum.审核管理.getValue(), userId, pageUrl, ip,
				"审核管理管理- 客户资质审核：审核客户资质", params, result);

		return result;
	}

	/**
	 * 获取oem代理商订单id序号的最大值
	 * @param orderIdPre
	 * @return
	 */
	@Override
	public String getOemAgentOrderTheMostNumForMinute(String orderIdPre) {
		Map<String,Object> sqlParams = new HashMap<>();
		sqlParams.put("orderIdPre", orderIdPre);
		String numStr = masterDao.getOneInfo("customerAudit.getOemAgentOrderTheMostNumForMinute", sqlParams);
		return numStr;
	}

	/**
	 * 获取oem客户订单id序号的最大值
	 * @param orderIdPre
	 * @return
	 */
	@Override
	public String getOemClientOrderTheMostNumForMinute(String orderIdPre) {
		Map<String,Object> sqlParams = new HashMap<>();
		sqlParams.put("orderIdPre", orderIdPre);
		String numStr = masterDao.getOneInfo("customerAudit.getOemClientOrderTheMostNumForMinute", sqlParams);
		return numStr;
	}

	//测试短信赠送
	@Override
	@Deprecated
	public void giveShortMessage(Account account){

		logger.debug("测试短信赠送,方法为--->{},参数为---->{}","GiveShortMessage",account.getClientid());

		/*Map<String,Object> clientIdParams = new HashMap<>();
		clientIdParams.put("clientid",client_id);

		Integer paytype = masterDao.getOneInfo("customerAudit.getPaytype", clientIdParams);*/
		if(account.getPaytype() != null && !account.getPaytype().equals(0)){
			logger.debug("付费类型，0：预付费，1：后付费--->{}，不是预付费，不能赠送短信",account.getPaytype());
			return;
		}

		// 查询客户的名字
		/*String realName = masterDao.getOneInfo("customerAudit.getRealName", clientIdParams);*/

		/*String agent_id = masterDao.getOneInfo("customerAudit.getAgentIdByClientId",clientIdParams);*/

		Map<String,Object> agentIdParams = new HashMap<>();
		agentIdParams.put("agent_id",account.getAgentId());

		int agent_type = masterDao.getOneInfo("customerAudit.getAgentTypeByAgentId",agentIdParams);

		//品牌代理/销售代理的客户,不赠送测试短信(只有OEM代理商赠送短信)
		if(agent_type != 5){
			logger.debug("代理商类型为--->{}，不是oem代理商，不能赠送短信",agent_type);
			return;
		}

		//不赠送短信产品
		Map<String,Object> oemDataMap = masterDao.getOneInfo("customerAudit.getOemDataConfig",agentIdParams);

		if(oemDataMap == null || oemDataMap.get("id") == null){
			logger.debug("代理商：{}------->没有对应的oem资料",account.getAgentId());
			return;
		}

		String oemDataId = oemDataMap.get("id").toString();

		if(oemDataMap.get("test_product_id") == null || oemDataMap.get("test_sms_number") == null || "0".equals(oemDataMap.get("test_sms_number").toString())){
			logger.debug("oemid:{}资料----->为空、或者条数--->为空、或者-->条数",oemDataId);
			return;
		}

		String test_product_id = oemDataMap.get("test_product_id").toString();
		String test_sms_number = oemDataMap.get("test_sms_number").toString();
		//测试产品包已经下架或者已经过期
		Map<String,Object> productIdMap = new HashMap<>();
		productIdMap.put("product_id",test_product_id);

		Map<String,Object> oemProductInfoMap = masterDao.getOneInfo("customerAudit.getOemProductInfoByProductId",productIdMap);
		int productStatus = (int) oemProductInfoMap.get("status"); //状态，0：待上架，1：已上架，2：已下架
		String unit_price = oemProductInfoMap.get("unit_price").toString();
		String product_type = oemProductInfoMap.get("product_type").toString();//产品类型,0:行业,1:营销,2:国际
		String operator_code = oemProductInfoMap.get("operator_code").toString();
		String area_code = oemProductInfoMap.get("area_code").toString();
		String due_time = oemProductInfoMap.get("due_time").toString();
		String product_id = oemProductInfoMap.get("product_id").toString();
		String product_code = oemProductInfoMap.get("product_code").toString();
		String product_name = oemProductInfoMap.get("product_name").toString();

		if(productStatus != 1){
			logger.debug("产品id:{}--------->为待上架或者已下架",test_product_id);
			return;
		}

		//满足赠送短信的条件
		logger.debug("满足赠送短信的条件=============================================================");

		Date now = new Date();

		Map<String,Object> agentAccount = masterDao.getOneInfo("customerAudit.getAgentAccountByAgentId",agentIdParams);
		String balance = agentAccount.get("balance").toString();
		BigDecimal oldBgBalance = new BigDecimal(balance);

		BigDecimal bgTestNum = new BigDecimal(test_sms_number);
		BigDecimal bgUnitPrice = new BigDecimal(unit_price);
		BigDecimal bgAmount = bgTestNum.multiply(bgUnitPrice);

		BigDecimal newBgBalance = oldBgBalance.add(bgAmount);

		//生成余额账单(代理商入账)
		Map<String,Object> agentBalanceBillParams = new HashMap<>();
		agentBalanceBillParams.put("id",null);
		agentBalanceBillParams.put("agent_id",account.getAgentId());
		agentBalanceBillParams.put("payment_type","5");//业务类型，0：充值，1：扣减，2：佣金转余额，3：购买产品包，4：退款，5：赠送
		agentBalanceBillParams.put("financial_type","0");//财务类型，0：入账，1：出账
		agentBalanceBillParams.put("amount",bgAmount.toString());

		agentBalanceBillParams.put("balance",newBgBalance.toString());
		agentBalanceBillParams.put("create_time",now);
		agentBalanceBillParams.put("order_id",null);   //充值操作订单id为null
		agentBalanceBillParams.put("admin_id",0);
		agentBalanceBillParams.put("client_id",account.getClientid());
		agentBalanceBillParams.put("remark","赠送短信充值");

		int i = masterDao.insert("customerAudit.createAgentBalanceBill",agentBalanceBillParams);
		if(i <= 0){
			throw new OperationException("赠送短信，生成余额入账账单失败");
		}

		//判断OEM代理商短信池(t_sms_oem_agent_pool)是否存在记录(获取agent_pool_id)
		String agent_pool_id = null;

		Map<String,Object> agentPoolParams = new HashMap<>();
		agentPoolParams.put("agent_id",account.getAgentId());
		agentPoolParams.put("product_type",product_type); //产品类型，0：行业，1：营销，2：国际
		agentPoolParams.put("operator_code",operator_code); //对应运营商,0:全网,1:移动,2:联通,3:电信,4:国际
		agentPoolParams.put("area_code",area_code); //适用地区,0:全国,1:国际,2:省网
		agentPoolParams.put("due_time",due_time); //到期时间
		agentPoolParams.put("unit_price",unit_price);
		agentPoolParams.put("status","0"); //状态，0：正常，1：停用

		Map<String,Object> params = masterDao.getOneInfo("customerAudit.getAgentPoolIdByCondition",agentPoolParams);
		if(params != null && params.get("agent_pool_id") != null){
			agent_pool_id = params.get("agent_pool_id").toString();
		}else{

			Map<String,Object> agentPoolMap = new HashMap<>();
			agentPoolMap.put("agent_pool_id",null);
			agentPoolMap.put("agent_id",account.getAgentId());
			agentPoolMap.put("product_type",product_type);
			agentPoolMap.put("operator_code",operator_code);
			agentPoolMap.put("area_code",area_code);
			agentPoolMap.put("due_time",due_time);
			agentPoolMap.put("status","0"); //状态，0：正常，1：停用

			agentPoolMap.put("remain_number","0");
			agentPoolMap.put("unit_price",unit_price);
			agentPoolMap.put("remain_amount",null);
			agentPoolMap.put("update_time",now);
			agentPoolMap.put("remark",null);

			int j = masterDao.insert("customerAudit.createOemAgentPool",agentPoolMap);
			if(j <= 0){
				throw new OperationException("生成代理商短信池记录失败！");
			}
			agent_pool_id = agentPoolMap.get("agent_pool_id").toString();
		}

		//生成代理商订单(购买记录)
		Map<String,Object> oemAgentOrderMapForBuy = new HashMap<>();

		String orderId = this.getOemAgentOrderId().toString();
		oemAgentOrderMapForBuy.put("order_id",orderId);
		oemAgentOrderMapForBuy.put("order_no",orderId);
		oemAgentOrderMapForBuy.put("order_type",0); //订单类型，0：OEM代理商购买，1：OEM代理商分发，2：OEM代理商回退
		oemAgentOrderMapForBuy.put("product_id",product_id);
		oemAgentOrderMapForBuy.put("product_code",product_code);

		oemAgentOrderMapForBuy.put("product_type",product_type);
		oemAgentOrderMapForBuy.put("operator_code",operator_code);
		oemAgentOrderMapForBuy.put("area_code",area_code);
		oemAgentOrderMapForBuy.put("product_name",product_name);
		oemAgentOrderMapForBuy.put("unit_price",unit_price);
		oemAgentOrderMapForBuy.put("order_number",test_sms_number); //赠送的短信条数
		oemAgentOrderMapForBuy.put("order_amount",bgAmount.toString());

		oemAgentOrderMapForBuy.put("product_price","0");
		oemAgentOrderMapForBuy.put("agent_id",account.getAgentId());
		oemAgentOrderMapForBuy.put("client_id",account.getClientid());
		oemAgentOrderMapForBuy.put("name","云之讯");
		oemAgentOrderMapForBuy.put("agent_pool_id",agent_pool_id);

		oemAgentOrderMapForBuy.put("due_time",due_time);
		oemAgentOrderMapForBuy.put("create_time",now);
		oemAgentOrderMapForBuy.put("remark",null);

		int k = masterDao.insert("customerAudit.insertOemAgentOrder",oemAgentOrderMapForBuy);
		if(k <= 0){
			throw new OperationException("生成代理商订单（购买记录）失败！");
		}

		//生成余额账单(代理商出账)
		Map<String,Object> agentBalanceBillOutParams = new HashMap<>();
		agentBalanceBillOutParams.put("id",null);
		agentBalanceBillOutParams.put("agent_id",account.getAgentId());
		agentBalanceBillOutParams.put("payment_type","3");//业务类型，0：充值，1：扣减，2：佣金转余额，3：购买产品包，4：退款，5：赠送
		agentBalanceBillOutParams.put("financial_type","1");//财务类型，0：入账，1：出账
		agentBalanceBillOutParams.put("amount",bgAmount.toString());

		agentBalanceBillOutParams.put("balance",oldBgBalance.toString());
		agentBalanceBillOutParams.put("create_time",now);
		agentBalanceBillOutParams.put("order_id",orderId);
		agentBalanceBillOutParams.put("admin_id",0);
		agentBalanceBillOutParams.put("client_id",account.getClientid());
		agentBalanceBillOutParams.put("remark","赠送短信充值");

		int m = masterDao.insert("customerAudit.createAgentBalanceBill",agentBalanceBillOutParams);
		if(m <= 0){
			throw new OperationException("赠送短信，生成余额出账账单失败");
		}

		//======================================给客户充值===========================================

		//生成代理商订单(分发记录)
		Map<String,Object> oemAgentOrderMapForDistribute = new HashMap<>();

		String orderId2 = this.getOemAgentOrderId().toString();
		oemAgentOrderMapForDistribute.put("order_id",orderId2);
		oemAgentOrderMapForDistribute.put("order_no",orderId2);
		oemAgentOrderMapForDistribute.put("order_type",1); //订单类型，0：OEM代理商购买，1：OEM代理商分发，2：OEM代理商回退
		oemAgentOrderMapForDistribute.put("product_id",product_id);
		oemAgentOrderMapForDistribute.put("product_code",product_code);

		oemAgentOrderMapForDistribute.put("product_type",product_type);
		oemAgentOrderMapForDistribute.put("operator_code",operator_code);
		oemAgentOrderMapForDistribute.put("area_code",area_code);
		oemAgentOrderMapForDistribute.put("product_name",product_name);
		oemAgentOrderMapForDistribute.put("unit_price",unit_price);
		oemAgentOrderMapForDistribute.put("order_number",test_sms_number); //赠送的短信条数
		oemAgentOrderMapForDistribute.put("order_amount",bgAmount.toString());

		oemAgentOrderMapForDistribute.put("product_price","0");
		oemAgentOrderMapForDistribute.put("agent_id",account.getAgentId());
		oemAgentOrderMapForDistribute.put("client_id",account.getClientid());
		oemAgentOrderMapForDistribute.put("name",account.getName());
		oemAgentOrderMapForDistribute.put("agent_pool_id",agent_pool_id);

		oemAgentOrderMapForDistribute.put("due_time",due_time);
		oemAgentOrderMapForDistribute.put("create_time",now);
		oemAgentOrderMapForDistribute.put("remark",null);

		int n = masterDao.insert("customerAudit.insertOemAgentOrder",oemAgentOrderMapForDistribute);
		if(n <= 0){
			throw new OperationException("生成代理商订单（分发记录）失败！");
		}

		//判断oem客户短信池是否存在记录(获取client_pool_id)

		//判断OEM代理商短信池(t_sms_oem_agent_pool)是否存在记录(获取agent_pool_id)
		String client_pool_id = null;

		Map<String,Object> clientPoolParams = new HashMap<>();
		clientPoolParams.put("client_id",account.getClientid());
		clientPoolParams.put("product_type",product_type); //产品类型，0：行业，1：营销，2：国际
		clientPoolParams.put("operator_code",operator_code); //对应运营商，0：全网，1：移动，2：联通，3：电信，4：国际
		clientPoolParams.put("area_code",area_code); //适用区域，0：全国，1：国际
		clientPoolParams.put("due_time",due_time); //到期时间
		clientPoolParams.put("unit_price",unit_price);
		clientPoolParams.put("status","0"); //状态，0：正常，1：停用

		Map<String,Object> clientPoolIdMap = masterDao.getOneInfo("customerAudit.getClientPoolIdByCondition",clientPoolParams);
		if(clientPoolIdMap != null && clientPoolIdMap.get("client_pool_id") != null){
			client_pool_id = clientPoolIdMap.get("client_pool_id").toString();

			Map<String,Object> updateClientPoolMap = new HashMap<>();
			updateClientPoolMap.put("client_pool_id",client_pool_id);
			updateClientPoolMap.put("test_num",test_sms_number);

			int o = masterDao.update("customerAudit.updateClientPoolByCondition",updateClientPoolMap);
			if( o <= 0){
				throw new OperationException("更新客户短信池的测试条数失败！");
			}

		}else{

			Map<String,Object> clientPoolMap = new HashMap<>();
			clientPoolMap.put("client_pool_id",null);
			clientPoolMap.put("client_id",account.getClientid());
			clientPoolMap.put("product_type",product_type);
			clientPoolMap.put("operator_code",operator_code);
			clientPoolMap.put("area_code",area_code);
			clientPoolMap.put("due_time",due_time);
			clientPoolMap.put("status",0); //状态，0：正常，1：停用

			clientPoolMap.put("total_number",test_sms_number);
			clientPoolMap.put("remain_number",test_sms_number);
			clientPoolMap.put("unit_price",unit_price);
			clientPoolMap.put("total_amount",0);
			clientPoolMap.put("remain_amount",0);

			clientPoolMap.put("remain_test_number",test_sms_number);
			clientPoolMap.put("update_time",now);
			clientPoolMap.put("remark",null);

			int p = masterDao.insert("customerAudit.createOemClientPool",clientPoolMap);
			if(p <= 0){
				throw new OperationException("生成客户短信池失败！");
			}

			client_pool_id = clientPoolMap.get("client_pool_id").toString();
		}

		//给客户订单增加分发记录(生成oem客户订单)
		Map<String,Object> oemClientOrderMap = new HashMap<>();
		String oemClientOrderId = OemOrderIdGenerate.getOemClientOrderId().toString();

		oemClientOrderMap.put("order_id",oemClientOrderId);
		oemClientOrderMap.put("order_no",oemClientOrderId);
		oemClientOrderMap.put("product_type",product_type); //产品类型，0：行业，1：营销，2：国际
		oemClientOrderMap.put("operator_code",operator_code); //对应运营商，0：全网，1：移动，2：联通，3：电信，4：国际 所有订单类型时都有值
		oemClientOrderMap.put("area_code",area_code); //适用区域，0：全国，1：国际 所有订单类型时都有值
		oemClientOrderMap.put("order_type","1"); //订单类型，1：OEM代理商分发，2：OEM代理商回退
		oemClientOrderMap.put("order_number",test_sms_number);//赠送的短信条数

		oemClientOrderMap.put("unit_price",unit_price);
		oemClientOrderMap.put("order_price",bgAmount.toString());
		oemClientOrderMap.put("client_id",account.getClientid());
		oemClientOrderMap.put("agent_id",account.getAgentId());
		oemClientOrderMap.put("client_pool_id",client_pool_id);

		oemClientOrderMap.put("due_time",due_time);
		oemClientOrderMap.put("create_time",now);
		oemClientOrderMap.put("remark",null);

		int q = masterDao.insert("customerAudit.insertOemClientOrder",oemClientOrderMap);
		if(q <= 0){
			throw new OperationException("生成oem客户订单失败！");
		}

	}

	//组装表t_sms_oem_agent_order的orderID
	public synchronized Long getOemAgentOrderId(){

		Date date = new Date();
		int num = 0;
		String orderIdPre = DateUtil.dateToStr(date,"yyMMdd") + DateUtil.dateToStr(date, "HHmm")
				+ ConfigUtils.platform_oem_agent_order_identify;// 运营平台下单标识，oem代理商订单标识4
		if(orderIdPre.equals(StaticInitVariable.OEM_AGENT_ORDERID_PRE)){
			num = StaticInitVariable.OEM_AGENT_ORDER_NUM;
			StaticInitVariable.OEM_AGENT_ORDER_NUM = num + 1;
		}else{
			StaticInitVariable.OEM_AGENT_ORDERID_PRE = orderIdPre;
			num = 1;
			StaticInitVariable.OEM_AGENT_ORDER_NUM = num + 1;
		}

		//拼成订单号
		String orderIdStr = orderIdPre + StringUtils.addZeroForNum(num, 4, "0");
		Long orderId = Long.valueOf(orderIdStr);

		System.out.println("生成的代理商orderId------------->"+orderId);
		logger.debug("生成的代理商orderId------------->"+orderId);

		return orderId;
	}




	@Override
	public Map<String, Object> view(Map<String, String> params) {
		Map<String,Object> data = new HashMap<String,Object>();
		if(StringUtils.isNotBlank(params.get("type"))){
			data = masterDao.getOneInfo("customerAudit.getAuditInfoOfAudit", params);
			data.put("remark","");
		}else {
			data = masterDao.getOneInfo("customerAudit.getAuditInfo", params);
		}
		if(data !=null) {
			String imgUrl = (String) data.get("img_url");
			if (imgUrl != null) {
				String smspImgUrl = ConfigUtils.smsp_img_url.endsWith("/") ? ConfigUtils.smsp_img_url.substring(0, ConfigUtils.smsp_img_url.lastIndexOf("/")) : ConfigUtils.smsp_img_url;
				data.put("img_url", SecurityUtils.encodeDes3(imgUrl)); // 给路径加密
				data.put("smsp_img_url", smspImgUrl); // 添加图片服务器地址
			}
		}
		return data;
	}

	/*@Override
	public Map<String, Object> viewOfAccount(Map<String, String> params) {
		Map<String,Object> data = new HashMap<String,Object>();
		data = masterDao.getOneInfo("agentAudit.getAuditAccount", params);
		if(data != null){
			String imgUrl = (String)data.get("img_url");
			if(imgUrl != null){
				String smspImgUrl =  ConfigUtils.smsp_img_url.endsWith("/")? ConfigUtils.smsp_img_url.substring(0, ConfigUtils.smsp_img_url.lastIndexOf("/")):ConfigUtils.smsp_img_url;
				data.put("img_url", SecurityUtils.encodeDes3(imgUrl)); // 给路径加密
				data.put("smsp_img_url", smspImgUrl); // 添加图片服务器地址
			}
		}
		return data;
	}

	@Override
	public Map<String, Object> viewOfAccountZK(Map<String, String> params) {
		Map<String,Object> data = new HashMap<String,Object>();
		data = masterDao.getOneInfo("agentAudit.getAuditAccountZK", params);
		if(data != null){
			String imgUrl = (String)data.get("img_url");
			if(imgUrl != null){
				String smspImgUrl =  ConfigUtils.smsp_img_url.endsWith("/")? ConfigUtils.smsp_img_url.substring(0, ConfigUtils.smsp_img_url.lastIndexOf("/")):ConfigUtils.smsp_img_url;
				data.put("img_url", SecurityUtils.encodeDes3(imgUrl)); // 给路径加密
				data.put("smsp_img_url", smspImgUrl); // 添加图片服务器地址
			}
		}
		return data;
	}
*/

}
