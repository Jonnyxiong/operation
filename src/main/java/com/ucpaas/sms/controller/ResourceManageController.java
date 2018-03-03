package com.ucpaas.sms.controller;

import com.ucpaas.sms.common.entity.R;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.entity.message.Demand;
import com.ucpaas.sms.entity.message.Resource;
import com.ucpaas.sms.enums.ResourceStatus;
import com.ucpaas.sms.enums.SessionEnum;
import com.ucpaas.sms.exception.OperationException;
import com.ucpaas.sms.model.Page;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.common.CommonService;
import com.ucpaas.sms.service.order.ResourceService;
import com.ucpaas.sms.util.AgentUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 资源管理
 */
@Controller
@RequestMapping("/order/resource")
public class ResourceManageController extends BaseController {

	private static Logger logger = LoggerFactory.getLogger(ResourceManageController.class);

	@Autowired
	private ResourceService resourceService;
	
	@Autowired
	private CommonService commonService;

	@GetMapping("/view")
	public String view(Model model, HttpSession session) {
		UserSession user = getUserFromSession(session);
		model.addAttribute("menus", AgentUtils.hasMenuRight(user, "我提交的-wtjd-261", "资源需求-zyxq"));

		return "resourceMgnt/resource-list";
	}

	/**
	 * 资源列表
	 * 
	 * @param rows
	 * @param page
	 * @param state
	 */
	@GetMapping("/list")
	@ResponseBody
	public R resourceList(String rows, String page, String state) {
		Map<String, String> params = new HashMap<>();
		params.put("pageRowCount", rows);
		params.put("currentPage", page);
		if (StringUtils.isNotBlank(state)) {
			params.put("state", state);
		}

		R r;
		try {
			params.put("orderBy", "a.create_time desc");
			PageContainer<Resource> pageC = resourceService.findResourceList(params);

			r = R.ok("获取列表成功!", pageC);
		} catch (OperationException oe) {
			logger.error(oe.getMessage());
			r = R.error(oe.getMessage());
		} catch (Exception e) {
			logger.error("{}", e);
			r = R.error("服务器异常,正在检修中...");
		}
		return r;
	}

	/**
	 * 资源详情
	 * 
	 * @param resourceId
	 * @return
	 */
	@GetMapping("/detail")
	public ModelAndView resourceDetail(ModelAndView mv, String resourceId) {
		
		Resource resource = resourceService.getResource(resourceId);
		String channelType = resource.getChannelType().replace(",","，");
        channelType = channelType.replace("0", "三网合一");
        channelType = channelType.replace("1", "移动");
        channelType = channelType.replace("2", "电信");
        channelType = channelType.replace("3", "联通");
        channelType = channelType.replace("4", "全网");
        resource.setChannelType(channelType);
        
        String signType = resource.getSignType().replace(",","，");
        signType = signType.replace("0", "自定义");
        signType = signType.replace("1", "报备签名");
        signType = signType.replace("2", "固签");
        resource.setSignType(signType);
        
		mv.addObject("model", resource);
        mv.setViewName("resourceMgnt/resource-detail");
		return mv;
	}

	@GetMapping("/add")
	public String addView() {
		return "resourceMgnt/resource-add";
	}

	/**
	 * 添加资源
	 *
	 * @param resource
	 */
	@PostMapping("/add/save")
	@ResponseBody
	public R doAddResource(Resource resource, Model model, HttpServletRequest request) {
		R r;
		if (resource == null) {
			return R.error("参数不能为空！");
		}
		
		if(resource.getOnlineDate() == null){
			// 资源状态: 0 待接入 1 已接入 2 待审批 3 撤销
			resource.setState("1");
		}else{
			resource.setState("0");
		}
		
		String purchasePriceFlag = request.getParameter("purchasePriceFlag");
		resource.setPurchasePrice(purchasePriceFlag + ":" + resource.getPurchasePrice());
		
		if(StringUtils.isBlank(resource.getChannelId())){
			resource.setChannelId(null);
		}

		if (!beanValidator(model, resource)) {
			return R.error(model.asMap().get("message").toString());
		}

		try {
			UserSession userSession = (UserSession) request.getSession().getAttribute(SessionEnum.SESSION_USER.toString());
			resource.setOperatorId(userSession.getId().toString());
			int count = resourceService.addResource(resource);
			r = count > 0 ? R.ok("提交资源成功！") : R.error("提交资源失败");
		} catch (OperationException oe) {
			logger.error(oe.getMessage());
			r = R.error(oe.getMessage());
		} catch (Exception e) {
			logger.error("{}", e);
			r = R.error("服务器异常,正在检修中...");
		}
		return r;
	}

	@GetMapping("/edit")
	public ModelAndView editView(ModelAndView mv, String resourceId) {
		
		mv.addObject("model", resourceService.getResource(resourceId));
		mv.addObject("businessManList", commonService.getAllBusinessMan());
        mv.setViewName("resourceMgnt/resource-edit");
		return mv;
	}

	/**
	 * 编辑更新资源
	 * 
	 * @param resource
	 */
	@PostMapping("/edit/save")
	@ResponseBody
	public R doEditResource(Resource resource, Model model, HttpServletRequest request) {
		R r;
		if (resource == null) {
			return R.error("参数不能为空！");
		}
		
		if(resource.getOnlineDate() == null){
			// 资源状态: 0 待接入 1 已接入 2 待审批 3 撤销
			resource.setState("1");
		}else{
			resource.setState("0");
		}
		
		String purchasePriceFlag = request.getParameter("purchasePriceFlag");
		resource.setPurchasePrice(purchasePriceFlag + ":" + resource.getPurchasePrice());
		
		if(StringUtils.isBlank(resource.getChannelId())){
			resource.setChannelId(null);
		}

		if (!beanValidator(model, resource)) {
			return R.error(model.asMap().get("message").toString());
		}

		try {
			UserSession userSession = (UserSession) request.getSession().getAttribute(SessionEnum.SESSION_USER.toString());
			resource.setOperatorId(userSession.getId().toString());
			int count = resourceService.editResource(resource);
			r = count > 0 ? R.ok("修改资源成功！") : R.error("修改资源失败");
		} catch (OperationException oe) {
			logger.error(oe.getMessage());
			r = R.error(oe.getMessage());
		} catch (Exception e) {
			logger.error("{}", e);
			r = R.error("服务器异常,正在检修中...");
		}
		return r;
	}

	/**
	 * 状态更新
	 * 
	 * @param resourceId
	 */
	@PostMapping("/repeal")
	@ResponseBody
	public R updateResource(String resourceId) {
		R r;
		if (StringUtils.isBlank(resourceId)) {
			return R.error("参数不能为空！");
		}

		try {
			Resource resource = new Resource();
			resource.setResourceId(resourceId);
			resource.setState(ResourceStatus.撤销.toString());
			int count = resourceService.updateResourceStatus(resource);
			r = count > 0 ? R.ok("撤销成功！") : R.error("撤销失败！");
		} catch (OperationException oe) {
			logger.error(oe.getMessage());
			r = R.error(oe.getMessage());
		} catch (Exception e) {
			logger.error("{}", e);
			r = R.error("服务器异常,正在检修中...");
		}
		return r;
	}

	/**
	 * 通道是否使用
	 * 
	 * @param channelId
	 *            通道ID
	 */
	@GetMapping("/channel/used")
	@ResponseBody
	public R validChannel(String channelId) {
		R r;
		if (StringUtils.isBlank(channelId)) {
			return R.error("参数不能为空！");
		}

		try {

			boolean used = resourceService.hasUsedChannel(channelId);
			r = R.ok("", used);
		} catch (OperationException oe) {
			logger.error(oe.getMessage());
			r = R.error(oe.getMessage());
		} catch (Exception e) {
			logger.error("{}", e);
			r = R.error("服务器异常,正在检修中...");
		}
		return r;
	}

	@GetMapping("/demand/view")
	public String demandView(Model model) {
		return "resourceMgnt/demand/list";
	}

	@GetMapping("/demand/list")
	@ResponseBody
	public R demandList(String rows, String page) {
		Map<String, String> params = new HashMap<>();
		params.put("pageRowCount", rows);
		params.put("currentPage", page);

		R r;
		try {
			params.put("orderBy", "a.create_time desc");
			PageContainer pageC = resourceService.findDemandList(params);

			r = R.ok("获取列表成功!", pageC);
		} catch (OperationException oe) {
			logger.error(oe.getMessage());
			r = R.error(oe.getMessage());
		} catch (Exception e) {
			logger.error("{}", e);
			r = R.error("服务器异常,正在检修中...");
		}
		return r;
	}

	@GetMapping("/demand/detail")
	public ModelAndView demandDetail(ModelAndView mv, String demandId) {
		
		Demand demand = resourceService.getDemand(demandId);
		String channelType = demand.getChannelType().replace(",","，");
        channelType = channelType.replace("0", "三网合一");
        channelType = channelType.replace("1", "移动");
        channelType = channelType.replace("2", "电信");
        channelType = channelType.replace("3", "联通");
        channelType = channelType.replace("4", "全网");
        demand.setChannelType(channelType);
        
        String signType = demand.getSignType().replace(",","，");
        signType = signType.replace("0", "自定义");
        signType = signType.replace("1", "报备签名");
        signType = signType.replace("2", "固签");
        demand.setSignType(signType);
		mv.addObject("model", demand);
		mv.setViewName("resourceMgnt/demand-detail");
		mv.addObject("resourceList", resourceService.findResourceListByDemandId(demandId));
		return mv;
	}

	/**
	 * 资源需求 无资源
	 *
	 * @param demandId
	 * @param remark
	 */
	@PostMapping("/demand/nores")
	@ResponseBody
	public R demandNoResource(String demandId, String remark, HttpServletRequest request) {
		R r;
		if (StringUtils.isBlank(demandId)) {
			return R.error("参数不能为空！");
		}
		try {
			Demand demand = new Demand();
			demand.setDemandId(demandId);
			if (StringUtils.isNotBlank(remark)) {
				demand.setRemark(remark);
			}

			// 设置操作员
			UserSession userSession = (UserSession) request.getSession().getAttribute(SessionEnum.SESSION_USER.toString());
			demand.setOperatorId(userSession.getId().toString());

			// 无资源处理
			boolean result = resourceService.doSetNoResourceForDemand(demand);
			r = result ? R.ok("需求设置无资源成功！") : R.error("需求设置无资源失败！");
		} catch (OperationException oe) {
			logger.error(oe.getMessage());
			r = R.error(oe.getMessage());
		} catch (Exception e) {
			logger.error("{}", e);
			r = R.error("服务器异常,正在检修中...");
		}
		return r;
	}

	/**
	 * 资源需求-有资源
	 *
	 */
	@GetMapping("/demand/hasres")
	public String demandHasResource(String demandId, Model model) {
		Demand demand = new Demand();
		if (StringUtils.isBlank(demandId)) {
		} else {
			// 需求信息
			demand = resourceService.getDemand(demandId);
			String channelType = demand.getChannelType().replace(",","，");
	        channelType = channelType.replace("0", "三网合一");
	        channelType = channelType.replace("1", "移动");
	        channelType = channelType.replace("2", "电信");
	        channelType = channelType.replace("3", "联通");
	        channelType = channelType.replace("4", "全网");
	        demand.setChannelType(channelType);
	        
	        String signType = demand.getSignType().replace(",","，");
	        signType = signType.replace("0", "自定义");
	        signType = signType.replace("1", "报备签名");
	        signType = signType.replace("2", "固签");
	        demand.setSignType(signType);
		}

		model.addAttribute("demandItem", demand);
		return "resourceMgnt/demand-hasRes";
	}

	/**
	 * 资源需求-选择资源弹出框页面
	 * @param mv
	 * @return
	 */
	@GetMapping("/demand/selectRes")
	public ModelAndView selectRes(ModelAndView mv) {
		// 已接入
		List<Resource> hasInResourceList = resourceService.findHasInResourceList(null);
		// 待接入
		List<Resource> waitInResourceList = resourceService.findWaitInResourceList(null);
			
		mv.addObject("isOnlineRes", hasInResourceList);
		mv.addObject("isWaitOnlineRes", waitInResourceList);
		mv.setViewName("resourceMgnt/demand-selectRes");
		return mv;
	}
	
	
	
	
	/**
	 * 
	 * @param params
	 *            demandId 字符串<br>
	 *            resourceIds 数组
	 * @param request
	 * @return
	 */
	@PostMapping("/demand/hasres/save")
	@ResponseBody
	public R demandHasResourceSave(@RequestBody Map<String, Object> params, HttpServletRequest request) {
		R r;
		if (params == null || params.size() != 2) {
			return R.error("参数错误！");
		}

		try {
			// 设置操作员
			UserSession userSession = (UserSession) request.getSession().getAttribute(SessionEnum.SESSION_USER.toString());
			params.put("operatorId", userSession.getId());

			boolean result = resourceService.doSetHasResourceForDemand(params);
			r = result ? R.ok("设置资源成功！") : R.error("设置资源失败！");
		} catch (OperationException oe) {
			logger.error(oe.getMessage());
			r = R.error(oe.getMessage());
		} catch (Exception e) {
			logger.error("{}", e);
			r = R.error("服务器异常,正在检修中...");
		}
		return r;
	}
	
	/**
	 * 查询所有的商务人员
	 * @return
	 */
	@GetMapping("/getAllBusinessMan")
	@ResponseBody
	public R getAllBusinessMan() {
		R r;
		try {
			List<UserSession> bussinessManList = commonService.getAllBusinessMan();
			r = R.ok("获取商务人员列表成功!", bussinessManList);
		} catch (OperationException oe) {
			logger.error(oe.getMessage());
			r = R.error(oe.getMessage());
		} catch (Exception e) {
			logger.debug(e.getMessage());
			r = R.error("服务器异常,正在检修中...");
		}
		return r;
	}
	
	/**
	 * 获取通道开启状态
	 * @return
	 */
	@GetMapping("/channelIsOpen")
	@ResponseBody
	public R channelIsOpen(String channelId) {
		R r;
		try {
			boolean channelIsOpen = resourceService.channelIsOpen(channelId);
			r = R.ok("获取通道开启状态成功!", channelIsOpen);
		} catch (OperationException oe) {
			logger.error(oe.getMessage());
			r = R.error(oe.getMessage());
		} catch (Exception e) {
			logger.error("{}", e);
			r = R.error("服务器异常,正在检修中...");
		}
		return r;
	}

	@GetMapping("/demand/byorder/{orderId}")
	@ResponseBody
	public R getDemandByOrderId(@PathVariable("orderId") String orderId) {
		R r;
		if (StringUtils.isBlank(orderId)) {
			return R.error("订单ID不能为空！");
		}
		try {
			Demand demand = resourceService.getDemandByOrderId(orderId);
			r = R.ok("获取需求信息成功!", demand);
		}  catch (Exception e) {
			logger.error("{}", e);
			r = R.error("服务器异常,正在检修中...");
		}
		return r;
	}
	
	  /**
     * 获取资源列表接口 
     * @param params
     * @return
     */
    @RequestMapping(value = "/query",method = RequestMethod.POST)
    @ResponseBody
    public Page orderList(Page page, @RequestParam Map params,HttpServletRequest request){
        UserSession user = (UserSession) request.getSession().getAttribute(SessionEnum.SESSION_USER.toString());
        page.setParams(params);
        return resourceService.queryList(page);
    }
}
