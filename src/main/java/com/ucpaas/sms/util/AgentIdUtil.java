package com.ucpaas.sms.util;

import java.util.Date;

import com.ucpaas.sms.common.util.DateUtils;
import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.util.enums.StaticInitVariable;

public class AgentIdUtil {
	/**
	 * @Description: 获取当前代理商id(已有当月代理商id最大值最大值)
	 * @author: Niu.T
	 * @date: 2016年12月30日 下午6:02:51
	 * @return int
	 */
	public static synchronized int getAgentId() {

		Date date = new Date();
		int num = 0;
		String agentIdPre = DateUtils.formatDate(date, "yyyyMM");
		if (agentIdPre.equals(StaticInitVariable.AGENTID_PRE)) {
			num = StaticInitVariable.AGENT_NUM;
			StaticInitVariable.AGENT_NUM = num + 1;
		} else {
			StaticInitVariable.AGENTID_PRE = agentIdPre;
			num = 1;
			StaticInitVariable.AGENT_NUM = num + 1;
		}
		// 拼接AgentId
		String agentId = agentIdPre + StringUtils.addZeroForNum(num, 4, "0");
		return Integer.valueOf(agentId);
	}
}
