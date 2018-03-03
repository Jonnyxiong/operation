package com.ucpaas.sms.controller;

import com.jsmsframework.common.dto.R;
import com.jsmsframework.sale.credit.constant.SysConstant;
import com.jsmsframework.sale.credit.service.JsmsSaleCreditService;
import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.constant.UserConstant;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.entity.message.User;
import com.ucpaas.sms.enums.SessionEnum;
import com.ucpaas.sms.enums.WebId;
import com.ucpaas.sms.exception.OperationException;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.po.SaleEntityPo;
import com.ucpaas.sms.service.RoleService;
import com.ucpaas.sms.service.account.ApplyRecordService;
import com.ucpaas.sms.service.admin.AdminService;
import com.ucpaas.sms.util.HttpUtils;
import com.ucpaas.sms.common.util.web.ServletUtils;
import org.apache.commons.lang3.math.NumberUtils;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@RequestMapping("/admin")
public class AdminController {
	private static Logger logger = LoggerFactory.getLogger(AdminController.class);
	@Autowired
	private AdminService adminService;
	@Autowired
	private RoleService roleService;
	@Autowired
	private ApplyRecordService applyRecordService;
	@Autowired
	private JsmsSaleCreditService jsmsSaleCreditService;

	@RequestMapping(path = "/query", method = RequestMethod.GET)
	public ModelAndView query(ModelAndView mv) {
		mv.setViewName("admin/list");
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
		PageContainer pageData = adminService.query(params);
		return pageData;
	}

	@RequestMapping(path = "/add", method = RequestMethod.GET)
	public ModelAndView pageAdd(ModelAndView mv) {
		// 身份列表
		mv.addObject("roleList", this.roleService.queryRoleList("3"));
		mv.setViewName("admin/add");
		return mv;
	}

	/**
	 * 添加管理员
	 * 
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/add", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO add(User user, HttpSession session, HttpServletRequest request) {
		ResultVO result = ResultVO.failure();
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		try {
			Map<String, String> params = new HashMap<>();
			params.put("web_id", user.getWebId().toString());
			params.put("role_id", user.getRoleId().toString());
			params.put("email", user.getEmail().toString());
			params.put("mobile", user.getMobile().toString());
			params.put("password", user.getPassword().toString());
			params.put("realname", user.getRealname());

			params.put("userId", userSession.getId().toString());
			params.put("pageUrl", request.getRequestURI());
			params.put("ip", HttpUtils.getIpAddress(request));

			Map resultMap = adminService.saveAdmin(params);
			if ("success".equals(resultMap.get("result"))) {
				result.setSuccess(true);
				result.setMsg((String) resultMap.get("msg"));
			} else {
				result.setSuccess(false);
				result.setMsg((String) resultMap.get("msg"));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setMsg("添加管理员失败");
			return result;
		}
		return result;
	}

	@RequestMapping(path = "/edit", method = RequestMethod.GET)
	public ModelAndView pageEdit(ModelAndView mv, Long id) {
		Map<String,Object> user =  this.adminService.getAdmin(id);
		Object web_id = user.get("web_id");
		String webId = "3";
		// 身份列表
		mv.addObject("model", user);
		if(web_id!=null&&"2".equals(web_id.toString())){ //代理商
			webId = "2";
		} else if(web_id!=null&&"4".equals(web_id.toString())){ //OEM代理商
			webId = "4";
		}
		
		//mv.addObject("roleList", this.roleService.queryRoleList(webId));
		mv.setViewName("admin/edit");
		return mv;
	}

	/**
	 * 编辑管理员信息
	 * 
	 * @param request
	 * @param user
	 * @return
	 */
	@RequestMapping(value = "/edit", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO edit(User user, HttpSession session, HttpServletRequest request) {
		ResultVO result = ResultVO.failure();
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		try {
			Map<String, String> params = new HashMap<>();
			params.put("id", user.getId().toString());
			params.put("web_id", WebId.运营平台.getValue().toString());
			//params.put("role_id", user.getRoleId().toString());
			params.put("email", user.getEmail().toString());
			params.put("mobile", user.getMobile().toString());
			params.put("password", user.getPassword().toString());
			params.put("realname", user.getRealname());

			params.put("userId", userSession.getId().toString());
			params.put("pageUrl", request.getRequestURI());
			params.put("ip", HttpUtils.getIpAddress(request));

			Map resultMap = adminService.saveAdmin(params);
			if ("success".equals(resultMap.get("result"))) {
				result.setSuccess(true);
				result.setMsg((String) resultMap.get("msg"));
			} else {
				result.setSuccess(false);
				result.setMsg((String) resultMap.get("msg"));
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setMsg("系统错误，请稍后再试");
		}
		return result;
	}

	/***
	 * 修改管理员状态
	 * 
	 * @return
	 */
	@RequestMapping(value = "/updateStatus", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO updateStatus(User user, HttpSession session, HttpServletRequest request) {
		ResultVO result = ResultVO.failure();
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		try {
			Long id = user.getId();
			if (NumberUtils.isDigits(user.getStatus())) {
				Map resultMap = adminService.updateStatus(id, Integer.parseInt(user.getStatus()), userSession.getId(),
						request.getRequestURI(), HttpUtils.getIpAddress(request));

				if ("success".equals(resultMap.get("result"))) {
					result.setSuccess(true);
					result.setMsg((String) resultMap.get("msg"));
				} else {
					result.setSuccess(false);
					result.setMsg((String) resultMap.get("msg"));
				}
			}
		} catch (Exception e) {
			logger.error(result.getMsg(), e);
			result.setMsg("系统错误，请稍后再试");
		}
		return result;

	}

	/**
	 * 删除管理员
	 * 
	 * @param id
	 * @author wangwei
	 * @return
	 */
	@RequestMapping(value = "/delete", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO delete(Long id, HttpSession session, HttpServletRequest request) {
		ResultVO result = ResultVO.failure();
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		try {
			Map resultMap = adminService.updateStatus(id, UserConstant.USER_STATUS_3, userSession.getId(), request.getRequestURI(),
					HttpUtils.getIpAddress(request));

			if ("success".equals(resultMap.get("result"))) {
				result.setSuccess(true);
				result.setMsg((String) resultMap.get("msg"));
			} else {
				result.setSuccess(false);
				result.setMsg((String) resultMap.get("msg"));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result.setMsg("出错了，待会再试一下吧");
		}
		return result;
	}
	
	@RequestMapping(path = "/view", method = RequestMethod.GET)
	public ModelAndView pageView(ModelAndView mv, Long id, HttpSession session) {
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		mv.addObject("model", this.adminService.getAdmin(userSession.getId()));
		mv.setViewName("admin/view");
		return mv;
	}

	@RequestMapping(path = "/transferCustomerPage", method = RequestMethod.GET)
	public ModelAndView transferCustomerPage(ModelAndView mv,HttpServletRequest request){

		Map<String, String> formData = ServletUtils.getFormData(request);
		mv.addObject("oldUserId",formData.get("oldUserId"));

		List<SaleEntityPo> saleList = applyRecordService.getSaleList();
		mv.addObject("saleList",saleList);

		mv.setViewName("admin/transferCustomerPage");
		return mv;
	}

	/**
	 * 确认转交
	 * @param request
	 * @return
	 */
	@RequestMapping(path = "/confirmTransfer", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO confirmTransfer(HttpServletRequest request){

		Map<String, String> formData = ServletUtils.getFormData(request);
		Long oldUserId =Long.valueOf(formData.get("oldUserId")) ;
		Long newUserId = Long.valueOf(formData.get("newUserId"));
		ResultVO resultVO = ResultVO.failure();
		try {
			R r=jsmsSaleCreditService.belongSaleChaned(oldUserId,newUserId);
			if(Objects.equals(SysConstant.FAIL_CODE,r.getCode())){
				return  ResultVO.failure(r.getMsg());
			}
			resultVO = adminService.confirmTransfer(formData);
		} catch (OperationException o) {
			o.printStackTrace();
			logger.error(o.getMessage(),o);
			resultVO.setMsg(o.getMessage());
		}catch (Exception e) {
			e.printStackTrace();
			resultVO.setMsg("系统错误");
		}

		return resultVO;
	}


}
