package com.ucpaas.sms.service;

import com.ucpaas.sms.entity.po.DataAuthorityCondition;
import com.ucpaas.sms.util.JsonUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:spring.xml"})
public class AgentInfoServiceTest {

    @Autowired
    private AgentInfoService agentInfoService;

    @Test
    public void findOEMAgentIdByBelongSales() throws Exception {
        DataAuthorityCondition dataAuthorityCondition = new DataAuthorityCondition();
        List<Long> ids = new ArrayList<>();
        ids.add(317L);
        dataAuthorityCondition.setIds(ids);
        Set<Integer> agentIds = agentInfoService.findOEMAgentIdByBelongSales(dataAuthorityCondition);
        System.err.println(JsonUtils.toJson(agentIds.size()));
    }

}