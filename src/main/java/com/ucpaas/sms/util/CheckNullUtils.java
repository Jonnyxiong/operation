package com.ucpaas.sms.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 数值null检查,如果是null 返回 0, 否则返回 原值
 * @author dylan
 */
@Component
public class CheckNullUtils {
	private static final Logger logger = LoggerFactory.getLogger(CheckNullUtils.class);
	
	public static BigDecimal check(BigDecimal num){
		if(num == null){
			return BigDecimal.ZERO;
		}
		return num;
	}


	public static Integer check(Integer num){
		if(num == null){
			return 0;
		}
		return num;
	}


	public static Long check(Long num){
		if(num == null){
			return 0L;
		}
		return num;
	}




 
}
