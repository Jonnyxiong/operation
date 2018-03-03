package com.ucpaas.sms.util;

import com.jsmsframework.common.dto.JsmsPage;
import com.ucpaas.sms.model.PageContainer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by dylan on 2017/8/16.
 */
public class PageConvertUtil {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(PageConvertUtil.class);

    public static JsmsPage paramToPage(Map<String,String> pageParams){
        JsmsPage page = new JsmsPage();
        try {
            if(!StringUtils.isEmpty(pageParams.get("pageRowCount"))){
                page.setRows(Integer.parseInt(pageParams.get("pageRowCount")));
            }else if(!StringUtils.isEmpty(pageParams.get("rows"))){
                page.setRows(Integer.parseInt(pageParams.get("rows")));
            }else{
                page.setRows( 30);
            }
            if(!StringUtils.isEmpty(pageParams.get("currentPage"))){
                page.setPage(Integer.parseInt(pageParams.get("currentPage")));
            }else if(!StringUtils.isEmpty(pageParams.get("page"))){
                page.setPage(Integer.parseInt(pageParams.get("page")));
            }else{
                page.setPage(1);
            }
            if(!StringUtils.isEmpty(pageParams.get("orderByClause"))){
                page.setOrderByClause(pageParams.get("orderByClause"));
            }

        } catch (ClassCastException e) {
            LOGGER.debug("pageParams 转 Page , 参数异常 ---> {}",e);
        }
        page.setParams(pageParams);
        return page;
    }

    public static PageContainer pageToContainer(JsmsPage page){
        PageContainer pageContainer = new PageContainer();
        pageContainer.setCurrentPage(page.getPage());
        pageContainer.setTotalPage(page.getTotalPage());
        pageContainer.setTotalCount(page.getTotalRecord());
        pageContainer.setPageRowCount(page.getRows());
        pageContainer.getEntityList().addAll(page.getData());
        /*for (Object obj : page.getData()) {
            try {
                Map<String, String> describe = BeanUtils.describe(obj);
                list.add(describe);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }*/
        return pageContainer;
    }

}
