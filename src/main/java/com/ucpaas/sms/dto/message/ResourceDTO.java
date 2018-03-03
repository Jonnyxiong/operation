package com.ucpaas.sms.dto.message;

import com.ucpaas.sms.entity.message.Resource;

/**
 * 
 * @author huangwenjie
 *
 */
public class ResourceDTO extends Resource {

	String channelTypeName;
	String directConnectName;
	String signTypeName;
	String extendSizeName;
	String minimumGuaranteeName;
	String isCreditName;
	String invoiceTypeName;
	String protocolTypeName;
	String stateName;

	public String getStateName() {
		if (stateName == null && getState() != null) {
			String s = getState();
			if ("0".equals(s))
				stateName = "待接入";
			else if ("1".equals(s))
				stateName = "已接入";
			else if ("2".equals(s))
				stateName = "待审批";
			else if ("3".equals(s))
				stateName = "撤销";
		}
		return stateName;
	}

	public void setStateName(String stateName) {
		this.stateName = stateName;
	}

	public String getProtocolTypeName() {
		if (protocolTypeName == null && getProtocolType() != null) {
			String pt = getProtocolType();
			if ("0".equals(pt))
				protocolTypeName = "HTTP";
			else if ("1".equals(pt))
				protocolTypeName = "CMPP";
			else if ("2".equals(pt))
				protocolTypeName = "SMGP";
			else if ("3".equals(pt))
				protocolTypeName = "SGIP";
			else if ("4".equals(pt))
				protocolTypeName = "SMPP";
		}
		return protocolTypeName;
	}

	public void setProtocolTypeName(String protocolTypeName) {
		this.protocolTypeName = protocolTypeName;
	}

	public String getInvoiceTypeName() {
		if (invoiceTypeName == null && getInvoiceType() != null) {
			String it = getInvoiceType();
			if ("0".equals(it))
				invoiceTypeName = "不要票";
			else if ("1".equals(it))
				invoiceTypeName = "专票";
			else if ("2".equals(it))
				invoiceTypeName = "普票";
		}
		return invoiceTypeName;
	}

	public void setInvoiceTypeName(String invoiceTypeName) {
		this.invoiceTypeName = invoiceTypeName;
	}

	public String getIsCreditName() {
		if (isCreditName == null && getIsCredit() != null) {
			if ("0".equals(getIsCredit()))
				isCreditName = "否";
			else
				isCreditName = getIsCredit() + "万条/月";
		}
		return isCreditName;
	}

	public void setIsCreditName(String isCreditName) {
		this.isCreditName = isCreditName;
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

	public String getDirectConnectName() {
		if (directConnectName == null && getDirectConnect() != null) {
			String dc = getDirectConnect();
			if ("0".equals(dc)) {
				directConnectName = "直连";
			} else if ("1".equals(dc)) {
				directConnectName = "第三方";
			}
		}
		return directConnectName;
	}

	public void setDirectConnectName(String directConnectName) {
		this.directConnectName = directConnectName;
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

	public String getExtendSizeName() {
		if (extendSizeName == null && getExtendSize() != null) {
			Integer es = Integer.valueOf(getExtendSize());
			if (es.intValue() == 0)
				extendSizeName = "不支持";
			else
				extendSizeName = "支持" + getExtendSize() + "位";
		}
		return extendSizeName;
	}

	public void setExtendSizeName(String extendSizeName) {
		this.extendSizeName = extendSizeName;
	}

}
