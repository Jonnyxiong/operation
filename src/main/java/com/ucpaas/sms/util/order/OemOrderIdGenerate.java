package com.ucpaas.sms.util.order;

import com.jsmsframework.common.util.StringUtils;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.enums.StaticInitVariable;
import com.ucpaas.sms.util.rest.utils.DateUtil;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by dylan on 2017/10/16.
 */
public class OemOrderIdGenerate {
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(OemOrderIdGenerate.class);

    //组装表t_sms_oem_client_order的orderID
    public static synchronized Long getOemClientOrderId(){

//        Date date = new Date();
        int num = 0;
//        String orderIdPre = DateUtil.dateToStr(date,"yyMMdd") + DateUtil.dateToStr(date, "HHmm")
        String orderIdPre = DateUtil.dateToStr(new Date(),"yyMMddHHmm")
                + ConfigUtils.platform_oem_agent_order_identify;// oem代理商订单标识3
//                + "3";// oem代理商订单标识3

//        if(orderIdPre.equals(StaticInitVariable.OEM_CLIENT_ORDERID_PRE)){
        if(orderIdPre.equals(StaticInitVariable.OEM_CLIENT_ORDERID_PRE)){
            num = StaticInitVariable.OEM_CLIENT_ORDER_NUM;
            StaticInitVariable.OEM_CLIENT_ORDER_NUM = num + 1;
        }else{
            StaticInitVariable.OEM_CLIENT_ORDERID_PRE = orderIdPre;
            num = 1;
            StaticInitVariable.OEM_CLIENT_ORDER_NUM = num + 1;
        }

        //拼成订单号
        String orderIdStr = orderIdPre + StringUtils.addZeroForNum(num, 4, "0");
        Long orderId = Long.valueOf(orderIdStr);

        logger.debug("生成的客户的订单id:=========="+orderId);

        return orderId;
    }

    public static void main(String[] args) {
        for (int i = 0 ; i<10 ;i++) {
            System.out.println(OemOrderIdGenerate.getOemClientOrderId());

        }
    }
}
