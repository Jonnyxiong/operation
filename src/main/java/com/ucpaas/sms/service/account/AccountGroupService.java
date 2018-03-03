package com.ucpaas.sms.service.account;

import com.ucpaas.sms.common.entity.R;
import com.ucpaas.sms.entity.message.Account;
import com.ucpaas.sms.entity.message.AccountGroup;
import com.ucpaas.sms.model.Page;

import java.util.List;
import java.util.Map;

/**
 * @description 客户组
 * @author
 * @date 2017-07-25
 */
public interface AccountGroupService {

	int insert(AccountGroup model);

	int insertBatch(List<AccountGroup> modelList);

	int delete(Integer accountGid);

	int update(AccountGroup model);

	int updateSelective(AccountGroup model);

	AccountGroup getByAccountGid(Integer accountGid);

	Page queryList(Page page);

	int count(Map<String, Object> params);

	R checkAccountGroup(AccountGroup accountGroup, Boolean isAdd);

	R addAccountGroup(AccountGroup accountGroup);

	R modAccountGroup(AccountGroup accountGroup);

	R delAccountGroup(Integer id);

	AccountGroup getAccountGroup(Integer accountGid);

	List<Account> getClientInfoList(Integer clientType);

	/**
	 * 根据clientid获取客户组信息,如果没有无则使用t_sms_account表中部分信息代替
	 * @param clientId
	 * @return
	 */
	Map getByClientId(String clientId);


}
