package com.ucpaas.sms.controller;

import com.jsmsframework.common.dto.ResultVO;
import com.opensymphony.oscache.util.StringUtil;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.entity.message.OrderList;
import com.ucpaas.sms.entity.po.OrderListPo;
import com.ucpaas.sms.entity.po.OrderProgressPo;
import com.ucpaas.sms.enums.SessionEnum;
import com.ucpaas.sms.model.Page;
import com.ucpaas.sms.service.account.ApplyRecordService;
import com.ucpaas.sms.service.order.OrderListService;
import com.ucpaas.sms.service.order.OrderProgressService;
import com.ucpaas.sms.util.AgentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

/**
 * Created by Niu.T on 2017/5/16.
 *
 */
@Controller
//@RequestMapping(value = "/orderlist")
@RequestMapping(value = "/demandOrder")
public class DemandOrderController extends  BaseController{
    private static Logger logger = LoggerFactory.getLogger(DemandOrderController.class);

    @Autowired
    private OrderListService orderListService;
    @Autowired
    private OrderProgressService orderProgressService;

    @Autowired
    private ApplyRecordService applyRecordService;

    /**
     * 订单管理 --> route
     * @returns
     */
    @GetMapping(value = "/list")
    public ModelAndView orderListView(ModelAndView mv, HttpSession session) {
        mv.setViewName("demandOrder/order-list");
        UserSession user = getUserFromSession(session);
        mv.addObject("menus", AgentUtils.hasMenuRight(user, "我提交的-wtjd-259"));
        return mv;
    }

    /**
     * 订单管理 - 加载数据 --> action
     * @param params
     * @return
     */
    @PostMapping(value = "/list")
    @ResponseBody
    public Page orderList(Page<OrderListPo> page, @RequestParam Map params,HttpServletRequest request){
        UserSession user = (UserSession) request.getSession().getAttribute(SessionEnum.SESSION_USER.toString());
        params.put("operatorId", user.getId());
        page.setParams(params);
        return orderListService.queryListAndDemanState(page);
    }

    /**
     * 提交新订单 --> route
     */
    @GetMapping(value = "/add")
    public ModelAndView submitView(ModelAndView mv){
        mv.setViewName("demandOrder/order-submit");
        return mv;
    }

    /**
     * 提交新订单 --> action
     * @return
     */
    @PostMapping(value = "/add")
    @ResponseBody
    public ResultVO submitAction(@Valid OrderList orderList, BindingResult errorResult, HttpServletRequest request) {
        String onlineDate = request.getParameter("onlineDate");
        String salePriceFlag = request.getParameter("salePriceFlag");
        String smsTypeFlag = request.getParameter("smsTypeFlag");

        if(!StringUtil.isEmpty(onlineDate)){
            try {
                    orderList.setOnlineDate( new SimpleDateFormat("yyyy-MM-dd").parse(onlineDate));
            } catch (ParseException e) {
                orderList.setOnlineDate(null);
                logger.debug("要求上线时间 转换时间失败 --> {}",onlineDate);
            }
        }
        ResultVO result = ResultVO.failure();
        if (!validateParam(errorResult, result)) {
            return result;
        }
        try {
            orderList.setSalePrice(salePriceFlag + ":"+ orderList.getSalePrice());
            if("特殊营销".equals(smsTypeFlag) || "其他".equals(smsTypeFlag) ){
                orderList.setSmsType(smsTypeFlag + ":"+ orderList.getSmsType());
            }
            UserSession user = (UserSession) request.getSession().getAttribute(SessionEnum.SESSION_USER.toString());
            orderList.setOperatorId(user.getId());
            result = orderListService.insert(orderList);
        } catch (Exception e) {
            logger.error(result.getMsg(), e);
        }
        return result;
    }

    /**
     * 编辑订单 --> route
     */
    @GetMapping(value = "/{orderId}/edit")
    public ModelAndView edit(ModelAndView mv,@PathVariable String orderId){
        OrderListPo poById = orderListService.getPoById(orderId);
        mv.addObject("model", poById);
        mv.addObject("saleList", applyRecordService.getSaleList());
        mv.setViewName("demandOrder/order-edit");
        return mv;
    }

    /**
     * 修改订单 --> action
     */
    @PostMapping(value = "/edit")
    @ResponseBody
    public ResultVO editAction(@Valid OrderList orderList, BindingResult errorResult, HttpServletRequest request) {

        String onlineDate = request.getParameter("onlineDate");
        String salePriceFlag = request.getParameter("salePriceFlag");
        String updateTime = request.getParameter("updateTime");
        String smsTypeFlag = request.getParameter("smsTypeFlag");
        if(!StringUtil.isEmpty(onlineDate)){
            try {
                orderList.setOnlineDate( new SimpleDateFormat("yyyy-MM-dd").parse(onlineDate));
            } catch (ParseException e) {
                orderList.setOnlineDate(null);
                logger.debug("要求上线时间 转换时间失败 --> {}",onlineDate);
            }
        }
        ResultVO result = ResultVO.failure();
        if (!validateParam(errorResult, result)) {
            return result;
        }
        try {
            orderList.setSalePrice(salePriceFlag + ":"+ orderList.getSalePrice());
            if("特殊营销".equals(smsTypeFlag) || "其他".equals(smsTypeFlag) ){
                orderList.setSmsType(smsTypeFlag + ":"+ orderList.getSmsType());
            }
            orderList.setUpdateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(updateTime));
            UserSession user = (UserSession) request.getSession().getAttribute(SessionEnum.SESSION_USER.toString());
            orderList.setOperatorId(user.getId());
            orderList.setState(0);
            result = orderListService.updateSelective(orderList);
        } catch (Exception e) {
            logger.error(result.getMsg(), e);
        }
        return result;
    }

    /**
     * 验证参数中是否有错误
     * @param errorResult
     * @param resultVO
     * @return
     */
    private boolean validateParam(BindingResult errorResult,ResultVO resultVO){
        if (errorResult.hasErrors()) {
            for (ObjectError error : errorResult.getAllErrors()) {
                String[] codes = error.getCodes();
                boolean flag = false;
                for (String code : codes) {
                    if (code.contains("onlineDate")|| code.contains("updateTime")){
                        flag = true;
                        break;
                    }
                }
                if(flag){
                    continue;
                }
                resultVO.setFail(true);
                resultVO.setMsg(error.getDefaultMessage());
                return false;
            }
        }
        return true;
    }


    /**
     * 查看订单 --> route
     */
//    @RequestMapping(value = "/getDetail",method = RequestMethod.GET)
    @GetMapping(value = "/{orderId}/view")
    @ResponseBody
    public ModelAndView getDetail(ModelAndView mv,@PathVariable String orderId){
        OrderListPo poById = orderListService.getPoById(orderId);
//        String channelType = poById.getChannelType().replace(",","+");
//        channelType = channelType.replace("0", "三网合一");
//        channelType = channelType.replace("1", "移动");
//        channelType = channelType.replace("2", "电信");
//        channelType = channelType.replace("3", "联通");
//        channelType = channelType.replace("4", "全网");
//        poById.setChannelType(channelType);

        //0：自定义，1：报备签名，2：固签
//        String signType = poById.getSignType().replace(",","+");
//        signType = signType.replace("0", "自定义");
//        signType = signType.replace("1", "报备签名");
//        signType = signType.replace("2", "固签");
//        poById.setSignType(signType);
        List<OrderProgressPo> orderProgresses = orderProgressService.queryOrderProgressList(orderId);
        mv.addObject("model", poById);
        mv.addObject("progressList", orderProgresses);
        mv.setViewName("demandOrder/order-detail");
        return mv;
    }

    /**
     * 修改订单状态 --> action
     */
//    @RequestMapping(value = "/updateStatus",method = RequestMethod.POST)
//    @ResponseBody
//    public ResultVO updateStatusAction(OrderList orderList, HttpServletRequest request) {
//
//        ResultVO result = ResultVO.failure();
//
//        try {
//            UserSession user = (UserSession) request.getSession().getAttribute(SessionEnum.SESSION_USER.toString());
//            orderList.setOperatorId(user.getId());
//            result = orderListService.updateOrderState(orderList);
//        } catch (Exception e) {
//            logger.error(result.getMsg(), e);
//        }
//        return result;
//    }

    
    /**
     * 获取订单详情
     */
    @GetMapping(value = "/{orderId}")
	@ResponseBody
	public ResultVO view(ModelAndView mv, @PathVariable("orderId") String orderId) {
		ResultVO resultVO = new ResultVO();
		OrderListPo model = orderListService.getPoById(orderId);
		resultVO.setSuccess(true);
		resultVO.setMsg("查询成功！");
		resultVO.setData(model);
		return resultVO;
	}

	@PostMapping(value = "/chedan")
	@ResponseBody
	public ResultVO chedan(HttpServletRequest request, String orderId) {

		ResultVO result = ResultVO.failure();

		try {
			UserSession user = (UserSession) request.getSession().getAttribute(SessionEnum.SESSION_USER.toString());
			result = orderListService.chedan(orderId, user.getId());
		} catch (Exception e) {
			logger.error("撤单操作失败", e);
			result.setMsg(e.getMessage());
		}
		return result;
	}

}
