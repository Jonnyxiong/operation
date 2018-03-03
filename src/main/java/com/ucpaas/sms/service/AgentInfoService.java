package com.ucpaas.sms.service;

import com.jsmsframework.product.entity.JsmsOemAgentProduct;
import com.ucpaas.sms.common.entity.R;
import com.ucpaas.sms.dto.AgentRequest;
import com.ucpaas.sms.entity.message.Account;
import com.ucpaas.sms.entity.message.AgentInfo;
import com.ucpaas.sms.entity.po.DataAuthorityCondition;
import com.ucpaas.sms.model.Page;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description 代理商信息表
 * @author huangwenjie
 * @date 2017-07-13
 */
public interface AgentInfoService {

    public int insert(AgentInfo model);
    
    public int insertBatch(List<AgentInfo> modelList);
     
    public int update(AgentInfo model);
    
    public int updateSelective(AgentInfo model);
    
    public AgentInfo getByAgentId(Integer agentId);
    
    public Page queryList(Page page);
    
    public int count(Map<String,Object> params);

	R insert(AgentRequest agentRequest, Long userId, String pageUrl, String ip,boolean isSale);

    R checkOemAgentProduct(List<JsmsOemAgentProduct> oemAgentProducts);

    R saveOemAgentProduct(List<JsmsOemAgentProduct> oemAgentProducts, String pageUrl, String ip);

    public AgentInfo getByAdminId(Integer adminId);

    //账户设置余额告警，验证码500条、通知500条、营销500条、国际10元
    int insertClientBalanceAlarm(Account account, String agentMobile, String agentEmail);

    /**
      * @Description: 根据权限获取所属销售下或所属销售为空的所有OEM代理商id
      * @Author: tanjiangqiang
      * @Date: 2017/12/18 - 11:05
      * @param dataAuthorityCondition 数据权限参数
      *
      */
    Set<Integer> findOEMAgentIdByBelongSales(DataAuthorityCondition dataAuthorityCondition);

    /**
     * @Description: 根据权限获取所属销售下或所属销售为空的所有代理商id
     * @Author: xiaoqingwen
     * @Date: 2018/02/01 - 10:11
     * @param dataAuthorityCondition 数据权限参数
     *此接口是为了修改线上财务管理-客户财务-余额账单根据回退条数查找只能查找到OEM代理商的问题  v5.19.5(现在版本)
     */
    Set<Integer> findAgentIdByBelongSales(DataAuthorityCondition dataAuthorityCondition);
}
