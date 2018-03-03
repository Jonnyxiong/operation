package com.ucpaas.sms.util.order;

import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.StrUtils;
import com.ucpaas.sms.util.enums.StaticInitVariable;
import com.ucpaas.sms.util.rest.utils.DateUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;

/**
 * Created by dylan on 2017/9/5.
 */
public enum OrderIdGenerate {

    GENERATE;

    public synchronized Long getOrderId(){

        Date date = new Date();
        int num = 0;
        String identify = ConfigUtils.platform_order_identify;
        String orderIdPre = DateUtil.dateToStr(date,"yyMMdd") + DateUtil.dateToStr(date, "HHmm") + (StringUtils.isBlank(identify) ? 3 : identify);// 00 , platform_order_identify区分各个平台(客户与OEM) ;
        if(orderIdPre.equals(StaticInitVariable.ORDERID_PRE)){
            num = StaticInitVariable.ORDER_NUM;
            StaticInitVariable.ORDER_NUM = num + 1;
        }else{
            StaticInitVariable.ORDERID_PRE = orderIdPre;
            num = 1;
            StaticInitVariable.ORDER_NUM = num + 1;
        }

        //拼成订单号
        String orderIdStr = orderIdPre + StrUtils.addZeroForNum(num, 4, "0");
        Long orderId = Long.valueOf(orderIdStr);

        return orderId;

    }

}
