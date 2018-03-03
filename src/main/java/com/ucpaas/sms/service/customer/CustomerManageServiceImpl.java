package com.ucpaas.sms.service.customer;

import com.jsmsframework.common.enums.OauthStatusEnum;
import com.ucpaas.sms.common.entity.R;
import com.jsmsframework.common.enums.ProductType;
import com.jsmsframework.common.util.JsonUtil;
import com.jsmsframework.finance.entity.JsmsOemAgentAccountStatistics;
import com.jsmsframework.finance.service.JsmsOemAgentAccountStatisticsService;
import com.jsmsframework.order.entity.JsmsOemAgentOrder;
import com.jsmsframework.order.entity.JsmsOemAgentPool;
import com.jsmsframework.order.entity.JsmsOemClientOrder;
import com.jsmsframework.order.entity.JsmsOemClientPool;
import com.jsmsframework.order.entity.po.JsmsOemAgentPoolPo;
import com.jsmsframework.order.enums.OEMAgentOrderType;
import com.jsmsframework.order.enums.OrderType;
import com.jsmsframework.order.exception.JsmsOemAgentOrderException;
import com.jsmsframework.order.exception.JsmsOemAgentPoolException;
import com.ucpaas.sms.entity.message.AgentInfo;
import com.ucpaas.sms.util.AgentUtils;
import com.ucpaas.sms.util.enums.StaticInitVariable;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.common.util.DateUtils;
import com.jsmsframework.order.service.JsmsOemAgentOrderService;
import com.jsmsframework.order.service.JsmsOemAgentPoolService;
import com.jsmsframework.order.service.JsmsOemClientOrderService;
import com.jsmsframework.order.service.JsmsOemClientPoolService;
import com.jsmsframework.user.entity.JsmsAccount;
import com.jsmsframework.user.service.JsmsAccountService;
import com.ucpaas.sms.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 短信充值
 *
 * @outhor tanjiangqiang
 * @create 2017-11-14 20:57
 */
@Service
@Transactional
public class CustomerManageServiceImpl implements CustomerManageService {
    private static final Logger logger = LoggerFactory.getLogger(CustomerManageServiceImpl.class);

//    @Autowired
//    private MessageMasterDao masterDao;

    @Autowired
    private JsmsOemAgentPoolService jsmsOemAgentPoolService;

    @Autowired
    private JsmsOemAgentOrderService jsmsOemAgentOrderService;

    @Autowired
    private JsmsAccountService jsmsAccountService;


    @Autowired
    private JsmsOemClientPoolService jsmsOemClientPoolService;

    @Autowired
    private JsmsOemClientOrderService jsmsOemClientOrderService;

    @Autowired
    private JsmsOemAgentAccountStatisticsService jsmsOemAgentAccountStatisticsService;

    /**
     * @param
     * @return
     * @Title: queryInterSmsInfo
     * @Description: 查询代理商短信信息
     * @return: List<Map<String,Object>>
     */
//    @Override
//    public List<Map<String, Object>> querySmsInfo(JsmsOemAgentPool jsmsOemAgentPool) {
//        return this.jsmsOemAgentPoolService.querySmsInfo(jsmsOemAgentPool);
//    }
    @Override
    @Transactional
    public R oemClientRecharge(List<JsmsOemAgentPoolPo> poolPos, String clientId) {

        logger.debug("【OEM代理商客户充值】充值开始----------------------------");
        for (JsmsOemAgentPoolPo poolPo : poolPos) {

            AgentInfo agentInfo = AgentUtils.queryAgentInfoByAgentId(poolPo.getAgentId().toString());
            if (null == agentInfo) {
                return R.error("未查询到代理商相关信息");
            }
            if (!OauthStatusEnum.证件已认证.getValue().equals(agentInfo.getOauthStatus())){
                return R.error("代理商还未认证！");
            }

            // 确定OEM代理商的短信池
            JsmsOemAgentPool jsmsOemAgentPool = new JsmsOemAgentPool();
            jsmsOemAgentPool.setAgentId(poolPo.getAgentId());
            jsmsOemAgentPool.setProductType(poolPo.getProductType());
            jsmsOemAgentPool.setOperatorCode(poolPo.getOperatorCode());
            jsmsOemAgentPool.setUnitPrice(poolPo.getUnitPrice());
            jsmsOemAgentPool.setDueTime(poolPo.getDueTime());
            jsmsOemAgentPool.setStatus(poolPo.getStatus());
            jsmsOemAgentPool.setAreaCode(poolPo.getAreaCode());
            List<JsmsOemAgentPool> jsmsOemAgentPoolList =  jsmsOemAgentPoolService.getListByAgentPoolInfo(jsmsOemAgentPool);

            // 检查充值的数量是否充足
            R r = oemClientRecharegePreCheck(poolPo, jsmsOemAgentPoolList);
            if (!"0".equals(r.getCode().toString())) {
                logger.debug("【OEM代理商客户充值】余额校验不通过，充值结束----------------------------");
                return R.error(r.getMsg());
            }

            poolPo.setUpdateTime(new Date());
            // 存在多个OEM代理商短信池的时候取最后创建的那个短信池
            Long agentPoolId = jsmsOemAgentPoolList.get(jsmsOemAgentPoolList.size() - 1).getAgentPoolId();
            // 扣减OEM代理商的短信池
            this.updateForReduceAgentPoolRemainNum(poolPo, jsmsOemAgentPoolList);

            // 创建OEM代理商分发订单
            this.createOemAgentOrder(poolPo, clientId, agentPoolId, OEMAgentOrderType.OEM代理商分发.getValue());

            // 确定要充值的OEM客户短信池，不存在则创建
            List<JsmsOemClientPool> jsmsOemClientPoolList = this.confirmOemClientPool(clientId, 0, poolPo.getProductType(), poolPo.getOperatorCode(),
                    poolPo.getAreaCode(), poolPo.getDueTime(), poolPo.getUnitPrice());

            Long clientPoolId = jsmsOemClientPoolList.get(jsmsOemClientPoolList.size() - 1).getClientPoolId();
            // OEM客户短信池充值操作
            this.updateForAddClientPoolRemainNum(clientPoolId, poolPo);

            // 创建OEM客户购买订单
            this.createOemClientOrder(clientPoolId, poolPo, clientId, OrderType.充值.getValue());

            // 更新OEM代理商累计购买条数统计表
            this.updateOemAgentAccountStatistics(poolPo.getAgentId(), new BigDecimal(poolPo.getUpdateNum()), poolPo.getProductType());
        }
        logger.debug("【OEM代理商客户充值】充值成功，结束----------------------------");

        return R.ok("充值成功!");
    }

    /**
     * 更新 t_sms_oem_agent_account_statistics 表的累计卖出数量
     *
     * @param agentId
     * @param purchaseNum
     * @param productType
     * @return
     */
    private JsmsOemAgentAccountStatistics updateOemAgentAccountStatistics(Integer agentId, BigDecimal purchaseNum, Integer productType) {

        JsmsOemAgentAccountStatistics insertOrUpdateObj = new JsmsOemAgentAccountStatistics();
        insertOrUpdateObj.setAgentId(agentId);

        if (ProductType.行业.getValue().equals(productType)) {
            insertOrUpdateObj.setHyRemainRebateNumber(purchaseNum.intValue());
        } else if (ProductType.营销.getValue().equals(productType)) {
            insertOrUpdateObj.setYxRemainRebateNumber(purchaseNum.intValue());
        } else if (ProductType.国际.getValue().equals(productType)) {
            insertOrUpdateObj.setGjRemainRebateAmount(purchaseNum);
        } else if (ProductType.验证码.getValue().equals(productType)) {
            insertOrUpdateObj.setYzmRemainRebateNumber(purchaseNum.intValue());
        } else if (ProductType.通知.getValue().equals(productType)) {
            insertOrUpdateObj.setTzRemainRebateNumber(purchaseNum.intValue());
        }

        JsmsOemAgentAccountStatistics oemAgentAccountStatistics = jsmsOemAgentAccountStatisticsService.getByAgentId(agentId);

        if (oemAgentAccountStatistics == null) {
            int insert = jsmsOemAgentAccountStatisticsService.insert(insertOrUpdateObj);
            logger.debug("【OEM代理商客户充值】增加OEM代理商帐户统计表 t_sms_oem_agent_account_statistics.insert = {} , insertObj --> {}", insert, JsonUtil.toJson(insertOrUpdateObj));
            return insertOrUpdateObj;
        } else {
            int update = jsmsOemAgentAccountStatisticsService.updateForAddPurchaseNumber(insertOrUpdateObj);
            logger.debug("【OEM代理商客户充值】更新OEM代理商帐户统计表 t_sms_oem_agent_account_statistics.update = {} , updateObj --> {}", update, JsonUtil.toJson(insertOrUpdateObj));
            return insertOrUpdateObj;
        }
    }


    private void createOemClientOrder(Long clientPoolId, JsmsOemAgentPoolPo poolPos, String clientId, Integer orderType) {

        BigDecimal updateNum = new BigDecimal(poolPos.getUpdateNum());

        JsmsOemClientOrder jsmsOemClientOrder = new JsmsOemClientOrder();

        Long oemClientOrderId = this.getClientOrderId();
        jsmsOemClientOrder.setOrderId(oemClientOrderId);
        if (OrderType.充值.getValue().equals(orderType)) {
            jsmsOemClientOrder.setOrderType(OrderType.充值.getValue());
        } else {
            jsmsOemClientOrder.setOrderType(OrderType.回退.getValue());
        }
        jsmsOemClientOrder.setOrderNo(oemClientOrderId);
        jsmsOemClientOrder.setClientPoolId(clientPoolId);
        jsmsOemClientOrder.setProductType(poolPos.getProductType());
        jsmsOemClientOrder.setClientId(clientId);
        jsmsOemClientOrder.setAgentId(poolPos.getAgentId());
        jsmsOemClientOrder.setDueTime(poolPos.getDueTime());
        jsmsOemClientOrder.setOperatorCode(poolPos.getOperatorCode());
        jsmsOemClientOrder.setAreaCode(poolPos.getAreaCode());
        jsmsOemClientOrder.setCreateTime(poolPos.getUpdateTime());
        jsmsOemClientOrder.setRemark(null);

        if (ProductType.国际.getValue().equals(poolPos.getProductType())) {
            jsmsOemClientOrder.setOrderNumber(null);
            jsmsOemClientOrder.setOrderPrice(updateNum);
        } else {
            jsmsOemClientOrder.setOrderNumber(updateNum.intValue());
            if (poolPos.getUnitPrice() == null) {
                jsmsOemClientOrder.setUnitPrice(BigDecimal.ZERO);
            } else {
                jsmsOemClientOrder.setUnitPrice(poolPos.getUnitPrice());
            }
        }

        this.jsmsOemClientOrderService.insert(jsmsOemClientOrder);

        logger.debug("【OEM代理商客户充值】:生成OEM客户订单{}成功，订单类型为{}", oemClientOrderId, OrderType.getDescByValue(orderType));
    }


    private void updateForAddClientPoolRemainNum(Long clientPoolId, JsmsOemAgentPoolPo poolPos) {
        BigDecimal updateNum = new BigDecimal(poolPos.getUpdateNum());
        jsmsOemClientPoolService.updateForAddClientPoolRemainNum(clientPoolId, updateNum, poolPos.getProductType(), poolPos.getUpdateTime());
        logger.debug("【OEM代理商客户充值】:充值OEM客户短信池{}，充值{}条数（金额），产品类型为{}", clientPoolId, updateNum, ProductType.getDescByValue(poolPos.getProductType()));
    }

    private List<JsmsOemClientPool> confirmOemClientPool(String clientId, Integer status, Integer productType, Integer operatorCode, Integer areaCode,
                                                         Date dueTime, BigDecimal unitPrice) {

        JsmsOemClientPool poolInfo = new JsmsOemClientPool();
        poolInfo.setClientId(clientId);
        poolInfo.setProductType(productType);
        poolInfo.setDueTime(dueTime);
        poolInfo.setOperatorCode(operatorCode);
        poolInfo.setAreaCode(areaCode);
        poolInfo.setUnitPrice(unitPrice);
        poolInfo.setStatus(0);
        List<JsmsOemClientPool> jsmsOemClientPoolList = jsmsOemClientPoolService.getListByClientPoolInfo(poolInfo);

        // 如果OEM客户的短信池存在直接返回；不存在则创建一个
        if (jsmsOemClientPoolList != null && jsmsOemClientPoolList.size() > 0) {
            return jsmsOemClientPoolList;
        } else {
            if (ProductType.国际.getValue().equals(productType)) {
                poolInfo.setTotalNumber(null);
                poolInfo.setRemainNumber(null);
                poolInfo.setTotalAmount(BigDecimal.ZERO);
                poolInfo.setRemainAmount(BigDecimal.ZERO);
            } else {
                poolInfo.setTotalNumber(0);
                poolInfo.setRemainNumber(0);
                poolInfo.setTotalAmount(null);
                poolInfo.setRemainAmount(null);
            }
            poolInfo.setUpdateTime(dueTime);
            poolInfo.setRemark(null);

            jsmsOemClientPoolService.insert(poolInfo);
            List<JsmsOemClientPool> list = new ArrayList<>();
            list.add(jsmsOemClientPoolService.getByClientPoolId(poolInfo.getClientPoolId()));
            return list;
        }

    }

    // 组装orderID
    public static synchronized Long getClientOrderId() {

        Date date = new Date();
        int num = 0;
        String orderIdPre = DateUtils.formatDate(date, "yyMMdd") + DateUtils.formatDate(date, "HHmm")
                + ConfigUtils.platform_oem_agent_order_identify;

        if (orderIdPre.equals(StaticInitVariable.OEM_CLIENT_ORDERID_PRE)) {
            num = StaticInitVariable.OEM_CLIENT_ORDER_NUM;
            StaticInitVariable.OEM_CLIENT_ORDER_NUM = num + 1;
        } else {
            StaticInitVariable.OEM_CLIENT_ORDERID_PRE = orderIdPre;
            num = 1;
            StaticInitVariable.OEM_CLIENT_ORDER_NUM = num + 1;
        }

        // 拼成订单号
        String orderIdStr = orderIdPre + StringUtils.addZeroForNum(num, 4, "0");
        Long orderId = Long.valueOf(orderIdStr);

        System.out.println("生成的客户的订单id:==========" + orderId);
        logger.debug("生成的客户的订单id:==========" + orderId);

        return orderId;
    }

    /**
     * 创建OEM代理商订单
     *
     * @param poolPos
     * @param clientId
     * @param agentPoolId
     * @param oemAgentOrderType
     */
    private void createOemAgentOrder(JsmsOemAgentPoolPo poolPos, String clientId, Long agentPoolId, Integer oemAgentOrderType) {

        BigDecimal updateNum = new BigDecimal(poolPos.getUpdateNum());
        // 生成订单号
        Long oemAgentOrderId = this.getClientOrderId();

        // 生成订单信息
        JsmsOemAgentOrder oemAgentPurchaseOrder = new JsmsOemAgentOrder();
        oemAgentPurchaseOrder.setOrderId(oemAgentOrderId);
        if (OEMAgentOrderType.OEM代理商分发.getValue().equals(oemAgentOrderType)) {
            oemAgentPurchaseOrder.setOrderType(OEMAgentOrderType.OEM代理商分发.getValue());
        } else if (OEMAgentOrderType.OEM代理商回退.getValue().equals(oemAgentOrderType)) {
            oemAgentPurchaseOrder.setOrderType(OEMAgentOrderType.OEM代理商回退.getValue());
        } else {
            throw new JsmsOemAgentOrderException("当前方法只能创建‘OEM代理商分发’或者‘OEM代理商回退’订单");
        }
        oemAgentPurchaseOrder.setProductId(null); // 订单类型为0时有值
        oemAgentPurchaseOrder.setProductCode(null);
        oemAgentPurchaseOrder.setProductName(null);
        oemAgentPurchaseOrder.setUnitPrice(null);
        oemAgentPurchaseOrder.setOrderNo(oemAgentOrderId);
        oemAgentPurchaseOrder.setAgentPoolId(agentPoolId);
        oemAgentPurchaseOrder.setCreateTime(poolPos.getUpdateTime());
        oemAgentPurchaseOrder.setRemark(null);
        JsmsAccount jsmsAccount = jsmsAccountService.getByClientId(clientId);
        oemAgentPurchaseOrder.setName(jsmsAccount.getName());

        // 将页面信息放到生成的订单中
        oemAgentPurchaseOrder.setProductType(poolPos.getProductType());
        oemAgentPurchaseOrder.setOperatorCode(poolPos.getOperatorCode());
        oemAgentPurchaseOrder.setAreaCode(poolPos.getAreaCode());
        oemAgentPurchaseOrder.setAgentId(poolPos.getAgentId());
        oemAgentPurchaseOrder.setClientId(clientId);
        oemAgentPurchaseOrder.setDueTime(poolPos.getDueTime());

        if (ProductType.国际.getValue().equals(poolPos.getProductType())) {
            oemAgentPurchaseOrder.setOrderNumber(null);
            oemAgentPurchaseOrder.setOrderAmount(updateNum);
            oemAgentPurchaseOrder.setProductPrice(updateNum);
        } else {
            oemAgentPurchaseOrder.setOrderNumber(updateNum.intValue());
            oemAgentPurchaseOrder.setProductPrice(null);
            if (poolPos.getUnitPrice() != null) {
                oemAgentPurchaseOrder.setUnitPrice(poolPos.getUnitPrice());
            } else {
                oemAgentPurchaseOrder.setUnitPrice(BigDecimal.ZERO);
            }
            // 算出订单金额
            oemAgentPurchaseOrder.setOrderAmount(updateNum.multiply(poolPos.getUnitPrice()));
        }

        this.jsmsOemAgentOrderService.insert(oemAgentPurchaseOrder);


        logger.debug("【OEM代理商客户充值】:生成OEM代理订单{}成功，订单类型={}", oemAgentOrderId, OEMAgentOrderType.getDescByValue(oemAgentOrderType));
    }

    /**
     * 根据充值信息确认要操作的OEM代理商短信池
     *
     * @param agentId
     * @param status
     * @param productType
     * @param operatorCode
     * @param areaCode
     * @param dueTime
     * @param unitPrice
     * @return
     */
    private List<JsmsOemAgentPool> confirmOemAgentPool(Integer agentId, Integer status, Integer
            productType, Integer operatorCode, Integer areaCode,
                                                       Date dueTime, BigDecimal unitPrice) {
        JsmsOemAgentPool poolInfo = new JsmsOemAgentPool();
        poolInfo.setAgentId(agentId);
        poolInfo.setStatus(status);
        poolInfo.setProductType(productType);
        poolInfo.setOperatorCode(operatorCode);
        poolInfo.setAreaCode(areaCode);
        poolInfo.setDueTime(dueTime);
        poolInfo.setUnitPrice(unitPrice);

        return jsmsOemAgentPoolService.getListByAgentPoolInfo(poolInfo);
    }

    private R oemClientRecharegePreCheck(JsmsOemAgentPoolPo poolPos, List<JsmsOemAgentPool> jsmsOemAgentPoolList) {
        BigDecimal remainNum = BigDecimal.ZERO;
        Integer productType = poolPos.getProductType();
        if (null == poolPos.getUpdateNum() || poolPos.getUpdateNum() <= 0) {
            logger.error("充值数量为空-----{}"+JsonUtil.toJson(poolPos));
            return R.error("请选择充值数量");
        }
        for (JsmsOemAgentPool oemAgentPool : jsmsOemAgentPoolList) {
            if (ProductType.国际.getValue().equals(productType)) {
                // 国际产品
                remainNum = remainNum.add(oemAgentPool.getRemainAmount());
            } else {
                // 普通产品
                remainNum = remainNum.add(new BigDecimal(oemAgentPool.getRemainNumber()));
            }
        }
        if (new BigDecimal(poolPos.getUpdateNum()).compareTo(remainNum) == 1) {
            logger.error("充值数量为空-----{}"+JsonUtil.toJson(poolPos));
            return R.error("您充值的数量大于剩余的数量，请重新充值!");
        }
        return R.ok("检验通过");
    }

    private void updateForReduceAgentPoolRemainNum(JsmsOemAgentPoolPo poolPos, List<JsmsOemAgentPool> jsmsOemAgentPoolList) {
        BigDecimal updateNum = new BigDecimal(poolPos.getUpdateNum());
        for (JsmsOemAgentPool jsmsOemAgentPool : jsmsOemAgentPoolList) {
            Long agentPoolId = jsmsOemAgentPool.getAgentPoolId();
            BigDecimal reduceNum = jsmsOemAgentPoolService.updateForReduceAgentPoolRemainNum(agentPoolId, updateNum, poolPos.getProductType(), poolPos.getUpdateTime());

            updateNum = updateNum.subtract(reduceNum);
            logger.debug("【OEM代理商客户充值】:扣减OEM代理商短信池{}，扣减{}条数（金额），产品类型为{}", agentPoolId, reduceNum, ProductType.getDescByValue(poolPos.getProductType()));
            // 一个池没有扣完继续扣减下一个
            if (updateNum.compareTo(BigDecimal.ZERO) != 0) {
                logger.debug("【OEM代理商客户充值】扣减一个短信池后未扣完需要扣减的条数，继续扣减下一个短信池");
                continue;
            } else {
                break;
            }

        }

        if (updateNum.compareTo(BigDecimal.ZERO) != 0) {
            logger.warn("【OEM代理商客户充值】扣减OEM代理商短信池数量时剩余数量不够扣，执行回滚");
            throw new JsmsOemAgentPoolException("您充值的数量大于剩余的数量，请重新充值!");
        }

    }
}