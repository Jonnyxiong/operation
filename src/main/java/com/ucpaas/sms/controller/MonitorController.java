package com.ucpaas.sms.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSON;
import com.jsmsframework.monitor.pojo.JsmsAppServerInfo;
import com.jsmsframework.monitor.service.JsmsAppRunningSubject;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

@Api(value = "系统监控", description = "系统监控")
@RestController
@RequestMapping("/monitor")
public class MonitorController {
    private Logger logger = LoggerFactory.getLogger(MonitorController.class);

    @Autowired
    @Qualifier("jsmsAppRunningSubject")
    private JsmsAppRunningSubject jsmsAppRunningSubject;

    @ApiOperation(value = "获取应用运行信息及数据库连接信息", notes = "获取应用运行信息及数据库连接信息",tags = "系统监控")
    @ApiImplicitParam(name = "token", value = "令牌", required = true, dataType = "String")
    @PostMapping("/testrunning")
    @ResponseBody
    public JsmsAppServerInfo testRunning(String token) {
        logger.debug("token --> {}", token);
        JsmsAppServerInfo jsmsAppServerInfo = jsmsAppRunningSubject.isRunning(token);
        logger.debug("jsmsAppServerInfo --> {}", JSON.toJSONString(jsmsAppServerInfo));
        return jsmsAppServerInfo;
    }

}
