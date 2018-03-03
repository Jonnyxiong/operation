package com.ucpaas.sms.service.finance.bill;

import com.jsmsframework.user.entity.JsmsUser;
import com.jsmsframework.user.service.JsmsUserService;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.model.PageContainer;
import com.jsmsframework.common.util.JsonUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:spring.xml"})
public class BillInfoServiceImplTest {

    @Autowired
    private JsmsUserService jsmsUserService;

    @Autowired
    private BillInfoService billInfoService;

    @Test
    public void queryBalanceBill() throws Exception {
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

        map.put("pageRowCount",20);
        map.put("currentPage",1);

        PageContainer result = billInfoService.queryBalanceBill(map,userSession);
        System.err.println(JsonUtil.toJson(result));
    }

}