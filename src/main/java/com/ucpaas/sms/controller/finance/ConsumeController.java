package com.ucpaas.sms.controller.finance;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.common.enums.Code;
import com.jsmsframework.finance.entity.JsmsClientConsumerList;
import com.jsmsframework.order.entity.JsmsOemClientOrder;
import com.jsmsframework.order.enums.OEMClientOrderType;
import com.opensymphony.oscache.util.StringUtil;
import com.ucpaas.sms.controller.BaseController;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.model.Excel;
import com.ucpaas.sms.service.finance.consume.ConsumeService;
import com.ucpaas.sms.util.AgentUtils;
import com.ucpaas.sms.util.ConfigUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.UUID;

/**
 * Created by lpjLiu on 2017/5/16. 账单信息
 */
@Controller
@RequestMapping("/finance/consumer")
public class ConsumeController extends BaseController{

    private static Logger logger = LoggerFactory.getLogger(ConsumeController.class);

    @Autowired
    private ConsumeService consumeService;

    /**
     * 账单首页
     */
    @RequestMapping("/brand")
    public ModelAndView brandPage(ModelAndView mv, HttpSession session) {
        mv.addObject("max_export_num", ConfigUtils.max_export_excel_num);
		UserSession user = getUserFromSession(session);
		mv.addObject("menus", AgentUtils.hasMenuRight(user, "品牌消费记录-ppxfjl", "子账户消费-zzhxf"));
        mv.setViewName("finance/consume/list");
//		return "finance/consume/list";
        return mv;
    }

    /**
     * 账户消费-品牌消费记录列表
     */
    @RequestMapping("/brand/list")
    @ResponseBody
    public JsmsPage brandList(JsmsPage<JsmsClientConsumerList> jsmsPage, @RequestParam Map params,HttpSession session) {
        String clientid = (String) params.get("clientid");
        if(StringUtil.isEmpty(clientid)){
            String clientId = (String) params.get("clientId");
            if(!StringUtil.isEmpty(clientId)){
                params.put("clientid",clientId);
            }
        }
        String name = (String) params.get("name");
        if(StringUtil.isEmpty(name)){
            String clientName = (String) params.get("clientName");
            if(!StringUtil.isEmpty(clientName)){
                params.put("name",clientName);
            }
        }
        // 权限控制
        UserSession userSession = getUserFromSession(session);
        params.put("userId", userSession.getId().toString());
        jsmsPage.setParams(params);
        jsmsPage.setOrderByClause(" operate_time DESC");
        consumeService.queryBrandList(jsmsPage);
        return jsmsPage;
    }

    /**
     * 账户消费-品牌消费记录,短信数量总数
     */
    @RequestMapping("/brand/total")
    @ResponseBody
    public R brandTotal(@RequestParam Map params,HttpSession session) {
        try {
            JsmsPage<JsmsClientConsumerList> jsmsPage = new JsmsPage<>();
            String clientid = (String) params.get("clientid");
            if(StringUtil.isEmpty(clientid)){
                String clientId = (String) params.get("clientId");
                if(!StringUtil.isEmpty(clientId)){
                    params.put("clientid",clientId);
                }
            }
            String name = (String) params.get("name");
            if(StringUtil.isEmpty(name)){
                String clientName = (String) params.get("clientName");
                if(!StringUtil.isEmpty(clientName)){
                    params.put("name",clientName);
                }
            }
            // 权限控制
            UserSession userSession = getUserFromSession(session);
            params.put("userId", userSession.getId().toString());
            jsmsPage.setParams(params);
            jsmsPage.setOrderByClause(" operate_time DESC");
            Integer total = consumeService.queryBrandTotal(jsmsPage);
            return R.ok("获取品牌消费记录短信总数成功",total+"条");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取品牌消费记录短信总数失败{}", e);
            return R.error(Code.SYS_ERR,"服务器正在检修...");
        }
    }

    /**
     * 账单首页
     */
    @RequestMapping("/oem")
    public ModelAndView oemPage(ModelAndView mv, HttpSession session) {
        mv.addObject("max_export_num", ConfigUtils.max_export_excel_num);
        UserSession user = getUserFromSession(session);
        mv.addObject("menus", AgentUtils.hasMenuRight(user, "品牌消费记录-ppxfjl", "子账户消费-zzhxf"));
        mv.setViewName("finance/consume/oemList");
//        return "finance/consume/oemList";
        return mv;
    }

    /**
     * 账户消费-子账户消费列表
     */
    @RequestMapping("/oem/list")
    @ResponseBody
    public JsmsPage oemList(JsmsPage<JsmsOemClientOrder> jsmsPage, @RequestParam Map params,HttpSession session) {
        String clientid = (String) params.get("clientid");
        if(StringUtil.isEmpty(clientid)){
            String clientId = (String) params.get("clientId");
            if(!StringUtil.isEmpty(clientId)){
                params.put("clientid",clientId);
            }
        }
        String name = (String) params.get("name");
        if(StringUtil.isEmpty(name)){
            String clientName = (String) params.get("clientName");
            if(!StringUtil.isEmpty(clientName)){
                params.put("name",clientName);
            }
        }
        String orderType = (String)params.get("orderType");
        if(StringUtils.isBlank(orderType)){
            params.put("orderType", OEMClientOrderType.短信失败返还.getValue());
        }

        // 权限控制
        UserSession userSession = getUserFromSession(session);
        params.put("userId", userSession.getId().toString());
        jsmsPage.setParams(params);
        consumeService.queryOemList(jsmsPage);
        return jsmsPage;
    }

    /**
     * 账户消费-子账户消费记录,短信数量总数
     */
    @RequestMapping("/oem/total")
    @ResponseBody
    public R OEMTotal(@RequestParam Map params,HttpSession session) {
        try {
            JsmsPage<JsmsOemClientOrder> jsmsPage = new JsmsPage<>();
            String clientid = (String) params.get("clientid");
            if(StringUtil.isEmpty(clientid)){
                String clientId = (String) params.get("clientId");
                if(!StringUtil.isEmpty(clientId)){
                    params.put("clientid",clientId);
                }
            }
            String name = (String) params.get("name");
            if(StringUtil.isEmpty(name)){
                String clientName = (String) params.get("clientName");
                if(!StringUtil.isEmpty(clientName)){
                    params.put("name",clientName);
                }
            }
            String orderType = (String)params.get("orderType");
            if(StringUtils.isBlank(orderType)){
                params.put("orderType", OEMClientOrderType.短信失败返还.getValue());
            }

            // 权限控制
            UserSession userSession = getUserFromSession(session);
            params.put("userId", userSession.getId().toString());
            jsmsPage.setParams(params);
            Integer tatol = consumeService.queryOemTotal(jsmsPage);
            return R.ok("获取子账户消费短信总数成功",tatol + "条");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取子账户消费短信总数错误{}",e);
            return R.error(Code.SYS_ERR,"服务器正在检修...");
        }
    }

    /**
     * 【生成】品牌消耗记录 --> interface
     */
    @PostMapping("/brand/list/export")
    @ResponseBody
    public ResultVO exportBrandList(JsmsPage<JsmsClientConsumerList> jsmsPage, @RequestParam Map params,HttpSession session) {
        jsmsPage.setOrderByClause(" operate_time DESC");
        ResultVO resultVO = ResultVO.failure("生成报表失败");
        String clientid = (String) params.get("clientid");
        if(StringUtil.isEmpty(clientid)){
            String clientId = (String) params.get("clientId");
            if(!StringUtil.isEmpty(clientId)){
                params.put("clientid",clientId);
            }
        }
        String name = (String) params.get("name");
        if(StringUtil.isEmpty(name)){
            String clientName = (String) params.get("clientName");
            if(!StringUtil.isEmpty(clientName)){
                params.put("name",clientName);
            }
        }
        try {
            StringBuffer filePath = new StringBuffer(ConfigUtils.save_path);
            if(!ConfigUtils.save_path.endsWith("/")){
                filePath.append("/");
            }
            filePath.append("品牌客户消耗记录")
                    .append(".xls")
                    .append("$$$")
                    .append(UUID.randomUUID());
            StringBuilder builder = new StringBuilder("查询条件 -> ")
                    .append("客户ID：").append(params.get("clientId")).append("；")
                    .append("客户名称：").append(params.get("clientName")).append("；")
                    .append("操作类型：").append(params.get("_operateType")).append("；")
                    .append("产品类型：").append(params.get("_productType")).append("；")
                    .append("运营商类型：").append(params.get("_operatorCode")).append("；")
                    .append("操作开始日期：").append(params.get("startTimeDay")).append("；")
                    .append("操作结束日期：").append(params.get("endTimeDay"))
                    ;
            Excel excel = new Excel();
            excel.setFilePath(filePath.toString());
            excel.setTitle("品牌客户消耗记录");
            excel.addRemark(builder.toString());
            excel.addHeader(20, "客户ID", "clientid");
            excel.addHeader(20, "客户名称", "clientName");
            excel.addHeader(20, "操作类型", "operateTypeStr");
            excel.addHeader(20, "订单编号", "orderId");
            excel.addHeader(20, "产品类型", "productTypeStr");
            excel.addHeader(20, "运营商类型", "operatorCodeStr");
            excel.addHeader(20, "区域", "areaCodeStr");
            excel.addHeader(20, "单价(元)", "unitPriceStr");
            excel.addHeader(20, "到期时间", "dueTimeStr");
            excel.addHeader(20, "短信数量", "smsNumberStr");
            excel.addHeader(20, "消费日期", "consumerDateStr");
            excel.addHeader(20, "操作者", "operatorStr");
            excel.addHeader(20, "操作日期", "operateTimeStr");

            // 权限控制
            UserSession userSession = getUserFromSession(session);
            params.put("userId", userSession.getId().toString());
            jsmsPage.setParams(params);
            resultVO  = consumeService.exportBrandList(jsmsPage,excel);

        } catch (Exception e) {
            logger.error("生成报表失败", e);
        }
        return resultVO;
    }

    /**
     * 【生成】子账户消耗记录 --> interface
     */
    @PostMapping("/oem/list/export")
    @ResponseBody
    public ResultVO exportOemList(JsmsPage<JsmsClientConsumerList> jsmsPage, @RequestParam Map params,HttpSession session) {

        String clientid = (String) params.get("clientid");
        if(StringUtil.isEmpty(clientid)){
            String clientId = (String) params.get("clientId");
            if(!StringUtil.isEmpty(clientId)){
                params.put("clientid",clientId);
            }
        }
        String name = (String) params.get("name");
        if(StringUtil.isEmpty(name)){
            String clientName = (String) params.get("clientName");
            if(!StringUtil.isEmpty(clientName)){
                params.put("name",clientName);
            }
        }

        String orderType = (String)params.get("orderType");
        if(StringUtils.isBlank(orderType)){
            params.put("orderType", OEMClientOrderType.短信失败返还.getValue());
        }

        ResultVO resultVO = ResultVO.failure("生成报表失败");
        try {
            StringBuffer filePath = new StringBuffer(ConfigUtils.save_path);
            if(!ConfigUtils.save_path.endsWith("/")){
                filePath.append("/");
            }
            filePath.append("子账户消耗记录")
                    .append(".xls")
                    .append("$$$")
                    .append(UUID.randomUUID());
            StringBuilder builder = new StringBuilder("查询条件 -> ")
                    .append("子账户ID：").append(params.get("clientId")).append("；")
                    .append("子账户名称：").append(params.get("clientName")).append("；")
                    .append("操作类型：").append(params.get("_operateType")).append("；")
                    .append("产品类型：").append(params.get("_productType")).append("；")
                    .append("运营商类型：").append(params.get("_operatorCode")).append("；")
                    .append("操作开始日期：").append(params.get("beginCreateTime")).append("；")
                    .append("操作结束日期：").append(params.get("endCreateTime"))
                    ;
            Excel excel = new Excel();
            excel.setFilePath(filePath.toString());
            excel.setTitle("子账户消耗记录");
            excel.addRemark(builder.toString());
            excel.addHeader(20, "子账户ID", "clientId");
            excel.addHeader(20, "子账户名称", "clientName");
            excel.addHeader(20, "操作类型", "orderTypeStr");
            excel.addHeader(20, "订单编号", "orderId");
            excel.addHeader(20, "产品类型", "productTypeStr");
            excel.addHeader(20, "运营商类型", "operatorCodeStr");
            excel.addHeader(20, "区域", "areaCodeStr");
            excel.addHeader(20, "单价(元)", "unitPriceStr");
            excel.addHeader(20, "到期时间", "dueTimeStr");
            excel.addHeader(20, "短信数量", "orderNumberStr");
            excel.addHeader(20, "消费日期", "consumerDateStr");
            excel.addHeader(20, "操作者", "operatorStr");
            excel.addHeader(20, "操作日期", "createTimeStr");

            // 权限控制
            UserSession userSession = getUserFromSession(session);
            params.put("userId", userSession.getId().toString());
            jsmsPage.setParams(params);
            resultVO  = consumeService.exportOemList(jsmsPage,excel);

        } catch (Exception e) {
            logger.error("生成报表失败", e);
        }
        return resultVO;
    }




}
