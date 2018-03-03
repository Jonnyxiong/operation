package com.ucpaas.sms.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucpaas.sms.mapper.message.OemDataConfig;
import com.ucpaas.sms.mapper.message.OemDataConfigMapper;
import com.ucpaas.sms.model.Page;

/**
 * @description OEM资料配置
 * @author huangwenjie
 * @date 2017-07-13
 */
@Service
public class OemDataConfigServiceImpl implements OemDataConfigService{

    @Autowired
    private OemDataConfigMapper oemDataConfigMapper;
    
    @Override
	@Transactional("message")
    public int insert(OemDataConfig model) {
        return this.oemDataConfigMapper.insert(model);
    }

    @Override
	@Transactional("message")
    public int insertBatch(List<OemDataConfig> modelList) {
        return this.oemDataConfigMapper.insertBatch(modelList);
    }
 
	@Override
	@Transactional("message")
    public int update(OemDataConfig model) {
		OemDataConfig old = this.oemDataConfigMapper.getById(model.getId());
		if(old == null){
			return 0;
		}
		return this.oemDataConfigMapper.update(model); 
    }

    @Override
	@Transactional("message")
    public int updateSelective(OemDataConfig model) {
		OemDataConfig old = this.oemDataConfigMapper.getById(model.getId());
		if(old != null)
        	return this.oemDataConfigMapper.updateSelective(model);
		return 0;
    }

    @Override
	@Transactional("message")
    public OemDataConfig getById(Integer id) {
        OemDataConfig model = this.oemDataConfigMapper.getById(id);
		return model;
    }

    @Override
	@Transactional("message")
    public Page queryList(Page page) {
        List<OemDataConfig> list = this.oemDataConfigMapper.queryList(page);
        page.setData(list);
        return page;
    }

    @Override
	@Transactional("message")
    public int count(Map<String,Object> params) {
		return this.oemDataConfigMapper.count(params);
    }
    
}
