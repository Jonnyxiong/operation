package com.ucpaas.sms.mapper.message;

import java.util.List;
import java.util.Map;

import com.ucpaas.sms.entity.message.Role;
import com.ucpaas.sms.entity.po.DeptTree;
import org.springframework.stereotype.Repository;

import com.ucpaas.sms.entity.message.Department;
import com.ucpaas.sms.model.Page;

/**
 * @description 部门
 * @author
 * @date 2017-07-25
 */
@Repository
public interface DepartmentMapper {

	int insert(Department model);

	int insertBatch(List<Department> modelList);

	int delete(Integer departmentId);

	int update(Department model);

	int updateSelective(Department model);

	Department getByDepartmentId(Integer departmentId);

	List<Department> queryList(Page<Department> page);

	int count(Map<String, Object> params);

	int getLevelByDepartmentId(Integer departmentId);

	int getUserCountByDepartmentId(Integer departmentId);

	int getDeptCountByDepartmentId(Integer departmentId);

	List<Integer> findRoleIdsByDeptId(Integer departmentId);

	int isExistName(Department model);

	int insertRoleAssociationDepartment(Department model);

	int deleteRoleAssociationDepartment(Department model);

	List<Role> findOwnerRoleInfoByDepartmentId(Integer departmentId);

	List<Department> findLowerDepartmentListByDepartmentId(Integer departmentId);

	List<DeptTree> findAllDept();
}