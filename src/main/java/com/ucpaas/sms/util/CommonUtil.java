package com.ucpaas.sms.util;

import com.jsmsframework.common.dto.JsmsPage;
import com.ucpaas.sms.model.PageContainer;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Administrator on 2017/9/11.
 */
@Component
public class CommonUtil {
    public static String getClientIP(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip.equals("0:0:0:0:0:0:0:1")?"127.0.0.1":ip;
    }

    /**
     * 将Jsms framwork中的分页对象修改为调度系统使用的分页对象
     * @param jsmsPage
     * @return
     */
    public static PageContainer converterJsmsPage2PageContainer(JsmsPage jsmsPage){

        PageContainer pageContainer = new PageContainer();
        pageContainer.setList(jsmsPage.getData()); // 查询结果
        pageContainer.setCurrentPage(jsmsPage.getPage());  // 当前页号
        pageContainer.setTotalCount(jsmsPage.getTotalRecord());  // 总行数
        pageContainer.setTotalPage(jsmsPage.getTotalPage()); // 总页数
        pageContainer.setPageRowCount(jsmsPage.getRows()); // 默认每页显示行数

        return pageContainer;
    }

    /**
     * 根据JSP页面传值初始化Jsms分页对象
     * @param params
     * @return
     */
    public static JsmsPage initJsmsPage(Map<String, String> params){
        JsmsPage jsmsPage = new JsmsPage();
        String currentPage = Objects.toString(params.get("currentPage"), "");
        String pageRowCount = Objects.toString(params.get("pageRowCount"), "");

        if(StringUtils.isNotBlank(currentPage)){
            jsmsPage.setPage(Integer.valueOf(currentPage));
        }

        if(StringUtils.isNotBlank(pageRowCount)){
            jsmsPage.setRows(Integer.valueOf(pageRowCount));
        }
        return jsmsPage;
    }
    public static Boolean isObjectEmptyString(Object obj){
        if(obj==null){
            return  true;
        }
        return StringUtils.isBlank(obj.toString());
    }

}

