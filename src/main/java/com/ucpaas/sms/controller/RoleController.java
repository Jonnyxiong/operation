package com.ucpaas.sms.controller;

import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.entity.message.Menu;
import com.ucpaas.sms.entity.message.Role;
import com.ucpaas.sms.enums.SessionEnum;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.RoleService;
import com.ucpaas.sms.service.admin.AuthorityService;
import com.ucpaas.sms.service.common.MenuService;
import com.ucpaas.sms.util.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/authority")
public class RoleController {

	private static Logger logger = LoggerFactory.getLogger(RoleController.class);
	@Autowired
	private AuthorityService authorityService;
	@Autowired
	private RoleService roleService;
	@Autowired
	private MenuService menuService;

	@RequestMapping(path = "/query", method = RequestMethod.GET)
	public ModelAndView query(ModelAndView mv) {
		mv.setViewName("authority/list");
		return mv;
	}

	@RequestMapping(path = "/list", method = RequestMethod.POST)
	@ResponseBody
	public PageContainer query(String rows, String page, String text) {
		String pageRowCount = rows;
		String currentPage = page;
		Map<String, String> params = new HashMap<>();
		params.put("pageRowCount", rows);
		params.put("currentPage", page);
		params.put("text", text);
		PageContainer pageData = authorityService.query(params);
		return pageData;
	}

	@RequestMapping(path = "/add", method = RequestMethod.GET)
	public ModelAndView pageAdd(ModelAndView mv,String webId,String roleName) throws UnsupportedEncodingException {
		if(StringUtils.isEmpty(webId))
			webId="3";

		if(!StringUtils.isEmpty(roleName)){
			roleName = URLDecoder.decode(roleName, "utf-8");
		}
		mv.addObject("menus", roleService.queryMenuList(webId));
		mv.addObject("webId", webId);
		mv.addObject("roleName", roleName);
		mv.setViewName("authority/add");
		return mv;
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO add(Role role, HttpSession session, HttpServletRequest request, String[] menuId) {
		ResultVO result = ResultVO.failure();
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		try {
			if (StringUtils.isBlank(role.getRoleName())){
				result.setMsg("角色名称不能为空");
				return result;
			}

			if (role.getRoleName().length() > 10){
				result.setMsg("角色名称长度必须介于 1 和 10 之间");
				return result;
			}

			Map<String, String> params = new HashMap<>();
			params.put("role_name", role.getRoleName());
			params.put("webId", role.getWebId().toString());
			StringBuffer menuIdStr = new StringBuffer();
			if (menuId != null && menuId.length > 0) {
				String delim = "";
				for (String mi : menuId) {
					menuIdStr.append(delim).append(mi);
					delim = ",";
				}
			}
			params.put("menu_id", menuIdStr.toString());
			params.put("userId", userSession.getId().toString());
			params.put("pageUrl", request.getRequestURI());
			params.put("ip", HttpUtils.getIpAddress(request));

			Map resultMap = authorityService.saveAuthority(params);
			if ("success".equals(resultMap.get("result"))) {
				result.setSuccess(true);
				result.setMsg((String) resultMap.get("msg"));
			} else {
				result.setSuccess(false);
				result.setMsg((String) resultMap.get("msg"));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setMsg("系统错误！请稍后再试");
		}
		return result;
	}

	@RequestMapping(path = "/edit", method = RequestMethod.GET)
	public ModelAndView pageEdit(ModelAndView mv, Integer id,String webId,String roleName) throws UnsupportedEncodingException {

		if(StringUtils.isEmpty(webId)){
			Role role = roleService.getById(id);
			webId=role.getWebId().toString();
		}

		if(!StringUtils.isEmpty(roleName)){
			roleName = URLDecoder.decode(roleName, "utf-8");
		}
		mv.addObject("menus", roleService.queryMenuList(webId));
		mv.addObject("roleId", id);
		mv.addObject("webId", webId);
		mv.addObject("roleName", roleName);
		mv.setViewName("authority/edit");
		return mv;
	}

	/**
	 * 获取角色菜单
	 * 
	 * @return
	 */
	@RequestMapping(value = "/roleMenu")
	@ResponseBody
	public ResultVO menuList(String id,String  webId) {
		if (id != null && id != "") {
			Map params = new HashMap<>();
			params.put("id", id);
			params.put("webId", webId);
			List<Menu> menus = roleService.queryRoleMenuList(params);
			Role role = roleService.getById(Integer.parseInt(id));
			role.setMenu(menus);
			return ResultVO.successDefault(role);
		} else
			return ResultVO.failure("获取角色ID失败");

	}

	/**
	 * 修改角色
	 * 
	 * @param role
	 * @return
	 */
	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO edit(Role role, HttpSession session, HttpServletRequest request, String[] menuId) {
		ResultVO result = ResultVO.failure();
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		try {
			if (StringUtils.isBlank(role.getRoleName())){
				result.setMsg("角色名称不能为空");
				return result;
			}

			if (role.getRoleName().length() > 10){
				result.setMsg("角色名称长度必须介于 1 和 10 之间");
				return result;
			}

			Map<String, String> params = new HashMap<>();
			params.put("role_id", role.getId().toString());
			params.put("role_name", role.getRoleName());
			StringBuffer menuIdStr = new StringBuffer();
			if (menuId != null && menuId.length > 0) {
				String delim = "";
				for (String mi : menuId) {
					menuIdStr.append(delim).append(mi);
					delim = ",";
				}
			}
			params.put("menu_id", menuIdStr.toString());
			params.put("userId", userSession.getId().toString());
			params.put("pageUrl", request.getRequestURI());
			params.put("ip", HttpUtils.getIpAddress(request));

			Map resultMap = authorityService.saveAuthority(params);
			if ("success".equals(resultMap.get("result"))) {
				result.setSuccess(true);
				result.setMsg((String) resultMap.get("msg"));
			} else {
				result.setSuccess(false);
				result.setMsg((String) resultMap.get("msg"));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setMsg("系统错误！请稍后再试");
		}
		return result;
	}

	/**
	 * 删除角色
	 * 
	 * @return
	 */
	@RequestMapping(value = "/delete")
	@ResponseBody
	public ResultVO menuList(Integer id, HttpSession session, HttpServletRequest request) {
		ResultVO result = ResultVO.failure();
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		Map resultMap = authorityService.deleteRole(id, userSession.getId(), request.getRequestURI(), HttpUtils.getIpAddress(request));
		if ("success".equals(resultMap.get("result"))) {
			result.setSuccess(true);
			result.setMsg((String) resultMap.get("msg"));
		} else {
			result.setSuccess(false);
			result.setMsg((String) resultMap.get("msg"));
		}
		return result;

	}

}
