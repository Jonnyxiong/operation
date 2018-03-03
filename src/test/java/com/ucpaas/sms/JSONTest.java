package com.ucpaas.sms;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ucpaas.sms.dto.message.ResourceDTO;
import com.ucpaas.sms.entity.po.OrderListPo;
import com.ucpaas.sms.util.JacksonUtil;

public class JSONTest {
	public static void main(String[] args) throws JsonProcessingException, ParseException {
		ResourceDTO resourceDTO = new ResourceDTO();
		resourceDTO.setResourceId("T000020170621");
		resourceDTO.setChannelId("205");
		resourceDTO.setChannelType("0,1,2");
//		resourceDTO.setChannelTypeName("三网合一 ,移动 ,电信");
		resourceDTO.setSmsType("纯验证码");
		resourceDTO.setContentProp("发送内容属性");
		resourceDTO.setBelongBusiness("1");
		resourceDTO.setBelongBusinessName("商务黄文杰");
		resourceDTO.setDirectConnect("1");
		resourceDTO.setDirectConnectName("过平台");
		resourceDTO.setExtendSize("0");
		resourceDTO.setSignType("0,1,2");
//		resourceDTO.setSignTypeName("自定义 ,报备签名 ,固签");
		resourceDTO.setPurchasePrice("阶梯价:0.222");
		resourceDTO.setRate("34");
		resourceDTO.setMinimumGuarantee("10");
		resourceDTO.setIsCredit("1");
		resourceDTO.setPayType("预付");
		resourceDTO.setInvoiceType("0");
		resourceDTO.setMtIp("192.168.1.20");
		resourceDTO.setProtocolType("1");
		resourceDTO.setSupplier("供应商");
		resourceDTO.setContact("曹线上");
		resourceDTO.setMobile("13636201207");
		resourceDTO.setIsAudit("0");
		resourceDTO.setOnlineDate(new Date());
		resourceDTO.setState("0");
		resourceDTO.setAuditorId("1");
		resourceDTO.setOperatorId("1");
		resourceDTO.setCreateTime(new Date());
		resourceDTO.setUpdateTime(new Date());
		
		
		System.out.println(JacksonUtil.toJSON(resourceDTO));
		
		OrderListPo op = new OrderListPo();
    	op.setOrderId("D000020170623");
    	op.setCompanyName("黄文杰测试");
    	op.setIndustryType("互联网");
    	op.setBelongSale(149l);
    	op.setBelongSaleName("黄文杰销售归属");
    	op.setSmsType("验证码,通知,普通营销");
    	op.setExpectNumber("100万以下");
    	op.setMinimumGuarantee("100");
    	op.setChannelType("0,1,2");
//    	op.setChannelTypeName("三网合一,移动,电信");
    	op.setDirectConnect(0);
//    	op.setDirectConnectName("直连");
    	op.setExtendSize(3);
    	op.setRate(300);
    	op.setSignType("0,1,2");
//    	op.setSignTypeName("自定义,报备签名,固签");
    	op.setContentTemplate("短信模版测试测试测试测试");
    	op.setPayType("按月结算");
    	op.setSalePrice("统一价:0.22");
    	op.setInvoiceType(1);
//    	op.setInvoiceTypeName("普票");
    	op.setOnlineDate(new SimpleDateFormat("yyyy-MM-dd").parse("2017-06-27"));
    	op.setIsAudit(1);
//    	op.setIsAuditName("是");
    	op.setState(4);
//    	op.setStateName("撤单");
    	op.setRemark("备注");
    	op.setAuditorId(123l);
    	op.setOperatorId(123l);
    	op.setCreateTime(new Date());
    	op.setUpdateTime(new Date());
    	MappingJackson2HttpMessageConverter c = new MappingJackson2HttpMessageConverter();
    	System.out.println(JacksonUtil.toJSON(op));
    	System.out.println(c.getObjectMapper().writeValueAsString(op));
		
		
	}
}
