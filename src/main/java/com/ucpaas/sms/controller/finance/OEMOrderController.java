package com.ucpaas.sms.controller.finance;

import com.ucpaas.sms.common.util.file.ExcelUtils;
import com.ucpaas.sms.common.util.file.FileUtils;
import com.ucpaas.sms.controller.BaseController;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.model.Excel;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.finance.order.OrderInfoService;
import com.ucpaas.sms.util.AgentUtils;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.ConverUtil;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lpjLiu on 2017/5/16.
 * OEM 订单
 */
@Controller
@RequestMapping("/finance/oem")
public class OEMOrderController extends BaseController{
    private static Logger logger = LoggerFactory.getLogger(OEMOrderController.class);

    @Autowired
    private OrderInfoService orderInfoService;

    @RequestMapping("/agentOrder")
    public String view(String type, Model model, HttpSession session,String tab) {
        model.addAttribute("max_export_num", ConfigUtils.max_export_excel_num);
        if (type == null){
            type = "0";
        }
        model.addAttribute("tab",tab);
        model.addAttribute("type", type);
        UserSession user = getUserFromSession(session);
        model.addAttribute("menus", AgentUtils.hasMenuRight(user, "品牌代理商订单-PINdlsdd","客户订单-OEMdlsdd", "子账户订单-OEMkhdd","客户库存-OEMdlskc", "子账户库存-OEMkhkc"));
        return "finance/agent_oem/order/list";
    }

    @RequestMapping("/agentOrder/view")
    public String agentOrderDetail(Long orderId, Model model) {
        model.addAttribute("data", orderInfoService.getOEMAgentOrderById(orderId));
        return "finance/agent_oem/order/agent/view";
    }

    @ApiOperation(value = "查询代理商订单列表数据", notes = "查询代理商订单列表数据", tags = "财务管理-客户订单信息", response = PageContainer.class)
    @ApiImplicitParams({ @ApiImplicitParam(name = "page", value = "页码", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "rows", value = "行数", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "operatorCode", value = "运营商类型", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "condition", value = "订单编号/产品代码/代理商ID", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "productType", value = "产品类型", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "orderType", value = "订单类型", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "start_time_day", value = "开始时间", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "end_time_day", value = "结束时间", dataType = "String", paramType = "query") })
    @PostMapping("/agentOrder/list")
    @ResponseBody
    public PageContainer agentOrderlist(String operatorCode, String rows, String page, String condition, String productType, String orderType, String start_time_day, String end_time_day, HttpSession session) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("pageRowCount", rows);
            params.put("currentPage", page);

            // 订单号/产品ID/代理商ID
            params.put("agent_text", condition);

            if (StringUtils.isNotBlank(operatorCode)) {
                params.put("operatorCode", operatorCode);
            }

            if (StringUtils.isNotBlank(productType)) {
                params.put("product_type", productType);
            }

            if (StringUtils.isNotBlank(orderType)) {
                params.put("order_type", orderType);
            }

            if (StringUtils.isNotBlank(start_time_day)) {
                params.put("start_time_day", start_time_day);
            }

            if (StringUtils.isNotBlank(end_time_day)) {
                params.put("end_time_day", end_time_day);
            }

//        return orderInfoService.queryAgentOrder(params);
            UserSession userSession = getUserFromSession(session);
            PageContainer result = orderInfoService.queryOEMAgentOrder(params, userSession);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取客户订单失败{}", e);
            return new PageContainer();
        }
    }

    @RequestMapping("/agentOrder/export")
    public void agentOrderExport(String operatorCode, String condition, String productType, String orderType, String start_time_day, String end_time_day, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String filePath = ConfigUtils.save_path + "/客户订单信息_" + DateTime.now().toString("yyyyMMddHHmmss") + ".xls";

        Map<String, Object> params = new HashMap<String, Object>();
        StringBuffer buffer = new StringBuffer();
        buffer.append("查询条件 -> ");
        if (StringUtils.isNotBlank(condition)) {
            params.put("agent_text", condition);
            buffer.append("订单编号/产品代码/客户ID ：")
                    .append(condition)
                    .append(";");
        }

        if (StringUtils.isNotBlank(productType)) {
            params.put("product_type", productType);
            buffer.append("产品类型 ：")
                    .append(ConverUtil.productTypeToName(productType))
                    .append(";");
        }

        if (StringUtils.isNotBlank(orderType)) {
            params.put("order_type", orderType);
            buffer.append("订单类型 ：")
                    .append(ConverUtil.oemOrderTypeToName(orderType))
                    .append(";");
        }

        if (StringUtils.isNotBlank(operatorCode)) {
            params.put("operatorCode", operatorCode);
            buffer.append("运营商类型 ：")
                    .append(operatorCode)
                    .append(";");
        }

        if (StringUtils.isNotBlank(start_time_day)) {
            params.put("start_time_day", start_time_day);
            buffer.append("  开始时间：")
                    .append(start_time_day)
                    .append(";");
        }

        if (StringUtils.isNotBlank(end_time_day)) {
            params.put("end_time_day", end_time_day);
            buffer.append("  结束时间：")
                    .append(end_time_day);
        }

        Excel excel = new Excel();
        excel.setFilePath(filePath);
        excel.setTitle("客户订单信息");
        excel.addRemark(buffer.toString());
        excel.addHeader(20, "订单编号", "order_no");
        excel.addHeader(20, "订单类型", "order_type_name");
        excel.addHeader(20, "产品代码", "product_code");
        excel.addHeader(20, "产品类型", "product_type_name");
        excel.addHeader(20, "运营商", "operator_code");
        excel.addHeader(20, "区域", "area_code");
        excel.addHeader(20, "短信单价（元）", "unit_price");
        excel.addHeader(20, "订单短信条数", "order_number");
        excel.addHeader(20, "订单金额（元）", "order_amount");
        excel.addHeader(20, "客户ID", "agent_id");
        excel.addHeader(20, "到期时间", "due_time");
        excel.addHeader(20, "创建时间", "create_time");

//        excel.setDataList(orderInfoService.exportAgentOrder(params));
        UserSession userSession = getUserFromSession(session);
        List<Map<String, Object>> exportData =  orderInfoService.exportOEMAgentOrder(params,userSession);
        excel.setDataList(exportData);

        if (ExcelUtils.exportExcel(excel)) {
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

    @RequestMapping("/clientOrder/view")
    public String clientOrderDetail(Long orderId, Model model) {
        model.addAttribute("data", orderInfoService.getOEMClientOrderById(orderId));
        return "finance/agent_oem/order/client/view";
    }

    @ApiOperation(value = "查询客户订单列表数据", notes = "查询客户订单列表数据", tags = "财务管理-OEM客户订单信息", response = PageContainer.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "page", value = "页码", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "rows", value = "行数", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "operatorCode", value = "运营商类型", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "condition", value = "订单编号/客户ID/代理商ID", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "productType", value = "产品类型", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "orderType", value = "订单类型", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "start_time_day", value = "开始时间", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "end_time_day", value = "结束时间", dataType = "String", paramType = "query")})
    @RequestMapping("/clientOrder/list")
    @ResponseBody
    public PageContainer clientOrderlist(String operatorCode, String rows, String page, String condition, String productType, String orderType, String start_time_day, String end_time_day, HttpSession session) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("pageRowCount", rows);
            params.put("currentPage", page);

            // 订单号/客户ID/代理商ID
            params.put("client_text", condition);

            if (StringUtils.isNotBlank(operatorCode)) {
                params.put("operatorCode", operatorCode);
            }

            if (StringUtils.isNotBlank(productType)) {
                params.put("product_type", productType);
            }

            if (StringUtils.isNotBlank(orderType)) {
                params.put("order_type", orderType);
            }

            if (StringUtils.isNotBlank(start_time_day)) {
                params.put("start_time_day", start_time_day);
            }

            if (StringUtils.isNotBlank(end_time_day)) {
                params.put("end_time_day", end_time_day);
            }

//        return orderInfoService.queryClientOrder(params);
            UserSession userSession = getUserFromSession(session);
            PageContainer result = orderInfoService.queryOEMClientOrder(params, userSession);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取OEM子客户订单信息失败{}", e);
            return new PageContainer();
        }
    }

    @RequestMapping("/clientOrder/export")
    public void clientOrderExport(String operatorCode, String condition, String productType, String orderType, String start_time_day, String end_time_day, HttpServletRequest request, HttpServletResponse response,HttpSession session) {
        String filePath = ConfigUtils.save_path + "/子账户订单信息_" + DateTime.now().toString("yyyyMMddHHmmss") + ".xls";

        Map<String, Object> params = new HashMap<String, Object>();
        StringBuffer buffer = new StringBuffer();
        buffer.append("查询条件 -> ");
        if (StringUtils.isNotBlank(condition)) {
            params.put("client_text", condition);
            buffer.append("订单编号/客户ID/子账户ID ：")
                    .append(condition)
                    .append(";");
        }

        if (StringUtils.isNotBlank(productType)) {
            params.put("product_type", productType);
            buffer.append("产品类型 ：")
                    .append(ConverUtil.productTypeToName(productType))
                    .append(";");
        }

        if (StringUtils.isNotBlank(orderType)) {
            params.put("order_type", orderType);
            buffer.append("订单类型 ：")
                    .append(ConverUtil.oemOrderTypeToName(orderType))
                    .append(";");
        }

        if (StringUtils.isNotBlank(operatorCode)) {
            params.put("operatorCode", operatorCode);
            buffer.append("运营商类型 ：")
                    .append(operatorCode)
                    .append(";");
        }

        if (StringUtils.isNotBlank(start_time_day)) {
            params.put("start_time_day", start_time_day);
            buffer.append("  开始时间：")
                    .append(start_time_day)
                    .append(";");
        }

        if (StringUtils.isNotBlank(end_time_day)) {
            params.put("end_time_day", end_time_day);
            buffer.append("  结束时间：")
                    .append(end_time_day);
        }

        Excel excel = new Excel();
        excel.setFilePath(filePath);
        excel.setTitle("子账户订单信息");
        excel.addRemark(buffer.toString());
        excel.addHeader(20, "订单编号", "order_no");
        excel.addHeader(20, "订单类型", "order_type_name");
        excel.addHeader(20, "产品类型", "product_type_name");
        excel.addHeader(20, "运营商", "operator_code");
        excel.addHeader(20, "区域", "area_code");
        excel.addHeader(20, "短信单价(元)", "unit_price");
        excel.addHeader(20, "订单短信条数", "order_number");
        excel.addHeader(20, "订单金额（元）", "order_price");
        excel.addHeader(20, "子账户ID", "client_id");
        excel.addHeader(20, "客户ID", "agent_id");
        excel.addHeader(20, "到期时间", "due_time");
        excel.addHeader(20, "创建时间", "create_time");

//        excel.setDataList(orderInfoService.exportClientOrder(params));
        UserSession userSession = getUserFromSession(session);
        List<Map<String, Object>> exportData = orderInfoService.exportOEMClientOrder(params, userSession);
        excel.setDataList(exportData);

        if (ExcelUtils.exportExcel(excel)) {
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
}
