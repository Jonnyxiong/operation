package com.ucpaas.sms.service.admin;

import com.jsmsframework.common.util.JsonUtil;
import com.jsmsframework.user.entity.JsmsMenu;
import com.ucpaas.sms.entity.message.Role;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:spring.xml"})
public class AuthorityServiceTest {
    @Test
    public void getJsmsMenu() throws Exception {
        JsmsMenu jsmsMenu = authorityService.getJsmsMenu("子账户管理");
        System.err.println(JsonUtil.toJson(jsmsMenu));
    }

    @Autowired
    private AuthorityService authorityService;

    @Test
    public void isMenuAuthority() throws Exception {
        List<Role> roles = new ArrayList<>();
        Role role1 = new Role();
        role1.setId(22);
        Role role2 = new Role();
        role2.setId(13);
        Role role3 = new Role();
        role3.setId(23);
        roles.add(role1);
        roles.add(role2);
        roles.add(role3);
        boolean a = authorityService.isMenuAuthority(roles,"3334");
        System.err.println(a);
    }

}