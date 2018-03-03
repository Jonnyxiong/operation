package com.ucpaas.sms.util.enums;

public class StaticInitVariable {
	
	
	//订单前缀
	public static String ORDERID_PRE ="";
	
	//同一分钟内的第几笔订单
	public static int ORDER_NUM = 1;
	
	
	public static String AGENTID_PRE = "";
	
	public static int AGENT_NUM = 1;

	// 资源ID
	public static final String RESOURCE_ID_PRE = "T";
	public static String RESOURCE_ID_SUFFIX = "";
	public static final int RESOURCE_ID_SEQ_LEN = 4;
	public static int RESOURCE_ID_SEQ = -1;

	// 需求ID
	public static final String DEMAND_ID_PRE = "X";
	public static String DEMAND_ID_SUFFIX = "";
	public static final int DEMAND_ID_SEQ_LEN = 4;
	public static int DEMAND_ID_SEQ = -1;

	//==============需求订单-开始================
	//需求订单后缀
	public static String DEMAND_ORDERID_SUF = "";

	//同一天内的第几笔订单
	public static int DEMAND_ORDER_NUM = 1;


	//==============需求订单-结束===============
	//代理商订单表-订单前缀
	public static String OEM_AGENT_ORDERID_PRE ="";

	//代理商订单表-订单序号（同一分钟内的第几笔订单）
	public static int OEM_AGENT_ORDER_NUM = 1;

	//客户订单表-订单前缀
	public static String OEM_CLIENT_ORDERID_PRE ="";

	//客户订单表-订单序号（同一分钟内的第几笔订单）
	public static int OEM_CLIENT_ORDER_NUM = 1;

	// 发票订单ID
	public static String AGENT_INVOICE_ID_PRE ="";
	public static int AGENT_INVOICE_ID_NUM = 1;
}
