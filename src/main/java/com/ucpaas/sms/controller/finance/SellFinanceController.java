package com.ucpaas.sms.controller.finance;

import com.jsmsframework.common.constant.SysConstant;
import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.common.enums.*;
import com.jsmsframework.common.util.BeanUtil;
import com.jsmsframework.finance.dto.JsmsSaleCreditBillDTO;
import com.jsmsframework.finance.entity.JsmsAgentAccount;
import com.jsmsframework.finance.entity.JsmsAgentBalanceBill;
import com.jsmsframework.finance.entity.JsmsSaleCreditAccount;
import com.jsmsframework.finance.entity.JsmsSaleCreditBill;
import com.jsmsframework.finance.service.JsmsAgentAccountService;
import com.jsmsframework.finance.service.JsmsAgentBalanceBillService;
import com.jsmsframework.finance.service.JsmsSaleCreditAccountService;
import com.jsmsframework.finance.service.JsmsSaleCreditBillService;
import com.jsmsframework.sale.credit.service.JsmsSaleCreditService;
import com.jsmsframework.user.entity.JsmsAgentInfo;
import com.jsmsframework.user.entity.JsmsUser;
import com.jsmsframework.user.service.JsmsAgentInfoService;
import com.jsmsframework.user.service.JsmsUserService;
import com.ucpaas.sms.common.util.file.ExcelUtils;
import com.ucpaas.sms.common.util.file.FileUtils;
import com.ucpaas.sms.controller.BaseController;
import com.ucpaas.sms.dto.AgentForSaleDTO;
import com.ucpaas.sms.dto.JsmsAgentBalanceBillVO;
import com.ucpaas.sms.dto.JsmsSaleCreditBillVO;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.entity.po.DataAuthorityCondition;
import com.ucpaas.sms.enums.DataAuthority;
import com.ucpaas.sms.enums.SessionEnum;
import com.ucpaas.sms.model.Excel;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.util.AgentUtils;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.PageConvertUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Don on 2017/10/23. 财务信息
 */
@Controller
@RequestMapping("/sellFinance")
public class SellFinanceController extends BaseController{

	private static Logger logger = LoggerFactory.getLogger(SellFinanceController.class);


	@Autowired
	private JsmsSaleCreditAccountService jsmsSaleCreditAccountService;
	@Autowired
	private JsmsSaleCreditBillService jsmsSaleCreditBillService;
	@Autowired
	private JsmsUserService jsmsUserService;
	@Autowired
	private JsmsAgentAccountService jsmsAgentAccountService;
	@Autowired
	private JsmsAgentInfoService jsmsAgentInfoService;
	@Autowired
	private JsmsAgentBalanceBillService jsmsAgentBalanceBillService;
	@Autowired
	private JsmsSaleCreditService jsmsSaleCreditService;
	/**
	 * 我的授信
	 */
	@ApiOperation(value = "查询我的授信", notes = "查询我的授信", tags = "财务管理-销售财务", response = String.class)
	@RequestMapping("/myCredit")
	public String myCreditView(Model model, HttpSession session) {
		model.addAttribute("max_export_num", ConfigUtils.max_export_excel_num);
		UserSession user=getUserFromSession(session);
		if(AgentUtils.isSaleRole(user)){
			JsmsSaleCreditAccount credit=jsmsSaleCreditAccountService.getBySaleId(user.getId());
			model.addAttribute("credit",credit);
		}

		model.addAttribute("menus", AgentUtils.hasMenuRight(user, "我的授信-wdsx", "客户授信-khsx","客户历史授信-khlssx","销售财务-xscw","销售历史财务-xslscw","销售授信账单-xssxzd","客户授信账单-khsxzd"));

		//sell\myCredit.html
		if(AgentUtils.isSaleRole(user)){
			return "finance/sell/myCredit";
		}else {
			return "finance/sell/credit4Customer";
		}

	}


	/**
	 * 我的授信查询分页数据
	 *
	 * @param rows           行数
	 * @param page           当前查询页码
	 * @param condition      条件
	 * @param financialType  财务类型
	 * @param businessType  业务类型
	 * @param startDateTime 开始时间
	 * @param endDateTime   结束时间
	 * @return
	 */
	@ApiOperation(value = "查询我的授信列表数据", notes = "查询我的授信列表数据", tags = "财务管理-销售财务", response = PageContainer.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "page", value = "页码", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "rows", value = "行数", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "session", value = "session", dataType = "HttpSession", paramType = "query"),
			@ApiImplicitParam(name = "condition", value = "用户名称/用户ID", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "financialType", value = "财务类型", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "businessType", value = "业务类型", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "startDateTime", value = "开始时间", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "endDateTime", value = "结束时间", dataType = "String", paramType = "query") })
	@RequestMapping("/myCredit/list")
	@ResponseBody
//    public PageContainer list(String rows, String page, String condition, String status, String startDateTime, String endDateTime) {
	public R myCreditList(HttpSession session,String rows, String page,String condition,String financialType,String businessType,String startDateTime,String endDateTime) {

		JsmsPage jpage=new JsmsPage();
		Map<String, Object> params = new HashMap<>();
		long start = System.currentTimeMillis();
		if(StringUtils.isNotBlank(condition)){
			params.put("condition",condition);
			List<Long> objectIds=new ArrayList();
			List<JsmsUser> conUser=jsmsUserService.getByRealname(condition);
			List<Integer> conAgent=jsmsAgentInfoService.queryAgentIdsByParams(params);
			if(null!=conUser){
				for (JsmsUser jsmsUser : conUser) {
					objectIds.add(jsmsUser.getId());
				}
			}

			if(null!=conAgent){
				for (Integer integer : conAgent) {
					objectIds.add(Long.valueOf(integer));
				}
			}
			if(null!=objectIds){
				params.put("objectids",objectIds);
			}

		}

		long conend = System.currentTimeMillis();
		logger.info("查询条件相关耗时={}",conend-start);
		if(StringUtils.isNotBlank(financialType)){
			params.put("financialType",financialType);
		}
		if(StringUtils.isNotBlank(businessType)){
			params.put("businessType",businessType);
		}
		if(StringUtils.isNotBlank(startDateTime)){
			params.put("startDateTime",startDateTime);
		}
		if(StringUtils.isNotBlank(endDateTime)){
			params.put("endDateTime",endDateTime);
		}

		UserSession user=getUserFromSession(session);
		if(AgentUtils.isSaleRole(user)){
			params.put("saleId",user.getId());
		}else {
			return	R.error("非销售人员,无法查询流水记录");
		}




		jpage.setParams(params);

		if(StringUtils.isNotBlank(rows)){
			jpage.setRows(Integer.valueOf(rows));
		}
		if(StringUtils.isNotBlank(page)){
			jpage.setPage(Integer.valueOf(page));
		}

		jpage.setOrderByClause("create_time desc");


		jpage=jsmsSaleCreditBillService.queryList(jpage);
		long end = System.currentTimeMillis();
		logger.info("查库耗时={}",end-conend);
		jpage.setData(billEntityToDTO(jpage.getData()));
		long end1 = System.currentTimeMillis();
		logger.info("转换dto耗时={}",end1-end);
		PageContainer container = PageConvertUtil.pageToContainer(jpage);

		return R.ok("获取我的授信流水列表成功",container);
//        return orderInfoService.query(params);
	}

	/**
	 * 流水转换dto
	 * @return
	 */
	private List<JsmsSaleCreditBillDTO> billEntityToDTO(List<JsmsSaleCreditBill> salebills){

		List<JsmsSaleCreditBillDTO> saledto=new ArrayList<>();

		for (JsmsSaleCreditBill salebill : salebills) {
			JsmsSaleCreditBillDTO dto=new JsmsSaleCreditBillDTO();
			long end = System.currentTimeMillis();

			BeanUtil.copyProperties(salebill,dto);
			long end1 = System.currentTimeMillis();
			logger.info("转换单个dto耗时={}",end1-end);
			//授信对象
			if(Objects.equals(salebill.getObjectType(), Integer.valueOf(ObjectType.销售.getValue()))){
				long end111 = System.currentTimeMillis();
				JsmsUser ouser=jsmsUserService.getById2(salebill.getObjectId());
				long end1111 = System.currentTimeMillis();
				logger.info("查询授信销售耗时={}",end1111-end111);
				dto.setObjectName(ouser.getRealname());
			}else if(Objects.equals(salebill.getObjectType(),Integer.valueOf(ObjectType.代理商.getValue()))){
				JsmsAgentInfo agent=jsmsAgentInfoService.getByAgentId(salebill.getObjectId().intValue());
				dto.setObjectName(agent.getAgentName());
			}
			long end11 = System.currentTimeMillis();
			logger.info("查询授信对象耗时={}",end11-end1);
			//操作对象
			if(Objects.equals(salebill.getAdminId(),0L)){

				dto.setAdminName("系统");
			}else {
				JsmsUser adminuser=jsmsUserService.getById2(salebill.getAdminId());
				dto.setAdminName(adminuser.getRealname());
			}

			long end2 = System.currentTimeMillis();
			logger.info("查询操作用户耗时={}",end2-end11);
			saledto.add(dto);
		}

		return saledto;

	}

	@RequestMapping("/myCredit/total")
	@ResponseBody
	public R totalAmount(HttpSession session,@RequestParam Map<String,String> params){

		Map<String,Object> params1=new HashedMap();
		params1.putAll(params);
		if(StringUtils.isNotBlank(params.get("condition"))){
			params1.put("condition",params.get("condition"));
			List<Long> objectIds=new ArrayList();
			List<JsmsUser> conUser=jsmsUserService.getByRealname(params.get("condition"));
			List<Integer> conAgent=jsmsAgentInfoService.queryAgentIdsByParams(params1);
			if(null!=conUser){
				for (JsmsUser jsmsUser : conUser) {
					objectIds.add(jsmsUser.getId());
				}
			}

			if(null!=conAgent){
				for (Integer integer : conAgent) {
					objectIds.add(Long.valueOf(integer));
				}
			}
			if(null!=objectIds){
				params1.put("objectids",objectIds);
			}

		}
		if(StringUtils.isNotBlank(params.get("financialType"))){
			params1.put("financialType",params.get("financialType"));
		}
		if(StringUtils.isNotBlank(params.get("businessType"))){
			params1.put("businessType",params.get("businessType"));
		}
		if(StringUtils.isNotBlank(params.get("startDateTime"))){
			params1.put("startDateTime",params.get("startDateTime"));
		}
		if(StringUtils.isNotBlank(params.get("endDateTime"))){
			params1.put("endDateTime",params.get("endDateTime"));
		}

		UserSession user=getUserFromSession(session);
		if(AgentUtils.isSaleRole(user)){
			params1.put("saleId",user.getId());
		}else {
			return	R.error("非销售人员,无法查询流水记录");
		}
		String total=jsmsSaleCreditBillService.getSumAmount(params1);

		return  R.ok("请求计算流水总额成功！",total);
	}

	/**
	 * 我的销售数据导出
	 *@param param
	 * @param response
	 */
	@RequestMapping("/myCredit/export")
//    public void stdSMSPageListExport(String condition, String status, String start_time_day, String end_time_day, HttpServletRequest request, HttpServletResponse response) {
	public void myCreditListExport(HttpSession session,@RequestParam Map<String,String> param,  HttpServletResponse response) {
		String filePath = ConfigUtils.save_path + "/我的销售信息_" + DateTime.now().toString("yyyyMMddHHmmss") + ".xls";
		Map<String,Object> params=new HashedMap();
		params.putAll(param);
		StringBuffer buffer = new StringBuffer();
		buffer.append("查询条件 -> ");
		if (StringUtils.isNotBlank(param.get("condition"))) {
//            params.put("condition", params.get("condition"));
			buffer.append("用户名称/用户ID：")
					.append(params.get("condition"))
					.append(";");
			params.put("condition",params.get("condition"));
			List<Long> objectIds=new ArrayList();
			List<JsmsUser> conUser=jsmsUserService.getByRealname(param.get("condition"));
			List<Integer> conAgent=jsmsAgentInfoService.queryAgentIdsByParams(params);
			if(null!=conUser){
				for (JsmsUser jsmsUser : conUser) {
					objectIds.add(jsmsUser.getId());
				}
			}

			if(null!=conAgent){
				for (Integer integer : conAgent) {
					objectIds.add(Long.valueOf(integer));
				}
			}
			if(null!=objectIds){
				params.put("objectids",objectIds);
			}
		}

		if (StringUtils.isNotBlank(param.get("financialType"))) {
			buffer.append("财务类型 ：")
					.append(FinancialType.getDescByValue(param.get("financialType")))
					.append(";");
		}
		if (StringUtils.isNotBlank(param.get("businessType"))) {
			buffer.append("业务类型 ：")
					.append(BusinessType.getDescByValue(Integer.valueOf(param.get("businessType"))))
					.append(";");
		}

		if (StringUtils.isNotBlank(param.get("startDateTime"))) {
			buffer.append("  开始时间：")
					.append(params.get("startDateTime"))
					.append(";");
		}

		if (StringUtils.isNotBlank(param.get("endDateTime"))) {
			buffer.append("  结束时间：")
					.append(params.get("endDateTime"));
			params.put("endDateTime", new StringBuilder(param.get("endDateTime")).append(" 23:59:59").toString());
		}
		UserSession user=getUserFromSession(session);
		if(AgentUtils.isSaleRole(user)){
			params.put("saleId",user.getId());
		}else {
			logger.info("非销售人员,无法查询流水记录!");
			return;
		}

		JsmsPage jpage=new JsmsPage();
		jpage.setParams(params);
		jpage.setRows(Integer.valueOf(ConfigUtils.max_export_excel_num));
		jpage.setOrderByClause("create_time desc");
		jpage=jsmsSaleCreditBillService.queryList(jpage);

		List<JsmsSaleCreditBillDTO>  billdto= billEntityToDTO(jpage.getData());

		Excel excel = new Excel();
		excel.setFilePath(filePath);
		excel.setTitle("我的授信流水");
		excel.addRemark(buffer.toString());
		excel.addHeader(20, "业务单号", "id");
		excel.addHeader(20, "业务类型", "businessTypeName");
		excel.addHeader(20, "财务类型", "financialTypeName");
		excel.addHeader(20, "账单金额(元)", "billamount");
		excel.addHeader(20, "用户ID", "objectId");
		excel.addHeader(20, "用户名称", "objectName");
		excel.addHeader(20, "用户类型", "objectTypeName");
		excel.addHeader(20, "操作者", "adminName");
		excel.addHeader(20, "备注", "remark");
		excel.addHeader(20, "操作时间", "createTimeStr");


		Map<String,Object> params1=new HashedMap();
		params1.putAll(params);
		Map<String,Object> totalDate = new HashedMap();
		totalDate.put("id", "总计");
		totalDate.put("businessTypeName", "-");
		totalDate.put("financialTypeName", "-");
		totalDate.put("billamount", jsmsSaleCreditBillService.getSumAmount(params1));
		totalDate.put("objectId", "-");
		totalDate.put("objectName", "-");
		totalDate.put("objectTypeName", "-");
		totalDate.put("adminName", "-");
		totalDate.put("remark", "-");
		totalDate.put("createTimeStr", "-");
		List<Map<String,Object>> data=BeanUtil.ListbeanToMap(billdto,false);
		data.add(totalDate);



		excel.setDataList(data);

		if (ExcelUtils.exportExcel(excel,true)) {
			FileUtils.download(response, filePath);
			FileUtils.delete(filePath);
		} else {
			response.setContentType("text/plain;charset=UTF-8");
			try {
				response.getWriter().write("导出Excel文件失败，请联系管理员");
				response.getWriter().flush();
			} catch (IOException e) {
				logger.error("导出Excel文件失败", e);
			}
		}
	}



	/**
	 * 客户授信
	 * finance\sell\credit4Customer.html
	 */
	@RequestMapping("/credit4Customer")
	public String credit4CustomerView(Model model, HttpSession session) {
		model.addAttribute("max_export_num", ConfigUtils.max_export_excel_num);
		UserSession user=getUserFromSession(session);
		model.addAttribute("menus", AgentUtils.hasMenuRight(user, "我的授信-wdsx", "客户授信-khsx","客户历史授信-khlssx","销售财务-xscw","销售历史财务-xslscw","销售授信账单-xssxzd","客户授信账单-khsxzd"));
//		UserSession user = getUserFromSession(session);
//		model.addAttribute("menus", AgentUtils.hasMenuRight(user, "代理商财务-dlscw", "历史数据-lssj"));
		//sell\myCredit.html

		if(AgentUtils.isSaleRole(user) && Objects.equals(user.getDataAuthority(), DataAuthority.仅看自己数据.getValue())){
			model.addAttribute("saleIdforOne", user.getId());
		}

		return "finance/sell/credit4Customer";
	}

	/**
	 * 查询所有的销售
	 */
	@RequestMapping("/queryAllSales")
	@ResponseBody
	public List<JsmsUser> getAccounts(HttpSession session, HttpServletRequest request) {
		// String clientId = request.getParameter("clientId");
		UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		//	List<JsmsAccount> data = jsmsAccountService.findAllListOfOperation(user.getId());
		List<JsmsUser> data=new ArrayList<>();
		if(AgentUtils.isSaleRole(user) && Objects.equals(user.getDataAuthority(), DataAuthority.仅看自己数据.getValue()) ){
			//普通销售看自己
			JsmsUser saleUser=jsmsUserService.getById2(user.getId());
			data.add(saleUser);
		}else if(AgentUtils.isSaleRole(user) && Objects.equals(user.getDataAuthority(), DataAuthority.所在部门及下级部门.getValue()) ){
			// 销售总监查询自己及下属数据权限判断
			DataAuthorityCondition auth=AgentUtils.getDataAuthorityCondition(user.getId(),true,false);
			Set ids= new HashSet();


			for (Long id : auth.getIds()) {
				ids.add(id);
			}
			data=jsmsUserService.getByIds(ids);
		}else {
			//财务或者各个大佬能看所有销售角色
			data =jsmsUserService.getUserByRoleName("销售人员");
		}


		return data;
	}


	/**
	 * 客户授信查询分页数据
	 *
	 * @param rows           行数
	 * @param page           当前查询页码
	 * @return
	 */
	@ApiOperation(value = "查询客户授信列表数据", notes = "查询客户授信列表数据", tags = "财务管理-销售财务", response = PageContainer.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "page", value = "页码", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "rows", value = "行数", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "page", value = "运营商类型", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "condition", value = "订单号/产品代码/客户ID/代理商ID", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "agentType", value = "代理商类型", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "status", value = "代理商状态", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "besale", value = "归属销售", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "startDateTime", value = "开始时间", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "endDateTime", value = "结束时间", dataType = "String", paramType = "query") })
	@RequestMapping("/credit4Customer/list")
	@ResponseBody
//    public PageContainer list(String rows, String page, String condition, String status, String startDateTime, String endDateTime) {
	public R credit4CustomerList(HttpSession session,String rows, String page,String condition,String agentType,String status,String besales,String startDateTime,String endDateTime) {
//        Map<String, String> params = new HashMap<>();
		JsmsPage jpage=new JsmsPage();
		Map<String,Object> params=new HashMap<>();

		if(StringUtils.isNotBlank(condition)){
			params.put("condition",condition);
		}
		if(StringUtils.isNotBlank(agentType)){
			params.put("agentType",agentType);
		}
		if(StringUtils.isNotBlank(status)){
			params.put("status",status);
		}
		UserSession user=getUserFromSession(session);
		if(StringUtils.isNotBlank(besales)){
			params.put("belongSale",besales);
		}else {
			if(AgentUtils.isFinacorRole(user)){

			}else {
				if(AgentUtils.isSaleRole(user) && Objects.equals(user.getDataAuthority(), DataAuthority.所在部门及下级部门.getValue()) ){
					// 销售总监查询自己及下属数据权限判断
					DataAuthorityCondition auth=AgentUtils.getDataAuthorityCondition(user.getId(),true,false);

					params.put("belongSales",auth.getIds());
				}else if(AgentUtils.isSaleRole(user)){
					params.put("belongSale",user.getId());
				}
			}

		}

//		params.put("pageRowCount", rows);
//		params.put("currentPage", page);
//		params.put("orderByClause", "create_time DESC");

		jpage.setOrderByClause("a.update_time desc");
		jpage.setParams(params);

		if(StringUtils.isNotBlank(rows)){
			jpage.setRows(Integer.valueOf(rows));
		}
		if(StringUtils.isNotBlank(page)){
			jpage.setPage(Integer.valueOf(page));
		}

		jpage=jsmsAgentInfoService.queryListForSale(jpage);


		jpage.setData(this.agentEntityToDTO(jpage.getData()));

		PageContainer container = PageConvertUtil.pageToContainer(jpage);

		return R.ok("获取客户授信列表成功",container);
	}

	@RequestMapping("/credit4Customer/total")
	@ResponseBody
	public R credit4CustomerTotalAmount(HttpSession session,@RequestParam Map<String,String> params){

		Map<String,Object> params1=new HashedMap();
		params1.putAll(params);

		if(StringUtils.isNotBlank(params.get("condition"))){
			params1.put("condition",params.get("condition"));
		}
		if(StringUtils.isNotBlank(params.get("agentType"))){
			params1.put("agentType",params.get("agentType"));
		}
		if(StringUtils.isNotBlank(params.get("status"))){
			params1.put("status",params.get("status"));
		}
		UserSession user=getUserFromSession(session);
		if(StringUtils.isNotBlank(params.get("besales"))){
			params1.put("belongSale",params.get("besales"));
		}else {
			if(AgentUtils.isFinacorRole(user)){

			}else {
				if(AgentUtils.isSaleRole(user) && Objects.equals(user.getDataAuthority(), DataAuthority.所在部门及下级部门.getValue()) ){
					// 销售总监查询自己及下属数据权限判断
					DataAuthorityCondition auth=AgentUtils.getDataAuthorityCondition(user.getId(),true,false);

					params1.put("belongSales",auth.getIds());
				}else if(AgentUtils.isSaleRole(user)){
					params1.put("belongSale",user.getId());
				}
			}

		}

		Map<String,Object> sum=new HashedMap();
		sum.put("sumBa",BigDecimal.ZERO);
		sum.put("sumCr",BigDecimal.ZERO);
		sum.put("sumHi",BigDecimal.ZERO);
		sum.put("sumCu",BigDecimal.ZERO);
		sum.put("sumNo",BigDecimal.ZERO);
		List<Integer> agentIds=jsmsAgentInfoService.queryAgentIdsByParams(params1);
		if(agentIds.size()>0){
			sum=jsmsAgentAccountService.querySumBlance(agentIds);
		}



		return  R.ok("请求计算流水总额成功！",sum);
	}

	/**
	 * 代理商信息组装为dto
	 * @param agents
	 * @return
	 */
	private  List<AgentForSaleDTO> agentEntityToDTO(List<JsmsAgentInfo> agents){
		List<AgentForSaleDTO> agentdto=new ArrayList<>();
		for (JsmsAgentInfo jsmsAgentInfo : agents) {
			AgentForSaleDTO dto=new AgentForSaleDTO();
			BigDecimal balance,creditBalance,historyPayment,currentCredit,noBackPayment;

			//查询归属销售
			JsmsUser bUser=jsmsUserService.getById2(jsmsAgentInfo.getBelongSale());
			if(bUser!=null){
				dto.setBelongSaleName(bUser.getRealname());
			}else {
				dto.setBelongSaleName("未知");
			}

			//关联代理商账号
			JsmsAgentAccount account=jsmsAgentAccountService.getByAgentId(jsmsAgentInfo.getAgentId());
			if(account==null){
				balance=BigDecimal.ZERO;
				creditBalance=BigDecimal.ZERO;
				historyPayment=BigDecimal.ZERO;
				currentCredit=BigDecimal.ZERO;
				noBackPayment=BigDecimal.ZERO;

			}else {
				balance=account.getBalance();
				creditBalance=account.getCreditBalance();
				historyPayment=account.getHistoryPayment();
				currentCredit=account.getCurrentCredit();
				noBackPayment=account.getNoBackPayment();
			}
			dto.setBalance(balance);
			dto.setCreditBalance(creditBalance);
			dto.setHistoryPayment(historyPayment);
			dto.setCurrentCredit(currentCredit);
			dto.setNoBackPayment(noBackPayment);
			BeanUtil.copyProperties(jsmsAgentInfo,dto);

			agentdto.add(dto);
		}

		return agentdto;
	}


	/**
	 * 客户授信数据导出
	 *@param param
	 * @param response
	 */
	@RequestMapping("/credit4Customer/export")
//    public void stdSMSPageListExport(String condition, String status, String start_time_day, String end_time_day, HttpServletRequest request, HttpServletResponse response) {
	public void credit4CustomerExport(HttpSession session,@RequestParam Map<String,String> param,  HttpServletResponse response) {
		String filePath = ConfigUtils.save_path + "/客户授信信息_" + DateTime.now().toString("yyyyMMddHHmmss") + ".xls";
		Map<String,Object> params=new HashedMap();
		params.putAll(param);
		StringBuffer buffer = new StringBuffer();
		buffer.append("查询条件 -> ");
		if (StringUtils.isNotBlank(param.get("condition"))) {
//            params.put("condition", params.get("condition"));
			buffer.append("代理商ID/代理商名称/邮箱/手机：")
					.append(params.get("condition"))
					.append(";");
			params.put("condition",params.get("condition"));
		}

		if (StringUtils.isNotBlank(param.get("agentType"))) {
			buffer.append("代理商类型 ：")
					.append(AgentType.getDescByValue(Integer.valueOf(param.get("agentType"))))
					.append(";");
		}
		if (StringUtils.isNotBlank(param.get("status"))) {
			buffer.append("代理商状态 ：")
					.append(AgentStatus.getDescByValue(param.get("status")))
					.append(";");
		}
		UserSession user=getUserFromSession(session);
		if(StringUtils.isNotBlank(param.get("besales"))){
			JsmsUser saleUser=jsmsUserService.getById2(Long.valueOf(param.get("besales")));
			buffer.append(" 归属销售：")
					.append(params.get("besales")+"-"+saleUser.getRealname())
					.append(";");
			params.put("belongSale",param.get("besales"));
		}else {
			if(AgentUtils.isFinacorRole(user)){

			}else {
				if(AgentUtils.isSaleRole(user) && Objects.equals(user.getDataAuthority(), DataAuthority.所在部门及下级部门.getValue()) ){
					// 销售总监查询自己及下属数据权限判断
					DataAuthorityCondition auth=AgentUtils.getDataAuthorityCondition(user.getId(),true,false);

					params.put("belongSales",auth.getIds());
				}else if(AgentUtils.isSaleRole(user)){
					params.put("belongSale",user.getId());
				}
			}

		}


//		UserSession user=getUserFromSession(session);
//		if(isSaleRole(user)){
//			params.put("saleId",user.getId());
//		}else {
//			logger.info("非销售人员,无法查询流水记录!");
//			return;
//		}

		JsmsPage jpage=new JsmsPage();
		jpage.setParams(params);
		jpage.setRows(Integer.valueOf(ConfigUtils.max_export_excel_num));
		jpage.setOrderByClause("a.update_time desc");
		jpage=jsmsAgentInfoService.queryListForSale(jpage);

		List<AgentForSaleDTO>  billdto= this.agentEntityToDTO(jpage.getData());

		Excel excel = new Excel();
		excel.setFilePath(filePath);
		excel.setTitle("客户授信");
		excel.addRemark(buffer.toString());
		excel.addHeader(20, "代理商ID", "agentId");
		excel.addHeader(20, "代理商名称", "agentName");
		excel.addHeader(20, "代理商类型", "agentTypeName");
		excel.addHeader(20, "代理商状态", "statusName");
		excel.addHeader(20, "邮箱", "email");
		excel.addHeader(20, "手机", "mobile");
		excel.addHeader(20, "归属销售", "belongSaleName");
		excel.addHeader(20, "余额(元)", "balance");
		excel.addHeader(20, "历史授信额度(元)", "creditBalance");
		excel.addHeader(20, "历史授信回款额度(元)", "historyPayment");
		excel.addHeader(20, "授信余额(元)", "currentCredit");
		excel.addHeader(20, "未回款授信额度(元)", "noBackPayment");


		Map<String,Object> params1=new HashedMap();
		params1.putAll(params);

		if(StringUtils.isNotBlank(param.get("besales"))){
			params1.put("belongSale",params.get("besales"));
		}else {
			if(AgentUtils.isFinacorRole(user)){

			}else {
				if(AgentUtils.isSaleRole(user) && Objects.equals(user.getDataAuthority(), DataAuthority.所在部门及下级部门.getValue()) ){
					// 销售总监查询自己及下属数据权限判断
					DataAuthorityCondition auth=AgentUtils.getDataAuthorityCondition(user.getId(),true,false);

					params1.put("belongSales",auth.getIds());
				}else if(AgentUtils.isSaleRole(user)){
					params1.put("belongSale",user.getId());
				}
			}

		}



		Map<String,Object> sum=new HashedMap();
		sum.put("sumBa",BigDecimal.ZERO);
		sum.put("sumCr",BigDecimal.ZERO);
		sum.put("sumHi",BigDecimal.ZERO);
		sum.put("sumCu",BigDecimal.ZERO);
		sum.put("sumNo",BigDecimal.ZERO);
		List<Integer> agentIds=jsmsAgentInfoService.queryAgentIdsByParams(params1);
		if(agentIds!=null){
			sum=jsmsAgentAccountService.querySumBlance(agentIds);
		}

		Map<String,Object> totalDate = new HashedMap();
		totalDate.put("agentId", "");
		totalDate.put("agentName", "-");
		totalDate.put("agentTypeName", "-");
		totalDate.put("statusName","-");
		totalDate.put("email", "-");
		totalDate.put("mobile", "-");
		totalDate.put("belongSaleName", "总计");
		totalDate.put("balance", sum.get("sumBa"));
		totalDate.put("creditBalance",sum.get("sumCr"));
		totalDate.put("historyPayment", sum.get("sumHi"));
		totalDate.put("currentCredit",sum.get("sumCu"));
		totalDate.put("noBackPayment",sum.get("sumNo"));
		List<Map<String,Object>> data=BeanUtil.ListbeanToMap(billdto,false);
		data.add(totalDate);



		excel.setDataList(data);

		if (ExcelUtils.exportExcel(excel,true)) {
			FileUtils.download(response, filePath);
			FileUtils.delete(filePath);
		} else {
			response.setContentType("text/plain;charset=UTF-8");
			try {
				response.getWriter().write("导出Excel文件失败，请联系管理员");
				response.getWriter().flush();
			} catch (IOException e) {
				logger.error("导出Excel文件失败", e);
			}
		}
	}





	@RequestMapping("/credit4Customer/toOperation")
	@ResponseBody
	public R operationView(HttpSession session,@RequestParam Map<String,String> params){

		Map<String,Object> params1=new HashedMap();
		params1.putAll(params);

		if(StringUtils.isNotBlank(params.get("agentId"))){
			params1.put("agentId",params.get("agentId"));
		}
		Map<String,Object> data=new HashedMap();

		JsmsAgentInfo agent=jsmsAgentInfoService.getByAgentId(Integer.valueOf(params.get("agentId")));
		if(agent==null){
			return	R.error("代理商不存在！");
		}else {
			data.put("agentId",agent.getAgentId());
			data.put("agentName",agent.getAgentName());

			//销售信息相关
			if(agent.getBelongSale()==null){
				return	R.error("代理商不存在归属销售,请关联后操作！");
			}else {
				data.put("belongSale",agent.getBelongSale());
				JsmsUser beUser=jsmsUserService.getById2(agent.getBelongSale());
				if(beUser==null){
					return	R.error("归属销售ID不存在！");
				}else {
					data.put("belongSaleName",beUser.getRealname());
				}
				JsmsSaleCreditAccount saleAccount=jsmsSaleCreditAccountService.getBySaleId(agent.getBelongSale());
				if(saleAccount==null){
					data.put("saleCurrentCredit",BigDecimal.ZERO);
				}else {
					data.put("saleCurrentCredit",saleAccount.getCurrentCredit());
				}
			}

			//代理商账号信息
			JsmsAgentAccount agentAccount=jsmsAgentAccountService.getByAgentId(agent.getAgentId());
			if(agentAccount==null){
				data.put("creditBalance",BigDecimal.ZERO);
				data.put("historyPayment",BigDecimal.ZERO);
				data.put("currentCredit",BigDecimal.ZERO);
				data.put("noBackPayment",BigDecimal.ZERO);
			}else {
				data.put("creditBalance",agentAccount.getCreditBalance());
				data.put("historyPayment",agentAccount.getHistoryPayment());
				data.put("currentCredit",agentAccount.getCurrentCredit());
				data.put("noBackPayment",agentAccount.getNoBackPayment());
			}
		}


		return  R.ok("请求客户授信操作数据成功！",data);
	}

	@RequestMapping("/credit4Customer/operationOfCredit")
	@ResponseBody
	public R operationOfCredit(HttpSession session,@RequestParam Map<String,String> params){

		Map<String, Object> data = new HashedMap();
		UserSession user = getUserFromSession(session);
		//校验授信余额变化
		data = this.checkForCredit(params);
		if (Objects.equals("fail", data.get("status"))) {

			return R.error(data.get("msg").toString());
		} else if (Objects.equals("overdue", data.get("status"))) {
			R r = new R();
			r.setCode(405);
			r.setMsg(data.get("msg").toString());
			return r;
		} else {
			//代理商信息
			Integer opType=Integer.valueOf(params.get("operateType"));
			Integer sopType;
			if(Objects.equals(0,opType)){
				opType=PaymentType.增加授信.getValue();
				sopType=BusinessType.销售给客户授信.getValue();
			}else if(Objects.equals(1 ,opType)){
				opType=PaymentType.降低授信.getValue();
				sopType=BusinessType.销售降低客户授信.getValue();
			}else {
				return R.error("操作非法！");
			}
			R r1=jsmsSaleCreditService.creditForAgent(user.getId(),opType,Integer.valueOf(params.get("agentId")),new BigDecimal(params.get("amount")),params.get("remark"));


			if(Objects.equals(r1.getCode(), SysConstant.FAIL_CODE)){
				return  r1;
			}
			//销售信息
			r1=jsmsSaleCreditService.creditForSale(user.getId(),sopType,Integer.valueOf(params.get("agentId")),Long.valueOf(data.get("saleId").toString()),new BigDecimal(params.get("amount")),params.get("remark"));

			if(Objects.equals(r1.getCode(),SysConstant.FAIL_CODE)){
				return  r1;
			}


			if (Objects.equals("0", params.get("operateType"))) {
				data.put("msg", "客户增加授信操作成功");
			} else if (Objects.equals("1", params.get("operateType"))) {
				data.put("msg", "客户降低授信操作成功");
			}
		}


		return  R.ok("客户授信操作成功！",data);
	}


	/**
	 * 校验授信余额是否变化
	 * @param params
	 * @return
	 */
	private  Map<String,Object> checkForCredit(Map<String,String> params){
		Map<String, Object> results = new HashedMap();
		if(BigDecimal.ZERO.compareTo(new BigDecimal(params.get("amount")))==0){
			results.put("status", SysConstant.FAIL);
			results.put("msg", "操作金额不能为0");
			return results;

		}

		JsmsAgentInfo agent = jsmsAgentInfoService.getByAgentId(Integer.valueOf(params.get("agentId")));
		if (agent == null) {
			results.put("status",  SysConstant.FAIL);
			results.put("msg", "代理商不存在！");
			return results;
		} else {
			results.put("saleId",agent.getBelongSale());
			JsmsAgentAccount agentAccount = jsmsAgentAccountService.getByAgentId(agent.getAgentId());
			if (agentAccount == null) {
				results.put("status",  SysConstant.FAIL);
				results.put("msg", "代理商账户信息不存在！");
				return results;
			} else {
				if (agentAccount.getCurrentCredit().compareTo(new BigDecimal(params.get("currentCredit"))) != 0) {
					results.put("status", "overdue");
					results.put("code", 405);
					results.put("msg", "当前数据已过期,请重新授信！");
					return results;
				} else if (Objects.equals("1", params.get("operateType")) && agentAccount.getCurrentCredit().compareTo(new BigDecimal(params.get("amount"))) < 0) {
					results.put("status",  SysConstant.FAIL);
					results.put("msg", "代理商授信余额不足！");
					return results;
				}

			}
		}
		JsmsUser saleUser = jsmsUserService.getById2(Long.valueOf(params.get("saleId")));
		if (saleUser == null) {
			results.put("status", "fail");
			results.put("msg", "销售账户不存在！");
			return results;
		} else {
			JsmsSaleCreditAccount saleAccount = jsmsSaleCreditAccountService.getBySaleId(saleUser.getId());
			if (saleAccount == null) {
				results.put("status",  SysConstant.FAIL);
				results.put("msg", "销售账户信息不存在！");
				return results;
			} else {
				if (saleAccount.getCurrentCredit().compareTo(new BigDecimal(params.get("saleCurrentCredit"))) != 0) {
					results.put("status", "overdue");
					results.put("code", 405);
					results.put("msg", "当前数据已过期,请重新授信！");
					return results;
				} else if (Objects.equals("0", params.get("operateType")) && saleAccount.getCurrentCredit().compareTo(new BigDecimal(params.get("amount"))) < 0) {
					results.put("status",  SysConstant.FAIL);
					results.put("msg", "销售授信余额不足！");
					return results;
				}

			}
		}
		results.put("status",  SysConstant.SUCCESS);
		results.put("msg", "校验成功！");
		return results;
	}


/*
	*/
/**
 * 授信代理商变化
 * @param user
 * @param params
 * @return
 *//*

	private  Map<String,Object> creditForAgent(UserSession user,Map<String,String> params){
		//,String agentId,String amount,String operateType
		Map<String,Object> results=new HashedMap();

		JsmsAgentInfo agent=jsmsAgentInfoService.getByAgentId(Integer.valueOf(params.get("agentId")));
		if(agent==null){
			results.put("status","fail");
			results.put("msg","代理商不存在！");
			return	results;
		}else {
			JsmsAgentAccount agentAccount=jsmsAgentAccountService.getByAgentId(agent.getAgentId());
			if(agentAccount==null){
				results.put("status","fail");
				results.put("msg","代理商账户信息不存在！");
				return	results;
			}else {

				JsmsAgentBalanceBill bill=new JsmsAgentBalanceBill();

				bill.setAgentId(agentAccount.getAgentId());
				bill.setAmount(new BigDecimal(params.get("amount")));
				bill.setCreateTime(new Date());
				bill.setAdminId(user.getId());
				bill.setRemark(params.get("remark"));


				JsmsAgentAccount agentAcc=new JsmsAgentAccount();
				agentAcc.setAgentId(agentAccount.getAgentId());
				if(Objects.equals("0",params.get("operateType"))){
					//添加授信
					bill.setPaymentType(PaymentType.增加授信.getValue());
					bill.setFinancialType(FinancialType.入账.getValue());
					agentAcc.setCurrentCredit(agentAccount.getCurrentCredit().add(new BigDecimal(params.get("amount"))));
					agentAcc.setCreditBalance(agentAccount.getCreditBalance().add(new BigDecimal(params.get("amount"))));
				}else if(Objects.equals("1",params.get("operateType"))){
					//降低授信
					bill.setPaymentType(PaymentType.降低授信.getValue());
					bill.setFinancialType(FinancialType.出账.getValue());
					agentAcc.setCreditBalance(agentAccount.getCreditBalance().subtract(new BigDecimal(params.get("amount"))));
					agentAcc.setCurrentCredit(agentAccount.getCurrentCredit().subtract(new BigDecimal(params.get("amount"))));
				}

				bill.setBalance(agentAccount.getBalance());
				bill.setCreditBalance(agentAcc.getCreditBalance());
				bill.setHistoryPayment(agentAccount.getHistoryPayment());
				bill.setCurrentCredit(agentAcc.getCurrentCredit());
				bill.setNoBackPayment(agentAccount.getNoBackPayment());
				//生成账单信息
				int abi=jsmsAgentBalanceBillService.insert(bill);
				if(abi>0){
					logger.info("生成代理商账单信息成功,账单bill={}", JsonUtils.toJson(bill));
					//更新账户信息

					int aup=jsmsAgentAccountService.updateSelective(agentAcc);
					if(aup>0){
						logger.info("更新代理商账户信息成功,agentAccount={}", JsonUtils.toJson(agentAcc));
						results.put("status","success");
						results.put("msg","更新代理商账户信息成功！");

					}else {
						results.put("status","fail");
						results.put("msg","更新代理商账户信息失败!");
						logger.error("生成代理商账单信息入库失败,agentAccount={}", JsonUtils.toJson(agentAcc));
						return	results;
					}


				}else {
					results.put("status","fail");
					results.put("msg","生成代理商账单信息入库失败!");
					logger.error("生成代理商账单信息入库失败,账单bill={}", JsonUtils.toJson(bill));
					return	results;
				}


			}
		}

		results.put("status","success");
		results.put("msg","代理商账单信息更新入库成功!");

		return results;
	}

	*/
/**
 * 授信销售变化
 * @param user
 * @param params
 * @return
 *//*

	private  Map<String,Object> creditForSale(UserSession user,Map<String,String> params){
		//,String agentId,String amount,String operateType
		Map<String,Object> results=new HashedMap();

		JsmsUser saleUser=jsmsUserService.getById2(Long.valueOf(params.get("saleId")));
		if(saleUser==null){
			results.put("status","fail");
			results.put("msg","销售账户不存在！");
			return	results;
		}else {
			JsmsSaleCreditAccount saleAccount=jsmsSaleCreditAccountService.getBySaleId(saleUser.getId());
			if(saleAccount==null){
				results.put("status","fail");
				results.put("msg","销售账户信息不存在！");
				return	results;
			}else {

				JsmsSaleCreditBill bill=new JsmsSaleCreditBill();



				JsmsSaleCreditAccount saleAcc=new JsmsSaleCreditAccount();
				saleAcc.setSaleId(saleAccount.getSaleId());
				if(Objects.equals("0",params.get("operateType"))){
					//添加授信
					bill.setBusinessType(BusinessType.销售给客户授信.getValue());
					bill.setFinancialType(FinancialType.出账.getValue());
					saleAcc.setSaleHistoryCredit(saleAccount.getSaleHistoryCredit().add(new BigDecimal(params.get("amount"))));
					saleAcc.setCurrentCredit(saleAccount.getCurrentCredit().subtract(new BigDecimal(params.get("amount"))));
					saleAcc.setNoBackPayment(saleAccount.getNoBackPayment().add(new BigDecimal(params.get("amount"))));

				}else if(Objects.equals("1",params.get("operateType"))){
					//降低授信
					bill.setBusinessType(BusinessType.销售降低客户授信.getValue());
					bill.setFinancialType(FinancialType.入账.getValue());
					saleAcc.setSaleHistoryCredit(saleAccount.getSaleHistoryCredit().subtract(new BigDecimal(params.get("amount"))));
					saleAcc.setCurrentCredit(saleAccount.getCurrentCredit().add(new BigDecimal(params.get("amount"))));
					saleAcc.setNoBackPayment(saleAccount.getNoBackPayment().subtract(new BigDecimal(params.get("amount"))));
				}

				bill.setSaleId(saleUser.getId());
				bill.setAmount(new BigDecimal(params.get("amount")));
				bill.setObjectId(Long.valueOf(params.get("agentId")));
				bill.setObjectType(ObjectType.代理商.getValue());
				bill.setFinancialHistoryCredit(saleAccount.getFinancialHistoryCredit());
				bill.setCurrentCredit(saleAccount.getCurrentCredit());
				bill.setSaleHistoryCredit(saleAccount.getSaleHistoryCredit());
				bill.setAgentHistoryPayment(saleAccount.getAgentHistoryPayment());
				bill.setNoBackPayment(saleAccount.getNoBackPayment());
				bill.setAdminId(user.getId());
				bill.setCreateTime(new Date());
				bill.setRemark(params.get("remark"));

				//生成账单信息
				int abi=jsmsSaleCreditBillService.insert(bill);
				if(abi>0){
					logger.info("生成销售账单信息成功,账单bill={}", JsonUtils.toJson(bill));
					//更新账户信息

					int aup=jsmsSaleCreditAccountService.updateSelective(saleAcc);
					if(aup>0){
						logger.info("更新销售账户信息成功,agentAccount={}", JsonUtils.toJson(saleAcc));
						results.put("status","success");
						results.put("msg","更新销售账户信息成功！");

					}else {
						results.put("status","fail");
						results.put("msg","更新销售账户信息失败!");
						logger.error("更新销售账户信息失败,agentAccount={}", JsonUtils.toJson(saleAcc));
						return	results;
					}


				}else {
					results.put("status","fail");
					results.put("msg","生成销售账单信息入库失败!");
					logger.error("生成销售账单信息入库失败,账单bill={}", JsonUtils.toJson(bill));
					return	results;
				}


			}
		}

		results.put("status","success");
		results.put("msg","销售账单信息更新入库成功!");

		return results;
	}
*/






	/**
	 * 客户历史授信
	 * finance\sell\credit4Customer.html
	 */
	@RequestMapping("/customerHisCredit")
	public String customerHisCreditView(Model model, HttpSession session) {
		model.addAttribute("max_export_num", ConfigUtils.max_export_excel_num);
		UserSession user=getUserFromSession(session);
		model.addAttribute("menus", AgentUtils.hasMenuRight(user, "我的授信-wdsx", "客户授信-khsx","客户历史授信-khlssx","销售财务-xscw","销售历史财务-xslscw","销售授信账单-xssxzd","客户授信账单-khsxzd"));

		if(AgentUtils.isSaleRole(user) && Objects.equals(user.getDataAuthority(), DataAuthority.仅看自己数据.getValue())){
			model.addAttribute("saleIdforOne", user.getId());
		}
		return "finance/sell/customerHisCredit";
	}

	/**
	 * 客户历史授信查询分页数据
	 *
	 * @return
	 */
	@RequestMapping("/customerHisCredit/list")
	@ResponseBody
	public PageContainer customerHisCreditList(@RequestParam Map<String,String> params,HttpSession session) {
		params.put("pageRowCount", params.get("rows"));
		params.put("currentPage", params.get("page"));
		StringBuilder parstr = new StringBuilder();
		UserSession user=getUserFromSession(session);
		if(StringUtils.isNotBlank(params.get("besales"))){
			params.put("besales","'"+String.valueOf(params.get("besales"))+"'");
		}else{
			if(AgentUtils.isFinacorRole(user)){
				params.put("besales", null);
			}else {
				if(AgentUtils.isSaleRole(user) && Objects.equals(user.getDataAuthority(), DataAuthority.所在部门及下级部门.getValue()) ){
					// 销售总监查询自己及下属数据权限判断
					DataAuthorityCondition auth=AgentUtils.getDataAuthorityCondition(user.getId(),true,false);
					if(auth.getIds().size()>0){
						for(int i =0;i<auth.getIds().size();i++){
							if(auth.getIds().size()>1){
								if(i==0&&i!=auth.getIds().size()-1){
									parstr = parstr.append("'").append(auth.getIds().get(i)).append("'").append(",");
								}else if(i==auth.getIds().size()-1){
									parstr =  parstr.append("'").append(auth.getIds().get(i)).append("'");
								}else{
									parstr = parstr.append("'").append(auth.getIds().get(i)).append("'").append(",");
								}
							}else{
								parstr = parstr.append("'").append(auth.getIds().get(i)).append("'");
							}
						}
						params.put("besales", parstr.toString());
					}
					//params.put("belongSales",auth.getIds());
				}else if(AgentUtils.isSaleRole(user)){
					params.put("besales","'"+String.valueOf(user.getId())+"'");
				}
			}
		}
		JsmsPage jsmsPage = PageConvertUtil.paramToPage(params);
		jsmsPage.setParams(params);
		JsmsPage queryPage = jsmsAgentBalanceBillService.queryHisCreditList(jsmsPage);
		List<JsmsAgentBalanceBillVO> list = new ArrayList<>();
		JsmsAgentInfo jsmsAgentInfo = new JsmsAgentInfo();
		if(queryPage.getData().size()>0){
			for (Object temp : queryPage.getData()) {
				JsmsAgentBalanceBillVO jsmsAgentBalanceBillVO = new JsmsAgentBalanceBillVO();
				BeanUtils.copyProperties(temp , jsmsAgentBalanceBillVO);
				if(jsmsAgentBalanceBillVO!=null&&!jsmsAgentBalanceBillVO.getAgentId().equals("")){
					jsmsAgentInfo = jsmsAgentInfoService.getByAgentId(jsmsAgentBalanceBillVO.getAgentId());
					if(jsmsAgentInfo!=null){
						jsmsAgentBalanceBillVO.setAgentName(jsmsAgentInfo.getAgentName());
						jsmsAgentBalanceBillVO.setAgentType(jsmsAgentInfo.getAgentType());
						if(jsmsAgentInfo.getAgentType().intValue()==AgentType.品牌代理商.getValue().intValue()){
							jsmsAgentBalanceBillVO.setAgentTypeStr("品牌代理商");
						}else if(jsmsAgentInfo.getAgentType().intValue()==AgentType.OEM代理商.getValue().intValue()){
							jsmsAgentBalanceBillVO.setAgentTypeStr("OEM代理商");
						}
						jsmsAgentBalanceBillVO.setStatus(jsmsAgentInfo.getStatus());
						if(jsmsAgentInfo.getStatus().equals(AgentStatus.注册完成.getValue())){
							jsmsAgentBalanceBillVO.setStatusStr("已启用");
						}else if(jsmsAgentInfo.getStatus().equals(AgentStatus.冻结.getValue())){
							jsmsAgentBalanceBillVO.setStatusStr("已冻结");
						}else if(jsmsAgentInfo.getStatus().equals(AgentStatus.注销.getValue())){
							jsmsAgentBalanceBillVO.setStatusStr("已注销");
						}else if(jsmsAgentInfo.getStatus().equals(AgentStatus.锁定.getValue())){
							jsmsAgentBalanceBillVO.setStatusStr("已锁定");
						}
						if(jsmsAgentInfo.getAdminId()!=null){
							JsmsUser jsmsUser = jsmsUserService.getById2(jsmsAgentInfo.getAdminId());
							if(jsmsUser!=null){
								jsmsAgentBalanceBillVO.setMobile(jsmsUser.getMobile());
								jsmsAgentBalanceBillVO.setEmail(jsmsUser.getEmail());
							}
						}
						if(jsmsAgentInfo.getBelongSale()!=null){
							JsmsUser jsmsUser1 = jsmsUserService.getById2(jsmsAgentInfo.getBelongSale());
							if(jsmsUser1!=null){
								jsmsAgentBalanceBillVO.setRealName(jsmsUser1.getRealname());
							}
						}
						jsmsAgentBalanceBillVO.setBalanceStr(jsmsAgentBalanceBillVO.getBalance().toString());
						jsmsAgentBalanceBillVO.setCreditBalanceStr(jsmsAgentBalanceBillVO.getCreditBalance().toString());
						jsmsAgentBalanceBillVO.setHistoryPaymentStr(jsmsAgentBalanceBillVO.getHistoryPayment().toString());
						jsmsAgentBalanceBillVO.setCurrentCreditStr(jsmsAgentBalanceBillVO.getCurrentCredit().toString());
						jsmsAgentBalanceBillVO.setNoBackPaymentStr(jsmsAgentBalanceBillVO.getNoBackPayment().toString());
					}
				}
				list.add(jsmsAgentBalanceBillVO);
			}
		}
		queryPage.setData(list);
		PageContainer page  = PageConvertUtil.pageToContainer(queryPage);
		return page;
	}
	//历史授信合计
	@RequestMapping("/customerHisCredit/total")
	@ResponseBody
	public ResultVO list(@RequestParam Map<String,String> params,HttpSession session) {
		StringBuilder parstr = new StringBuilder();
		UserSession user=getUserFromSession(session);
		if(StringUtils.isNotBlank(params.get("besales"))){
			params.put("besales","'"+String.valueOf(params.get("besales"))+"'");
		}else{
			if(AgentUtils.isFinacorRole(user)){
				params.put("besales", null);
			}else {
				if(AgentUtils.isSaleRole(user) && Objects.equals(user.getDataAuthority(), DataAuthority.所在部门及下级部门.getValue()) ){
					// 销售总监查询自己及下属数据权限判断
					DataAuthorityCondition auth=AgentUtils.getDataAuthorityCondition(user.getId(),true,false);
					if(auth.getIds().size()>0){
						for(int i =0;i<auth.getIds().size();i++){
							if(auth.getIds().size()>1){
								if(i==0&&i!=auth.getIds().size()-1){
									parstr = parstr.append("'").append(auth.getIds().get(i)).append("'").append(",");
								}else if(i==auth.getIds().size()-1){
									parstr =  parstr.append("'").append(auth.getIds().get(i)).append("'");
								}else{
									parstr = parstr.append("'").append(auth.getIds().get(i)).append("'").append(",");
								}
							}else{
								parstr = parstr.append("'").append(auth.getIds().get(i)).append("'");
							}
						}
						params.put("besales", parstr.toString());
					}
					//params.put("belongSales",auth.getIds());
				}else if(AgentUtils.isSaleRole(user)){
					params.put("besales","'"+String.valueOf(user.getId())+"'");
				}
			}
		}
		ResultVO resultVO = ResultVO.failure();
		JsmsAgentBalanceBill jsmsAgentBalanceBill = jsmsAgentBalanceBillService.total(params);
		Map data = new HashMap();
		if(jsmsAgentBalanceBill!=null){
			data.put("credit_balance",jsmsAgentBalanceBill.getCreditBalance());
			data.put("history_payment",jsmsAgentBalanceBill.getHistoryPayment());
			data.put("no_back_payment",jsmsAgentBalanceBill.getNoBackPayment());
			data.put("balance",jsmsAgentBalanceBill.getBalance());
			data.put("current_credit",jsmsAgentBalanceBill.getCurrentCredit());
		}else{
			data.put("credit_balance","0.0000");
			data.put("history_payment","0.0000");
			data.put("no_back_payment","0.0000");
			data.put("balance","0.0000");
			data.put("current_credit","0.0000");
		}
		resultVO.setSuccess(true);
		resultVO.setData(data);
		return resultVO;
	}

	/**
	 * 历史授信数据导出
	 *@param param
	 * @param response
	 */
	@RequestMapping("/customerHisCredit/export")
	public void customerHisCreditExport(HttpSession session,@RequestParam Map<String,String> param,  HttpServletResponse response) {
		String filePath = ConfigUtils.save_path + "/客户历史授信信息_" + DateTime.now().toString("yyyyMMddHHmmss") + ".xls";
		Map<String,Object> params=new HashedMap();
		Map<String, Object> jsmsAgentBalanceBillVOMap = new HashMap<>();
		List<Map<String, Object>> exportList = new ArrayList<>();
		StringBuilder parstr = new StringBuilder();
		UserSession user=getUserFromSession(session);
		if(StringUtils.isNotBlank(param.get("besales"))){
			param.put("besales","'"+String.valueOf(param.get("besales"))+"'");
		}else {
			if (AgentUtils.isFinacorRole(user)) {
				param.put("besales", null);
			} else {
				if (AgentUtils.isSaleRole(user) && Objects.equals(user.getDataAuthority(), DataAuthority.所在部门及下级部门.getValue())) {
					// 销售总监查询自己及下属数据权限判断
					DataAuthorityCondition auth = AgentUtils.getDataAuthorityCondition(user.getId(), true, false);
					if (auth.getIds().size() > 0) {
						for (int i = 0; i < auth.getIds().size(); i++) {
							if (auth.getIds().size() > 1) {
								if (i == 0 && i != auth.getIds().size() - 1) {
									parstr = parstr.append("'").append(auth.getIds().get(i)).append("'").append(",");
								} else if (i == auth.getIds().size() - 1) {
									parstr = parstr.append("'").append(auth.getIds().get(i)).append("'");
								} else {
									parstr = parstr.append("'").append(auth.getIds().get(i)).append("'").append(",");
								}
							} else {
								parstr = parstr.append("'").append(auth.getIds().get(i)).append("'");
							}
						}
						param.put("besales", parstr.toString());
					}
					//params.put("belongSales",auth.getIds());
				} else if (AgentUtils.isSaleRole(user)) {
					param.put("besales", "'" + String.valueOf(user.getId()) + "'");
				}
			}
		}
		params.putAll(param);
		JsmsPage jpage=new JsmsPage();
		jpage.setParams(params);
		jpage.setRows(Integer.valueOf(ConfigUtils.max_export_excel_num));
		Excel excel = new Excel();
		excel.setFilePath(filePath);
		excel.setTitle("客户历史授信信息");
		excel.addHeader(20, "代理商ID", "agentId");
		excel.addHeader(20, "代理商名称", "agentName");
		excel.addHeader(20, "代理商类型", "agentTypeStr");
		excel.addHeader(20, "代理商状态", "statusStr");
		excel.addHeader(20, "邮箱", "email");
		excel.addHeader(20, "手机", "mobile");
		excel.addHeader(20, "归属销售", "realName");
		excel.addHeader(20, "余额（元）", "balance");
		excel.addHeader(20, "历史授信额度（元）", "creditBalance");
		excel.addHeader(20, "历史授信回款额度（元）", "historyPayment");
		excel.addHeader(20, "授信余额（元）", "currentCredit");
		excel.addHeader(20, "未回款额度（元）", "noBackPayment");
		if(jpage.getRows()>0){
			JsmsPage queryPage = jsmsAgentBalanceBillService.queryHisCreditList(jpage);
			List<JsmsAgentBalanceBillVO> list = new ArrayList<>();
			JsmsAgentInfo jsmsAgentInfo = new JsmsAgentInfo();
			if(queryPage.getData().size()>0){
				for (Object temp : queryPage.getData()) {
					JsmsAgentBalanceBillVO jsmsAgentBalanceBillVO = new JsmsAgentBalanceBillVO();
					BeanUtils.copyProperties(temp , jsmsAgentBalanceBillVO);
					if(jsmsAgentBalanceBillVO!=null&&!jsmsAgentBalanceBillVO.getAgentId().equals("")){
						jsmsAgentInfo = jsmsAgentInfoService.getByAgentId(jsmsAgentBalanceBillVO.getAgentId());
						if(jsmsAgentInfo!=null){
							jsmsAgentBalanceBillVO.setAgentName(jsmsAgentInfo.getAgentName());
							jsmsAgentBalanceBillVO.setAgentType(jsmsAgentInfo.getAgentType());
							if(jsmsAgentInfo.getAgentType().intValue()==AgentType.销售代理商.getValue().intValue()){
								jsmsAgentBalanceBillVO.setAgentTypeStr("销售代理商");
							}else if(jsmsAgentInfo.getAgentType().intValue()==AgentType.品牌代理商.getValue().intValue()){
								jsmsAgentBalanceBillVO.setAgentTypeStr("品牌代理商");
							}else if(jsmsAgentInfo.getAgentType().intValue()==AgentType.资源合作商.getValue().intValue()){
								jsmsAgentBalanceBillVO.setAgentTypeStr("资源合作商");
							}else if(jsmsAgentInfo.getAgentType().intValue()==AgentType.代理商和资源合作.getValue().intValue()){
								jsmsAgentBalanceBillVO.setAgentTypeStr("代理商和资源合作");
							}else if(jsmsAgentInfo.getAgentType().intValue()==AgentType.OEM代理商.getValue().intValue()){
								jsmsAgentBalanceBillVO.setAgentTypeStr("OEM代理商");
							}
							jsmsAgentBalanceBillVO.setStatus(jsmsAgentInfo.getStatus());
							if(jsmsAgentInfo.getStatus().equals(AgentStatus.注册完成.getValue())){
								jsmsAgentBalanceBillVO.setStatusStr("已启用");
							}else if(jsmsAgentInfo.getStatus().equals(AgentStatus.冻结.getValue())){
								jsmsAgentBalanceBillVO.setStatusStr("已冻结");
							}else if(jsmsAgentInfo.getStatus().equals(AgentStatus.注销.getValue())){
								jsmsAgentBalanceBillVO.setStatusStr("已注销");
							}else if(jsmsAgentInfo.getStatus().equals(AgentStatus.锁定.getValue())){
								jsmsAgentBalanceBillVO.setStatusStr("已锁定");
							}
							/*jsmsAgentBalanceBillVO.setStatus(jsmsAgentInfo.getStatus());
							if(jsmsAgentInfo.getStatus().equals("1")){
								jsmsAgentBalanceBillVO.setStatusStr("已启用");
							}else if(jsmsAgentInfo.getStatus().equals("5")){
								jsmsAgentBalanceBillVO.setStatusStr("已冻结");
							}else if(jsmsAgentInfo.getStatus().equals("6")){
								jsmsAgentBalanceBillVO.setStatusStr("已注销");
							}*/
							if(jsmsAgentInfo.getAdminId()!=null){
								JsmsUser jsmsUser = jsmsUserService.getById2(jsmsAgentInfo.getAdminId());
								if(jsmsUser!=null){
									jsmsAgentBalanceBillVO.setMobile(jsmsUser.getMobile());
									jsmsAgentBalanceBillVO.setEmail(jsmsUser.getEmail());
								}
							}
							if(jsmsAgentInfo.getBelongSale()!=null){
								JsmsUser jsmsUser1 = jsmsUserService.getById2(jsmsAgentInfo.getBelongSale());
								if(jsmsUser1!=null){
									jsmsAgentBalanceBillVO.setRealName(jsmsUser1.getRealname());
								}
							}
						}
					}
					jsmsAgentBalanceBillVOMap = BeanUtil.beanToMap(jsmsAgentBalanceBillVO, true);
					exportList.add(jsmsAgentBalanceBillVOMap);
				}
			}
		}else{
			response.setContentType("text/plain;charset=UTF-8");
			try {
				response.getWriter().write("导出Excel文件失败，请联系管理员");
				response.getWriter().flush();
			} catch (IOException e) {
				logger.error("导出Excel文件失败", e);
			}
		}
		JsmsAgentBalanceBill jsmsAgentBalanceBill = jsmsAgentBalanceBillService.total(param);
		Map data = new HashMap();
		data.put("agentId","-");
		data.put("agentName","-");
		data.put("agentTypeStr","-");
		data.put("statusStr","-");
		data.put("email","-");
		data.put("mobile","-");
		if(jsmsAgentBalanceBill!=null){
			data.put("creditBalance",jsmsAgentBalanceBill.getCreditBalance());
			data.put("historyPayment",jsmsAgentBalanceBill.getHistoryPayment());
			data.put("noBackPayment",jsmsAgentBalanceBill.getNoBackPayment());
			data.put("balance",jsmsAgentBalanceBill.getBalance());
			data.put("currentCredit",jsmsAgentBalanceBill.getCurrentCredit());
		}else{
			data.put("creditBalance","0.0000");
			data.put("historyPayment","0.0000");
			data.put("noBackPayment","0.0000");
			data.put("balance","0.0000");
			data.put("currentCredit","0.0000");
		}
		data.put("realName","合计");
		exportList.add(data);
		excel.setDataList(exportList);
		if (ExcelUtils.exportExcel(excel)) {
			FileUtils.download(filePath,response);
			FileUtils.delete(filePath);
		} else {
			response.setContentType("text/plain;charset=UTF-8");
			try {
				response.getWriter().write("导出Excel文件失败，请联系管理员");
				response.getWriter().flush();
			} catch (IOException e) {
				logger.error("导出Excel文件失败", e);
			}
		}
	}

	/**
	 * 销售财务
	 * \finance\sell\saleFinance.html
	 */
	@RequestMapping("/saleFinance")
	public String saleFinanceView(Model model, HttpSession session) {
		model.addAttribute("max_export_num", ConfigUtils.max_export_excel_num);
//		UserSession user = getUserFromSession(session);
//		model.addAttribute("menus", AgentUtils.hasMenuRight(user, "代理商财务-dlscw", "历史数据-lssj"));
		UserSession user=getUserFromSession(session);
		model.addAttribute("menus", AgentUtils.hasMenuRight(user, "我的授信-wdsx", "客户授信-khsx","客户历史授信-khlssx","销售财务-xscw","销售历史财务-xslscw","销售授信账单-xssxzd","客户授信账单-khsxzd"));
		//sell\myCredit.html
		return "finance/sell/saleFinance";
	}

	/**
	 * 销售财务查询分页数据
	 *
	 * @param rows           行数
	 * @param page           当前查询页码
	 * @return
	 */
	@ApiOperation(value = "查询销售财务列表数据", notes = "查询销售财务列表数据", tags = "财务管理-销售财务", response = PageContainer.class)
	@ApiImplicitParams({ @ApiImplicitParam(name = "page", value = "页码", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "rows", value = "行数", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "operatorCode", value = "运营商类型", dataType = "String", paramType = "query"),
			@ApiImplicitParam(name = "condition", value = "订单号/产品代码/客户ID/代理商ID", dataType = "String", paramType = "query")})
	@RequestMapping("/saleFinance/list")
	@ResponseBody
//    public PageContainer list(String rows, String page, String condition, String status, String startDateTime, String endDateTime) {
	public R saleFinanceList(HttpSession session,String rows, String page,String condition) {

		JsmsPage jpage=new JsmsPage();
		Map<String, String> params = new HashMap<>();
		if(StringUtils.isNotBlank(condition)){
			params.put("condition",condition);
		}

		//权限判断
		UserSession user=getUserFromSession(session);
//		if(isSaleRole(user)){
//			params.put("saleId",user.getId());
//		}else {
//			return	R.error("非销售人员,无法查询流水记录");
//		}




		jpage.setParams(params);

		if(StringUtils.isNotBlank(rows)){
			jpage.setRows(Integer.valueOf(rows));
		}
		if(StringUtils.isNotBlank(page)){
			jpage.setPage(Integer.valueOf(page));
		}

		jpage.setOrderByClause("sa.sale_id asc");

		jpage=jsmsSaleCreditAccountService.SaleFinQueryList(jpage);


		PageContainer container = PageConvertUtil.pageToContainer(jpage);

		return R.ok("获取销售财务列表成功",container);
//        return orderInfoService.query(params);
	}

	@RequestMapping("/saleFinance/total")
	@ResponseBody
	public R saleFinanceTotalAmount(HttpSession session,@RequestParam Map<String,String> params){

		Map<String,Object> params1=new HashedMap();
		params1.putAll(params);

		if(StringUtils.isNotBlank(params.get("condition"))){
			params1.put("condition",params.get("condition"));
		}

		//权限判断
		UserSession user=getUserFromSession(session);
//		if(StringUtils.isNotBlank(params.get("besales"))){
//			params1.put("belongSale",params.get("besales"));
//		}else if(isSaleRole(user)){
//			params1.put("belongSale",user.getId());
//		}


		Map<String,Object> sum=new HashedMap();
		sum.put("sumFi",BigDecimal.ZERO);
		sum.put("sumCu",BigDecimal.ZERO);
		sum.put("sumHi",BigDecimal.ZERO);
		sum.put("sumAg",BigDecimal.ZERO);
		sum.put("sumNo",BigDecimal.ZERO);

		sum=jsmsSaleCreditAccountService.querySumBlance(params1);

		return  R.ok("请求计算销售授信账户总计成功！",sum);
	}

	/**
	 * 销售财务数据导出
	 *@param param
	 * @param response
	 */
	@RequestMapping("/saleFinance/export")
	public void saleFinanceExport(HttpSession session,@RequestParam Map<String,String> param,  HttpServletResponse response) {
		String filePath = ConfigUtils.save_path + "/销售授信信息_" + DateTime.now().toString("yyyyMMddHHmmss") + ".xls";
		Map<String,Object> params=new HashedMap();
		params.putAll(param);
		StringBuffer buffer = new StringBuffer();
		buffer.append("查询条件 -> ");
		if (StringUtils.isNotBlank(param.get("condition"))) {
//            params.put("condition", params.get("condition"));
			buffer.append("销售ID/销售名称/邮箱/手机：")
					.append(params.get("condition"))
					.append(";");
			params.put("condition",params.get("condition"));
		}



//		UserSession user=getUserFromSession(session);
//		if(isSaleRole(user)){
//			params.put("saleId",user.getId());
//		}else {
//			logger.info("非销售人员,无法查询流水记录!");
//			return;
//		}

		JsmsPage jpage=new JsmsPage();
		jpage.setParams(params);
		jpage.setRows(Integer.valueOf(ConfigUtils.max_export_excel_num));
		jpage.setOrderByClause("sa.sale_id asc");
		jpage=jsmsSaleCreditAccountService.SaleFinQueryList(jpage);


		Excel excel = new Excel();
		excel.setFilePath(filePath);
		excel.setTitle("客户授信");
		excel.addRemark(buffer.toString());
		excel.addHeader(20, "销售ID", "saleId");
		excel.addHeader(20, "销售名称", "realName");
		excel.addHeader(20, "邮箱", "email");
		excel.addHeader(20, "手机", "mobile");
		excel.addHeader(20, "历史授信额度(元)", "financialHistoryCredit");
		excel.addHeader(20, "销售历史售出额度(元)", "saleHistoryCredit");
		excel.addHeader(20, "历史回款额度(元)", "agentHistoryPayment");
		excel.addHeader(20, "授信余额(元)", "currentCredit");
		excel.addHeader(20, "未回款额度(元)", "noBackPayment");


		Map<String,Object> params1=new HashedMap();
		params1.putAll(params);

		Map<String,Object> sum=new HashedMap();
		sum.put("sumFi",BigDecimal.ZERO);
		sum.put("sumCu",BigDecimal.ZERO);
		sum.put("sumHi",BigDecimal.ZERO);
		sum.put("sumAg",BigDecimal.ZERO);
		sum.put("sumNo",BigDecimal.ZERO);

		sum=jsmsSaleCreditAccountService.querySumBlance(params1);

		Map<String,Object> totalDate = new HashedMap();
		totalDate.put("saleId", "");
		totalDate.put("realName", "-");
		totalDate.put("email", "-");
		totalDate.put("mobile", "合计");
		totalDate.put("financialHistoryCredit", sum.get("sumFi"));
		totalDate.put("saleHistoryCredit", sum.get("sumHi"));
		totalDate.put("agentHistoryPayment",sum.get("sumAg"));
		totalDate.put("currentCredit",sum.get("sumCu"));
		totalDate.put("noBackPayment",sum.get("sumNo"));
		List<Map<String,Object>> data=BeanUtil.ListbeanToMap(jpage.getData(),false);
		data.add(totalDate);



		excel.setDataList(data);

		if (ExcelUtils.exportExcel(excel,true)) {
			FileUtils.download(response, filePath);
			FileUtils.delete(filePath);
		} else {
			response.setContentType("text/plain;charset=UTF-8");
			try {
				response.getWriter().write("导出Excel文件失败，请联系管理员");
				response.getWriter().flush();
			} catch (IOException e) {
				logger.error("导出Excel文件失败", e);
			}
		}
	}







	/**
	 * 财务授信销售数据
	 * @param session
	 * @param params
	 * @return
	 */
	@RequestMapping("/saleFinance/toOperation")
	@ResponseBody
	public R sfOperationView(HttpSession session,@RequestParam Map<String,String> params){

		Map<String,Object> params1=new HashedMap();
		params1.putAll(params);

		if(StringUtils.isNotBlank(params.get("saleId"))){
			params1.put("saleId",params.get("saleId"));
		}
		Map<String,Object> data=new HashedMap();

		JsmsUser saUser=jsmsUserService.getById2(Long.valueOf(params.get("saleId")));
		if(saUser==null){
			return	R.error("销售不存在！");
		}else {
			data.put("saleId",saUser.getId());
			data.put("realName",saUser.getRealname());

			JsmsSaleCreditAccount saleAcc=jsmsSaleCreditAccountService.getBySaleId(saUser.getId());
			//销售信息相关
			if(saleAcc==null){
				return	R.error("销售未关联授信信息,请联系管理员！");
			}else {
				data.put("financialHistoryCredit",saleAcc.getFinancialHistoryCredit());
				data.put("currentCredit",saleAcc.getCurrentCredit());
				//	data.put("saleHistoryCredit",saleAcc.getSaleHistoryCredit());
				data.put("agentHistoryPayment",saleAcc.getAgentHistoryPayment());
				data.put("noBackPayment",saleAcc.getNoBackPayment());
			}

		}


		return  R.ok("请求财务授信销售数据成功！",data);
	}

	@RequestMapping("/saleFinance/operationOfCredit")
	@ResponseBody
	public R sfOperationOfCredit(HttpSession session,@RequestParam Map<String,String> params){

		Map<String,Object> data=new HashedMap();
		UserSession user=getUserFromSession(session);
		//校验授信余额变化

		Integer opType=Integer.valueOf(params.get("operateType"));
		if(Objects.equals(0,opType)){
			opType=BusinessType.财务给销售授信.getValue();
		}else if(Objects.equals(1 ,opType)){
			opType=BusinessType.财务降低销售授信.getValue();
		}else {
			return R.error("操作非法！");
		}



		data=this.sfCheckForCredit(params);
		if(Objects.equals("fail",data.get("status"))){

			return R.error(data.get("msg").toString());
		}else  if(Objects.equals(SysConstant.OVERDUE,data.get("status"))){
			R r=new R();
			r.setCode(SysConstant.OVERDUE_CODE);
			r.setMsg(data.get("msg").toString());
			return r;
		}else {
			//销售信息

			R r1=jsmsSaleCreditService.creditForSale(user.getId(),opType,null,Long.valueOf(params.get("saleId")),new BigDecimal(params.get("amount")),params.get("remark"));
			if(Objects.equals(r1.getCode(), SysConstant.FAIL_CODE)){
				return  r1;
			}
			if(Objects.equals("0",params.get("operateType"))){
				data.put("msg","增加销售授信操作成功");
			}else if(Objects.equals("1",params.get("operateType"))) {
				data.put("msg","降低销售授信操作成功");
			}
		}


		return  R.ok("销售授信操作成功！",data);
	}


	/**
	 * 校验授信余额是否变化
	 * @param params
	 * @return
	 */
	private  Map<String,Object> sfCheckForCredit(Map<String,String> params){
		Map<String,Object> results=new HashedMap();
		if(BigDecimal.ZERO.compareTo(new BigDecimal(params.get("amount")))==0){
			results.put("status", SysConstant.FAIL);
			results.put("msg", "操作金额不能为0");
			return results;

		}
		JsmsUser saler=jsmsUserService.getById2(Long.valueOf(params.get("saleId")));
		if(saler==null){
			results.put("status","fail");
			results.put("msg","销售不存在！");
			return	results;
		}else {
			JsmsSaleCreditAccount saleAcc=jsmsSaleCreditAccountService.getBySaleId(saler.getId());
			if(saleAcc==null){
				results.put("status",SysConstant.FAIL);
				results.put("msg","销售账户信息不存在！");
				return	results;
			}else {
				if(saleAcc.getCurrentCredit().compareTo(new BigDecimal(params.get("currentCredit")))!=0){
					results.put("status","overdue");
					results.put("code",405);
					results.put("msg","当前数据已过期,请重新授信！");
					return	results;
				}else if(Objects.equals("1",params.get("operateType")) && saleAcc.getCurrentCredit().compareTo(new BigDecimal(params.get("amount")))<0){
					results.put("status",SysConstant.FAIL);
					results.put("msg","销售授信余额不足！");
					return	results;
				}

			}
		}
		results.put("status","success");
		results.put("msg","校验成功！");
		return results;
	}

	/**
	 * 财务授信销售变化
	 * @param user
	 * @param params
	 * @return
	 *//*
	private  Map<String,Object> FinCreditForSale(UserSession user,Map<String,String> params){
		//,String agentId,String amount,String operateType
		Map<String,Object> results=new HashedMap();

		JsmsUser saleUser=jsmsUserService.getById2(Long.valueOf(params.get("saleId")));
		if(saleUser==null){
			results.put("status","fail");
			results.put("msg","销售账户不存在！");
			return	results;
		}else {
			JsmsSaleCreditAccount saleAccount=jsmsSaleCreditAccountService.getBySaleId(saleUser.getId());
			if(saleAccount==null){
				results.put("status","fail");
				results.put("msg","销售账户信息不存在！");
				return	results;
			}else {

				JsmsSaleCreditBill bill=new JsmsSaleCreditBill();
				bill.setSaleId(saleUser.getId());
				bill.setAmount(new BigDecimal(params.get("amount")));
				bill.setObjectId(saleUser.getId());
				bill.setObjectType(ObjectType.销售.getValue());
				bill.setFinancialHistoryCredit(saleAccount.getFinancialHistoryCredit());
				bill.setCurrentCredit(saleAccount.getCurrentCredit());
				bill.setSaleHistoryCredit(saleAccount.getSaleHistoryCredit());
				bill.setAgentHistoryPayment(saleAccount.getAgentHistoryPayment());
				bill.setNoBackPayment(saleAccount.getNoBackPayment());
				bill.setAdminId(user.getId());
				bill.setCreateTime(new Date());
				bill.setRemark(params.get("remark"));


				JsmsSaleCreditAccount saleAcc=new JsmsSaleCreditAccount();
				saleAcc.setSaleId(saleAccount.getSaleId());
				if(Objects.equals("0",params.get("operateType"))){
					//添加授信
					bill.setBusinessType(BusinessType.财务给销售授信.getValue());
					bill.setFinancialType(FinancialType.入账.getValue());

					saleAcc.setCurrentCredit(saleAccount.getCurrentCredit().add(new BigDecimal(params.get("amount"))));
					saleAcc.setFinancialHistoryCredit(saleAccount.getFinancialHistoryCredit().add(new BigDecimal(params.get("amount"))));

				}else if(Objects.equals("1",params.get("operateType"))){
					//降低授信
					bill.setBusinessType(BusinessType.财务降低销售授信.getValue());
					bill.setFinancialType(FinancialType.出账.getValue());
					saleAcc.setCurrentCredit(saleAccount.getCurrentCredit().subtract(new BigDecimal(params.get("amount"))));
					saleAcc.setFinancialHistoryCredit(saleAccount.getFinancialHistoryCredit().subtract(new BigDecimal(params.get("amount"))));
				}
				//生成账单信息
				int abi=jsmsSaleCreditBillService.insert(bill);
				if(abi>0){
					logger.info("生成销售账单信息成功,账单bill={}", JsonUtils.toJson(bill));
					//更新账户信息

					int aup=jsmsSaleCreditAccountService.updateSelective(saleAcc);
					if(aup>0){
						logger.info("更新销售账户信息成功,saleAccount={}", JsonUtils.toJson(saleAcc));
						results.put("status","success");
						results.put("msg","更新销售账户信息成功！");

					}else {
						results.put("status","fail");
						results.put("msg","更新销售账户信息失败!");
						logger.error("更新销售账户信息失败,saleAccount={}", JsonUtils.toJson(saleAcc));
						return	results;
					}


				}else {
					results.put("status","fail");
					results.put("msg","生成销售账单信息入库失败!");
					logger.error("生成销售账单信息入库失败,账单bill={}", JsonUtils.toJson(bill));
					return	results;
				}


			}
		}

		results.put("status","success");
		results.put("msg","销售账单信息更新入库成功!");

		return results;
	}
*/




	/**
	 * 销售数据
	 * \finance\sell\salehisFinance.html
	 */
	@RequestMapping("/salehisFinance")
	public String salehisFinanceView(Model model, HttpSession session) {
		model.addAttribute("max_export_num", ConfigUtils.max_export_excel_num);
		UserSession user=getUserFromSession(session);
		model.addAttribute("menus", AgentUtils.hasMenuRight(user, "我的授信-wdsx", "客户授信-khsx","客户历史授信-khlssx","销售财务-xscw","销售历史财务-xslscw","销售授信账单-xssxzd","客户授信账单-khsxzd"));
		return "finance/sell/salehisFinance";
	}

	/**
	 * 销售历史数据查询分页数据
	 *
	 * @return
	 */
	@ApiOperation(value = "查询销售历史授信列表数据", notes = "查询销售历史授信列表数据", tags = "财务管理-销售财务", response = PageContainer.class)
	@RequestMapping("/salehisFinance/list")
	@ResponseBody
	public PageContainer hisDataList(@RequestParam Map<String,String> params) {
		params.put("pageRowCount", params.get("rows"));
		params.put("currentPage", params.get("page"));
		JsmsPage jsmsPage = PageConvertUtil.paramToPage(params);
		jsmsPage.setParams(params);
		JsmsPage queryPage = jsmsSaleCreditBillService.queryHisCreditList(jsmsPage);
		List<JsmsSaleCreditBillVO> list = new ArrayList<>();
		if(queryPage.getData().size()>0){
			for (Object temp : queryPage.getData()) {
				JsmsSaleCreditBillVO jsmsSaleCreditBillVO = new JsmsSaleCreditBillVO();
				BeanUtils.copyProperties(temp , jsmsSaleCreditBillVO);
				if(jsmsSaleCreditBillVO!=null&&jsmsSaleCreditBillVO.getSaleId()!=null){
					JsmsUser jsmsUser = jsmsUserService.getById2(jsmsSaleCreditBillVO.getSaleId());
					if(jsmsUser!=null){
						jsmsSaleCreditBillVO.setRealName(jsmsUser.getRealname());
						jsmsSaleCreditBillVO.setMobile(jsmsUser.getMobile());
						jsmsSaleCreditBillVO.setEmail(jsmsUser.getEmail());
					}
					jsmsSaleCreditBillVO.setFinancialHistoryCreditStr(jsmsSaleCreditBillVO.getFinancialHistoryCredit().toString());
					jsmsSaleCreditBillVO.setSaleHistoryCreditStr(jsmsSaleCreditBillVO.getSaleHistoryCredit().toString());
					jsmsSaleCreditBillVO.setAgentHistoryPaymentStr(jsmsSaleCreditBillVO.getAgentHistoryPayment().toString());
					jsmsSaleCreditBillVO.setNoBackPaymentStr(jsmsSaleCreditBillVO.getNoBackPayment().toString());
					jsmsSaleCreditBillVO.setCurrentCreditStr(jsmsSaleCreditBillVO.getCurrentCredit().toString());
				}
				list.add(jsmsSaleCreditBillVO);
			}
		}
		queryPage.setData(list);
		PageContainer page  = PageConvertUtil.pageToContainer(queryPage);
		return page;
	}

	//销售授信合计
	@RequestMapping("/salehisFinance/total")
	@ResponseBody
	public ResultVO salehisFinanceList(@RequestParam Map<String,String> params) {
		ResultVO resultVO = ResultVO.failure();
		JsmsSaleCreditBill jsmsSaleCreditBill = jsmsSaleCreditBillService.total(params);
		Map data = new HashMap();
		if(jsmsSaleCreditBill!=null){
			data.put("financial_history_credit",jsmsSaleCreditBill.getFinancialHistoryCredit());
			data.put("current_credit",jsmsSaleCreditBill.getCurrentCredit());
			data.put("sale_history_credit",jsmsSaleCreditBill.getSaleHistoryCredit());
			data.put("agent_history_payment",jsmsSaleCreditBill.getAgentHistoryPayment());
			data.put("no_back_payment",jsmsSaleCreditBill.getNoBackPayment());
		}else{
			data.put("financial_history_credit","0.0000");
			data.put("current_credit","0.0000");
			data.put("sale_history_credit","0.0000");
			data.put("agent_history_payment","0.0000");
			data.put("no_back_payment","0.0000");
		}
		/*data.put("financial_history_credit",jsmsSaleCreditBill.getFinancialHistoryCredit());
		data.put("current_credit",jsmsSaleCreditBill.getCurrentCredit());
		data.put("sale_history_credit",jsmsSaleCreditBill.getSaleHistoryCredit());
		data.put("agent_history_payment",jsmsSaleCreditBill.getAgentHistoryPayment());
		data.put("no_back_payment",jsmsSaleCreditBill.getNoBackPayment());*/
		resultVO.setSuccess(true);
		resultVO.setData(data);
		return resultVO;
	}
	/**
	 * 历史授信数据导出
	 *@param param
	 * @param response
	 */
	@RequestMapping("/salehisFinance/export")
	public void salehisFinanceExport(HttpSession session,@RequestParam Map<String,String> param,  HttpServletResponse response) {
		String filePath = ConfigUtils.save_path + "/销售历史财务信息_" + DateTime.now().toString("yyyyMMddHHmmss") + ".xls";
		Map<String,Object> params=new HashedMap();
		Map<String, Object> jsmsSaleCreditBillVOMap = new HashMap<>();
		List<Map<String, Object>> exportList = new ArrayList<>();
		params.putAll(param);
		JsmsPage jpage=new JsmsPage();
		jpage.setParams(params);
		jpage.setRows(Integer.valueOf(ConfigUtils.max_export_excel_num));
		Excel excel = new Excel();
		excel.setFilePath(filePath);
		excel.setTitle("销售历史财务信息");
		excel.addHeader(20, "销售ID", "saleId");
		excel.addHeader(20, "销售名称", "realName");
		excel.addHeader(20, "邮箱", "email");
		excel.addHeader(20, "手机", "mobile");
		excel.addHeader(20, "历史授信额度(元)", "financialHistoryCredit");
		excel.addHeader(20, "销售历史授出额度(元)", "saleHistoryCredit");
		excel.addHeader(20, "历史回款额度(元)", "agentHistoryPayment");
		excel.addHeader(20, "授信余额(元)", "currentCredit");
		excel.addHeader(20, "未回款额度(元)", "noBackPayment");
		if(jpage.getRows()>0){
			JsmsPage queryPage = jsmsSaleCreditBillService.queryHisCreditList(jpage);
			List<JsmsSaleCreditBillVO> list = new ArrayList<>();
			if(queryPage.getData().size()>0){
				for (Object temp : queryPage.getData()) {
					JsmsSaleCreditBillVO jsmsSaleCreditBillVO = new JsmsSaleCreditBillVO();
					BeanUtils.copyProperties(temp , jsmsSaleCreditBillVO);
					if(jsmsSaleCreditBillVO!=null&&jsmsSaleCreditBillVO.getSaleId()!=null){
						JsmsUser jsmsUser = jsmsUserService.getById2(jsmsSaleCreditBillVO.getSaleId());
						if(jsmsUser!=null){
							jsmsSaleCreditBillVO.setRealName(jsmsUser.getRealname());
							jsmsSaleCreditBillVO.setMobile(jsmsUser.getMobile());
							jsmsSaleCreditBillVO.setEmail(jsmsUser.getEmail());
						}
						jsmsSaleCreditBillVO.setFinancialHistoryCreditStr(jsmsSaleCreditBillVO.getFinancialHistoryCredit().toString());
						jsmsSaleCreditBillVO.setSaleHistoryCreditStr(jsmsSaleCreditBillVO.getSaleHistoryCredit().toString());
						jsmsSaleCreditBillVO.setAgentHistoryPaymentStr(jsmsSaleCreditBillVO.getAgentHistoryPayment().toString());
						jsmsSaleCreditBillVO.setNoBackPaymentStr(jsmsSaleCreditBillVO.getNoBackPayment().toString());
						jsmsSaleCreditBillVO.setCurrentCreditStr(jsmsSaleCreditBillVO.getCurrentCredit().toString());
					}
					jsmsSaleCreditBillVOMap = BeanUtil.beanToMap(jsmsSaleCreditBillVO, true);
					exportList.add(jsmsSaleCreditBillVOMap);
				}
			}
		}else{
			response.setContentType("text/plain;charset=UTF-8");
			try {
				response.getWriter().write("导出Excel文件失败，请联系管理员");
				response.getWriter().flush();
			} catch (IOException e) {
				logger.error("导出Excel文件失败", e);
			}
		}
		JsmsSaleCreditBill jsmsSaleCreditBill = jsmsSaleCreditBillService.total(param);
		Map data = new HashMap();
		data.put("agentId","-");
		data.put("realName","-");
		data.put("email","-");
		data.put("mobile","合计");
		if(jsmsSaleCreditBill!=null){
			data.put("financialHistoryCredit",jsmsSaleCreditBill.getFinancialHistoryCredit());
			data.put("saleHistoryCredit",jsmsSaleCreditBill.getSaleHistoryCredit());
			data.put("agentHistoryPayment",jsmsSaleCreditBill.getAgentHistoryPayment());
			data.put("currentCredit",jsmsSaleCreditBill.getCurrentCredit());
			data.put("noBackPayment",jsmsSaleCreditBill.getNoBackPayment());
		}else{
			data.put("financialHistoryCredit","0.0000");
			data.put("saleHistoryCredit","0.0000");
			data.put("agentHistoryPayment","0.0000");
			data.put("currentCredit","0.0000");
			data.put("noBackPayment","0.0000");
		}
		exportList.add(data);
		excel.setDataList(exportList);
		if (ExcelUtils.exportExcel(excel)) {
			FileUtils.download(filePath,response);
			FileUtils.delete(filePath);
		} else {
			response.setContentType("text/plain;charset=UTF-8");
			try {
				response.getWriter().write("导出Excel文件失败，请联系管理员");
				response.getWriter().flush();
			} catch (IOException e) {
				logger.error("导出Excel文件失败", e);
			}
		}
	}

	/**
	 * 销售授信账单
	 * finance\sell\saleCreditBill.html
	 */
	@RequestMapping("/saleCreditBill")
	public String saleCreditBillView(Model model, HttpSession session) {
		model.addAttribute("max_export_num", ConfigUtils.max_export_excel_num);
		UserSession user=getUserFromSession(session);
		model.addAttribute("menus", AgentUtils.hasMenuRight(user, "我的授信-wdsx", "客户授信-khsx","客户历史授信-khlssx","销售财务-xscw","销售历史财务-xslscw","销售授信账单-xssxzd","客户授信账单-khsxzd"));
		return "finance/sell/saleCreditBill";
	}
	/**
	 * 销售授信账单查询分页数据
	 *
	 * @return
	 */
	@ApiOperation(value = "查询销售授信账单列表数据", notes = "查询销售授信账单列表数据", tags = "财务管理-销售财务", response = PageContainer.class)
	@RequestMapping("/saleCreditBill/list")
	@ResponseBody
	public PageContainer saleCreditBillList(@RequestParam Map<String,String> params) {
		params.put("pageRowCount", params.get("rows"));
		params.put("currentPage", params.get("page"));
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
		JsmsPage jsmsPage = PageConvertUtil.paramToPage(params);
		jsmsPage.setParams(params);
		JsmsPage queryPage = jsmsSaleCreditBillService.queryCreditBillList(jsmsPage);
		List<JsmsSaleCreditBillVO> list = new ArrayList<>();
		if(queryPage.getData().size()>0){
			for (Object temp : queryPage.getData()) {
				JsmsSaleCreditBillVO jsmsSaleCreditBillVO = new JsmsSaleCreditBillVO();
				BeanUtils.copyProperties(temp , jsmsSaleCreditBillVO);
				if(jsmsSaleCreditBillVO!=null&&jsmsSaleCreditBillVO.getSaleId()!=null){
					JsmsUser jsmsUser = jsmsUserService.getById2(jsmsSaleCreditBillVO.getSaleId());
					if(jsmsUser!=null){
						jsmsSaleCreditBillVO.setRealName(jsmsUser.getRealname());
					}
					JsmsUser jsmsUser1 = jsmsUserService.getById2(jsmsSaleCreditBillVO.getAdminId());
					if(jsmsUser1!=null){
						jsmsSaleCreditBillVO.setAdminName(jsmsUser1.getRealname());
					}else{
						jsmsSaleCreditBillVO.setAdminName("系统");
					}
					if(jsmsSaleCreditBillVO.getFinancialType().equals(FinancialType.入账.getValue())){
						jsmsSaleCreditBillVO.setFinancialTypeStr("出账");
					}else{
						jsmsSaleCreditBillVO.setFinancialTypeStr("入账");
					}

					if(jsmsSaleCreditBillVO.getBusinessType().intValue()==BusinessType.财务给销售授信.getValue().intValue()){
						jsmsSaleCreditBillVO.setBusinessTypeStr("财务给销售授信");
					}else if(jsmsSaleCreditBillVO.getBusinessType().intValue()==BusinessType.财务降低销售授信.getValue().intValue()){
						jsmsSaleCreditBillVO.setBusinessTypeStr("财务降低销售授信");
					}
					jsmsSaleCreditBillVO.setCreateTimeStr(format.format(jsmsSaleCreditBillVO.getCreateTime()));
					jsmsSaleCreditBillVO.setAmountStr(jsmsSaleCreditBillVO.getAmount().toString());
				}
				list.add(jsmsSaleCreditBillVO);
			}
		}
		queryPage.setData(list);
		PageContainer page  = PageConvertUtil.pageToContainer(queryPage);
		return page;
	}
	//销售账单合计
	@RequestMapping("/saleCreditBill/total")
	@ResponseBody
	public ResultVO saleCreditBillTotal(@RequestParam Map<String,String> params) {
		ResultVO resultVO = ResultVO.failure();
		JsmsSaleCreditBill jsmsSaleCreditBill = jsmsSaleCreditBillService.totalCreditBill(params);
		Map data = new HashMap();
		if(jsmsSaleCreditBill!=null){
			data.put("amount",jsmsSaleCreditBill.getAmount());
		}else{
			data.put("amount","0.0000");
		}
		resultVO.setSuccess(true);
		resultVO.setData(data);
		return resultVO;
	}
	/**
	 * 销售授信数据导出
	 *@param param
	 * @param response
	 */
	@RequestMapping("/saleCreditBill/export")
	public void saleCreditBillExport(HttpSession session,@RequestParam Map<String,String> param,  HttpServletResponse response) {
		String filePath = ConfigUtils.save_path + "/销售授信账单信息_" + DateTime.now().toString("yyyyMMddHHmmss") + ".xls";
		Map<String,Object> params=new HashedMap();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
		Map<String, Object> jsmsSaleCreditBillVOMap = new HashMap<>();
		List<Map<String, Object>> exportList = new ArrayList<>();
		params.putAll(param);
		JsmsPage jpage=new JsmsPage();
		jpage.setParams(params);
		jpage.setRows(Integer.valueOf(ConfigUtils.max_export_excel_num));
		Excel excel = new Excel();
		excel.setFilePath(filePath);
		excel.setTitle("销售授信账单信息");
		excel.addHeader(20, "业务单号", "id");
		excel.addHeader(20, "业务类型", "businessTypeStr");
		excel.addHeader(20, "财务类型", "financialTypeStr");
		excel.addHeader(20, "账单金额(元)", "amount");
		excel.addHeader(20, "销售ID", "saleId");
		excel.addHeader(20, "销售名称", "realName");
		excel.addHeader(20, "操作者", "adminName");
		excel.addHeader(20, "备注", "remark");
		excel.addHeader(20, "操作时间", "createTimeStr");
		if(jpage.getRows()>0){
			JsmsPage queryPage = jsmsSaleCreditBillService.queryCreditBillList(jpage);
			List<JsmsSaleCreditBillVO> list = new ArrayList<>();
			if(queryPage.getData().size()>0){
				for (Object temp : queryPage.getData()) {
					JsmsSaleCreditBillVO jsmsSaleCreditBillVO = new JsmsSaleCreditBillVO();
					BeanUtils.copyProperties(temp , jsmsSaleCreditBillVO);
					if(jsmsSaleCreditBillVO!=null&&jsmsSaleCreditBillVO.getSaleId()!=null){
						JsmsUser jsmsUser = jsmsUserService.getById2(jsmsSaleCreditBillVO.getSaleId());
						if(jsmsUser!=null){
							jsmsSaleCreditBillVO.setRealName(jsmsUser.getRealname());
						}
						JsmsUser jsmsUser1 = jsmsUserService.getById2(jsmsSaleCreditBillVO.getAdminId());
						if(jsmsUser1!=null){
							jsmsSaleCreditBillVO.setAdminName(jsmsUser1.getRealname());
						}else{
							jsmsSaleCreditBillVO.setAdminName("系统");
						}
						if(jsmsSaleCreditBillVO.getFinancialType().equals(FinancialType.入账.getValue())){
							jsmsSaleCreditBillVO.setFinancialTypeStr("出账");
						}else{
							jsmsSaleCreditBillVO.setFinancialTypeStr("入账");
						}
						if(jsmsSaleCreditBillVO.getBusinessType().intValue()==BusinessType.财务给销售授信.getValue().intValue()){
							jsmsSaleCreditBillVO.setBusinessTypeStr("财务给销售授信");
						}else if(jsmsSaleCreditBillVO.getBusinessType().intValue()==BusinessType.财务降低销售授信.getValue().intValue()){
							jsmsSaleCreditBillVO.setBusinessTypeStr("财务降低销售授信");
						}
						jsmsSaleCreditBillVO.setCreateTimeStr(format.format(jsmsSaleCreditBillVO.getCreateTime()));
						jsmsSaleCreditBillVO.setAmountStr(jsmsSaleCreditBillVO.getAmount().toString());
					}
					jsmsSaleCreditBillVOMap = BeanUtil.beanToMap(jsmsSaleCreditBillVO, true);
					exportList.add(jsmsSaleCreditBillVOMap);
				}
			}
		}else{
			response.setContentType("text/plain;charset=UTF-8");
			try {
				response.getWriter().write("导出Excel文件失败，请联系管理员");
				response.getWriter().flush();
			} catch (IOException e) {
				logger.error("导出Excel文件失败", e);
			}
		}
		JsmsSaleCreditBill jsmsSaleCreditBill = jsmsSaleCreditBillService.totalCreditBill(param);
		Map data = new HashMap();
		data.put("id", "-");
		data.put("businessTypeStr", "-");
		data.put("financialTypeStr", "合计");
		if(jsmsSaleCreditBill!=null){
			data.put("amount",jsmsSaleCreditBill.getAmount());
		}else{
			data.put("amount","0.0000");
		}
		data.put("saleId","-");
		data.put("realName","-");
		data.put("adminName","-");
		data.put("remark","-");
		data.put("createTimeStr","-");
		exportList.add(data);
		excel.setDataList(exportList);
		if (ExcelUtils.exportExcel(excel)) {
			FileUtils.download(filePath,response);
			FileUtils.delete(filePath);
		} else {
			response.setContentType("text/plain;charset=UTF-8");
			try {
				response.getWriter().write("导出Excel文件失败，请联系管理员");
				response.getWriter().flush();
			} catch (IOException e) {
				logger.error("导出Excel文件失败", e);
			}
		}
	}
	/**
	 * 客户授信账单
	 * \finance\sell\customerCreditBill.html
	 */
	@RequestMapping("/CustomerCreditBill")
	public String CustomerCreditBillView(Model model, HttpSession session) {
		model.addAttribute("max_export_num", ConfigUtils.max_export_excel_num);
		UserSession user=getUserFromSession(session);
		model.addAttribute("menus", AgentUtils.hasMenuRight(user, "我的授信-wdsx", "客户授信-khsx","客户历史授信-khlssx","销售财务-xscw","销售历史财务-xslscw","销售授信账单-xssxzd","客户授信账单-khsxzd"));
		return "finance/sell/customerCreditBill";
	}

	/**
	 * 客户授信账单查询分页数据
	 *
	 * @return
	 */
	@ApiOperation(value = "查询客户授信账单列表数据", notes = "查询销客户信账单列表数据", tags = "财务管理-客户财务", response = PageContainer.class)
	@RequestMapping("/CustomerCreditBill/list")
	@ResponseBody
	public PageContainer CustomerCreditBillList(@RequestParam Map<String,String> params) {
		params.put("pageRowCount", params.get("rows"));
		params.put("currentPage", params.get("page"));
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
		JsmsPage jsmsPage = PageConvertUtil.paramToPage(params);
		jsmsPage.setParams(params);
		JsmsPage queryPage = jsmsSaleCreditBillService.queryCreditBillListOfCustomer(jsmsPage);
		List<JsmsSaleCreditBillVO> list = new ArrayList<>();
		if(queryPage.getData().size()>0){
			for (Object temp : queryPage.getData()) {
				JsmsSaleCreditBillVO jsmsSaleCreditBillVO = new JsmsSaleCreditBillVO();
				BeanUtils.copyProperties(temp , jsmsSaleCreditBillVO);
				if(jsmsSaleCreditBillVO!=null&&jsmsSaleCreditBillVO.getSaleId()!=null){
					JsmsUser jsmsUser = jsmsUserService.getById2(jsmsSaleCreditBillVO.getSaleId());
					if(jsmsUser!=null){
						jsmsSaleCreditBillVO.setRealName(jsmsUser.getRealname());
					}
					JsmsUser jsmsUser1 = jsmsUserService.getById2(jsmsSaleCreditBillVO.getAdminId());
					if(jsmsUser1!=null){
						jsmsSaleCreditBillVO.setAdminName(jsmsUser1.getRealname());
					}else{
						jsmsSaleCreditBillVO.setAdminName("系统");
					}
					JsmsAgentInfo  jsmsAgentInfo =jsmsAgentInfoService.getByAgentId(Integer.parseInt(String.valueOf(jsmsSaleCreditBillVO.getObjectId())));
					if(jsmsAgentInfo!=null){
						jsmsSaleCreditBillVO.setAgentName(jsmsAgentInfo.getAgentName());
						if(jsmsAgentInfo.getAgentType().intValue()==AgentType.品牌代理商.getValue().intValue()){
							jsmsSaleCreditBillVO.setAgentTypeStr("品牌代理商");
						}else if(jsmsAgentInfo.getAgentType().intValue()==AgentType.OEM代理商.getValue().intValue()){
							jsmsSaleCreditBillVO.setAgentTypeStr("OEM代理商");
						}
						jsmsSaleCreditBillVO.setAgentId(jsmsAgentInfo.getAgentId());
					}
					if(jsmsSaleCreditBillVO.getFinancialType().equals(FinancialType.入账.getValue())){
						jsmsSaleCreditBillVO.setFinancialTypeStr("入账");
					}else{
						jsmsSaleCreditBillVO.setFinancialTypeStr("出账");
					}
					if(jsmsSaleCreditBillVO.getBusinessType().intValue()==BusinessType.销售给客户授信.getValue().intValue()){
						jsmsSaleCreditBillVO.setBusinessTypeStr("销售给客户授信");
					}else if(jsmsSaleCreditBillVO.getBusinessType().intValue()==BusinessType.销售降低客户授信.getValue().intValue()){
						jsmsSaleCreditBillVO.setBusinessTypeStr("销售降低客户授信");
					}else if(jsmsSaleCreditBillVO.getBusinessType().intValue()==BusinessType.客户回款.getValue().intValue()){
						jsmsSaleCreditBillVO.setBusinessTypeStr("客户回款");
					}
					jsmsSaleCreditBillVO.setCreateTimeStr(format.format(jsmsSaleCreditBillVO.getCreateTime()));
					jsmsSaleCreditBillVO.setAmountStr(jsmsSaleCreditBillVO.getAmount().toString());
				}
				list.add(jsmsSaleCreditBillVO);
			}
		}
		queryPage.setData(list);
		PageContainer page  = PageConvertUtil.pageToContainer(queryPage);
		return page;
	}

	//销售账单合计
	@RequestMapping("/CustomerCreditBill/total")
	@ResponseBody
	public ResultVO CustomerCreditBillTotal(@RequestParam Map<String,String> params) {
		ResultVO resultVO = ResultVO.failure();
		JsmsSaleCreditBill jsmsSaleCreditBill = jsmsSaleCreditBillService.totalCreditBillOfCustomer(params);
		Map data = new HashMap();
		if(jsmsSaleCreditBill!=null){
			data.put("amount",jsmsSaleCreditBill.getAmount());
		}else{
			data.put("amount","0.0000");
		}
		resultVO.setSuccess(true);
		resultVO.setData(data);
		return resultVO;
	}
	/**
	 * 历史授信数据导出
	 *@param param
	 * @param response
	 */
	@RequestMapping("/CustomerCreditBill/export")
	public void CustomerCreditBillExport(HttpSession session,@RequestParam Map<String,String> param,  HttpServletResponse response) {
		String filePath = ConfigUtils.save_path + "/客户授信账单信息_" + DateTime.now().toString("yyyyMMddHHmmss") + ".xls";
		Map<String,Object> params=new HashedMap();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss");
		Map<String, Object> jsmsSaleCreditBillVOMap = new HashMap<>();
		List<Map<String, Object>> exportList = new ArrayList<>();
		params.putAll(param);
		JsmsPage jpage=new JsmsPage();
		jpage.setParams(params);
		jpage.setRows(Integer.valueOf(ConfigUtils.max_export_excel_num));
		Excel excel = new Excel();
		excel.setFilePath(filePath);
		excel.setTitle("销售授信账单信息");
		excel.addHeader(20, "业务单号", "id");
		excel.addHeader(20, "业务类型", "businessTypeStr");
		excel.addHeader(20, "财务类型", "financialTypeStr");
		excel.addHeader(20, "账单金额(元)", "amount");
		excel.addHeader(20, "代理商ID", "agentId");
		excel.addHeader(20, "代理商名称", "agentName");
		excel.addHeader(20, "代理商类型", "agentTypeStr");
		excel.addHeader(20, "归属销售", "realName");
		excel.addHeader(20, "操作者", "adminName");
		excel.addHeader(20, "备注", "remark");
		excel.addHeader(20, "操作时间", "createTimeStr");
		if(jpage.getRows()>0){
			JsmsPage queryPage = jsmsSaleCreditBillService.queryCreditBillListOfCustomer(jpage);
			List<JsmsSaleCreditBillVO> list = new ArrayList<>();
			if(queryPage.getData().size()>0){
				for (Object temp : queryPage.getData()) {
					JsmsSaleCreditBillVO jsmsSaleCreditBillVO = new JsmsSaleCreditBillVO();
					BeanUtils.copyProperties(temp , jsmsSaleCreditBillVO);
					if(jsmsSaleCreditBillVO!=null&&jsmsSaleCreditBillVO.getSaleId()!=null){
						JsmsUser jsmsUser = jsmsUserService.getById2(jsmsSaleCreditBillVO.getSaleId());
						if(jsmsUser!=null){
							jsmsSaleCreditBillVO.setRealName(jsmsUser.getRealname());
						}
						JsmsUser jsmsUser1 = jsmsUserService.getById2(jsmsSaleCreditBillVO.getAdminId());
						if(jsmsUser1!=null){
							jsmsSaleCreditBillVO.setAdminName(jsmsUser1.getRealname());
						}else{
							jsmsSaleCreditBillVO.setAdminName("系统");
						}
						JsmsAgentInfo  jsmsAgentInfo =jsmsAgentInfoService.getByAgentId(Integer.parseInt(String.valueOf(jsmsSaleCreditBillVO.getObjectId())));
						if(jsmsAgentInfo!=null){
							jsmsSaleCreditBillVO.setAgentName(jsmsAgentInfo.getAgentName());
							if(jsmsAgentInfo.getAgentType().intValue()==AgentType.品牌代理商.getValue().intValue()){
								jsmsSaleCreditBillVO.setAgentTypeStr("品牌代理商");
							}else if(jsmsAgentInfo.getAgentType().intValue()==AgentType.OEM代理商.getValue().intValue()){
								jsmsSaleCreditBillVO.setAgentTypeStr("OEM代理商");
							}
							jsmsSaleCreditBillVO.setAgentId(jsmsAgentInfo.getAgentId());
						}
						if(jsmsSaleCreditBillVO.getFinancialType().equals(FinancialType.入账.getValue())){
							jsmsSaleCreditBillVO.setFinancialTypeStr("入账");
						}else{
							jsmsSaleCreditBillVO.setFinancialTypeStr("出账");
						}
						if(jsmsSaleCreditBillVO.getBusinessType().intValue()==BusinessType.销售给客户授信.getValue().intValue()){
							jsmsSaleCreditBillVO.setBusinessTypeStr("销售给客户授信");
						}else if(jsmsSaleCreditBillVO.getBusinessType().intValue()==BusinessType.销售降低客户授信.getValue().intValue()){
							jsmsSaleCreditBillVO.setBusinessTypeStr("销售降低客户授信");
						}else if(jsmsSaleCreditBillVO.getBusinessType().intValue()==BusinessType.客户回款.getValue().intValue()){
							jsmsSaleCreditBillVO.setBusinessTypeStr("客户回款");
						}
						jsmsSaleCreditBillVO.setCreateTimeStr(format.format(jsmsSaleCreditBillVO.getCreateTime()));
						jsmsSaleCreditBillVO.setAmountStr(jsmsSaleCreditBillVO.getAmount().toString());
					}
					jsmsSaleCreditBillVOMap = BeanUtil.beanToMap(jsmsSaleCreditBillVO, true);
					exportList.add(jsmsSaleCreditBillVOMap);
				}
			}
		}else{
			response.setContentType("text/plain;charset=UTF-8");
			try {
				response.getWriter().write("导出Excel文件失败，请联系管理员");
				response.getWriter().flush();
			} catch (IOException e) {
				logger.error("导出Excel文件失败", e);
			}
		}
		JsmsSaleCreditBill jsmsSaleCreditBill = jsmsSaleCreditBillService.totalCreditBillOfCustomer(param);
		Map data = new HashMap();
		data.put("id", "-");
		data.put("businessTypeStr", "-");
		data.put("financialTypeStr", "合计");
		if(jsmsSaleCreditBill!=null){
			data.put("amount",jsmsSaleCreditBill.getAmount());
		}else{
			data.put("amount","0.0000");
		}
		data.put("agentId","-");
		data.put("agentName","-");
		data.put("agentType","-");
		data.put("realName","-");
		data.put("adminName","-");
		data.put("remark","-");
		data.put("createTimeStr","-");
		exportList.add(data);
		excel.setDataList(exportList);
		if (ExcelUtils.exportExcel(excel)) {
			FileUtils.download(filePath,response);
			FileUtils.delete(filePath);
		} else {
			response.setContentType("text/plain;charset=UTF-8");
			try {
				response.getWriter().write("导出Excel文件失败，请联系管理员");
				response.getWriter().flush();
			} catch (IOException e) {
				logger.error("导出Excel文件失败", e);
			}
		}
	}
}