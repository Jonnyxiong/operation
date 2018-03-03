package com.ucpaas.sms.service.finance.invoice;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.enums.WebId;
import com.jsmsframework.finance.dto.JsmsAgentInvoiceListDTO;
import com.jsmsframework.user.entity.JsmsUser;
import com.ucpaas.sms.entity.UserSession;

import java.util.List;
import java.util.Map;

/**
 * (发票)申请记录接口
 *
 * @author xiaoqingwen
 * @create 2018-01-25 11:51
 */
public interface InvoiceRecordService {

    //根据用户数据权限去获取归属销售
    List<JsmsUser> queryUserByDataAuthority(Long id);
    //分页获取申请记录列表
    JsmsPage list(JsmsPage page, UserSession userSession);
    //查看单个申请记录信息
    R view(Integer id, Integer invoiceType);
    //申请记录的状态改变
    R cancel(Integer id,Long userId);
    //申请记录的导出
    List<JsmsAgentInvoiceListDTO> export(Map<String,Object> params, UserSession userSession);

}