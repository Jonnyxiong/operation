package com.ucpaas.sms.mapper.message;

import java.util.List;
import java.util.Map;

import com.ucpaas.sms.entity.po.DataAuthorityCondition;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import com.ucpaas.sms.entity.message.AgentInfo;
import com.ucpaas.sms.model.Page;

@Repository
public interface AgentInfoMapper {
	List<AgentInfo> findAgentInfoForAddClient();

	int insert(AgentInfo model);

	int insertBatch(List<AgentInfo> modelList);

	int update(AgentInfo model);

	int updateSelective(AgentInfo model);

	AgentInfo getByAgentId(Integer agentId);

	List<AgentInfo> queryList(Page<AgentInfo> page);

	int count(Map<String,Object> params);

	List<AgentInfo> findAgentInfoList(Integer agentType);
	List<AgentInfo> findAgentInfoList2(Integer agentType);

	AgentInfo getByAdminId(Integer adminId);
	//按权限获取获取代理商总数和本月新增代理个数((needQuerySaleIsNullData))
	public int getAgentNum(DataAuthorityCondition dataAuthorityCondition);

	/**
	  * @Description: 根据权限获取所属销售下或所属销售为空的的代理商
	  * @Author: tanjiangqiang
	  * @Date: 2017/12/18 - 10:40
	  * @param dataAuthorityCondition 数据权限参数
	  *
	  */
	List<AgentInfo> findAllListByBelongSales(DataAuthorityCondition dataAuthorityCondition);

	List<AgentInfo> findListByAgentIdAndAgentName(@Param("condition") String condition);

}