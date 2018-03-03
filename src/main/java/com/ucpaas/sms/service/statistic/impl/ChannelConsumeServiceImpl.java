package com.ucpaas.sms.service.statistic.impl;

import com.jsmsframework.channel.entity.JsmsChannel;
import com.jsmsframework.channel.service.JsmsChannelService;
import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.common.util.file.ExcelUtils;
import com.ucpaas.sms.dto.AccessStatisticVO;
import com.ucpaas.sms.dto.ChannelConsumeVO;
import com.ucpaas.sms.entity.access.AccessChannelStatistics;
import com.ucpaas.sms.entity.message.Channel;
import com.ucpaas.sms.entity.message.User;
import com.ucpaas.sms.entity.po.DeptTree;
import com.ucpaas.sms.entity.record.RecordChannelStatistics;
import com.ucpaas.sms.entity.record.RecordConsumeStat;
import com.ucpaas.sms.mapper.accessSlave.AccessChannelStatisticsMapper;
import com.ucpaas.sms.mapper.message.ChannelMapper;
import com.ucpaas.sms.mapper.message.DepartmentMapper;
import com.ucpaas.sms.mapper.message.UserMapper;
import com.ucpaas.sms.mapper.recordSlave.RecordChannelStatisticsMapper;
import com.ucpaas.sms.mapper.recordSlave.RecordConsumeStatMapper;
import com.ucpaas.sms.model.Excel;
import com.ucpaas.sms.model.Page;
import com.ucpaas.sms.service.statistic.ChannelConsumeService;
import com.ucpaas.sms.util.AgentUtils;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.beans.BeanUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * Created by dylan on 2017/7/26.
 */
@Service
public class ChannelConsumeServiceImpl implements ChannelConsumeService {
    private static Logger logger = LoggerFactory.getLogger(ChannelConsumeServiceImpl.class);

    @Autowired
    private RecordConsumeStatMapper recordConsumeStatMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AccessChannelStatisticsMapper accessChannelStatisticsMapper;
    @Autowired
    private RecordChannelStatisticsMapper recordChannelStatisticsMapper;
    @Autowired
    private DepartmentMapper departmentMapper;
    @Autowired
    private ChannelMapper channelMapper;
    @Autowired
    private JsmsChannelService jsmsChannelService;



    @Override
    public Page queryPage(Page page) {

        setAuthority4Consume(page);

        List<RecordConsumeStat> recordConsumeStats = recordConsumeStatMapper.querySumList(page);
        if(recordConsumeStats.size() < 1){
            return page;
        }

        List<ChannelConsumeVO> resultList = new ArrayList<>();
        Set<Long> userIds = new HashSet<>();
        // 存储通道id,用于获取单价
        Set<Integer> cids = new HashSet<>(recordConsumeStats.size());
        for (RecordConsumeStat recordConsumeStat:recordConsumeStats){
            if(recordConsumeStat.getBelongBusiness() != null){
                userIds.add(recordConsumeStat.getBelongBusiness());
            }
            recordConsumeStat.setCostprice(recordConsumeStat.getCostprice().divide(new BigDecimal("1000")));
            ChannelConsumeVO channelConsumeVO = new ChannelConsumeVO();
            BeanUtil.copyProperties(recordConsumeStat,channelConsumeVO);
            resultList.add(channelConsumeVO);
            cids.add(channelConsumeVO.getChannelid());
        }

        List<JsmsChannel> jsmsChannelList = jsmsChannelService.getByCids(cids);
        // 用于存储单价,key:通道id, value:通道单价
        Map<Integer, BigDecimal> channelMap = new HashMap<>(jsmsChannelList.size());
        for (JsmsChannel jsmsChannel : jsmsChannelList) {
            channelMap.put(jsmsChannel.getCid(), jsmsChannel.getCostprice());
        }

        /**
         * 获取归属商务
         */
        List<User> users = new ArrayList<>();
        if(userIds.size() > 0){
            users = userMapper.queryBelongInfo(userIds);
        }
        /**
         * 获取所属部门
         */
        List<DeptTree> allDept = departmentMapper.findAllDept();

        for(ChannelConsumeVO channelConsumeVO: resultList){
            for (User user: users){
                if(channelConsumeVO.getBelongBusiness() == null){
                    continue;
                }else if(channelConsumeVO.getBelongBusiness().equals(user.getId())){
                    channelConsumeVO.setBelongBusinessStr(user.getRealname());
                    break;
                }
            }
            for (DeptTree deptTree: allDept){
                if(deptTree == null){
                    continue;
                }else if(deptTree.getDeptId().equals(channelConsumeVO.getDepartmentId())){
                    channelConsumeVO.setDepartmentStr(deptTree.getDeptName());
                    break;
                }
            }
            // 补全通道单价
            BigDecimal costprice = channelMap.get(channelConsumeVO.getChannelid());
            channelConsumeVO.setCostprice(costprice == null ? BigDecimal.ZERO : costprice);
            channelConsumeVO.setCostpriceStr(channelConsumeVO.getCostprice().setScale(4,BigDecimal.ROUND_DOWN).toString());
        }
        page.setData(resultList);
        return page;
    }

    private void setAuthority4Consume(Page page){
        // 构造数据权限查询条件
        Long userId = page.getParams().get("userId") == null ? null :
                Long.parseLong(page.getParams().get("userId").toString());
        if (userId != null){
            page.getParams().put("dataAuthorityCondition", AgentUtils.getDataAuthorityCondition(userId, false, true));
        }
    }

    @Override
    public Page queryPageTotal(Page page) {
        page.setPageSize(-1);
        if(page.getData() == null) {
            queryPage(page);
        }
        ChannelConsumeVO channelConsumeVO = ChannelConsumeVO.init();
        Map<String, ChannelConsumeVO> totalLine = new HashMap<>();
        totalLine.put("totalLine", channelConsumeVO);
        page.setTotalOtherData(totalLine);
        // 去除原有的数据
        channelConsumeVO.setCostpriceTotal(BigDecimal.ZERO);
        sum4Total(page);
        BigDecimal costpriceSum = channelConsumeVO.getCostpriceTotal().setScale(4, BigDecimal.ROUND_DOWN);
        channelConsumeVO.setCostpriceStr(costpriceSum.toString());
        channelConsumeVO.setCostprice(costpriceSum);
        // 成功率和毛利率导出为"-"
        channelConsumeVO.setSuccessRate("-");
        channelConsumeVO.setProfitRate("-");
//        setAuthority4Consume(page);
//
//        ChannelConsumeVO channelConsumeVO = ChannelConsumeVO.init();
//        Map<String, ChannelConsumeVO> totalLine = new HashMap<>();
//        totalLine.put("totalLine", channelConsumeVO);
//        page.setTotalOtherData(totalLine);
//        if(page.getData() != null && !page.getData().isEmpty()){
//            sum4Total(page);
//            return page;
//        }
//        do {
//            queryOnePage(page);
//            page.setPageNo(page.getPageNo() + 1);
//        } while (page.getPageNo() <= page.getTotalPage());

        return page;
    }

    public void sum4Total(Page page) {
        ChannelConsumeVO sum = (ChannelConsumeVO) page.getTotalOtherData().get("totalLine");

        List<ChannelConsumeVO> recordConsumeStats = recordConsumeStats = page.getData();
        if(null == recordConsumeStats || recordConsumeStats.isEmpty()){
            return;
        }
        for (ChannelConsumeVO temp:recordConsumeStats){
            sum.setCostpriceTotal(sum.getCostpriceTotal() == null ? temp.getCostprice() : sum.getCostpriceTotal().add(temp.getCostprice()));
            sum.setSendTotal(sum.getSendTotal() + temp.getSendTotal());
            sum.setChargeTotal(sum.getChargeTotal() + temp.getChargeTotal());
            sum.setSuccessTotal(sum.getSuccessTotal() + temp.getSuccessTotal());
            sum.setSubmitsuccess((sum.getSubmitsuccess() == null ? 0 : sum.getSubmitsuccess()) + temp.getSubmitsuccess());
            sum.setSubretsuccess((sum.getSubretsuccess() == null ? 0 : sum.getSubretsuccess()) + temp.getSubretsuccess());
            sum.setReportsuccess((sum.getReportsuccess() == null ? 0 : sum.getReportsuccess()) + temp.getReportsuccess());
            sum.setFailTotal(sum.getFailTotal() + temp.getFailTotal());
            sum.setSaletotal((sum.getSaletotal() == null ? BigDecimal.ZERO :sum.getSaletotal()).add(temp.getSaletotal()));
            sum.setCosttotal((sum.getCosttotal() == null ? BigDecimal.ZERO :sum.getCosttotal()).add(temp.getCosttotal()));
        }
    }

    public void queryOnePage(Page page) {
        ChannelConsumeVO sum = (ChannelConsumeVO) page.getTotalOtherData().get("totalLine");
        List<RecordConsumeStat> recordConsumeStats = null;
        recordConsumeStats = recordConsumeStatMapper.queryList(page);
        if(recordConsumeStats.isEmpty()){
            return ;
        }
        // 存储通道id,用于获取单价
        Set<Integer> cids = new HashSet<>(recordConsumeStats.size());
        for (RecordConsumeStat recordConsumeStat:recordConsumeStats){
            cids.add(recordConsumeStat.getChannelid());
        }
        List<JsmsChannel> jsmsChannelList = jsmsChannelService.getByCids(cids);
        // 用于存储单价,key:通道id, value:通道单价
        Map<Integer, BigDecimal> channelMap = new HashMap<>(jsmsChannelList.size());
        for (JsmsChannel jsmsChannel : jsmsChannelList) {
            channelMap.put(jsmsChannel.getCid(), jsmsChannel.getCostprice());
        }

        for (RecordConsumeStat recordConsumeStat:recordConsumeStats){
            ChannelConsumeVO temp = new ChannelConsumeVO();
            BeanUtil.copyProperties(recordConsumeStat,temp);
            // 计算通道单价总和
            sum.setCostpriceTotal(sum.getCostpriceTotal() == null ? BigDecimal.ZERO : sum.getCostpriceTotal().add(channelMap.get(temp.getChannelid())));
            sum.setSendTotal(sum.getSendTotal() + temp.getSendTotal());
            sum.setChargeTotal(sum.getChargeTotal() + temp.getChargeTotal());
            sum.setSuccessTotal(sum.getSuccessTotal() + temp.getSuccessTotal());
            sum.setSubmitsuccess((sum.getSubmitsuccess() == null ? 0 : sum.getSubmitsuccess()) + temp.getSubmitsuccess());
            sum.setSubretsuccess((sum.getSubretsuccess() == null ? 0 : sum.getSubretsuccess()) + temp.getSubretsuccess());
            sum.setReportsuccess((sum.getReportsuccess() == null ? 0 : sum.getReportsuccess()) + temp.getReportsuccess());
            sum.setFailTotal(sum.getFailTotal() + temp.getFailTotal());
            sum.setSaletotal((sum.getSaletotal() == null ? BigDecimal.ZERO :sum.getSaletotal()).add(temp.getSaletotal()));
            sum.setCosttotal((sum.getCosttotal() == null ? BigDecimal.ZERO :sum.getCosttotal()).add(temp.getCosttotal()));
        }

    }

    @Override
    public ResultVO exportPage4BasicStatis(Page page, Excel excel) {

        return commonExport(page,excel,"exportPage4BasicStatis");

    }
    @Override
    public ResultVO exportPage(Page page,Excel excel) {
        return commonExport(page,excel,"exportPage");
    }

    @Override
    public Page<AccessStatisticVO> queryPage4BasicStatis(Page page){

        setAuthority4BasicStatis(page);
        List<AccessChannelStatistics> accessChannelStatisticss = accessChannelStatisticsMapper.querySumByClientids(page);
        if (accessChannelStatisticss.size() < 1){
            return page;
        }
        List<RecordChannelStatistics> recordChannelStatisticss = recordChannelStatisticsMapper.querySumByClientids(page.getParams());
        List<AccessStatisticVO> resultList = new ArrayList<>();
        Set<Integer> channelIds = new HashSet();
        Set<Long> userIds = new HashSet<>();
        for(AccessChannelStatistics accessStatTemp :accessChannelStatisticss){
            AccessStatisticVO temp = new AccessStatisticVO();
            BeanUtil.copyProperties(accessStatTemp,temp);
            channelIds.add(temp.getChannelid());

            for(RecordChannelStatistics recordStatTemp : recordChannelStatisticss){
                /**
                 * channelid,smstype,paytype
                 */
                if (recordStatTemp != null
                        && recordStatTemp.getPaytype() != null
                        && recordStatTemp.getSmstype() != null
                        && ObjectUtils.nullSafeEquals(recordStatTemp.getOperatorstype(),temp.getOperatorstype())
                        && recordStatTemp.getChannelid().equals(temp.getChannelid())
                        && recordStatTemp.getSmstype().equals(temp.getSmstype())
                        && recordStatTemp.getDate().equals(temp.getDate())
                        && recordStatTemp.getPaytype().equals(temp.getPaytype())){
                    temp.setRecordCosttotal(temp.getRecordCosttotal().add(recordStatTemp.getCosttotal()));
                    /**
                     * 通道计费数（条）（1+2+3）（通道侧）
                     */
                    temp.setRecordChargeTotal(
                            temp.getRecordChargeTotal()
                            + recordStatTemp.getSubmitsuccess()     // 提交成功数量（状态：1）
                            + recordStatTemp.getSubretsuccess()     // 发送成功数量（状态：2）
                            + recordStatTemp.getReportsuccess());   // 明确成功数量（状态：3）
                    /**
                     * 归属商务
                     */
                    temp.setBelongBusiness(recordStatTemp.getBelongBusiness());
                }
            }
            if(temp.getBelongBusiness() != null){
                userIds.add(temp.getBelongBusiness());
            }
            resultList.add(temp);
        }
        /**
         * 获取归属商务
         */
        List<User> users = new ArrayList<>();
        if(userIds.size() > 0){
            users = userMapper.queryBelongInfo(userIds);
        }

        /**
         * 获取通道单价
         */
        List<Channel> channelList = channelMapper.getByChannelIds(channelIds);
        for (AccessStatisticVO result: resultList){
            for (Channel channel: channelList){
                if(channel == null){
                    continue;
                }else if(channel.getCid().equals(result.getChannelid())){
                    result.setCostprice(channel.getCostprice());
                    break;
                }
            }
            for (User user: users){
                if(result.getBelongBusiness() == null){
                    continue;
                }else if(result.getBelongBusiness().equals(user.getId())){
                    result.setBelongBusinessStr(user.getRealname());
                    break;
                }
            }
        }

        page.setData(resultList);

        return page;
    }

    private void setAuthority4BasicStatis(Page page){
        // 构造数据权限查询条件
        Long userId = page.getParams().get("userId") == null ? null :
                Long.parseLong(page.getParams().get("userId").toString());
        if (userId != null){
            page.getParams().put("dataAuthorityCondition", AgentUtils.getDataAuthorityCondition(userId, true,false));
        }
    }

    @Override
    public Page<AccessStatisticVO> queryPage4BasicStatisTotal(Page page) {
        page.setPageSize(1000);

        setAuthority4BasicStatis(page);
        AccessStatisticVO statisticVO = AccessStatisticVO.init();
        Map<String, AccessStatisticVO> totalLine = new HashMap<>();
        totalLine.put("totalLine", statisticVO);
        page.setTotalOtherData(totalLine);
        if(page.getData() != null && !page.getData().isEmpty()){
            sum4BasicStatisTotal(page);
            return page;
        }
        List<RecordChannelStatistics> recordChannelStatisticss = recordChannelStatisticsMapper.queryListByClientids(page.getParams());
        do {
            queryOnePageBasicStatis(page,recordChannelStatisticss);
            page.setPageNo(page.getPageNo() + 1);
        } while (page.getPageNo() <= page.getTotalPage());
        return page;
    }

    private void sum4BasicStatisTotal(Page page){

        List<AccessStatisticVO> accessChannelStatisticss = page.getData();
        if (accessChannelStatisticss.isEmpty()){
            return;
        }
        AccessStatisticVO sum = (AccessStatisticVO) page.getTotalOtherData().get("totalLine");

        for(AccessStatisticVO temp :accessChannelStatisticss){
            sum.setSendtotal((sum.getSendtotal()== null ? 0 : sum.getSendtotal()) + temp.getSendtotal());
            sum.setSuccessTotal(sum.getSuccessTotal() + temp.getSuccessTotal());
            sum.setSubmitsuccess((sum.getSubmitsuccess() == null ? 0 : sum.getSubmitsuccess()) + temp.getSubmitsuccess());
            sum.setReportsuccess((sum.getReportsuccess() == null ? 0 : sum.getReportsuccess()) + temp.getReportsuccess());
            sum.setReportfail((sum.getReportfail() == null ? 0: sum.getReportfail()) + temp.getReportfail());
            sum.setSalefee((sum.getSalefee() == null ? BigDecimal.ZERO: sum.getSalefee()).add(temp.getSalefee()) );
            sum.setRecordChargeTotal(sum.getRecordChargeTotal() + temp.getRecordChargeTotal());
            sum.setRecordCosttotal(sum.getRecordCosttotal().add(temp.getRecordCosttotal()));
        }

    }

    private void queryOnePageBasicStatis(Page page,List<RecordChannelStatistics> recordChannelStatisticss){
        List<AccessChannelStatistics> accessChannelStatisticss = accessChannelStatisticsMapper.queryListByClientids(page);
        if (accessChannelStatisticss.isEmpty()){
            return;
        }
        AccessStatisticVO sum = (AccessStatisticVO) page.getTotalOtherData().get("totalLine");

        for(AccessChannelStatistics accessStatTemp :accessChannelStatisticss){
            AccessStatisticVO temp = new AccessStatisticVO();
            BeanUtil.copyProperties(accessStatTemp,temp);
            for(RecordChannelStatistics recordStatTemp : recordChannelStatisticss){
                /**
                 * channelid,smstype,paytype
                 */
                if (recordStatTemp != null
                        && recordStatTemp.getPaytype() != null
                        && recordStatTemp.getSmstype() != null
                        && ObjectUtils.nullSafeEquals(recordStatTemp.getOperatorstype(),temp.getOperatorstype())
                        && recordStatTemp.getChannelid().equals(temp.getChannelid())
                        && recordStatTemp.getSmstype().equals(temp.getSmstype())
                        && recordStatTemp.getDate().equals(temp.getDate())
                        && recordStatTemp.getPaytype().equals(temp.getPaytype())){
                    temp.setRecordCosttotal(temp.getRecordCosttotal().add(recordStatTemp.getCosttotal()));
                    /**
                     * 通道计费数（条）（1+2+3）（通道侧）
                     */
                    temp.setRecordChargeTotal(
                            temp.getRecordChargeTotal()
                                    + recordStatTemp.getSubmitsuccess()     // 提交成功数量（状态：1）
                                    + recordStatTemp.getSubretsuccess()     // 发送成功数量（状态：2）
                                    + recordStatTemp.getReportsuccess());   // 明确成功数量（状态：3）

                }
            }
            sum.setSendtotal((sum.getSendtotal()== null ? 0 : sum.getSendtotal()) + temp.getSendtotal());
            sum.setSuccessTotal(sum.getSuccessTotal() + temp.getSuccessTotal());
            sum.setSubmitsuccess((sum.getSubmitsuccess() == null ? 0 : sum.getSubmitsuccess()) + temp.getSubmitsuccess());
            sum.setReportsuccess((sum.getReportsuccess() == null ? 0 : sum.getReportsuccess()) + temp.getReportsuccess());
            sum.setReportfail((sum.getReportfail() == null ? 0: sum.getReportfail()) + temp.getReportfail());
            sum.setSalefee((sum.getSalefee() == null ? BigDecimal.ZERO: sum.getSalefee()).add(temp.getSalefee()) );
            sum.setRecordChargeTotal(sum.getRecordChargeTotal() + temp.getRecordChargeTotal());
            sum.setRecordCosttotal(sum.getRecordCosttotal().add(temp.getRecordCosttotal()));
        }

    }

    private ResultVO commonExport(Page page, Excel excel, String method){
        ResultVO resultVO = ResultVO.failure();
        resultVO.setMsg("生成报表失败");
        try {
            page.setPageSize(Integer.valueOf(ConfigUtils.max_export_excel_num) + 1);
            String queryMethod = method.replaceFirst("export", "query");

            page = (Page) this.getClass().getMethod(queryMethod,page.getClass()).invoke(this, page);
//            page = queryPage(page);
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
            page = (Page) this.getClass().getMethod(queryMethod + "Total",page.getClass()).invoke(this, page);
            Map<String, String> totalLine = BeanUtils.describe(page.getTotalOtherData().get("totalLine"));
            if ("queryPageTotal".equals(queryMethod+ "Total")) {
                // 成功率和毛利率导出为"-"
                totalLine.put("successRate","-");
                totalLine.put("profitRate","-");
            }
            result.add(totalLine);
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
}
