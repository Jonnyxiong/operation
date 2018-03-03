package com.ucpaas.sms.po;

import java.math.BigDecimal;


public class ProductInfoPo extends ProductInfo{
	
	private Integer productNum;
	
	private BigDecimal product_price_dis;

	public Integer getProductNum() {
		return productNum;
	}

	public void setProductNum(Integer productNum) {
		this.productNum = productNum;
	}

	public BigDecimal getProduct_price_dis() {
		return product_price_dis;
	}

	public void setProduct_price_dis(BigDecimal product_price_dis) {
		this.product_price_dis = product_price_dis;
	}
	
}
