package com.ucpaas.sms.mapper.message;

import com.ucpaas.sms.entity.message.Menu;
import com.ucpaas.sms.entity.message.Role;
import com.ucpaas.sms.model.Page;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface RoleMapper {

	/**
	 * 添加角色
	 * 
	 * @param role
	 * @return
	 */
	int insert(Role role);

	/**
	 * 检查角色名是否存在
	 * 
	 * @return
	 */
	int checkRoleName(Role role);

	int insertRoleMenu(Role role);

	/**
	 * 删除角色的菜单
	 * 
	 * @param roleId
	 * @return
	 */
	int deleteRoleMenu(Integer roleId);

	/**
	 * 删除角色
	 * 
	 * @param roleId
	 * @return
	 */
	int deleteRole(Integer roleId);

	/**
	 * 查询角色对应的用户数量
	 * 
	 * @param roleId
	 * @return
	 */
	int countRoleUser(Integer roleId);

	/**
	 * 删除角色对应的用户关系
	 * 
	 * @param roleId
	 * @return
	 */
	int deleteRoleUser(Integer roleId);

	int countRoleMenu(Integer roleId);

	/**
	 * 修改角色信息
	 * 
	 * @param role
	 * @return
	 */
	int update(Role role);	

	/**
	 * 更新角色状态
	 * 
	 * @param role
	 * @return
	 */
	int updateStatus(Role role);

	Role getById(Integer id);

	/** 获取全部角色名称 */
	List<Role> queryRoleList(String webId);

	/** 分页获取角色列表 */
	List<Role> queryList(Page<Role> page);

	List<Menu> queryRoleMenuList(Map params);

	List<Menu> queryMenuList(String webId);

	List<Role> findRoleList(Role role);
}
