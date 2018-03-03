package com.ucpaas.sms.controller.statistics;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.enums.Code;
import com.jsmsframework.common.enums.OperatorType;
import com.jsmsframework.common.enums.SmsTypeEnum;
import com.jsmsframework.user.entity.JsmsMenu;
import com.jsmsframework.user.service.JsmsMenuService;
import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.common.util.file.ExcelUtils;
import com.ucpaas.sms.common.util.file.FileUtils;
import com.ucpaas.sms.entity.po.JsmsClientOperationStatisticsPo;
import com.ucpaas.sms.enums.ClientOperation;
import com.ucpaas.sms.model.Excel;
import com.ucpaas.sms.service.admin.AuthorityService;
import com.ucpaas.sms.service.statistic.ClientOperationStatisticsService;
import com.ucpaas.sms.util.ConfigUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * created by xiaoqingwen on 2018/1/11 10:09 客户运营分析统计
 */
@Controller
@RequestMapping("/operation/statistics/client")
public class ClientOperationStatisticsController {
	private static Logger logger = LoggerFactory.getLogger(ClientOperationStatisticsController.class);

	@Autowired
	private ClientOperationStatisticsService clientOperationStatisticsService;
    @Autowired
    private JsmsMenuService jsmsMenuService;

	@ApiOperation(value = "账户运营分析主页", notes = "账户运营分析主页", tags = "运营分析-账户营分析")
	@GetMapping("/list")
	public ModelAndView list(ModelAndView mv) {
        mv.addObject("max_export_num", ConfigUtils.max_export_excel_num);
		mv.setViewName("/statistic/clientOperation/list");
        //按钮权限控制
        JsmsMenu menu=new JsmsMenu();
        menu.setMenuName("账户运营分析");
        menu.setLevel(2);
        menu.setMenuType("1");
        menu.setWebId(3);
        menu.setStatus("1");
        List<JsmsMenu> list = jsmsMenuService.findList(menu);
        if(list!=null && list.size()>0){
            mv.addObject("jsmsMenu", list.get(0));
        }else{
            logger.debug("权限获取失败!");
            mv.addObject("jsmsMenu", null);
        }
		return mv;
	}

	@ApiOperation(value = "账户运营分析列表数据查询", notes = "账户运营分析列表数据查询", tags = "运营分析-账户运营分析")
	@PostMapping("/list")
	@ResponseBody
	public JsmsPage<JsmsClientOperationStatisticsPo> list(JsmsClientOperationStatisticsPo clientOperation) {
        R r = clientOperationStatisticsService.checkSearchCondition(clientOperation);
        if (r != null && r.getCode() == Code.SYS_ERR.getValue())
        {
          return new JsmsPage<JsmsClientOperationStatisticsPo>();
        }
        String beginTime = clientOperation.getBeginTime();
        String endTime = clientOperation.getEndTime();
        //传入的时间格式为2018-01
        if(org.apache.commons.lang3.StringUtils.isNotBlank(beginTime) && beginTime.length()==7){
            //处理时间
            clientOperation.setBeginTime(beginTime.substring(0,4)+beginTime.substring(5));
        }else{
            clientOperation.setBeginTime(null);
        }
        if(org.apache.commons.lang3.StringUtils.isNotBlank(endTime) && endTime.length()==7){
            clientOperation.setEndTime(endTime.substring(0,4)+endTime.substring(5));
        }else{
            clientOperation.setEndTime(null);
        }
		return clientOperationStatisticsService.queryPage(clientOperation);
	}

	@ApiOperation(value = "账户运营分析初始化数据", notes = "账户运营分析初始化数据", tags = "运营分析-账户运营分析")
	@PostMapping("data")
	@ResponseBody
	public R initData() {

	    // 查询寻所有客户，查询所有销售
		return null;
	}

	@ApiOperation(value = "账户运营分析导出", notes = "账户运营分析导出", tags = "运营分析-客户运营分析", produces = "application/octet-stream")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "smstype", value = "短信类型", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "operatorstype", value = "运营商类型", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "client.clientid", value = "客户Id", dataType = "String", paramType = "form"),
            @ApiImplicitParam(name = "beginTime", value = "起始时间", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "endTime", value = "截止时间", dataType = "int", paramType = "form"),
            @ApiImplicitParam(name = "orderBy", value = "排序", dataType = "String", paramType = "form") })
    @PostMapping(path = "/export")
    public void exportExcel(JsmsClientOperationStatisticsPo clientOperation, HttpServletResponse response) {
	    String orderBy = clientOperation.getOrderBy();
        R r = clientOperationStatisticsService.checkSearchCondition(clientOperation);
        if (r != null && r.getCode() == Code.SYS_ERR.getValue()) {
            String fullContentType = "text/plain;charset=UTF-8";
            response.setContentType(fullContentType);
            try {
                response.getWriter().write("导出Excel文件失败，"+r.getMsg());
                response.getWriter().flush();
            } catch (IOException e) {
                logger.error("导出Excel文件失败: {} {}", r.getMsg(), e);
            }
        }

        String beginTime = clientOperation.getBeginTime();
        String endTime = clientOperation.getEndTime();
        //传入的时间格式为2018-01
        if(org.apache.commons.lang3.StringUtils.isNotBlank(beginTime) && beginTime.length()==7){
            //处理时间
            clientOperation.setBeginTime(beginTime.substring(0,4)+beginTime.substring(5));
        }else{
            clientOperation.setBeginTime(null);
        }
        if(org.apache.commons.lang3.StringUtils.isNotBlank(endTime) && endTime.length()==7){
            clientOperation.setEndTime(endTime.substring(0,4)+endTime.substring(5));
        }else{
            clientOperation.setEndTime(null);
        }

        String filePath = ConfigUtils.save_path + "/账户运营分析列表.xls";

        Excel excel = new Excel();
        excel.setPageRowCount(2000); // 设置每页excel显示2000行
        excel.setFilePath(filePath);
        excel.setTitle("账户运营分析");

        StringBuffer buffer = new StringBuffer("查询条件：");
        int count = 0;

        JsmsClientOperationStatisticsPo clientOperationStatisticsPo = clientOperation;
        if (clientOperationStatisticsPo == null)
        {
            clientOperationStatisticsPo = (JsmsClientOperationStatisticsPo)r.getData();
        }

        if (clientOperationStatisticsPo.getSmstype() != null)
            buffer.append("（").append(++count).append("）" + "短信类型：").append(SmsTypeEnum.getDescByValue(clientOperationStatisticsPo.getSmstype()));

        if (clientOperationStatisticsPo.getOperatorstype() != null)
            buffer.append("（").append(++count).append("）" + "运营商：").append(OperatorType.getDescByValue(clientOperationStatisticsPo.getOperatorstype()));

        if (clientOperationStatisticsPo.getBeginTime() != null)
            buffer.append("（").append(++count).append("）" + "开始日期：").append(clientOperationStatisticsPo.getBeginTime());

        if (clientOperationStatisticsPo.getEndTime() != null)
            buffer.append("（").append(++count).append("）" + "结束日期：").append(clientOperationStatisticsPo.getEndTime());

        if (clientOperationStatisticsPo.getClient() != null && StringUtils.isNotBlank(clientOperationStatisticsPo.getClient().getName()))
        {
            buffer.append("（").append(++count).append("）" + "客户：").append(clientOperationStatisticsPo.getClient().getName());
        }

        if (clientOperationStatisticsPo.getUser() != null && StringUtils.isNotBlank(clientOperationStatisticsPo.getUser().getRealname()))
        {
            buffer.append("（").append(++count).append("）" + "归属销售：").append(clientOperationStatisticsPo.getUser().getRealname());
        }

        if (clientOperationStatisticsPo.getOrderBy() != null)
        {
            buffer.append("（").append(++count).append("）" + "排序：").append(ClientOperation.OrderBy.getEnumByValue(Integer.parseInt(clientOperationStatisticsPo.getOrderByValue())));
        }

        // 在表格对象中添加查询条件,无查询条件则不添加
        if (count > 0)
            excel.addRemark(buffer.toString());

        excel.addHeader(20, "客户ID", "clientId");
        excel.addHeader(20, "客户名称", "clientName");
        excel.addHeader(20, "运营商", "operatorstype");
        excel.addHeader(20, "短信类型", "smstype");
        excel.addHeader(20, "提交条数", "submitTotal");
        excel.addHeader(20, "明确成功提交条数", "reportsuccess");
        excel.addHeader(20, "发送成功率", "sendSuccessRatio");
        excel.addHeader(20, "投诉个数", "complaintNumber");
        excel.addHeader(20, "投诉率", "complaintRatio");
        excel.addHeader(20, "投诉系数", "complaintCoefficient");
        excel.addHeader(20, "投诉差异值", "complaintDifference");
        excel.addHeader(20, "销售单价", "salefee");
        excel.addHeader(20, "所属销售", "belongSale");
        excel.addHeader(20, "日期", "dateStr");

        List<JsmsClientOperationStatisticsPo> list = clientOperationStatisticsService.findList(clientOperationStatisticsPo);
        List<Map<String, Object>> mapList = clientOperationStatisticsService.ListToMap(list);
        excel.setDataList(mapList);

        if (ExcelUtils.exportExcel(excel)) {
            FileUtils.download(filePath, response);
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

}