package com.ucpaas.sms.controller.statistics;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.user.entity.JsmsMenu;
import com.jsmsframework.user.service.JsmsMenuService;
import com.ucpaas.sms.common.util.file.ExcelUtils;
import com.ucpaas.sms.entity.po.JsmsChannelOperationStatisticsExt;
import com.ucpaas.sms.enums.ChannelOperationEnum;
import com.ucpaas.sms.model.Excel;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.admin.AuthorityService;
import com.ucpaas.sms.service.statistic.ChannelOperationStatisticsService;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.FileUtils;
import com.ucpaas.sms.util.ObjectToMapUtil;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * created by xiaoqingwen on 2018/1/11 10:09
 * 通道运营分析
 */
@Controller
@RequestMapping("/operation/statistics/channel")
public class ChannelOperationStatisticsController {

    private static Logger logger = LoggerFactory.getLogger(ChannelOperationStatisticsController.class);
    @Autowired
    private ChannelOperationStatisticsService channelOperationStatisticsService;
    @Autowired
    private JsmsMenuService jsmsMenuService;

    /**
     * 跳转到通道运营分析
     */
    @RequestMapping(path = "/channelList", method = RequestMethod.GET)
    public ModelAndView channelList(ModelAndView mv) {
        mv.setViewName("/statistic/channelOperation/list");
        //按钮权限控制
        JsmsMenu menu=new JsmsMenu();
        menu.setMenuName("通道运营分析");
        menu.setLevel(2);
        menu.setMenuType("1");
        menu.setWebId(3);
        menu.setStatus("1");
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
     * 搜索通道运营分析,分页
     *
     * @param rows
     * @param page
     * @return
     */
    @ApiOperation(value = "运营分析(通道运营分析)", notes = "运营分析", tags = "运营分析", response = PageContainer.class)
    @RequestMapping(path = "searchChannelOperationStatistics", method = RequestMethod.POST)
    @ResponseBody
    public JsmsPage<JsmsChannelOperationStatisticsExt> searchChannelOperationStatistics(Integer channelId, Integer operatorstype, Integer ownerType, String dateStart, String dateEnd, Integer sort, String page, String rows) {
        JsmsPage jsmsPage = new JsmsPage<>();
        Map<String,Object> params=new HashMap<>();
        //塞入必要的参数
        setParamas(channelId, operatorstype, ownerType, dateStart, dateEnd, sort, jsmsPage, params);

        if (StringUtils.isNotBlank(rows)) {
            jsmsPage.setRows(Integer.valueOf(rows));
        }
        if (StringUtils.isNotBlank(page)) {
            jsmsPage.setPage(Integer.valueOf(page));
        }
        //应倪大佬要求:当提交条数和明确成功条数同时为0的时候，整条数据不显示
        params.put("submitTotalAndReportsuccess","submitTotalAndReportsuccess");
        jsmsPage.setParams(params);
        return channelOperationStatisticsService.queryList(jsmsPage);
    }

    /*
    * 批量导出报表
    *
    * */
    @ApiOperation(value = "运营分析(导出通道)", notes = "运营分析", tags = "运营分析")
    @PostMapping("/channelOperationStatisticsExport")
    @ResponseBody
    public void channelOperationStatisticsExport(Integer channelId,Integer operatorstype,Integer ownerType,String dateStart,String dateEnd,Integer sort,HttpServletResponse response){
        JsmsPage jsmsPage = new JsmsPage<>();
        Map<String,Object> params=new HashMap<>();
        String filePath = ConfigUtils.save_path + "/通道运营分析.xls";
        //塞入必要的参数
        setParamas(channelId, operatorstype, ownerType, dateStart, dateEnd, sort, jsmsPage, params);
        //jsmsPage.setParams(params);

        Excel excel = new Excel();
        excel.setPageRowCount(2000); // 设置每页excel显示2000行
        excel.setFilePath(filePath);
        excel.setTitle("通道运营分析");

        //构建excel表
        excel.addHeader(20, "通道号", "channelId");
        excel.addHeader(20, "通道名称", "channelname");
        excel.addHeader(20, "运营商", "operatorstypeStr");
        excel.addHeader(20, "提交条数", "submitTotal");
        excel.addHeader(20, "明确成功条数", "reportsuccess");
        excel.addHeader(20, "发送成功率", "sendSuccessRatioStr");
        excel.addHeader(20, "低消条数", "lowConsumeNumber");
        excel.addHeader(20, "低消完成率", "lowConsumeRatioStr");
        excel.addHeader(20, "投诉个数", "complaintNumber");
        excel.addHeader(20, "投诉率（百万分之一）", "complaintRatioStr");
        excel.addHeader(20, "投诉系数（百万分之一）", "complaintCoefficientStr");
        excel.addHeader(20, "投诉差异值（百万分之一）", "complaintDifferenceStr");
        excel.addHeader(20, "成本单价", "costpriceStr");
        excel.addHeader(20, "所属商务", "realname");
        excel.addHeader(20, "通道来源", "ownerTypeStr");
        excel.addHeader(20, "月份", "dateStr");

        //List<JsmsChannelOperationStatisticsExt> list=channelOperationStatisticsService.searchChannelOperationStatistics(jsmsPage);

        //应倪大佬要求:当提交条数和明确成功条数同时为0的时候，整条数据不显示
        params.put("submitTotalAndReportsuccess","submitTotalAndReportsuccess");
        List<JsmsChannelOperationStatisticsExt> list= channelOperationStatisticsService.findList(params);
        List<Map<String, Object>> ll=new ArrayList<>();
        if(list!=null && list.size()>0){
            for (int i=0;i<list.size();i++){
                JsmsChannelOperationStatisticsExt channelOperationStatisticsExt = list.get(i);
                try {
                    Map<String, Object> map = ObjectToMapUtil.objectToMap(channelOperationStatisticsExt);
                    ll.add(map);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("对象转map失败!失败id为{}",channelOperationStatisticsExt.getId());
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
    private void setParamas(Integer channelId, Integer operatorstype, Integer ownerType, String dateStart, String dateEnd, Integer sort, JsmsPage<JsmsChannelOperationStatisticsExt> jsmsPage, Map<String, Object> params) {
        //将值塞入参数中
        params.put("channelId",channelId);
        params.put("operatorstype",operatorstype);
        params.put("ownerType",ownerType);
        //dateStart格式为yyyy-MM
        if(StringUtils.isBlank(dateStart) || dateStart.length()!=7){
            params.put("dateStart",null);
        }else{
            params.put("dateStart",Integer.valueOf(dateStart.substring(0, 4) + dateStart.substring(5)));
        }
        if(StringUtils.isBlank(dateEnd) || dateEnd.length()!=7){
            params.put("dateEnd",null);
        }else{
            params.put("dateEnd",Integer.valueOf(dateEnd.substring(0, 4) + dateEnd.substring(5)));
        }
        //jsmsPage.setOrderByClause(sort);
        //排序需要另外处理
        //按低消完成率由高到低-->1
        if((ChannelOperationEnum.低消完成率由高到低.getValue()).equals(sort)){
            jsmsPage.setOrderByClause("low_consume_ratio DESC,id");
            params.put("orderBy","low_consume_ratio DESC,id");
        }
        //按低消完成率由低到高-->2
        if((ChannelOperationEnum.低消完成率由低到高.getValue()).equals(sort)){
            jsmsPage.setOrderByClause("low_consume_ratio asc,id");
            params.put("orderBy","low_consume_ratio asc,id");
        }
        //按投诉率由高到低-->3
        if((ChannelOperationEnum.投诉率由高到低.getValue()).equals(sort)){
            jsmsPage.setOrderByClause("complaint_ratio desc,id");
            params.put("orderBy","complaint_ratio desc,id");
        }
        //按投诉率由低到高-->4
        if((ChannelOperationEnum.投诉率由低到高.getValue()).equals(sort)){
            jsmsPage.setOrderByClause("complaint_ratio asc,id");
            params.put("orderBy","complaint_ratio asc,id");
        }
        //按投诉差异值由高到低-->5
        if((ChannelOperationEnum.投诉差异值由高到低.getValue()).equals(sort)){
            jsmsPage.setOrderByClause("complaint_difference desc,id");
            params.put("orderBy","complaint_difference desc,id");
        }
        //按投诉差异值由低到高-->6
        if((ChannelOperationEnum.投诉差异值由低到高.getValue()).equals(sort)){
            jsmsPage.setOrderByClause("complaint_difference asc,id");
            params.put("orderBy","complaint_difference asc,id");
        }
    }
}
