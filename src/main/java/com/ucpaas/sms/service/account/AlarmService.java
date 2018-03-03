package com.ucpaas.sms.service.account;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.entity.message.ClientBalanceAlarm;

import java.util.List;

/**
 * @description 客户组
 * @author
 * @date 2017-07-25
 */
public interface AlarmService {

	public ResultVO insert(ClientBalanceAlarm clientBalanceAlarm);

	public ResultVO insertBatch(List<ClientBalanceAlarm> clientBalanceAlarmList);

	public ResultVO delete(Integer id);

	public ResultVO update(ClientBalanceAlarm clientBalanceAlarm);

	public ResultVO updateSelective(ClientBalanceAlarm clientBalanceAlarm);

	public ResultVO updateIdempotent(ClientBalanceAlarm oldModel,ClientBalanceAlarm newModel);

	public ClientBalanceAlarm getByClientId(String clientId);

	public ResultVO queryList(JsmsPage<ClientBalanceAlarm> page);

	public ResultVO count(ClientBalanceAlarm clientBalanceAlarm);



}
