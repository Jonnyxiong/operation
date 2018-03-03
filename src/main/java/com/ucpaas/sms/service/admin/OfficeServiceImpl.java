package com.ucpaas.sms.service.admin;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.jsmsframework.channel.service.JsmsChannelService;
import com.jsmsframework.common.enums.AccountStatusEnum;
import com.jsmsframework.common.util.JsonUtil;
import com.jsmsframework.finance.entity.JsmsSaleCreditAccount;
import com.jsmsframework.finance.service.JsmsSaleCreditAccountService;
import com.jsmsframework.user.service.JsmsAccountService;
import com.ucpaas.sms.common.entity.R;
import com.ucpaas.sms.common.util.*;
import com.ucpaas.sms.entity.message.Department;
import com.ucpaas.sms.entity.message.Role;
import com.ucpaas.sms.entity.message.User;
import com.ucpaas.sms.entity.po.DataAuthorityCondition;
import com.ucpaas.sms.entity.po.DeptTree;
import com.ucpaas.sms.entity.po.UserPo;
import com.ucpaas.sms.enums.DataAuthority;
import com.ucpaas.sms.exception.OperationException;
import com.ucpaas.sms.mapper.message.DepartmentMapper;
import com.ucpaas.sms.mapper.message.RoleMapper;
import com.ucpaas.sms.mapper.message.UserMapper;
import com.ucpaas.sms.model.Page;
import com.ucpaas.sms.service.account.AccountService;
import com.ucpaas.sms.util.AgentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @description 部门
 * @author
 * @date 2017-07-25
 */
@Service
public class OfficeServiceImpl implements OfficeService {

	private static Logger logger = LoggerFactory.getLogger(OfficeServiceImpl.class);

	@Autowired
	private DepartmentMapper departmentMapper;

	@Autowired
	private RoleMapper roleMapper;

	@Autowired
	private UserMapper userMapper;
	@Autowired
	private JsmsSaleCreditAccountService jsmsSaleCreditAccountService;
	@Autowired
	private AccountService accountService;
	@Autowired
	private JsmsChannelService jsmsChannelService;

	@Override
	@Transactional("message")
	public int insertBatch(List<Department> modelList) {
		return this.departmentMapper.insertBatch(modelList);
	}

	@Override
	@Transactional("message")
	public int delete(Integer departmentId) {
		Department model = this.departmentMapper.getByDepartmentId(departmentId);
		if (model != null)
			return this.departmentMapper.delete(departmentId);
		return 0;
	}

	@Override
	@Transactional("message")
	public int update(Department model) {
		Department old = this.departmentMapper.getByDepartmentId(model.getDepartmentId());
		if (old == null) {
			return 0;
		}
		return this.departmentMapper.update(model);
	}

	@Override
	@Transactional("message")
	public int updateSelective(Department model) {
		Department old = this.departmentMapper.getByDepartmentId(model.getDepartmentId());
		if (old != null)
			return this.departmentMapper.updateSelective(model);
		return 0;
	}

	@Override
	public Department getByDepartmentId(Integer departmentId) {
		Department model = this.departmentMapper.getByDepartmentId(departmentId);
		return model;
	}

	@Override
	public Page queryList(Page page) {
		List<Department> list = this.departmentMapper.queryList(page);
		page.setData(list);
		return page;
	}

	@Override
	public int count(Map<String, Object> params) {
		return this.departmentMapper.count(params);
	}

	@Override
	public R checkDept(Department department, Boolean isAdd) {
		if (department == null) {
			return R.error("部门不能为空！");
		}

		if (StringUtils.isBlank(department.getDepartmentName())) {
			return R.error("部门名称不能为空");
		}

		if (department.getDepartmentName().length() > 10) {
			return R.error("部门名称长度必须介于 1 和 10 之间");
		}

		if (department.getParentId() == null) {
			return R.error("上级部门不能为空");
		}

		if (Collections3.isEmpty(department.getRoles())) {
			return R.error("部门角色不能为空");
		}

		String msg = "";
		for (Role role : department.getRoles()) {
			if (role.getId() == null) {
				msg = "角色ID不能为空";
				break;
			}
		}
		if (StringUtils.isNotBlank(msg)) {
			return R.error(msg);
		}

		// 判断同级部门是否存在相同的名称
		Department dept = new Department();
		dept.setDepartmentName(department.getDepartmentName());
		dept.setParentId(department.getParentId());

		// 判断
		if (!isAdd) {
			if (department.getDepartmentId() == null) {
				return R.error("部门Id不能为空");
			}

			// 判断除了本组，其它组是否存在本次修改的名称
			dept.setDepartmentId(department.getDepartmentId());
		}

		if (departmentMapper.isExistName(dept) > 0) {
			return R.error("该部门已存在，请重新输入部门名称");
		}

		return null;
	}

	@Transactional("message")
	@Override
	public R addDept(Department department) {
		department.setState(1);
		department.setCreateTime(Calendar.getInstance().getTime());
		department.setUpdateTime(department.getCreateTime());

		// 查询父级部门的级别
		int parentLevel = departmentMapper.getLevelByDepartmentId(department.getParentId());

		// 设置本级部门的级别+1
		int selfLevel = parentLevel + 1;
		department.setLevel(selfLevel);

		// 添加部门
		int addDept = departmentMapper.insert(department);

		// 添加关联关系
		int refCount = departmentMapper.insertRoleAssociationDepartment(department);

		if (addDept > 0 && refCount == department.getRoles().size()) {
			return R.ok("添加部门成功");
		} else {
			// return R.error("添加部门失败");
			throw new OperationException("添加部门失败");
		}
	}

	private void deleteDepartmentRole(Department department) {
		// 删除部门的角色
		departmentMapper.deleteRoleAssociationDepartment(department);

		// 删除部门成员的角色
		List<UserPo> users = userMapper.findUserListByDepartmentId(department.getDepartmentId());
		for (UserPo user : users) {
			user.setId(user.getId());
			user.setRoles(department.getRoles());
			userMapper.deleteRoleAssociationUser(user);
		}

		// 删除子部门角色、及子部门的成员角色
		List<Department> departments = departmentMapper
				.findLowerDepartmentListByDepartmentId(department.getDepartmentId());
		for (Department dept : departments) {
			dept.setRoles(department.getRoles());
			deleteDepartmentRole(dept);
		}
	}

	@Transactional("message")
	@Override
	public R modDept(Department department) {
		department.setUpdateTime(Calendar.getInstance().getTime());

		Department old = departmentMapper.getByDepartmentId(department.getDepartmentId());

		// 判断是是否修改
		boolean isMod = false;
		if (!old.getDepartmentName().equals(department.getDepartmentName())) {
			isMod = true;
		}

		boolean modSuccess = true;
		if (isMod) {
			modSuccess = departmentMapper.updateSelective(department) > 0;
		}

		// 原关联角色列表
		List<Integer> oldRoles = departmentMapper.findRoleIdsByDeptId(department.getDepartmentId());

		// 新角色列表
		List<Role> addRoles = Lists.newArrayList();
		// 删除的角色列表
		List<Role> delRoles = Lists.newArrayList();

		for (Role role : department.getRoles()) {
			if (oldRoles.contains(role.getId())) {
				oldRoles.remove(role.getId());
				continue;
			}

			addRoles.add(role);
		}

		if (oldRoles.size() > 0) {
			for (Integer roleId : oldRoles) {
				Role role = new Role();
				role.setId(roleId);
				delRoles.add(role);
			}
		}

		if (addRoles.size() == 0 && delRoles.size() == 0 && modSuccess) {
			return R.ok("修改部门成功");
		}

		int delCount = 0;
		if (delRoles.size() > 0) {
			Department dept = new Department();
			dept.setDepartmentId(department.getDepartmentId());
			dept.setRoles(delRoles);
			// delCount =
			// departmentMapper.deleteRoleAssociationDepartment(dept);

			// 部门角色删除后，删除子部门的角色，删除成员的角色、删除子部门的角色
			deleteDepartmentRole(dept);
			delCount = delRoles.size();
		}

		int addCount = 0;
		if (addRoles.size() > 0) {
			Department dept = new Department();
			dept.setCreateTime(department.getUpdateTime());
			dept.setDepartmentId(department.getDepartmentId());
			dept.setRoles(addRoles);
			addCount = departmentMapper.insertRoleAssociationDepartment(dept);
		}

		if (delCount == delRoles.size() && addCount == addRoles.size() && modSuccess) {
			return R.ok("修改部门成功");
		} else {
			throw new OperationException("修改部门失败");
		}
	}

	@Transactional("message")
	@Override
	public R delDept(Integer departmentId) {
		Department department = departmentMapper.getByDepartmentId(departmentId);
		if (department == null) {
			return R.error("部门不存在");
		}

		if (department.getState() == 0) {
			return R.ok("部门已删除");
		}

		// 检测是否有成员
		int userCount = departmentMapper.getUserCountByDepartmentId(departmentId);
		if (userCount > 0) {
			return R.error("部门下有成员，不能进行删除");
		}

		// 检测是否有子部门
		int dpetCount = departmentMapper.getDeptCountByDepartmentId(departmentId);
		if (dpetCount > 0) {
			return R.error("部门下有子部门，不能进行删除");
		}

		// 删除部门
		int count = departmentMapper.delete(departmentId);

		// 删除关联
		int count1 = departmentMapper.deleteRoleAssociationDepartment(department);

		if (count > 0 && count1 > 0) {
			return R.ok("部门已删除");
		} else {
			throw new OperationException("部门删除失败");
		}
	}

	/**
	 * 根据部门ID获取部门信息
	 * 
	 * @param departmentId
	 */
	@Override
	public Department getDepartment(Integer departmentId) {
		Department department = departmentMapper.getByDepartmentId(departmentId);
		department.setRoles(departmentMapper.findOwnerRoleInfoByDepartmentId(departmentId));
		return department;
	}

	/**
	 * 获取所有的运营品台的角色列表
	 * 
	 */
	@Override
	public List<Role> findRoleList() {
		Role role = new Role();
		role.setWebId(3);
		role.setStatus("1");
		return roleMapper.findRoleList(role);
	}

	/**
	 * 根据部门ID获取部门可设置的所有角色列表
	 * 
	 * @param departmentId
	 */
	@Override
	public List<Role> findCanSetRoleListByDepartmentId(Integer departmentId) {
		Department department = departmentMapper.getByDepartmentId(departmentId);

		if (department == null) {
			throw new OperationException("部门不存在");
		}

		// 级别为0的为云之讯， 查询所有的角色，级别为1的部门的可设置角色直接继承云之讯，所以级别为1也要查询所有的角色
		if (department.getLevel() == 0 || department.getLevel() == 1) {
			return findRoleList();
		}

		return departmentMapper.findOwnerRoleInfoByDepartmentId(department.getParentId());
	}

	/**
	 * 获取下级部门
	 * 
	 * @param departmentId
	 */
	@Override
	public List<Department> findLowerDepartmentList(Integer departmentId) {
		List<Department> departments = departmentMapper.findLowerDepartmentListByDepartmentId(departmentId);
		for (Department department : departments) {
			department.setRoles(departmentMapper.findOwnerRoleInfoByDepartmentId(department.getDepartmentId()));
		}
		return departments;
	}

	/**
	 * 获取部门成员
	 * 
	 * @param departmentId
	 */
	@Override
	public List<UserPo> findUserListByDepartmentId(Integer departmentId) {
		List<UserPo> users = userMapper.findUserListByDepartmentId(departmentId);
		// 查询角色
		for (User user : users) {
			user.setRoles(userMapper.findRoleInfoByUserId(user.getId()));
		}
		return users;
	}

	@Override
	public R checkUser(User user, Boolean isAdd) {
		if (user == null) {
			return R.error("成员不能为空！");
		}

		if (StringUtils.isBlank(user.getEmail()) || user.getEmail().length() > 100) {
			return R.error("帐号长度必须介于 1 和 100 之间");
		}

		if (!RegexUtils.checkEmail(user.getEmail())) {
			return R.error("帐号格式错误，请输入正确的邮箱地址");
		}

		if (StringUtils.isBlank(user.getRealname()) || user.getRealname().length() > 10) {
			return R.error("姓名长度必须介于 1 和 10 之间");
		}

		if (StringUtils.isBlank(user.getMobile())) {
			return R.error("手机号码不能为空");
		}

		if (user.getMobile().length() > 11 || !AgentUtils.isMobileNO(user.getMobile())) {
			return R.error("手机号码格式错误，请输入正确的手机号码");
		}

		if (isAdd) {
			user.setId(null);
			if (StringUtils.isBlank(user.getPassword())) {
				return R.error("登录密码不能为空");
			}
		}

		if (StringUtils.isNotBlank(user.getPassword()) && user.getPassword().length() > 12) {
			return R.error("登录密码长度必须介于 1 和 12 之间");
		}

		Set<Integer> sets = Sets.newHashSet();
		sets.add(0);
		sets.add(1);
		if (user.getDataAuthority() == null || !sets.contains(user.getDataAuthority())) {
			return R.error("数据权限只能选择仅看自己数据或所在部门及下级部门");
		}

		// 判断角色是否选择
		if (Collections3.isEmpty(user.getRoles())) {
			return R.error("角色不能为空");
		}

		String msg = null;
		for (Role role : user.getRoles()) {
			if (role.getId() == null) {
				msg = "角色ID不能为空";
				break;
			}
		}
		if (msg != null) {
			return R.error(msg);
		}

		// 判断手机号码或邮箱是否已存在
		User check = userMapper.checkEmailAndMobile(user);
		if (null != check) {
			if (StringUtils.isNotBlank(check.getEmail())) {
				return R.error("邮箱已经被注册");
			}

			if (StringUtils.isNoneBlank(check.getMobile())) {
				return R.error("手机已经被注册");
			}
		}
		return null;
	}

	private boolean checkUserRole(User user, List<Role> deptRoles) {
		if (Collections3.isEmpty(deptRoles) || user == null || Collections3.isEmpty(user.getRoles())) {
			return false;
		}
		Set<Integer> deptRoleIds = Sets.newHashSet();
		for (Role role : deptRoles) {
			deptRoleIds.add(role.getId());
		}

		boolean result = true;
		for (Role role : user.getRoles()) {
			if (!deptRoleIds.contains(role.getId())) {
				result = false;
				break;
			}
		}
		return result;
	}

	@Override
	@Transactional("message")
	public R addUser(User user) {
		JsmsSaleCreditAccount jsmsSaleCreditAccount = new JsmsSaleCreditAccount();
		int addSaleCreditAccount = 0;
		R r = this.checkUser(user, true);
		if (r != null) {
			return r;
		}

		// 查询部门的
		if (user.getDepartmentId() == null) {
			return R.error("部门不能为空");
		}

		List<Role> deptRoles = this.findCanSetRoleListByDepartmentId(user.getDepartmentId());
		if (!checkUserRole(user, deptRoles)) {
			return R.error("添加成员失败，可能的原因：<br/>1、成员为空；<br/>2.成员角色为空<br/>3.部门的角色为空<br/>4.成员的角色在部门中不存在");
		}

		user.setWebId(3);
		user.setCreateDate(Calendar.getInstance().getTime());
		user.setUpdateDate(user.getCreateDate());
		user.setPassword(MD5.md5(SecurityUtils.encryptMD5(user.getPassword())));

		// 添加部门
		int addUser = userMapper.insert(user);

		// 添加关联关系
		int refCount = userMapper.insertRoleAssociationUser(user);
		if (user.getRoles().size() == 1) {
			if (roleMapper.getById(user.getRoles().get(0).getId()).getRoleName().equals("销售人员") && addUser > 0) {
				User userNew = userMapper.getUserEmailAndMobile(user.getEmail(), user.getMobile());
				if (userNew != null) {
					jsmsSaleCreditAccount.setSaleId(userNew.getId());
					addSaleCreditAccount = jsmsSaleCreditAccountService.insert(jsmsSaleCreditAccount);
				} else {
					return R.error("添加销售失败");
				}
			}
		}
		if (addUser > 0 && refCount == user.getRoles().size()) {
			if (user.getRoles().size() == 1) {
				if (roleMapper.getById(user.getRoles().get(0).getId()).getRoleName().equals("销售人员")) {
					if (addSaleCreditAccount > 0) {
						return R.ok("添加销售成功");
					} else {
						// return R.ok("添加销售失败");
						throw new OperationException("添加销售失败");

					}
				} else {
					return R.ok("添加成员成功");
				}
			} else {
				return R.ok("添加成员成功");
			}
		} else {
			// return R.error("添加成员失败");
			throw new OperationException("添加成员失败");
		}
	}

	private R doModUser(User user, boolean isModUser, boolean isAdjust, int addSaleCreditAccount) {
		boolean modSuccess = true;
		if (isModUser) {
			user.setUpdateDate(Calendar.getInstance().getTime());
			modSuccess = isAdjust ? userMapper.updateDept(user) > 0 : userMapper.updateSelective(user) > 0;
		}

		// 原关联角色列表
		List<Integer> oldRoles = userMapper.findRoleIdByUserId(user.getId());

		// 新角色列表
		List<Role> addRoles = Lists.newArrayList();
		// 删除的角色列表
		List<Role> delRoles = Lists.newArrayList();

		for (Role role : user.getRoles()) {
			if (oldRoles.contains(role.getId())) {
				oldRoles.remove(role.getId());
				continue;
			}

			addRoles.add(role);
		}

		if (oldRoles.size() > 0) {
			for (Integer roleId : oldRoles) {
				Role role = new Role();
				role.setId(roleId);
				delRoles.add(role);
			}
		}

		if (addRoles.size() == 0 && delRoles.size() == 0 && modSuccess && addSaleCreditAccount > 0) {
			return isAdjust ? R.ok("成员调整部门成功") : R.ok("修改成员成功");
		}

		int delCount = 0;
		if (delRoles.size() > 0) {
			User dealUser = new User();
			dealUser.setId(user.getId());
			dealUser.setRoles(delRoles);
			delCount = userMapper.deleteRoleAssociationUser(dealUser);
		}

		int addCount = 0;
		if (addRoles.size() > 0) {
			User dealUser = new User();
			dealUser.setId(user.getId());
			dealUser.setRoles(addRoles);
			addCount = userMapper.insertRoleAssociationUser(dealUser);
		}

		if (delCount == delRoles.size() && addCount == addRoles.size() && modSuccess && addSaleCreditAccount > 0) {
			return isAdjust ? R.ok("成员调整部门成功") : R.ok("修改成员成功");
		} else {
			throw new OperationException(isAdjust ? "成员调整部门失败" : "修改成员失败");
		}
	}

	private boolean isModUser(User user, User old) {
		if (StringUtils.isNotBlank(user.getPassword())) {
			if (!old.getPassword().equals(user.getPassword())) {
				return true;
			}
		}

		if (!user.getRealname().equals(old.getRealname())) {
			return true;
		}

		if (!user.getMobile().equals(old.getMobile())) {
			return true;
		}

		if (!user.getDataAuthority().equals(old.getDataAuthority())) {
			return true;
		}

		return false;
	}

	@Override
	@Transactional("message")
	public R modUser(User user) {
		R r = this.checkUser(user, false);
		if (r != null) {
			return r;
		}

		// 查询部门的
		if (user.getDepartmentId() == null) {
			return R.error("部门不能为空");
		}

		List<Role> deptRoles = this.findCanSetRoleListByDepartmentId(user.getDepartmentId());
		if (!checkUserRole(user, deptRoles)) {
			return R.error("修改成员失败，可能的原因：<br/>1、成员为空；<br/>2.成员角色为空<br/>3.部门的角色为空<br/>4.成员的角色在部门中不存在");
		}

		User old = userMapper.getById(user.getId());

		// 部门
		if (!old.getDepartmentId().equals(user.getDepartmentId())) {
			return R.error("当前为编辑成员，不能调整部门");
		}

		// 若修改了密码，进行加密
		if (StringUtils.isNotBlank(user.getPassword())) {
			user.setPassword(MD5.md5(SecurityUtils.encryptMD5(user.getPassword())));
		}

		// 判断是是否修改
		boolean isMod = isModUser(user, old);

		return doModUser(user, isMod, false, 1);
	}

	@Override
	@Transactional("message")
	public R disabledUser(Long id) {
		userMapper.disabled(id);
		return R.ok("禁用成员成功");
	}

	@Override
	@Transactional("message")
	public R enabledUser(Long id) {
		userMapper.enabled(id);
		return R.ok("启用成员成功");
	}

	@Override
	@Transactional("message")
	public R adjustUserDept(User user) {
		JsmsSaleCreditAccount jsmsSaleCreditAccount = new JsmsSaleCreditAccount();
		User old = userMapper.getById(user.getId());
		int oldRoleName = 0;
		int newRoleName = 0;
		int addSaleCreditAccount = 0;
		int departmentRoleName = 0;
		if (user == null) {
			return R.error("成员不能为空");
		}

		if (user.getId() == null) {
			return R.error("成员Id不能为空");
		}

		if (user.getDepartmentId() == null) {
			return R.error("成员部门不能为空");
		}

		if (Collections3.isEmpty(user.getRoles())) {
			return R.error("角色不能为空");
		}

		String msg = null;
		for (Role role : user.getRoles()) {
			if (role.getId() == null) {
				msg = "角色ID不能为空";
				break;
			}
		}

		if (msg != null) {
			return R.error(msg);
		}

		// 查询用户原有角色
		List<UserPo> userPo = this.findUserListByDepartmentId(old.getDepartmentId());
		if (!Collections3.isEmpty(userPo)) {
			for (UserPo po : userPo) {
				if (("销售人员").equals(po.getRolesAsStr()) && po.getId().intValue() == user.getId().intValue()) {
					oldRoleName = 1;
					break;
				}
			}
		}

		// 调整部门后的角色
		if (!Collections3.isEmpty(user.getRoles())) {
			for (Role r : user.getRoles()) {
				Role role = roleMapper.getById(r.getId());
				if (("销售人员").equals(role.getRoleName())) {
					newRoleName = 1;
					break;
				}
			}
		}

		// 调整到当前部门所拥有的角色
		List<Role> deptRoles = departmentMapper.findOwnerRoleInfoByDepartmentId(user.getDepartmentId());
		if (!Collections3.isEmpty(deptRoles)) {
			for (Role r : deptRoles) {
				if (("销售人员").equals(r.getRoleName())) {
					departmentRoleName = 1;
					break;
				}
			}
		} else {
			Department dept = departmentMapper.getByDepartmentId(user.getDepartmentId());
			// 云之讯拥有所有角色
			if (dept.getLevel() == 0) {
				deptRoles = findRoleList();
				departmentRoleName = 1;
			}
		}

		// 调整部门时1、若是销售人员调整,调整的部门必须要有销售且不能转为其他角色只能转销售
		if (oldRoleName > 0) {
			if (departmentRoleName > 0) {
				if (newRoleName == 1) {
					if (user.getRoles().size() > 1) {
						return R.error("销售人员只能转销售人员");
					}
				} else {
					return R.error("销售人员只能转销售人员");
				}
			} else {
				return R.error("销售人员调整的部门必须有销售角色");
			}
			addSaleCreditAccount = 1;
		}

		// 调整部门时2、其他角色调整,若是转为销售要更新到销售授信帐户表 t_sms_sale_credit_account
		if (oldRoleName == 0) {
			if (newRoleName == 1) {
				if (user.getRoles().size() == 1) {
					jsmsSaleCreditAccount.setSaleId(user.getId());
					addSaleCreditAccount = jsmsSaleCreditAccountService.insert(jsmsSaleCreditAccount);
				} else {
					return R.error("不能存在既是销售人员又是其他人员的用户");
				}
			} else {
				addSaleCreditAccount = 1;
			}
		}

		if (!checkUserRole(user, deptRoles)) {
			return R.error("修改成员失败，可能的原因：<br/>1、成员为空；<br/>2.成员角色为空<br/>3.部门的角色为空<br/>4.成员的角色在部门中不存在");
		}

		boolean isMod = false;
		if (!old.getDepartmentId().equals(user.getDepartmentId())) {
			isMod = true;
		}

		return doModUser(user, isMod, true, addSaleCreditAccount);
	}

	@Override
	public DeptTree getTree(Long id) {
		// 查询用户
		UserPo userPo = userMapper.getUserAndDeptById(id);
		if (userPo.getDataAuthority() == DataAuthority.仅看自己数据.getValue()) {
			return null;
		}

		List<DeptTree> trees = departmentMapper.findAllDept();
		int rootIndex = -1;
		for (int i = 0; i < trees.size(); i++) {
			if (trees.get(i).getDeptId() == userPo.getDepartmentId()) {
				rootIndex = i;
				break;
			}
		}

		// 找到根节点删除
		DeptTree root = trees.get(rootIndex);
		setSubTree(root, trees);

		return root;
	}

	private void setSubTree(DeptTree deptTree, List<DeptTree> trees) {
		List<DeptTree> subTrees = Lists.newArrayList();
		Iterator<DeptTree> iterator = trees.iterator();
		while (iterator.hasNext()) {
			DeptTree temp = iterator.next();
			if (temp.getParentId() == deptTree.getDeptId()) {
				subTrees.add(temp);
				setSubTree(temp, trees);
			}
		}
		deptTree.setSubDepts(subTrees);

		// 查询成员
		int count = userMapper.getUserCountByDepartmentId(deptTree.getDeptId());
		deptTree.setUserCount(count);
	}

	@Override
	public UserPo getUserAndDeptById(Long id) {
		return userMapper.getUserAndDeptById(id);
	}

	private List<Integer> getAllDeptIdByDeptId(Integer id) {
		List<DeptTree> trees = departmentMapper.findAllDept();
		int rootIndex = -1;
		for (int i = 0; i < trees.size(); i++) {
			if (trees.get(i).getDeptId() == id) {
				rootIndex = i;
				break;
			}
		}

		List<Integer> deptIds = Lists.newArrayList();
		Integer deptId = trees.get(rootIndex).getDeptId();
		deptIds.add(deptId);
		setSubId(deptId, trees, deptIds);
		return deptIds;
	}

	private void setSubId(Integer deptId, List<DeptTree> trees, List<Integer> deptIds) {
		Iterator<DeptTree> iterator = trees.iterator();
		while (iterator.hasNext()) {
			DeptTree temp = iterator.next();
			if (temp.getParentId() == deptId) {
				deptIds.add(temp.getDeptId());
				setSubId(temp.getDeptId(), trees, deptIds);
			}
		}
	}

	@Override
	public List<Long> findSalesOrBusinessByDeptId(User user) {
		List<Integer> deptIds = getAllDeptIdByDeptId(user.getDepartmentId());
		return userMapper.findSalesOrBusinessByDeptId(user, deptIds);
	}

	@Override
	public boolean isClient(long userId) {
		int count = accountService.getCountByBelongSale(userId, AccountStatusEnum.注销.getValue());
		return count <= 0 ? false : true;
	}

	@Override
	public boolean isChannel(long userId) {
		Map params = new HashMap(1);
		params.put("belongBusiness", userId);
		int count = jsmsChannelService.count(params);
		return count <= 0 ? false : true;
	}
}
