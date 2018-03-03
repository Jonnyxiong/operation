package com.ucpaas.sms.controller.finance.invoice;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.enums.Code;
import com.jsmsframework.common.enums.invoice.InvoiceStatusEnum;
import com.jsmsframework.common.enums.invoice.InvoiceTypeEnum;
import com.jsmsframework.common.util.StringUtils;
import com.jsmsframework.finance.dto.JsmsAgentInvoiceListDTO;
import com.jsmsframework.finance.exception.JsmsAgentInvoiceListException;
import com.jsmsframework.user.entity.JsmsMenu;
import com.ucpaas.sms.common.util.file.ExcelUtils;
import com.ucpaas.sms.common.util.file.FileUtils;
import com.ucpaas.sms.controller.BaseController;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.enums.SessionEnum;
import com.ucpaas.sms.model.Excel;
import com.ucpaas.sms.service.admin.AuthorityService;
import com.ucpaas.sms.service.finance.invoice.InvoiceAuditService;
import com.ucpaas.sms.util.AgentUtils;
import com.ucpaas.sms.util.ConfigUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

@Api("发票管理-发票审核")
@Controller
@RequestMapping("/finance/invoice/audit")
public class InvoiceAuditController extends BaseController {

    protected static Logger logger = LoggerFactory.getLogger(InvoiceAuditController.class);

    @Autowired
    private AuthorityService authorityService;
    @Autowired
    private InvoiceAuditService invoiceAuditService;

    /**
     * @param mv
     * @Description: 发票审核路由
     * @Author: tanjiangqiang
     * @Date: 2018/1/23 - 15:53
     */
    @ApiOperation(value = "发票审核主页面", notes = "发票审核主页面", tags = "发票管理-发票审核", response = ModelAndView.class)
    @RequestMapping(path = "/list", method = RequestMethod.GET)
    public ModelAndView list(ModelAndView mv, HttpSession session) {
        UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        mv.addObject("max_export_num", ConfigUtils.max_export_excel_num);
        mv.addObject("menus", AgentUtils.hasMenuRight(userSession, "发票申请-fpsq", "申请记录-sqjl", "发票信息-fpxx", "发票审核-fpsh"));
        JsmsMenu jsmsMenu = authorityService.getJsmsMenu("发票审核");
        mv.addObject("jsmsMenu", jsmsMenu);
        mv.setViewName("finance/invoice/audit/list");
        return mv;
    }

    /**
     * @Description: 发票审核-获取列表
     * @Author: tanjiangqiang
     * @Date: 2018/1/5 - 10:16
     */
    @ApiOperation(value = "发票审核-获取列表", notes = "发票审核-获取列表: 客户ID/客户名称/发票抬头/申请ID：condition，<br/>" + " 归属销售：belongSale(全部可空)，<br/>" + " 发票类型：invoiceType(全部可空)，<br/>" + " 申请状态：status(全部可空)，<br/>" + " 申请时间：开始时间startTime，结束时间endTime<br/>", tags = "发票管理-发票审核", response = R.class)
    @RequestMapping(value = "/list", method = RequestMethod.POST)
    @ResponseBody
    public R list(String rows, String page, String condition, String belongSale, String invoiceType, String status, String startTime, String endTime, HttpSession session) {
        try {
            UserSession userSession = getUserFromSession(session);
            Map<String, Object> map = new HashMap<>(8);
            if (StringUtils.isNotBlank(condition)) {
                map.put("applicationOperation", condition);
            }
            if (StringUtils.isNotBlank(rows)) {
                map.put("rows", rows);
            }
            if (StringUtils.isNotBlank(page)) {
                map.put("page", page);
            }
            if (StringUtils.isNotBlank(belongSale)) {
                map.put("belongSale", belongSale);
            }
            if (StringUtils.isNotBlank(status)) {
                map.put("status", status);
            }
            if (StringUtils.isNotBlank(invoiceType)) {
                map.put("invoiceType", invoiceType);
            }
            if (StringUtils.isNotBlank(startTime) && StringUtils.isNotBlank(endTime)) {
                map.put("createTimeStart", startTime);
                map.put("createTimeEnd", endTime);
            }
            JsmsPage<JsmsAgentInvoiceListDTO> data = invoiceAuditService.getList(map, userSession);
            return R.ok("发票审核-获取列表成功", data);
        } catch (Exception e) {
            logger.error("发票审核-获取列表失败--------{}", e);
            e.printStackTrace();
            if (e instanceof JsmsAgentInvoiceListException) {
                return R.error(Code.OPT_ERR, e.getMessage());
            } else {
                return R.error(Code.SYS_ERR, "服务器正在检修...");
            }
        }
    }

    /**
     * @Description: 发票审核-获取列表导出
     * @Author: tanjiangqiang
     * @Date: 2018/1/5 - 10:16
     */
    @ApiOperation(value = "发票审核-获取列表导出", notes = "发票审核-获取列表导出: 客户ID/客户名称/发票抬头/申请ID：condition，<br/>" + " 归属销售：belongSale(全部可空)，<br/>" + " 发票类型：invoiceType(全部可空)，<br/>" + " 申请状态：status(全部可空)，<br/>" + " 申请时间：开始时间startTime，结束时间endTime<br/>", tags = "发票管理-发票审核")
    @RequestMapping(value = "/exportList", method = RequestMethod.POST)
    @ResponseBody
    public void exportList(String condition, String belongSale, String belongSaleName, String invoiceType, String status, String startDateTime, String endDateTime, HttpSession session, HttpServletResponse response) {
        try {
            StringBuffer buffer = new StringBuffer("查询条件：");
            UserSession userSession = getUserFromSession(session);
            Map<String, Object> map = new HashMap<>(6);
            int count = 0;
            if (StringUtils.isNotBlank(condition)) {
                map.put("applicationOperation", condition);
                buffer.append("（").append(++count).append("）客户ID/客户名称/发票抬头/申请ID：").append(condition);
            }
            if (StringUtils.isNotBlank(belongSale)) {
                map.put("belongSale", belongSale);
                buffer.append("（").append(++count).append("）归属销售：").append(belongSaleName);
            }
            if (StringUtils.isNotBlank(invoiceType)) {
                map.put("invoiceType", invoiceType);
                buffer.append("（").append(++count).append("）发票类型：").append(InvoiceTypeEnum.getDescByValue(Integer.valueOf(invoiceType)));
            }
            if (StringUtils.isNotBlank(status)) {
                map.put("status", status);
                buffer.append("（").append(++count).append("）申请状态：").append(InvoiceStatusEnum.getDescByValue(Integer.valueOf(status)));
            }
            if (StringUtils.isNotBlank(startDateTime) && StringUtils.isNotBlank(endDateTime)) {
                map.put("createTimeStart", startDateTime);
                map.put("createTimeEnd", endDateTime);
                buffer.append("（").append(++count).append("）申请时间：").append(startDateTime + "-" + endDateTime);
            }
            map.put("rows", "-1");
            map.put("page", "1");
            JsmsPage<JsmsAgentInvoiceListDTO> data = invoiceAuditService.getList(map, userSession);
            List<JsmsAgentInvoiceListDTO> resultData = data.getData();
            if (null == resultData || resultData.isEmpty()) {
                return;
            }

            // 按创建时间倒序
            Collections.sort(resultData, new Comparator<JsmsAgentInvoiceListDTO>() {
                @Override
                public int compare(JsmsAgentInvoiceListDTO o1, JsmsAgentInvoiceListDTO o2) {
                    return o2.getCreateTime().compareTo(o1.getCreateTime());
                }
            });

            Excel excel = new Excel();
            excel.setPageRowCount(2000); // 设置每页excel显示2000行
            String filePath = ConfigUtils.save_path + "/发票审核报表.xls";
            excel.setFilePath(filePath);
            excel.setTitle("发票审核报表");
            // 在表格对象中添加查询条件,无查询条件则不添加
            if (count > 0) {
                excel.addRemark(buffer.toString());
            }
            excel.addHeader(20, "申请ID", "invoiceId");
            excel.addHeader(20, "客户ID", "agentId");
            excel.addHeader(20, "客户名称", "name");
            excel.addHeader(20, "归属销售", "belongSaleStr");
            excel.addHeader(20, "发票金额", "invoiceAmountStr");
            excel.addHeader(20, "发票类型", "invoiceTypeStr");
            excel.addHeader(20, "开票主体", "invoiceBodyStr");
            excel.addHeader(20, "发票抬头", "invoiceHead");
            excel.addHeader(20, "申请人", "applicantStr");
            excel.addHeader(20, "申请时间", "createTimeStr");
            excel.addHeader(20, "申请状态", "statusStr");

            List<Map<String, Object>> result = new ArrayList<>(resultData.size());
            for (Object obj : data.getData()){
                Map<String, Object> describe = (Map)BeanUtils.describe(obj);
                result.add(describe);
            }
            excel.setDataList(result);

            if (ExcelUtils.exportExcel(excel)) {
                FileUtils.download(response, filePath);
                FileUtils.delete(filePath);
            } else {
                String fullContentType = "text/plain;charset=UTF-8";
                response.setContentType(fullContentType);
                response.getWriter().write("导出Excel文件失败，请联系管理员");
                response.getWriter().flush();
            }
        } catch (Exception e) {
            logger.error("发票审核-获取列表导出失败--------{}", e);
        }
    }


    /**
     * @Description: 发票审核-查看按钮
     * @Author: tanjiangqiang
     * @Date: 2018/1/5 - 10:16
     */
    @ApiOperation(value = "发票审核-查看按钮", notes = "发票审核-查看按钮: 申请id：invoiceId<br/>", tags = "发票管理-发票审核", response = R.class)
    @RequestMapping(value = "/view", method = RequestMethod.POST)
    @ResponseBody
    public R view(@RequestParam(value = "invoiceId") String invoiceId) {
        try {
            JsmsAgentInvoiceListDTO data = invoiceAuditService.view(invoiceId);
            return R.ok("发票审核-查看详情", data);
        } catch (Exception e) {
            logger.error("发票审核-查看详情失败--------{}", e);
            e.printStackTrace();
            if (e instanceof JsmsAgentInvoiceListException) {
                return R.error(Code.OPT_ERR, e.getMessage());
            } else {
                return R.error(Code.SYS_ERR, "服务器正在检修...");
            }
        }
    }

    /**
     * @Description: 发票审核-审核按钮
     * @Author: tanjiangqiang
     * @Date: 2018/1/5 - 10:16
     */
    @ApiOperation(value = "发票审核-审核按钮", notes = "发票审核-审核按钮: 申请id：invoiceId，<br/>" + " 申请状态：status(传2为审核不通过,3为审核通过)，<br/>" + " 审核不通过原因：auditFailCause(status为3时,可不传)<br/>", tags = "发票管理-发票审核", response = R.class)
    @RequestMapping(value = "/doaudit", method = RequestMethod.POST)
    @ResponseBody
    public R doaudit(@RequestParam(value = "invoiceId") String invoiceId, @RequestParam(value = "status") Integer status, @RequestParam(value = "auditFailCause", required = false) String auditFailCause, HttpSession session) {
        try {
            invoiceAuditService.doaudit(invoiceId, status, auditFailCause, getUserFromSession(session));
            return R.ok("发票审核-审核成功", null);
        } catch (Exception e) {
            logger.error("发票审核-审核失败--------{}", e);
            e.printStackTrace();
            if (e instanceof JsmsAgentInvoiceListException) {
                return R.error(Code.OPT_ERR, e.getMessage());
            } else {
                return R.error(Code.SYS_ERR, "服务器正在检修...");
            }
        }
    }


    /**
     * @Description: 发票审核-审核驳回按钮
     * @Author: tanjiangqiang
     * @Date: 2018/1/5 - 10:16
     */
    @ApiOperation(value = "发票审核-审核驳回按钮", notes = "发票审核-审核驳回按钮: 申请id：invoiceId，<br/>" + " 审核不通过原因：auditFailCause<br/>", tags = "发票管理-发票审核", response = R.class)
    @RequestMapping(value = "/audittoback", method = RequestMethod.POST)
    @ResponseBody
    public R audittoback(@RequestParam(value = "invoiceId") String invoiceId, @RequestParam(value = "auditFailCause") String auditFailCause, HttpSession session) {
        try {
            invoiceAuditService.audittoback(invoiceId, auditFailCause, getUserFromSession(session));
            return R.ok("发票审核-审核驳回成功", null);
        } catch (Exception e) {
            logger.error("发票审核-审核驳回失败--------{}", e);
            e.printStackTrace();
            if (e instanceof JsmsAgentInvoiceListException) {
                return R.error(Code.OPT_ERR, e.getMessage());
            } else {
                return R.error(Code.SYS_ERR, "服务器正在检修...");
            }
        }
    }

    /**
     * @Description: 发票审核-邮寄按钮
     * @Author: tanjiangqiang
     * @Date: 2018/1/5 - 10:16
     */
    @ApiOperation(value = "发票审核-邮寄按钮", notes = "发票审核-邮寄按钮: 申请id：invoiceId，<br/>" + " 快递公司：expressCompany，<br/>" + " 快递单号：expressOrder<br/>", tags = "发票管理-发票审核", response = R.class)
    @RequestMapping(value = "/express", method = RequestMethod.POST)
    @ResponseBody
    public R express(@RequestParam(value = "invoiceId") String invoiceId, @RequestParam(value = "expressCompany") Integer expressCompany, @RequestParam(value = "expressOrder") String expressOrder, HttpSession session) {
        try {
            invoiceAuditService.express(invoiceId, expressCompany, expressOrder, getUserFromSession(session));
            return R.ok("发票审核-邮寄成功", null);
        } catch (Exception e) {
            logger.error("发票审核-邮寄失败--------{}", e);
            e.printStackTrace();
            if (e instanceof JsmsAgentInvoiceListException) {
                return R.error(Code.OPT_ERR, e.getMessage());
            } else {
                return R.error(Code.SYS_ERR, "服务器正在检修...");
            }
        }
    }
}
