package com.ucpaas.sms.util;/**
							* Created by Dylan on 2017/2/21.
							*/

import java.io.BufferedReader;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonArray;
import com.ucpaas.sms.controller.PeiDanController;
import com.ucpaas.sms.dto.PeidanRequest;
import com.ucpaas.sms.exception.OrderException;

/**
 * @author huangwenjie
 */
public class PendanRequestHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

	private static Logger logger = LoggerFactory.getLogger(PendanRequestHandlerMethodArgumentResolver.class);
	
	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return PeidanRequest.class.equals(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {

		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		// content-type不是json的不处理
		if (!request.getContentType().contains("application/json")) {
			logger.error("请求ContentType不对,期望 application/json,实际为"+request.getContentType());
			throw new OrderException("请求ContentType需为 application/json");
		}
		// 把reqeust的body读取到StringBuilder
		BufferedReader reader = request.getReader();
		StringBuilder sb = new StringBuilder();

		char[] buf = new char[1024];
		int rd;
		while ((rd = reader.read(buf)) != -1) {
			sb.append(buf, 0, rd);
		}

		PeidanRequest peidan = new PeidanRequest();
		try {
			JSONObject jsonObject = JSONObject.parseObject(sb.toString());
			peidan.setOrderId(jsonObject.getString("orderId"));
			peidan.setRemark(jsonObject.getString("remark"));
			JSONArray jsonArray = jsonObject.getJSONArray("resourceIds");
			String[] resourceIds = null;
			if(jsonArray!=null){
				Object objs[] = jsonArray.toArray();
				if (objs != null && objs.length > 0) {
					resourceIds = new String[objs.length];
					for (int i = 0; i < objs.length; i++) {
						Object o = objs[i];
						if (o != null) {
							resourceIds[i] = o.toString();
						}
					}
				}
			}
			peidan.setResourceIds(resourceIds);
		} catch (Exception e) {
			logger.error("转换匹配订单的请求参数异常",e);
		}
		return peidan;
	}

}
