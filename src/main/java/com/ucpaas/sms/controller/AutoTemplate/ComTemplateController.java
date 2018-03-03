package com.ucpaas.sms.controller.AutoTemplate;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.enums.AutoTemplateLevel;
import com.jsmsframework.common.enums.AutoTemplateStatus;
import com.jsmsframework.common.enums.WebId;
import com.jsmsframework.user.audit.service.JsmsUserAutoTemplateService;
import com.ucpaas.sms.controller.BaseController;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.util.AgentUtils;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.JsonUtils;
import com.ucpaas.sms.util.PageConvertUtil;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * created by xiaoqingwen on 2017/12/18 12:04
 * 通用模版
 */
@Controller
@RequestMapping(value="/comTemplate")
public class ComTemplateController extends BaseController{
    private static final Logger logger = LoggerFactory.getLogger(ComTemplateController.class);
    @Autowired
    private JsmsUserAutoTemplateService jsmsUserAutoTemplateService;

    @ApiOperation(value = "通用模版路由", notes = "通用模版路由", tags = "智能模版报备",response = ModelAndView.class)
    @GetMapping("comTemplateQuery")
    public ModelAndView comTemplate(ModelAndView mv,HttpSession session){

        UserSession user = getUserFromSession(session);

        Map<String, Boolean> right = AgentUtils.hasMenuRight(user, "智能模板-znmb-286", "通用模板-tymb");
        mv.addObject("menus", right);

        if ((Boolean)right.get("tymb")){
            mv.setViewName("AutoTemplate/comTemplate");
        } else if ((Boolean)right.get("znmb286")) {
            mv.addObject("userId",user.getId());
            mv.addObject("max_export_num", ConfigUtils.max_export_excel_num);
            mv.setViewName("AutoTemplate/autoTemplate");
        } else {
            mv.setViewName("AutoTemplate/comTemplate");
        }

        return mv;
    }

    /**
     * 通用模版搜索及分页
     * @return
     */
    @ApiOperation(value = "通用模版搜索", notes = "通用模版搜索", tags = "智能模版报备",response = Map.class)
    @RequestMapping(path="/comTemplateQuery",method = RequestMethod.POST )
    @ResponseBody
    public String comTemplateQuery(Integer smsType, Integer templateId, String content, String templateType,@RequestParam(value="page", defaultValue="1")Integer page, @RequestParam(value="rows", defaultValue="20")Integer rows){

        Map<String,String> params=new HashMap<>();
        Map<String, Object> objectMap = new HashMap<>();
        params.put("currentPage",String.valueOf(page));
        params.put("pageRowCount",String.valueOf(rows));
        JsmsPage jsmsPage = PageConvertUtil.paramToPage(params);
        if(null !=smsType){
            objectMap.put("smsType",smsType);
        }
        if(null !=templateId){
            objectMap.put("templateId",templateId);
        }
        if(StringUtils.isNotBlank(content)){
            objectMap.put("content",content);
        }
        if(StringUtils.isNotBlank(templateType)){
            objectMap.put("templateType",templateType);
        }
        objectMap.put("state", AutoTemplateStatus.审核通过.getValue());//审核通过为1
        AutoTemplateLevel autoTemplateLevel=AutoTemplateLevel.全局级别;//全局级别为0
        jsmsPage.setParams(objectMap);
        jsmsPage.setOrderByClause("a.create_time DESC");
        JsmsPage queryPage = jsmsUserAutoTemplateService.findListOfAutoTemplate(jsmsPage, WebId.运营平台.getValue(),autoTemplateLevel);
        PageContainer pageCon  = PageConvertUtil.pageToContainer(queryPage);
        return JsonUtils.toJson(pageCon);
    }
}
