package com.ucpaas.sms.mapper.accessSlave;

import com.jsmsframework.common.dto.JsmsPage;
import com.ucpaas.sms.entity.access.ClientFailReturn;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;


/**
 * @description 客户失败返回清单表
 * @author lpjLiu
 * @date 2017-10-11
 */
@Repository
public interface ClientFailReturnMapper {

    int insert(ClientFailReturn model);

    int insertBatch(List<ClientFailReturn> modelList);


    int update(ClientFailReturn model);

    int updateSelective(ClientFailReturn model);

    ClientFailReturn getById(Integer id);

    List<ClientFailReturn> queryList(JsmsPage<ClientFailReturn> page);

    List<ClientFailReturn> queryList1(JsmsPage<ClientFailReturn> page);

    List<ClientFailReturn> findList(ClientFailReturn model);

    int count(Map<String, Object> params);

}