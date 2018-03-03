package com.ucpaas.sms.exception;/**
 * Created by Dylan on 2017/3/13.
 */

/**
 * 权限分配异常
 * @author : Niu.T
 * @date: 2017年03月13 10:19
 */
public class AuthorityAssignException extends OperationException{


    public AuthorityAssignException(String message, Throwable cause) {
        super(message, cause);
    }

    public AuthorityAssignException(String message) {
        super(message);
    }
}
