package com.ucpaas.sms.service.customer;

import com.ucpaas.sms.common.entity.R;
import com.jsmsframework.order.entity.po.JsmsOemAgentPoolPo;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface CustomerManageService {

    /**
     * @Title: confirmRecharge
     * @Description: 代理商给客户充值
     * @param poolPos
     * @return
     * @return: Map<String,Object>
     */
    R oemClientRecharge(List<JsmsOemAgentPoolPo> poolPos , String clientId);


    /**
     * @Title: queryInterSmsInfo
     * @Description: 查询代理商短信信息
     * @param jsmsOemAgentPool
     * @return
     * @return: List<Map<String,Object>>
     */
//    List<Map<String, Object>> querySmsInfo(JsmsOemAgentPool jsmsOemAgentPool);
}
