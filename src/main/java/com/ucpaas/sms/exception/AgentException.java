package com.ucpaas.sms.exception;

/**
 * 代理商相关异常
 * @author huangwenjie
 *
 */
public class AgentException extends RuntimeException {

	public AgentException(String message, Throwable cause) {
		super(message, cause);
	}

	public AgentException(String message) {
		super(message);
	}

	public AgentException(StringBuilder msg) {
		super(msg.toString());
	}
}
