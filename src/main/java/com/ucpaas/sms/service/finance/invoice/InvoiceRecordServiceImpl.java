package com.ucpaas.sms.service.finance.invoice;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.enums.WebId;
import com.jsmsframework.common.enums.invoice.InvoiceStatusEnum;
import com.jsmsframework.common.util.JsonUtil;
import com.jsmsframework.finance.dto.JsmsAgentInvoiceListDTO;
import com.jsmsframework.finance.entity.JsmsAgentInvoiceList;
import com.jsmsframework.finance.exception.JsmsAgentInvoiceListException;
import com.jsmsframework.finance.service.JsmsAgentInvoiceListService;
import com.jsmsframework.user.entity.JsmsUser;
import com.jsmsframework.user.finance.service.JsmsUserFinanceService;
import com.jsmsframework.user.service.JsmsUserService;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.entity.po.DataAuthorityCondition;
import com.ucpaas.sms.service.AgentInfoService;
import com.ucpaas.sms.util.AgentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * created by xiaoqingwen on 2018/1/25 11:57
 */
@Service
public class InvoiceRecordServiceImpl implements InvoiceRecordService {

    protected static Logger logger = LoggerFactory.getLogger(InvoiceRecordServiceImpl.class);
    @Autowired
    private JsmsUserService jsmsUserService;
    @Autowired
    private JsmsUserFinanceService jsmsUserFinanceService;
    @Autowired
    private JsmsAgentInvoiceListService jsmsAgentInvoiceListService;
    @Autowired
    private AgentInfoService agentInfoService;

    /**
     * 根据用户数据权限去获取归属销售
     *
     * @return
     */
    @Override
    public List<JsmsUser> queryUserByDataAuthority(Long id) {

        DataAuthorityCondition dataAuthorityCondition = AgentUtils.getDataAuthorityCondition(Long.valueOf(id), true, false);
        List<Long> ids = dataAuthorityCondition.getIds();
        List<JsmsUser> list = null;
        if (ids != null && ids.size() > 0) {
            Set<Long> idList = new HashSet<>();
            for (int i = 0; i < ids.size(); i++) {
                idList.add(ids.get(i));
            }
            logger.debug("此用户名下的归属销售有-->{}", idList);
            list = jsmsUserService.getByIds(idList);
        } else {
            logger.debug("此用户未查找到归属销售,用户id为-->{}", id);
        }
        return list;
    }

    /**
     * 分页获取申请记录列表
     *
     * @param page
     * @return
     */
    @Override
    public JsmsPage list(JsmsPage page, UserSession userSession) {
        // 数据权限控制
        DataAuthorityCondition dataAuthorityCondition = AgentUtils.getDataAuthorityCondition(userSession.getId(), true, false);
        Set<Integer> agentIds = agentInfoService.findOEMAgentIdByBelongSales(dataAuthorityCondition);
        if (null == agentIds || agentIds.isEmpty()) {
            logger.debug("数据权限控制,获取该用户下客户为空------", JsonUtil.toJson(agentIds));
            return new JsmsPage<>();
        }
        page.getParams().put("agentIdPermission", agentIds);
        WebId webId = WebId.运营平台;
        JsmsPage jsmsPage = jsmsUserFinanceService.queryPageList(page, webId);
//        //格式化金额为小数点后两位
//        if (jsmsPage != null) {
//            List<JsmsAgentInvoiceListDTO> list = jsmsPage.getData();
//            if (list != null && list.size() > 0) {
//                for (int i = 0; i < list.size(); i++) {
//                    JsmsAgentInvoiceListDTO jsmsAgentInvoiceListDTO = list.get(i);
//                    BigDecimal invoiceAmount = jsmsAgentInvoiceListDTO.getInvoiceAmount();
//                    jsmsAgentInvoiceListDTO.setInvoiceAmountStr(invoiceAmount.setScale(2, BigDecimal.ROUND_DOWN).toString());
//                }
//            }
//        }

        return jsmsPage;
    }

    /**
     * 查看单个申请记录信息
     *
     * @param id
     * @param invoiceType
     * @return
     */
    @Override
    public R view(Integer id, Integer invoiceType) {
        //Integer id,Integer invoiceType,WebId webId
        WebId webId = WebId.运营平台;
        JsmsAgentInvoiceListDTO jsmsAgentInvoiceListDTO = jsmsUserFinanceService.checkDetailedInformation(id, invoiceType, webId);

        if(jsmsAgentInvoiceListDTO==null){
            return R.error("数据不存在!");
        }
        //格式化金额为小数点后两位
//        BigDecimal invoiceAmount = jsmsAgentInvoiceListDTO.getInvoiceAmount();
//        jsmsAgentInvoiceListDTO.setInvoiceAmountStr(invoiceAmount.setScale(2, BigDecimal.ROUND_DOWN).toString());
        return R.ok("获取数据成功!",jsmsAgentInvoiceListDTO);
    }

    /**
     * 申请记录的状态改变
     *
     * @param id
     * @return
     */
    @Override
    public R cancel(Integer id, Long userId) {

        JsmsAgentInvoiceList jsmsAgentInvoiceList = jsmsAgentInvoiceListService.getById(id);
        jsmsAgentInvoiceList.setOperator(userId);
        if (jsmsAgentInvoiceList == null) {
            return R.error("此申请记录不存在!");
        }
        //只有此代理商申请记录为待审核的时候才能把状态改为已取消
        if(!InvoiceStatusEnum.待审核.getValue().equals(jsmsAgentInvoiceList.getStatus())){
            logger.debug("状态为非待审核,无法取消!");
            return R.error("数据状态为非待审核,无法取消!");
        }

        int i = jsmsAgentInvoiceListService.cancelApply(jsmsAgentInvoiceList);
        if (i != 1) {
            logger.debug("更改代理商发票申请记录列表-->t_sms_agent_invoice_list失败,id为-->{}状态为-->{}", JsonUtil.toJson(jsmsAgentInvoiceList));
            throw new JsmsAgentInvoiceListException("更改状态失败i!");
        }
        logger.debug("运营平台更改申请记录状态成功-->", JsonUtil.toJson(jsmsAgentInvoiceList));
        return R.ok("更新状态成功!");
    }

    /**
     * 导出申请记录
     *
     * @return
     */
    @Override
    public List<JsmsAgentInvoiceListDTO> export(Map<String, Object> params, UserSession userSession) {
        // 数据权限控制
        DataAuthorityCondition dataAuthorityCondition = AgentUtils.getDataAuthorityCondition(userSession.getId(), true, false);
        Set<Integer> agentIds = agentInfoService.findOEMAgentIdByBelongSales(dataAuthorityCondition);
        if (null == agentIds || agentIds.isEmpty()) {
            logger.debug("数据权限控制,获取该用户下客户为空------", JsonUtil.toJson(agentIds));
            return new ArrayList<>();
        }
        params.put("agentIdPermission", agentIds);
        List<JsmsAgentInvoiceListDTO> list = jsmsUserFinanceService.getAgentInvoiceLists(params);
        //格式化金额为小数点后两位
//        if (list != null && list.size() > 0) {
//            for (int i = 0; i < list.size(); i++) {
//                JsmsAgentInvoiceListDTO jsmsAgentInvoiceListDTO = list.get(i);
//                BigDecimal invoiceAmount = jsmsAgentInvoiceListDTO.getInvoiceAmount();
//                jsmsAgentInvoiceListDTO.setInvoiceAmountStr(invoiceAmount.setScale(2, BigDecimal.ROUND_DOWN).toString());
//            }
//        }
        return list;
    }
}
