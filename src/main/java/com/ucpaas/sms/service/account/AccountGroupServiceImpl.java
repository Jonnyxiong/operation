package com.ucpaas.sms.service.account;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.ucpaas.sms.common.entity.R;
import com.ucpaas.sms.common.util.Collections3;
import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.entity.message.Account;
import com.ucpaas.sms.entity.message.AccountGroup;
import com.ucpaas.sms.exception.OperationException;
import com.ucpaas.sms.mapper.message.AccountGroupMapper;
import com.ucpaas.sms.mapper.message.AccountMapper;
import com.ucpaas.sms.model.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description 客户组
 * @author
 * @date 2017-07-25
 */
@Service
public class AccountGroupServiceImpl implements AccountGroupService {

	@Autowired
	private AccountGroupMapper accountGroupMapper;

	@Autowired
	private AccountMapper accountMapper;

	@Override
	@Transactional
	public int insert(AccountGroup model) {
		return this.accountGroupMapper.insert(model);
	}

	@Override
	@Transactional
	public int insertBatch(List<AccountGroup> modelList) {
		return this.accountGroupMapper.insertBatch(modelList);
	}

	@Override
	@Transactional
	public int delete(Integer accountGid) {
		AccountGroup model = this.accountGroupMapper.getByAccountGid(accountGid);
		if (model != null)
			return this.accountGroupMapper.delete(accountGid);
		return 0;
	}

	@Override
	@Transactional
	public int update(AccountGroup model) {
		AccountGroup old = this.accountGroupMapper.getByAccountGid(model.getAccountGid());
		if (old == null) {
			return 0;
		}
		return this.accountGroupMapper.update(model);
	}

	@Override
	@Transactional
	public int updateSelective(AccountGroup model) {
		AccountGroup old = this.accountGroupMapper.getByAccountGid(model.getAccountGid());
		if (old != null)
			return this.accountGroupMapper.updateSelective(model);
		return 0;
	}

	@Override
	@Transactional
	public AccountGroup getByAccountGid(Integer accountGid) {
		AccountGroup model = this.accountGroupMapper.getByAccountGid(accountGid);
		return model;
	}

	@Override
	@Transactional
	public Page queryList(Page page) {
		List<AccountGroup> list = this.accountGroupMapper.queryList(page);
		if (!Collections3.isEmpty(list)) {
			StringBuffer sb = new StringBuffer();
			for (AccountGroup group : list) {
				sb.setLength(0);
				List<Account> accounts = accountGroupMapper.getClientInfoByGroupId(group.getAccountGid());
				for (int i = 0; i < accounts.size(); i++) {
					if (sb.length() == 0) {
						sb.append(accounts.get(i).getClientid());
					} else {
						sb.append("，");
						sb.append(accounts.get(i).getClientid());
					}
				}
				group.setGroupClients(sb.toString());
			}
		}

		page.setData(list);
		return page;
	}

	@Override
	@Transactional
	public int count(Map<String, Object> params) {
		return this.accountGroupMapper.count(params);
	}

	@Override
	public R checkAccountGroup(AccountGroup accountGroup, Boolean isAdd) {
		if (accountGroup == null) {
			return R.error("客户组不能为空！");
		}

		if (StringUtils.isBlank(accountGroup.getAccountGname())) {
			return R.error("客户组名称不能为空");
		}

		if (accountGroup.getAccountGname().length() > 50) {
			return R.error("客户组名称长度必须介于 1 和 50 之间");
		}

		Set<Integer> sets = Sets.newHashSet();
		sets.add(0);
		sets.add(1);

		if (accountGroup.getType() != null && !sets.contains(accountGroup.getType())) {
			return R.error("客户类型必须是代理商子客户或直客");
		}

		if (Collections3.isEmpty(accountGroup.getClients())) {
			return R.error("客户组内的客户为空");
		}

		AccountGroup queryGroup = new AccountGroup();
		queryGroup.setAccountGname(accountGroup.getAccountGname());

		// 判断
		if (!isAdd) {
			if (accountGroup.getAccountGid() == null) {
				return R.error("客户组Id不能为空");
			}

			// 判断除了本组，其它组是否存在本次修改的名称
			queryGroup.setAccountGid(accountGroup.getAccountGid());
		}

		if (accountGroupMapper.isExistGname(queryGroup) > 0) {
			return R.error("该客户组已存在，请重新输入客户组");
		}

		return null;
	}

	@Transactional("message")
	@Override
	public R addAccountGroup(AccountGroup accountGroup) {
		accountGroup.setState(1);
		accountGroup.setCreateTime(Calendar.getInstance().getTime());
		accountGroup.setUpdateTime(accountGroup.getCreateTime());
		int addGroup = this.insert(accountGroup);

		// 添加关联关系
		int refCount = accountGroupMapper.insertAccountAssociationAccountGroup(accountGroup);

		if (addGroup > 0 && refCount == accountGroup.getClients().size()) {
			return R.ok("添加客户组成功");
		} else {
			return R.error("添加客户组失败");
		}
	}

	@Transactional("message")
	@Override
	public R modAccountGroup(AccountGroup accountGroup) {
		accountGroup.setUpdateTime(Calendar.getInstance().getTime());

		AccountGroup old = this.accountGroupMapper.getByAccountGid(accountGroup.getAccountGid());
		// 判断是是否修改
		boolean isMod = false;
		if (!old.getAccountGname().equals(accountGroup.getAccountGname())) {
			isMod = true;
		}
		if (old.getType() == null && accountGroup.getType() != null
				|| old.getType() != null && accountGroup.getType() == null) {
			isMod = true;
		}

		if (old.getType() != null && accountGroup.getType() != null && !old.getType().equals(accountGroup.getType()))
		{
			isMod = true;
		}

		boolean modGroupSuccess = true;
		if (isMod) {
			modGroupSuccess = this.accountGroupMapper.updateSelective(accountGroup) > 0;
		}

		// 原关联客户列表
		List<String> oldClients = accountGroupMapper.getClientIdsByGroupId(accountGroup.getAccountGid());

		// 新客户列表
		List<Account> addClients = Lists.newArrayList();
		// 删除的客户列表
		List<Account> delClients = Lists.newArrayList();

		for (Account account : accountGroup.getClients()) {
			if (oldClients.contains(account.getClientid())) {
				oldClients.remove(account.getClientid());
				continue;
			}

			addClients.add(account);
		}

		if (oldClients.size() > 0) {
			for (String clientid : oldClients) {
				Account account = new Account();
				account.setClientid(clientid);
				delClients.add(account);
			}
		}

		if (addClients.size() == 0 && delClients.size() == 0 && modGroupSuccess) {
			return R.ok("修改客户组成功");
		}

		int delCount = 0;
		if (delClients.size() > 0) {
			AccountGroup group = new AccountGroup();
			group.setAccountGid(accountGroup.getAccountGid());
			group.setClients(delClients);
			delCount = accountGroupMapper.deleteAccountAssociationAccountGroup(group);
		}

		int addCount = 0;
		if (addClients.size() > 0) {
			AccountGroup group = new AccountGroup();
			group.setCreateTime(accountGroup.getUpdateTime());
			group.setAccountGid(accountGroup.getAccountGid());
			group.setClients(addClients);
			addCount = accountGroupMapper.insertAccountAssociationAccountGroup(group);
		}

		if (delCount == delClients.size() && addCount == addClients.size() && modGroupSuccess) {
			return R.ok("修改客户组成功");
		} else {
			throw new OperationException("修改客户组失败");
		}
	}

	@Transactional("message")
	@Override
	public R delAccountGroup(Integer id) {
		AccountGroup group = accountGroupMapper.getByAccountGid(id);
		if (group == null) {
			return R.error("客户组不存在");
		}

		if (group.getState() == 0) {
			return R.ok("客户组已删除");
		}

		// 删除客户组
		int count = accountGroupMapper.delete(id);
		// 删除关联
		int count1 = accountGroupMapper.deleteAccountAssociationAccountGroup(group);

		if (count > 0 && count1 > 0) {
			return R.ok("客户组已删除");
		} else {
			throw new OperationException("客户组删除失败");
		}
	}

	@Override
	public AccountGroup getAccountGroup(Integer accountGid) {
		AccountGroup accountGroup = accountGroupMapper.getByAccountGid(accountGid);
		accountGroup.setClients(accountGroupMapper.getClientInfoByGroupId(accountGid));
		return accountGroup;
	}

	@Override
	public List<Account> getClientInfoList(Integer clientType) {
		return accountMapper.getAccountsForAccountGroup(clientType);
	}

	/**
	 * 根据clientid获取客户组信息,如果没有无则使用t_sms_account表中部分信息代替
	 * @param clientId
	 * @return
	 */
	@Override
	public Map getByClientId(String clientId){

        return accountGroupMapper.getByClientId(clientId);
	}
}
