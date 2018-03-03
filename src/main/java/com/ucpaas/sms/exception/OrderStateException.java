package com.ucpaas.sms.exception;

/**
 * 订单状态
 * @author huangwenjie
 *
 */
public class OrderStateException extends RuntimeException {

	public OrderStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public OrderStateException(String message) {
		super(message);
	}
}
