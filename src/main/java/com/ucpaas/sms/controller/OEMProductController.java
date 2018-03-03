package com.ucpaas.sms.controller;

import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.common.enums.Code;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.enums.SessionEnum;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.service.common.OEMCommonService;
import com.ucpaas.sms.service.product.OEMProductService;
import com.ucpaas.sms.util.AgentUtils;
import com.ucpaas.sms.util.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/product/oem")
public class OEMProductController extends BaseController {
    private static Logger logger = LoggerFactory.getLogger(OEMProductController.class);

    @Autowired
    private OEMProductService oemProductService;

    @Autowired
    private OEMCommonService commonService;

    /**
     * 主页视图
     *
     * @param model
     * @return
     */
    @RequestMapping("/product")
    public String view(Model model,String page, HttpSession session) {
        List<Map<String, Object>> dataList = oemProductService.getAgentRebate(new HashMap<String, String>());
        model.addAttribute("dataList", dataList);
        model.addAttribute("page", page);

        UserSession user = getUserFromSession(session);
        model.addAttribute("menus", AgentUtils.hasMenuRight(user, "OEM产品包管理-OEMcpbgl", "OEM代理属性配置-OEMdlsxpz"));
        return "product/oem/list";
    }

    /**
     * 列表数据
     *
     * @param rows
     * @param page
     * @param condition
     * @param status
     * @param start_time_day
     * @param end_time_day
     * @return
     */
    @RequestMapping("/list")
    @ResponseBody
    public PageContainer list(String rows, String page, String condition, String status, String start_time_day, String end_time_day) {
        Map<String, String> params = new HashMap<>();
        params.put("pageRowCount", rows);
        params.put("currentPage", page);

        // 代理商ID/代理商名称/手机号码
        params.put("multi_text", condition);

        if (StringUtils.isNotBlank(status)) {
            params.put("status", status);
        }

        if (StringUtils.isNotBlank(start_time_day)) {
            params.put("start_time_day", start_time_day);
        }

        if (StringUtils.isNotBlank(end_time_day)) {
            params.put("end_time_day", end_time_day);
        }

        return oemProductService.queryProduct(params);
    }

    /**
     * 添加
     *
     * @param model
     * @return
     */
    @RequestMapping("/add")
    public String add(Model model) {
        List<String> params = new ArrayList<String>();
        params.add("OEM_GJ_SMS_DISCOUNT");
        params.add("OEM_PRODUCT_DUETIME");
        List<Map<String, Object>> dataList = commonService.getAgentParams(params);
        model.addAttribute("dataList", dataList);
        return "product/oem/add";
    }

    @RequestMapping("/add/save")
    @ResponseBody
    public ResultVO addSave(@RequestParam Map<String, String> params, HttpSession session, HttpServletRequest request) {
        ResultVO result = ResultVO.failure();
        // 需要用户Id
        UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        params.put("userId", userSession.getId().toString());
        params.put("pageUrl", request.getRequestURI());
        params.put("ip", HttpUtils.getIpAddress(request));

        if (params.get("unit_price") == null || "".equals(params.get("unit_price").toString())){
            params.remove("unit_price");
        } else {
            Integer unitPrice = Integer.valueOf(params.get("unit_price"));
            if (unitPrice <= 0) {
                return ResultVO.failure(Code.OPT_ERR, "短信单价不能小于等于0");
            }
        }

        Map data = oemProductService.addProduct(params);
        result.setSuccess("success".equals(data.get("result").toString()));
        result.setMsg(data.get("msg").toString());
        return result;
    }

    /**
     * 编辑/修改
     *
     * @param id
     * @param model
     * @return
     */
    @RequestMapping("/edit")
    public String edit(int id, Model model) {
        List<String> params = new ArrayList<String>();
        params.add("OEM_GJ_SMS_DISCOUNT");
        params.add("OEM_PRODUCT_DUETIME");
        List<Map<String, Object>> dataList = commonService.getAgentParams(params);
        Map<String, Object> data = oemProductService.getProductDetail(id);
        model.addAttribute("data", data);
        model.addAttribute("dataList", dataList);
        return "product/oem/edit";
    }

    @RequestMapping("/edit/save")
    @ResponseBody
    public ResultVO editSave(@RequestParam Map<String, String> params, HttpSession session, HttpServletRequest request) {
        ResultVO result = ResultVO.failure();
        // 需要用户Id
        UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        params.put("userId", userSession.getId().toString());
        params.put("pageUrl", request.getRequestURI());
        params.put("ip", HttpUtils.getIpAddress(request));

        if (params.get("unit_price") == null || "".equals(params.get("unit_price").toString())){
            params.remove("unit_price");
        }

        Map data = oemProductService.editProduct(params);
        result.setSuccess("success".equals(data.get("result").toString()));
        result.setMsg(data.get("msg").toString());
        return result;
    }

    /**
     * 详情
     *
     * @param id
     * @param model
     * @return
     */
    @RequestMapping("/view")
    public String detail(String id, Model model) {
        Map data;
        if (NumberUtils.isDigits(id)) {
            data = oemProductService.getProductDetail(Integer.parseInt(id));
        } else {
            data = new HashMap<String, Object>();
        }
        model.addAttribute("data", data);
        return "product/oem/view";
    }

    /**
     * 更新状态-上下架
     *
     * @param params
     * @param session
     * @param request
     * @return
     */
    @RequestMapping("/updateStatus")
    @ResponseBody
    public ResultVO updateStatus(@RequestParam Map<String, String> params, HttpSession session, HttpServletRequest request) {
        ResultVO result = ResultVO.failure();
        // 需要用户Id
        UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        params.put("userId", userSession.getId().toString());
        params.put("pageUrl", request.getRequestURI());
        params.put("ip", HttpUtils.getIpAddress(request));

        Map data = oemProductService.updateStatus(params);
        result.setSuccess("success".equals(data.get("result").toString()));
        result.setMsg(data.get("msg").toString());
        return result;
    }

    @RequestMapping("/agentParams")
    @ResponseBody
    public List<Map<String, Object>> agentParams(){
        List<String> params = new ArrayList<String>();
        params.add("OEM_GJ_SMS_DISCOUNT"); // OEM国际短信优惠比
        params.add("OEM_AGENT_REBATE_PRICE"); // OEM代理商返点结算单价
        params.add("OEM_PRODUCT_DUETIME"); // OEM产品到期时间
        return commonService.getAgentParams(params);
    }

    @RequestMapping("/addAgentParams")
    @ResponseBody
    public ResultVO addAgentParams(@RequestParam Map<String, String> params, HttpServletRequest request){
        UserSession userSession = (UserSession) request.getSession().getAttribute(SessionEnum.SESSION_USER.toString());
        params.put("adminId", userSession.getId().toString());
        params.put("pageUrl", request.getRequestURI());
        params.put("ip", HttpUtils.getIpAddress(request));

        ResultVO result = ResultVO.failure();
        Map data = commonService.addOrUpdateAgentParams(params);
        result.setSuccess("success".equals(data.get("result").toString()));
        result.setMsg(data.get("msg").toString());
        return result;
    }

    @RequestMapping("/addRebate")
    @ResponseBody
    public ResultVO addRebate(@RequestParam Map<String, String> params, HttpSession session, HttpServletRequest request) {
        ResultVO result = ResultVO.failure();

        // 需要用户Id
        UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        params.put("userId", userSession.getId().toString());
        params.put("pageUrl", request.getRequestURI());
        params.put("ip", HttpUtils.getIpAddress(request));

        Map data = null;
        
        if(StringUtils.isEmpty(params.get("start_line"))){
        	params.remove("start_line");
        }
        if(StringUtils.isEmpty(params.get("end_line"))){
        	params.remove("end_line");
        }
        
        try {
            data = commonService.addOrUpdateRebate(params);
        } catch (Exception e) {
            data = new HashMap<String,Object>();
            logger.debug(e.getMessage());
            data.put("result", "服务器异常,正在检修中...");
        }

        result.setSuccess("success".equals(data.get("result").toString()));
        result.setMsg(data.get("msg").toString());
        return result;
    }
}
