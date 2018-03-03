package com.ucpaas.sms.service.finance.consume;

import com.jsmsframework.finance.service.JsmsClientConsumerListService;
import com.jsmsframework.user.entity.JsmsUser;
import com.jsmsframework.user.service.JsmsUserService;
import com.ucpaas.sms.entity.UserSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:spring.xml"})
public class ConsumeServiceTest {

    @Autowired
    private JsmsClientConsumerListService jsmsClientConsumerListService;

    @Test
    public void queryBrandTotal() throws Exception {
        Map map = new HashMap();
        map.put("clientid","b000e4");
        map.put("operateType","0");
        map.put("operateType","0");

        Integer n = jsmsClientConsumerListService.queryBrandConsumeTotal(map);
        System.err.println(n);
    }

}