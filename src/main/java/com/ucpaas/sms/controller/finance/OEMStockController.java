package com.ucpaas.sms.controller.finance;

import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.common.enums.Code;
import com.ucpaas.sms.common.util.ColumnConverUtil;
import com.ucpaas.sms.common.util.file.ExcelUtils;
import com.ucpaas.sms.common.util.file.FileUtils;
import com.ucpaas.sms.controller.BaseController;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.entity.message.User;
import com.ucpaas.sms.model.Excel;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.finance.inventory.OEMInventoryService;
import com.ucpaas.sms.util.AgentUtils;
import com.ucpaas.sms.util.ConfigUtils;
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
 * OEM 库存
 */
@Controller
@RequestMapping("/finance/oem")
public class OEMStockController extends BaseController{

    private static Logger logger = LoggerFactory.getLogger(OEMStockController.class);

    @Autowired
    private OEMInventoryService oemInventoryService;

    @RequestMapping("/agentInventory")
    public String view(Model model, HttpSession session,String tab) {
        model.addAttribute("max_export_num", ConfigUtils.max_export_excel_num);
        UserSession user = getUserFromSession(session);
        model.addAttribute("menus", AgentUtils.hasMenuRight(user, "品牌代理商订单-PINdlsdd","客户订单-OEMdlsdd", "子账户订单-OEMkhdd","客户库存-OEMdlskc", "子账户库存-OEMkhkc"));
        return "finance/agent_oem/stock/list";
    }

    @ApiOperation(value = "查询OEM代理商库存列表数据", notes = "查询OEM代理商库存列表数据", tags = "财务管理-OEM代理商库存信息", response = PageContainer.class)
    @ApiImplicitParams({ @ApiImplicitParam(name = "page", value = "页码", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "rows", value = "行数", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "operatorCode", value = "运营商类型", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "condition", value = "代理商ID/代理商名称/手机号码", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "productType", value = "产品类型", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "start_time_day", value = "开始时间", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "end_time_day", value = "结束时间", dataType = "String", paramType = "query") })
    @PostMapping("/agentInventory/list")
    @ResponseBody
    public PageContainer agentlist(String operatorCode, String rows, String page, String condition, String productType, String start_time_day, String end_time_day, HttpSession session) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("pageRowCount", rows);
            params.put("currentPage", page);

            // 代理商ID/代理商名称/手机号码
            params.put("agent_text", condition);

            if (StringUtils.isNotBlank(operatorCode)) {
                params.put("operatorCode", operatorCode);
            }

            if (StringUtils.isNotBlank(productType)) {
                params.put("product_type", productType);
            }

            if (StringUtils.isNotBlank(start_time_day)) {
                params.put("start_time_day", start_time_day);
            }

            if (StringUtils.isNotBlank(end_time_day)) {
                params.put("end_time_day", end_time_day);
            }

//        return oemInventoryService.queryAgentInventory(params);
            UserSession userSession = getUserFromSession(session);
            PageContainer result = oemInventoryService.queryOEMAgentInventory(params, userSession);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("查询OEM代理商库存列表数据失败", e);
            return new PageContainer();
        }
    }
    

    @RequestMapping("/agentInventory/total")
    @ResponseBody
    public ResultVO agentTotal(String operatorCode, String condition, String productType, String start_time_day, String end_time_day, HttpSession session) {
        try {
            ResultVO resultVO = ResultVO.failure();
            Map<String, Object> params = new HashMap<>();

            // 代理商ID/代理商名称/手机号码
            params.put("agent_text", condition);

            if (StringUtils.isNotBlank(operatorCode)) {
                params.put("operatorCode", operatorCode);
            }

            if (StringUtils.isNotBlank(productType)) {
                params.put("product_type", productType);
            }

            if (StringUtils.isNotBlank(start_time_day)) {
                params.put("start_time_day", start_time_day);
            }

            if (StringUtils.isNotBlank(end_time_day)) {
                params.put("end_time_day", end_time_day);
            }

//        Map data= oemInventoryService.agentTotal(params);
            UserSession userSession = getUserFromSession(session);
            Map data =  oemInventoryService.agentOEMTotal(params, userSession);

            resultVO.setSuccess(true);
            resultVO.setData(data);
            resultVO.setMsg("操作成功!");
            return resultVO;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取OEM代理商库存总条数失败{}",e);
            return ResultVO.failure("获取OEM代理商库存总条数失败");
        }
    }
    
    @RequestMapping("/agentInventory/export")
    public void agentExport(String operatorCode, String condition, String productType, String start_time_day, String end_time_day, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String filePath = ConfigUtils.save_path + "/客户库存信息_" + DateTime.now().toString("yyyyMMddHHmmss") + ".xls";

        Map<String, Object> params = new HashMap<String, Object>();
        StringBuffer buffer = new StringBuffer();
        buffer.append("查询条件 -> ");
        if (StringUtils.isNotBlank(condition)) {
            params.put("agent_text", condition);
            buffer.append("客户ID/客户名称/手机号码 ：")
                    .append(condition)
                    .append(";");
        }

        if (StringUtils.isNotBlank(productType)) {
            params.put("product_type", productType);
            buffer.append("产品类型 ：")
                    .append(ColumnConverUtil.productTypeToName(productType))
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
        excel.setTitle("客户库存信息");
        excel.addRemark(buffer.toString());
        excel.addHeader(20, "客户ID", "agent_id");
        excel.addHeader(20, "客户名称", "agent_name");
        excel.addHeader(20, "手机号码", "mobile");
        excel.addHeader(20, "产品类型", "product_type_name");
        excel.addHeader(20, "运营商", "operator_code");
        excel.addHeader(20, "区域", "area_code");
        excel.addHeader(20, "到期时间", "due_time");
        excel.addHeader(20, "单价(元)", "unit_price");
        excel.addHeader(20, "库存数", "remain_num");
//        List<Map<String,Object>> data = oemInventoryService.exportAgentInventory(params);
//        Map totalData = oemInventoryService.agentTotal(params);
        UserSession userSession = getUserFromSession(session);
        List<Map<String,Object>> data = oemInventoryService.exportOEMAgentInventory(params, userSession);
        Map totalData = oemInventoryService.agentOEMTotal(params,userSession);

        totalData.put("unit_price", "总计");
        data.add(totalData);
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

    @ApiOperation(value = "查询OEM客户库存列表数据", notes = "查询OEM客户库存列表数据", tags = "财务管理-OEM客户库存信息", response = PageContainer.class)
    @ApiImplicitParams({ @ApiImplicitParam(name = "page", value = "页码", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "rows", value = "行数", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "operatorCode", value = "运营商类型", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "condition", value = "客户ID/客户名称/手机号码", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "productType", value = "产品类型", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "start_time_day", value = "开始时间", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "end_time_day", value = "结束时间", dataType = "String", paramType = "query") })
    @PostMapping("/clientInventory/list")
    @ResponseBody
    public PageContainer clientlist(String operatorCode, String rows, String page, String condition, String productType, String start_time_day, String end_time_day, HttpSession session) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("pageRowCount", rows);
            params.put("currentPage", page);

            // 客户ID/客户名称/手机号码
            params.put("client_text", condition);

            if (StringUtils.isNotBlank(operatorCode)) {
                params.put("operatorCode", operatorCode);
            }

            if (StringUtils.isNotBlank(productType)) {
                params.put("product_type", productType);
            }

            if (StringUtils.isNotBlank(start_time_day)) {
                params.put("start_time_day", start_time_day);
            }

            if (StringUtils.isNotBlank(end_time_day)) {
                params.put("end_time_day", end_time_day);
            }

//        return oemInventoryService.queryClientInventory(params);
            UserSession userSession = getUserFromSession(session);
            return oemInventoryService.queryOEMClientInventory(params,userSession);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取OEM客户库存失败{}", e);
            return new PageContainer();
        }
    }
    
    @RequestMapping("/clientInventory/total")
    @ResponseBody
    public ResultVO clientTotal(String operatorCode, String condition, String productType, String start_time_day, String end_time_day, HttpSession session) {
        try {
            ResultVO resultVO = ResultVO.failure();
            Map<String, Object> params = new HashMap<>();

            // 代理商ID/代理商名称/手机号码
            // 客户ID/客户名称/手机号码
            params.put("client_text", condition);

            if (StringUtils.isNotBlank(operatorCode)) {
                params.put("operatorCode", operatorCode);
            }

            if (StringUtils.isNotBlank(productType)) {
                params.put("product_type", productType);
            }

            if (StringUtils.isNotBlank(start_time_day)) {
                params.put("start_time_day", start_time_day);
            }

            if (StringUtils.isNotBlank(end_time_day)) {
                params.put("end_time_day", end_time_day);
            }

//        Map data= oemInventoryService.clientTotal(params);
            UserSession userSession = getUserFromSession(session);
            Map data= oemInventoryService.clientOEMTotal(params, userSession);
            resultVO.setSuccess(true);
            resultVO.setData(data);
            resultVO.setMsg("操作成功!");
            return resultVO;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取OEM客户库存总计失败{}",e);
            return ResultVO.failure(Code.SYS_ERR,"服务器正在检修...");
        }
    }

    @RequestMapping("/clientInventory/export")
    public void clientExport(String operatorCode, String condition, String productType, String start_time_day, String end_time_day, HttpServletRequest request, HttpServletResponse response, HttpSession session) {
        String filePath = ConfigUtils.save_path + "/子账户库存信息_" + DateTime.now().toString("yyyyMMddHHmmss") + ".xls";

        Map<String, Object> params = new HashMap<String, Object>();
        StringBuffer buffer = new StringBuffer();
        buffer.append("查询条件 -> ");
        if (StringUtils.isNotBlank(condition)) {
            params.put("client_text", condition);
            buffer.append("子账户ID/子账户名称/手机号码 ：")
                    .append(condition)
                    .append(";");
        }

        if (StringUtils.isNotBlank(productType)) {
            params.put("product_type", productType);
            buffer.append("产品类型 ：")
                    .append(ColumnConverUtil.productTypeToName(productType))
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
        excel.setTitle("子账户库存信息");
        excel.addRemark(buffer.toString());
        excel.addHeader(20, "子账户ID", "client_id");
        excel.addHeader(20, "子账户名称", "name");
        excel.addHeader(20, "手机号码", "mobile");
        excel.addHeader(20, "产品类型", "product_type_name");
        excel.addHeader(20, "运营商", "operator_code");
        excel.addHeader(20, "区域", "area_code");
        excel.addHeader(20, "到期时间", "due_time");
        excel.addHeader(20, "单价(元)", "unit_price");
        excel.addHeader(20, "库存数", "remain_num");

//        List<Map<String,Object>> data = oemInventoryService.exportClientInventory(params);
//        Map totalData = oemInventoryService.clientTotal(params);
        UserSession userSession = getUserFromSession(session);

        List<Map<String, Object>> data = oemInventoryService.exportOEMAgentInventory(params,userSession);
        Map totalData = oemInventoryService.clientOEMTotal(params, userSession);
        totalData.put("unit_price", "总计");
        data.add(totalData);
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
}