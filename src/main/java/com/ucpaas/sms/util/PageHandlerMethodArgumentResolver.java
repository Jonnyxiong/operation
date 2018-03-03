package com.ucpaas.sms.util;/**
 * Created by Dylan on 2017/2/21.
 */

import com.opensymphony.oscache.util.StringUtil;
import com.ucpaas.sms.model.Page;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * @author : Niu.T
 * @date: 2017年02月21 20:24
 */
public class PageHandlerMethodArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return Page.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        String rows = webRequest.getParameter("rows");
        String page = webRequest.getParameter("page");
        int pageSize = 10;
        int pageNo = 1;
        if(!StringUtil.isEmpty(rows))
            pageSize = Integer.valueOf(rows);
        if(!StringUtil.isEmpty(page))
            pageNo = Integer.valueOf(page);

        return new Page(pageNo,pageSize);
    }

}
