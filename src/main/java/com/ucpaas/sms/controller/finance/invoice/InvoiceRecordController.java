package com.ucpaas.sms.controller.finance.invoice;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.R;
import com.jsmsframework.finance.dto.JsmsAgentInvoiceListDTO;
import com.jsmsframework.finance.exception.JsmsAgentInvoiceListException;
import com.jsmsframework.user.entity.JsmsMenu;
import com.jsmsframework.user.entity.JsmsUser;
import com.ucpaas.sms.common.util.file.ExcelUtils;
import com.ucpaas.sms.controller.BaseController;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.enums.SessionEnum;
import com.ucpaas.sms.model.Excel;
import com.ucpaas.sms.service.admin.AuthorityService;
import com.ucpaas.sms.service.finance.invoice.InvoiceRecordService;
import com.ucpaas.sms.util.AgentUtils;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.FileUtils;
import com.ucpaas.sms.util.ObjectToMapUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 申请记录
 */

@Api("发票管理-发票记录")
@Controller
@RequestMapping("/finance/invoice/record")
public class InvoiceRecordController extends BaseController {

    protected static Logger logger = LoggerFactory.getLogger(InvoiceRecordController.class);
    @Autowired
    private AuthorityService authorityService;
    @Autowired
    private InvoiceRecordService invoiceRecordService;


    /**
     * @Description: 申请记录路由
     * @Author: xiaoqingwen
     * @Date: 2018/1/23 - 18:11
     * @param mv
     *
     */
    @RequestMapping(path = "/list", method = RequestMethod.GET)
    public ModelAndView list(ModelAndView mv, HttpSession session) {
        UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        mv.addObject("menus", AgentUtils.hasMenuRight(userSession, "发票申请-fpsq", "申请记录-sqjl", "发票信息-fpxx", "发票审核-fpsh"));
        JsmsMenu jsmsMenu = authorityService.getJsmsMenu("申请记录");
        mv.addObject("jsmsMenu", jsmsMenu);
        mv.setViewName("finance/invoice/record/list");
        return mv;
    }

    /**
     *根据用户数据权限去获取归属销售
     */
    @ApiOperation(value = "申请记录-根据用户数据权限获取归属销售", notes = "申请记录-获取归属销售", tags = "发票管理-申请记录", response = List.class)
    @RequestMapping(path = "/queryUserByDataAuthority", method = RequestMethod.GET)
    @ResponseBody
    public List<JsmsUser> queryUserByDataAuthority(HttpSession session) {
        UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        Long id = userSession.getId();
        return invoiceRecordService.queryUserByDataAuthority(id);
    }

    /**
     *申请记录-获取列表
     */
    @ApiOperation(value = "申请记录-搜索并获取列表", notes = "申请记录-获取搜索列表", tags = "发票管理-申请记录", response = JsmsPage.class)
    @RequestMapping(path = "/list", method = RequestMethod.POST)
    @ResponseBody
    public JsmsPage list(String applicationOperation,Integer belongSale,Integer invoiceType,Integer status,String createTimeStart,String createTimeEnd,Integer page,Integer rows, HttpSession session) {
        JsmsPage jsmsPage=new JsmsPage();
        if (page!=null && rows!=null) {
            jsmsPage.setRows(Integer.valueOf(rows));
            jsmsPage.setPage(Integer.valueOf(page));
        }
        Map<String,Object> params=new HashMap<>();
        //设置参数
        boolean b = setParams(applicationOperation, belongSale, invoiceType, status, createTimeStart, createTimeEnd, params);
        if(b==false){
            return jsmsPage;
        }
        jsmsPage.setParams(params);
        //按更新时间的倒叙排序
        jsmsPage.setOrderByClause("update_time desc");
        UserSession userSession = getUserFromSession(session);
        JsmsPage jsmsPag = invoiceRecordService.list(jsmsPage, userSession);
        return jsmsPag;
    }

    private boolean setParams(String applicationOperation, Integer belongSale, Integer invoiceType, Integer status, String createTimeStart, String createTimeEnd, Map<String, Object> params) {
        params.put("applicationOperation",applicationOperation);
        params.put("belongSale",belongSale);
        params.put("invoiceType",invoiceType);
        params.put("status",status);
        //匹配时间格式
        //String createTimeStart="yyyy-MM-dd HH:mm:ss";
        Pattern patt = Pattern.compile("^[0-9]{4}[-][0-1]{1}[0-9]{1}[-][0-3]{1}[0-9]{1}[ ][0-9]{2}[:][0-9]{2}[:][0-9]{2}$");
        if(StringUtils.isNotBlank(createTimeStart)){
            Matcher isN = patt.matcher(createTimeStart);
            //匹配成功
            if(isN.matches()){
                params.put("createTimeStart",createTimeStart);
            }else{
                logger.debug("开始时间格式不正确-->{}",createTimeStart);
                return false;
            }
        }else{
            params.put("createTimeStart",null);
        }
        if(StringUtils.isNotBlank(createTimeEnd)){
            Matcher isN = patt.matcher(createTimeEnd);
            //匹配成功
            if(isN.matches()){
                params.put("createTimeEnd",createTimeEnd);
            }else{
                logger.debug("结束时间格式不正确-->{}",createTimeEnd);
                return false;
            }
        }else{
            params.put("createTimeEnd",null);
        }
        return true;
    }

    /**
     * 申请记录的状态改变
     * @param id
     * @return
     */
    @ApiOperation(value = "申请记录-更改状态", notes = "申请记录-更改状态", tags = "发票管理-申请记录", response = R.class)
    @RequestMapping(path = "/cancel", method = RequestMethod.POST)
    @ResponseBody
    public R cancel(Integer id,HttpSession session) {
        UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        Long userId = userSession.getId();
        R r = null;
        try {
            r = invoiceRecordService.cancel(id, userId);
        } catch (JsmsAgentInvoiceListException e) {
            return R.error(e.getMessage());
        }catch(Exception e) {
            return R.error("更改状态失败!");
        }
        return r;
    }

    /**
     * 查看单个申请记录信息
     * @param id
     * @param invoiceType
     * @return
     */
    @ApiOperation(value = "申请记录-查看记录详情", notes = "申请记录-查看记录详情", tags = "发票管理-申请记录", response = R.class)
    @RequestMapping(path = "/view", method = RequestMethod.POST)
    @ResponseBody
    public R view(Integer id, Integer invoiceType) {
        return invoiceRecordService.view(id,invoiceType);
    }

    /*
    * 批量导出报表
    * */
    @ApiOperation(value = "申请记录-导出报表", notes = "申请记录-导出报表", tags = "发票管理-申请记录", response = R.class)
    @PostMapping("/export")
    @ResponseBody
    //http://localhost:8080/finance/invoice/record/invoiceRecordExport?applicationOperation=1&belongSale=2017110074&invoiceType=1&status=4&createTimeStart=2018-01-24 09:36:48&createTimeEnd=2018-01-25 17:26:22
    public R export(String applicationOperation,Integer belongSale,Integer invoiceType,Integer status,String createTimeStart,String createTimeEnd,HttpServletResponse response, HttpSession session) {

        Map<String, Object> params = new HashMap<>();
        String filePath = ConfigUtils.save_path + "/申请记录报表.xls";
        //塞入必要的参数
        Excel excel = new Excel();
        excel.setPageRowCount(2000); // 设置每页excel显示2000行
        excel.setFilePath(filePath);
        excel.setTitle("申请记录报表");

        //设置参数
        boolean b = setParams(applicationOperation, belongSale, invoiceType, status, createTimeStart, createTimeEnd, params);
        if(b==false){
            return R.error("开始时间或者结束时间格式不正确!");
        }
        //构建excel表
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
        excel.addHeader(20, "更新时间", "updateTimeStr");
        excel.addHeader(20, "审核人", "auditorStr");

        params.put("orderBy","orderBy");
        List<JsmsAgentInvoiceListDTO> list = invoiceRecordService.export(params, getUserFromSession(session));
        List<Map<String, Object>> ll = new ArrayList<>();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                JsmsAgentInvoiceListDTO jsmsAgentInvoiceListDTO = list.get(i);
                try {
                    Map<String, Object> map = ObjectToMapUtil.objectToMap(jsmsAgentInvoiceListDTO);
                    ll.add(map);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("对象转map失败!失败id为-->{}",jsmsAgentInvoiceListDTO.getId());
                    return R.error("导出失败!");
                }
            }
        }else{
            return R.error("没有数据可以导出!");
        }
        excel.setDataList(ll);
        if (ExcelUtils.exportExcel(excel)) {
            FileUtils.download(filePath, response);
            FileUtils.delete(filePath);
        } else {
            String fullContentType = "text/plain;charset=UTF-8";
            response.setContentType(fullContentType);
            try {
                response.getWriter().write("导出Excel文件失败，请联系管理员");
                response.getWriter().flush();
            } catch (IOException e) {
                logger.error("导出Excel文件失败", e);
                return R.error("导出失败!");
            }
        }
        return R.ok("导出成功!");
    }
}
