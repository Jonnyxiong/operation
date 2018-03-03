package com.ucpaas.sms.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;


/**
 * Created by dylan on 2017/7/26.
 */
public class ByZeroUtil {
    private static final Logger logger = LoggerFactory.getLogger(ByZeroUtil.class);


    public static BigDecimal divideExcludeZero(BigDecimal divisor) {

        return BigDecimal.ZERO.compareTo(divisor) == 0 ? BigDecimal.ONE : divisor;
    }

    public static boolean isDivisorZero(BigDecimal divisor) {
        if (divisor == null){
            return true;
        }
        return BigDecimal.ZERO.compareTo(divisor) == 0 ? true : false;
    }

    public static BigDecimal divideExcludeZero(String divisor) {
        if (StringUtils.isEmpty(divisor) || !divisor.matches("^\\d+$") || divisor.matches("^0+$")) {
            return BigDecimal.ONE;
        }
        return new BigDecimal(divisor);
    }

    public static boolean isDivisorZero(String divisor) {
        if (StringUtils.isEmpty(divisor) || !divisor.matches("^\\d+$") || divisor.matches("^0+$")) {
            return true;
        }
        return false;
    }

    public static BigDecimal divideExcludeZero(Integer divisor) {
        if (divisor == null || divisor.equals(0)) {
            return BigDecimal.ONE;
        }
        return new BigDecimal(String.valueOf(divisor));
    }

    public static boolean isDivisorZero(Integer divisor) {
        if (divisor == null || divisor.equals(0)) {
            return true;
        }
        return false;
    }

    public static BigDecimal divideExcludeZero(double divisor) {
        BigDecimal bigDecimal = BigDecimal.valueOf(divisor);
        return BigDecimal.ZERO.compareTo(bigDecimal) == 0 ? BigDecimal.ONE : bigDecimal;

    }

    public static boolean isDivisorZero(double divisor) {
        BigDecimal bigDecimal = BigDecimal.valueOf(divisor);
        return BigDecimal.ZERO.compareTo(bigDecimal) == 0 ? true : false;

    }


}
