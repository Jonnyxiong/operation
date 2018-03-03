package com.ucpaas.sms.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.jsmsframework.common.dto.ResultVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.entity.po.UserPo;
import com.ucpaas.sms.enums.SessionEnum;
import com.ucpaas.sms.service.common.CommonService;

@Controller
public class LoginOrOutController {
	private static Logger logger = LoggerFactory.getLogger(LoginOrOutController.class);
	@Autowired
	private CommonService commonService;

	@RequestMapping(path = "/login", method = RequestMethod.GET)
	public ModelAndView loginPage(ModelAndView mv) {
		mv.setViewName("login");
		return mv;
	}

	@RequestMapping(path = "/login", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO loginValidate(String username, String password, HttpSession session) {
		ResultVO resultVO = new ResultVO();
		try {
			resultVO = commonService.login(username, password);
			if (resultVO.isSuccess()) {

				UserPo userPo = (UserPo) resultVO.getData();
				UserSession user = new UserSession();
				user.setId(userPo.getId());
				user.setDepartmentName(userPo.getDepartmentName());
				user.setRealname(userPo.getRealname());
				user.setRoles(userPo.getRoles());
				user.setRoleId(userPo.getRoleId());
				user.setRoleName(userPo.getRoleName());
				user.setStatus(userPo.getStatus());
				user.setWebId(userPo.getWebId());
				user.setDepartmentId(userPo.getDepartmentId());
				user.setDataAuthority(userPo.getDataAuthority());

				session.setAttribute(SessionEnum.SESSION_USER.toString(), user);
			}
		} catch (Exception e) {
			logger.error("登陆异常，请求信息为username=" + username + ", password= " + password, e);
		}
		return resultVO;
	}

	@RequestMapping(value = "/logout")
	public void quit(HttpSession session, HttpServletResponse response) throws Exception {
		UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		user = null;
		session.setAttribute(SessionEnum.SESSION_USER.toString(), null);
		try {
			response.sendRedirect("/login");
		} catch (IOException e) {
			logger.error("springmvc转发/login失败", e);
			throw e;
		}
	}
}
