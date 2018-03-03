package com.ucpaas.sms.mapper.message;

import com.ucpaas.sms.entity.message.AgentInfo;
import com.ucpaas.sms.entity.po.DataAuthorityCondition;
import com.ucpaas.sms.util.JsonUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:spring.xml"})
public class AgentInfoMapperTest {

    @Autowired
    private AgentInfoMapper agentInfoMapper;

    @Test
    public void findAllListByBelongSales() throws Exception {
        DataAuthorityCondition dataAuthorityCondition = new DataAuthorityCondition();
        List<Long> ids = new ArrayList<>();
        ids.add(317L);
        dataAuthorityCondition.setIds(ids);
        List<AgentInfo> agentInfoList =  agentInfoMapper.findAllListByBelongSales(dataAuthorityCondition);
        System.err.println(JsonUtils.toJson(agentInfoList));
        for (AgentInfo info: agentInfoList
             ) {
            if (!"5".equals(info.getAgentType().toString())){
                System.err.println(JsonUtils.toJson(info));
            }
        }
    }

}