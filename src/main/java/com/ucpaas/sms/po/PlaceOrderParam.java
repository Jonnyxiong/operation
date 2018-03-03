package com.ucpaas.sms.po;

import java.util.List;

import com.ucpaas.sms.entity.po.ProductInfoPo;

public class PlaceOrderParam {
	
	private String clientid;
	
	private String agentId;
	
	private List<ProductInfoPo> productInfoPoList;

	public String getClientid() {
		return clientid;
	}

	public void setClientid(String clientid) {
		this.clientid = clientid;
	}

	public List<ProductInfoPo> getProductInfoPoList() {
		return productInfoPoList;
	}

	public void setProductInfoPoList(List<ProductInfoPo> productInfoPoList) {
		this.productInfoPoList = productInfoPoList;
	}

	public String getAgentId() {
		return agentId;
	}

	public void setAgentId(String agentId) {
		this.agentId = agentId;
	}
	
	
	
	

}
