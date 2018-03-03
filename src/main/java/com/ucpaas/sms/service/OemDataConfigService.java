package com.ucpaas.sms.service;

import java.util.List;
import java.util.Map;

import com.ucpaas.sms.mapper.message.OemDataConfig;
import com.ucpaas.sms.model.Page;

/**
 * @description OEM资料配置
 * @author huangwenjie
 * @date 2017-07-13
 */
public interface OemDataConfigService {

    public int insert(OemDataConfig model);
    
    public int insertBatch(List<OemDataConfig> modelList);
    
    
    public int update(OemDataConfig model);
    
    public int updateSelective(OemDataConfig model);
    
    public OemDataConfig getById(Integer id);
    
    public Page queryList(Page page);
    
    public int count(Map<String,Object> params);
    
}
