package com.ucpaas.sms.service.admin;

import java.util.List;
import java.util.Map;

import com.ucpaas.sms.common.entity.R;
import com.ucpaas.sms.entity.message.Department;
import com.ucpaas.sms.entity.message.Role;
import com.ucpaas.sms.entity.message.User;
import com.ucpaas.sms.entity.po.DeptTree;
import com.ucpaas.sms.entity.po.UserPo;
import com.ucpaas.sms.model.Page;

/**
 * @description 部门
 * @author
 * @date 2017-07-25
 */
public interface OfficeService {

	int insertBatch(List<Department> modelList);

	int delete(Integer departmentId);

	int update(Department model);

	int updateSelective(Department model);

	Department getByDepartmentId(Integer departmentId);

	Page queryList(Page page);

	int count(Map<String, Object> params);

	R checkDept(Department department, Boolean isAdd);

	R addDept(Department department);

	R modDept(Department department);

	R delDept(Integer departmentId);

	Department getDepartment(Integer departmentId);

	List<Role> findRoleList();

	List<Role> findCanSetRoleListByDepartmentId(Integer departmentId);

	List<Department> findLowerDepartmentList(Integer departmentId);

	List<UserPo> findUserListByDepartmentId(Integer departmentId);

	R checkUser(User user, Boolean isAdd);

	R addUser(User user);

	R modUser(User user);

	R disabledUser(Long id);

	R enabledUser(Long id);

	R adjustUserDept(User user);

	DeptTree getTree(Long id);

	UserPo getUserAndDeptById(Long id);

	List<Long> findSalesOrBusinessByDeptId(User user);

	/**
	  * @Description: 判断该销售名下是否存在子账户
	  * @Author: tanjiangqiang
	  * @Date: 2018/1/17 - 11:29
	  * @param userId
	  *
	  */
	boolean isClient(long userId);

	/**
	 * @Description: 判断该商务名下是否存在通道
	 * @Author: tanjiangqiang
	 * @Date: 2018/1/17 - 11:29
	 * @param userId
	 *
	 */
	boolean isChannel(long userId);
}
