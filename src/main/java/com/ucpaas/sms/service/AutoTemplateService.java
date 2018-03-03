package com.ucpaas.sms.service;

import com.jsmsframework.audit.entity.JsmsAutoTemplate;
import com.jsmsframework.common.dto.JsmsPage;

/**
 * Created by xiongfenglin on 2018/1/5.
 *
 * @author: xiongfenglin
 */
public interface AutoTemplateService {
    JsmsPage findListOfAutoTemplate(JsmsPage<JsmsAutoTemplate> page);

//
}
