package com.ucpaas.sms.controller;

import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.dto.PeidanRequest;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.enums.SessionEnum;
import com.ucpaas.sms.service.order.OrderListService;
import com.ucpaas.sms.util.AgentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;

/**
 * 
 * @author huangwenjie
 *
 */
@Controller
@RequestMapping("/peidan")
public class PeiDanController extends BaseController{
	private static Logger logger = LoggerFactory.getLogger(PeiDanController.class);

	@Autowired
	private OrderListService orderListService;

	@GetMapping("/list")
	public String list(Model model, HttpSession session) {
		UserSession user = getUserFromSession(session);
		model.addAttribute("menus", AgentUtils.hasMenuRight(user, "配单中心-pdzx"));
		return "/peidan/list";
	}

	@GetMapping("/peidan/{orderId}")
	public ModelAndView peidan(@PathVariable("orderId") String orderId, ModelAndView mv) {
		mv.addObject("orderId", orderId);
		mv.setViewName("/peidan/peidan");
		return mv;
	}

	@GetMapping("/view/{orderId}")
	public ModelAndView view(@PathVariable("orderId") String orderId, ModelAndView mv) {
		mv.addObject("orderId", orderId);
		mv.setViewName("/peidan/view");
		return mv;
	}

	@PostMapping(path = "/daipipei")
	@ResponseBody
	public ResultVO daipipei(PeidanRequest peidanRequest, HttpSession session) {
		ResultVO resultVO = ResultVO.failure();
		UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		try {
			resultVO = orderListService.daipipei(peidanRequest.getOrderId(), peidanRequest.getResourceIds(), peidanRequest.getRemark(), user.getId());
		} catch (Exception e) {
			logger.error("待匹配操作失败", e);
			resultVO.setMsg(e.getMessage());
		}
		return resultVO;
	}

	@PostMapping(path = "/pipeichenggong")
	@ResponseBody
	public ResultVO pipeichenggong(PeidanRequest peidanRequest, HttpSession session) {
		ResultVO resultVO = ResultVO.failure();
		UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		try {
			resultVO = orderListService.pipeichenggong(peidanRequest.getOrderId(), peidanRequest.getResourceIds(), peidanRequest.getRemark(), user.getId());
		} catch (Exception e) {
			logger.error("匹配成功操作失败", e);
			resultVO.setMsg(e.getMessage());
		}
		return resultVO;
	}

	@PostMapping("/huitui")
	@ResponseBody
	public ResultVO huitui(PeidanRequest peidanRequest, HttpSession session) {
		ResultVO resultVO = ResultVO.failure();
		UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		try {
			resultVO = orderListService.huitui(peidanRequest.getOrderId(), peidanRequest.getRemark(), user.getId());
		} catch (Exception e) {
			logger.error("匹配成功操作失败", e);
			resultVO.setMsg(e.getMessage());
		}
		return resultVO;
	}

	@PostMapping("/faqixuqiu")
	@ResponseBody
	public ResultVO faqixuqiu(PeidanRequest peidanRequest, HttpSession session) {
		ResultVO resultVO = ResultVO.failure();
		UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		try {
			resultVO = orderListService.faqixuqiu(peidanRequest.getOrderId(), peidanRequest.getRemark(), user.getId());
		} catch (Exception e) {
			logger.error("发起需求操作失败", e);
			resultVO.setMsg(e.getMessage());
		}
		return resultVO;
	}

}
