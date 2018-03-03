package com.ucpaas.sms.service.common;

import java.util.List;
import java.util.Map;

import com.ucpaas.sms.entity.message.Role;
import com.ucpaas.sms.model.Menu;

/**
 * 菜单业务
 * 
 * @author xiejiaan
 */
public interface MenuService {

	/**
	 * 获取页面顶部1、2级菜单
	 * @param integer 
	 * 
	 * @return
	 */
	List<Menu> getHeaderMenu(Integer roleId);

	List<Menu> getHeaderMenu(List<Role> roles);
}
