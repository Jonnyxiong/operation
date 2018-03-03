package com.ucpaas.sms.service;

import com.jsmsframework.audit.entity.JsmsAutoTemplate;
import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.enums.WebId;
import com.jsmsframework.common.enums.balckAndWhiteTemplate.TemplateLevel;
import com.jsmsframework.user.audit.service.JsmsUserAutoTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by xiongfenglin on 2018/1/5.
 *
 * @author: xiongfenglin
 */
@Service
public class AutoTemplateServiceImpl implements AutoTemplateService{
    @Autowired
    private JsmsUserAutoTemplateService jsmsUserAutoTemplateService;
    @Override
    public JsmsPage findListOfAutoTemplate(JsmsPage<JsmsAutoTemplate> jsmsPage) {
        jsmsUserAutoTemplateService.findListOfAutoTemplate(jsmsPage, WebId.运营平台.getValue(), TemplateLevel.用户级别);

        return jsmsPage;
    }
}
