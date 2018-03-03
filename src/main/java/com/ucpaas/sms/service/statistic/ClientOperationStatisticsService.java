package com.ucpaas.sms.service.statistic;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.R;
import com.ucpaas.sms.entity.po.JsmsClientOperationStatisticsPo;

import java.util.List;
import java.util.Map;

/**
 * created by xiaoqingwen on 2018/1/11 10:19 客户运营分析统计
 */
public interface ClientOperationStatisticsService {

	R checkSearchCondition(JsmsClientOperationStatisticsPo clientOperationStatistics);

	List<JsmsClientOperationStatisticsPo> findList(JsmsClientOperationStatisticsPo clientOperationStatistics);

	JsmsPage<JsmsClientOperationStatisticsPo> queryPage(JsmsClientOperationStatisticsPo clientOperationStatistics);

	List<Map<String, Object>> ListToMap(List<JsmsClientOperationStatisticsPo> list);
}
