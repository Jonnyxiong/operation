package com.ucpaas.sms.service.returnFail;


import com.jsmsframework.access.access.entity.JsmsClientFailReturn;
import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.entity.access.ClientFailReturn;
import com.ucpaas.sms.model.PageContainer;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @description 客户失败返回清单表
 * @author Niu.T
 * @date 2017-10-11
 */
public interface ClientFailReturnService {

    int insert(ClientFailReturn model);

    int insertBatch(List<ClientFailReturn> modelList);

    int update(ClientFailReturn model);

    int updateSelective(ClientFailReturn model);

    ClientFailReturn getById(Integer id);

    ResultVO returnSendFail(JsmsClientFailReturn clientFailReturn, List<Integer> waitUpdateFailIds, Long orderId,Long adminId ,Date updateTime);

    ResultVO returnOemSendFail(JsmsClientFailReturn clientFailReturn, List<Integer> waitUpdateFailIds, Integer agentId,Long orderId,Long orderNo,Long adminId, Date updateTime);

    PageContainer queryList(String clientid,Map params);

    List<ClientFailReturn> findList(ClientFailReturn model);

    int count(Map<String, Object> params);

}
