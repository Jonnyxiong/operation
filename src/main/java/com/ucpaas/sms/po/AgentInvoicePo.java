package com.ucpaas.sms.po;

import com.jsmsframework.finance.entity.JsmsAgentInvoiceList;

import java.math.BigDecimal;


public class AgentInvoicePo extends JsmsAgentInvoiceList {
	private String saler;
	private Long salerId;

	public Long getSalerId() {
		return salerId;
	}

	public void setSalerId(Long salerId) {
		this.salerId = salerId;
	}

	public String getSaler() {
		return saler;
	}

	public void setSaler(String saler) {
		this.saler = saler;
	}
}
