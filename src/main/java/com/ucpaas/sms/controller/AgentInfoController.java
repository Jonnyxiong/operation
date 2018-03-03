package com.ucpaas.sms.controller;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.common.util.JsonUtil;
import com.jsmsframework.common.util.StringUtils;
import com.jsmsframework.order.entity.JsmsOemAgentPool;
import com.jsmsframework.order.product.exception.JsmsOEMAgentOrderProductException;
import com.jsmsframework.order.service.JsmsOemAgentPoolService;
import com.jsmsframework.user.entity.JsmsAccount;
import com.jsmsframework.user.service.JsmsAccountService;
import com.ucpaas.sms.common.entity.R;
import com.ucpaas.sms.dto.AgentRequest;
import com.ucpaas.sms.dto.PurchaseOrder;
import com.ucpaas.sms.dto.PurchaseOrderVO;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.enums.AgentType;
import com.ucpaas.sms.enums.SessionEnum;
import com.ucpaas.sms.exception.AgentException;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.po.ProductInfo;
import com.ucpaas.sms.po.SaleEntityPo;
import com.ucpaas.sms.service.AgentInfoService;
import com.ucpaas.sms.service.account.ApplyRecordService;
import com.ucpaas.sms.service.customer.CustomerManageService;
import com.ucpaas.sms.service.product.ProductService;
import com.ucpaas.sms.util.AgentUtils;
import com.ucpaas.sms.util.ConfigUtils;
import com.ucpaas.sms.util.HttpUtils;
import com.ucpaas.sms.util.PageConvertUtil;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/agentInfo")
public class AgentInfoController extends BaseController {

    private static Logger logger = LoggerFactory.getLogger(AgentInfoController.class);

    @Autowired
    private ApplyRecordService applyRecordService;

    @Autowired
    private AgentInfoService agentInfoService;

    @Autowired
    private JsmsOemAgentPoolService jsmsOemAgentPoolService;

    @Autowired
    private JsmsAccountService jsmsAccountService;

    @Autowired
    private ProductService productService;
    /**
     *客户开户---客户开户路由
     * @param mv
     * @return
     */
    @RequestMapping(path = "/add", method = RequestMethod.GET)
    public ModelAndView addPage(ModelAndView mv, HttpSession session) {
        UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        boolean isSale = false;
        if(userSession.getRoles().size()>0){
            for(int i=0;i<userSession.getRoles().size();i++){
                if(userSession.getRoles().get(i).getRoleName().equals("销售人员")){
                    isSale = true;
                }
            }
        }
       /*重复加载
       List<SaleEntityPo> saleList = applyRecordService.getSaleList();
        mv.addObject("saleList", saleList);*/
        // 测试产品列表
        List<ProductInfo> productInfoList = applyRecordService.getProductInfoListForOemData();
        mv.addObject("productInfoList", productInfoList);

        String smspImgUrl = ConfigUtils.smsp_img_url.endsWith("/")
                ? ConfigUtils.smsp_img_url.substring(0, ConfigUtils.smsp_img_url.lastIndexOf("/"))
                : ConfigUtils.smsp_img_url;
        mv.addObject("smsp_img_url", smspImgUrl);
        mv.addObject("smspImgUrl", smspImgUrl);
        mv.addObject("clientSiteUrl",ConfigUtils.client_site_url);
        if(isSale){
            mv.setViewName("/accountInfo/agent/addSale");
        }else{
            mv.setViewName("/accountInfo/agent/add");
        }
        UserSession user = getUserFromSession(session);
        mv.addObject("menus", AgentUtils.hasMenuRight(user, "直客开户-zkkh", "子账户开户-zzhkh", "客户开户-khkh"));
        return mv;
    }

    @ApiOperation(value = "客户开户", notes = "客户开户", tags = "客户开户", response = R.class)
    @ApiImplicitParams({@ApiImplicitParam(name = "agentType", value = "代理商类型", dataType = "int",required = true, paramType="query"),
            @ApiImplicitParam(name = "company", value = "代理商名称", dataType = "string",required = true, paramType = "query"),
            @ApiImplicitParam(name = "address", value = "联系地址", dataType = "string",required = true, paramType = "query"),
            @ApiImplicitParam(name = "email", value = "邮箱", dataType = "string",required = true, paramType = "query"),
            @ApiImplicitParam(name = "mobile", value = "手机号码", dataType = "string",required = true, paramType = "query"),
            @ApiImplicitParam(name = "belongSale", value = "归属销售", dataType = "long",required = true, paramType = "query"),
            @ApiImplicitParam(name = "password", value = "登入密码", dataType = "string",required = true, paramType = "query"),
            @ApiImplicitParam(name = "testProductId", value = "测试短信产品id", dataType = "int",required = true, paramType = "query"),
            @ApiImplicitParam(name = "testSmsNumber", value = "赠送的测试短信条数", dataType = "int",required = true, paramType = "query"),
            @ApiImplicitParam(name = "idType", value = "证件类型", dataType = "string",required = true, paramType = "query"),
            @ApiImplicitParam(name = "idNbr", value = "证件号码", dataType = "string",required = true, paramType = "query"),
            @ApiImplicitParam(name = "imgUrl", value = "证件图片", dataType = "string",required = true, paramType = "query"),
            @ApiImplicitParam(name = "domainName", value = "域名", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "copyrightText", value = "版权文字", dataType = "string",required = true, paramType = "query"),
            @ApiImplicitParam(name = "navigationBackcolor", value = "导航栏背景色", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "navigationTextColor", value = "导航栏文字颜色", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "tabLogoUrl", value = "标签页图标", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "logoUrl", value = "LOGO图", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "companyLogoUrl", value = "公司名称图", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "apiDocumentUrl", value = "接口文档", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "fAQDocumentUrl", value = "FAQ文档", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "name", value = "子账户名称", dataType = "string",required = true, paramType = "query"),
            @ApiImplicitParam(name = "paytype", value = "付费方式", dataType = "int",required = true, paramType = "query"),
            @ApiImplicitParam(name = "chargeRule", value = "计费规则", dataType = "int",required = true, paramType = "query"),
            @ApiImplicitParam(name = "agentOwned", value = "使用对象", dataType = "int",required = true, paramType = "query"),
            @ApiImplicitParam(name = "smsfrom", value = "短信协议", dataType = "int",required = true, paramType = "query"),
            @ApiImplicitParam(name = "httpProtocolType", value = "http子协议", dataType = "int",required = true, paramType = "query"),
            @ApiImplicitParam(name = "needreport", value = "状态报告获取方式", dataType = "int",required = true, paramType = "query"),
            @ApiImplicitParam(name = "needmo", value = "上行获取方式", dataType = "int",required = true, paramType = "query"),
            @ApiImplicitParam(name = "mourl", value = "上行回调地址", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "deliveryurl", value = "状态报告回调地址", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "noticeurl", value = "模版审核通知回调地址", dataType = "string", paramType = "query"),
            @ApiImplicitParam(name = "ip", value = "IP白名单", dataType = "string",required = true, paramType = "query"),
            @ApiImplicitParam(name = "needextend", value = "自扩展", dataType = "int",required = true, paramType = "query"),
            @ApiImplicitParam(name = "remark", value = "备注", dataType = "string",required = true, paramType = "query")
    })
    @RequestMapping(path = "/add", method = RequestMethod.POST)
    @ResponseBody
    public R add(@RequestBody AgentRequest agentRequest,HttpSession session, HttpServletRequest request) {
        //默认为oem代理商
        agentRequest.setAgentType(AgentType.OEM代理商.getValue());
        String msg = "新增代理商失败";
        UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        boolean isSale = false;
        if(userSession.getRoles().size()>0){
            for(int i=0;i<userSession.getRoles().size();i++){
                if(userSession.getRoles().get(i).getRoleName().equals("销售人员")){
                    isSale = true;
                }
            }
        }
        R r;
        try {
            r = agentInfoService.insert(agentRequest, userSession.getId(), request.getRequestURI(), HttpUtils.getIpAddress(request),isSale);
        } catch (AgentException e) {
            logger.error("新增代理商失败", e);
            r = R.error(e.getMessage());
        } catch (Exception e) {
            logger.error("新增代理商失败", e);
            r = R.error(msg);
        }

        return r;
    }


    /**
     * @Description: 获取代理商池信息
     * @Author: tanjiangqiang
     * @Date: 2017/11/20 - 9:28
     * @param clientId 客户id
     *
     */
    @RequestMapping(path = "/agentpools/{clientId}", method = RequestMethod.GET)
    @ApiOperation(value = "/agentpools/{clientId}", notes = "代理商池信息", response = PageContainer.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "clientId", value = "客户id", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "page", value = "当前页码", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "rows", value = "每页行数", dataType = "String", paramType = "query")})
    @ResponseBody
    public PageContainer getAgentPools(@PathVariable("clientId") String clientId, @RequestParam Map<String, String> params) {
        PageContainer pageContainer = new PageContainer();
        try {
            //根据clientId查出归属代理商信息
            JsmsAccount jsmsAccount = jsmsAccountService.getByClientId(clientId);
            JsmsOemAgentPool jsmsOemAgentPool = new JsmsOemAgentPool();
            jsmsOemAgentPool.setAgentId(jsmsAccount.getAgentId());
            if (StringUtils.isNotBlank(params.get("productType"))) {
                jsmsOemAgentPool.setProductType(Integer.parseInt(params.get("productType")));
            }
            JsmsPage<JsmsOemAgentPool> jsmsPage;
            if (StringUtils.isNotBlank(params.get("operatorCode"))) {
                jsmsOemAgentPool.setOperatorCode(Integer.parseInt(params.get("operatorCode")));
            }
            if (StringUtils.isNotBlank(params.get("page")) || StringUtils.isNotBlank(params.get("rows"))) {
                Integer page = Integer.parseInt(params.get("page"));
                Integer rows = Integer.parseInt(params.get("rows"));
                jsmsPage = new JsmsPage<>(page, rows);
            }else{
                jsmsPage = new JsmsPage<>();
            }
            //查询出该代理商的池信息
            List<JsmsOemAgentPool> result = jsmsOemAgentPoolService.getListByAgentPoolInfo(jsmsOemAgentPool);
            //迭代遍历，删除剩余数量为0的和国际短信剩余金额为0或为空的
            Iterator<JsmsOemAgentPool> resultIterator = result.iterator();
            while (resultIterator.hasNext()){
                JsmsOemAgentPool oemAgentPool = resultIterator.next();
                if (oemAgentPool.getRemainNumber() <= 0){
                    if ( null == oemAgentPool.getRemainAmount() || oemAgentPool.getRemainAmount().compareTo(BigDecimal.ZERO) <= 0) {
                        resultIterator.remove();
                    }
                }
            }
            jsmsPage.setData(result);
            jsmsPage.setTotalRecord(result.size());
            pageContainer = PageConvertUtil.pageToContainer(jsmsPage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pageContainer;
    }






    /**
     * @param
     * @Description: 短信购买，生成订单
     * @Author: tanjiangqiang
     * @Date: 2017/11/14 - 14:06
     */
    @PostMapping("/submitOrder")
    @ResponseBody
    public ResultVO submitOrder(@RequestBody PurchaseOrderVO purchaseOrderList, HttpSession session) {
        ResultVO result = null;
        try {
            UserSession user = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
            List<PurchaseOrder> purchaseOrders = purchaseOrderList.getPurchaseList();
            if (purchaseOrders.isEmpty()) {
                logger.error("购买参数为空----------------{}"+JsonUtil.toJson(purchaseOrders));
                return ResultVO.failure("请选择正确的购买数量");
            }
            Iterator<PurchaseOrder> purchaseOrderIteratorr = purchaseOrders.iterator();
            while (purchaseOrderIteratorr.hasNext()){
                PurchaseOrder purchaseOrder = purchaseOrderIteratorr.next();
                if (purchaseOrder.getPurchaseNum().compareTo(BigDecimal.ZERO) < 1) {
                    purchaseOrderIteratorr.remove();
                }
            }
            if (purchaseOrders.isEmpty()) {
                logger.error("购买参数为空----------------{}"+JsonUtil.toJson(purchaseOrderList));
                return ResultVO.failure("请选择正确的购买数量");
            }
            Integer agentId = purchaseOrderList.getAgentId();
            if (null == agentId || StringUtils.isBlank(agentId.toString())) {
                return ResultVO.failure("请选择要充值的代理商");
            }
            result = productService.confirmPurchaseOrder(purchaseOrders, agentId, user.getId());

        } catch (JsmsOEMAgentOrderProductException e) {
            logger.debug("购买订单失败:{} ----------------------------> {}", e.getMessage(), e);
            return ResultVO.failure(e.getMessage());
        } catch (Exception e) {
            logger.error("购买产品包失败:{} , purchaseOrder --> {},----------------------------> {}", e.getMessage(), JsonUtil.toJson(purchaseOrderList.getPurchaseList()), e);
            return ResultVO.failure("服务器异常,正在检修中...");
        }
        return result;
    }
}
