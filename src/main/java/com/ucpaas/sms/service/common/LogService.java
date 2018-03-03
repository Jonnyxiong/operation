package com.ucpaas.sms.service.common;

import java.util.Map;

import com.ucpaas.sms.constant.LogConstant.LogType;

/**
 * 日志业务
 * 
 * @author huangwenjie
 */
public interface LogService {
 
	/**
	 * 
	 * @param logType 日志类型
	 * @param moduleName
	 * @param userId 操作者
	 * @param pageUrl
	 * @param ip
	 * @param desc 日志信息
	 * @return
	 */
	boolean add(LogType logType, String moduleName, Long userId, String pageUrl, String ip, Object... desc);
 

}
