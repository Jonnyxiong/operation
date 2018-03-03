package com.ucpaas.sms.controller.finance;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.order.product.exception.JsmsClientOrderProductException;
import com.jsmsframework.product.service.JsmsProductInfoService;
import com.jsmsframework.user.entity.JsmsAccount;
import com.jsmsframework.user.service.JsmsAccountService;
import com.ucpaas.sms.common.util.ColumnConverUtil;
import com.ucpaas.sms.common.util.file.ExcelUtils;
import com.ucpaas.sms.common.util.file.FileUtils;
import com.ucpaas.sms.controller.BaseController;
import com.ucpaas.sms.dto.PurchaseOrderVO;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.enums.SessionEnum;
import com.ucpaas.sms.model.Excel;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.admin.AuthorityService;
import com.ucpaas.sms.service.finance.order.OrderInfoService;
import com.ucpaas.sms.util.AgentUtils;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.HttpUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by lpjLiu on 2017/5/16.
 * 品牌订单
 */
@Controller
@RequestMapping("/finance")
public class OrderController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderInfoService orderInfoService;
    @Autowired
    private JsmsProductInfoService jsmsProductInfoService;
    @Autowired
    private JsmsAccountService jsmsAccountService;
    @Autowired
    private AuthorityService authorityService;


    @RequestMapping("/order")
    public String view(Model model,HttpSession session) {
        model.addAttribute("max_export_num", ConfigUtils.max_export_excel_num);
        UserSession user = getUserFromSession(session);
        model.addAttribute("menus", AgentUtils.hasMenuRight(user, "品牌代理商订单-PINdlsdd","客户订单-OEMdlsdd", "子账户订单-OEMkhdd","客户库存-OEMdlskc", "子账户库存-OEMkhkc"));
        return "finance/agent/order/list";
    }

    /**
     * 代理商财务信息查询分页数据
     *
     * @param rows           行数
     * @param page           当前查询页码
     * @param params      条件
     * @return
     */
    @ApiOperation(value = "查询品牌订单列表数据", notes = "查询品牌订单列表数据", tags = "财务管理-品牌订单信息", response = PageContainer.class)
    @ApiImplicitParams({ @ApiImplicitParam(name = "page", value = "页码", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "rows", value = "行数", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "operatorCode", value = "运营商类型", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "condition", value = "订单号/产品代码/客户ID/代理商ID", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "status", value = "订单状态", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "startDateTime", value = "开始时间", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "endDateTime", value = "结束时间", dataType = "String", paramType = "query") })
    @RequestMapping("/order/list")
    @ResponseBody
//    public PageContainer list(String rows, String page, String condition, String status, String startDateTime, String endDateTime) {
    public PageContainer list(String rows, String page, @RequestParam Map<String,String> params, HttpSession session) {
//        Map<String, String> params = new HashMap<>();
        params.put("pageRowCount", rows);
        params.put("currentPage", page);
        params.put("orderByClause", "create_time DESC");

        if (StringUtils.isNotBlank(params.get("endDateTime"))) {
            params.put("endDateTime", new StringBuilder(params.get("endDateTime")).append(" 23:59:59").toString());
        }

//        PageContainer container = orderInfoService.query(params);
        UserSession user = getUserFromSession(session);

        //判断登录用户仅自己查看并没有销售角色
        if (authorityService.isDataAuthority(user.getId()) && !AgentUtils.isSaleRole(user)){
            return new PageContainer();
        }
        PageContainer container = orderInfoService.queryOrder(params, user);

        return container;
//        return orderInfoService.query(params);
    }
    
    @RequestMapping("/order/total")
    @ResponseBody
//    public ResultVO list(String condition, String status, String start_time_day, String end_time_day) {
    public ResultVO list(@RequestParam Map<String,String> params,HttpSession session) {
    	ResultVO resultVO = ResultVO.failure();

        if (StringUtils.isNotBlank(params.get("endDateTime"))) {
            params.put("endDateTime", new StringBuilder(params.get("endDateTime")).append(" 23:59:59").toString());
        }
//        Map data = orderInfoService.total(params);
        UserSession userSession = getUserFromSession(session);
        Map data = orderInfoService.totalOrder(params, userSession);
        resultVO.setSuccess(true);
        resultVO.setData(data);
    	return resultVO;
    }

    /**
     * 代理商财务信息查询页面数据导出
     *
     * @param params      查询条件
     * @param request
     * @param response
     */
    @RequestMapping("/order/export")
//    public void stdSMSPageListExport(String condition, String status, String start_time_day, String end_time_day, HttpServletRequest request, HttpServletResponse response) {
    public void stdSMSPageListExport(@RequestParam Map<String,String> params, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String filePath = ConfigUtils.save_path + "/品牌订单信息_" + DateTime.now().toString("yyyyMMddHHmmss") + ".xls";

        StringBuffer buffer = new StringBuffer();
        buffer.append("查询条件 -> ");
        if (StringUtils.isNotBlank(params.get("condition"))) {
//            params.put("condition", params.get("condition"));
            buffer.append("订单号/产品ID/客户ID/代理商ID ：")
                    .append(params.get("condition"))
                    .append(";");
        }

        if (StringUtils.isNotBlank(params.get("status"))) {
            buffer.append("订单状态 ：")
                    .append(ColumnConverUtil.orderStatusToName(params.get("status")))
                    .append(";");
        }

        if (StringUtils.isNotBlank(params.get("status"))) {
            buffer.append("  开始时间：")
                    .append(params.get("startDateTime"))
                    .append(";");
        }

        if (StringUtils.isNotBlank(params.get("endDateTime"))) {
            buffer.append("  结束时间：")
                    .append(params.get("endDateTime"));
            params.put("endDateTime", new StringBuilder(params.get("endDateTime")).append(" 23:59:59").toString());
        }
        params.put("orderByClause", "create_time DESC");

        Excel excel = new Excel();
        excel.setFilePath(filePath);
        excel.setTitle("品牌订单信息");
        excel.addRemark(buffer.toString());
        excel.addHeader(20, "订单号", "orderId");
        excel.addHeader(20, "订单类型", "orderTypeStr");
        excel.addHeader(20, "产品代码", "productCode");
        excel.addHeader(20, "产品类型", "productTypeStr");
        excel.addHeader(20, "运营商", "operatorCodeStr");
        excel.addHeader(20, "区域", "areaCodeStr");
        excel.addHeader(20, "产品总售价（元）", "salePriceStr");
        excel.addHeader(20, "产品总成本价（元）", "productCostStr");
        excel.addHeader(20, "总数量", "quantityStr");
        excel.addHeader(20, "剩余数量", "remainQuantityStr");
        excel.addHeader(20, "客户ID", "clientId");
        excel.addHeader(20, "代理商ID", "agentId");
        excel.addHeader(20, "状态", "statusStr");
        excel.addHeader(20, "有效期（天）", "activePeriodStr");
        excel.addHeader(20, "到期时间", "endTimeStr");
        excel.addHeader(20, "创建时间", "createTimeStr");

        UserSession userSession = getUserFromSession(session);
//        List<Map<String,Object>> data = orderInfoService.exportOrderExcel(params);
//        Map<String,Object> totalDate = orderInfoService.total(params);
        List<Map<String,Object>> data = orderInfoService.exportBrandOrderExcel(params,userSession);
        Map<String,Object> totalDate = orderInfoService.totalOrder(params, userSession);

        totalDate.put("orderId", "总计");
        totalDate.put("orderTypeStr", "-");
        totalDate.put("productCode", "-");
        totalDate.put("productTypeStr", "-");
        totalDate.put("operatorCodeStr", "-");
        totalDate.put("areaCodeStr", "-");
        totalDate.put("salePriceStr", ((BigDecimal)totalDate.get("sale_price")).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        totalDate.put("productCostStr", ((BigDecimal)totalDate.get("product_cost")).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        totalDate.put("quantityStr", (totalDate.get("quantity")).toString());
        totalDate.put("remainQuantityStr", (totalDate.get("remain_quantity")).toString());
        totalDate.put("clientId", "-");
        totalDate.put("agentId", "-");
        totalDate.put("statusStr", "-");
        totalDate.put("activePeriodStr", "-");
        totalDate.put("endTimeStr", "-");
        totalDate.put("createTimeStr", "-");
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
     * 订单支付
     *
     * @param params
     * @return
     */
    @RequestMapping("/order/payment")
    @ResponseBody
    public ResultVO payment(@RequestParam Map<String, String> params, HttpSession session, HttpServletRequest request) {
        ResultVO result = ResultVO.failure();
        // 需要用户Id
        UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        /*params.put("userId", userSession.getId().toString());*/
        params.put("pageUrl", request.getRequestURI());
        params.put("ip", HttpUtils.getIpAddress(request));

//        Map data = orderInfoService.confirmBuy(params);
        Map data = orderInfoService.jsmsConfirmBuy(params,userSession.getId());
        result.setSuccess("success".equals(data.get("result").toString()));
        result.setMsg(data.get("msg").toString());
        return result;
    }

    @RequestMapping("/order/view")
    public String pageView(Long sub_id, Model model) {
        model.addAttribute("data", orderInfoService.queryOrderInfoBySubId(sub_id));
        return "finance/agent/order/view";
    }

    @GetMapping("/order/charge")
    public ModelAndView charge(ModelAndView mv,String clientId){
        mv.setViewName("finance/agent/order/charge");
        mv.addObject("clientId",clientId);
        return mv;
    }

    @PostMapping("/order/charge")
    @ResponseBody
    public JsmsPage pageCharge(@RequestParam Map params,JsmsPage page){
        page.setParams(params);
        String clientId = (String) params.get("clientId");
        if(StringUtils.isBlank(clientId)){
            return page;
        }
        JsmsAccount account = jsmsAccountService.getByClientId(clientId);
        return jsmsProductInfoService.queryProxiedList(page, account.getAgentId(), clientId);
    }

    @PostMapping("/order/createOrder")
    @ResponseBody
    public com.jsmsframework.common.dto.ResultVO doCharge(@RequestBody PurchaseOrderVO purchaseOrderList, HttpSession session){
        JsmsAccount account = jsmsAccountService.getByClientId(purchaseOrderList.getClientId());
        purchaseOrderList.setAgentId(account.getAgentId());
        UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        com.jsmsframework.common.dto.ResultVO resultVO = null;
        try {
            resultVO = orderInfoService.createOrderAndBuy(purchaseOrderList, user.getId());
        } catch (JsmsClientOrderProductException e) {
            resultVO = com.jsmsframework.common.dto.ResultVO.failure(e.getMessage());
            logger.debug("短信充值失败 ---> {} , --------> {}",e.getMessage(),e);
        } catch (Exception e) {
            resultVO = com.jsmsframework.common.dto.ResultVO.failure("系统错误，请联系管理员");
            logger.debug("短信充值失败 ---> {} , --------> {}",e.getMessage(),e);
        }
        return resultVO;
    }
}
