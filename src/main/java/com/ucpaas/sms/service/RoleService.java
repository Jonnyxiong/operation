package com.ucpaas.sms.service;

import java.util.List;
import java.util.Map;

import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.entity.message.Menu;
import com.ucpaas.sms.entity.message.Role;

public interface RoleService {

	List<Role> queryRoleList(String webId);

	List<Menu> queryRoleMenuList(Map params);

	Role getById(Integer id);

	 List<Menu>  queryMenuList(String webId);

	ResultVO update(Role role);

}
