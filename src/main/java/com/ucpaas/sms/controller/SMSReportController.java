package com.ucpaas.sms.controller;

import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.model.Excel;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.smsReport.SmsReportService;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.common.util.file.ExcelUtils;
import com.ucpaas.sms.common.util.file.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lpjLiu on 2017/5/16.
 */
@Controller
public class SMSReportController {
    private static Logger logger = LoggerFactory.getLogger(SMSReportController.class);

    @Autowired
    private SmsReportService smsReportService;

    /**
     * 传统短信-页面视图
     */
    @RequestMapping("/stdSms/query")
    public String stdSMSView(Model model) {
        model.addAttribute("max_export_num", ConfigUtils.max_export_excel_num);
        return "smsReport/stdSms/list";
    }

    /**
     * 传统短信-查询页面分页数据
     *
     * @param rows       行数
     * @param page       当前查询页码
     * @param condition  条件
     * @param start_time 开始时间
     * @param end_time   结束时间
     * @return
     */
    @RequestMapping("/stdSms/list")
    @ResponseBody
    public PageContainer stdSMSPageList(String rows, String page, String condition, String start_time, String end_time) {
        Map<String, String> params = new HashMap<>();
        params.put("pageRowCount", rows);
        params.put("currentPage", page);

        // 客户Id/客户名称/代理商ID
        params.put("text", condition);

        if (start_time == null || end_time == null) {
            DateTime dt = DateTime.now();
            start_time = dt.minusDays(1).toString("yyyy-MM-dd");
            end_time = dt.minusDays(1).toString("yyyy-MM-dd");
        }

        params.put("start_time", start_time.replace("-", ""));
        params.put("end_time", end_time.replace("-", ""));

        return smsReportService.stdSmsQuery(params);
    }
    
    /**
     * 传统短信-查询 总计栏
     */
    @RequestMapping("/stdSms/total")
    @ResponseBody
    public ResultVO stdSmsTotal(String condition, String start_time, String end_time) {
    	ResultVO resultVO = ResultVO.failure();
		Map<String, Object> params = new HashMap<>();
		
		// 客户Id/客户名称/代理商ID
		params.put("text", condition);
		
		if (start_time == null || end_time == null) {
		    DateTime dt = DateTime.now();
		    start_time = dt.minusDays(1).toString("yyyy-MM-dd");
		end_time = dt.minusDays(1).toString("yyyy-MM-dd");
		}
		
		params.put("start_time", start_time.replace("-", ""));
		params.put("end_time", end_time.replace("-", ""));


		List<Integer> product_types = new ArrayList<>();
		product_types.add(0);
		product_types.add(1);
		product_types.add(2);
		product_types.add(9);
		params.put("product_types", product_types); // 按产品类型统计 0：行业，1：营销，2：国际，7：USSD，8：闪信，9：挂机短信 
        Map data = smsReportService.smsTotal(params);
        resultVO.setSuccess(true);
        resultVO.setData(data);
        resultVO.setMsg("操作成功！");
    	return resultVO;
    }

    /**
     * 传统短信-查询页面数据导出
     *
     * @param condition  查询条件
     * @param start_time 开始时间
     * @param end_time   结束时间
     * @param request
     * @param response
     */
    @RequestMapping("/stdSms/list/export")
    public void stdSMSPageListExport(String condition, String start_time, String end_time, HttpServletRequest request, HttpServletResponse response) {
        String filePath = ConfigUtils.save_path + "/传统短信发送统计_" + DateTime.now().toString("yyyyMMddHHmmss") + ".xls";

        Map<String, String> params = new HashMap<String, String>();
        StringBuffer buffer = new StringBuffer();
        buffer.append("查询条件 -> ");
        if (StringUtils.isNotBlank(condition)) {
            params.put("text", condition);
            buffer.append("客户ID/客户名称/代理商ID ：")
                    .append(condition)
                    .append(";");
        }

        if (StringUtils.isNotBlank(start_time)) {
            params.put("start_time", start_time);
            buffer.append("  开始时间：")
                    .append(start_time)
                    .append(";");
        }

        if (StringUtils.isNotBlank(end_time)) {
            params.put("end_time", end_time);
            buffer.append("  结束时间：")
                    .append(end_time);
        }

        Excel excel = new Excel();
        excel.setFilePath(filePath);
        excel.setTitle("传统短信发送统计");
        excel.addRemark(buffer.toString());
        excel.addHeader(20, "客户ID", "clientid");
        excel.addHeader(20, "客户名称", "name");
        excel.addHeader(20, "客户注册时间", "createtime");
        excel.addHeader(20, "所属代理商ID", "agent_id");
        excel.addHeader(20, "代理商名称", "agent_name");
        excel.addHeader(20, "代理商类型", "agent_type_name");
        excel.addHeader(20, "归属销售", "belong_sale_name");
        excel.addHeader(20, "提交条数", "cusSubTotal");
        excel.addHeader(20, "成功条数", "successTotal");
        excel.addHeader(20, "失败条数", "failTotal");
        excel.addHeader(20, "计费条数", "chargetotal");
        excel.addHeader(20, "待发送条数", "notsend");
        excel.addHeader(20, "未知条数", "submitsuccess");
        excel.addHeader(20, "消费金额(元)", "salefee");
        excel.addHeader(20, "消耗成本(元)", "productfee");
        excel.addHeader(20, "通道成本(元)", "costfee");
        List<Map<String, Object>> data = smsReportService.stdSmsExcel(params);
    	Map<String,Object> paramsObj = new HashMap<>();
		paramsObj.putAll(params);
		List<Integer> product_types = new ArrayList<>();
		product_types.add(0);
		product_types.add(1);
		product_types.add(2);
		product_types.add(9);
		paramsObj.put("product_types", product_types); // 按产品类型统计 0：行业，1：营销，2：国际，7：USSD，8：闪信，9：挂机短信 
        Map totalData = smsReportService.smsTotal(paramsObj);
        totalData.put("agent_id", "总计");
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

    /**
     * USSD-页面视图
     */
    @RequestMapping("/ussd/query")
    public String ussdView(Model model) {
        model.addAttribute("max_export_num", ConfigUtils.max_export_excel_num);
        return "smsReport/ussd/list";
    }

    /**
     * USSD-查询页面分页数据
     *
     * @param rows       行数
     * @param page       当前查询页码
     * @param condition  条件
     * @param start_time 开始时间
     * @param end_time   结束时间
     * @return
     */
    @RequestMapping("/ussd/list")
    @ResponseBody
    public PageContainer ussdPageList(String rows, String page, String condition, String start_time, String end_time) {
        Map<String, String> params = new HashMap<>();
        params.put("pageRowCount", rows);
        params.put("currentPage", page);

        // 客户Id/客户名称/代理商ID
        params.put("text", condition);

        if (start_time == null || end_time == null) {
            DateTime dt = DateTime.now();
            start_time = dt.minusDays(1).toString("yyyy-MM-dd");
            end_time = dt.minusDays(1).toString("yyyy-MM-dd");
        }

        params.put("start_time", start_time.replace("-", ""));
        params.put("end_time", end_time.replace("-", ""));

        return smsReportService.ussdSmsQuery(params);
    }
    
    /**
     * ussd短信-查询 总计栏
     */
    @RequestMapping("/ussd/total")
    @ResponseBody
    public ResultVO ussdTotal(String condition, String start_time, String end_time) {
    	ResultVO resultVO = ResultVO.failure();
		Map<String, Object> params = new HashMap<>();
		
		// 客户Id/客户名称/代理商ID
		params.put("text", condition);
		
		if (start_time == null || end_time == null) {
		    DateTime dt = DateTime.now();
		    start_time = dt.minusDays(1).toString("yyyy-MM-dd");
		end_time = dt.minusDays(1).toString("yyyy-MM-dd");
		}
		
		params.put("start_time", start_time.replace("-", ""));
		params.put("end_time", end_time.replace("-", ""));


		List<Integer> product_types = new ArrayList<>();
		product_types.add(7);
		params.put("product_types", product_types); // 按产品类型统计 0：行业，1：营销，2：国际，7：USSD，8：闪信，9：挂机短信 
        Map data = smsReportService.smsTotal(params);
        resultVO.setSuccess(true);
        resultVO.setData(data);
        resultVO.setMsg("操作成功！");
    	return resultVO;
    }

    /**
     * USSD-查询页面数据导出
     *
     * @param condition  查询条件
     * @param start_time 开始时间
     * @param end_time   结束时间
     * @param request
     * @param response
     */
    @RequestMapping(path = "/ussd/list/export")
    public void ussdPageListExport(String condition, String start_time, String end_time, HttpServletRequest request, HttpServletResponse response) {
        String filePath = ConfigUtils.save_path + "/USSD发送统计_" + DateTime.now().toString("yyyyMMddHHmmss") + ".xls";
        Map<String, String> params = new HashMap<String, String>();

        StringBuffer buffer = new StringBuffer();
        buffer.append("查询条件 -> ");
        if (StringUtils.isNotBlank(condition)) {
            params.put("text", condition);
            buffer.append("客户ID/客户名称/代理商ID ：")
                    .append(condition)
                    .append(";");
        }

        if (StringUtils.isNotBlank(start_time)) {
            params.put("start_time", start_time);
            buffer.append("  开始时间：")
                    .append(start_time)
                    .append(";");
        }

        if (StringUtils.isNotBlank(end_time)) {
            params.put("end_time", end_time);
            buffer.append("  结束时间：")
                    .append(end_time);
        }

        Excel excel = new Excel();
        excel.setFilePath(filePath);
        excel.setTitle("USSD发送统计");
        excel.addRemark(buffer.toString());
        excel.addHeader(20, "客户ID", "clientid");
        excel.addHeader(20, "客户名称", "name");
        excel.addHeader(20, "客户注册时间", "createtime");
        excel.addHeader(20, "所属代理商ID", "agent_id");
        excel.addHeader(20, "代理商名称", "agent_name");
        excel.addHeader(20, "代理商类型", "agent_type_name");
        excel.addHeader(20, "归属消息", "belong_sale_name");
        excel.addHeader(20, "提交条数", "cusSubTotal");
        excel.addHeader(20, "成功条数", "successTotal");
        excel.addHeader(20, "失败条数", "failTotal");
        excel.addHeader(20, "计费条数", "chargetotal");
        excel.addHeader(20, "待发送条数", "notsend");
        excel.addHeader(20, "未知条数", "submitsuccess");
        excel.addHeader(20, "消费金额(元)", "salefee");
        excel.addHeader(20, "消耗成本(元)", "productfee");
        excel.addHeader(20, "通道成本(元)", "costfee");
        

        List<Map<String, Object>> data = smsReportService.ussdSmsExcel(params);
    	Map<String,Object> paramsObj = new HashMap<>();
		paramsObj.putAll(params);
		List<Integer> product_types = new ArrayList<>();
		product_types.add(7);
		paramsObj.put("product_types", product_types); // 按产品类型统计 0：行业，1：营销，2：国际，7：USSD，8：闪信，9：挂机短信 
        Map totalData = smsReportService.smsTotal(paramsObj);
        totalData.put("agent_id", "总计");
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

    /**
     * 闪信-页面视图
     */
    @RequestMapping("/flashSms/query")
    public String flashSMSQuery(Model model) {
        model.addAttribute("max_export_num", ConfigUtils.max_export_excel_num);
        return "smsReport/flashSms/list";
    }

    /**
     * 闪信-查询页面分页数据
     *
     * @param rows       行数
     * @param page       当前查询页码
     * @param condition  条件
     * @param start_time 开始时间
     * @param end_time   结束时间
     * @return
     */
    @RequestMapping("/flashSms/list")
    @ResponseBody
    public PageContainer flashSMSPageList(String rows, String page, String condition, String start_time, String end_time) {
        Map<String, String> params = new HashMap<>();
        params.put("pageRowCount", rows);
        params.put("currentPage", page);

        // 客户Id/客户名称/代理商ID
        params.put("text", condition);

        if (start_time == null || end_time == null) {
            DateTime dt = DateTime.now();
            start_time = dt.minusDays(1).toString("yyyy-MM-dd");
            end_time = dt.minusDays(1).toString("yyyy-MM-dd");
        }

        params.put("start_time", start_time.replace("-", ""));
        params.put("end_time", end_time.replace("-", ""));

        return smsReportService.flashSmsQuery(params);
    }
    
    /**
     * 闪信短信-查询 总计栏
     */
    @RequestMapping("/flashSms/total")
    @ResponseBody
    public ResultVO flashSmsTotal(String condition, String start_time, String end_time) {
    	ResultVO resultVO = ResultVO.failure();
		Map<String, Object> params = new HashMap<>();
		
		// 客户Id/客户名称/代理商ID
		params.put("text", condition);
		
		if (start_time == null || end_time == null) {
		    DateTime dt = DateTime.now();
		    start_time = dt.minusDays(1).toString("yyyy-MM-dd");
		end_time = dt.minusDays(1).toString("yyyy-MM-dd");
		}
		
		params.put("start_time", start_time.replace("-", ""));
		params.put("end_time", end_time.replace("-", ""));


		List<Integer> product_types = new ArrayList<>();
		product_types.add(8);
		params.put("product_types", product_types); // 按产品类型统计 0：行业，1：营销，2：国际，7：USSD，8：闪信，9：挂机短信 
        Map data = smsReportService.smsTotal(params);
        resultVO.setSuccess(true);
        resultVO.setData(data);
        resultVO.setMsg("操作成功！");
    	return resultVO;
    }

    /**
     * 闪信-查询页面数据导出
     *
     * @param condition  查询条件
     * @param start_time 开始时间
     * @param end_time   结束时间
     * @param request
     * @param response
     */
    @RequestMapping(path = "/flashSms/list/export")
    public void flashSMSPageListExport(String condition, String start_time, String end_time, HttpServletRequest request, HttpServletResponse response) {
        String filePath = ConfigUtils.save_path + "/闪信发送统计_" + DateTime.now().toString("yyyyMMddHHmmss") + ".xls";
        Map<String, String> params = new HashMap<String, String>();

        StringBuffer buffer = new StringBuffer();
        buffer.append("查询条件 -> ");
        if (StringUtils.isNotBlank(condition)) {
            params.put("text", condition);
            buffer.append("客户ID/客户名称/代理商ID ：")
                    .append(condition)
                    .append(";");
        }

        if (StringUtils.isNotBlank(start_time)) {
            params.put("start_time", start_time);
            buffer.append("  开始时间：")
                    .append(start_time)
                    .append(";");
        }

        if (StringUtils.isNotBlank(end_time)) {
            params.put("end_time", end_time);
            buffer.append("  结束时间：")
                    .append(end_time);
        }

        Excel excel = new Excel();
        excel.setFilePath(filePath);
        excel.setTitle("闪信发送统计");
        excel.addRemark(buffer.toString());
        excel.addHeader(20, "客户ID", "clientid");
        excel.addHeader(20, "客户名称", "name");
        excel.addHeader(20, "客户注册时间", "createtime");
        excel.addHeader(20, "所属代理商ID", "agent_id");
        excel.addHeader(20, "代理商名称", "agent_name");
        excel.addHeader(20, "代理商类型", "agent_type_name");
        excel.addHeader(20, "归属销售", "belong_sale_name");
        excel.addHeader(20, "提交条数", "cusSubTotal");
        excel.addHeader(20, "成功条数", "successTotal");
        excel.addHeader(20, "失败条数", "failTotal");
        excel.addHeader(20, "计费条数", "chargetotal");
        excel.addHeader(20, "待发送条数", "notsend");
        excel.addHeader(20, "未知条数", "submitsuccess");
        excel.addHeader(20, "消费金额(元)", "salefee");
        excel.addHeader(20, "消耗成本(元)", "productfee");
        excel.addHeader(20, "通道成本(元)", "costfee");
        List<Map<String, Object>> data = smsReportService.flashSmsExcel(params);
    	Map<String,Object> paramsObj = new HashMap<>();
		paramsObj.putAll(params);
		List<Integer> product_types = new ArrayList<>();
		product_types.add(8);
		paramsObj.put("product_types", product_types); // 按产品类型统计 0：行业，1：营销，2：国际，7：USSD，8：闪信，9：挂机短信 
        Map totalData = smsReportService.smsTotal(paramsObj);
        totalData.put("agent_id", "总计");
		data.add(totalData);
		excel.setDataList(data );

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
