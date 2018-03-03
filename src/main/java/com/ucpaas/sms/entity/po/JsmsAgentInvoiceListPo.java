package com.ucpaas.sms.entity.po;

import com.jsmsframework.finance.entity.JsmsAgentInvoiceList;

import java.util.List;

public class JsmsAgentInvoiceListPo extends JsmsAgentInvoiceList {

	private boolean isOpenInvoice;
	private List<String> returnInvoiceIds;

	public boolean isOpenInvoice() {
		return isOpenInvoice;
	}

	public void setOpenInvoice(boolean openInvoice) {
		isOpenInvoice = openInvoice;
	}

	public List<String> getReturnInvoiceIds() {
		return returnInvoiceIds;
	}

	public void setReturnInvoiceIds(List<String> returnInvoiceIds) {
		this.returnInvoiceIds = returnInvoiceIds;
	}
}
