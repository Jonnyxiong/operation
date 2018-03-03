package com.ucpaas.sms.entity.po;

import com.ucpaas.sms.entity.ProductInfo;

import java.math.BigDecimal;

public class ProductInfoPo extends ProductInfo{
	
	private Integer productNum;
	
	private BigDecimal product_price_dis;
	private BigDecimal gn_discount_price;

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

	public BigDecimal getGn_discount_price() {
		return gn_discount_price;
	}

	public void setGn_discount_price(BigDecimal gn_discount_price) {
		this.gn_discount_price = gn_discount_price;
	}
}
