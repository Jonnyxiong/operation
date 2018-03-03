package com.ucpaas.sms.mapper.message;

import com.ucpaas.sms.entity.message.Role;
import com.ucpaas.sms.entity.message.User;
import com.ucpaas.sms.entity.po.UserPo;
import com.ucpaas.sms.model.Page;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @description 用户表
 * @author huangwenjie
 * @date 2017-07-15
 */
@Repository
public interface UserMapper{

	int insert(User model);
	
	int insertBatch(List<User> modelList);
	
	int update(User model);
	
	int updateDept(User model);

	int updateSelective(User model);

	User getById(Long id);
	
	List<User> queryList(Page<User> page);
	
	int count(Map<String,Object> params);
	
	List<User> checkByMobile(@Param("mobile") String mobile, @Param("webId") String webId);

	List<UserPo> findUserListByDepartmentId(Integer departmentId);

	int getUserCountByDepartmentId(Integer departmentId);

	List<Role> findRoleInfoByUserId(Long id);

	List<Integer> findRoleIdByUserId(Long id);

	User checkEmailAndMobile(User user);

	int insertRoleAssociationUser(User model);

	int deleteRoleAssociationUser(User model);

	int disabled(Long id);

	int enabled(Long id);

	UserPo getUserForLogin(User user);

	String getUserStatusById(Long id);

	UserPo getUserAndDeptById(Long id);

	List<Long> findSalesOrBusinessByDeptId(@Param("user") User user, @Param("deptIds") List<Integer> deptIds);

	int updateLoginInfo(Long id);

	List<User> queryBelongInfo(@Param("userIds") Set userIds);

	List<Long> queryBelongIdByName(String name);

	User getUserEmailAndMobile(@Param("email")String email,@Param("mobile")String mobile);
}