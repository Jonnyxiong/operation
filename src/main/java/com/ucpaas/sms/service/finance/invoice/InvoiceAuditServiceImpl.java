package com.ucpaas.sms.service.finance.invoice;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.enums.WebId;
import com.jsmsframework.common.enums.invoice.InvoiceBodyEnum;
import com.jsmsframework.common.enums.invoice.InvoiceStatusEnum;
import com.jsmsframework.common.enums.invoice.InvoiceTypeEnum;
import com.jsmsframework.common.util.BeanUtil;
import com.jsmsframework.common.util.JsonUtil;
import com.jsmsframework.common.util.StringUtils;
import com.jsmsframework.finance.dto.JsmsAgentInvoiceListDTO;
import com.jsmsframework.finance.entity.JsmsAgentInvoiceList;
import com.jsmsframework.finance.exception.JsmsAgentInvoiceListException;
import com.jsmsframework.finance.service.JsmsAgentAccountService;
import com.jsmsframework.finance.service.JsmsAgentInvoiceListService;
import com.jsmsframework.user.entity.JsmsAgentInfo;
import com.jsmsframework.user.entity.JsmsUser;
import com.jsmsframework.user.finance.service.JsmsUserFinanceService;
import com.jsmsframework.user.service.JsmsAgentInfoService;
import com.jsmsframework.user.service.JsmsUserService;
import com.ucpaas.sms.dto.InvoiceAuditVO;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.entity.po.DataAuthorityCondition;
import com.ucpaas.sms.service.AgentInfoService;
import com.ucpaas.sms.util.AgentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 发票审核实现类
 *
 * @author tanjiangqiang
 * @create 2018-01-23 18:03
 */
@Service
public class InvoiceAuditServiceImpl implements InvoiceAuditService {

    protected static Logger logger = LoggerFactory.getLogger(InvoiceAuditServiceImpl.class);

    @Autowired
    private JsmsAgentInvoiceListService jsmsAgentInvoiceListService;
    @Autowired
    private JsmsUserService userService;
    @Autowired
    private JsmsAgentInfoService jsmsAgentInfoService;
    @Autowired
    private JsmsAgentAccountService jsmsAgentAccountService;
    @Autowired
    private InvoiceRecordService invoiceRecordService;

    @Override
    public JsmsPage<JsmsAgentInvoiceListDTO> getList(Map<String, Object> params, UserSession userSession) {
        JsmsPage<JsmsAgentInvoiceListDTO> jsmsPage = new JsmsPage();
        jsmsPage.setOrderByClause(" create_time DESC");
        String rows = (String) params.get("rows");
        String page = (String) params.get("page");
        if (StringUtils.isNotBlank(rows) && StringUtils.isNotBlank(page)) {
            jsmsPage.setRows(Integer.valueOf(rows));
            jsmsPage.setPage(Integer.valueOf(page));
        }
        jsmsPage.setParams(params);
        jsmsPage = invoiceRecordService.list(jsmsPage, userSession);
        return jsmsPage;
    }

    @Override
    public JsmsAgentInvoiceListDTO view(String invoiceId) {
        if (StringUtils.isBlank(invoiceId)) {
            logger.error("发票审核查看详情,invoiceId为空------", JsonUtil.toJson(invoiceId));
            throw new JsmsAgentInvoiceListException("申请ID为空");
        }
        JsmsAgentInvoiceList jsmsAgentInvoiceList = jsmsAgentInvoiceListService.getByInvoiceId(invoiceId);
        if (null == jsmsAgentInvoiceList) {
            logger.error("发票审核查看详情失败,invoiceId错误------", invoiceId);
            throw new JsmsAgentInvoiceListException("发票审核查看详情失败,申请ID错误");
        }
        JsmsAgentInvoiceListDTO vo = new JsmsAgentInvoiceListDTO();
        BeanUtil.copyProperties(jsmsAgentInvoiceList, vo);
        vo.setStatusStr(InvoiceStatusEnum.getDescByValue(vo.getStatus()));
        vo.setInvoiceBodyStr(InvoiceBodyEnum.getDescByValue(vo.getInvoiceBody()));
        vo.setInvoiceTypeStr(InvoiceTypeEnum.getDescByValue(vo.getInvoiceType()));
        vo.setInvoiceAmountStr(vo.getInvoiceAmount().setScale(2, BigDecimal.ROUND_DOWN).toString());
        vo.setInvoiceBodyStr(InvoiceBodyEnum.getDescByValue(vo.getInvoiceBody()));
        // 详细地址 = 省市区 + 详细地址
        String toAddDetail = null == vo.getToAddr() || null == vo.getToAddrDetail() ? "" : vo.getToAddr() +  vo.getToAddrDetail();
        vo.setToAddrDetail(toAddDetail);
        JsmsAgentInfo info = jsmsAgentInfoService.getByAgentId(vo.getAgentId());
        vo.setName(info.getAgentName());

        // 存储客户归属销售和申请人(操作者)
        Set<Long> userIds = new HashSet<>(4);
        Long operator = vo.getOperator();
        if (null != operator) {
            userIds.add(operator);
        }
        Long auditor = vo.getAuditor();
        if (null != auditor) {
            userIds.add(vo.getAuditor());
        }
        Long applicant = vo.getApplicant();
        if (null != applicant) {
            userIds.add(vo.getApplicant());
        }
        userIds.add(info.getBelongSale());
        // 客户归属销售和申请人(操作者)批量查询用户
        Map<Long, String> userMap = new HashMap<>(userIds.size());
        List<JsmsUser> users = userService.getByIds(userIds);
        for (JsmsUser user : users) {
            userMap.put(user.getId(), user.getRealname());
        }
        vo.setOperatorName(null == operator ? "-" : userMap.get(vo.getOperator()));
        vo.setAuditorStr(null == auditor ? "-" : userMap.get(vo.getAuditor()));
        vo.setApplicantStr(null == applicant ? "-" : userMap.get(vo.getApplicant()));
        vo.setBelongSaleStr(userMap.get(info.getBelongSale()));
        // 发票金额保留两位小数
        vo.setInvoiceAmountStr(vo.getInvoiceAmount().setScale(2, BigDecimal.ROUND_DOWN).toString());
        return vo;
    }

    @Override
    @Transactional("message")
    public void doaudit(String invoiceId, Integer status, String auditFailCause, UserSession userSession) throws JsmsAgentInvoiceListException {
        if (StringUtils.isBlank(invoiceId)) {
            logger.error("发票审核失败,invoiceId为空------", invoiceId);
            throw new JsmsAgentInvoiceListException("发票审核失败,申请ID为空");
        }
        if (null == status) {
            logger.error("发票审核失败,status为空------", status);
            throw new JsmsAgentInvoiceListException("发票审核失败,状态为空");
        }
        // 数据库中的数据
        JsmsAgentInvoiceList jsmsAgentInvoiceList = jsmsAgentInvoiceListService.getByInvoiceId(invoiceId);
        if (null == jsmsAgentInvoiceList) {
            logger.error("发票审核失败,invoiceId错误------", invoiceId);
            throw new JsmsAgentInvoiceListException("发票审核失败,invoiceId错误");
        }
        // 要更新的数据
        JsmsAgentInvoiceList newModel = new JsmsAgentInvoiceList();
        if (!InvoiceStatusEnum.待审核.getValue().equals(jsmsAgentInvoiceList.getStatus())) {
            logger.error("发票审核失败,该发票状态不是待审核-----{}", jsmsAgentInvoiceList);
            throw new JsmsAgentInvoiceListException("发票审核失败,该发票状态不是待审核,请刷新");
        }

        if (InvoiceStatusEnum.审核不通过.getValue().equals(status)) {
            if (StringUtils.isBlank(auditFailCause)) {
                logger.error("发票审核失败,auditFailCause为空------", auditFailCause);
                throw new JsmsAgentInvoiceListException("发票审核失败,审核不通过原因为空");
            }
            newModel.setAuditFailCause(auditFailCause);
            newModel.setUpdateTime(new Date());
            newModel.setAuditor(userSession.getId());
            newModel.setStatus(InvoiceStatusEnum.审核不通过.getValue());
            int n = jsmsAgentInvoiceListService.updateStatus(newModel, jsmsAgentInvoiceList);
            if (n != 1) {
                logger.error("发票审核失败,更新SQL错误-----{}", jsmsAgentInvoiceList);
                throw new JsmsAgentInvoiceListException("服务器正在检修...");
            }
        } else {
            if (InvoiceTypeEnum.普通发票.getValue().equals(jsmsAgentInvoiceList.getInvoiceType())) {
                newModel.setStatus(InvoiceStatusEnum.已邮寄.getValue());
                newModel.setUpdateTime(new Date());
                newModel.setAuditor(userSession.getId());
                int n = jsmsAgentInvoiceListService.updateStatus(newModel, jsmsAgentInvoiceList);
                if (n != 1) {
                    logger.error("发票审核失败,更新SQL错误-----{}", jsmsAgentInvoiceList);
                    throw new JsmsAgentInvoiceListException("服务器正在检修...");
                }
                // 更新代理商已开票金额
                BigDecimal invoiceAmount = jsmsAgentInvoiceList.getInvoiceAmount();
                // 更改为负数
                invoiceAmount = invoiceAmount.multiply(new BigDecimal(-1));
                int u = jsmsAgentAccountService.reduceHasTakeInvoice(jsmsAgentInvoiceList.getAgentId(), invoiceAmount, false);
                if (u != 1) {
                    logger.error("发票审核失败,更新客户已开票金额错误-----{}", jsmsAgentInvoiceList);
                    throw new JsmsAgentInvoiceListException("服务器正在检修...");
                }
            } else {
                newModel.setStatus(InvoiceStatusEnum.开票中.getValue());
                newModel.setUpdateTime(new Date());
                newModel.setAuditor(userSession.getId());
                int n = jsmsAgentInvoiceListService.updateStatus(newModel, jsmsAgentInvoiceList);
                if (n != 1) {
                    logger.error("发票审核失败,更新SQL错误-----{}", jsmsAgentInvoiceList);
                    throw new JsmsAgentInvoiceListException("服务器正在检修...");
                }
            }
        }
    }

    @Override
    @Transactional("message")
    public void audittoback(String invoiceId, String auditFailCause, UserSession userSession) throws JsmsAgentInvoiceListException {
        if (StringUtils.isBlank(invoiceId)) {
            logger.error("发票审核驳回失败,invoiceId为空------", invoiceId);
            throw new JsmsAgentInvoiceListException("发票审核驳回失败,申请ID为空");
        }
        if (StringUtils.isBlank(auditFailCause)) {
            logger.error("发票审核驳回失败,auditFailCause为空------", auditFailCause);
            throw new JsmsAgentInvoiceListException("发票审核驳回失败,审核不通过原因为空");
        }
        // 数据库中的数据
        JsmsAgentInvoiceList jsmsAgentInvoiceList = jsmsAgentInvoiceListService.getByInvoiceId(invoiceId);
        if (null == jsmsAgentInvoiceList) {
            logger.error("发票审核驳回失败,invoiceId错误------", invoiceId);
            throw new JsmsAgentInvoiceListException("发票审核驳回失败,invoiceId错误");
        }
        if (!InvoiceTypeEnum.增值税专票.getValue().equals(jsmsAgentInvoiceList.getInvoiceType())) {
            logger.error("发票审核驳回失败,该发票类型不是增值税专票-----{}", jsmsAgentInvoiceList);
            throw new JsmsAgentInvoiceListException("发票审核驳回失败,该发票类型不是增值税专票");
        }
        if (InvoiceStatusEnum.审核不通过.getValue().equals(jsmsAgentInvoiceList.getStatus())) {
            logger.error("发票审核驳回失败,该发票状态已是审核不通过-----{}", jsmsAgentInvoiceList);
            throw new JsmsAgentInvoiceListException("发票审核驳回失败,该发票状态已是审核不通过");
        }
        // 要更新的数据
        JsmsAgentInvoiceList newModel = new JsmsAgentInvoiceList();
        newModel.setAuditFailCause(auditFailCause);
        newModel.setStatus(InvoiceStatusEnum.审核不通过.getValue());
        newModel.setUpdateTime(new Date());
        newModel.setAuditor(userSession.getId());
        int n = jsmsAgentInvoiceListService.updateStatus(newModel, jsmsAgentInvoiceList);
        if (n != 1) {
            logger.error("发票审核驳回失败,更新SQL错误-----{}", jsmsAgentInvoiceList);
            throw new JsmsAgentInvoiceListException("服务器正在检修...");
        }
    }

    @Override
    @Transactional("message")
    public void express(String invoiceId, Integer expressCompany, String expressOrder, UserSession userSession) throws JsmsAgentInvoiceListException {
        if (StringUtils.isBlank(invoiceId)) {
            logger.error("发票邮寄失败,invoiceId为空------", invoiceId);
            throw new JsmsAgentInvoiceListException("发票邮寄失败,申请ID为空");
        }
        if (null == expressCompany) {
            logger.error("发票邮寄失败,expressCompany为空------", expressCompany);
            throw new JsmsAgentInvoiceListException("发票邮寄失败,快递公司为空");
        }
        if (StringUtils.isBlank(expressOrder)) {
            logger.error("发票邮寄失败,expressOrder为空------", expressOrder);
            throw new JsmsAgentInvoiceListException("发票邮寄失败,快递单号为空");
        }
        // 数据库中的数据
        JsmsAgentInvoiceList jsmsAgentInvoiceList = jsmsAgentInvoiceListService.getByInvoiceId(invoiceId);
        if (null == jsmsAgentInvoiceList) {
            logger.error("发票审核查看详情失败,invoiceId错误------", invoiceId);
            throw new JsmsAgentInvoiceListException("发票审核查看详情失败,invoiceId错误");
        }
        if (!InvoiceTypeEnum.增值税专票.getValue().equals(jsmsAgentInvoiceList.getInvoiceType())) {
            logger.error("发票邮寄失败,该发票类型不是增值税专票-----{}", jsmsAgentInvoiceList);
            throw new JsmsAgentInvoiceListException("发票邮寄失败,该发票类型不是增值税专票");
        }
        if (!InvoiceStatusEnum.开票中.getValue().equals(jsmsAgentInvoiceList.getStatus())) {
            logger.error("发票邮寄失败,该发票状态不是开票中-----{}", jsmsAgentInvoiceList);
            throw new JsmsAgentInvoiceListException("发票邮寄失败,该发票状态不是开票中");
        }
        // 要更新的数据
        JsmsAgentInvoiceList newModel = new JsmsAgentInvoiceList();
        newModel.setOperator(userSession.getId());
        newModel.setStatus(InvoiceStatusEnum.已邮寄.getValue());
        newModel.setUpdateTime(new Date());
        newModel.setExpressCompany(expressCompany);
        newModel.setExpressOrder(expressOrder);
        int n = jsmsAgentInvoiceListService.updateStatus(newModel, jsmsAgentInvoiceList);
        if (n != 1) {
            logger.error("发票邮寄失败,更新SQL错误-----{}", jsmsAgentInvoiceList);
            throw new JsmsAgentInvoiceListException("服务器正在检修...");
        }
        // 更新代理商已开票金额
        BigDecimal invoiceAmount = jsmsAgentInvoiceList.getInvoiceAmount();
        // 更改为负数
        invoiceAmount = invoiceAmount.multiply(new BigDecimal(-1));
        int u = jsmsAgentAccountService.reduceHasTakeInvoice(jsmsAgentInvoiceList.getAgentId(), invoiceAmount, false);
        if (u != 1) {
            logger.error("发票邮寄失败,更新客户已开票金额错误-----{}", jsmsAgentInvoiceList);
            throw new JsmsAgentInvoiceListException("服务器正在检修...");
        }
    }
}