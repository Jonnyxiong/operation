package com.ucpaas.sms.service.finance.invoice;

import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.enums.AgentType;
import com.jsmsframework.common.enums.Code;
import com.jsmsframework.common.enums.OauthStatusEnum;
import com.jsmsframework.common.enums.WebIdNew;
import com.jsmsframework.common.enums.invoice.InvoiceBodyEnum;
import com.jsmsframework.common.enums.invoice.InvoiceSourceEnum;
import com.jsmsframework.common.enums.invoice.InvoiceStatusEnum;
import com.jsmsframework.common.enums.invoice.InvoiceTypeEnum;
import com.jsmsframework.finance.entity.JsmsAgentAccount;
import com.jsmsframework.finance.entity.JsmsAgentInvoiceList;
import com.jsmsframework.finance.exception.JsmsAgentInvoiceListException;
import com.jsmsframework.finance.service.JsmsAgentAccountService;
import com.jsmsframework.finance.service.JsmsAgentInvoiceConfigService;
import com.jsmsframework.finance.service.JsmsAgentInvoiceListService;
import com.jsmsframework.user.entity.JsmsAgentInfo;
import com.jsmsframework.user.finance.service.JsmsUserFinanceService;
import com.jsmsframework.user.service.JsmsAgentInfoService;
import com.ucpaas.sms.common.util.Collections3;
import com.ucpaas.sms.common.util.DateUtils;
import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.entity.po.JsmsAgentInvoiceListPo;
import com.ucpaas.sms.util.AgentUtils;
import com.ucpaas.sms.util.enums.StaticInitVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.*;

@Service
public class AgentInvoiceServiceImpl implements AgentInvoiceService {
    private static Logger logger = LoggerFactory.getLogger(AgentInvoiceServiceImpl.class);

    @Autowired
    private JsmsAgentInvoiceListService jsmsAgentInvoiceListService;

    @Autowired
    private JsmsAgentInvoiceConfigService jsmsAgentInvoiceConfigService;

    @Autowired
    private JsmsUserFinanceService jsmsUserFinanceService;

    @Autowired
    private JsmsAgentInfoService jsmsAgentInfoService;

    @Autowired
    private JsmsAgentAccountService jsmsAgentAccountService;

    // 组装orderID
    public static synchronized String getInvoiceId() {
        Date date = new Date();
        int num = 0;
        String idPre = "I"+ DateUtils.formatDate(date, "yyyyMMdd") + "000"+ WebIdNew.运营平台.getValue();
        if (idPre.equals(StaticInitVariable.AGENT_INVOICE_ID_PRE)) {
            num = StaticInitVariable.AGENT_INVOICE_ID_NUM;
            StaticInitVariable.AGENT_INVOICE_ID_NUM = num + 1;
        } else {
            StaticInitVariable.AGENT_INVOICE_ID_PRE = idPre;
            num = 1;
            StaticInitVariable.AGENT_INVOICE_ID_NUM = num + 1;
        }

        // 拼成订单号
        String idStr = idPre + StringUtils.addZeroForNum(num, 4, "0");
        logger.debug("生成发票id:==========" + idStr);
        return idStr;
    }

    @Override
    public List<JsmsAgentInfo> getAgentList(Long userId) {
        // 根据权限 设置
        Map<String, Object> params = new HashMap<>();
        params.put("agentType", AgentType.OEM代理商.getValue().toString());
        params.put("oauthStatus", OauthStatusEnum.证件已认证.getValue());
        params.put("dataAuthorityCondition", AgentUtils.getDataAuthorityCondition(userId, true, false));
        return jsmsAgentInfoService.findListByRight(params);
    }

    @Override
    public List<JsmsAgentInvoiceList> findInvoiceList4ReturnInvoice(Map<String, Object> params) {
        return jsmsAgentInvoiceListService.findList(params);
    }

    @Override
    public BigDecimal getCanInvoiceAmount(Integer agentId) {
        return jsmsUserFinanceService.getCanInvoiceAmount(agentId);
    }

    @Override
    public BigDecimal getCanBackAmount(Integer agentId) {
        return jsmsUserFinanceService.getCanBackAmount(agentId);
    }

    @Override
    public BigDecimal getHasTakeInvoiceInitAmount(Integer agentId) {
        JsmsAgentInfo agentInfo = jsmsAgentInfoService.getByAgentId(agentId);
        if (agentInfo == null)
        {
            return BigDecimal.ZERO;
        }

        JsmsAgentAccount account = jsmsAgentAccountService.getByAgentId(agentId);
        if (account == null)
        {
            return BigDecimal.ZERO;
        }

        return account.getHasTakeInvoiceInit();
    }

    @Transactional("message")
    @Override
    public R applicationInvoice(JsmsAgentInvoiceList invoiceList) {
        invoiceList.setInvoiceId(getInvoiceId());
        invoiceList.setWebId(WebIdNew.运营平台.getValue());
        return jsmsUserFinanceService.applicationInvoice(invoiceList);
    }

    @Transactional("message")
    @Override
    public R returnInvoice(JsmsAgentInvoiceListPo invoiceList) {
        Assert.notNull(invoiceList.isOpenInvoice(), "是否已开票不能为空");

        if (invoiceList.getAgentId() == null)
        {
            return R.error(Code.SYS_ERR, "客户ID不能为空");
        }

        JsmsAgentInfo agentInfo = jsmsAgentInfoService.getByAgentId(invoiceList.getAgentId());
        if (agentInfo == null){
            return R.error(Code.SYS_ERR, "客户不存在");
        }

        if (!agentInfo.getAgentType().equals(AgentType.OEM代理商.getValue())){
            return R.error(Code.SYS_ERR, "客户不支持返还发票");
        }

        invoiceList.setWebId(WebIdNew.运营平台.getValue());

        // 已开票
        if(invoiceList.isOpenInvoice())
        {
            if (Collections3.isEmpty(invoiceList.getReturnInvoiceIds()))
            {
                return R.error(Code.SYS_ERR, "已选发票列表不能为空");
            }

            // 查询出 已选发票列表
            // 循环处理
            JsmsAgentInvoiceList invoice;
            JsmsAgentInvoiceList waitUpdate;
            int count = 0;
            BigDecimal amount = BigDecimal.ZERO;
            for (String id : invoiceList.getReturnInvoiceIds()) {
                invoice = jsmsAgentInvoiceListService.getByInvoiceId(id);
                Assert.notNull(invoice, "已选发票不存在：" + id);
                Assert.notNull(invoice.getStatus(), "已选发票状态为空：" + id);

                if (!invoice.getSource().equals(InvoiceSourceEnum.发票申请.getValue()))
                {
                    throw new JsmsAgentInvoiceListException("已选发票来源不支持");
                }

                if (!invoice.getStatus().equals(InvoiceStatusEnum.已邮寄.getValue()))
                {
                    throw new JsmsAgentInvoiceListException("当前状态已更改，不能返还");
                }

                // 更新状态为已返还
                waitUpdate = new JsmsAgentInvoiceList();
                waitUpdate.setStatus(InvoiceStatusEnum.已返还.getValue());
                waitUpdate.setInvoiceId(id);
                waitUpdate.setOperator(invoice.getOperator());
                waitUpdate.setUpdateTime(Calendar.getInstance().getTime());
                count = jsmsAgentInvoiceListService.updateStatus(waitUpdate, invoice);
                if (count <= 0)
                {
                    throw new JsmsAgentInvoiceListException("当前状态已更改，不能返还");
                }

                amount = amount.add(invoice.getInvoiceAmount());
            }

            count = jsmsAgentAccountService.reduceHasTakeInvoice(agentInfo.getAgentId(), amount, false);
            if (count <= 0)
            {
                throw new JsmsAgentInvoiceListException("更新代理商已开票金额失败");
            }
        }
        // 未开票
        else {
            // 发票类型
            if (invoiceList.getInvoiceType() == null
                    || StringUtils.isBlank(InvoiceTypeEnum.getDescByValue(invoiceList.getInvoiceType()))) {
                return R.error(Code.SYS_ERR, "必须是" + InvoiceTypeEnum.普通发票.getDesc() + "或者" + InvoiceTypeEnum.增值税专票.getDesc());
            }

            // 发票主体
            if (invoiceList.getInvoiceBody() == null
                    || StringUtils.isBlank(InvoiceBodyEnum.getDescByValue(invoiceList.getInvoiceBody()))) {
                return R.error(Code.SYS_ERR, "发票主体必须是" + InvoiceBodyEnum.个人.getDesc() + "或者" + InvoiceBodyEnum.企业.getDesc());
            }

            // 发票抬头
            if (StringUtils.isBlank(invoiceList.getInvoiceHead()) || invoiceList.getInvoiceHead().length() > 100) {
                return R.error(Code.SYS_ERR, "发票抬头长度必须介于 1 和 100 之间");
            }

            // 返还金额
            if (invoiceList.getInvoiceAmount() == null || invoiceList.getInvoiceAmount().compareTo(BigDecimal.ZERO) <= 0)
            {
                return R.error(Code.SYS_ERR, "发票金额必须大于0");
            }

            JsmsAgentAccount agentAccount = jsmsAgentAccountService.getByAgentId(invoiceList.getAgentId());
            Assert.notNull(agentAccount, "客户不存在");

            // 返还金额
            if (invoiceList.getInvoiceAmount().compareTo(agentAccount.getHasTakeInvoiceInit()) > 0)
            {
                return R.error(Code.SYS_ERR, "可返还金额已更改，不能返还");
            }

            JsmsAgentInvoiceList invoice = new JsmsAgentInvoiceList();
            invoice.setAgentId(invoiceList.getAgentId());
            invoice.setInvoiceId(getInvoiceId());
            invoice.setInvoiceType(invoiceList.getInvoiceType());
            invoice.setInvoiceBody(invoiceList.getInvoiceBody());
            invoice.setInvoiceHead(invoiceList.getInvoiceHead());
            invoice.setInvoiceAmount(invoiceList.getInvoiceAmount());
            invoice.setSource(InvoiceSourceEnum.未开票客户返还发票.getValue());
            invoice.setStatus(InvoiceStatusEnum.已返还.getValue());
            invoice.setOperator(invoiceList.getOperator());
            invoice.setApplicant(invoiceList.getOperator());
            invoice.setCreateTime(Calendar.getInstance().getTime());
            invoice.setUpdateTime(invoice.getCreateTime());
            invoice.setWebId(WebIdNew.运营平台.getValue());
            int count = jsmsAgentInvoiceListService.insert(invoice);
            if (count <= 0)
            {
                throw new JsmsAgentInvoiceListException("添加发票申请记录失败");
            }

            count = jsmsAgentAccountService.reduceHasTakeInvoice(agentInfo.getAgentId(), invoiceList.getInvoiceAmount(), true);
            if (count <= 0)
            {
                throw new JsmsAgentInvoiceListException("更新代理商已开票金额失败");
            }
        }

        return R.ok("返还发票成功");
    }

    @Override
    public R getInvoiceConfig(Integer agentId, Integer invoiceType) {
        if (agentId == null)
        {
            return R.error(Code.SYS_ERR, "客户ID不能为空");
        }

        if (invoiceType == null)
        {
            return R.error(Code.SYS_ERR, "发票类型不能为空");
        }

        // 发票类型
        if (invoiceType == null
                || StringUtils.isBlank(InvoiceTypeEnum.getDescByValue(invoiceType))) {
            return R.error(Code.SYS_ERR, "必须是" + InvoiceTypeEnum.普通发票.getDesc() + "或者" + InvoiceTypeEnum.增值税专票.getDesc());
        }

        if(InvoiceTypeEnum.普通发票.getValue() == invoiceType)
        {
            return R.ok("获取普票配置信息成功", jsmsUserFinanceService.findListNomal(agentId));
        }

        if(InvoiceTypeEnum.增值税专票.getValue() == invoiceType)
        {
            return R.ok("获取增票配置信息成功", jsmsUserFinanceService.findListAdd(agentId));
        }

        return R.ok("未获取到信息", null);
    }
}
