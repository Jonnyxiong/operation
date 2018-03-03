package com.ucpaas.sms.service.finance.invoice;

import com.jsmsframework.common.dto.R;
import com.jsmsframework.finance.entity.JsmsAgentInvoiceList;
import com.jsmsframework.user.entity.JsmsAgentInfo;
import com.ucpaas.sms.entity.po.JsmsAgentInvoiceListPo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface AgentInvoiceService {

    List<JsmsAgentInfo> getAgentList(Long userId);

    List<JsmsAgentInvoiceList> findInvoiceList4ReturnInvoice(Map<String, Object> params);

    BigDecimal getCanInvoiceAmount(Integer agentId);

    BigDecimal getCanBackAmount(Integer agentId);

    BigDecimal getHasTakeInvoiceInitAmount(Integer agentId);

    R applicationInvoice(JsmsAgentInvoiceList invoiceList);

    R returnInvoice(JsmsAgentInvoiceListPo invoiceList);

    R getInvoiceConfig(Integer agentId, Integer invoiceType);
}
