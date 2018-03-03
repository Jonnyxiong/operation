package com.ucpaas.sms.controller.finance.invoice;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.common.enums.Code;
import com.jsmsframework.common.enums.invoice.InvoiceBodyEnum;
import com.jsmsframework.common.enums.invoice.InvoiceConfigStatus;
import com.jsmsframework.common.enums.invoice.InvoiceTypeEnum;
import com.jsmsframework.finance.dto.JsmsAgentInvoiceConfigDto;
import com.jsmsframework.finance.dto.JsmsAgentInvoiceListDTO;
import com.jsmsframework.finance.entity.JsmsAgentInvoiceConfig;
import com.jsmsframework.finance.entity.JsmsAgentInvoiceList;
import com.jsmsframework.finance.service.JsmsAgentInvoiceConfigService;
import com.jsmsframework.finance.service.JsmsAgentInvoiceListService;
import com.jsmsframework.user.entity.JsmsAgentInfo;
import com.jsmsframework.user.entity.JsmsMenu;
import com.jsmsframework.user.entity.JsmsUser;
import com.jsmsframework.user.finance.service.JsmsUserFinanceService;
import com.jsmsframework.user.service.JsmsAgentInfoService;
import com.jsmsframework.user.service.JsmsUserService;
import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.common.util.file.ExcelUtils;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.entity.po.DataAuthorityCondition;
import com.ucpaas.sms.enums.AccountEnums;
import com.ucpaas.sms.enums.AgentType;
import com.ucpaas.sms.enums.OptType;
import com.ucpaas.sms.enums.SessionEnum;
import com.ucpaas.sms.model.Excel;
import com.ucpaas.sms.model.PageContainer;
import com.ucpaas.sms.po.AgentInvoicePo;
import com.ucpaas.sms.service.admin.AuthorityService;
import com.ucpaas.sms.service.finance.invoice.AgentInvoiceService;
import com.ucpaas.sms.util.*;
import com.ucpaas.sms.util.rest.utils.DateUtil;
import io.swagger.annotations.*;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.collections.CollectionUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Api("发票管理-发票信息")
@Controller
@RequestMapping("/finance/invoice/info")
public class InvoiceInfoController {
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    @Autowired
    private AgentInvoiceService agentInvoiceService;

    @Autowired
    private JsmsAgentInvoiceConfigService jsmsAgentInvoiceConfigService;

    @Autowired
    private JsmsUserFinanceService jsmsUserFinanceService;

    @Autowired
    private JsmsAgentInfoService jsmsAgentInfoService;

    @Autowired
    private JsmsUserService jsmsUserService;

    @Autowired
    private AuthorityService authorityService;

    @ApiOperation(value = "发票信息主页面", notes = "发票信息主页面", tags = "发票管理-发票信息", response = R.class)
    @GetMapping("/list")
    public ModelAndView view(ModelAndView mv, HttpSession session) {
        UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        mv.addObject("max_export_num", ConfigUtils.max_export_excel_num);
        mv.addObject("menus", AgentUtils.hasMenuRight(userSession, "发票申请-fpsq", "申请记录-sqjl", "发票信息-fpxx", "发票审核-fpsh"));
        JsmsMenu jsmsMenu = authorityService.getJsmsMenu("发票信息");
        mv.addObject("jsmsMenu", jsmsMenu);
        mv.setViewName("finance/invoice/info/list");
                        //finance/invoice/audit/list
        return mv;
    }

    /*
     * 发票信息列表
     */
    @ApiOperation(value = "发票信息列表", notes = "发票信息列表", tags = "发票管理-发票信息", response = R.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fourItem", value = "客户ID/客户名称/手机号码/销售名字", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "belongSaleId", value = "所属销售", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "invoiceType", value = "发票类型(1,普通发票, 2,增值税专票）", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "updateTimeStart", value = "最后修改时间开始", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "updateTimeEnd", value = "最后修改时间结束", dataType = "String", paramType = "form"),

    })
    @RequestMapping("/invoiceList")
    @ResponseBody
    public R invoiceList(HttpSession session, @RequestParam Map<String, String> params) {
        JsmsPage jpage = new JsmsPage();
        if (!StringUtils.isBlank(params.get("rows"))) {
            jpage.setRows(Integer.parseInt(params.get("rows")));
        }
        if (!StringUtils.isBlank(params.get("page"))) {
            jpage.setPage(Integer.parseInt(params.get("page")));
        }
        UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        Long id = userSession.getId();
        DataAuthorityCondition dataAuthorityCondition = AgentUtils.getDataAuthorityCondition(Long.valueOf(id), true, false);
        List<Long> ids = dataAuthorityCondition.getIds();
        mapDeal(params);
        jpage.setParams(params);
        jpage.getParams().put("belongSaleIds",ids);
        jpage.setOrderByClause("create_time desc");

        jpage.getParams().put("status",0);


        List<JsmsAgentInvoiceConfigDto> jsmsAgentInvoiceConfigs = jsmsAgentInvoiceConfigService.queryListForInvoice(jpage);
        jpage.setData(jsmsAgentInvoiceConfigs);
        //PageContainer container = PageConvertUtil.pageToContainer(jpage);
        return R.ok("获取发票列表信息成功",jpage);
    }
    @ApiOperation(value = "发票信息", notes = "发票信息", tags = "发票管理-发票信息", response = R.class)
    @RequestMapping("/invoiceById")
    @ResponseBody
    public R invoiceById( @RequestParam Integer id) {
        JsmsPage jpage = new JsmsPage();
        jpage.getParams().put("id",id);
        jpage.getParams().put("status",0);
        List<JsmsAgentInvoiceConfigDto> jsmsAgentInvoiceConfigs = jsmsAgentInvoiceConfigService.queryListForInvoice(jpage);
        //JsmsAgentInvoiceConfig jsmsAgentInvoiceConfig = jsmsAgentInvoiceConfigService.getById(id);
        if(CollectionUtils.isEmpty(jsmsAgentInvoiceConfigs)){
            return R.error(Code.SYS_ERR, "获取发票信息成功");
        }else {
            return R.ok("获取发票信息成功",jsmsAgentInvoiceConfigs.get(0));
        }

    }
    /*
     * 获取归属销售
     */
   /* @ApiOperation(value = "归属销售", notes = "归属销售", tags = "发票管理-发票信息", response = R.class)
    @RequestMapping("/salesList")
    @ResponseBody
    public R salesList(HttpSession session, @RequestParam Map<String, Object> params) {
        ResultVO resultVO = ResultVO.failure();

        List<JsmsAgentInvoiceList> jsmsAgentInvoiceList = jsmsAgentInvoiceListService.findList(params);
        List<AgentInvoicePo> agentInvoicePos = new ArrayList<>();
        for (JsmsAgentInvoiceList jsmsAgentInvoice : jsmsAgentInvoiceList) {
            AgentInvoicePo agentInvoicePo = new AgentInvoicePo();
            BeanUtils.copyProperties(jsmsAgentInvoice, agentInvoicePo);
            Integer agentId = jsmsAgentInvoice.getAgentId();
            JsmsAgentInfo jsmsAgentInfo = jsmsAgentInfoService.getByAgentId(agentId);
            if (jsmsAgentInfo != null) {
                JsmsUser jsmsUser = jsmsUserService.getById(jsmsAgentInfo.getBelongSale().toString());

                if (jsmsUser != null) {
                    agentInvoicePo.setSalerId(jsmsUser.getId());
                    agentInvoicePo.setSaler(jsmsUser.getRealname());
                } else {
                    continue;
                }
            } else {
                continue;
            }
            agentInvoicePos.add(agentInvoicePo);
        }
        return R.ok("获取销售成功",agentInvoicePos);
    }*/

    /*
     * 获取开票客户
     */
    @ApiOperation(value = "获取开票客户", notes = "获取开票客户", tags = "发票管理-发票信息", response = R.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "searchContent", value = "客户id/客户名称", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "invoiceType", value = "发票类型/1普票 2增票", dataType = "String", paramType = "form")
    })
    @RequestMapping("/agentList")
    @ResponseBody
    public R agentList(HttpSession session,@RequestParam Map<String,String> params) {
        UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        Long id = userSession.getId();
        DataAuthorityCondition dataAuthorityCondition = AgentUtils.getDataAuthorityCondition(Long.valueOf(id), true, false);
        List<Long> belong_salers = dataAuthorityCondition.getIds();
        //params.put("belong_salers",belong_salers);
        JsmsAgentInfo model = new JsmsAgentInfo();
        model.setAgentType(AgentType.OEM代理商.getValue());

        JsmsPage jpage = new JsmsPage();
        jpage.setRows(Integer.MAX_VALUE);
        jpage.setParams(params);
        jpage.getParams().put("belong_salers",belong_salers);

        List<JsmsAgentInfo> jsmsAgentInfos = jsmsAgentInfoService.getAgentInfoNotInInvoiceAuth(jpage);
        return R.ok("客户列表获取成功",jsmsAgentInfos);
    }

    /*
     * 修改收件人信息
     */
    @ApiOperation(value = "收件人信息", notes = "新增/修改收件人信息", tags = "发票管理-发票信息", response = R.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "修改主键", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "toName", value = "收件人", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "toPhone", value = "接受手机号", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "toAddr", value = "地址", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "toAddrDetail", value = "详细地址", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "toQq", value = "QQ", dataType = "String", paramType = "form")
    })
    @RequestMapping("/receiverInfo")
    @ResponseBody
    public R receiverInfo(HttpSession session,@RequestParam Map<String, String> params) {
        if (StringUtils.isBlank(params.get("id"))) {
            return R.error(Code.SYS_ERR, "id为必须字段");
        }
        if (StringUtils.isBlank(params.get("toName"))) {
            return R.error(Code.SYS_ERR, "收件人不能为空");
        }
        if (StringUtils.isBlank(params.get("toPhone"))) {
            return R.error(Code.SYS_ERR, "手机号不能为空");
        }else {
            String toPhone=params.get("toPhone").toString();
                String regExp = "^((13[0-9])|(15[0-9])|(18[0-9])|(17[0-9])|(147))\\d{8}$";
                Pattern p = Pattern.compile(regExp);
                Matcher m = p.matcher(toPhone);
                if(m.matches()==false){
                    return R.error(Code.SYS_ERR, "手机号格式错误");
                }


        }
        if (StringUtils.isBlank(params.get("toAddr"))) {
            return R.error(Code.SYS_ERR, "收件地址不能为空");
        }
        if (StringUtils.isBlank(params.get("toAddrDetail"))) {
            return R.error(Code.SYS_ERR, "收件详细地址不能为空");
        }
        if (!StringUtils.isBlank(params.get("toQq"))) {
            if(params.get("toQq").toString().length()>20)
            return R.error(Code.SYS_ERR, "请输入正确的QQ号");
        }
        if(updateReceiver(params)){
            return R.ok("修改收件信息成功");
        }
        return R.error(Code.SYS_ERR, "修改收件信息成功");
    }


    /*
     * 新增信息
     */
    @ApiOperation(value = "新增发票信息", notes = "新增发票信息", tags = "发票管理-发票信息", response = R.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "主键", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "optType", value = "操作类型（1新增，2修改，3删除）", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "agingId", value = "代理商ID", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "invoiceBody", value = "发票主体", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "invoiceHead", value = "发票抬头", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "creditCode", value = "社会信用号", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "invoiceType", value = "发票类型", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "companyRegAddr", value = "公司地址", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "bank", value = "开户银行", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "bankAccount", value = "开户账号", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "telphone", value = "公司电话", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "email", value = "电子邮箱", dataType = "String", paramType = "form"),

    })
    @RequestMapping("/optInvoice")
    @ResponseBody
    public R optInvoice(HttpSession session, @RequestParam Map<String, String> params) {
        UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        Long id = userSession.getId();
        if (StringUtils.isBlank(params.get("agingId"))) {
            return R.error(Code.SYS_ERR, "代理商ID为空");
        }
        if (OptType.删除.getValue() == Integer.valueOf(params.get("optType"))) {
            if (StringUtils.isBlank(params.get("id"))) {
                return R.error(Code.SYS_ERR, "发票主键不能为空");
            }
            if(deleteInvoice(params,id)){
                return R.ok("删除成功");
            }else {
                return R.error(Code.SYS_ERR, "删除失败");
            }
        }else if(OptType.修改.getValue() == Integer.valueOf(params.get("optType"))){
            if (StringUtils.isBlank(params.get("id"))) {
                return R.error(Code.SYS_ERR, "发票主键不能为空");
            }
        }



        if (StringUtils.isBlank(params.get("invoiceHead"))) {
            return R.error(Code.SYS_ERR, "发票抬头为空");
        }
        if (StringUtils.isBlank(params.get("invoiceType"))) {
            return R.error(Code.SYS_ERR, "发票类型为空");
        }
        if((!InvoiceBodyEnum.个人.equals(params.get("invoiceBody"))&&(InvoiceTypeEnum.普通发票.getValue().equals(params.get("invoiceType"))))){
            if (StringUtils.isBlank(params.get("creditCode"))) {
                return R.error(Code.SYS_ERR, "社会信用号为空");
            }
        }



        if (InvoiceTypeEnum.增值税专票.getValue().toString().equals(params.get("invoiceType"))) {
            if (StringUtils.isBlank(params.get("companyRegAddr"))) {
                return R.error(Code.SYS_ERR, "公司地址为空");
            }
            if (StringUtils.isBlank(params.get("bank"))) {
                return R.error(Code.SYS_ERR, "开户银行为空");
            }
            if (StringUtils.isBlank(params.get("bankAccount"))) {
                return R.error(Code.SYS_ERR, "开户账号为空");
            }
            if (StringUtils.isBlank(params.get("telphone"))) {
                return R.error(Code.SYS_ERR, "公司电话为空");
            }


            if (StringUtils.isBlank(params.get("toName"))) {
                return R.error(Code.SYS_ERR, "收件人不能为空");
            }
            if (StringUtils.isBlank(params.get("toPhone"))) {
                return R.error(Code.SYS_ERR, "手机号不能为空");
            }else {
                String toPhone=params.get("toPhone").toString();
                String regExp = "^((13[0-9])|(15[0-9])|(18[0-9])|(17[0-9])|(147))\\d{8}$";
                Pattern p = Pattern.compile(regExp);
                Matcher m = p.matcher(toPhone);
                if(m.matches()==false){
                    return R.error(Code.SYS_ERR, "手机号格式错误");
                }


            }
            if (StringUtils.isBlank(params.get("toAddr"))) {
                return R.error(Code.SYS_ERR, "收件地址不能为空");
            }
            if (StringUtils.isBlank(params.get("toAddrDetail"))) {
                return R.error(Code.SYS_ERR, "收件详细地址不能为空");
            }
            if (!StringUtils.isBlank(params.get("toQq"))) {
                if(params.get("toQq").toString().length()>20)
                    return R.error(Code.SYS_ERR, "请输入正确的QQ号");
            }
        } else if ((InvoiceTypeEnum.普通发票.getValue().toString()).equals((params.get("invoiceType")))) {
            if (StringUtils.isBlank(params.get("email"))) {
                return R.error(Code.SYS_ERR, "电子邮箱为空");
            }else {
                String email=params.get("email").toString();
                String regex = "^(.+)@(.+)$";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher((CharSequence) email);
                if(matcher.matches()==false){
                    return R.error(Code.SYS_ERR, "电子邮件格式错误");
                }


            }
            if (StringUtils.isBlank(params.get("invoiceBody"))) {
                return R.error(Code.SYS_ERR, "发票主体为空");
            }
        }else {
            return R.error(Code.SYS_ERR, "发票类型错误");
        }


        Map result=insertOrUpdate(params,id);
        if ((Boolean) result.get("result")) {
            return R.ok("信息操作成功");

        }else {
            return R.error(Code.SYS_ERR, result.get("Msg").toString());
        }

    }

    private Boolean updateReceiver(Map<String, String> params) {

        JsmsAgentInvoiceConfig model = new JsmsAgentInvoiceConfig();
        model.setToName(params.get("toName"));
        model.setToPhone(params.get("toPhone"));
        model.setStatus(InvoiceConfigStatus.正常.getValue());
        model.setToAddr(params.get("toAddr"));
        model.setToAddrDetail(params.get("toAddrDetail"));
        model.setToQq(params.get("toQq"));
        model.setId(Integer.valueOf(params.get("id")));

        jsmsAgentInvoiceConfigService.updateSelective(model);
        return true;

    }

    private Map insertOrUpdate(Map<String, String> params,Long operator) {

        Map<String,Object> result=new HashMap<>();
        result.put("result",true);
        JsmsAgentInvoiceConfig model = new JsmsAgentInvoiceConfig();
        model.setAgingId(Integer.valueOf(params.get("agingId")));
        model.setInvoiceType(Integer.valueOf(params.get("invoiceType")));
        model.setStatus(InvoiceConfigStatus.正常.getValue());

        model.setInvoiceBody(params.get("invoiceBody")!=null?Integer.valueOf(params.get("invoiceBody")):null);
        model.setInvoiceHead(params.get("invoiceHead"));
        model.setCreditCode(params.get("creditCode"));
        model.setEmail(params.get("email"));
        model.setCompanyRegAddr(params.get("companyRegAddr"));
        model.setBank(params.get("bank"));
        model.setBankAccount(params.get("bankAccount"));
        model.setTelphone(params.get("telphone"));
        model.setOperator(operator);
        model.setUpdateTime(new Date());



        model.setToName(params.get("toName"));
        model.setToPhone(params.get("toPhone"));
        model.setStatus(InvoiceConfigStatus.正常.getValue());
        model.setToAddr(params.get("toAddr"));
        model.setToAddrDetail(params.get("toAddrDetail"));
        model.setToQq(params.get("toQq"));
       // model.setId(Integer.valueOf(params.get("id")));

        JsmsAgentInvoiceConfig jsmsAgentInvoiceConfigs=new JsmsAgentInvoiceConfig();
        if(InvoiceTypeEnum.普通发票.getValue().equals(model.getInvoiceType())){
            jsmsAgentInvoiceConfigs = jsmsUserFinanceService.findListNomal(model.getAgingId());
        }else if (InvoiceTypeEnum.增值税专票.getValue().equals(model.getInvoiceType())){
            jsmsAgentInvoiceConfigs = jsmsUserFinanceService.findListAdd(model.getAgingId());
        }else {
            result.put("result",false);
            result.put("Msg","发票类型错误！");
            return result;
        }
        if (OptType.修改.getValue() == Integer.valueOf(params.get("optType"))) {
            if (jsmsAgentInvoiceConfigs == null) {
                result.put("result",false);
                result.put("Msg","客户不存在增票，修改失败！");
            } else {
                model.setId(Integer.valueOf(params.get("id")));
                jsmsAgentInvoiceConfigService.updateSelective(model);
                return result;
            }
        } else if (OptType.新增.getValue() == Integer.valueOf(params.get("optType"))) {
            if (jsmsAgentInvoiceConfigs == null) {
                model.setCreateTime(new Date());
                jsmsAgentInvoiceConfigService.insert(model);
                return result;
            } else {
                result.put("result",false);
                result.put("Msg","客户已经开过增票，新增失败");
            }
        }
        return result;

    }

    private void mapDeal(Map<String, String> map) {
    List<String > keys=new ArrayList<>();
    for (Map.Entry<String, String> entry : map.entrySet()) {
        if(StringUtils.isBlank(entry.getValue())){
            keys.add(entry.getKey());
        }else{
            entry.setValue(entry.getValue().trim());
        }
    }

     for (String key : keys) {
         map.remove(key);
        }

    }


    private Boolean deleteInvoice(Map<String, String> params,Long operator) {
        JsmsAgentInvoiceConfig model=new JsmsAgentInvoiceConfig();
        model.setId(Integer.valueOf(params.get("id")));
        model.setOperator(operator);
        model.setStatus(InvoiceConfigStatus.删除.getValue());
        if(jsmsAgentInvoiceConfigService.updateSelective(model)>0){
            return true;
        }
        return false;
    }
    @ApiOperation(value = "发票信息导出", notes = "发票信息导出", tags = "发票管理-发票信息", response = R.class)
    @ApiImplicitParams({
            @ApiImplicitParam(name = "fourItem", value = "客户ID/客户名称/手机号码/销售名字", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "belongSaleId", value = "所属销售", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "invoiceType", value = "发票类型(1,普通发票, 2,增值税专票）", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "updateTimeStart", value = "最后修改时间开始", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "updateTimeEnd", value = "最后修改时间结束", dataType = "String", paramType = "form"),

    })
    @RequestMapping("/invoice/export")
    public void invoiceExport(HttpSession session,@ApiParam(name = "params", hidden = true) @RequestParam Map<String,String> params, HttpServletRequest request,
                              HttpServletResponse response) {

        mapDeal(params);
        JsmsPage jpage = new JsmsPage();
        jpage.setParams(params);
        jpage.setOrderByClause("create_time desc");


        UserSession userSession = (UserSession) session.getAttribute(SessionEnum.SESSION_USER.toString());
        Long id = userSession.getId();
        DataAuthorityCondition dataAuthorityCondition = AgentUtils.getDataAuthorityCondition(Long.valueOf(id), true, false);
        List<Long> ids = dataAuthorityCondition.getIds();


        jpage.getParams().put("belongSaleIds",ids);
        jpage.setRows(Integer.MAX_VALUE);
        jpage.setMaxQueryLimit(Integer.MAX_VALUE);
        initinvoice4Export(jpage);


        String fileName = "发票信息";

        StringBuffer buffer = new StringBuffer();
        buffer.append("查询条件 -> ");


        String agentTypeName = "";
        String filePath = ConfigUtils.save_path + "/" + fileName + "_" + DateTime.now().toString("yyyyMMddHHmmss")
                + ".xls";
        Excel excel = new Excel();
        excel.setFilePath(filePath);
        excel.setTitle(fileName);
        excel.addRemark(buffer.toString());
        excel.addHeader(20, "客户ID", "agingId");
        excel.addHeader(30, "客户名称", "name");
        excel.addHeader(30, "归属销售", "belongSaleStr");
        excel.addHeader(30, "发票抬头", "invoiceHead");
        excel.addHeader(30, "资质类型", "invoiceType");
        excel.addHeader(30, "统一社会信用代码", "creditCode");
        excel.addHeader(30, "更新时间", "updateTime");
        excel.addHeader(30, "操作人", "operatorName");
        excel.setDataList(jpage.getData());

        if (ExcelUtils.exportExcel(excel)) {
            FileUtils.download( filePath,response);
            FileUtils.delete(filePath);
        } else {
            response.setContentType("text/plain;charset=UTF-8");
            try {
                response.getWriter().write("导出Excel文件失败，请联系管理员");
                response.getWriter().flush();
            } catch (IOException e) {
                logger.error("导出Excel文件失败", e);
            }
        }
    }
    private JsmsPage initinvoice4Export(JsmsPage page) {
        List belongSaleIds=(List)page.getParams().get("belongSaleIds");
        Map<String, String> params=new HashMap<>();
        if (!StringUtils.isBlank((String)page.getParams().get("fourItem"))) {
            params.put("fourItem",(String)page.getParams().get("fourItem"));
        }
        if (!StringUtils.isBlank((String)page.getParams().get("belongSaleId"))) {
            params.put("belongSaleId",(String)page.getParams().get("belongSaleId"));
        }
        if (!StringUtils.isBlank((String)page.getParams().get("invoiceType"))) {
            params.put("invoiceType",(String)page.getParams().get("invoiceType"));
        }
        if (!StringUtils.isBlank((String)page.getParams().get("updateTimeStart"))) {
            params.put("updateTimeStart",(String)page.getParams().get("updateTimeStart"));
        }
        if (!StringUtils.isBlank((String)page.getParams().get("updateTimeEnd"))) {
            params.put("updateTimeEnd",(String)page.getParams().get("updateTimeEnd"));
        }
        page.setParams(params);
        page.getParams().put("status",0);
        page.getParams().put("belongSaleIds",belongSaleIds);
        List<JsmsAgentInvoiceConfigDto> jsmsAgentInvoiceConfigs = jsmsAgentInvoiceConfigService.queryListForInvoice(page);

        List<Map> ls2=new ArrayList<>();
        if(jsmsAgentInvoiceConfigs!=null)
            for(JsmsAgentInvoiceConfigDto jsmsAgentInvoiceList:jsmsAgentInvoiceConfigs){
                ls2.add(getValue(jsmsAgentInvoiceList));
            }
        page.setData(ls2);
        return page;

    }
    private  Map getValue(Object obj)
    {
        Map<String, Object> params = new HashMap<String, Object>(0);
        try {
            PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
            PropertyDescriptor[] descriptors = propertyUtilsBean.getPropertyDescriptors(obj);
            for (int i = 0; i < descriptors.length; i++) {
                String name = descriptors[i].getName();
                if (!"class".equals(name)) {
                    params.put(name, propertyUtilsBean.getNestedProperty(obj, name));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(InvoiceTypeEnum.普通发票.getValue().equals(params.get("invoiceType"))){
            params.put("invoiceType","普通发票");
        }else if(InvoiceTypeEnum.增值税专票.getValue().equals(params.get("invoiceType"))){
            params.put("invoiceType","增值税专票");
        }

        Date date=(Date)params.get("updateTime");

        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String sDate=sdf.format(date);

        params.put("updateTime",sDate);
        return params;
    }

}
