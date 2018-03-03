package com.ucpaas.sms.service.returnFail;


import com.jsmsframework.access.access.entity.JsmsClientFailReturn;
import com.jsmsframework.access.enums.RefundStateType;
import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.order.service.JsmsClientOrderFinanceService;
import com.ucpaas.sms.dto.ClientFailReturnVO;
import com.ucpaas.sms.entity.access.ClientFailReturn;
import com.ucpaas.sms.mapper.accessSlave.ClientFailReturnMapper;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.util.PageConvertUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @description 客户失败返回清单表
 * @author Niu.T
 * @date 2017-10-11
 */
@Service
public class ClientFailReturnServiceImpl implements ClientFailReturnService {

    @Autowired
    private ClientFailReturnMapper clientFailReturnMapper;
    @Autowired
    private JsmsClientOrderFinanceService jsmsClientOrderFinanceService;

    @Override
	@Transactional
    public int insert(ClientFailReturn model) {
        return this.clientFailReturnMapper.insert(model);
    }

    @Override
	@Transactional
    public int insertBatch(List<ClientFailReturn> modelList) {
        return this.clientFailReturnMapper.insertBatch(modelList);
    }

	@Override
	@Transactional
    public int update(ClientFailReturn model) {
		ClientFailReturn old = this.clientFailReturnMapper.getById(model.getId());
		if(old == null){
			return 0;
		}
		return this.clientFailReturnMapper.update(model);
    }

    @Override
	@Transactional
    public int updateSelective(ClientFailReturn model) {
		ClientFailReturn old = this.clientFailReturnMapper.getById(model.getId());
		if(old != null)
        	return this.clientFailReturnMapper.updateSelective(model);
		return 0;
    }

    @Override
    public ClientFailReturn getById(Integer id) {
        ClientFailReturn model = this.clientFailReturnMapper.getById(id);
		return model;
    }

    @Override
    @Transactional("message")
    public ResultVO returnSendFail(JsmsClientFailReturn clientFailReturn, List<Integer> waitUpdateFailIds, Long orderId, Long adminId,Date updateTime) {
        return jsmsClientOrderFinanceService.returnSendFail(clientFailReturn, waitUpdateFailIds, orderId, adminId, updateTime);
    }

    @Override
    @Transactional("message")
    public ResultVO returnOemSendFail(JsmsClientFailReturn clientFailReturn, List<Integer> waitUpdateFailIds, Integer agentId,Long orderId,Long orderNo,Long adminId, Date updateTime) {
        return jsmsClientOrderFinanceService.returnOemSendFail(clientFailReturn, waitUpdateFailIds, agentId, orderId,orderNo,adminId, updateTime);
    }

    @Override
    public PageContainer queryList(String clientid,Map params){
        String startDate = (String) params.get("startDate");
        String endDate = (String) params.get("endDate");
        if (StringUtils.isNotBlank(startDate)){
            params.put("startDate", Integer.valueOf(startDate.replace("-","")));
        }
        if (StringUtils.isNotBlank(endDate)){
            params.put("endDate", Integer.valueOf(endDate.replace("-","")));
        }

        JsmsPage jsmsPage = PageConvertUtil.paramToPage(params);
        if(StringUtils.isBlank(clientid))
            return PageConvertUtil.pageToContainer(jsmsPage);
        else
            params.put("clientid", clientid);
        params.put("refundState", RefundStateType.未退费.getValue());

        // Mod by lpjLiu 2018-01-09 统计表若存在多次修改归属销售后导致 计算失败未返还条数错误
        //List<ClientFailReturn> list = this.clientFailReturnMapper.queryList(jsmsPage);
        List<ClientFailReturn> list = this.clientFailReturnMapper.queryList1(jsmsPage);
        List<ClientFailReturnVO> dtoResult = new ArrayList<>();
        for (ClientFailReturn clientFailReturn : list) {
            ClientFailReturnVO temp = new ClientFailReturnVO();
            BeanUtils.copyProperties(clientFailReturn,temp);
            dtoResult.add(temp);
        }
        jsmsPage.setData(dtoResult);
        return PageConvertUtil.pageToContainer(jsmsPage);
    }

    @Override
    public List<ClientFailReturn> findList(ClientFailReturn model) {
        return clientFailReturnMapper.findList(model);
    }

    @Override
    public int count(Map<String,Object> params) {
		return this.clientFailReturnMapper.count(params);
    }

}
