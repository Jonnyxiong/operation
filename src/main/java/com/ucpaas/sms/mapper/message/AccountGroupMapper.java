package com.ucpaas.sms.mapper.message;

import com.ucpaas.sms.entity.message.Account;
import com.ucpaas.sms.entity.message.AccountGroup;
import com.ucpaas.sms.model.Page;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @description 客户组
 * @author
 * @date 2017-07-25
 */
@Repository
public interface AccountGroupMapper {

	int insert(AccountGroup model);

	int insertBatch(List<AccountGroup> modelList);

	int delete(Integer accountGid);

	int update(AccountGroup model);

	int updateSelective(AccountGroup model);

	AccountGroup getByAccountGid(Integer accountGid);

	List<AccountGroup> queryList(Page<AccountGroup> page);

	int count(Map<String, Object> params);

	int isExistGname(AccountGroup model);

	List<String> getClientIdsByGroupId(Integer accountGid);

	List<Account> getClientInfoByGroupId(Integer accountGid);

	int insertAccountAssociationAccountGroup(AccountGroup model);

	int deleteAccountAssociationAccountGroup(AccountGroup model);

	/**
	 * 根据clientid获取客户组信息,如果没有无则使用t_sms_account表中部分信息代替
	 */
	Map getByClientId(String clientId);
}