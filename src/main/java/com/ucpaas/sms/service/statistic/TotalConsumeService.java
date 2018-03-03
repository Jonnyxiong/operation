package com.ucpaas.sms.service.statistic;

import com.jsmsframework.common.dto.ResultVO;

import java.util.List;
import java.util.Map;

/**
 * Created by dylan on 2017/7/26.
 */
public interface TotalConsumeService {


    /**
     * 各BU消耗占比
     */
    List consumeVsByBU(Map params);
    /**
     * 月合计
     */
    List queryMonthSumList(Map params);
    /**
     * 合计
     */
    List querySumList(Map params);
    /**
     * 回款金额占比
     */
    ResultVO consumeVsByPay(Map params);
    /**
     * TOP10客户发送量
     */
    ResultVO topClients(Map params);
    /**
     * TOP10通道消耗量
     */
    ResultVO topChannels(Map params);
}
