package com.ucpaas.sms.controller.statistics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.jsmsframework.channel.entity.JsmsChannel;
import com.jsmsframework.channel.entity.JsmsComplaintListExt;
import com.jsmsframework.channel.exception.JsmsComplaintListException;
import com.jsmsframework.channel.service.JsmsChannelService;
import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.enums.OperatorType;
import com.jsmsframework.common.enums.SmsTypeEnum;
import com.jsmsframework.user.entity.JsmsAccount;
import com.jsmsframework.user.entity.JsmsMenu;
import com.jsmsframework.user.service.JsmsAccountService;
import com.jsmsframework.user.service.JsmsMenuService;
import com.ucpaas.sms.common.util.DateUtils;
import com.ucpaas.sms.common.util.file.ExcelUtils;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.enums.SessionEnum;
import com.ucpaas.sms.model.Excel;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.statistic.ComplaintListService;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.FileUtils;
import com.ucpaas.sms.util.ObjectToMapUtil;
import com.ucpaas.sms.util.PageConvertUtil;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
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
 * created by xiaoqingwen on 2018/1/9 18:14
 * 投诉管理分析统计
 */
@Controller
@RequestMapping("/operation/statistics/complaint")
public class ComplaintListController {

    private static Logger logger = LoggerFactory.getLogger(ComplaintListController.class);
    @Autowired
    private ComplaintListService complaintListService;
    @Autowired
    private JsmsChannelService jsmsChannelService;
    @Autowired
    private JsmsAccountService jsmsAccountService;
    @Autowired
    private JsmsMenuService jsmsMenuService;

    /**
     * 跳转到投诉明细管理
     */
    @RequestMapping(path = "/complaints", method = RequestMethod.GET)
    public ModelAndView complaints(ModelAndView mv,Integer channelId,String dateStr,String clientId,Integer operatorstype,Integer smstype) {
        String sendTimeStart = null;
        String sendTimeEnd = null;
        mv.addObject("channelId",channelId);
        //获取通道名称-->channelName
        if(channelId!=null){
            JsmsChannel jsmsChannel = jsmsChannelService.getByCid(channelId);
            //存在此通道
            if(jsmsChannel!=null){
                mv.addObject("channelName",jsmsChannel.getChannelname());
            }else{
                //不存在此通道
                mv.addObject("channelName",null);
            }
        }else{
            mv.addObject("channelName",null);
        }
        mv.addObject("clientId",clientId);
        //获取客户名称-->name
        if(StringUtils.isNotBlank(clientId)){
            JsmsAccount jsmsAccount = jsmsAccountService.getByClientId(clientId);
            //客户存在
            if(jsmsAccount!=null){
                mv.addObject("name",jsmsAccount.getName());
            }else{
                //客户不存在
                mv.addObject("name",null);
            }
        }else{
            mv.addObject("name",null);
        }
        mv.addObject("operatorstype",operatorstype);
        //获取运营商类型
        if(operatorstype!=null){
            mv.addObject("operatorstypeDesc", OperatorType.getDescByValue(operatorstype));
        }else{
            mv.addObject("operatorstypeDesc",null);
        }
        mv.addObject("smstype",smstype);
        //获取运短信类型
        if(smstype!=null){
            mv.addObject("smstypeDesc", SmsTypeEnum.getDescByValue(smstype));
        }else{
            mv.addObject("smstypeDesc",null);
        }

        //判断dateStr是否有值(2018/01)
        if(StringUtils.isNotBlank(dateStr) && dateStr.length()==7){
            dateStr=dateStr.substring(0,4)+"-"+dateStr.substring(5);
            try {
                Date date = DateUtils.parseDate(dateStr, "yyyy-MM");
                //获取指定月份的开始和结束时间
                Map<String, Object> data = getDate(date);
                //sendTimeStart=data.get("");
                sendTimeStart=(String) data.get("sendTimeStart");
                sendTimeEnd=(String) data.get("sendTimeEnd");
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("格式化时间有误!");
            }
        }
        mv.addObject("sendTimeStart",sendTimeStart);
        mv.addObject("sendTimeEnd",sendTimeEnd);
        mv.setViewName("/statistic/complaints/list");
        JsmsMenu menu=new JsmsMenu();
        menu.setMenuName("投诉明细管理");
        menu.setLevel(2);
        menu.setMenuType("1");
        menu.setWebId(3);
        menu.setStatus("1");
        //按钮权限控制
        List<JsmsMenu> list = jsmsMenuService.findList(menu);
        if(list!=null && list.size()>0){
            mv.addObject("jsmsMenu", list.get(0));
        }else{
            logger.debug("权限获取失败!");
            mv.addObject("jsmsMenu", null);
        }
        return mv;
    }

    /**
     * 获取客户
     * @return
     */
    @ApiOperation(value = "运营分析(获取客户列表)", notes = "运营分析", tags = "运营分析", response = JsmsPage.class)
    @RequestMapping(path = "queryListForAccount", method = RequestMethod.POST)
    @ResponseBody
    public JsmsPage queryListForAccount(String condition,Integer page, Integer rows) throws JsonProcessingException {

        JsmsPage jsmsPage = new JsmsPage<>();
        if(page!=null){
            jsmsPage.setPage(page);
        }
        if(rows!=null){
            jsmsPage.setRows(rows);
        }
        Map<String,Object> params=new HashMap<>();
        params.put("condition",condition);
        jsmsPage.setParams(params);
        return complaintListService.queryListForAccount(jsmsPage);
    }

    /**
     * 获取归属销售
     * @return
     */
    @ApiOperation(value = "运营分析(获取销售列表)", notes = "运营分析", tags = "运营分析", response = JsmsPage.class)
    @RequestMapping(path = "queryListForUser", method = RequestMethod.POST)
    @ResponseBody
    public JsmsPage queryListForUser(HttpSession session,String condition,Integer page, Integer rows) {
        UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        Long id = userSession.getId();
        JsmsPage jsmsPage=new JsmsPage();
        if(page!=null){
            jsmsPage.setPage(page);
        }
        if(rows!=null){
            jsmsPage.setRows(rows);
        }
        return complaintListService.queryListForUser(id,condition,jsmsPage);
    }

    /**
     * 获取投诉通道
     * @return
     */
    @ApiOperation(value = "运营分析(获取通道列表)", notes = "运营分析", tags = "运营分析", response = JsmsPage.class)
    @RequestMapping(path = "queryListForChannel", method = RequestMethod.POST)
    @ResponseBody
    public JsmsPage queryListForChannel(String condition,Integer page, Integer rows) {

        JsmsPage jsmsPage=new JsmsPage();
        if(page!=null){
            jsmsPage.setPage(page);
        }
        if(rows!=null){
            jsmsPage.setRows(rows);
        }
        Map<String,Object> params=new HashMap<>();
        params.put("condition",condition);
        jsmsPage.setParams(params);
        return complaintListService.queryListForChannel(jsmsPage);
    }

    /**
     * 搜索投诉,分页
     *
     * @param rows
     * @param page
     * @return
     */
    @ApiOperation(value = "运营分析(投诉明细管理)", notes = "运营分析", tags = "运营分析", response = PageContainer.class)
    @RequestMapping(path = "searchComplaint", method = RequestMethod.POST)
    @ResponseBody
    public PageContainer searchComplaint(String sendTimeStart, String sendTimeEnd, String sign, String content, String clientId, Long belongSale, Integer channelId, Integer operatorstype, String realname,String operatorStr, String createTimeStart, String createTimeEnd, String page, String rows) {
        JsmsPage jsmsPage = new JsmsPage<>();
        Map<String,Object> params=new HashMap<>();

        //塞入必要的参数
        setParamas(sendTimeStart, sendTimeEnd, sign, content, clientId, belongSale, channelId, operatorstype, realname,operatorStr, createTimeStart, createTimeEnd, params);

        if (StringUtils.isNotBlank(rows)) {
            jsmsPage.setRows(Integer.valueOf(rows));
        }
        if (StringUtils.isNotBlank(page)) {
            jsmsPage.setPage(Integer.valueOf(page));
        }
        //查询出状态为正常的数据
        params.put("status",0);
        //按创建时间排序
        //params.put("orderBy", "create_time desc");
        params.put("group", "group");
        //jsmsPage.setGroupByClause("channel_id,send_time,phone,sign,content,client_id,smstype");
        jsmsPage.setOrderByClause("create_time desc,id");
        jsmsPage.setParams(params);
        JsmsPage<JsmsComplaintListExt> jsmsPageComplaint = complaintListService.queryList(jsmsPage);
        PageContainer container = PageConvertUtil.pageToContainer(jsmsPageComplaint);
        return container;
    }

    private void setParamas(String sendTimeStart, String sendTimeEnd, String sign, String content, String clientId, Long belongSale, Integer channelId, Integer operatorstype, String realname,String operatorStr, String createTimeStart, String createTimeEnd, Map<String, Object> params) {
        //将值塞入参数中
        params.put("sendTimeStart",sendTimeStart);
        params.put("sendTimeEnd",sendTimeEnd);
        params.put("signExt",sign);
        params.put("contentExt",content);
        if(null!=clientId){
            String clien = StringUtils.deleteWhitespace(clientId);
            params.put("clientId",clien);
            logger.debug("客户id,clientid-->{}",clien);
        }else{
            params.put("clientId",null);
        }
        params.put("belongSale",belongSale);
        params.put("channelId",channelId);
        params.put("operatorstype",operatorstype);
        params.put("realname",realname);
        params.put("operatorStr",operatorStr);
        if(StringUtils.isNotBlank(createTimeStart)){
            params.put("createTimeStart",createTimeStart+" 00:00:00");
        }else{
            params.put("createTimeStart",createTimeStart);
        }
        if(StringUtils.isNotBlank(createTimeEnd)){
            params.put("createTimeEnd",createTimeEnd+" 23:59:59");
        }else{
            params.put("createTimeEnd",createTimeEnd);
        }
    }

    /**
     * 根据id删除投诉
     */
    @ApiOperation(value = "运营分析(删除投诉)", notes = "运营分析", tags = "运营分析", response = R.class)
    @RequestMapping(path = "deleteById", method = RequestMethod.POST)
    @ResponseBody
    public R deleteById(Integer id, HttpSession session){

        //获取用户信息
        UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        R r;
        try {
            r= complaintListService.deleteById(id,user.getId());
        } catch (JsmsComplaintListException e) {
            logger.error("删除失败!",e);
            return R.error(e.getMessage());
        }catch (Exception e) {
            logger.error("删除异常!",e);
            return R.error("删除异常!");
        }
        return r;
    }

    /**
     * 获取指定月份的开始和结束时间
     * @param date
     * @return
     * @throws ParseException
     */
    public Map<String,Object> getDate(Date date) throws ParseException {
        Map<String,Object> map=new HashMap<>();
        Calendar c = Calendar.getInstance();
        //c.add(Calendar.MONTH, -1);
        //Date date = DateUtils.parseDate("2018-02", "yyyy-MM");
        c.setTime(date);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String gtimelast = sdf.format(c.getTime()); //月分
        //System.out.println(gtimelast);

        //SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-01  00:00:00");
        SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM-01");
        String sendTimeStart = sdf2.format(c.getTime()); //指定月第一天
        map.put("sendTimeStart",sendTimeStart);

        int lastMonthMaxDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);
        //c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), lastMonthMaxDay, 23, 59, 59);
        c.set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), lastMonthMaxDay);

        //按格式输出
        String sendTimeEnd = sdf.format(c.getTime()); //指定月最后一天
        map.put("sendTimeEnd",sendTimeEnd);
        return map;
    }

    /*
    * 批量导出报表
    * */
    @ApiOperation(value = "运营分析(导出投诉)", notes = "运营分析", tags = "运营分析")
    @PostMapping("/complaintListExport")
    @ResponseBody
    public void complaintListExport(String sendTimeStart, String sendTimeEnd, String sign, String content, String clientId, Long belongSale, Integer channelId, Integer operatorstype, String realname,String operatorStr, String createTimeStart, String createTimeEnd,HttpServletResponse response){
        JsmsPage jsmsPage = new JsmsPage<>();
        Map<String,Object> params=new HashMap<>();
        String filePath = ConfigUtils.save_path + "/投诉明细管理报表.xls";

        //塞入必要的参数
        setParamas(sendTimeStart, sendTimeEnd, sign, content, clientId, belongSale, channelId, operatorstype, realname,operatorStr, createTimeStart, createTimeEnd, params);
        //jsmsPage.setParams(params);

        Excel excel = new Excel();
        excel.setPageRowCount(2000); // 设置每页excel显示2000行
        excel.setFilePath(filePath);
        excel.setTitle("投诉明细管理报表");

        //构建excel表
        excel.addHeader(20, "发送日期", "sendTimeStr");
        excel.addHeader(20, "投诉手机号", "phone");
        excel.addHeader(20, "对应签名", "sign");
        excel.addHeader(20, "投诉内容", "content");
        excel.addHeader(20, "备注", "remark");
        excel.addHeader(20, "客户账号", "clientId");
        excel.addHeader(20, "客户名称", "name");
        excel.addHeader(20, "短信类型", "smsTypeStr");
        excel.addHeader(20, "所属销售", "realname");
        excel.addHeader(20, "通道号", "channelId");
        excel.addHeader(20, "通道名称", "channelname");
        excel.addHeader(20, "运营商", "operatorstypeStr");
        excel.addHeader(20, "录入者", "operatorStr");
        excel.addHeader(20, "录入时间", "createTimeStr");

        //List<JsmsComplaintListExt> list = complaintListService.searchComplaint(jsmsPage);
        //按创建时间排序
        params.put("orderBy", "create_time desc,id");
        //将非删除状态的查出来
        params.put("status",0);
        //需要分组去重
        params.put("group", "group");
        List<JsmsComplaintListExt> list = complaintListService.findList(params);
        List<Map<String, Object>> ll=new ArrayList<>();
        if(list!=null && list.size()>0){
            for (int i=0;i<list.size();i++){
                JsmsComplaintListExt complaintListExt = list.get(i);
                try {
                    Map<String, Object> map = ObjectToMapUtil.objectToMap(complaintListExt);
                    ll.add(map);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("对象转map失败!失败id为"+complaintListExt.getId());
                }
            }
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
            }
        }
    }

    /**
     * 批量添加投诉明细(导入)
     */
    @ApiOperation(value = "运营分析(批量添加投诉明细)", notes = "运营分析", tags = "运营分析", response = R.class)
    @RequestMapping(path = "addComplaintBatch", method = RequestMethod.POST)
    @ResponseBody
    public R addComplaintBatch(HttpSession session,@RequestParam("excel") CommonsMultipartFile file) {
        R r;
        UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        String fileName = file.getOriginalFilename();
        logger.debug("importOperationExcel[fileName={}]", fileName);
        if (StringUtils.isBlank(file.getContentType())) {
            return R.error("请先选择导入Excel");
        }
        if (file.getSize() > 2097152L) {
            logger.debug("文件过大,大小为-->{}",file.getSize());
            return R.error("您选择的文件大于2M,请将excel拆分后重新导入");
        }
        if(!fileName.contains("xlsx")){
            logger.debug("模版格式需为xlsx格式,您的文件名为--->{}",fileName);
            return R.error("模版格式需为xlsx格式,您的文件名为--->"+fileName);
        }
        logger.debug("导入文件的类型 ----> {}", file.getContentType());
        String path = new StringBuilder(ConfigUtils.save_path).append("/").toString();
        FileUtils.delete(path);
        FileUtils.upload2(path, fileName, file);
        // 获得Excel文件中的数据
        logger.debug("投诉明细模板Excel 读取完成  ----------> 开始解析");
        r= complaintListService.addComplaintBatch(String.valueOf(user.getId()), path);
        return r;
    }

    /**
     * 下载excel模版
     */
    @RequestMapping("downloadExcelComplaint")
    public void downloadExcelComplaint(HttpSession session, HttpServletRequest request,HttpServletResponse response) {
        String path = request.getServletContext().getRealPath("/template/投诉明细管理报表.xlsx");
        FileUtils.download(path,response);
    }

    /**
     * 下载导入结果
     * @param session
     * @param response
     * @return
     */
    @RequestMapping("exportImportResult")
    @ResponseBody
    public String exportError(HttpSession session,HttpServletResponse response){
        String msg = "";
        UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        String filePath = ConfigUtils.save_path +"/import"+ "/批量添加投诉明细结果-userid-" + user.getId()+".xls";
        File file = new File(filePath);
        if(file.exists()){
            FileUtils.download(filePath,response);
            msg = "下载成功";
        }else{
            msg = "文件过期、不存在或者已经被管理员删除";
        }
        return msg;
    }
}
