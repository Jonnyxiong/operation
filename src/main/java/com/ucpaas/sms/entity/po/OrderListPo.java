package com.ucpaas.sms.entity.po;

import com.ucpaas.sms.entity.message.OrderList;

/**
 * Created by huangzaizheng on 2017/6/20.
 */
public class OrderListPo extends OrderList {

	private String belongSaleName;

	private String channelTypeName;

	private String directConnectName;

	private String signTypeName;

	private String invoiceTypeName;

	private String isAuditName;

	private String stateName;

	private String minimumGuaranteeName;

	private String extendSizeName;

	private String demandState;

	private String demandStateName;

	public String getDemandState() {
		return demandState;
	}

	public void setDemandState(String demandState) {
		this.demandState = demandState;
	}

	public String getDemandStateName() {
		if (demandStateName == null && getDemandState() != null) {
			String s = getDemandState();
			if ("0".equals(s))
				demandStateName = "寻资源";
			else if ("1".equals(s))
				demandStateName = "已供应";
			else if ("2".equals(s))
				demandStateName = "无资源";
			else if ("3".equals(s))
				demandStateName = "撤单";
		}
		if (demandStateName == null) {
			demandStateName = "——";
		}
		return demandStateName;
	}

	public void setDemandStateName(String demandStateName) {
		this.demandStateName = demandStateName;
	}

	public String getExtendSizeName() {
		if (extendSizeName == null && getExtendSize() != null) {
			Integer es = Integer.valueOf(getExtendSize());
			if (es.intValue() == 0)
				extendSizeName = "无特殊要求";
			else
				extendSizeName = "要求" + getExtendSize() + "位";
		}
		return extendSizeName;
	}

	public void setExtendSizeName(String extendSizeName) {
		this.extendSizeName = extendSizeName;
	}

	public String getMinimumGuaranteeName() {
		if (minimumGuaranteeName == null && getMinimumGuarantee() != null) {
			if ("0".equals(getMinimumGuarantee()))
				minimumGuaranteeName = "不承诺保底";
			else
				minimumGuaranteeName = getMinimumGuarantee() + "万条/月";
		}
		return minimumGuaranteeName;
	}

	public void setMinimumGuaranteeName(String minimumGuaranteeName) {
		this.minimumGuaranteeName = minimumGuaranteeName;
	}

	public String getStateName() {
		int s = getState();
		if (s == 0)
			stateName = "待配单";
		else if (s == 1)
			stateName = "待匹配";
		else if (s == 2)
			stateName = "待审批";
		else if (s == 3)
			stateName = "退单";
		else if (s == 4)
			stateName = "撤单";
		else if (s == 5)
			stateName = "匹配成功";
		else if (s == 6)
			stateName = "寻资源";
		else if (s == 7)
			stateName = "待审核";
		else if (s == 8)
			stateName = "待审核";
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	public String getIsAuditName() {
		if (isAuditName == null && getIsAudit() != null) {
			int ia = getIsAudit();
			if (ia == 0)
				isAuditName = "否";
			else if (ia == 1)
				isAuditName = "是";
		}
		return isAuditName;
	}

	public void setIsAuditName(String isAuditName) {
		this.isAuditName = isAuditName;
	}

	public String getInvoiceTypeName() {
		if (invoiceTypeName == null && getInvoiceType() != null) {
			int it = getInvoiceType();
			if (it == 0)
				invoiceTypeName = "不要票";
			else if (it == 1)
				invoiceTypeName = "专票";
			else if (it == 2)
				invoiceTypeName = "普票";
		}
		return invoiceTypeName;
	}

	public void setInvoiceTypeName(String invoiceTypeName) {
		this.invoiceTypeName = invoiceTypeName;
	}

	public String getSignTypeName() {
		if (signTypeName == null && getSignType() != null) {
			StringBuilder sb = new StringBuilder();
			String[] sts = getSignType().split(",");
			String delim = "";
			for (String st : sts) {
				if ("0".equals(st))
					sb.append(delim).append("自定义");
				else if ("1".equals(st))
					sb.append(delim).append("报备签名");
				else if ("2".equals(st))
					sb.append(delim).append("固签");
				else
					continue;
				delim = ",";
			}
			signTypeName = sb.toString();
		}
		return signTypeName;
	}

	public void setSignTypeName(String signTypeName) {
		this.signTypeName = signTypeName;
	}

	public String getChannelTypeName() {
		if (channelTypeName == null && getChannelType() != null) {
			StringBuilder sb = new StringBuilder();
			String[] cts = getChannelType().split(",");
			String delim = "";
			for (String ct : cts) {
				if ("0".equals(ct))
					sb.append(delim).append("三网合一");
				else if ("1".equals(ct))
					sb.append(delim).append("移动");
				else if ("2".equals(ct))
					sb.append(delim).append("电信");
				else if ("3".equals(ct))
					sb.append(delim).append("联通");
				else if ("4".equals(ct))
					sb.append(delim).append("全网");
				else
					continue;
				delim = ",";
			}
			channelTypeName = sb.toString();
		}
		return channelTypeName;
	}

	public void setChannelTypeName(String channelTypeName) {
		this.channelTypeName = channelTypeName;
	}

	public String getBelongSaleName() {
		return belongSaleName;
	}

	public void setBelongSaleName(String belongSaleName) {
		this.belongSaleName = belongSaleName;
	}

	public String getDirectConnectName() {
		if (directConnectName == null && getDirectConnect() != null) {
			int dc = getDirectConnect();
			if (dc == 0) {
				directConnectName = "直连";
			} else if (dc == 1) {
				directConnectName = "第三方";
			}
		}
		return directConnectName;
	}

	public void setDirectConnectName(String directConnectName) {
		this.directConnectName = directConnectName;
	}

}
