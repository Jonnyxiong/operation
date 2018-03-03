package com.ucpaas.sms.service.statistic;

import com.jsmsframework.common.dto.JsmsPage;
import com.ucpaas.sms.entity.po.JsmsChannelOperationStatisticsExt;

import java.util.List;
import java.util.Map;

/**
 * created by xiaoqingwen on 2018/1/11 10:17
 * 通道运营统计
 */
public interface ChannelOperationStatisticsService {
    //搜索通道统计
    JsmsPage<JsmsChannelOperationStatisticsExt> queryList(JsmsPage page);
    //导出用的搜索
    List<JsmsChannelOperationStatisticsExt> findList(Map<String,Object> params);

}
