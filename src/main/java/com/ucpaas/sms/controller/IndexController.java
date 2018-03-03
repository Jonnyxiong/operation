package com.ucpaas.sms.controller;

import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.common.enums.Code;
import com.ucpaas.sms.common.util.Collections3;
import com.ucpaas.sms.entity.MenuButtonDTO;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.enums.SessionEnum;
import com.ucpaas.sms.model.Menu;
import com.ucpaas.sms.service.admin.AuthorityService;
import com.ucpaas.sms.service.common.MenuService;
import com.ucpaas.sms.service.common.OpsIndexService;
import com.ucpaas.sms.util.AgentUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/index")
public class IndexController extends BaseController {

	private static Logger logger = LoggerFactory.getLogger(IndexController.class);

	@Autowired
	private OpsIndexService opsIndexService;
	@Autowired
	private MenuService menuService;
	@Autowired
	private AuthorityService authorityService;

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView index(ModelAndView mv, HttpSession session) {
		UserSession user = getUserFromSession(session);

		List<Menu> menus = null;
		if (Collections3.isEmpty(user.getRoles())){
			menus = menuService.getHeaderMenu(user.getRoleId());
		}else {
			menus = menuService.getHeaderMenu(user.getRoles());
		}
		Iterator<Menu> mit = menus.iterator();
		while(mit.hasNext()){
			Menu menu = mit.next();
			if(menu.getMenu_name()!=null&&menu.getMenu_name().equals("首页"))
				mit.remove();
		}
		// 设置查询月份
		mv.addObject("menus", menus);
		mv.setViewName("index");
		return mv;
	}

	@RequestMapping(path = "/welcome", method = RequestMethod.GET)
	public ModelAndView welcome(ModelAndView mv,HttpSession session) {
		// 设置查询月份
		mv.setViewName("welcome");
		Map<String, String> params = new HashMap<String, String>();
		DateTime dt = DateTime.now();
		String data_time = dt.toString("yyyyMM");
		params.put("data_time", data_time);

		//获取用户信息
		UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		if(null==user){
			return mv;
		}
		params.put("id", String.valueOf(user.getId()));
		//数据权限，0：仅看自己的数据，1：所在部门及下级部门，适用于运营平台
		//params.put("dataAuthority", String.valueOf(user.getDataAuthority()));

		Map<String, Object> data = opsIndexService.view(params);
		mv.addObject("data", data);
		return mv;
	}

	@GetMapping(path = "/hasRight")
	@ResponseBody
	public R hasRight(String menuName, HttpSession session) {
		UserSession user = getUserFromSession(session);
		return R.ok("获取信息成功", AgentUtils.hasMenuRight(user, menuName));
	}

	/**
	 * @Description: 首页 / 代理商趋势图
	 * @author: huangwenjie
	 * @date: 2017年05月05日 下午12:31:27
	 */
	@RequestMapping("/agentTendency")
	@ResponseBody
	public List<Map<String, Object>> queryAgentTendency(String start_time, String end_time,HttpSession session) {
		Map<String, Object> params = new HashMap<>();
		params.put("start_time",start_time);
		params.put("end_time",end_time);
		//获取用户信息
		UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		params.put("id", String.valueOf(user.getId()));
		List<Map<String, Object>> dataList = opsIndexService.queryAgentTendency(params);
		return dataList;
	}

	/**
	 * @Description: 首页 / 客户趋势图
	 * @author: huangwenjie
	 * @date: 2017年05月05日 下午12:31:27
	 */
	@RequestMapping("/clientTendency")
	@ResponseBody
	public List<Map<String, Object>> queryClientTendency(String start_time, String end_time,HttpSession session) {
		Map<String, Object> params = new HashMap<>();
		params.put("start_time",start_time);
		params.put("end_time",end_time);
		//获取用户信息
		UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		params.put("id", String.valueOf(user.getId()));
		List<Map<String, Object>> dataList = opsIndexService.queryClientTendency(params);
		return dataList;
	}

	/**
	 * @Description: 首页 / 消费趋势图
	 * @author: Niu.T
	 * @date: 2016年12月12日 下午12:29:07
	 */
	@RequestMapping("/consumeTendency")
	@ResponseBody
	public List<Map<String, Object>> queryagentTendency(String start_time, String end_time) {
		Map<String, String> params = new HashMap<>();
		params.put("start_time",start_time);
		params.put("end_time",end_time);
		List<Map<String, Object>> dataList = opsIndexService.queryConsumeTendency(params);
		return dataList;
	}

	/**
	 * 请求超时登录页面
	 * @param mv
	 * @return
	 */
	@RequestMapping("/lockscreen")
	public ModelAndView lockscreen(ModelAndView mv){
		mv.setViewName("lockscreen");
		return mv;
	}

	@RequestMapping("/isSessionValid")
	@ResponseBody
	public ResultVO isSessionValid(){
		return ResultVO.successDefault();
	}

	/**
	  * @Description: 判断角色是否拥有该菜单id的权限
	  * @Author: tanjiangqiang
	  * @Date: 2018/1/5 - 10:16
	  * @param menuId 菜单id
	  *
	  */
	@ApiOperation(value = "判断登录用户是否该菜单id的权限", notes = "判断用户是否拥有该菜单id的权限", tags = "账户管理-菜单和按钮权限判断", response = ResultVO.class)
	@RequestMapping(value = "/menuright/{menuId}", method = RequestMethod.GET)
	@ResponseBody
	public ResultVO menuRight(@PathVariable("menuId") String menuId, HttpSession session){
		try {
			UserSession userSession = getUserFromSession(session);
			boolean result = authorityService.isMenuAuthority(userSession.getRoles(),menuId);
			return ResultVO.successDefault(result);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取菜单或按钮权限失败",e);
			return ResultVO.failure(Code.SYS_ERR,"系统正在检修...");
		}
	}

	/**
	 * @Description: 根据父级菜单id获取该登录用户的按钮权限
	 * @Author: tanjiangqiang
	 * @Date: 2018/1/4 - 17:51
	 * @param session 用户信息
	 * @param menuId 菜单id
	 *
	 */
	@ApiOperation(value = "获取该登录用户的按钮权限", notes = "根据父级菜单id获取该登录用户的按钮权限", response = ResultVO.class, tags = "账户管理-按钮权限控制")
	@RequestMapping(value = "/menuright/{menuId}/button", method = RequestMethod.GET)
	@ResponseBody
	public ResultVO buttonRight(@PathVariable("menuId") String menuId, HttpSession session){
		try {
			UserSession userSession = getUserFromSession(session);
			List<MenuButtonDTO> result = authorityService.getButtonAuthority(userSession, menuId);
			return ResultVO.successDefault(result);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("获取按钮权限失败{}",e);
			return ResultVO.failure(Code.SYS_ERR,"系统正在检修...");
		}
	}

}
