package com.ucpaas.sms.service.statistic.impl;

import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.dto.ChannelConsumeVO;
import com.ucpaas.sms.dto.ClientConsumeVO;
import com.ucpaas.sms.entity.access.AccessSendStat;
import com.ucpaas.sms.entity.access.BackPaymentStat;
import com.ucpaas.sms.entity.message.User;
import com.ucpaas.sms.entity.po.DeptTree;
import com.ucpaas.sms.entity.record.RecordConsumeStat;
import com.ucpaas.sms.mapper.accessSlave.AccessSendStatMapper;
import com.ucpaas.sms.mapper.accessSlave.BackPaymentStatMapper;
import com.ucpaas.sms.mapper.message.AccountMapper;
import com.ucpaas.sms.mapper.message.DepartmentMapper;
import com.ucpaas.sms.mapper.message.UserMapper;
import com.ucpaas.sms.mapper.recordSlave.RecordConsumeStatMapper;
import com.ucpaas.sms.service.statistic.TotalConsumeService;
import com.ucpaas.sms.util.JacksonUtil;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by dylan on 2017/7/26.
 */
@Service
public class TotalConsumeServiceImpl implements TotalConsumeService {
    private Logger logger = LoggerFactory.getLogger(TotalConsumeServiceImpl.class);

    @Autowired
    private AccessSendStatMapper accessSendStatMapper;
    @Autowired
    private RecordConsumeStatMapper recordConsumeStatMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AccountMapper accountMapper;
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private BackPaymentStatMapper backPaymentStatMapper;

    /**
     * 月合计
     */
    @Override
    public List queryMonthSumList(Map params){
        List<ClientConsumeVO> accessSendStats = accessSendStatMapper.queryMonthSumList(params);

        List<ClientConsumeVO> resultList = new ArrayList<>();

//        for (AccessSendStat accessSendStat:accessSendStats){
//            ClientConsumeVO clientConsumeVO = new ClientConsumeVO();
//            BeanUtil.copyProperties(accessSendStat,clientConsumeVO);
//            resultList.add(clientConsumeVO);
//        }
        String start = (String) params.get("start");
        String end = (String) params.get("end");
        DateTime startDay = DateTime.parse(start);
//        while (!end.equals(startDay.plusMonths(count).toString("yyyy-MM"))) {
        while (DateTime.parse(end).plusMonths(1).isAfter(startDay)) {

            ClientConsumeVO init = ClientConsumeVO.init();
            init.setDate(Integer.parseInt(startDay.toString("yyyyMM")));
            for(ClientConsumeVO clientConsumeVO : accessSendStats){
                if(clientConsumeVO.getDate() < Integer.parseInt(startDay.plusMonths(1).toString("yyyyMMdd")) &&
                        clientConsumeVO.getDate() >= Integer.parseInt(startDay.toString("yyyyMMdd")) ){
                    init.setChargeTotal(init.getChargeTotal() + clientConsumeVO.getChargeTotal());
                    init.setSalefee(init.getSalefee().add(clientConsumeVO.getSalefee()));
                }
            }
            resultList.add(init);
            startDay = startDay.plusMonths(1);
        }
        logger.debug("近三月消耗对比 --> {}",JacksonUtil.toJSON(resultList));

        return resultList;
    }


    /**
     * 合计
     */
    @Override
    public List querySumList(Map params){
        List<ClientConsumeVO> consumeVOList = accessSendStatMapper.queryTotalList(params);

        if(params.get("startTime") != null && params.get("endTime") != null ){
            supplement(consumeVOList,(Integer) params.get("startTime"),(Integer)params.get("endTime"),(Integer)params.get("stattype"));
        }
        List<ClientConsumeVO> resultList = new ArrayList<>();

        /*for (ClientConsumeVO temp:consumeVOList){
            ClientConsumeVO clientConsumeVO = new ClientConsumeVO();
            BeanUtil.copyProperties(temp,clientConsumeVO);
            resultList.add(clientConsumeVO);
        }*/

        return consumeVOList;
    }

    /**
     * 各BU消耗占比
     */
    @Override
    public List consumeVsByBU(Map params){
        List<ClientConsumeVO> clientConsumeVOS = accessSendStatMapper.queryMonthSumList(params);

//        List<AccessSendStat> accessSendStats = accessSendStatMapper.queryTotalList(params);
        logger.debug("各BU消耗占比 --> {}",JacksonUtil.toJSON(clientConsumeVOS));

        List<DeptTree> allDept = departmentMapper.findAllDept();

//        List<ClientConsumeVO> resultList = new ArrayList<>();
        for (ClientConsumeVO clientConsumeVO:clientConsumeVOS){
//            ClientConsumeVO clientConsumeVO = new ClientConsumeVO();
//            BeanUtil.copyProperties(accessSendStat,clientConsumeVO);
            if(clientConsumeVO.getDepartmentId() == null ){
                continue;
            }
            for (DeptTree deptTree : allDept){
                if( clientConsumeVO.getDepartmentId().equals(deptTree.getDeptId())){
                    clientConsumeVO.setDepartmentStr(deptTree.getDeptName());
                    continue;
                }
            }

//            resultList.add(clientConsumeVO);
        }
        logger.debug("各BU消耗占比 --> {}",JacksonUtil.toJSON(clientConsumeVOS));

        return clientConsumeVOS;
    }


    /**
     * 回款金额占比
     */
    @Override
    public ResultVO consumeVsByPay(Map params){
        BackPaymentStat backPaymentStat = backPaymentStatMapper.querySum(params);
        if(backPaymentStat == null){
            backPaymentStat = new BackPaymentStat();
        }
        logger.debug("回款金额占比 BackPaymentStat--> {}",JacksonUtil.toJSON(backPaymentStat));
        return ResultVO.successDefault(backPaymentStat);
    }
    /**
     * TOP10客户发送量
     */
    @Override
    public ResultVO topClients(Map params) {
        ResultVO resultVO = ResultVO.successDefault();
        List<ClientConsumeVO> resultList = accessSendStatMapper.queryTop(params);
        logger.debug("TOP10客户发送量 --> {}",JacksonUtil.toJSON(resultList));
        if(resultList.size() < 1){
            return resultVO;
        }
        Set<String> clientIds = new HashSet<>();
        Set<Long> belongIds = new HashSet<>();

        for (AccessSendStat accessSendStat:resultList){
            clientIds.add(accessSendStat.getClientid());
            if(accessSendStat.getBelongSale() != null){
                belongIds.add(accessSendStat.getBelongSale());
            }
        }
        List<User> users = new ArrayList<>();
        if (belongIds.size() > 0){
            users = userMapper.queryBelongInfo(belongIds);
        }
        List<ClientConsumeVO> clientInfoList = accountMapper.queryStatisticClientInfo(clientIds);
        List<DeptTree> allDept = departmentMapper.findAllDept();
        for (ClientConsumeVO clientConsume : resultList){
            for (ClientConsumeVO info : clientInfoList){
                if(clientConsume.getClientid() == null){
                    continue;
                }else if(clientConsume.getClientid().equals(info.getClientid())){
                    clientConsume.setClientCreateTime(info.getClientCreateTime());
                    clientConsume.setAgentName(info.getAgentName());
                    clientConsume.setAccountGname(StringUtils.isEmpty(info.getAccountGname())? info.getClientName():info.getAccountGname());
                    clientConsume.setAgentType(info.getAgentType());
                    continue;
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
            if(clientConsume.getDepartmentId() == null){
                continue;
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

        resultVO.setData(resultList);
        return resultVO;
    }
    /**
     * TOP10通道消耗量
     */
    @Override
    public ResultVO topChannels(Map params){
        ResultVO resultVO = ResultVO.successDefault();
        List<ChannelConsumeVO> resultList = recordConsumeStatMapper.queryTop(params);
        if(resultList.size() < 1){
            return ResultVO.successDefault();
        }

        Set<Long> userIds = new HashSet<>();
        for (RecordConsumeStat recordConsumeStat:resultList){
            if(recordConsumeStat.getBelongBusiness() == null){
                continue;
            }
            userIds.add(recordConsumeStat.getBelongBusiness());
        }

        List<User> users = userMapper.queryBelongInfo(userIds);
        List<DeptTree> allDept = departmentMapper.findAllDept();

        for(ChannelConsumeVO channelConsumeVO: resultList){
            for (User user: users){
                if(channelConsumeVO.getBelongBusiness() == null){
                    continue;
                }else if(channelConsumeVO.getBelongBusiness().equals(user.getId())){
                    channelConsumeVO.setBelongBusinessStr(user.getRealname());
                    continue;
                }
            }
            if(channelConsumeVO.getDepartmentId() == null){
                continue;
            }
            for (DeptTree deptTree: allDept){
                if(deptTree == null){
                    continue;
                }else if(deptTree.getDeptId().equals(channelConsumeVO.getDepartmentId())){
                    channelConsumeVO.setDepartmentStr(deptTree.getDeptName());
                    continue;
                }
            }

        }
        resultVO.setData(resultList);
        return resultVO;
    }

    /**
     * @Description: 将时间段内缺少的数据以0补充
     * @author: Niu.T
     * @date: 2017年7月28日    下午3:08:59
     * @param dataList
     * @param startTime
     * @param endTime void
     */
    private void supplement(List<ClientConsumeVO> dataList, Integer startTime, Integer endTime, Integer stattype){
        String pattern1 = "";
        String pattern2 = "";
        String start = "";
        String end = "";
        /**
         * 统计类型, 0:每日,2:每月
         */
        if (stattype.equals(0)){
            pattern1 = "yyyyMMdd";
            pattern2 = "yyyy-MM-dd";
//            start = new StringBuilder(startTime.toString().substring(0,4))
//                    .append("-")
//                    .append(startTime.toString().substring(4,6))
//                    .append("-")
//                    .append(startTime.toString().substring(6))
//                    .toString();
            start = numberFormatDate(startTime.toString(),0);
//            end = new StringBuilder(endTime.toString().substring(0,4))
//                    .append("-")
//                    .append(endTime.toString().substring(4,6))
//                    .append("-")
//                    .append(endTime.toString().substring(6))
//                    .toString();
            end = numberFormatDate(endTime.toString(),0);
        }else if(stattype.equals(2)){
            pattern1 = "yyyyMM";
            pattern2 = "yyyy-MM";
//            start = new StringBuilder(startTime.toString().substring(0,4))
//                    .append("-")
//                    .append(startTime.toString().substring(4))
//                    .toString();
            start = numberFormatDate(startTime.toString(),2);
//            end = new StringBuilder(endTime.toString().substring(0,4))
//                    .append("-")
//                    .append(endTime.toString().substring(4))
//                    .toString();
            end = numberFormatDate(endTime.toString(),2);
        }else {
            logger.debug("不支持的补充数据的 统计类型 stattype = {}",stattype);
        }
        DateTime startDay = DateTime.parse(start);

        if(startDay.isAfterNow()||(start).equals(DateTime.now().toString(pattern2))){
            return;
        }
        if(DateTime.parse(end).isAfterNow()||(end).equals(DateTime.now().toString(pattern2))){
            end = DateTime.now().toString(pattern2);
        }
        int count = 0;
        while (!end.equals(
                stattype.equals(0) ? startDay.plusDays(count).toString(pattern2) : startDay.plusMonths(count).toString(pattern2))) {
            Integer date;
            if (stattype.equals(0)){
                date = Integer.parseInt(startDay.plusDays(count).toString(pattern1));
            }else{
                date = Integer.parseInt(startDay.plusMonths(count).toString(pattern1));
            }
            ClientConsumeVO dataTemp = ClientConsumeVO.init();
            dataTemp.setDate(date);
            if(count >= dataList.size()){
                dataList.add(count, dataTemp);
                ++count;
                continue;
            }
            ClientConsumeVO temp = dataList.get(count);
            if(!date.equals(temp.getDate())){
                dataList.add(count, dataTemp);
            }
            ++count;
        }
        Integer date;
        if (stattype.equals(0)){
            date = Integer.parseInt(startDay.plusDays(count).toString(pattern1));
        }else{
            date = Integer.parseInt(startDay.plusMonths(count).toString(pattern1));
        }
        ClientConsumeVO dataTemp = ClientConsumeVO.init();
        dataTemp.setDate(date);
        if(count >= dataList.size()){
            dataList.add(count, dataTemp);
        }
    }

    private String numberFormatDate(String date,Integer stattype){
        if (stattype.equals(0)) {
            return new StringBuilder(date.toString().substring(0, 4))
                    .append("-")
                    .append(date.toString().substring(4, 6))
                    .append("-")
                    .append(date.toString().substring(6))
                    .toString();
        }else if(stattype.equals(2)){
            return  new StringBuilder(date.toString().substring(0,4))
                    .append("-")
                    .append(date.toString().substring(4))
                    .toString();
        }
        return null;
    }


}
