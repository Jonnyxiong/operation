package com.ucpaas.sms.util;

import com.google.common.collect.Lists;
import com.ucpaas.sms.common.util.Collections3;
import com.ucpaas.sms.common.util.SpringContextUtils;
import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.controller.AutoTemplate.ComTemplateController;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.entity.message.AgentInfo;
import com.ucpaas.sms.entity.message.Role;
import com.ucpaas.sms.entity.message.User;
import com.ucpaas.sms.entity.po.DataAuthorityCondition;
import com.ucpaas.sms.entity.po.UserPo;
import com.ucpaas.sms.enums.DataAuthority;
import com.ucpaas.sms.exception.OperationException;
import com.ucpaas.sms.mapper.message.AgentInfoMapper;
import com.ucpaas.sms.model.Menu;
import com.ucpaas.sms.model.Page;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.admin.OfficeService;
import com.ucpaas.sms.service.common.MenuService;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by liulipengju on 2017/5/12. 公共的
 */
public class AgentUtils {

	private static OfficeService officeService = SpringContextUtils.getBean(OfficeService.class);
	private static MenuService menuService = SpringContextUtils.getBean(MenuService.class);
	private static AgentInfoMapper agentInfoMapper = SpringContextUtils.getBean(AgentInfoMapper.class);
	private static final Logger logger = LoggerFactory.getLogger(AgentUtils.class);

	/**
	  * @Description:查询代理商信息
	  * @Author: tanjiangqiang
	  * @Date: 2017/11/15 - 10:25
	  * @param agentId
	  *
	  */
	public static AgentInfo queryAgentInfoByAgentId(String agentId) {
		return agentInfoMapper.getByAgentId(Integer.parseInt(agentId));
	}

	/**
	 * 查询所有销售和商务
	 * 
	 * @param userId
	 * @return
	 */
	public static DataAuthorityCondition getDataAuthorityCondition(Long userId) {
		return getDataAuthorityCondition(userId, true, true);
	}

	/**
	 * 判断是否拥有销售人员角色
	 * @param user
	 * @return
	 */
	public  static Boolean isSaleRole(UserSession user){
		//单个角色
		if("销售人员".equals(user.getRoleName())){
			return  true;
		}
		for (Role role : user.getRoles()) {
			if("销售人员".equals(role.getRoleName())){
				return  true;
			}
		}

		return  false;
	}


	/**
	 *判断是否拥有财务人员角色
	 * @param user
	 * @return
	 */
	public  static Boolean isFinacorRole(UserSession user){
		//单个角色
		if("财务人员".equals(user.getRoleName())){
			return  true;
		}
		for (Role role : user.getRoles()) {
			if("财务人员".equals(role.getRoleName())){
				return  true;
			}
		}

		return  false;
	}

	/**
	 * 根据参数查询归属销售和归属商务
	 * 
	 * @param userId
	 *            用户id
	 * @param querySale
	 *            查询归属销售
	 * @param queryBusiness
	 *            查询归属商务
	 * @return
	 */
	public static DataAuthorityCondition getDataAuthorityCondition(Long userId, boolean querySale,
			boolean queryBusiness, String userRealName) {
		if (userId == null) {
			throw new OperationException("用户Id为空");
		}

		UserPo user = officeService.getUserAndDeptById(userId);
		if (user.getDataAuthority() == DataAuthority.仅看自己数据.getValue()) {
			List<Long> ids = Lists.newArrayList();
			ids.add(user.getId());
			return new DataAuthorityCondition(ids);
		}

		DataAuthorityCondition dataAuthorityCondition = new DataAuthorityCondition();

		// 若是云之讯，能查询到没有归属销售/归属商务的数据
		if (user.getDepartmentLevel() == 0) {
			dataAuthorityCondition.setNeedQuerySaleIsNullData(true);
		}

		List<Role> roles = Lists.newArrayList();

		// 查询部门的归属销售
		if (querySale) {
			Role role = new Role();
			role.setRoleName("销售人员");
			roles.add(role);
		}

		// 查询部门的归属商务
		if (queryBusiness) {
			Role role = new Role();
			role.setRoleName("商务人员");
			roles.add(role);
		}

		user.setRealname(userRealName);
		user.setRoles(roles);
		List<Long> ids = officeService.findSalesOrBusinessByDeptId(user);
		if(ids==null || ids.size()==0){
          ids.add(-1L);
		}
		dataAuthorityCondition.setIds(ids);

		return dataAuthorityCondition;
	}

	public static DataAuthorityCondition getDataAuthorityCondition(Long userId, boolean querySale,
																   boolean queryBusiness) {
		return getDataAuthorityCondition(userId, querySale, queryBusiness, null);
	}

	public static User convertToUser(UserSession userSession) {
		User user = new User();
		user.setId(userSession.getId());
		user.setRealname(userSession.getRealname());
		user.setRoles(userSession.getRoles());
		user.setRoleId(userSession.getRoleId());
		user.setRoleName(userSession.getRoleName());
		user.setStatus(userSession.getStatus());
		user.setWebId(userSession.getWebId());
		user.setDepartmentId(userSession.getDepartmentId());
		user.setDataAuthority(userSession.getDataAuthority());
		return user;
	}

	public static UserSession convertToUserSession(User user) {
		UserSession userSession = new UserSession();
		userSession.setId(user.getId());
		userSession.setRealname(user.getRealname());
		userSession.setRoles(user.getRoles());
		userSession.setRoleId(user.getRoleId());
		userSession.setRoleName(user.getRoleName());
		userSession.setStatus(user.getStatus());
		userSession.setWebId(user.getWebId());
		userSession.setDepartmentId(user.getDepartmentId());
		userSession.setDataAuthority(user.getDataAuthority());
		return userSession;
	}

	public static boolean isMobileNO(String mobiles) {
		boolean flag = false;

		try {
			Pattern p = Pattern.compile("^((13[0-9])|(14[0-9])|(15[0-9])|(17[0-9])|(18[0-9]))\\d{8}$");
			Matcher m = p.matcher(mobiles);
			flag = m.matches();
		} catch (Throwable var4) {
			flag = false;
		}

		return flag;
	}

	public static Page buildPageByParams(Map params) {
		int pageSize = 20;
		int pageNo = 1;

		String rows = params.get("rows") == null ? null : params.get("rows").toString();
		String page = params.get("page") == null ? null : params.get("page").toString();

		if (StringUtils.isNotBlank(rows)) {
			pageSize = Integer.valueOf(rows);
		}
		if (StringUtils.isNotBlank(page)) {
			pageNo = Integer.valueOf(page);
		}

		return new Page(pageNo, pageSize);
	}

	public static void buildPageLimitParams(Map params, Integer totalCount, PageContainer p) {
		if (totalCount > 0) {
			String pageRowCountS = params.get("pageRowCount") != null ? params.get("pageRowCount").toString() : null;
			if (NumberUtils.isDigits(pageRowCountS)) {
				int pageRowCount = Integer.parseInt(pageRowCountS);
				if (pageRowCount > 0) {
					p.setPageRowCount(pageRowCount);
				}
			}
			int totalPage = totalCount / p.getPageRowCount() + (totalCount % p.getPageRowCount() == 0 ? 0 : 1);
			p.setTotalPage(totalPage);

			String currentPageS = params.get("currentPage") != null ? params.get("currentPage").toString() : null;
			if (NumberUtils.isDigits(currentPageS)) {
				int currentPage = Integer.parseInt(currentPageS);
				if (currentPage > 0 && currentPage <= totalPage) {
					p.setCurrentPage(currentPage);
				}
			}

			int startRow = (p.getCurrentPage() - 1) * p.getPageRowCount(); // 分页开始行号
			int rows = p.getPageRowCount();// 分页返回行数
			params.put("limit", "LIMIT " + startRow + ", " + rows);
		}
	}

	public static List<Menu> getMenus(UserSession user) {
		if (user == null || (user.getRoles() == null && user.getRoleId() == null)) {
			return null;
		}

		List<Menu> menus = null;
		if (Collections3.isEmpty(user.getRoles())) {
			menus = menuService.getHeaderMenu(user.getRoleId());
		} else {
			menus = menuService.getHeaderMenu(user.getRoles());
		}

		return menus;
	}

	/**
	 * 是否有三级菜单权限
	 *
	 * @param user
	 *            用户，用于获取菜单
	 * @param menuNames
	 *            需要判断的菜单名称，参数格式为 menuName-menuNameDesc-parentId ,各个参数之间用 - 分隔，
	 *            parentId不是必须的，若不需要parentId，格式为menuName-menuNameDesc，menuNameDesc必须要英文的
	 */
	public static Map<String, Boolean> hasMenuRight(UserSession user, String... menuNames) {
		if (user == null || menuNames == null || menuNames.length == 0) {
			throw new OperationException("获取用户菜单权限错误");
		}

		List<Menu> menus = getMenus(user);
		if (menus == null || menus.isEmpty()) {
			throw new OperationException("获取用户菜单权限错误");
		}

		Map<String, Boolean> results = new HashMap<>();

		// 循环判断权限
		for (String menuName : menuNames) {
			String[] temp = menuName.split("-");

			// 是否需要判断父Id
			boolean hasParent = false;
			if (temp.length == 3) {
				hasParent = true;
			} else if (temp.length == 2) {
				hasParent = false;
			} else {
				throw new OperationException("获取用户菜单权限错误, 参数异常");
			}

			boolean hasRight = false;
			for (Menu menu : menus) {
				if (menu.getLevel() != 3 || StringUtils.isBlank(menu.getMenu_name())) {
					continue;
				}

				logger.debug(menu.getMenu_name()+"========================"+temp[0]+"---------"+(menu.getMenu_name().equals(temp[0])));
				if (menu.getMenu_name().equals(temp[0])) {
					if (hasParent) {
						if (temp[2].equals(menu.getParent_id().toString())) {
							hasRight = true;
							break;
						}
					} else {
						hasRight = true;
						break;
					}
				}
			}

			if (hasParent) {
				results.put(temp[1] + temp[2], hasRight);
			} else {
				results.put(temp[1], hasRight);
			}
		}

		return results;
	}

}
