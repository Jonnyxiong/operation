package com.ucpaas.sms.service.statistic.impl;

import com.jsmsframework.access.access.entity.JsmsAccessSendStat;
import com.jsmsframework.access.access.entity.JsmsClientFailReturn;
import com.jsmsframework.access.service.JsmsAccessSendStatService;
import com.jsmsframework.access.service.JsmsClientFailReturnService;
import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.common.enums.ChargeRuleType;
import com.jsmsframework.common.enums.ClientFailReturnRefundStatus;
import com.jsmsframework.common.enums.OperatorType;
import com.jsmsframework.common.enums.SmsTypeEnum;
import com.ucpaas.sms.common.util.file.ExcelUtils;
import com.ucpaas.sms.dto.ClientConsumeVO;
import com.ucpaas.sms.dto.ClientConsumeVO2Point3;
import com.ucpaas.sms.entity.access.AccessSendStat;
import com.ucpaas.sms.entity.message.Department;
import com.ucpaas.sms.entity.message.User;
import com.ucpaas.sms.entity.po.DeptTree;
import com.ucpaas.sms.enums.AgentType;
import com.ucpaas.sms.enums.PayType;
import com.ucpaas.sms.mapper.accessSlave.AccessSendStatMapper;
import com.ucpaas.sms.mapper.message.AccountMapper;
import com.ucpaas.sms.mapper.message.DepartmentMapper;
import com.ucpaas.sms.mapper.message.UserMapper;
import com.ucpaas.sms.model.Excel;
import com.ucpaas.sms.model.Page;
import com.ucpaas.sms.service.statistic.ClientConsumeService;
import com.ucpaas.sms.util.AgentUtils;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.beans.BeanUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by dylan on 2017/7/26.
 */
@Service
public class ClientConsumeServiceImpl implements ClientConsumeService {
    private static Logger logger = LoggerFactory.getLogger(ClientConsumeServiceImpl.class);

    @Autowired
    private AccessSendStatMapper accessSendStatMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private JsmsAccessSendStatService jsmsAccessSendStatService;

    @Autowired
    private JsmsClientFailReturnService jsmsClientFailReturnService;


    @Override
    public Page queryPage(Page page) {
        setAuthority(page);
        List<AccessSendStat> accessSendStats = accessSendStatMapper.querySumList(page);
        if(accessSendStats.size() < 1){
            return page;
        }
        List<ClientConsumeVO> resultList = new ArrayList<>();
        Set<String> clientIds = new HashSet<>();
        Set<Long> belongIds = new HashSet<>();
        for (AccessSendStat accessSendStat:accessSendStats){
            if(!StringUtils.isEmpty(accessSendStat.getClientid())){
                clientIds.add(accessSendStat.getClientid());
            }
            if(accessSendStat.getBelongSale() != null){
                belongIds.add(accessSendStat.getBelongSale());
            }
            ClientConsumeVO clientConsumeVO = new ClientConsumeVO();
            BeanUtil.copyProperties(accessSendStat,clientConsumeVO);
//            clientConsumeVO.setAccountGroupType((AccountType) page.getParams().get("accountType"));
            resultList.add(clientConsumeVO);
        }
        List<ClientConsumeVO> clientInfoList = new ArrayList<>();
        if (clientIds.size() > 0){
            clientInfoList = accountMapper.queryStatisticClientInfo(clientIds);
        }

        List<User> users = new ArrayList<>();
        if (belongIds.size() > 0){
            users = userMapper.queryBelongInfo(belongIds);
        }

        List<DeptTree> allDept = departmentMapper.findAllDept();

        for (ClientConsumeVO clientConsume : resultList){

            for (ClientConsumeVO info : clientInfoList){
                if(clientConsume.getClientid() == null){
                    continue;
                }else if(clientConsume.getClientid().equals(info.getClientid())){
                    clientConsume.setClientCreateTime(info.getClientCreateTime());
                    clientConsume.setAccountGname(info.getAccountGname());

                    clientConsume.setAgentType(info.getAgentType());
                    clientConsume.setAgentName(info.getAgentName());
                }else if(StringUtils.isEmpty(clientConsume.getAgentName()) && clientConsume.getAgentId()!= null && clientConsume.getAgentId().equals(info.getAgentId())){
                    clientConsume.setAgentType(info.getAgentType());
                    clientConsume.setAgentName(info.getAgentName());
                }
            }

            for (User user: users){
                if(user == null){
                    continue;
                }else if(StringUtils.isEmpty(clientConsume.getBelongSaleStr()) && clientConsume.getBelongSale()!= null &&  user.getId().equals(clientConsume.getBelongSale())){
                    clientConsume.setBelongSaleStr(user.getRealname());
                    continue;
                }
            }

            for (DeptTree deptTree: allDept){
                if(deptTree == null){
                    continue;
                }else if(deptTree.getDeptId().equals(clientConsume.getDepartmentId())){
                    clientConsume.setDepartmentStr(deptTree.getDeptName());
                    continue;
                }
            }
        }

        page.setData(resultList);
        return page;
    }

    private void setAuthority(Page page){
        if (!StringUtils.isEmpty((String) page.getParams().get("condition"))){
            List<String> clientids = accountMapper.queryIdByName((String) page.getParams().get("condition"));
            if(clientids.size() > 0){
                page.getParams().put("clientids", clientids);
            }
            List<Long> belongIds = userMapper.queryBelongIdByName((String) page.getParams().get("condition"));
            if(belongIds.size() > 0){
                page.getParams().put("belongSales", belongIds);
            }
        }

        // 构造数据权限查询条件
        Long userId = page.getParams().get("userId") == null ? null :
                Long.parseLong(page.getParams().get("userId").toString());
        if (userId != null){
            page.getParams().put("dataAuthorityCondition", AgentUtils.getDataAuthorityCondition(userId, true, false));
        }
    }


    @Override
    public Page queryPageTotal(Page page) {
        page.setPageSize(1000);
        setAuthority(page);
        ClientConsumeVO clientConsumeVO = ClientConsumeVO.init();
        Map<String, ClientConsumeVO> totalLine = new HashMap<>();
        totalLine.put("totalLine", clientConsumeVO);
        page.setTotalOtherData(totalLine);
        if(page.getData() != null && !page.getData().isEmpty()){
            sum4Total(page);
            return page;
        }
        do {
            queryOnePage(page);
            page.setPageNo(page.getPageNo() + 1);
        }while (page.getPageNo() <= page.getTotalPage() );

        return page;
    }

    private void sum4Total(Page page){
        List<ClientConsumeVO> accessSendStats = page.getData();
        if(accessSendStats.isEmpty()){
            return;
        }
        ClientConsumeVO sum = (ClientConsumeVO) page.getTotalOtherData().get("totalLine");
        for (ClientConsumeVO temp:accessSendStats){
            sum.setSubmitTotal(sum.getSubmitTotal() + temp.getSubmitTotal());
            sum.setChargeTotal(sum.getChargeTotal() + temp.getChargeTotal());
            sum.setReportsuccess(sum.getReportsuccess() + temp.getReportsuccess());
            sum.setSubmitsuccess(sum.getSubmitsuccess() + temp.getSubmitsuccess());
            sum.setFailTotal(sum.getFailTotal() + temp.getFailTotal());
            sum.setNotsend(sum.getNotsend() + temp.getNotsend());
            sum.setInterceptTotal(sum.getInterceptTotal() + temp.getInterceptTotal());
            sum.setSalefee(sum.getSalefee().add(temp.getSalefee()));
            sum.setCostfee(sum.getCostfee().add(temp.getCostfee()));
        }
    }

    private void queryOnePage(Page page){
        List<AccessSendStat> accessSendStats = accessSendStatMapper.queryList(page);
        if(accessSendStats.size() < 1){
            return;
        }
        ClientConsumeVO sum = (ClientConsumeVO) page.getTotalOtherData().get("totalLine");
        for (AccessSendStat accessSendStat:accessSendStats){
            ClientConsumeVO temp = new ClientConsumeVO();
            BeanUtil.copyProperties(accessSendStat,temp);
            sum.setSubmitTotal(sum.getSubmitTotal() + temp.getSubmitTotal());
            sum.setChargeTotal(sum.getChargeTotal() + temp.getChargeTotal());
            sum.setReportsuccess(sum.getReportsuccess() + temp.getReportsuccess());
            sum.setSubmitsuccess(sum.getSubmitsuccess() + temp.getSubmitsuccess());
            sum.setFailTotal(sum.getFailTotal() + temp.getFailTotal());
            sum.setNotsend(sum.getNotsend() + temp.getNotsend());
            sum.setInterceptTotal(sum.getInterceptTotal() + temp.getInterceptTotal());
            sum.setSalefee(sum.getSalefee().add(temp.getSalefee()));
            sum.setCostfee(sum.getCostfee().add(temp.getCostfee()));

        }
    }

    @Override
    public ResultVO exportPage(Page page, Excel excel) {
        ResultVO resultVO = ResultVO.failure();
        resultVO.setMsg("生成报表失败");
        try {
            page.setPageSize(Integer.valueOf(ConfigUtils.max_export_excel_num) + 1);
            page = queryPage(page);
            List data = page.getData();
            if (data == null || data.size() <= 0){
                return ResultVO.failure("没有数据！先不导出了  ^_^");
            }else if (data.size() > Integer.valueOf(ConfigUtils.max_export_excel_num)){
                return ResultVO.failure("数据量超过"+ConfigUtils.max_export_excel_num+"，请缩小范围后再导出吧  ^_^");
            }

            List result = new ArrayList();
            for (Object obj:data){
                Map<String, String> describe = BeanUtils.describe(obj);
                result.add(describe);
            }
            page = queryPageTotal(page);

            result.add(BeanUtils.describe(page.getTotalOtherData().get("totalLine")));
            excel.setDataList(result);

            if (ExcelUtils.exportExcel(excel)) {
                resultVO.setSuccess(true);
                resultVO.setMsg("报表生成成功");
                resultVO.setData(excel.getFilePath());
                return resultVO;
            }
        } catch (Exception e) {
            logger.error("生成报表失败", e);
        }
        return resultVO;
    }

    /**
     * 查询所有的一级部门
     */
    @Override
    public List<Department> getAllDept() {
        List<Department> departments = departmentMapper.findLowerDepartmentListByDepartmentId(1);
        return departments;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public Page getClientConsume2point3(Page page) {
        setAuthority(page);

        /**
         * 1.根据客户类型、部门、短信类型、付费类型、时间段、operatorstypeStr（普通短信/国际短信）查询 客户发送量表 t_sms_access_send_stat
         *
         */
        JsmsPage jsmsPage = new JsmsPage();
        jsmsPage.setPage(page.getPageNo());
        jsmsPage.setRows(page.getPageSize());
        jsmsPage.setOrderByClause("chargeTotal DESC");
        jsmsPage.setParams(page.getParams());
        String groupByClause = "clientType,department_id,agent_id,account_gid,clientid,belong_sale,smstype,paytype,charge_rule,operatorstype";
        try {
            jsmsPage = jsmsAccessSendStatService.querySumList(jsmsPage,groupByClause);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e.getMessage());
        }
        List<JsmsAccessSendStat> jsmsAccessSendStats = jsmsPage.getData();
        page.setData(jsmsAccessSendStats);
        page.setTotalPage(jsmsPage.getTotalPage());
        page.setTotalRecord(jsmsPage.getTotalRecord());

        /**
         * 2. 遍历客户发送量jsmsAccessSendStats，根据分组[客户类型-部门-客户ID-客户名称-代理商ID-归属销售-计费规则-短信类型]，组装ClientConsumeVO2Point3
         */
        //2.1 加载基础数据到内存中
        Set<String> clientIds = new HashSet<>();
        Set<Long> belongIds = new HashSet<>();
        for(JsmsAccessSendStat jsmsAccessSendStat:jsmsAccessSendStats){
            if(!StringUtils.isEmpty(jsmsAccessSendStat.getClientid())){
                clientIds.add(jsmsAccessSendStat.getClientid());
            }
            if(jsmsAccessSendStat.getBelongSale() != null){
                belongIds.add(jsmsAccessSendStat.getBelongSale());
            }

        }
        List<ClientConsumeVO> clientInfoList = new ArrayList<>();
        if (clientIds.size() > 0){
            clientInfoList = accountMapper.queryStatisticClientInfo(clientIds);
        }

        List<User> users = new ArrayList<>();
        if (belongIds.size() > 0){
            users = userMapper.queryBelongInfo(belongIds);
        }
        List<DeptTree> allDept = departmentMapper.findAllDept();


        Map<String,ClientConsumeVO2Point3> temp = new LinkedHashMap<>();
        for(JsmsAccessSendStat jsmsAccessSendStat:jsmsAccessSendStats){
            String clientTypeStr = "";
            if(jsmsAccessSendStat.getAgentId()==null||jsmsAccessSendStat.getAgentId()==0||jsmsAccessSendStat.getAgentId()==1||jsmsAccessSendStat.getAgentId()==2) {
                clientTypeStr = "直客";
            }else{
                clientTypeStr = "代理商子客户";
            }
            //clientType,department_id,agent_id,account_gid,clientid,belong_sale,smstype,paytype,charge_rule
            String key=clientTypeStr+"-"+jsmsAccessSendStat.getDepartmentId()+"-"+jsmsAccessSendStat.getClientid()+//
                    "-"+jsmsAccessSendStat.getAccountGid()+"-"+jsmsAccessSendStat.getAgentId()+"-"+jsmsAccessSendStat.getBelongSale()+//
                    "-"+jsmsAccessSendStat.getChargeRule()+"-"+jsmsAccessSendStat.getSmstype()+"-"+jsmsAccessSendStat.getPaytype()+"-"+jsmsAccessSendStat.getOperatorstype();

            ClientConsumeVO2Point3 clientConsumeVO2Point3 = temp.get(key);
            if(clientConsumeVO2Point3==null){
                clientConsumeVO2Point3 = new ClientConsumeVO2Point3();
                clientConsumeVO2Point3.setClientTypeStr(clientTypeStr);
                //设置部门
                for (DeptTree deptTree: allDept){
                    if(deptTree!=null&&deptTree.getDeptId().equals(jsmsAccessSendStat.getDepartmentId())){
                        clientConsumeVO2Point3.setDepartmentStr(deptTree.getDeptName());
                        clientConsumeVO2Point3.setDepartmentId(deptTree.getDeptId());
                    }
                }
                clientConsumeVO2Point3.setClientid(jsmsAccessSendStat.getClientid());
                //设置客户信息

                if(jsmsAccessSendStat.getAgentId()==null||jsmsAccessSendStat.getAgentId()==0||jsmsAccessSendStat.getAgentId()==1||jsmsAccessSendStat.getAgentId()==2) {
                    clientConsumeVO2Point3.setAgentId(0);
                    clientConsumeVO2Point3.setAgentName("-");
                    clientConsumeVO2Point3.setAgentTypeStr("-");
                }else{
                    for (ClientConsumeVO info : clientInfoList){
                        if(jsmsAccessSendStat.getClientid().equals(info.getClientid())){
                            clientConsumeVO2Point3.setAccountGname(info.getAccountGname());
                            clientConsumeVO2Point3.setAgentId(jsmsAccessSendStat.getAgentId());
                            clientConsumeVO2Point3.setAgentName(info.getAgentName());
                            clientConsumeVO2Point3.setAgentType(info.getAgentType());
                            clientConsumeVO2Point3.setAgentTypeStr(AgentType.getDescByValue(info.getAgentType()));
                        }
                    }
                }
                for (ClientConsumeVO info : clientInfoList) {
                    if (jsmsAccessSendStat.getClientid().equals(info.getClientid())) {
                        clientConsumeVO2Point3.setAccountGname(info.getAccountGname());
                    }
                }



                //设置归属销售
                for (User user: users){
                    if(user!=null&&jsmsAccessSendStat.getBelongSale()!=null&&user.getId().equals(jsmsAccessSendStat.getBelongSale())){
                        clientConsumeVO2Point3.setBelongSale(jsmsAccessSendStat.getBelongSale());
                        clientConsumeVO2Point3.setBelongSaleStr(user.getRealname());
                    }
                }
                clientConsumeVO2Point3.setPaytype(jsmsAccessSendStat.getPaytype());
                clientConsumeVO2Point3.setPaytypeStr(PayType.getDescByValue(jsmsAccessSendStat.getPaytype()));
                clientConsumeVO2Point3.setChargeRule(jsmsAccessSendStat.getChargeRule());
                clientConsumeVO2Point3.setChargeRuleStr(ChargeRuleType.getDescByValue(jsmsAccessSendStat.getChargeRule()));
                clientConsumeVO2Point3.setSmstype(jsmsAccessSendStat.getSmstype());
                clientConsumeVO2Point3.setSmstypeStr(SmsTypeEnum.getDescByValue(jsmsAccessSendStat.getSmstype()));
                clientConsumeVO2Point3.setSalefee(BigDecimal.ZERO);
                clientConsumeVO2Point3.setCostfee(BigDecimal.ZERO);
                clientConsumeVO2Point3.setPendingReturnAmount(BigDecimal.ZERO);

//                if(jsmsAccessSendStat.getOperatorstype().intValue() == 4){
//                    clientConsumeVO2Point3.setOperatorstypeStr("国际短信");
//                }else{
//                    clientConsumeVO2Point3.setOperatorstypeStr("普通短信");
//                }

                Integer operatorstype = jsmsAccessSendStat.getOperatorstype();
                clientConsumeVO2Point3.setOperatorstype(operatorstype);
                String operatorstypeDesc = OperatorType.getDescByValue(operatorstype);
                clientConsumeVO2Point3.setOperatorstypeStr(operatorstypeDesc == null ? "-" : operatorstypeDesc);

            }

            //(0+1+3+4+5+6+7+8+9+10)
            long status0 = clientConsumeVO2Point3.getNotsend() + jsmsAccessSendStat.getNotsend();
            long status1 = clientConsumeVO2Point3.getSubmitsuccess() + jsmsAccessSendStat.getSubmitsuccess();
            long status3 = clientConsumeVO2Point3.getReportsuccess() + jsmsAccessSendStat.getReportsuccess();
            long status4 = clientConsumeVO2Point3.getSubmitfail() + jsmsAccessSendStat.getSubmitfail();
            long status5 = clientConsumeVO2Point3.getSubretfail() +jsmsAccessSendStat.getSubretfail();
            long status6 = clientConsumeVO2Point3.getReportfail() + jsmsAccessSendStat.getReportfail();
            long status7 = clientConsumeVO2Point3.getAuditfail() + jsmsAccessSendStat.getAuditfail();
            long status8 = clientConsumeVO2Point3.getRecvintercept() + jsmsAccessSendStat.getRecvintercept();
            long status9 = clientConsumeVO2Point3.getSendintercept() + jsmsAccessSendStat.getSendintercept();
            long status10 = clientConsumeVO2Point3.getOverrateintercept() + jsmsAccessSendStat.getOverrateintercept() ;
            clientConsumeVO2Point3.setNotsend(status0);
            clientConsumeVO2Point3.setSubmitsuccess(status1);
            clientConsumeVO2Point3.setReportsuccess(status3);
            clientConsumeVO2Point3.setSubmitfail(new Long(status4).intValue());
            clientConsumeVO2Point3.setSubretfail(new Long(status5).intValue());
            clientConsumeVO2Point3.setReportfail(new Long(status6).intValue());
            clientConsumeVO2Point3.setAuditfail(new Long(status7).intValue());
            clientConsumeVO2Point3.setRecvintercept(new Long(status8).intValue());
            clientConsumeVO2Point3.setSendintercept(new Long(status9).intValue());
            clientConsumeVO2Point3.setOverrateintercept(new Long(status10).intValue());




            long submitTotal = status0+status1+status3//
                    +status4+status5+status6+status7+status8//
                    +status9+status10;
            clientConsumeVO2Point3.setSubmitTotal(submitTotal);

            long chargeTotal = clientConsumeVO2Point3.getChargeTotal();
            //计费条数计算
            if(clientConsumeVO2Point3.getChargeRule().equals(ChargeRuleType.成功量.getValue())){
                chargeTotal = status1 + status3;
                clientConsumeVO2Point3.setChargeRuleStr(ChargeRuleType.成功量.getDesc());

            }else if (clientConsumeVO2Point3.getChargeRule().equals(ChargeRuleType.提交量.getValue())){
                chargeTotal = status1+status3+status4+status6;
                clientConsumeVO2Point3.setChargeRuleStr(ChargeRuleType.提交量.getDesc());

            }else if (clientConsumeVO2Point3.getChargeRule().equals(ChargeRuleType.明确成功量.getValue())){
                chargeTotal = status3;
                clientConsumeVO2Point3.setChargeRuleStr(ChargeRuleType.明确成功量.getDesc());

            }else{ //历史数据没有计费规则则按直客的“计费规则”是成功量，代理商子客户的“计费规则”是提交量。
                if(clientConsumeVO2Point3.getAgentId()==null||clientConsumeVO2Point3.getAgentId()==0||clientConsumeVO2Point3.getAgentId()==1||clientConsumeVO2Point3.getAgentId()==2) {  //直客
                    chargeTotal = status1 + status3;
                    clientConsumeVO2Point3.setChargeRuleStr(ChargeRuleType.成功量.getDesc());
                }else{ //代理商子客户
                    chargeTotal = status1+status3+status4+status6;
                    clientConsumeVO2Point3.setChargeRuleStr(ChargeRuleType.提交量.getDesc());
                }
            }
            clientConsumeVO2Point3.setChargeTotal(chargeTotal);

            //成功率
            BigDecimal successRete = new BigDecimal(status3).divide(new BigDecimal(submitTotal),4,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
            clientConsumeVO2Point3.setSuccessRate(successRete.setScale(2,BigDecimal.ROUND_HALF_UP).toString()+"%");

            //成功条数（条） 明确成功数量(状态:3)
            clientConsumeVO2Point3.setReportsuccess(status3);

            //未知条数（条） 提交成功数量(状态:1)
            clientConsumeVO2Point3.setSubmitsuccess(status1);

            //失败条数（条）  (4+6)
            long failTotal = status4+status6;
            clientConsumeVO2Point3.setFailTotal(failTotal);

            //待发送条数（条） 未发送数量(状态:0)
            clientConsumeVO2Point3.setNotsend(status0);

            //拦截条数（条） (5+7+8+9+10)
            long interceptTotal = status5+status7+status8+status9+status10;
            clientConsumeVO2Point3.setInterceptTotal(interceptTotal);


            //消费金额（元） 客户总消耗
            BigDecimal salefee = clientConsumeVO2Point3.getSalefee().add(jsmsAccessSendStat.getSalefee().divide(new BigDecimal("1000")));
            clientConsumeVO2Point3.setSalefee(salefee);


            //通道成本（元） 通道总成本
            BigDecimal costfee = clientConsumeVO2Point3.getCostfee().add(jsmsAccessSendStat.getCostfee().divide(new BigDecimal("1000")));
            clientConsumeVO2Point3.setCostfee(costfee);

            //毛利
            BigDecimal profit = salefee.subtract(costfee);
            clientConsumeVO2Point3.setProfit(profit.setScale(2, BigDecimal.ROUND_HALF_UP).toString());

            //毛利率
            if(salefee.compareTo(BigDecimal.ZERO)!=0) {
                BigDecimal profitRate = profit.divide(salefee, 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
                clientConsumeVO2Point3.setProfitRate(profitRate.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%");
            }else{
                clientConsumeVO2Point3.setProfitRate("-");
            }

            if(clientConsumeVO2Point3.getPaytype().equals(PayType.PREPAY.getValue())) { //后付费不统计
                //未返还条数
                int pendingReturnNumber = clientConsumeVO2Point3.getPendingReturnNumber() + jsmsAccessSendStat.getReturnTotalNumber();
                clientConsumeVO2Point3.setPendingReturnNumber(pendingReturnNumber);

                //未返还计费
                BigDecimal pendingReturnAmount = clientConsumeVO2Point3.getPendingReturnAmount().add(jsmsAccessSendStat.getReturnTotalAmount().divide(new BigDecimal(1000)));
                clientConsumeVO2Point3.setPendingReturnAmount(pendingReturnAmount);
            }
            temp.put(key,clientConsumeVO2Point3);

        }

        List<ClientConsumeVO2Point3> clientConsumeVO2Point3s = new ArrayList<>(temp.values());

        //2.2 根据时间段获取 客户失败返回清单表 t_sms_client_fail_return
        List<JsmsClientFailReturn> clientFailReturns = jsmsClientFailReturnService.queryAll(page.getParams());
        for(ClientConsumeVO2Point3 clientConsumeVO2Point3:clientConsumeVO2Point3s){
            if (null == clientConsumeVO2Point3.getDepartmentId())
            {
                clientConsumeVO2Point3.setDepartmentId(-1);
            }

            if (null == clientConsumeVO2Point3.getAgentId())
            {
                clientConsumeVO2Point3.setAgentId(-1);
            }

            if (null == clientConsumeVO2Point3.getBelongSale())
            {
                clientConsumeVO2Point3.setBelongSale(-1L);
            }

            if (null == clientConsumeVO2Point3.getOperatorstype())
            {
                clientConsumeVO2Point3.setOperatorstype(-1);
            }

            for(JsmsClientFailReturn clientFailReturn:clientFailReturns){
                //后付费不统计
                if(!PayType.PREPAY.getValue().equals(clientConsumeVO2Point3.getPaytype())) {
                    continue;
                }

                if (null == clientFailReturn.getDepartmentId())
                {
                    clientFailReturn.setDepartmentId(-1);
                }

                if (null == clientFailReturn.getAgentId())
                {
                    clientFailReturn.setAgentId(-1);
                }

                if (null == clientFailReturn.getBelongSale())
                {
                    clientFailReturn.setBelongSale(-1L);
                }

                if (null == clientFailReturn.getOperatorstype())
                {
                    clientFailReturn.setOperatorstype(-1);
                }

                if (clientConsumeVO2Point3.getClientid().equals(clientFailReturn.getClientid())//客户id相同
                        && clientConsumeVO2Point3.getSmstype().equals(clientFailReturn.getSmstype())//短信类型相同
                        && ClientFailReturnRefundStatus.已退费.getValue().equals(clientFailReturn.getRefundState()) //已退费
                        && clientConsumeVO2Point3.getChargeRule().equals(clientFailReturn.getChargeRule())
                        && clientConsumeVO2Point3.getBelongSale().equals(clientFailReturn.getBelongSale())
                        && clientConsumeVO2Point3.getAgentId().equals(clientFailReturn.getAgentId())
                        && clientConsumeVO2Point3.getDepartmentId().equals(clientFailReturn.getDepartmentId())
                        && clientConsumeVO2Point3.getOperatorstype().equals(clientFailReturn.getOperatorstype())) {

                    // 减去已还条数
                    int pendingReturnNumber = clientConsumeVO2Point3.getPendingReturnNumber() - clientFailReturn.getReturnNumber();
                    clientConsumeVO2Point3.setPendingReturnNumber(pendingReturnNumber);

                    // 减去已返还计费
                    BigDecimal pendingReturnAmount = clientConsumeVO2Point3.getPendingReturnAmount().subtract(clientFailReturn.getUnitPrice().multiply(new BigDecimal(clientFailReturn.getReturnNumber())));
                    clientConsumeVO2Point3.setPendingReturnAmount(pendingReturnAmount);
                }
            }

        }

        page.setData(clientConsumeVO2Point3s);
        return page;
    }


    @Override
    public ClientConsumeVO2Point3 getClientConsume2point3Total(Page page) {
        ClientConsumeVO2Point3 total = new ClientConsumeVO2Point3();
        total.setSalefee(BigDecimal.ZERO); //消费金额
        total.setCostfee(BigDecimal.ZERO); //通道成本
        total.setPendingReturnAmount(BigDecimal.ZERO);//未返还金额

        page.setPageSize(Integer.MAX_VALUE); //获取全部
        page = this.getClientConsume2point3(page);

        List<ClientConsumeVO2Point3> clientConsumeVO2Point3s = page.getData();

        for(ClientConsumeVO2Point3 clientConsumeVO2Point3:clientConsumeVO2Point3s){


            total.setSubmitTotal(total.getSubmitTotal()+clientConsumeVO2Point3.getSubmitTotal()); //提交条数
            total.setChargeTotal(total.getChargeTotal()+clientConsumeVO2Point3.getChargeTotal()); //计费条数

            //成功条数（条） 明确成功数量(状态:3)
            total.setReportsuccess(total.getReportsuccess()+clientConsumeVO2Point3.getReportsuccess()); //成功条数
            //成功率
            BigDecimal successRete = new BigDecimal(total.getReportsuccess()).divide(new BigDecimal(total.getSubmitTotal()),4,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
            total.setSuccessRate(successRete.setScale(2,BigDecimal.ROUND_HALF_UP).toString()+"%"); //成功率


            total.setSubmitsuccess(total.getSubmitsuccess()+clientConsumeVO2Point3.getSubmitsuccess()); //未知条数
            total.setFailTotal(total.getFailTotal()+clientConsumeVO2Point3.getFailTotal()); //失败条数
            total.setNotsend(total.getNotsend()+clientConsumeVO2Point3.getNotsend()); //待发送条数
            total.setInterceptTotal(total.getInterceptTotal()+clientConsumeVO2Point3.getInterceptTotal()); //拦截条数
            total.setSalefee(total.getSalefee().add(clientConsumeVO2Point3.getSalefee())); //消费金额
            total.setCostfee(total.getCostfee().add(clientConsumeVO2Point3.getCostfee())); //通道成本



            //毛利
            BigDecimal profit = total.getSalefee().subtract(total.getCostfee());
            total.setProfit(profit.setScale(2, BigDecimal.ROUND_HALF_UP).toString());//毛利

            //毛利率
            if(total.getSalefee().compareTo(BigDecimal.ZERO)!=0) {
                BigDecimal profitRate = profit.divide(total.getSalefee(), 4, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
                total.setProfitRate(profitRate.setScale(2, BigDecimal.ROUND_HALF_UP).toString() + "%");
            }else{
                total.setProfitRate("-");
            }

            total.setPendingReturnNumber(total.getPendingReturnNumber()+clientConsumeVO2Point3.getPendingReturnNumber()); //未返还条数
            //未返还计费
            BigDecimal pendingReturnAmount = total.getPendingReturnAmount().add(clientConsumeVO2Point3.getPendingReturnAmount());
            total.setPendingReturnAmount(pendingReturnAmount);

        }
        return total;
    }
}
