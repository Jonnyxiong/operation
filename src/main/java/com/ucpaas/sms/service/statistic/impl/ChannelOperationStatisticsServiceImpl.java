package com.ucpaas.sms.service.statistic.impl;

import com.jsmsframework.channel.entity.JsmsChannel;
import com.jsmsframework.channel.service.JsmsChannelService;
import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.enums.OperatorType;
import com.jsmsframework.common.enums.OwnerType;
import com.jsmsframework.record.record.entity.JsmsChannelOperationStatistics;
import com.jsmsframework.record.service.JsmsChannelOperationStatisticsService;
import com.jsmsframework.user.entity.JsmsUser;
import com.jsmsframework.user.service.JsmsUserService;
import com.ucpaas.sms.common.util.Collections3;
import com.ucpaas.sms.entity.po.JsmsChannelOperationStatisticsExt;
import com.ucpaas.sms.service.statistic.ChannelOperationStatisticsService;
import com.ucpaas.sms.util.beans.BeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * created by xiaoqingwen on 2018/1/11 10:18
 * 通道运营分析统计
 */
@Service
public class ChannelOperationStatisticsServiceImpl implements ChannelOperationStatisticsService {

    private static Logger logger = LoggerFactory.getLogger(ChannelOperationStatisticsServiceImpl.class);
    @Autowired
    private JsmsChannelOperationStatisticsService jsmsChannelOperationStatisticsService;
    @Autowired
    private JsmsUserService userService;
    @Autowired
    private JsmsChannelService jsmsChannelService;

    /**
     * 搜索通道统计
     * @param page
     * @return
     */
    @Override
    public JsmsPage<JsmsChannelOperationStatisticsExt> queryList(JsmsPage page) {
        //创建返回值集合
        List<JsmsChannelOperationStatisticsExt> ll=new ArrayList<>();
        //先统计通道
        //Map<String,Object> map=new HashMap<>();
        Map map = page.getParams();

        //再分页通道
        //JsmsPage<JsmsChannelOperationStatistics> wait = jsmsChannelOperationStatisticsService.searchChannelOperationStatistics(map);
        JsmsPage<JsmsChannelOperationStatistics> wait = jsmsChannelOperationStatisticsService.queryList(page);

        List<JsmsChannelOperationStatistics> list = wait.getData();
        if (Collections3.isEmpty(list)) {
            JsmsPage<JsmsChannelOperationStatisticsExt> result = new JsmsPage<>();
            BeanUtil.copyProperties(wait, result);
            return result;
        }

        JsmsPage<JsmsChannelOperationStatisticsExt> result = new JsmsPage<>();
        BeanUtil.copyProperties(wait, result);
        //获取通道
        List<JsmsChannel> channels = getJsmsChannels(list);
        //获取user
        List<JsmsUser> users = getJsmsUsers(list);

        List<JsmsChannelOperationStatisticsExt> pos = buildPoList(list, channels, users);
        //组装处理数据
        dealWith(ll, pos);

        result.setData(ll);
        return result;
    }

    private void dealWith(List<JsmsChannelOperationStatisticsExt> ll, List<JsmsChannelOperationStatisticsExt> pos) {
        //组装数据
        if(pos!=null && pos.size()>0){
            for (int i=0;i<pos.size();i++){
                JsmsChannelOperationStatisticsExt channelOperationStatisticsExt = pos.get(i);

                //处理月份
                String dateStr = String.valueOf(channelOperationStatisticsExt.getDate());
                channelOperationStatisticsExt.setDateStr(dateStr.substring(0,4)+"/"+dateStr.substring(4));

                //处理运营商类型(0：全网1：移动2：联通3：电信4：国际)
                Integer operatorstype = channelOperationStatisticsExt.getOperatorstype();
                if((OperatorType.全网.getValue()).equals(operatorstype)){
                    channelOperationStatisticsExt.setOperatorstypeStr(OperatorType.全网.getDesc());
                }else if((OperatorType.移动.getValue()).equals(operatorstype)){
                    channelOperationStatisticsExt.setOperatorstypeStr(OperatorType.移动.getDesc());
                }else if((OperatorType.联通.getValue()).equals(operatorstype)){
                    channelOperationStatisticsExt.setOperatorstypeStr(OperatorType.联通.getDesc());
                }else if((OperatorType.电信.getValue()).equals(operatorstype)){
                    channelOperationStatisticsExt.setOperatorstypeStr(OperatorType.电信.getDesc());
                }else if((OperatorType.国际.getValue()).equals(operatorstype)){
                    channelOperationStatisticsExt.setOperatorstypeStr(OperatorType.国际.getDesc());
                }
                //通道所属类型(1：自有，2：直连，3：第三方)
                Integer ownerType = channelOperationStatisticsExt.getOwnerType();
                if((OwnerType.自有.getValue()).equals(ownerType)){
                    channelOperationStatisticsExt.setOwnerTypeStr(OwnerType.自有.getDesc());
                }else if((OwnerType.直连.getValue()).equals(ownerType)){
                    channelOperationStatisticsExt.setOwnerTypeStr(OwnerType.直连.getDesc());
                }else if((OwnerType.第三方.getValue()).equals(ownerType)){
                    channelOperationStatisticsExt.setOwnerTypeStr(OwnerType.第三方.getDesc());
                }
                //jsmsClientOperationStatisticsPo.setSendSuccessRatioStr(jsmsClientOperationStatisticsPo.getSendSuccessRatio().setScale(4, BigDecimal.ROUND_DOWN).toString());
                //发送成功率加上百分号
                channelOperationStatisticsExt.setSendSuccessRatioStr(channelOperationStatisticsExt.getSendSuccessRatio().setScale(4, BigDecimal.ROUND_DOWN)+"%");
                //低消完成率加上百分号
                channelOperationStatisticsExt.setLowConsumeRatioStr(channelOperationStatisticsExt.getLowConsumeRatio().setScale(4, BigDecimal.ROUND_DOWN)+"%");
                //投诉率
                channelOperationStatisticsExt.setComplaintRatioStr((channelOperationStatisticsExt.getComplaintRatio().setScale(4, BigDecimal.ROUND_DOWN)).toString());
                //投诉系数
                channelOperationStatisticsExt.setComplaintCoefficientStr(channelOperationStatisticsExt.getComplaintCoefficient().setScale(4, BigDecimal.ROUND_DOWN).toString());
                //投诉差异值
                channelOperationStatisticsExt.setComplaintDifferenceStr(channelOperationStatisticsExt.getComplaintDifference().setScale(4, BigDecimal.ROUND_DOWN).toString());
                //成本价,处理单价(厘-->元)
                BigDecimal costprice=channelOperationStatisticsExt.getCostprice();
                BigDecimal costP=costprice.divide(BigDecimal.valueOf(1000));
                channelOperationStatisticsExt.setCostpriceStr(costP.setScale(4, BigDecimal.ROUND_DOWN).toString());
                ll.add(channelOperationStatisticsExt);
            }
        }
    }

    /**
     * 导出用的搜索
     * @return
     */
    @Override
    public List<JsmsChannelOperationStatisticsExt> findList(Map<String,Object> params) {

        List<JsmsChannelOperationStatisticsExt> ll=new ArrayList<>();
        List<JsmsChannelOperationStatistics> list = jsmsChannelOperationStatisticsService.findList(params);
        if (Collections3.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<JsmsChannel> channels = getJsmsChannels(list);
        List<JsmsUser> users = getJsmsUsers(list);
        List<JsmsChannelOperationStatisticsExt> pos = buildPoList(list, channels, users);
        //组装返回数据
        dealWith(ll, pos);
        return ll;
    }

    private List<JsmsChannelOperationStatisticsExt> buildPoList(List<JsmsChannelOperationStatistics> list,
                                                              List<JsmsChannel> channels, List<JsmsUser> users) {
        List<JsmsChannelOperationStatisticsExt> pos = new ArrayList<>();

        JsmsChannelOperationStatisticsExt po;
        for (JsmsChannelOperationStatistics jsmsChannelOperationStatistics : list) {
            po = new JsmsChannelOperationStatisticsExt();
            BeanUtil.copyProperties(jsmsChannelOperationStatistics, po);

            JsmsUser user = getUser(jsmsChannelOperationStatistics.getBelongBusiness(), users);
            if(user!=null){
                po.setRealname(user.getRealname());
            }

            JsmsChannel channel = getChannel(jsmsChannelOperationStatistics.getChannelId(), channels);
            if(channel!=null){
                po.setChannelname(channel.getChannelname());
            }

            pos.add(po);
        }
        return pos;
    }
    private JsmsUser getUser(Long belongSale, List<JsmsUser> users) {
        JsmsUser result = null;
        if (belongSale == null || Collections3.isEmpty(users)) {
            return result;
        }
        for (JsmsUser user : users) {
            if (belongSale.equals(user.getId())) {
                result = new JsmsUser();
                result.setId(user.getId());
                result.setRealname(user.getRealname());
                break;
            }
        }

        return result;
    }
    private JsmsChannel getChannel(Integer channelId, List<JsmsChannel> channels) {
        JsmsChannel result = null;
        if (channelId==null || Collections3.isEmpty(channels)) {
            return result;
        }

        for (JsmsChannel channel : channels) {
            if (channelId.equals(channel.getCid())) {
                result = new JsmsChannel();
                result.setCid(channel.getCid());
                result.setChannelname(channel.getChannelname());
                break;
            }
        }
        return result;
    }
    private List<JsmsUser> getJsmsUsers(List<JsmsChannelOperationStatistics> list) {
        List<JsmsUser> users = null;
        Set<Long> userIds = new HashSet<>();
        for (JsmsChannelOperationStatistics operationStatistics : list) {
            if (userIds.contains(operationStatistics.getBelongBusiness())) {
                continue;
            }
            userIds.add(operationStatistics.getBelongBusiness());
        }
        if (!Collections3.isEmpty(userIds)) {
            users = userService.getByIds(userIds);
        }
        return users;
    }
    private List<JsmsChannel> getJsmsChannels(List<JsmsChannelOperationStatistics> list) {
        List<JsmsChannel> channels = null;
        Set<Integer> cids = new HashSet<>();
        for (JsmsChannelOperationStatistics operationStatistics : list) {
            if (cids.contains(operationStatistics.getChannelId())) {
                continue;
            }
            cids.add(operationStatistics.getChannelId());
        }
        if (!Collections3.isEmpty(cids)) {
            channels = jsmsChannelService.getByCids(cids);
        }
        return channels;
    }
}
