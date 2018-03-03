package com.ucpaas.sms.controller.finance;

import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.product.service.JsmsProductInfoService;
import com.jsmsframework.user.service.JsmsAccountService;
import com.ucpaas.sms.entity.message.ClientBalanceAlarm;
import com.ucpaas.sms.service.account.AlarmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

/**
 * Created by Dylan.
 * 告警
 */
@Controller
@RequestMapping("/alarm")
public class AlarmController {

    private static Logger logger = LoggerFactory.getLogger(AlarmController.class);

    @Autowired
    private JsmsProductInfoService jsmsProductInfoService;
    @Autowired
    private JsmsAccountService jsmsAccountService;
    @Autowired
    private AlarmService alarmService;

    /*@ApiOperation(value = "查询品牌订单列表数据", notes = "查询品牌订单列表数据", tags = "品牌订单信息", response = PageContainer.class)
    @ApiImplicitParams({ @ApiImplicitParam(name = "page", value = "页码", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "rows", value = "行数", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "operatorCode", value = "运营商类型", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "condition", value = "订单号/产品代码/客户ID/代理商ID", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "status", value = "订单状态", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "startDateTime", value = "开始时间", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "endDateTime", value = "结束时间", dataType = "String", paramType = "query") })
    @RequestMapping("/order/list")
    @ResponseBody
    public PageContainer list(String rows, String page, @RequestParam Map<String,String> params) {
        params.put("pageRowCount", rows);
        params.put("currentPage", page);
        params.put("orderByClause", "create_time DESC");

        if (StringUtils.isNotBlank(params.get("endDateTime"))) {
            params.put("endDateTime", new StringBuilder(params.get("endDateTime")).append(" 23:59:59").toString());
        }
        return container;
    }*/



    @GetMapping("/client/balanceAlarm")
    public ModelAndView balanceAlarm(ModelAndView mv,String clientId){
        ClientBalanceAlarm balanceAlarm = alarmService.getByClientId(clientId);
        mv.setViewName("accountInfo/client/balanceAlarm");
        mv.addObject("clientId",clientId);
        mv.addObject("model",balanceAlarm);
        return mv;
    }

    @PostMapping("/client/balanceAlarm/update")
    @ResponseBody
    public com.jsmsframework.common.dto.ResultVO pageBalanceAlarm(@RequestBody ClientBalanceAlarm[] clientBalanceAlarms){
        if (clientBalanceAlarms.length != 2)
            return ResultVO.failure("参数错误");

        com.jsmsframework.common.dto.ResultVO resultVO = alarmService.updateIdempotent(clientBalanceAlarms[0],clientBalanceAlarms[1]);
        return resultVO;
    }


}
