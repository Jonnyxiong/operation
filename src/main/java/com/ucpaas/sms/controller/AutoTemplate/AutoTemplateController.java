package com.ucpaas.sms.controller.AutoTemplate;


import com.jsmsframework.audit.entity.JsmsAutoTemplate;
import com.jsmsframework.audit.service.JsmsAutoTemplateService;
import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.common.entity.JsmsExcel;
import com.jsmsframework.common.enums.AutoTemplateLevel;
import com.jsmsframework.common.enums.AutoTemplateStatus;
import com.jsmsframework.common.enums.AutoTemplateSubmitType;
import com.jsmsframework.common.enums.WebId;
import com.jsmsframework.common.enums.balckAndWhiteTemplate.TemplateLevel;
import com.jsmsframework.common.util.BeanUtil;
import com.jsmsframework.common.util.PageExportUtil;
import com.jsmsframework.user.audit.service.JsmsUserAutoTemplateService;
import com.jsmsframework.user.entity.JsmsAccount;
import com.jsmsframework.user.service.JsmsAccountService;
import com.jsmsframework.user.service.JsmsAgentInfoService;
import com.jsmsframework.user.service.JsmsUserService;
import com.ucpaas.sms.common.util.Collections3;
import com.ucpaas.sms.common.util.file.ExcelUtils;
import com.ucpaas.sms.controller.BaseController;
import com.ucpaas.sms.dto.JsmsAutoTemplateDTO;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.enums.SessionEnum;
import com.ucpaas.sms.model.Excel;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.AutoTemplateService;
import com.ucpaas.sms.util.*;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by xiongfenglin on 2017/9/9.
 * 智能模板报备
 */
@Controller
@RequestMapping(value="/autoTemplate")
public class AutoTemplateController extends BaseController {
    @Autowired
    private JsmsAutoTemplateService jsmsAutoTemplateService;
    @Autowired
    private JsmsUserService jsmsUserService;
    @Autowired
    private JsmsAccountService jsmsAccountService;
    @Autowired
    private JsmsUserAutoTemplateService jsmsUserAutoTemplateService;
    @Autowired
    private AutoTemplateService autoTemplateService;
    private static final Logger LOGGER = LoggerFactory.getLogger(AutoTemplateController.class);
    @GetMapping(path={"/autoTemplateQuery"})
    public ModelAndView autoTemplate(HttpSession session, HttpServletRequest request,ModelAndView mv){
        UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        mv.addObject("userId",user.getId());
        mv.addObject("max_export_num", ConfigUtils.max_export_excel_num);
        Map<String, Boolean> right = AgentUtils.hasMenuRight(user, "智能模板-znmb-286", "通用模板-tymb");
        mv.addObject("menus", right);

        if ((Boolean)right.get("znmb286")) {
            mv.setViewName("AutoTemplate/autoTemplate");
        } else if ((Boolean)right.get("tymb")) {
            mv.setViewName("AutoTemplate/comTemplate");
        } else {
            mv.setViewName("AutoTemplate/autoTemplate");
        }

        return mv;
    }

    @RequestMapping(path="/autoTemplateQuery",method = RequestMethod.POST)
    @ResponseBody
    public String autoTemplateQuery(HttpSession session, HttpServletRequest request,@RequestParam Map<String,String> params){
        UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        Map<String, Object> objectMap = new HashMap<>();
        // 模板ID
        Object obj = new Object();
        obj = request.getParameter("templateId");
        if (obj != null) {
            objectMap.put("templateId", obj);
        }
        //审核状态
        obj = request.getParameter("state");
        if (obj != null) {
            objectMap.put("state", obj);
        }
        // 用户帐号
        Set<String> clientIds = new HashSet<>();
        obj = request.getParameter("clientId");
        if (obj != null && StringUtils.isNotBlank(String.valueOf(obj))) {
            objectMap.put("clientId",obj);
        }else{
            List<JsmsAccount> data = jsmsAccountService.findAllListOfOperation(user.getId());
            if(data.size()>0){
                for (int i =0;i<data.size();i++){
                    clientIds.add(data.get(i).getClientid());
                }
            }else{
                clientIds.add("-1");
            }
            objectMap.put("clientIds",clientIds);
        }

        // 模板属性
        obj = request.getParameter("smsType");
        if (obj != null) {
            objectMap.put("smsType", obj);
        }
        // 模板类型
        obj =request.getParameter("templateType");
        if (obj != null) {
            objectMap.put("templateType", obj);
        }
        // 短信签名
        obj = request.getParameter("sign");
        if (obj != null) {
            objectMap.put("sign", obj);
        }
        // 模板内容
        obj = request.getParameter("content");
        if (obj != null) {
            objectMap.put("content", obj);
        }
        // 审核人
        obj =request.getParameter("adminName");
        if (obj != null) {
            objectMap.put("adminName", obj);
        }
        // 创建者
        obj =request.getParameter("userName");
        if (obj != null) {
            objectMap.put("userName", obj);
        }
        //提交来源
        obj = request.getParameter("submitType");
        if(obj != null ){
            objectMap.put("submitType",obj);
        }
        // 创建时间：开始时间
        String createStartTime = Objects.toString(request.getParameter("createStartTime"), "");
        String createEndTime = Objects.toString(request.getParameter("createEndTime"), "");
        if (StringUtils.isNotBlank(createStartTime)) {
            StringBuilder createStartTimes = new StringBuilder(createStartTime);
            String str = " 00:00:00";
            objectMap.put("createStartTime", String.valueOf(createStartTimes.append(str)));
        }

        // 创建时间：结束时间
        if (StringUtils.isNotBlank(createEndTime)) {
            StringBuilder createEndTimes = new StringBuilder(createEndTime);
            String str = " 23:59:59";
            objectMap.put("createEndTime", String.valueOf(createEndTimes.append(str)));
        }
        params.put("currentPage",params.get("page"));
        params.put("pageRowCount",params.get("rows"));
        JsmsPage jsmsPage = PageConvertUtil.paramToPage(params);
        jsmsPage.setParams(objectMap);
        jsmsPage.setOrderByClause("a.create_time DESC");
        AutoTemplateLevel autoTemplateLevel=AutoTemplateLevel.用户级别;//用户级别为1
        JsmsPage queryPage = jsmsUserAutoTemplateService.findListOfAutoTemplate(jsmsPage,WebId.运营平台.getValue(),autoTemplateLevel);
        PageContainer page  = PageConvertUtil.pageToContainer(queryPage);
        return JsonUtils.toJson(page);
    }
    @RequestMapping(path="/autoTemplateAdd",method = RequestMethod.GET )
    public ModelAndView autoTemplateAddView(HttpSession session, HttpServletRequest request, ModelAndView mv){
        mv.setViewName("AutoTemplate/autoTemplateAdd");
        return mv;
    }
    @RequestMapping(path="/autoTemplateModify",method = RequestMethod.GET)
    public ModelAndView autoTemplateAdd(HttpSession session, HttpServletRequest request, ModelAndView mv){
        JsmsAutoTemplate jsmsAutoTemplate = null;
        if(StringUtils.isNotBlank(request.getParameter("templateId"))){
            jsmsAutoTemplate= jsmsAutoTemplateService.getByTemplateId(Integer.parseInt(request.getParameter("templateId")));
        }
        if(jsmsAutoTemplate!=null){
            mv.addObject("templateId", jsmsAutoTemplate.getTemplateId());
            mv.addObject("clientId", jsmsAutoTemplate.getClientId());
            mv.addObject("smsType", jsmsAutoTemplate.getSmsType());
            mv.addObject("sign", jsmsAutoTemplate.getSign());
            mv.addObject("content", jsmsAutoTemplate.getContent());
            mv.addObject("templateType", jsmsAutoTemplate.getTemplateType());
            mv.addObject("state", jsmsAutoTemplate.getState());
        }
        mv.setViewName("AutoTemplate/autoTemplateAdd");
        return mv;
    }

    /**
     * 删除客户模板
     */
    @RequestMapping(value = "/autoTemplateDel",method = RequestMethod.POST)
    @ResponseBody
    public R autoTemplateDel(HttpSession session, HttpServletRequest request) {
        R r = new R();
        String templateIdStr = request.getParameter("templateId");
        Integer templateId = null;
        // 模板ID
        if (StringUtils.isNotBlank(templateIdStr)) {
            templateId  = Integer.parseInt(templateIdStr);
        }

        if (templateId == null) {
            r.error("模板ID不能为空");
            return r;
        }else{
            r = jsmsAutoTemplateService.delAutoTemplate(templateId);
            return r;
        }
    }
    /**
     * 查询所有的客户
     */
    @RequestMapping("/autoTemplateAccounts")
    @ResponseBody
    public List<JsmsAccount> getAccounts(HttpSession session, HttpServletRequest request) {
        // String clientId = request.getParameter("clientId");
        UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        List<JsmsAccount> data = jsmsAccountService.findAllListOfOperation(user.getId());
        return data;
    }
    /**
     * 新增客户模板
     */
    @RequestMapping("/autoTemplateSave")
    @ResponseBody
    public R autoTemplateSave(HttpSession session, HttpServletRequest request,@RequestParam Map<String,String> params) {
        boolean isMod = false;
        UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        JsmsAutoTemplate template = new JsmsAutoTemplate();
        R r = null;
        // 模板ID
        Object obj = request.getParameter("templateId");
        if (obj != null && obj != "") {
            isMod = true;
            template.setTemplateId(Integer.parseInt(obj.toString()));
        }

        // 用户帐号
        obj = request.getParameter("clientId");
        if (obj != null && obj != "") {
            template.setClientId(obj.toString());
        }

        // 模板类型
        obj = request.getParameter("templateType");
        if (obj != null && obj != "") {
            template.setTemplateType(Integer.parseInt(obj.toString()));
        }

        // 短信类型
        obj = request.getParameter("smsType");
        if (obj != null && obj != "") {
            template.setSmsType(Integer.parseInt(obj.toString()));
        }
        // 模板内容
        obj = request.getParameter("content");
        if (obj != null && obj != "") {
            template.setContent(obj.toString());
        }

        // 短信签名
        obj = request.getParameter("sign");
        if (obj != null && obj != "") {
            template.setSign(obj.toString());
        }
        if (StringUtils.isNotBlank(request.getParameter("state"))){
            template.setAdminId(user.getId());
            if (request.getParameter("state").equals("1")){
                template.setRemark(" ");
                template.setState(AutoTemplateStatus.待审核.getValue());
            }else if(request.getParameter("state").equals("3")){
                template.setRemark(" ");
                template.setState(AutoTemplateStatus.审核不通过.getValue());
            }
        }else{
            template.setState(AutoTemplateStatus.待审核.getValue());
        }
        template.setSubmitType(AutoTemplateSubmitType.平台提交.getValue());
        template.setWebId(3);
        template.setTemplateLevel(TemplateLevel.用户级别.getValue());
        JsmsAccount jsa = jsmsAccountService.getByClientId(template.getClientId());
        if (StringUtils.isBlank(template.getClientId()) || jsmsAccountService.getByClientId(template.getClientId()) == null){
            return R.error("用户账号不存在");
        }
        if(template.getWebId()==3){
            if(jsa.getBelongSale().longValue()!=user.getId().longValue()){
                return R.error("用户账号不属于该销售");
            }
        }
        if (isMod) {
            if (template == null){
                r.error("智能模板不能为空");
                return r;
            }else{
                if (template.getTemplateId() == null){
                    r.error("智能模板的模板ID不能为空");
                    return r;
                }else{
                    r = jsmsAutoTemplateService.modifyTemplate(template);
                }
            }
        } else {
            template.setUserId(String.valueOf(user.getId()));
            r = jsmsAutoTemplateService.addAutoTemplate(template);
        }
        return r;
    }
    @RequestMapping("/downloadExcelTemplate")
    public void downloadExcelTemplate(HttpSession session, HttpServletRequest request,HttpServletResponse response) {
        String path = request.getServletContext().getRealPath("/template/批量添加智能模板.xls");
        FileUtils.download(path,response);
    }

    /*
    * 批量导出报表
    *
    * */
    @PostMapping("/autoTemplateExport")
    @ResponseBody
    public ResultVO agentFinanceExport(HttpSession session,@RequestParam Map<String,String> params, HttpServletRequest request,
                                   HttpServletResponse response) throws ParseException {
        UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        List<Map<String, Object>> exportList = new ArrayList<>();
        Map<String, Object> jsmsAutoTemplateMap = new HashMap<>();
//        String filePath = ConfigUtils.save_path + "/智能模板报表.xls";
        StringBuilder filePath = new StringBuilder(ConfigUtils.save_path);
        if(!ConfigUtils.save_path.endsWith("/")){
            filePath.append("/");
        }
        filePath.append("智能模板报表").append(".xls").append("$$$").append(UUID.randomUUID());
        String path = filePath.toString();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<JsmsAutoTemplateDTO> list = new ArrayList<>();
        StringBuilder parstrUserId = new StringBuilder();
        JsmsExcel jsmsExcel = new  JsmsExcel();
        jsmsExcel.setFilePath(path);
        jsmsExcel.setTitle("智能模板报表");
        // 用户帐号
        Set<String> clientIds = new HashSet<>();
        // 创建时间：开始时间
        String createStartTime = Objects.toString( params.get("createStartTime"), "");
        String createEndTime = Objects.toString( params.get("createEndTime"), "");
        if (StringUtils.isNotBlank(createStartTime)) {
            StringBuilder createStartTimes = new StringBuilder(createStartTime);
            String str = " 00:00:00";
            params.put("createStartTime", String.valueOf(createStartTimes.append(str)));
        }

        // 创建时间：结束时间
        if (StringUtils.isNotBlank(createEndTime)) {
            StringBuilder createEndTimes = new StringBuilder(createEndTime);
            String str = " 23:59:59";
            params.put("createEndTime", String.valueOf(createEndTimes.append(str)));
        }
        if (params.get("clientId") != null && StringUtils.isNotBlank(String.valueOf(params.get("clientId")))) {
            clientIds.add(params.get("clientId"));
        }else{
            List<JsmsAccount> data = jsmsAccountService.findAllListOfOperation(user.getId());
            if(data.size()>0){
                for (int i =0;i<data.size();i++){
                    clientIds.add(data.get(i).getClientid());
                }
            }else{
                clientIds.add("-1");
            }
        }
        JsmsPage jsmsPage = new JsmsPage();
        jsmsPage.setParams(params);
        jsmsPage.getParams().put("clientIds",clientIds);
//        jsmsPage.setRows(-1);
        jsmsPage.setMaxQueryLimit(-1);
        jsmsPage.setOrderByClause("a.create_time DESC");
        jsmsExcel.addHeader(20, "模板ID", "templateId");
        jsmsExcel.addHeader(20, "用户账号", "clientId");
        jsmsExcel.addHeader(20, "模板属性", "smsTypeStr");
        jsmsExcel.addHeader(20, "模板类型", "templateTypeStr");
        jsmsExcel.addHeader(20, "模板内容", "content");
        jsmsExcel.addHeader(20, "短信签名", "sign");
        jsmsExcel.addHeader(20, "创建人", "userName");
        jsmsExcel.addHeader(20, "创建时间", "createTimeStr");
        jsmsExcel.addHeader(20, "审核状态", "stateStr");
        jsmsExcel.addHeader(20, "原因", "remark");
        jsmsExcel.addHeader(20, "审核人", "adminName");
        jsmsExcel.addHeader(20, "更新时间", "updateTimeStr");
            AutoTemplateLevel autoTemplateLevel=AutoTemplateLevel.用户级别;//用户级别为1
        ResultVO resultVO = PageExportUtil.instance().exportPage(autoTemplateService, jsmsPage,jsmsExcel, "findListOfAutoTemplate");
//        jsmsExcel.setDataList(exportList);
        if(resultVO.isSuccess()){

//            com.jsmsframework.common.util.FileUtils.download("智能模板报表.xls", (String) resultVO.getData(),response);
//            com.jsmsframework.common.util.FileUtils.delete(path);
            return resultVO;
        }else {
            return resultVO;
        }
    }

   /* public void agentFinanceExport(HttpSession session,@RequestParam Map<String,String> params, HttpServletRequest request,
                                   HttpServletResponse response) throws ParseException {
        UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        List<Map<String, Object>> exportList = new ArrayList<>();
        Map<String, Object> jsmsAutoTemplateMap = new HashMap<>();
        String filePath = ConfigUtils.save_path + "/智能模板报表.xls";
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<JsmsAutoTemplateDTO> list = new ArrayList<>();
        StringBuilder parstrUserId = new StringBuilder();
        Excel excel = new Excel();
        excel.setFilePath(filePath);
        excel.setTitle("智能模板报表");
        // 用户帐号
        Set<String> clientIds = new HashSet<>();
        // 创建时间：开始时间
        String createStartTime = Objects.toString( params.get("createStartTime"), "");
        String createEndTime = Objects.toString( params.get("createEndTime"), "");
        if (StringUtils.isNotBlank(createStartTime)) {
            StringBuilder createStartTimes = new StringBuilder(createStartTime);
            String str = " 00:00:00";
            params.put("createStartTime", String.valueOf(createStartTimes.append(str)));
        }

        // 创建时间：结束时间
        if (StringUtils.isNotBlank(createEndTime)) {
            StringBuilder createEndTimes = new StringBuilder(createEndTime);
            String str = " 23:59:59";
            params.put("createEndTime", String.valueOf(createEndTimes.append(str)));
        }
        if (params.get("clientId") != null && StringUtils.isNotBlank(String.valueOf(params.get("clientId")))) {
            clientIds.add(params.get("clientId"));
        }else{
            List<JsmsAccount> data = jsmsAccountService.findAllListOfOperation(user.getId());
            if(data.size()>0){
                for (int i =0;i<data.size();i++){
                    clientIds.add(data.get(i).getClientid());
                }
            }else{
                clientIds.add("-1");
            }
        }
        JsmsPage jsmsPage = new JsmsPage();
        jsmsPage.setParams(params);
        jsmsPage.getParams().put("clientIds",clientIds);
//        jsmsPage.setRows(-1);
        jsmsPage.setMaxQueryLimit(-1);
        jsmsPage.setOrderByClause("a.create_time DESC");
        excel.addHeader(20, "模板ID", "templateId");
        excel.addHeader(20, "用户账号", "clientId");
        excel.addHeader(20, "模板属性", "smsTypeStr");
        excel.addHeader(20, "模板类型", "templateTypeStr");
        excel.addHeader(20, "模板内容", "content");
        excel.addHeader(20, "短信签名", "sign");
        excel.addHeader(20, "创建人", "userName");
        excel.addHeader(20, "创建时间", "createTimeStr");
        excel.addHeader(20, "审核状态", "stateStr");
        excel.addHeader(20, "原因", "remark");
        excel.addHeader(20, "审核人", "adminName");
        excel.addHeader(20, "更新时间", "updateTimeStr");
        AutoTemplateLevel autoTemplateLevel=AutoTemplateLevel.用户级别;//用户级别为1
        JsmsPage queryPage = jsmsUserAutoTemplateService.findListOfAutoTemplate(jsmsPage,WebId.运营平台.getValue(),autoTemplateLevel);
        for (Object temp : queryPage.getData()) {
            JsmsAutoTemplateDTO jsmsAutoTemplateDTO = new JsmsAutoTemplateDTO();
            BeanUtils.copyProperties(temp , jsmsAutoTemplateDTO);
            com.jsmsframework.user.entity.JsmsUser jsmsUser = jsmsUserService.getById(String.valueOf(jsmsAutoTemplateDTO.getAdminId()));
            if(jsmsUser != null){
                jsmsAutoTemplateDTO.setAdminName(jsmsUser.getRealname());
            }
            jsmsUser = jsmsUserService.getById(jsmsAutoTemplateDTO.getUserId());
            if(jsmsUser != null){
                jsmsAutoTemplateDTO.setUserName(jsmsUser.getRealname());
            }
            if(jsmsAutoTemplateDTO.getState()==0){
                jsmsAutoTemplateDTO.setStateStr("待审核");
            }else if(jsmsAutoTemplateDTO.getState()==1){
                jsmsAutoTemplateDTO.setStateStr("审核通过");
            }else if(jsmsAutoTemplateDTO.getState()==3){
                jsmsAutoTemplateDTO.setStateStr("审核不通过");
            }
            if(jsmsAutoTemplateDTO.getSmsType()==10){
                jsmsAutoTemplateDTO.setSmsTypeStr("行业");
            }else if(jsmsAutoTemplateDTO.getSmsType()==11){
                jsmsAutoTemplateDTO.setSmsTypeStr("会员营销");
            }
            if(jsmsAutoTemplateDTO.getTemplateType()==0){
                jsmsAutoTemplateDTO.setTemplateTypeStr("固定模板");
            }else if(jsmsAutoTemplateDTO.getTemplateType()==1){
                jsmsAutoTemplateDTO.setTemplateTypeStr("变量模板");
            }
            if(jsmsAutoTemplateDTO.getCreateTime()!=null){
                jsmsAutoTemplateDTO.setCreateTimeStr(formatter.format(jsmsAutoTemplateDTO.getCreateTime()));
            }
            if(jsmsAutoTemplateDTO.getUpdateTime()!=null){
                jsmsAutoTemplateDTO.setUpdateTimeStr(formatter.format(jsmsAutoTemplateDTO.getUpdateTime()));
            }
            jsmsAutoTemplateMap = BeanUtil.beanToMap(jsmsAutoTemplateDTO, true);
            exportList.add(jsmsAutoTemplateMap);
        }
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
                LOGGER.error("导出Excel文件失败", e);
            }
        }
    }*/

    /**
     * 批量添加智能模板
     */
    @RequestMapping("/addAutoTemplateBatch")
    @ResponseBody
    public R importOperationExcel(HttpSession session,HttpServletRequest request,@RequestParam("excel") CommonsMultipartFile file) {
        R r = new R();
        UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        String fileName = file.getOriginalFilename();
        LOGGER.debug("importOperationExcel[fileName={}]", fileName);
        if (StringUtils.isBlank(file.getContentType())) {
            r.setCode(500);
            r.setMsg("请先选择导入Excel");
            return r;
        }
        if (file.getSize() > 2097152L) {
            r.setCode(500);
            r.setMsg("您选择的文件大于2M,请将excel拆分后重新导入");
            return r;
        }
        LOGGER.debug("导入文件的类型 ----> {}", file.getContentType());
        String path = new StringBuilder(ConfigUtils.save_path).append("/").toString();
        FileUtils.delete(path);
        FileUtils.upload2(path, fileName, file);
        // 获得Excel文件中的数据
        LOGGER.debug("智能模板Excel 读取完成  ----------> 开始解析");
        // r = jsmsAutoTemplateService.addOperationTemplateBatch(user.getId(), path);
        r = jsmsUserAutoTemplateService.addAutoTemplateBatch(null,null,String.valueOf(user.getId()), path,WebId.运营平台.getValue());
        return r;
    }
    @RequestMapping("/exportImportResult")
    @ResponseBody
    public String exportError(HttpSession session,HttpServletResponse response){
        String msg = "";
        UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        String filePath = ConfigUtils.save_path +"/import"+ "/批量添加智能模板结果-userid-" + user.getId()+".xls";
        File file = new File(filePath);
        if(file.exists()){
            FileUtils.download(filePath,response);
            msg = "下载成功";
        }else{
            msg = "文件过期、不存在或者已经被管理员删除";
        }
        return msg;
    }
    private File multipartToFile(MultipartFile multfile) throws IOException {
        CommonsMultipartFile cf = (CommonsMultipartFile)multfile;
        //这个myfile是MultipartFile的
        DiskFileItem fi = (DiskFileItem) cf.getFileItem();
        File file = fi.getStoreLocation();
        return file;
    }
}
