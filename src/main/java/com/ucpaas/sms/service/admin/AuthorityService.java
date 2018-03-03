package com.ucpaas.sms.service.admin;

import java.util.List;
import java.util.Map;

import com.jsmsframework.user.entity.JsmsMenu;
import com.ucpaas.sms.entity.MenuButtonDTO;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.entity.message.Role;
import com.ucpaas.sms.model.PageContainer;

/**
 * 管理员中心-权限管理
 * 
 * @author xiejiaan
 */
public interface AuthorityService {

	/**
	 * 判断角色是否对url有访问权限<br/>
	 * 有权限的条件：1.没有配置菜单；2.分配了菜单，且菜单和角色状态是1
	 * 
	 * @param roleId
	 * @param url
	 * @return 当前选中的菜单
	 */
	Map<String, Object> isAuthority(int roleId, String url);

	/**
	 * 判断当前角色是否对menuId有访问权限
	 * 
	 * @param menuId
	 * @return
	 */
	boolean isAuthority(int menuId);
	
	/**
	 * 判断当前角色是否在<u:authority>标签中授权角色中
	 * @param roleIds
	 * @return
	 */
	boolean isRole(String roleIds);

	/**
	 * 分页查询权限
	 * 
	 * @param params
	 * @return
	 */
	PageContainer query(Map<String, String> params);

	/**
	 * 获取权限资料
	 * 
	 * @param roleId
	 * @return
	 */
	Map<String, Object> getAuthority(Integer roleId);
	
	
	/**
	 * 获取权限资料
	 * @return
	 */
	Map<String, Object> getAuthority(Map<String,String> paramsTemp);
	
	

	/**
	 * 保存管理权限，包括添加、修改
	 * 
	 * @param params
	 * @return
	 */
	Map<String, Object> saveAuthority(Map<String, String> params);

	/**
	 * 修改状态：恢复、删除
	 * 
	 * @param roleId
	 * @param status
	 * @return
	 */
	Map<String, Object> updateStatus(int roleId, int status, Long userId, String pageUrl, String ip);
	
	/**
	 * 删除权限
	 * 
	 * @param roleId
	 * @return
	 */
	Map<String, Object> deleteRole(int roleId, Long userId, String pageUrl, String ip);

	/**
	 * @param userId
	 * @Description: 判断用户是否 仅查看自己(数据权限)
	 * @Author: tanjiangqiang
	 * @Date: 2017/12/14 - 18:14
	 */
	public boolean isDataAuthority(Long userId);

	/**
	  * @Description: 判断角色是否拥有该菜单id的权限
	  * @Author: tanjiangqiang
	  * @Date: 2017/12/26 - 12:10
	  * @param roles 角色id
	  * @param menuId 菜单id
	  *
	  */
	boolean isMenuAuthority(List<Role> roles, String menuId);

	/**
	  * @Description: 根据父级菜单id获取该登录用户的按钮权限
	  * @Author: tanjiangqiang
	  * @Date: 2018/1/4 - 17:51
	  * @param userSession 用户信息
	  * @param menuId 菜单id
	  *
	  */
	List<MenuButtonDTO> getButtonAuthority(UserSession userSession, String menuId);

	JsmsMenu getJsmsMenu(String menuName);

}
