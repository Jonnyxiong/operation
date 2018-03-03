package com.ucpaas.sms.util;

import org.apache.commons.lang3.StringUtils;

public class ConverUtil {
    public ConverUtil() {
    }

    public static String agentTypeToName(String agentType) {
        return StringUtils.isBlank(agentType) ? "" : agentTypeToName(Integer.parseInt(agentType));
    }

    public static String agentTypeToName(int agentType) {
        switch (agentType) {
            case 1:
                return "销售代理商";
            case 2:
                return "品牌代理商";
            case 3:
                return "资源合作商";
            case 4:
                return "代理商和资源合作";
            case 5:
                return "OEM代理商";
            default:
                return "";
        }
    }

    public static String financialTypeToName(String financialType) {
        return StringUtils.isBlank(financialType) ? "" : financialTypeToName(Integer.parseInt(financialType));
    }

    public static String financialTypeToName(int financialType) {
        switch (financialType) {
            case 0:
                return "入账";
            case 1:
                return "出账";
            default:
                return "";
        }
    }

    public static String orderStatusToName(String orderStatus) {
        return StringUtils.isBlank(orderStatus) ? "" : orderStatusToName(Integer.parseInt(orderStatus));
    }

    public static String orderStatusToName(int orderStatus) {
        switch (orderStatus) {
            case 0:
                return "待审核";
            case 1:
                return "订单生效";
            case 2:
                return "订单完成";
            case 3:
                return "订单失败";
            case 4:
                return "订单取消";
            default:
                return "";
        }
    }

    public static String productTypeToName(String productType) {
        return StringUtils.isBlank(productType) ? "" : productTypeToName(Integer.parseInt(productType));
    }

    public static String productTypeToName(int productType) {
        switch (productType) {
            case 0:
                return "行业";
            case 1:
                return "会员营销";
            case 2:
                return "国际短信";
            case 3:
                return "验证码";
            case 4:
                return "通知";
            default:
                return "";
        }
    }

    public static String oemOrderTypeToName(String productType) {
        return oemOrderTypeToName(Integer.parseInt(productType));
    }

    public static String oemOrderTypeToName(int productType) {
        switch (productType) {
            case 0:
                return "客户购买";
            case 1:
                return "子账户充值";
            case 2:
                return "子账户回退";
            case 3:
                return "回退条数";
            default:
                return "";
        }
    }

    public static String paytypeToName(String paytype) {
        return StringUtils.isBlank(paytype) ? "" : paytypeToName(Integer.parseInt(paytype));
    }

    public static String paytypeToName(int paytype) {
        switch (paytype) {
            case 0:
                return "预付";
            case 1:
                return "后付";
            default:
                return "";
        }
    }

    public static String businessTypeToName(String businessType) {
        return StringUtils.isBlank(businessType) ? "" : businessTypeToName(Integer.parseInt(businessType));
    }

    public static String businessTypeToName(int businessType) {
        switch (businessType) {
            case 0:
                return "充值";
            case 1:
                return "扣减";
            case 2:
                return "佣金转余额";
            case 3:
                return "购买产品包";
            case 4:
                return "退款";
            case 5:
                return "赠送";
            case 6:
                return "后付费客户消耗";
            case 7:
                return "回退条数";
            default:
                return "";
        }
    }
}
