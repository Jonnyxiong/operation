package com.ucpaas.sms.controller;

import com.jsmsframework.common.dto.Result;
import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.common.enums.CodeEnum;
import com.jsmsframework.user.entity.JsmsDepartment;
import com.jsmsframework.user.entity.JsmsMenu;
import com.jsmsframework.user.service.JsmsDepartmentService;
import com.ucpaas.sms.annotation.JSON;
import com.ucpaas.sms.common.util.file.ExcelUtils;
import com.ucpaas.sms.common.util.file.FileUtils;
import com.ucpaas.sms.dto.AccessStatisticVO;
import com.ucpaas.sms.dto.ClientConsumeVO;
import com.ucpaas.sms.dto.ClientConsumeVO2Point3;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.entity.access.AccessSendStat;
import com.ucpaas.sms.entity.message.Department;
import com.ucpaas.sms.entity.record.RecordConsumeStat;
import com.ucpaas.sms.model.Excel;
import com.ucpaas.sms.model.Page;
import com.ucpaas.sms.service.account.AccountGroupService;
import com.ucpaas.sms.service.admin.AuthorityService;
import com.ucpaas.sms.service.statistic.ChannelConsumeService;
import com.ucpaas.sms.service.statistic.ClientConsumeService;
import com.ucpaas.sms.service.statistic.TotalConsumeService;
import com.ucpaas.sms.util.AgentUtils;
import com.ucpaas.sms.util.ConfigUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.*;

/**
 * Created by dylan on 2017/7/25.
 */

@Controller
@RequestMapping("/statistic")
public class StatisticController extends BaseController{
    private static Logger logger = LoggerFactory.getLogger(StatisticController.class);
    @Autowired
    private TotalConsumeService totalConsumeService;

    @Autowired
    private ClientConsumeService clientConsumeService;
    @Autowired
    private ChannelConsumeService channelConsumeService;
    @Autowired
    private AccountGroupService accountGroupService;
    @Autowired
    private JsmsDepartmentService jsmsDepartmentService;
    @Autowired
    private AuthorityService authorityService;

    /**
     * 短信总消耗报表 --> route
     */
    @GetMapping("/totalConsume")
    public ModelAndView totalConsumePage(ModelAndView mv) {
        mv.setViewName("statistic/totalConsume");
        return mv;
    }
    /**
     * 近三月消耗对比 --> interface
     */
    @PostMapping("/consumePerMonth")
//    @ResponseBody
    @JSON(type = ClientConsumeVO.class, include="chargeTotal,profit,salefeeStr,date,dateStr,chargeTotal,auditfail,costfee,costfeeStr,failTotal")
    public ResultVO consumePerMonth(@RequestParam Map params){

        String start = (String) params.get("startTime");
        String end = (String) params.get("endTime");

        if(StringUtils.isEmpty(start) ){
            params.put("startTime",DateTime.now().withDayOfMonth(1).minusMonths(2).toString("yyyy-MM-dd"));
            params.put("start",DateTime.now().minusMonths(2).toString("yyyy-MM"));
        }else{
            params.put("startTime",DateTime.parse(start).toString("yyyy-MM-dd"));
            params.put("start",start);

        }
        if(StringUtils.isEmpty(end)){
            params.put("endTime",DateTime.now().toString("yyyy-MM-dd"));
            params.put("end",DateTime.now().toString("yyyy-MM"));
        }else{
            params.put("endTime",DateTime.parse(end).plusMonths(1).minusDays(1).toString("yyyy-MM-dd"));
            params.put("end",end);
        }
        convertDateParams(params);
        params.put("groupParams","date,agent_id");
        return ResultVO.successDefault(totalConsumeService.queryMonthSumList(params));
    }

    /**
     * 消耗趋势 --> interface
     */
    @PostMapping("/consumeTrend")
//    @ResponseBody
    @JSON(type = ClientConsumeVO.class, filter="accountGid,accountGname,agentName,agentType,agentTypeStr,belongSaleStr,clientCreateTime,clientName,departmentStr,returnTotalAmount,returnTotalNumber")
    public ResultVO consumeTrend(@RequestParam Map params){
        String date = (String) params.get("date");

        if(date == null){
            params.put("date",DateTime.now().toString("yyyy-MM"));
        }
        converMonthToRange(params,1);
        params.remove("date");
        convertDateParams(params);
        params.put("groupParams","date");
        return ResultVO.successDefault(totalConsumeService.querySumList(params));
    }
    /**
     * 各BU消耗占比 --> interface
     */
    @PostMapping("/consumeVsByBU")
//    @ResponseBody
    @JSON(type = ClientConsumeVO.class, filter="accountGid,accountGname,agentName,agentType,agentTypeStr,belongSale,belongSaleStr,clientCreateTime,clientName,departmentId,departmentStr,returnTotalAmount,returnTotalNumber")
    public ResultVO consumeVsByBU(@RequestParam Map params){

        if(params.get("date") == null){
            params.put("date",DateTime.now().minusDays(1).toString("yyyy-MM"));
        }
        converMonthToRange(params,0);
        params.remove("date");
        convertDateParams(params);
        params.put("groupParams","department_id");
        return ResultVO.successDefault(totalConsumeService.consumeVsByBU(params)); // todo
    }

    /**
     * TOP10客户发送量 --> interface
     */
    @PostMapping("/topClients")
//    @ResponseBody
    @JSON(type = ClientConsumeVO.class, filter="agentName,agentType,agentTypeStr,clientName,returnTotalAmount,returnTotalNumber")
    public ResultVO topClients(@RequestParam Map params){
        if(params.get("date") == null){
            params.put("date",DateTime.now().toString("yyyy-MM"));
        };
        converMonthToRange(params,0);
        params.remove("date");
        convertDateParams(params);
        return totalConsumeService.topClients(params);
    }
    /**
     * TOP10通道消耗量 --> interface
     */
    @PostMapping("/topChannels")
    @ResponseBody
    public ResultVO topChannels(@RequestParam Map params){
        if(params.get("date") == null){
            params.put("date",DateTime.now().toString("yyyy-MM"));
        }
        converMonthToRange(params,0);
        params.remove("date");
        convertDateParams(params);
        return totalConsumeService.topChannels(params);
    }

    /**
     * 回款金额占比 --> interface
     */
    @PostMapping("/consumeVsByPay")
    @ResponseBody
    public ResultVO consumeVsByPay(@RequestParam Map params){
        if(params.get("date") == null){
            params.put("date",DateTime.now().toString("yyyy-MM"));
        }
        converMonthToRange(params,0);
        params.remove("date");
        convertDateParams(params);
        return totalConsumeService.consumeVsByPay(params);
    }

    /**
     * 客户短信消耗报表 --> route
     */
    @GetMapping("/clientConsume")
    @Deprecated
    public ModelAndView clientConsumePage(ModelAndView mv, HttpSession session) {

        mv.addObject("max_export_num", ConfigUtils.max_export_excel_num);
        mv.setViewName("statistic/clientConsume");

        UserSession user = getUserFromSession(session);
        mv.addObject("menus", AgentUtils.hasMenuRight(user, "代理商子客户消耗报表-dlszkhxhbb", "直客消耗报表-zkxhbb"));
        return mv;
    }
    /**
     * 客户短信消耗报表 --> interface
     *  todo 增加数据权限
     */
    @PostMapping("/clientConsume")
    @ResponseBody
    @Deprecated
    public Page getClientConsume(Page<AccessSendStat> page, @RequestParam Map params, HttpSession session){
        // 从Session中取出用户Id
        params.put("userId", getUserFromSession(session).getId().toString());
        convertDateParams(params);
//        params.put("stattype",0); // 统计类型, 0:每日,2:每月
        params.put("existAgent",true); // 是否是代理商子客户, 直客没有代理商
//        params.put("groupParams","date,clientid,paytype,smstype,agent_id,belong_sale"); // 统计类型, 0:每日,2:每月
        params.put("isOrderByChargeTotal", true);   // 是否需要计费条数排序
        params.put("groupParams","clientid,paytype,smstype,agent_id,belong_sale"); // 统计类型, 0:每日,2:每月 todo 不需要日期分组
        page.setParams(params);
        page.setOrderByClause("chargeTotal DESC");
        page = clientConsumeService.queryPage(page);
        return page;
    }



    /**
     * 需求2.3的
     * 客户短信消耗报表(普通) --> route
     */
    @GetMapping("/clientConsume2point3")
    public ModelAndView clientConsume2point3Page(ModelAndView mv, HttpSession session) {

        mv.addObject("max_export_num", ConfigUtils.max_export_excel_num);
        mv.setViewName("statistic/clientConsume2point3");

        UserSession user = getUserFromSession(session);
        mv.addObject("menus", AgentUtils.hasMenuRight(user, "普通短信消耗报表-ptdxxhbb", "国际短信消耗报表-gjdxxhbb"));
        JsmsMenu jsmsMenu = authorityService.getJsmsMenu("普通短信消耗报表");
        mv.addObject("jsmsMenu" , jsmsMenu);
        return mv;
    }
    /**
     * 需求2.3的
     * 客户短信消耗报表(普通)  --> interface
     *  todo 增加数据权限
     */
    @PostMapping("/clientConsume2point3")
    @ResponseBody
    public Page getClientConsume2point3(Page page, String clientType,String condition,String smstype,String departmentId,String paytype,String startTime,String endTime, String operatorCode, HttpSession session){
        Map params = new HashMap();
        // 从Session中取出用户Id
        params.put("userId", getUserFromSession(session).getId().toString());
        //客户类型
        if(StringUtils.isNotBlank(clientType)) {
            params.put("clientType", clientType);
            if("1".equals(clientType)) {
                // 是否是代理商子客户, 直客没有代理商
                params.put("existAgent", true);
            } else{
                // 是否是代理商子客户, 直客没有代理商
                params.put("existAgent", false);
            }

        }
        //客户ID/客户名称/代理商ID/销售名字
        if(StringUtils.isNotBlank(condition)) {
            params.put("condition", condition);
        }
        //短信类型
        if(StringUtils.isNotBlank(smstype)) {
            params.put("smstype", smstype);
        }
        // 运营商类型
        if(StringUtils.isNotBlank(operatorCode)) {
            params.put("operatorstype", operatorCode);
        }
        //部门
        if(StringUtils.isNotBlank(departmentId)) {
            params.put("departmentId", departmentId);
        }
        //付费类型
        if(StringUtils.isNotBlank(paytype)) {
            params.put("paytype", paytype);
        }
        //统计开始时间
        if(StringUtils.isNotBlank(startTime)) {
            params.put("startTime", Integer.parseInt(startTime.replace("-", "")));
            if(startTime.matches("^\\d{4}-\\d{2}-\\d{2}$")){
                params.put("stattype", 0);
            }else if(startTime.matches("^\\d{4}-\\d{2}$")){
                params.put("stattype", 2);
            }
        }
        if(StringUtils.isNotBlank(endTime)) {//统计结束时间
            params.put("endTime", Integer.parseInt(endTime.replace("-", "")));
            if(endTime.matches("^\\d{4}-\\d{2}-\\d{2}$")){
                params.put("stattype", 0);
            }else if(endTime.matches("^\\d{4}-\\d{2}$")){
                params.put("stattype", 2);
            }
        }


//        convertDateParams(params);
        params.put("operatorstypeExclude", Arrays.asList(4)); //operatorstypeExclude不包含， 通道运营商类型，0：全网，1：移动，2：联通，3：电信，4：国际，-1：拦截
        page.setParams(params);
        page = clientConsumeService.getClientConsume2point3(page);
        return page;
    }


    /**
     * 【下载】报表 --> interface
     */
    @PostMapping("/downloadExcel")
    @ResponseBody
    public ResponseEntity<byte[]> downloadExcel(String exportFileName,HttpSession session) throws IOException {

        File downloadFile = new File(exportFileName);
        /**
         * 截取文件名称
         */
        String fileName = URLEncoder.encode(
                exportFileName.substring(exportFileName.lastIndexOf("/")+1,exportFileName.lastIndexOf("$$$")),"UTF-8");
        /**
         * 读取文件, 读取后删除
         */
        byte[] fileBytes = org.apache.commons.io.FileUtils.readFileToByteArray(downloadFile);
        FileUtils.delete(exportFileName);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        logger.debug("下载报表fileName={}", exportFileName);
        headers.setContentDispositionFormData("attachment", new String(fileName.getBytes(), "utf-8"));
        return new ResponseEntity<byte[]>(fileBytes , headers, HttpStatus.CREATED);
     }

    /**
     * 【生成】代理商子客户短信消耗报表 --> interface
     */
    @PostMapping("/exportClientConsume")
    @ResponseBody
    public ResultVO exportClientConsume(Page page, @RequestParam Map params,HttpSession session ){
        // 从Session中取出用户Id
        params.put("userId", getUserFromSession(session).getId().toString());
        params.put("existAgent",true); // 是否是代理商子客户, 直客没有代理商
//        params.put("groupParams","date,clientid,paytype,smstype,agent_id,belong_sale"); // 统计类型, 0:每日,2:每月
        params.put("isOrderByChargeTotal", true);   // 是否需要计费条数排序
        params.put("groupParams","clientid,paytype,smstype,agent_id,belong_sale"); // 统计类型, 0:每日,2:每月 todo 不需要日期分组
        page.setParams(params);
        page.setOrderByClause("chargeTotal DESC");
        ResultVO resultVO = ResultVO.failure("生成报表失败");
        try {
            StringBuffer filePath = new StringBuffer(ConfigUtils.save_path);
            if(!ConfigUtils.save_path.endsWith("/")){
                filePath.append("/");
            }
            filePath.append("代理商子客户消耗报表")
                    .append(".xls")
                    .append("$$$")
                    .append(UUID.randomUUID());
            StringBuilder builder = new StringBuilder("查询条件 -> ")
                        .append("部门：").append(params.get("_department")).append("；")
                        .append("短信类型：").append(params.get("_smstype")).append("；")
                        .append("付费类型：").append(params.get("_paytype")).append("；")
                        .append("开始时间：").append(params.get("startTime")).append("；")
                        .append("结束时间：").append(params.get("endTime"))
//                        .append("客户ID/客户名称/代理商ID/销售名字：").append(params.get("_condition")).append("；")
                    ;
            Excel excel = new Excel();
            excel.setFilePath(filePath.toString());
            excel.setTitle("代理商子客户消耗报表");
            excel.addRemark(builder.toString());
            excel.addHeader(20, "部门", "departmentStr");
            excel.addHeader(20, "客户ID", "clientid");
            excel.addHeader(20, "客户名称", "accountGname");
            excel.addHeader(20, "客户注册时间", "clientCreateTimeStr");
            excel.addHeader(20, "所属代理商ID", "agentId");
            excel.addHeader(20, "代理商名称", "agentName");
            excel.addHeader(20, "代理商类型", "agentTypeStr");
            excel.addHeader(20, "归属销售", "belongSaleStr");
            excel.addHeader(20, "付费类型", "paytypeStr");
            excel.addHeader(20, "短信类型", "smstypeStr");
            excel.addHeader(20, "提交条数（条）", "submitTotal");
            excel.addHeader(20, "计费条数（条）", "chargeTotal");
            excel.addHeader(20, "成功率（%）", "successRate");
            excel.addHeader(20, "成功条数（条）", "reportsuccess");
            excel.addHeader(20, "未知条数", "submitsuccess");
            excel.addHeader(20, "失败条数（条）", "failTotal");
            excel.addHeader(20, "待发送条数（条）", "notsend");
            excel.addHeader(20, "拦截条数（条）", "interceptTotal");
            excel.addHeader(20, "消费金额(元)", "salefeeStr");
            excel.addHeader(20, "通道成本(元)", "costfeeStr");
            excel.addHeader(20, "毛利（元", "profit");
            excel.addHeader(20, "毛利率（%)", "profitRate");

            convertDateParams(params);
            resultVO  = clientConsumeService.exportPage(page,excel);

        } catch (Exception e) {
            logger.error("生成报表失败", e);
        }
        return resultVO;
    }

    /**
     * 【生成】直客户短信消耗报表 --> interface
     */
    @PostMapping("/exportDirectClientConsume")
    @ResponseBody
    public ResultVO exportDirectClientConsume(Page page, @RequestParam Map params,HttpSession session){
        // 从Session中取出用户Id
        params.put("userId", getUserFromSession(session).getId().toString());

        params.put("existAgent",false); // 统计类型, 0:每日,2:每月
//        params.put("groupParams","date,clientid,belong_sale"); // 统计类型, 0:每日,2:每月
        params.put("isOrderByChargeTotal", true);   // 是否需要计费条数排序
        params.put("groupParams","clientid,smstype,belong_sale"); // 统计类型, 0:每日,2:每月 todo 不需要日期分组
        page.setParams(params);
        page.setOrderByClause("chargeTotal DESC");
        ResultVO resultVO = ResultVO.failure("生成报表失败");
        try {
            StringBuffer filePath = new StringBuffer(ConfigUtils.save_path);
            if(!ConfigUtils.save_path.endsWith("/")){
                filePath.append("/");
            }
            filePath.append("直客消耗报表")
                    .append(".xls")
                    .append("$$$")
                    .append(UUID.randomUUID());
            StringBuilder builder = new StringBuilder("查询条件 -> ")
                    .append("部门：").append(params.get("_department")).append("；")
                    .append("短信类型：").append(params.get("_smstype")).append("；")
                    .append("开始时间：").append(params.get("startTime")).append("；")
                    .append("结束时间：").append(params.get("endTime"))
//                    .append("客户ID/客户名称/销售名字：").append(params.get("_condition")).append("；")
                    ;
            Excel excel = new Excel();
            excel.setFilePath(filePath.toString());
            excel.setTitle("直客消耗报表");
            excel.addRemark(builder.toString());
            excel.addHeader(20, "部门", "departmentStr");
            excel.addHeader(20, "客户ID", "clientid");
            excel.addHeader(20, "客户名称", "accountGname");
            excel.addHeader(20, "客户注册时间", "clientCreateTimeStr");
            excel.addHeader(20, "归属销售", "belongSaleStr");
            excel.addHeader(20, "付费类型", "paytypeStr");
            excel.addHeader(20, "短信类型", "smstypeStr");
            excel.addHeader(20, "提交条数（条）", "submitTotal");
            excel.addHeader(20, "计费条数（条）", "chargeTotal");
            excel.addHeader(20, "成功率（%）", "successRate");
            excel.addHeader(20, "成功条数（条）", "reportsuccess");
            excel.addHeader(20, "未知条数", "submitsuccess");
            excel.addHeader(20, "失败条数（条）", "failTotal");
            excel.addHeader(20, "待发送条数（条）", "notsend");
            excel.addHeader(20, "拦截条数（条）", "interceptTotal");
            excel.addHeader(20, "消费金额(元)", "salefeeStr");
            excel.addHeader(20, "通道成本(元)", "costfeeStr");
            excel.addHeader(20, "毛利（元", "profit");
            excel.addHeader(20, "毛利率（%)", "profitRate");

            convertDateParams(params);
            resultVO  = clientConsumeService.exportPage(page,excel);

        } catch (Exception e) {
            logger.error("生成报表失败", e);
        }
        return resultVO;
    }


    /**
     * 【生成】通道短信消耗报表 --> interface
     */
    @PostMapping("/exportChannelConsume")
    @ResponseBody
    public ResultVO exportChannelConsume(Page page, @RequestParam Map params,HttpSession session){
        // 从Session中取出用户Id
        params.put("userId", getUserFromSession(session).getId().toString());

//        params.put("existAgent",false); // 统计类型, 0:每日,2:每月
//        params.put("groupParams","date,channelid,belong_business"); // 统计类型, 0:每日,2:每月
        params.put("isOrderByChargeTotal", true);   // 是否需要计费条数排序
        params.put("groupParams","department_id,channelid,smstype,belong_business");  // 统计类型, 0:每日,2:每月 todo 不需要日期分组
        page.setParams(params);
        page.setOrderByClause("chargeTotal DESC");
        ResultVO resultVO = ResultVO.failure("生成报表失败");
        try {
            StringBuffer filePath = new StringBuffer(ConfigUtils.save_path);
            if(!ConfigUtils.save_path.endsWith("/")){
                filePath.append("/");
            }
            filePath.append("通道消耗报表")
                    .append(".xls")
                    .append("$$$")
                    .append(UUID.randomUUID());

            StringBuilder builder = new StringBuilder("查询条件 -> ")
                    .append("部门：").append(params.get("_department")).append("；")
                    .append("所属商务：").append(params.get("_belongBusiness")).append("；")
                    .append(" 运营商类型：").append( params.get("_operatorstype")).append("；")
                    .append("短信类型：").append(params.get("_smstype")).append("；")
                    .append("开始时间：").append(params.get("startTime")).append("；")
                    .append("结束时间：").append(params.get("endTime")).append("；")
                    .append("通道号：").append(params.get("_condition")).append("；")
                    ;
            Excel excel = new Excel();
            excel.setFilePath(filePath.toString());
            excel.addRemark(builder.toString());
            excel.setTitle("通道消耗报表");
            excel.addHeader(20, "部门", "departmentStr");
            excel.addHeader(20, "运营商类型", "operatorstypeStr");
            excel.addHeader(20, "通道号", "channelid");
            excel.addHeader(20, "归属商务", "belongBusinessStr");
            excel.addHeader(20, "通道备注", "remark");
            excel.addHeader(20, "短信类型", "smstypeStr");
            excel.addHeader(20, "总发送量（条）", "sendTotal");
            excel.addHeader(20, "通道计费条数（条）", "chargeTotal");
            excel.addHeader(20, "通道单价（元）", "costpriceStr"); /*todo 多个单价, 无法展示,先隐藏*/
            excel.addHeader(20, "成功率（%）", "successRate");
            excel.addHeader(20, "成功量（条）", "successTotal");
            excel.addHeader(20, "提交成功（条）", "submitsuccess");
            excel.addHeader(20, "发送成功（条）", "subretsuccess");
            excel.addHeader(20, "明确成功（条）", "reportsuccess");
            excel.addHeader(20, "失败条数（条）", "failTotal");
            excel.addHeader(20, "销售收入(元)", "saletotalStr");
            excel.addHeader(20, "通道成本(元)", "costtotalStr");
            excel.addHeader(20, "毛利（元", "profit");
            excel.addHeader(20, "毛利率（%)", "profitRate");

            convertDateParams(params);
            resultVO  = channelConsumeService.exportPage(page,excel);

        } catch (Exception e) {
            logger.error("生成报表失败", e);
        }
        return resultVO;
    }

    /**
     * 【生成】单个客户通道消耗详情报表 --> interface
     */
    @PostMapping("/exportSendDetail")
    @ResponseBody
    public ResultVO exportSendDetail(Page page, @RequestParam(required=false) String isInter, @RequestParam Map params,HttpSession session){
        // 从Session中取出用户Id
        params.put("userId", getUserFromSession(session).getId().toString());

        ResultVO resultVO = ResultVO.failure("生成报表失败");
        if(params.get("clientid") == null){
            return resultVO;
        }
//        params.put("stattype",0); // 统计类型, 0:每日,2:每月
//        params.put("existAgent",true); // 是否是代理商子客户, 直客没有代理商
        params.put("groupParams","date,paytype,smstype"); // 统计类型, 0:每日,2:每月

        params.put("operatorstypeExclude", Arrays.asList(4)); //operatorstypeExclude不包含， 通道运营商类型，0：全网，1：移动，2：联通，3：电信，4：国际，-1：拦截
        if (StringUtils.isNotBlank(isInter)) {
            if (isInter.equals("true")) {
                params.put("operatorstypeExclude", Arrays.asList(0, 1, 2, 3, -1)); //operatorstypeExclude不包含， 通道运营商类型，0：全网，1：移动，2：联通，3：电信，4：国际，-1：拦截
            }
        }
        page.setParams(params);
        page.setOrderByClause("date DESC");
        try {
            StringBuilder filePath = new StringBuilder(ConfigUtils.save_path);
            if(!ConfigUtils.save_path.endsWith("/")){
                filePath.append("/");
            }
            filePath.append(params.get("_clientName"))  // todo 客户名称
                    .append("消耗详情报表")
                    .append(".xls")
                    .append("$$$")
                    .append(UUID.randomUUID());
            StringBuilder builder = new StringBuilder("查询条件 -> ")
                    .append("短信类型：").append(params.get("_smstype")).append("；")
                    .append("付费类型：").append(params.get("_paytype")).append("；")
                    .append("开始时间：").append(params.get("startTime")).append("；")
                    .append("结束时间：").append(params.get("endTime"))
                    ;
            Excel excel = new Excel();
            excel.addRemark(builder.toString());
            excel.setFilePath(filePath.toString());
            excel.setTitle(new StringBuilder((String) params.get("_clientName")).append("消耗详情报表").toString());// todo 客户名称
            excel.addHeader(20, "日期", "dateStr");
//            excel.addHeader(20, "付费类型", "paytypeStr");
            excel.addHeader(20, "短信类型", "smstypeStr");
            excel.addHeader(20, "提交条数（条）", "submitTotal");
            excel.addHeader(20, "计费条数（条）", "chargeTotal");
            excel.addHeader(20, "成功率（%）", "successRate");
            excel.addHeader(20, "成功条数（条）", "reportsuccess");
            excel.addHeader(20, "未知条数", "submitsuccess");
            excel.addHeader(20, "失败条数（条）", "failTotal");
            excel.addHeader(20, "待发送条数（条）", "notsend");
            excel.addHeader(20, "拦截条数（条）", "interceptTotal");
            excel.addHeader(20, "消费金额(元)", "salefeeStr");
            excel.addHeader(20, "通道成本(元)", "costfeeStr");
            excel.addHeader(20, "毛利（元", "profit");
            excel.addHeader(20, "毛利率（%)", "profitRate");

            convertDateParams(params);
            resultVO  = clientConsumeService.exportPage(page,excel);

        } catch (Exception e) {
            logger.error("生成报表失败", e);
        }
        return resultVO;
    }


    /**
     * 【生成】客户通道消耗详情报表 --> interface
     */
    @PostMapping("/exportChannelDetail")
    @ResponseBody
    public ResultVO exportChannelDetail(Page page, @RequestParam(required=false) String isInter, @RequestParam Map params,HttpSession session){
        // 从Session中取出用户Id
        params.put("userId", getUserFromSession(session).getId().toString());

        ResultVO resultVO = ResultVO.failure("生成报表失败");
        if(params.get("clientid") == null){
            return resultVO;
        }
//        params.put("stattype",0); // 统计类型, 0:每日,2:每月
//        params.put("existAgent",false); // 统计类型, 0:每日,2:每月
        params.put("excludeIntercept", true);
        params.put("groupParams","date,channelid,smstype,paytype"); // 统计类型, 0:每日,2:每月

        params.put("operatorstypeExclude", Arrays.asList(4)); //operatorstypeExclude不包含， 通道运营商类型，0：全网，1：移动，2：联通，3：电信，4：国际，-1：拦截
        if (StringUtils.isNotBlank(isInter)) {
            if (isInter.equals("true")) {
                params.put("operatorstypeExclude", Arrays.asList(0, 1, 2, 3, -1)); //operatorstypeExclude不包含， 通道运营商类型，0：全网，1：移动，2：联通，3：电信，4：国际，-1：拦截
            }
        }
        page.setParams(params);
        page.setOrderByClause("date DESC");
        try {
            StringBuilder filePath = new StringBuilder(ConfigUtils.save_path);
            if(!ConfigUtils.save_path.endsWith("/")){
                filePath.append("/");
            }
            filePath.append(params.get("_clientName"))  // todo 客户名称
                    .append("通道消耗详情报表")
                    .append(".xls")
                    .append("$$$")
                    .append(UUID.randomUUID());
            StringBuilder builder = new StringBuilder("查询条件 -> ")
                    .append("短信类型：").append(params.get("_smstype")).append("；")
                    .append("付费类型：").append(params.get("_paytype")).append("；")
                    .append("开始时间：").append(params.get("startTime")).append("；")
                    .append("结束时间：").append(params.get("endTime"))
                    ;
            Excel excel = new Excel();
            excel.addRemark(builder.toString());
            excel.setFilePath(filePath.toString());
            excel.setTitle(new StringBuilder((String) params.get("_clientName")).append("通道消耗详情报表").toString());// todo 客户名称
            excel.addHeader(20, "日期", "dateStr");
            excel.addHeader(20, "运营商类型", "operatorstypeStr");
            excel.addHeader(20, "通道号", "channelid");
            excel.addHeader(20, "归属商务", "belongBusinessStr");
            excel.addHeader(20, "通道备注", "remark");
            excel.addHeader(20, "付费类型", "paytypeStr");
            excel.addHeader(20, "短信类型", "smstypeStr");
            excel.addHeader(20, "总发送量（条）", "sendtotal");
            excel.addHeader(20, "成功率（%）", "successRate");
            excel.addHeader(20, "成功量（条）", "successTotal");
            excel.addHeader(20, "提交成功（条）", "submitsuccess");
            excel.addHeader(20, "明确成功（条）", "reportsuccess");
            excel.addHeader(20, "明确失败（条）", "reportfail");
            excel.addHeader(20, "销售收入(元)", "salefeeStr");
//            excel.addHeader(20, "通道单价（元）", "costpriceStr");
            excel.addHeader(20, "通道计费条数（条）", "recordChargeTotal");
            excel.addHeader(20, "通道成本(元)", "recordCosttotalStr");
            excel.addHeader(20, "毛利（元", "profit");
            excel.addHeader(20, "毛利率（%)", "profitRate");

            convertDateParams(params);
            resultVO  = channelConsumeService.exportPage4BasicStatis(page,excel);

        } catch (Exception e) {
            logger.error("生成报表失败", e);
        }
        return resultVO;
    }


    /**
     * 单个客户消耗详情（代理商子客户） --> route
     */
    @GetMapping("/sendDetail")
    public ModelAndView sendDetailPage(ModelAndView mv, @RequestParam(required=false) String isInter, HttpSession session,@RequestParam Map<String,String> params) {
        // 从Session中取出用户Id
        params.put("userId", getUserFromSession(session).getId().toString());
        /*String clientid = request.getParameter("clientid");
        Map info = accountGroupService.getByClientId(clientid);
        if (info != null && !StringUtils.isEmpty((String) info.get("accountGname"))){
            mv.addObject("clientName", info.get("accountGname"));
        }else if(info != null && !StringUtils.isEmpty((String) info.get("clientName"))){
            mv.addObject("clientName", info.get("clientName"));
        }*/
        convertUnicode(params);
        params.put("isInter","false");
        if (StringUtils.isNotBlank(isInter)) {
            if (isInter.equals("true")) {
                params.put("isInter",isInter);
            }
        }
        params.put("max_export_num", ConfigUtils.max_export_excel_num);
        mv.setViewName("statistic/sendDetail");
        mv.addAllObjects(params);
        return mv;
    }
    /**
     * 单个客户短信消耗报表 --> interface
     */
    @PostMapping("/sendDetail")
    @ResponseBody
    public Page getSendDetail(Page<AccessSendStat> page, @RequestParam(required=false) String isInter, @RequestParam Map params,HttpSession session){
        // 从Session中取出用户Id
        params.put("userId", getUserFromSession(session).getId().toString());

        if(params.get("clientid") == null){
            return page;
        }
        convertDateParams(params);
//        params.put("stattype",0); // 统计类型, 0:每日,2:每月
//        params.put("existAgent",true); // 是否是代理商子客户, 直客没有代理商
        params.put("groupParams","date,paytype,smstype,charge_rule"); // 统计类型, 0:每日,2:每月

        Map params2 = new HashMap(params);
        params2.put("operatorstypeExclude", Arrays.asList(4)); //operatorstypeExclude不包含， 通道运营商类型，0：全网，1：移动，2：联通，3：电信，4：国际，-1：拦截
        if (StringUtils.isNotBlank(isInter)) {
            if (isInter.equals("true")) {
                params2.put("operatorstypeExclude", Arrays.asList(0, 1, 2, 3, -1)); //operatorstypeExclude不包含， 通道运营商类型，0：全网，1：移动，2：联通，3：电信，4：国际，-1：拦截
            }
        }

        page.setParams(params2);
        page.setOrderByClause("date DESC");
        page = clientConsumeService.queryPage(page);
        return page;
    }

    /**
     * 单个客户消耗详情（代理商子客户） --> route
     */
    @GetMapping("/channelDetail")
    public ModelAndView channelDetailPage(ModelAndView mv, @RequestParam(required=false) String isInter, HttpServletRequest request,@RequestParam Map<String,String> params) {
        // 从Session中取出用户Id
        params.put("userId", getUserFromSession(request.getSession()).getId().toString());

        String clientid = request.getParameter("clientid");
        Map info = accountGroupService.getByClientId(clientid);
        if (info != null && !StringUtils.isEmpty((String) info.get("accountGname"))){
            mv.addObject("clientName", info.get("accountGname"));
        }else if(info != null && !StringUtils.isEmpty((String) info.get("realname"))){
            mv.addObject("clientName", info.get("realname"));
        }
        convertUnicode(params);
        params.put("isInter","false");
        if (StringUtils.isNotBlank(isInter)) {
            if (isInter.equals("true")) {
                params.put("isInter",isInter);
            }
        }
        params.put("max_export_num", ConfigUtils.max_export_excel_num);
        params.put("clientid", clientid);
        mv.setViewName("statistic/channelDetail");
        mv.addAllObjects(params);
        return mv;
    }
    /**
     * 客户通道消耗详情 --> interface
     */
    @PostMapping("/channelDetail")
    @ResponseBody
    public Page<AccessStatisticVO> getChannelDetail(Page<AccessStatisticVO> page, @RequestParam(required=false) String isInter, @RequestParam Map params,HttpSession session){

        if(params.get("clientid") == null){
            return page;
        }
        // 从Session中取出用户Id
        params.put("userId", getUserFromSession(session).getId().toString());
        convertDateParams(params);

//        params.put("stattype",0); // 统计类型, 0:每日,2:每月
//        params.put("existAgent",false); // 统计类型, 0:每日,2:每月
        params.put("excludeIntercept", true);

        params.put("groupParams","operatorstype,date,channelid,smstype,paytype"); // 统计类型, 0:每日,2:每月


        Map params2 = new HashMap(params);
        params2.put("operatorstypeExclude", Arrays.asList(4)); //operatorstypeExclude不包含， 通道运营商类型，0：全网，1：移动，2：联通，3：电信，4：国际，-1：拦截
        if (StringUtils.isNotBlank(isInter)) {
            if (isInter.equals("true")) {
                params2.put("operatorstypeExclude", Arrays.asList(0, 1, 2, 3, -1)); //operatorstypeExclude不包含， 通道运营商类型，0：全网，1：移动，2：联通，3：电信，4：国际，-1：拦截
            }
        }
        page.setParams(params2);
        page.setOrderByClause("date DESC");
        page = channelConsumeService.queryPage4BasicStatis(page);
        return page;
    }

    /**
     * 直客短信消耗报表 --> route
     */
    @GetMapping("/directClientConsume")
    public ModelAndView directClientConsumePage(ModelAndView mv, HttpSession session) {
        mv.addObject("max_export_num", ConfigUtils.max_export_excel_num);
        mv.setViewName("statistic/directClientConsume");

        UserSession user = getUserFromSession(session);
        mv.addObject("menus", AgentUtils.hasMenuRight(user, "代理商子客户消耗报表-dlszkhxhbb", "直客消耗报表-zkxhbb"));
        return mv;
    }

    /**
     * 直客短信消耗报表 --> interface
     */
    @PostMapping("/directClientConsume")
    @ResponseBody
    public Page getDirectClientConsume(Page<AccessSendStat> page, @RequestParam Map params,HttpSession session){
        // 从Session中取出用户Id
        params.put("userId", getUserFromSession(session).getId().toString());

        convertDateParams(params);

//        params.put("stattype",0); // 统计类型, 0:每日,2:每月
        params.put("existAgent",false); // 是否是代理商子客户, 直客没有代理商
//        params.put("groupParams","date,clientid,belong_sale"); // 统计类型, 0:每日,2:每月
        params.put("isOrderByChargeTotal", true);   // 是否需要计费条数排序
        params.put("groupParams","clientid,smstype,belong_sale"); // 统计类型, 0:每日,2:每月 todo 不需要日期分组
        page.setParams(params);
        page.setOrderByClause("chargeTotal DESC");
        page = clientConsumeService.queryPage(page);
        return page;
    }

    /**
     * 通道短信消耗报表 --> route
     */
    @GetMapping("/channelConsume")
    public ModelAndView channelConsumePage(ModelAndView mv) {
        mv.addObject("max_export_num", ConfigUtils.max_export_excel_num);
        mv.setViewName("statistic/channelConsume");
        return mv;
    }

    /**
     * 通道消耗报表 --> interface
     */
    @PostMapping("/channelConsume")
    @ResponseBody
    public Page getChannelConsume(Page<RecordConsumeStat> page, @RequestParam Map params,HttpSession session){
        // 从Session中取出用户Id
        params.put("userId", getUserFromSession(session).getId().toString());

        convertDateParams(params);
//        params.put("stattype",0); // 统计类型, 0:每日,2:每月
//        params.put("groupParams","date,channelid,belong_business"); // 统计类型, 0:每日,2:每月
        params.put("isOrderByChargeTotal", true);   // 是否需要计费条数排序
        params.put("groupParams","department_id,channelid,smstype,belong_business");  // 统计类型, 0:每日,2:每月 todo 不需要日期分组
        page.setParams(params);
        page.setOrderByClause("chargeTotal DESC");
        page = channelConsumeService.queryPage(page);
        return page;
    }

    /**
     * 根据权限
     * 查询所有的一级部门
     */
    @PostMapping("/getAllDept")
    @ResponseBody
    public ResultVO getAllDept(HttpSession session){
        UserSession user = getUserFromSession(session);
//        if(user.getDepartmentId().equals(1)){
            List<Department> allDept = clientConsumeService.getAllDept();
            return ResultVO.successDefault(allDept);
//        }
//        JsmsDepartment fistLevelDeparment = jsmsDepartmentService.getFistLevelDeparment(user.getDepartmentId());
//        List data = new ArrayList();
//        data.add(fistLevelDeparment);
//        return ResultVO.successDefault(data);
//        return ResultVO.successDefault(fistLevelDeparment);
    }


    /**
     * 将请求参数中的时间参数转换成 Integer
     * @param params
     */
    @SuppressWarnings("Duplicates")
    private void convertDateParams(Map params){

        String startTime = (String) params.get("startTime");
        String endTime = (String) params.get("endTime");
        String date = (String) params.get("date");
        if(!StringUtils.isEmpty(startTime)){
            params.put("startTime", Integer.parseInt(startTime.replace("-", "")));
            if(startTime.matches("^\\d{4}-\\d{2}-\\d{2}$")){
                params.put("stattype", 0);
            }else if(startTime.matches("^\\d{4}-\\d{2}$")){
                params.put("stattype", 2);
            }
        }else {
            params.remove("startTime");
        }
        if(!StringUtils.isEmpty(endTime)){
            params.put("endTime", Integer.parseInt(endTime.replace("-", "")));
            if(endTime.matches("^\\d{4}-\\d{2}-\\d{2}$")){
                params.put("stattype", 0);
            }else if(endTime.matches("^\\d{4}-\\d{2}$")){
                params.put("stattype", 2);
            }
        }else {
            params.remove("endTime");
        }
        if(!StringUtils.isEmpty(date)){
            params.put("date", Integer.parseInt(date.replace("-", "")));
            if(date.matches("^\\d{4}-\\d{2}-\\d{2}$")){
                params.put("stattype", 0);
            }else if(date.matches("^\\d{4}-\\d{2}$")){
                params.put("stattype", 2);
            }
        }else {
            params.remove("date");
        }

    }

    /**
     * 月份装换成 时间区间
     * 如果是当前月则从当月1号到今天
     * @param endMinus --> 如果是当月, endTime 的结束范围,  endMinus = 1 , 则为昨天
     */
    private void converMonthToRange(Map params,int endMinus){
        String date = (String) params.get("date");
        if(!StringUtils.isEmpty(date) && date.matches("^\\d{4}-\\d{2}")){
            DateTime parse = DateTime.parse(date);
            int monthOfYear = DateTime.now().getMonthOfYear();
            if (parse.getMonthOfYear() >= monthOfYear){
                params.put("startTime", DateTime.now().withDayOfMonth(1).toString("yyyy-MM-dd"));
                params.put("endTime", DateTime.now().minusDays(endMinus).toString("yyyy-MM-dd"));
            }else{
                params.put("startTime", parse.toString("yyyy-MM-dd"));
                params.put("endTime", parse.plusMonths(1).minusDays(1).toString("yyyy-MM-dd"));
            }
        }

    }

    private void convertUnicode(Map<String,String> params){
        try {
            if("true".equals(params.get("_existAgent"))){
                params.put("_agentName", URLDecoder.decode(StringUtils.isEmpty(params.get("_agentName"))?"":params.get("_agentName"),"UTF-8"));
            }
            params.put("_accountGname", URLDecoder.decode(StringUtils.isEmpty(params.get("_accountGname"))?"":params.get("_accountGname"),"UTF-8"));
            params.put("_belongSaleStr", URLDecoder.decode(StringUtils.isEmpty(params.get("_belongSaleStr"))?"":params.get("_belongSaleStr"),"UTF-8"));
            params.put("_operatorstypeStr", URLDecoder.decode(StringUtils.isEmpty(params.get("_operatorstypeStr"))?"":params.get("_operatorstypeStr"),"UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }




    /**
     * 需求2.3的
     * 客户短信消耗报表(国际) --> route
     */
    @GetMapping("/clientConsumeInter2point3")
    public ModelAndView clientConsumeInter2point3Page(ModelAndView mv, HttpSession session) {

        mv.addObject("max_export_num", ConfigUtils.max_export_excel_num);
        mv.setViewName("statistic/clientConsumeInter2point3");

        UserSession user = getUserFromSession(session);
        mv.addObject("menus", AgentUtils.hasMenuRight(user, "普通短信消耗报表-ptdxxhbb", "国际短信消耗报表-gjdxxhbb"));
        JsmsMenu jsmsMenu = authorityService.getJsmsMenu("国际短信消耗报表");
        mv.addObject("jsmsMenu" , jsmsMenu);
        return mv;
    }
    /**
     * 需求2.3的
     * 客户短信消耗报表(国际)  --> interface
     */
    @SuppressWarnings("Duplicates")
    @PostMapping("/clientConsumeInter2point3")
    @ResponseBody
    public Page getClientConsumeInter2point3(Page page, String clientType,String condition,String smstype,String departmentId,String paytype,String startTime,String endTime, HttpSession session){
        Map params = new HashMap();
        // 从Session中取出用户Id
        params.put("userId", getUserFromSession(session).getId().toString());
        if(StringUtils.isNotBlank(clientType)) {//客户类型
            params.put("clientType", clientType);
            if(clientType.equals("1")) {
                params.put("existAgent", true); // 是否是代理商子客户, 直客没有代理商
            } else{
                params.put("existAgent", false); // 是否是代理商子客户, 直客没有代理商
            }
        }
        if(StringUtils.isNotBlank(condition)) //客户ID/客户名称/代理商ID/销售名字
            params.put("condition",condition);
        if(StringUtils.isNotBlank(smstype)) //短信类型
            params.put("smstype",smstype);
        if(StringUtils.isNotBlank(departmentId)) //部门
            params.put("departmentId",departmentId);
        if(StringUtils.isNotBlank(paytype)) //付费类型
            params.put("paytype",paytype);
        if(StringUtils.isNotBlank(startTime)) {//统计开始时间
            params.put("startTime", Integer.parseInt(startTime.replace("-", "")));
            if(startTime.matches("^\\d{4}-\\d{2}-\\d{2}$")){
                params.put("stattype", 0);
            }else if(startTime.matches("^\\d{4}-\\d{2}$")){
                params.put("stattype", 2);
            }
        }
        if(StringUtils.isNotBlank(endTime)) {//统计结束时间
            params.put("endTime", Integer.parseInt(endTime.replace("-", "")));
            if(endTime.matches("^\\d{4}-\\d{2}-\\d{2}$")){
                params.put("stattype", 0);
            }else if(endTime.matches("^\\d{4}-\\d{2}$")){
                params.put("stattype", 2);
            }
        }


//        convertDateParams(params);
        params.put("operatorstypeExclude", Arrays.asList(0,1,2,3,-1)); //operatorstypeExclude不包含， 通道运营商类型，0：全网，1：移动，2：联通，3：电信，4：国际，-1：拦截
        page.setParams(params);
        page = clientConsumeService.getClientConsume2point3(page);
        return page;
    }


    /**
     * 需求2.3的
     * 客户短信消耗报表(国际)  --> interface
     */
    @SuppressWarnings("Duplicates")
    @PostMapping("/clientConsume2point3Total")
//    @ResponseBody
    @JSON(type = ClientConsumeVO2Point3.class, filter="accountGname,agentId,agentName,agentType,agentTypeStr,belongSale,belongSaleStr,chargeRule,chargeRuleStr,clientTypeStr,clientid,departmentStr,operatorstype,operatorstypeStr,paytype,paytypeStr,smstype,smstypeStr")
    public Result<ClientConsumeVO2Point3> getClientConsume2point3Total(Page page, String clientType, String condition, String smstype, String departmentId, String paytype, String startTime, String endTime, String operatorCode, String isInter, HttpSession session) {
        Result<ClientConsumeVO2Point3> result = new Result<>(false, CodeEnum.FAIL,null,"操作失败");

        try {
            Map params = new HashMap();
            // 从Session中取出用户Id
            params.put("userId", getUserFromSession(session).getId().toString());
            if (StringUtils.isNotBlank(clientType)) {//客户类型
                params.put("clientType", clientType);
                if (clientType.equals("1")) {
                    params.put("existAgent", true); // 是否是代理商子客户, 直客没有代理商
                } else {
                    params.put("existAgent", false); // 是否是代理商子客户, 直客没有代理商
                }
            }
            //客户ID/客户名称/代理商ID/销售名字
            if(StringUtils.isNotBlank(condition)) {
                params.put("condition", condition);
            }
            //短信类型
            if(StringUtils.isNotBlank(smstype)) {
                params.put("smstype", smstype);
            }
            // 运营商类型
            if(StringUtils.isNotBlank(operatorCode)) {
                params.put("operatorstype", operatorCode);
            }
            //部门
            if(StringUtils.isNotBlank(departmentId)) {
                params.put("departmentId", departmentId);
            }
            //付费类型
            if(StringUtils.isNotBlank(paytype)) {
                params.put("paytype", paytype);
            }
            if (StringUtils.isNotBlank(startTime)) {//统计开始时间
                params.put("startTime", Integer.parseInt(startTime.replace("-", "")));
                if (startTime.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                    params.put("stattype", 0);
                } else if (startTime.matches("^\\d{4}-\\d{2}$")) {
                    params.put("stattype", 2);
                }
            }
            if (StringUtils.isNotBlank(endTime)) {//统计结束时间
                params.put("endTime", Integer.parseInt(endTime.replace("-", "")));
                if (endTime.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                    params.put("stattype", 0);
                } else if (endTime.matches("^\\d{4}-\\d{2}$")) {
                    params.put("stattype", 2);
                }
            }


            params.put("operatorstypeExclude", Arrays.asList(4)); //operatorstypeExclude不包含， 通道运营商类型，0：全网，1：移动，2：联通，3：电信，4：国际，-1：拦截

            if (StringUtils.isNotBlank(isInter)) {
                if (isInter.equals("true")) {
                    params.put("operatorstypeExclude", Arrays.asList(0, 1, 2, 3, -1)); //operatorstypeExclude不包含， 通道运营商类型，0：全网，1：移动，2：联通，3：电信，4：国际，-1：拦截
                }
            }

//        convertDateParams(params);
            page.setParams(params);
            ClientConsumeVO2Point3 total = clientConsumeService.getClientConsume2point3Total(page);
            result = new Result<>(true, CodeEnum.SUCCESS,total,"操作成功");

        }catch (Exception e){
            logger.error("获取短信消耗报表合计失败",e);
            result = new Result<>(false, CodeEnum.FAIL,null,"获取短信消耗报表合计失败");
        }

        return result;
    }



    /**
     * 需求2.3的
     * 【生成】代理商子客户短信消耗报表 --> interface
     */
    @SuppressWarnings("Duplicates")
    @PostMapping("/exportClientConsume2point3")
    @ResponseBody
    public ResultVO exportClientConsume2point3(Page page, String clientType,String condition,String smstype,String departmentId,String paytype,String startTime,String endTime, String operatorCode,
                                               String clientTypeName,String conditionName,String smstypeName,String departmentIdName,String paytypeName, String operatorCodeName,
                                                String isInter, HttpSession session){
        // 从Session中取出用户Id

        ResultVO resultVO = ResultVO.failure("生成报表失败");
        Map params = new HashMap();
        // 从Session中取出用户Id
        params.put("userId", getUserFromSession(session).getId().toString());
        if (StringUtils.isNotBlank(clientType)) {//客户类型
            params.put("clientType", clientType);
            if (clientType.equals("1")) {
                params.put("existAgent", true); // 是否是代理商子客户, 直客没有代理商
                clientTypeName = "代理商子客户" ;
            } else {
                params.put("existAgent", false); // 是否是代理商子客户, 直客没有代理商
                clientTypeName = "直客";
            }
        }else{
            clientTypeName="全部";
        }
        //客户ID/客户名称/代理商ID/销售名字
        if(StringUtils.isNotBlank(condition)) {
            params.put("condition", condition);
        }
        //短信类型
        if(StringUtils.isNotBlank(smstype)) {
            params.put("smstype", smstype);
        }
        // 运营商类型
        if(StringUtils.isNotBlank(operatorCode)) {
            params.put("operatorstype", operatorCode);
        }
        //部门
        if(StringUtils.isNotBlank(departmentId)) {
            params.put("departmentId", departmentId);
        }
        //付费类型
        if(StringUtils.isNotBlank(paytype)) {
            params.put("paytype", paytype);
        }
        if (StringUtils.isNotBlank(startTime)) {//统计开始时间
            params.put("startTime", Integer.parseInt(startTime.replace("-", "")));
            if (startTime.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                params.put("stattype", 0);
            } else if (startTime.matches("^\\d{4}-\\d{2}$")) {
                params.put("stattype", 2);
            }
        }
        if (StringUtils.isNotBlank(endTime)) {//统计结束时间
            params.put("endTime", Integer.parseInt(endTime.replace("-", "")));
            if (endTime.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
                params.put("stattype", 0);
            } else if (endTime.matches("^\\d{4}-\\d{2}$")) {
                params.put("stattype", 2);
            }
        }


        params.put("operatorstypeExclude", Arrays.asList(4)); //operatorstypeExclude不包含， 通道运营商类型，0：全网，1：移动，2：联通，3：电信，4：国际，-1：拦截

        String titile = "普通短信消耗报表";
        if (StringUtils.isNotBlank(isInter)) {
            if (isInter.equals("true")) {
                params.put("operatorstypeExclude", Arrays.asList(0, 1, 2, 3, -1)); //operatorstypeExclude不包含， 通道运营商类型，0：全网，1：移动，2：联通，3：电信，4：国际，-1：拦截
                titile = "国际短信消耗报表";
            }
        }

        page.setParams(params);
        page.setPageSize(Integer.MAX_VALUE);
        page = clientConsumeService.getClientConsume2point3(page);
        List data = page.getData();
        if (data == null || data.size() <= 0){
            return ResultVO.failure("没有数据！先不导出了  ^_^");
        }else if (data.size() > Integer.valueOf(ConfigUtils.max_export_excel_num)){
            return ResultVO.failure("数据量超过"+ConfigUtils.max_export_excel_num+"，请缩小范围后再导出吧  ^_^");
        }

        ClientConsumeVO2Point3 total = clientConsumeService.getClientConsume2point3Total(page);
        total.setSmstypeStr("合计");
        data.add(total);
        List dataMap = new ArrayList();
        for (Object obj:data){
            Map<String, String> describe = null;
            try {
                describe = BeanUtils.describe(obj);
                String agentIdStr = describe.get("agentId");
                String smstypeStr = describe.get("smstypeStr");
                if(agentIdStr==null||agentIdStr.equals("0")||agentIdStr.equals("0")||agentIdStr.equals("1")||agentIdStr.equals("2")){
                    describe.put("agentId","-");
                }
                if(smstypeStr!=null&&smstypeStr.equals("合计")){
                    describe.put("agentId","");
                }
            } catch (Exception e) {
                logger.debug("将对象转为map失败",e);
                return resultVO;
            }
            dataMap.add(describe);
        }

        try {
            StringBuffer filePath = new StringBuffer(ConfigUtils.save_path);
            if(!ConfigUtils.save_path.endsWith("/")){
                filePath.append("/");
            }


            filePath.append(titile)
                    .append(".xls")
                    .append("$$$")
                    .append(UUID.randomUUID());
            StringBuilder builder = new StringBuilder("查询条件 -> ")
                    .append("客户类型：").append(clientTypeName).append("；")
                    .append("部门：").append(departmentIdName).append("；")
                    .append("短信类型：").append(smstypeName).append("；")
                    .append("运营商类型：").append(operatorCodeName).append("；")
                    .append("付费类型：").append(paytypeName).append("；")
                    .append("开始时间：").append(startTime).append("；")
                    .append("结束时间：").append(endTime).append("；");
//                    .append("其他条件：").append(conditionName);
            Excel excel = new Excel();
            excel.setFilePath(filePath.toString());
            excel.setTitle(titile);
            excel.addRemark(builder.toString());
            excel.addHeader(20, "客户类型", "clientTypeStr");
            excel.addHeader(20, "部门", "departmentStr");
            excel.addHeader(20, "客户ID", "clientid");
            excel.addHeader(20, "客户名称", "accountGname");
            excel.addHeader(20, "所属代理商ID", "agentId");
            excel.addHeader(20, "代理商名称", "agentName");
            excel.addHeader(20, "代理商类型", "agentTypeStr");
            excel.addHeader(20, "归属销售", "belongSaleStr");
            excel.addHeader(20, "付费类型", "paytypeStr");
            excel.addHeader(20, "计费规则", "chargeRuleStr");
            excel.addHeader(20, "短信类型", "smstypeStr");
            excel.addHeader(20, "运营商类型", "operatorstypeStr");
            excel.addHeader(20, "提交条数（条）", "submitTotal");
            excel.addHeader(20, "计费条数（条）", "chargeTotal");
            excel.addHeader(20, "成功率（%）", "successRate");
            excel.addHeader(20, "成功条数（条）", "reportsuccess");
            excel.addHeader(20, "未知条数", "submitsuccess");
            excel.addHeader(20, "失败条数（条）", "failTotal");
            excel.addHeader(20, "待发送条数（条）", "notsend");
            excel.addHeader(20, "拦截条数（条）", "interceptTotal");
            excel.addHeader(20, "消费金额(元)", "salefeeStr");
            excel.addHeader(20, "通道成本(元)", "costfeeStr");
            excel.addHeader(20, "毛利（元)", "profit");
            excel.addHeader(20, "毛利率（%）", "profitRate");
            excel.addHeader(20, "未返还条数", "pendingReturnNumber");
            excel.addHeader(20, "未返还计费", "pendingReturnAmountStr");

            excel.setDataList(dataMap);


            if (ExcelUtils.exportExcel(excel,true)) {
                resultVO.setSuccess(true);
                resultVO.setMsg("报表生成成功");
                resultVO.setData(excel.getFilePath());
            }

        } catch (Exception e) {
            logger.error("生成报表失败", e);
        }
        return resultVO;
    }
}
