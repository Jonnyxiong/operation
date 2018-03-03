package com.ucpaas.sms.service.common;

import com.google.common.collect.Lists;
import com.ucpaas.sms.common.util.NumConverUtil;
import com.ucpaas.sms.entity.message.ClientIdSequence;
import com.ucpaas.sms.exception.OperationException;
import com.ucpaas.sms.mapper.message.ClientIdSequenceMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CommonSeqServiceImpl implements CommonSeqService {

	@Autowired
	private ClientIdSequenceMapper clientIdSequenceMapper;

	@Override
	@Transactional("message")
	public synchronized String getOrAddId(String prefix) {
		Map<String, Object> data = new HashMap<String, Object>();

		ClientIdSequence query = new ClientIdSequence();
		query.setClientIdType(getClientType(prefix));
		query.setPrefix(prefix);

		// 随机取出未使用的序列
		ClientIdSequence clientIdSequence = clientIdSequenceMapper.getUnusedRandom(query);

		// 生成帐号
		if (clientIdSequence == null) {
			batchAddClientId(getCreateStartClientId(prefix), Integer.parseInt(query.getClientIdType()));

			// 重新获取
			clientIdSequence = clientIdSequenceMapper.getUnusedRandom(query);
		}

		// 更新lock字段为1表示临时占用，更新lock_start_time表示开始占用时间，ucpaas-sms-task会检查占用超过30分钟的记录并修改lock为0
		int count = clientIdSequenceMapper.lock(clientIdSequence.getClientId());
		if (count <= 0)
		{
			return null;
		}
		return clientIdSequence.getClientId();
	}

	private String getClientType(String prefix) {
		String clientIdType = null;
		switch (prefix) {
		case "a":
			clientIdType = "1";
			break;
		case "b":
			clientIdType = "0";
			break;
		}

		if (clientIdType == null) {
			throw new OperationException("获取客户开户的类型错误！");
		}
		return clientIdType;
	}

	private String getCreateStartClientId(String prefix) {
		String start;
		String max = clientIdSequenceMapper.getMax(Integer.parseInt(getClientType(prefix)), prefix);
		if (StringUtils.isBlank(max)) {
			start = prefix + "00000";
		} else {
			long maxDecimal = NumConverUtil.converToDecimal(max.substring(0, max.length() - 1));
			start = NumConverUtil.converTo36HEX((maxDecimal + 1), "") + 0;
		}
		return start;
	}

	private void batchAddClientId(String startClientId, Integer clientIdType) {
		long nextClientIdDecimal = NumConverUtil
				.converToDecimal(startClientId.substring(0, startClientId.length() - 1));
		List<String> numbers = Lists.newLinkedList();
		for (int i = 1; i <= 1000; i++) {
			for (int j = 0; j < 10; j++) {
				if (i == 1) {
					numbers.add(NumConverUtil.converTo36HEX((nextClientIdDecimal), "") + j);
				} else {
					numbers.add(NumConverUtil.converTo36HEX((++nextClientIdDecimal), "") + j);
				}
			}
		}
		clientIdSequenceMapper.batchAdd(numbers, clientIdType);
	}

	@Override
	@Transactional("message")
	public boolean updateClientIdStatus(String clientId) {
		boolean result = false;
		if (StringUtils.isNotBlank(clientId)) {
			result = clientIdSequenceMapper.updateStatus(clientId) > 0;
		}
		return result;
	}

}
