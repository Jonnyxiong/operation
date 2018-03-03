package com.ucpaas.sms.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 短信模板审核通知回调
 * @author wangwei
 * @date 2017年3月8日 下午2:50:51
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CallbackResponse {
	private String code;
	private String errmsg;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getErrmsg() {
		return errmsg;
	}
	public void setErrmsg(String errmsg) {
		this.errmsg = errmsg;
	}
	
}
