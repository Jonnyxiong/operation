package com.ucpaas.sms.service.finance.order;

import com.jsmsframework.common.util.JsonUtil;
import com.jsmsframework.user.entity.JsmsUser;
import com.jsmsframework.user.service.JsmsUserService;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.entity.message.Account;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.account.AccountService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:spring.xml"})
public class OrderInfoServiceImplTest {
    @Test
    public void queryOEMClientOrder() throws Exception {
        Map map = new HashMap();
        JsmsUser user = jsmsUserService.getById("317");

        UserSession userSession = new UserSession();

        userSession.setId(user.getId());
        userSession.setDataAuthority(user.getDataAuthority());
        userSession.setDepartmentId(user.getDepartmentId());
        userSession.setRealname(user.getRealname());
        userSession.setStatus(user.getStatus());
        userSession.setWebId(user.getWebId());
        userSession.setSid(user.getSid());

//        _search:false
//        nd:1513570084855
//        rows:20
//        page:1
//        sidx:
//        sord:asc
        map.put("_search","false");
        map.put("nd","1513570084855");
        map.put("pageRowCount",20);
        map.put("currentPage",1);
        map.put("sord","asc");
        PageContainer result = orderInfoService.queryOEMClientOrder(map, userSession);
        System.err.println(JsonUtil.toJson(result));
    }

    @Test
    public void queryOEMAgentOrder() throws Exception {
        Map map = new HashMap();
        JsmsUser user = jsmsUserService.getById("317");

        UserSession userSession = new UserSession();

        userSession.setId(user.getId());
        userSession.setDataAuthority(user.getDataAuthority());
        userSession.setDepartmentId(user.getDepartmentId());
        userSession.setRealname(user.getRealname());
        userSession.setStatus(user.getStatus());
        userSession.setWebId(user.getWebId());
        userSession.setSid(user.getSid());

//        _search:false
//        nd:1513568147431
//        rows:20
//        page:1
//        sidx:
//        sord:asc
        map.put("_search","false");
        map.put("nd","1513568147431");
        map.put("pageRowCount",20);
        map.put("currentPage",1);
        map.put("sord","asc");

        PageContainer result = orderInfoService.queryOEMAgentOrder(map, userSession);
        System.err.println(JsonUtil.toJson(result));
    }

    @Autowired
    private OrderInfoService orderInfoService;

    @Autowired
    private JsmsUserService jsmsUserService;

    @Test
    public void queryOrder() throws Exception {
        Map map = new HashMap();

        JsmsUser user = jsmsUserService.getById("232");

        UserSession userSession = new UserSession();

        userSession.setId(user.getId());
        userSession.setDataAuthority(user.getDataAuthority());
        userSession.setDepartmentId(user.getDepartmentId());
        userSession.setRealname(user.getRealname());
        userSession.setStatus(user.getStatus());
        userSession.setWebId(user.getWebId());
        userSession.setSid(user.getSid());
//        userSession.setRoleName("销售人员");

//        map.put("status","0");
        map.put("_search","false");
        map.put("nd","1513328700167");
        map.put("rows","30");
        map.put("page","1");
        map.put("sord","asc");


        PageContainer pageContainer = orderInfoService.queryOrder(map,userSession);
        System.out.println(JsonUtil.toJson(pageContainer));
    }

    @Test
    public void total() throws Exception {
        Map params = new HashMap();
        JsmsUser user = jsmsUserService.getById("232");
        UserSession userSession = new UserSession();
        userSession.setId(user.getId());
        userSession.setDataAuthority(user.getDataAuthority());
        userSession.setDepartmentId(user.getDepartmentId());
        userSession.setRealname(user.getRealname());
        userSession.setStatus(user.getStatus());
        userSession.setWebId(user.getWebId());
        userSession.setSid(user.getSid());
        params.put("status","0");
//        Map map = orderInfoService.total(params);
        Map map = orderInfoService.totalOrder(params, userSession);
        System.err.println(JsonUtil.toJson(map));
    }

}