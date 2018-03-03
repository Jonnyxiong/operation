package com.ucpaas.sms.entity.po;

import com.jsmsframework.product.entity.JsmsOemAgentProduct;
import com.jsmsframework.product.entity.JsmsOemProductInfo;

public class OemProductInfoPo extends JsmsOemProductInfo {
	private JsmsOemAgentProduct oemAgentProduct;

	public JsmsOemAgentProduct getOemAgentProduct() {
		return oemAgentProduct;
	}

	public void setOemAgentProduct(JsmsOemAgentProduct oemAgentProduct) {
		this.oemAgentProduct = oemAgentProduct;
	}
}
