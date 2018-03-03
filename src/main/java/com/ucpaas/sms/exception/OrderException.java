package com.ucpaas.sms.exception;

/**
 * 订单异常
 * @author huangwenjie
 *
 */
public class OrderException extends RuntimeException {

	public OrderException(String message, Throwable cause) {
		super(message, cause);
	}

	public OrderException(String message) {
		super(message);
	}
}
