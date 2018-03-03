package com.ucpaas.sms.util.web;

import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ucpaas.sms.util.ConfigUtils;

/**
 * HttpClient工具类
 * 
 * @author xiejiaan
 */
public class HttpClientUtils {
	private static final Logger LOGGER = LoggerFactory.getLogger(HttpClientUtils.class);

	/**
	 * 发送get请求
	 * 
	 * @param url
	 * @return
	 */
	public static String doGet(String url) {
		String msg = null;
		try {
			msg = Request.Get(url).execute().returnContent().asString();
			msg = new String(msg.getBytes("iso-8859-1"), "utf-8");
		} catch (Throwable e) {
			LOGGER.error("发送get请求失败, url=" + url, e);
		}
		LOGGER.debug("发送get请求, url=" + url + ", msg=" + msg);
		return msg;
	}

	/**
	 * app审核通过后，分配短信号码
	 * 
	 * @param industry
	 *            行业
	 * @return
	 */
	public static String getMsgNbr(String industry) {
		return doGet(ConfigUtils.interface_url_getMsgNbr + industry);
	}

}
