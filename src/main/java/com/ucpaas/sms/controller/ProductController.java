package com.ucpaas.sms.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.common.enums.ProductType;
import com.jsmsframework.common.util.JsonUtil;
import com.ucpaas.sms.common.util.FmtUtils;
import com.ucpaas.sms.common.util.file.ExcelUtils;
import com.ucpaas.sms.common.util.file.FileUtils;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.entity.po.ProductInfoPo;
import com.ucpaas.sms.enums.SessionEnum;
import com.ucpaas.sms.model.Excel;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.po.PlaceOrderParam;
import com.ucpaas.sms.service.product.ProductService;
import com.ucpaas.sms.util.AgentUtils;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.HttpUtils;
import com.ucpaas.sms.util.JsonUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/product")
public class ProductController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(ProductController.class);
	@Autowired
	private ProductService productService;


	@RequestMapping(path = "/query", method = RequestMethod.GET)
	public ModelAndView query(ModelAndView mv, String page, HttpSession session) {
		List<Map<String, Object>> dataList = productService.getAgentRebate(null);
		mv.addObject("dataList", dataList);
		mv.addObject("page", page);
		mv.setViewName("product/list");

		UserSession user = getUserFromSession(session);
		mv.addObject("menus", AgentUtils.hasMenuRight(user, "品牌产品包管理-ppcpbgl", "品牌代理属性配置-ppdlsxpz"));

		return mv;
	}

	@RequestMapping(path = "/list", method = RequestMethod.POST)
	@ResponseBody
	public PageContainer list(String rows, String page, String condition, String status, String start_time_day,
			String end_time_day) {
		Map<String, String> params = new HashMap<String, String>();
		params.put("pageRowCount", rows);
		params.put("currentPage", page);
		if (!StringUtils.isEmpty(end_time_day)) {
			end_time_day += " 23:59:59";
		}
		params.put("productInfo", condition);
		params.put("status", status);
		params.put("start_time_day", start_time_day);
		params.put("end_time_day", end_time_day);
		PageContainer pageContainer = productService.query(params);
		return pageContainer;
	}

	@RequestMapping(path = "/updateStatus", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO updateStatus(String id, String state, String reason, HttpSession session,
								 HttpServletRequest request) {
		ResultVO result = ResultVO.failure();
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		Map<String, String> params = new HashMap<String, String>();
		params.put("userId", userSession.getId().toString());
		params.put("pageUrl", request.getRequestURI());
		params.put("ip", HttpUtils.getIpAddress(request));

		params.put("state", state);
		params.put("id", id);
		Map resultMap = productService.updateStatus(params);
		if ("success".equals(resultMap.get("result"))) {
			result.setSuccess(true);
			result.setMsg((String) resultMap.get("msg"));
		} else {
			result.setSuccess(false);
			result.setMsg((String) resultMap.get("msg"));
		}
		return result;
	}

	@RequestMapping(path = "/add", method = RequestMethod.GET)
	public ModelAndView addPage(ModelAndView mv) {
		mv.setViewName("product/add");
		return mv;
	}

	@RequestMapping(path = "/add", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO add(HttpServletRequest request, HttpSession session) {
		ResultVO result = ResultVO.failure();
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		Map<String, String> params = new HashMap<>();

		for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
			String key = e.nextElement();
			String value = request.getParameter(key);
			params.put(key, value);
		}

		String product_code = params.get("product_code");
		if (StringUtils.isBlank(product_code) || product_code.length() > 30) {
			result.setSuccess(false);
			result.setMsg("产品代码长度必须介于 1 和 30 之间");
			return result;
		}

		String product_name = params.get("product_name");
		if (StringUtils.isBlank(product_name) || product_name.length() > 30) {
			result.setSuccess(false);
			result.setMsg("产品名称长度必须介于 1 和 30 之间");
			return result;
		}

		String product_desc = params.get("product_desc");
		if (StringUtils.isBlank(product_desc) || product_desc.length() > 30) {
			result.setSuccess(false);
			result.setMsg("产品描述长度必须介于 1 和 30 之间");
			return result;
		}

		String product_price = params.get("product_price");
		if (StringUtils.isBlank(product_price)) {
			result.setSuccess(false);
			result.setMsg("请输入产品定价");
			return result;
		}
		String product_cost = params.get("product_cost");
		if (StringUtils.isBlank(product_cost)) {
			result.setSuccess(false);
			result.setMsg("请输入产品成本");
			return result;
		}

		params.put("userId", userSession.getId().toString());
		params.put("pageUrl", request.getRequestURI());
		params.put("ip", HttpUtils.getIpAddress(request));
		Map resultMap = productService.addData(params);
		if ("success".equals(resultMap.get("result"))) {
			result.setSuccess(true);
			result.setMsg((String) resultMap.get("msg"));
		} else {
			result.setSuccess(false);
			result.setMsg((String) resultMap.get("msg"));
		}
		return result;
	}

	@RequestMapping(path = "/edit", method = RequestMethod.GET)
	public ModelAndView editPage(ModelAndView mv, Integer id) {

		Map data = productService.getProductDetailByProductId(id);

		String product_cost = data.get("product_cost").toString();

		data.put("product_cost", FmtUtils.deleteZero(new BigDecimal(product_cost)));

		mv.addObject("data", data);
		mv.setViewName("product/edit");
		return mv;
	}

	@RequestMapping(path = "/edit", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO edit(HttpServletRequest request, HttpSession session) {
		ResultVO result = ResultVO.failure();
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		Map<String, String> params = new HashMap<>();

		for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
			String key = e.nextElement();
			String value = request.getParameter(key);
			params.put(key, value);
		}

		String product_price = params.get("product_price");
		if (StringUtils.isEmpty(product_price)) {
			result.setSuccess(false);
			result.setMsg("请输入产品定价");
			return result;
		}
		String product_cost = params.get("product_cost");
		if (StringUtils.isEmpty(product_cost)) {
			result.setSuccess(false);
			result.setMsg("请输入产品成本");
			return result;
		}

		params.put("userId", userSession.getId().toString());
		params.put("pageUrl", request.getRequestURI());
		params.put("ip", HttpUtils.getIpAddress(request));
		Map resultMap = productService.save(params);
		if ("success".equals(resultMap.get("result"))) {
			result.setSuccess(true);
			result.setMsg((String) resultMap.get("msg"));
		} else {
			result.setSuccess(false);
			result.setMsg((String) resultMap.get("msg"));
		}
		return result;
	}

	@RequestMapping(path = "/view", method = RequestMethod.GET)
	public ModelAndView viewPage(ModelAndView mv, Integer id) {

		Map data = productService.getProductDetailByProductId(id);
		mv.addObject("data", data);
		mv.setViewName("product/view");
		return mv;
	}

	@RequestMapping(path = "/addRebate")
	@ResponseBody
	public ResultVO addRebate(HttpServletRequest request, HttpSession session) {
		ResultVO result = ResultVO.failure();
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		Map<String, String> params = new HashMap<>();

		for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
			String key = e.nextElement();
			String value = request.getParameter(key);
			params.put(key, value);
		}
		String action = params.get("edit_action");
		if (action != null && !"delete".equals(action)) {
			try {
				String start_line = params.get("start_line");
				if (!StringUtils.isEmpty(start_line)) {
					BigDecimal startLine = new BigDecimal(start_line);
					if (startLine.compareTo(BigDecimal.ZERO) < 0
							|| startLine.compareTo(new BigDecimal("9999999999")) > 0) {
						result.setSuccess(false);
						result.setMsg("消耗数量输入范围（0-9,999,999,999）");
						return result;
					}
				} else {
					params.remove("start_line");
				}
				String end_line = params.get("end_line");
				if (!StringUtils.isEmpty(end_line)) {
					BigDecimal endLine = new BigDecimal(end_line);
					if (endLine.compareTo(BigDecimal.ZERO) < 0 || endLine.compareTo(new BigDecimal("9999999999")) > 0) {
						result.setSuccess(false);
						result.setMsg("消耗数量输入范围（0-9,999,999,999）");
						return result;
					}
				} else {
					params.remove("end_line");
				}
			} catch (Exception e) {
				result.setSuccess(false);
				result.setMsg("消耗数量只能为数字");
				return result;
			}
		}

		params.put("userId", userSession.getId().toString());
		params.put("pageUrl", request.getRequestURI());
		params.put("ip", HttpUtils.getIpAddress(request));
		Map resultMap = productService.addOrUpdateRebate(params);
		if ("success".equals(resultMap.get("result"))) {
			result.setSuccess(true);
			result.setMsg((String) resultMap.get("msg"));
		} else {
			result.setSuccess(false);
			result.setMsg((String) resultMap.get("msg"));
		}
		return result;
	}

	@RequestMapping(path = "/speCusSeting", method = RequestMethod.GET)
	public ModelAndView speCusSeting(ModelAndView mv, HttpSession session) {
		mv.setViewName("product/speCusSeting/list");

		UserSession user = getUserFromSession(session);
		mv.addObject("menus", AgentUtils.hasMenuRight(user, "特殊客户设置-tskhsz", "特殊客户设置记录-tskhszjl"));
		return mv;
	}

	@RequestMapping(path = "/speCusSeting/client/{clientId}", method = RequestMethod.GET)
	@ResponseBody
	public ResultVO clientInfoREST(ModelAndView mv, @PathVariable String clientId) {
		ResultVO result = ResultVO.failure();
		Map<String, String> params = new HashMap<>();
		params.put("customerInfo", clientId);
		Map data = productService.getCustomerInfo(params);
		result.setData(data);
		result.setSuccess(true);
		return result;
	}

	@RequestMapping(path = "/speCusSeting/clientinfo")
	@ResponseBody
	public ResultVO clientInfo(ModelAndView mv, String customerInfo) {
		ResultVO result = ResultVO.failure();
		Map<String, String> params = new HashMap<>();
		params.put("customerInfo", customerInfo);
		Map data = productService.getCustomerInfo(params);
		result.setData(data);
		result.setSuccess(true);
		result.setMsg("操作成功！");
		return result;
	}

	@RequestMapping(path = "/speCusSeting/list", method = RequestMethod.POST)
	@ResponseBody
	public PageContainer speCusSetingList(String rows, String page, String clientId, String agentId) {
		if (StringUtils.isEmpty(clientId) || StringUtils.isEmpty(agentId))
			return new PageContainer();

		Map<String, String> params = new HashMap<>();
		params.put("pageRowCount", rows);
		params.put("currentPage", page);
		params.put("client_id", clientId);
		params.put("agent_id", agentId);
		PageContainer pageContainer = productService.getAgentProductInfo(params);
		return pageContainer;
	}

	@RequestMapping(path = "/speCusSeting/updateDiscountInfoBatch", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO speCusSetingUpdateDiscountInfoBatch(String placeOrderParamJsonStr, HttpServletRequest request) {

		ResultVO result = ResultVO.failure();
		placeOrderParamJsonStr = StringEscapeUtils.unescapeHtml4(placeOrderParamJsonStr);
		PlaceOrderParam placeOrderParam = JsonUtils.toObject(placeOrderParamJsonStr, PlaceOrderParam.class);

		UserSession userSession = (UserSession) request.getSession().getAttribute(SessionEnum.SESSION_USER.toString());

		for (ProductInfoPo po : placeOrderParam.getProductInfoPoList()) {
			if (po.getProduct_price_dis() != null && po.getProduct_price_dis().compareTo(new BigDecimal("1")) > 0) {
				result.setSuccess(false);
				result.setMsg("折扣率大于1，请重新填写！");
				result.setData(po.getProduct_id());
				return result;
			}
			if (po.getProduct_price_dis() != null && po.getProduct_price_dis().compareTo(BigDecimal.ZERO) < 0) {
				result.setSuccess(false);
				result.setMsg("折扣率小于0，请重新填写！");
				result.setData(po.getProduct_id());
				return result;
			}

			if (po.getGn_discount_price() != null && (po.getGn_discount_price().compareTo(BigDecimal.ZERO) < 0
					|| po.getGn_discount_price().compareTo(new BigDecimal(1000 * 100000)) > 0)) {

				result.setSuccess(false);
				result.setMsg("折后价必须是0至1亿之间的值");
				result.setData(po.getProduct_id());
				return result;
			}

			// 折后价大于销售价不可保存
			BigDecimal gnDiscountPrice = po.getGn_discount_price();
			BigDecimal quantity = po.getQuantity();
			BigDecimal productPrice = po.getProduct_price();
			if (!ProductType.国际.getValue().equals(po.getProduct_type())) {
				if (gnDiscountPrice != null && quantity != null && productPrice != null) {
					if (gnDiscountPrice.multiply(quantity).compareTo(productPrice) == 1) {
						logger.error("普通产品的折后价小于销售价,参数为{}", JsonUtil.toJson(po));
						result.setSuccess(false);
						result.setMsg("折后价必须小于销售价");
						result.setData(po.getProduct_id());
						return result;
					}
				} else {
					logger.error("普通产品的校验折后价失败,参数为{}", JsonUtil.toJson(po));
					result.setSuccess(false);
					result.setMsg("服务器正在检修...");
					result.setData(po.getProduct_id());
					return result;
				}
			}
			// 折后价 = 折后单价 * 数量
			po.setGn_discount_price(gnDiscountPrice.multiply(quantity));
		}

		Map resultMap = productService.updateDiscountInfoBatch(placeOrderParam, userSession.getId().toString(),
				request.getRequestURI(), HttpUtils.getIpAddress(request));
		if ("success".equals(resultMap.get("result"))) {
			result.setSuccess(true);
			result.setMsg((String) resultMap.get("msg"));
		} else {
			result.setSuccess(false);
			result.setMsg((String) resultMap.get("msg"));
		}
		return result;
	}

	@RequestMapping(path = "/speCusSeting/clientProduct/list", method = RequestMethod.POST)
	@ResponseBody
	public PageContainer speCusSetingClientProductList(String rows, String page, String condition,
			String start_time_day, String end_time_day) {

		Map<String, String> params = new HashMap<>();
		params.put("pageRowCount", rows);
		params.put("currentPage", page);
		params.put("selectInfo", condition);
		params.put("start_time_day", start_time_day);
		params.put("end_time_day", end_time_day);
		PageContainer pageContainer = productService.cusSetingRecord(params);
		return pageContainer;
	}

	@RequestMapping(path = "/internetSmsPrice/query", method = RequestMethod.GET)
	public ModelAndView internetSmsPriceList(ModelAndView mv) {
		mv.setViewName("product/internetSmsPrice/list");
		return mv;
	}

	@RequestMapping(path = "/internetSmsPrice/list", method = RequestMethod.POST)
	@ResponseBody
	public PageContainer speCusSetingClientProductList(String rows, String page, String condition) {

		Map<String, String> params = new HashMap<>();
		params.put("pageRowCount", rows);
		params.put("currentPage", page);
		params.put("condition", condition);
		PageContainer pageContainer = productService.queryInterSmsPriceInfo(params);
		return pageContainer;
	}

	/**
	 * 删除管理员
	 * 
	 * @param id
	 * @author wangwei
	 * @return
	 */
	@RequestMapping(value = "/internetSmsPrice/delete", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO deleteInterSmsAction(Integer id, HttpSession session, HttpServletRequest request) {
		ResultVO result = ResultVO.failure();
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		Map resultMap = productService.deleteMsg(id, userSession.getId(), request.getRequestURI(),
				HttpUtils.getIpAddress(request));

		if ("success".equals(resultMap.get("result"))) {
			result.setSuccess(true);
			result.setMsg((String) resultMap.get("msg"));
		} else {
			result.setSuccess(false);
			result.setMsg((String) resultMap.get("msg"));
		}
		return result;
	}

	@RequestMapping(path = "/internetSmsPrice/add", method = RequestMethod.GET)
	public ModelAndView internetSmsPriceaAdd(ModelAndView mv) {
		mv.setViewName("product/internetSmsPrice/add");
		return mv;
	}

	@RequestMapping(value = "/internetSmsPrice/add", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO addInterSmsAction(Integer id, HttpSession session, HttpServletRequest request) {
		ResultVO result = ResultVO.failure();
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		Map<String, String> params = new HashMap<>();

		for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
			String key = e.nextElement();
			String value = request.getParameter(key);
			params.put(key, value);
		}

		params.put("userId", userSession.getId().toString());
		params.put("pageUrl", request.getRequestURI());
		params.put("ip", HttpUtils.getIpAddress(request));

		Map resultMap = productService.addInterSmsPrice(params);
		if ("success".equals(resultMap.get("result"))) {
			result.setSuccess(true);
			result.setMsg((String) resultMap.get("msg"));
		} else {
			result.setSuccess(false);
			result.setMsg((String) resultMap.get("msg"));
		}
		return result;
	}

	@RequestMapping(path = "/internetSmsPrice/edit", method = RequestMethod.GET)
	public ModelAndView internetSmsPriceaEdit(ModelAndView mv, Integer id) {
		Map data = productService.getInterSmsPriceById(id);
		mv.addObject("data", data);
		mv.setViewName("product/internetSmsPrice/edit");
		return mv;
	}

	@RequestMapping(value = "/internetSmsPrice/edit", method = RequestMethod.POST)
	@ResponseBody
	public ResultVO editInterSmsAction(Integer id, HttpSession session, HttpServletRequest request) {
		ResultVO result = ResultVO.failure();
		UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
		Map<String, String> params = new HashMap<>();

		for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
			String key = e.nextElement();
			String value = request.getParameter(key);
			params.put(key, value);
		}

		params.put("userId", userSession.getId().toString());
		params.put("pageUrl", request.getRequestURI());
		params.put("ip", HttpUtils.getIpAddress(request));

		Map resultMap = productService.updateInterSmsPrice(params);
		if ("success".equals(resultMap.get("result"))) {
			result.setSuccess(true);
			result.setMsg((String) resultMap.get("msg"));
		} else {
			result.setSuccess(false);
			result.setMsg((String) resultMap.get("msg"));
		}
		return result;
	}

	@RequestMapping(path = "/internetSmsPrice/exportExcel")
	public void agentQuery(String condition, HttpServletRequest request, HttpServletResponse response) {
		Map<String, String> params = new HashMap<String, String>();

		params.put("condition", condition);
		String filePath = ConfigUtils.save_path + "/国际短信价格配置报表.xls";

		Excel excel = new Excel();
		excel.setFilePath(filePath);
		excel.setTitle("国际短信价格配置报表");

		StringBuffer buffer = new StringBuffer();
		buffer.append("查询条件：");

		if (params.get("condition") != null) {
			buffer.append("  输入英文/中文/国际代码：");
			buffer.append(params.get("condition"));
			buffer.append(";");
		}
		excel.addRemark(buffer.toString());

		excel.addHeader(20, "国际名称", "intername");
		excel.addHeader(20, "中文名称", "areaname");
		excel.addHeader(20, "国际代码", "prefix");
		excel.addHeader(20, "国际简码", "intercode");
		excel.addHeader(20, "国际价格(元)", "fee");

		excel.setDataList(productService.exportInterSmsPrice(params));
		if (ExcelUtils.exportExcel(excel)) {
			FileUtils.download(response, filePath);
			FileUtils.delete(filePath);
		} else {
			String fullContentType = "text/plain;charset=UTF-8";
			response.setContentType(fullContentType);
			try {
				response.getWriter().write("导出Excel文件失败，请联系管理员");
				response.getWriter().flush();
			} catch (IOException e) {
				logger.error("导出Excel文件失败", e);
			}
		}
	}


	/**
	 * @param
	 * @Description: 获取产品列表
	 * @Author: tanjiangqiang
	 * @Date: 2017/11/14 - 14:04
	 */
	@PostMapping("/oem/products")
	@ResponseBody
	public PageContainer getOemProducts(@RequestParam Map<String, String> params) {
		PageContainer page = new PageContainer();
		try {
			if (params.isEmpty()) {
				logger.error("参数为空--------------{}"+JsonUtil.toJson(params));
                return page;
            }
			String agentId = params.get("agentId");
            if (StringUtils.isBlank(agentId)){
				logger.error("agentId为空--------------{}"+JsonUtil.toJson(params));
				return page;
			}
			// 查询产品列表
//			AgentInfo agentInfo = AgentUtils.queryAgentInfoByAgentId(agentId);

			params.put("agentId", agentId);
			params.put("orderByClause", "create_time DESC");
			page = productService.querySmsPurchaseProduct(params);
			logger.debug("获取产品列表为-------------{}" + page);
		} catch (Exception e) {
			e.printStackTrace();
			logger.debug("获取产品列表失败-------------{}" + e.getMessage());
		}
		return page;
	}



}
