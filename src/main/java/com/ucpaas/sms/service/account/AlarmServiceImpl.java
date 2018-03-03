package com.ucpaas.sms.service.account;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.entity.message.ClientBalanceAlarm;
import com.ucpaas.sms.mapper.message.ClientBalanceAlarmMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by dylan on 2017/9/12.
 */
@Service
public class AlarmServiceImpl implements AlarmService {
    @Autowired
    private ClientBalanceAlarmMapper clientBalanceAlarmMapper;

    @Override
    @Transactional
    public ResultVO insert(ClientBalanceAlarm clientBalanceAlarm) {
        this.clientBalanceAlarmMapper.insert(clientBalanceAlarm);
        return ResultVO.successDefault();
    }

    @Override
    @Transactional
    public ResultVO insertBatch(List<ClientBalanceAlarm> clientBalanceAlarmList) {
        this.clientBalanceAlarmMapper.insertBatch(clientBalanceAlarmList);
        return ResultVO.successDefault();
    }

    @Override
    @Transactional
    public ResultVO delete(Integer id) {
        ClientBalanceAlarm clientBalanceAlarm = this.clientBalanceAlarmMapper.getById(id);
        if(clientBalanceAlarm != null)
            this.clientBalanceAlarmMapper.delete(id);
        return ResultVO.successDefault();
    }

    @Override
    @Transactional
    public ResultVO update(ClientBalanceAlarm clientBalanceAlarm) {
        ClientBalanceAlarm old = this.clientBalanceAlarmMapper.getById(clientBalanceAlarm.getId());
        if(old == null){
            return ResultVO.failure();
        }
        this.clientBalanceAlarmMapper.update(clientBalanceAlarm);
        ClientBalanceAlarm newClientBalanceAlarm = this.clientBalanceAlarmMapper.getById(clientBalanceAlarm.getId());
        return ResultVO.successDefault(newClientBalanceAlarm);
    }

    @Override
    @Transactional
    public ResultVO updateSelective(ClientBalanceAlarm clientBalanceAlarm) {
        ClientBalanceAlarm old = this.clientBalanceAlarmMapper.getById(clientBalanceAlarm.getId());
        if(old != null)
            this.clientBalanceAlarmMapper.updateSelective(clientBalanceAlarm);
        return ResultVO.successDefault();
    }

    @Override
    @Transactional
    public ResultVO updateIdempotent(ClientBalanceAlarm oldModel,ClientBalanceAlarm newModel) {
        if (newModel == null)
            return ResultVO.failure( "参数为空，不保存！");

        if (StringUtils.isBlank(newModel.getClientid()))
            return ResultVO.failure( "客户ID不能为空！");

        if (!newModel.getClientid().matches("^[a-z]\\w{4}\\d$"))
            return ResultVO.failure( "客户ID错误！");

        /*if (StringUtils.isBlank(newModel.getCcAlarmPhone()))
            return ResultVO.failure( "运营手机号码不能为空！");

        if (newModel.getCcAlarmPhone().length() > 1000)
            return ResultVO.failure( "运营手机号码长度不能超过1000个字符！");*/

        if (newModel.getAlarmNumber() == null)
            return ResultVO.failure( "账户短信余额限制不能为空！");

        if (newModel.getAlarmNumber() > 1000 * 1000 * 100)
            return ResultVO.failure( "账户短信余额限制最大为1亿条！");
        Date now = new Date();
        newModel.setResetTime(now);
        newModel.setUpdateTime(now);
        newModel.setReminderNumber(1);

        if (oldModel == null){
            newModel.setCreateTime(now);
            return this.insert(newModel);
        }

        Map<String,ClientBalanceAlarm> idempotentParams = new HashMap<>();
        idempotentParams.put("oldModel", oldModel);
        idempotentParams.put("newModel", newModel);

        int update = this.clientBalanceAlarmMapper.updateIdempotent(idempotentParams);
        if (update > 0 ){
            return ResultVO.successDefault("设置成功");
        }
        return ResultVO.failure("当前数据不是最新数据, 请刷新后设置...");
    }

    @Override
    @Transactional
    public ClientBalanceAlarm getByClientId(String clientId ){
        ClientBalanceAlarm clientBalanceAlarm = this.clientBalanceAlarmMapper.getByClientId(clientId);
        return clientBalanceAlarm;
    }

    @Override
    @Transactional
    public ResultVO queryList(JsmsPage<ClientBalanceAlarm> page) {
        List<ClientBalanceAlarm> list = this.clientBalanceAlarmMapper.queryList(page);
        return ResultVO.successDefault(list);
    }

    @Override
    @Transactional
    public ResultVO count(ClientBalanceAlarm clientBalanceAlarm) {
        return ResultVO.successDefault(this.clientBalanceAlarmMapper.count(clientBalanceAlarm));
    }
}
