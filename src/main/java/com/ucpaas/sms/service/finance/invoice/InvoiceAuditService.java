package com.ucpaas.sms.service.finance.invoice;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.finance.dto.JsmsAgentInvoiceListDTO;
import com.jsmsframework.finance.exception.JsmsAgentInvoiceListException;
import com.ucpaas.sms.dto.InvoiceAuditVO;
import com.ucpaas.sms.entity.UserSession;

import java.util.Map;

/**
 * 发票审核接口
 *
 * @author tanjiangqiang
 * @create 2018-01-23 17:59
 */
public interface InvoiceAuditService {

    /**
     * @param params      参数
     * @param userSession 用户信息
     * @Description: 发票审核列表
     * @Author: tanjiangqiang
     * @Date: 2018/1/23 - 20:19
     */
    JsmsPage<JsmsAgentInvoiceListDTO> getList(Map<String, Object> params, UserSession userSession);

    /**
     * @param invoiceId
     * @Description: 根据申请id获取数据
     * @Author: tanjiangqiang
     * @Date: 2018/1/24 - 12:23
     */
    JsmsAgentInvoiceListDTO view(String invoiceId);

    /**
     * @param invoiceId      申请id
     * @param status         审核状态
     * @param auditFailCause 审核不通过原因(审核通过可空)
     * @param userSession    审核者
     * @Description: 发票审核--审核
     * @Author: tanjiangqiang
     * @Date: 2018/1/24 - 15:41
     */
    void doaudit(String invoiceId, Integer status, String auditFailCause, UserSession userSession) throws JsmsAgentInvoiceListException;

    /**
     * @param invoiceId      申请id
     * @param auditFailCause 审核不通过原因
     * @param userSession    审核者
     * @Description: 发票审核--审核驳回
     * @Author: tanjiangqiang
     * @Date: 2018/1/24 - 15:41
     */
    void audittoback(String invoiceId, String auditFailCause, UserSession userSession) throws JsmsAgentInvoiceListException;

    /**
     * @param invoiceId
     * @param expressCompany
     * @param expressOrder
     * @param userSession
     * @Description: 发票审核--邮寄
     * @Author: tanjiangqiang
     * @Date: 2018/1/24 - 17:11
     */
    void express(String invoiceId, Integer expressCompany, String expressOrder, UserSession userSession) throws JsmsAgentInvoiceListException;
}