package com.ucpaas.sms.controller;

import com.jsmsframework.user.service.JsmsDepartmentService;
import com.ucpaas.sms.dto.AccessStatisticVO;
import com.ucpaas.sms.entity.access.AccessSendStat;
import com.ucpaas.sms.entity.record.RecordConsumeStat;
import com.ucpaas.sms.model.Page;
import com.ucpaas.sms.service.account.AccountGroupService;
import com.ucpaas.sms.service.statistic.ChannelConsumeService;
import com.ucpaas.sms.service.statistic.ClientConsumeService;
import com.ucpaas.sms.service.statistic.TotalConsumeService;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by dylan on 2017/7/25.
 */

@Controller
@RequestMapping("/statistic")
public class StatisticTotalController extends BaseController{
    private static Logger logger = LoggerFactory.getLogger(StatisticTotalController.class);
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


    /**
     * 客户短信消耗报表 --> interface
     */
    @PostMapping("/clientConsumeTotal")
    @ResponseBody
    public Page getClientConsumeTotal(Page<AccessSendStat> page, @RequestParam Map params, HttpSession session){
        // 从Session中取出用户Id
        params.put("userId", getUserFromSession(session).getId().toString());
        convertDateParams(params);
        params.put("existAgent",true); // 是否是代理商子客户, 直客没有代理商
        params.put("isOrderByChargeTotal", true);   // 是否需要计费条数排序
        page.setParams(params);
        /*params.put("groupParams","clientid,paytype,smstype,agent_id,belong_sale"); // 统计类型, 0:每日,2:每月 todo 不需要日期分组
        page.setOrderByClause("chargeTotal DESC");*/
        page.setOrderByClause("id");
        page = clientConsumeService.queryPageTotal(page);
        return page;
    }


    /**
     * 单个客户短信消耗报表 --> interface
     */
    @PostMapping("/sendDetailTotal")
    @ResponseBody
    public Page getSendDetailTotal(@RequestParam Map params, @RequestParam(required=false) String isInter,HttpSession session){
        Page<AccessSendStat> page = new Page<>();
        // 从Session中取出用户Id
        params.put("userId", getUserFromSession(session).getId().toString());
//            resultList.add(clientConsumeVO);

        if(params.get("clientid") == null){
            return page;
        }
        convertDateParams(params);


        Map params2 = new HashMap(params);
        params2.put("operatorstypeExclude", Arrays.asList(4)); //operatorstypeExclude不包含， 通道运营商类型，0：全网，1：移动，2：联通，3：电信，4：国际，-1：拦截
        if (StringUtils.isNotBlank(isInter)) {
            if (isInter.equals("true")) {
                params2.put("operatorstypeExclude", Arrays.asList(0, 1, 2, 3, -1)); //operatorstypeExclude不包含， 通道运营商类型，0：全网，1：移动，2：联通，3：电信，4：国际，-1：拦截
            }
        }
        page.setParams(params2);
//        params.put("existAgent",true); // 是否是代理商子客户, 直客没有代理商
        /*params.put("groupParams","date,paytype,smstype"); // 统计类型, 0:每日,2:每月
        page.setOrderByClause("date DESC");*/
        page.setOrderByClause("id");
        page = clientConsumeService.queryPageTotal(page);
        return page;
    }

    /**
     * 客户通道消耗详情 --> interface
     */
    @PostMapping("/channelDetailTotal")
    @ResponseBody
    public Page<AccessStatisticVO> getChannelDetailTotal( @RequestParam Map params, @RequestParam(required=false) String isInter,HttpSession session){
        Page<AccessStatisticVO> page = new Page<>();
        if(params.get("clientid") == null){
            return page;
        }
        // 从Session中取出用户Id
        params.put("userId", getUserFromSession(session).getId().toString());
        convertDateParams(params);

        params.put("excludeIntercept", true);

        Map params2 = new HashMap(params);
        params2.put("operatorstypeExclude", Arrays.asList(4)); //operatorstypeExclude不包含， 通道运营商类型，0：全网，1：移动，2：联通，3：电信，4：国际，-1：拦截
        if (StringUtils.isNotBlank(isInter)) {
            if (isInter.equals("true")) {
                params2.put("operatorstypeExclude", Arrays.asList(0, 1, 2, 3, -1)); //operatorstypeExclude不包含， 通道运营商类型，0：全网，1：移动，2：联通，3：电信，4：国际，-1：拦截
            }
        }
        page.setParams(params2);
        /*params.put("groupParams","date,channelid,smstype,paytype"); // 统计类型, 0:每日,2:每月
        page.setOrderByClause("date DESC");*/
        page = channelConsumeService.queryPage4BasicStatisTotal(page);

        return page;
    }

    /**
     * 直客短信消耗报表 --> interface
     */
    @PostMapping("/directClientConsumeTotal")
    @ResponseBody
    public Page getDirectClientConsumeTotal(@RequestParam Map params,HttpSession session){
        Page<AccessSendStat> page = new Page<>();
        params.put("userId", getUserFromSession(session).getId().toString());

        convertDateParams(params);

        params.put("existAgent",false); // 是否是代理商子客户, 直客没有代理商
//        params.put("groupParams","date,clientid,belong_sale"); // 统计类型, 0:每日,2:每月
        params.put("isOrderByChargeTotal", true);   // 是否需要计费条数排序
        page.setParams(params);
        /*params.put("groupParams","clientid,smstype,belong_sale"); // 统计类型, 0:每日,2:每月 todo 不需要日期分组
        page.setOrderByClause("chargeTotal DESC");*/
        page.setOrderByClause("id");
        page = clientConsumeService.queryPageTotal(page);
        return page;
    }


    /**
     * 通道消耗报表 --> interface
     */
    @PostMapping("/channelConsumeTotal")
    @ResponseBody
    public Page getChannelConsumeTotal( @RequestParam Map params,HttpSession session){
        // 从Session中取出用户Id
        Page<RecordConsumeStat> page = new Page<>();
        params.put("userId", getUserFromSession(session).getId().toString());

        convertDateParams(params);

//        params.put("stattype",0); // 统计类型, 0:每日,2:每月
//        params.put("groupParams","date,channelid,belong_business"); // 统计类型, 0:每日,2:每月
        params.put("isOrderByChargeTotal", true);   // 是否需要计费条数排序
        page.setParams(params);
        params.put("groupParams","department_id,channelid,smstype,belong_business");  // 统计类型, 0:每日,2:每月 todo 不需要日期分组
        page.setOrderByClause("chargeTotal DESC");
        /*page.setOrderByClause("id");*/
        page = channelConsumeService.queryPageTotal(page);
        // 删除数据,只留总数
        page.setData(null);
        return page;
    }




    /**
     * 将请求参数中的时间参数转换成 Integer
     * @param params
     */
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
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }


}
