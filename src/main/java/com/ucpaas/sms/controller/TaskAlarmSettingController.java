package com.ucpaas.sms.controller;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.common.entity.JsmsEmailAlarmSetting;
import com.jsmsframework.common.entity.JsmsSendEmailList;
import com.jsmsframework.finance.entity.JsmsTaskAlarmSetting;
import com.jsmsframework.finance.service.JsmsTaskAlarmSettingService;
import com.ucpaas.sms.common.util.CacheUtils;
import com.ucpaas.sms.constant.CacheConstant;
import com.ucpaas.sms.annotation.JSON;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.service.TaskAlarmSettingService;
import com.ucpaas.sms.util.AgentUtils;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @description
 * @author
 * @date 2017-08-09
 */
@Controller
@RequestMapping("/managerCenter/taskAlarmSetting")
public class TaskAlarmSettingController extends BaseController {

	private static Logger logger = LoggerFactory.getLogger(TaskAlarmSettingController.class);

	@Autowired
	private JsmsTaskAlarmSettingService jsmsTaskAlarmSettingService;

	@Autowired
	private TaskAlarmSettingService taskAlarmSettingService;

	/**
	 * 列表 --> route
	 * 
	 * @param mv
	 * @return
	 */
	@ApiOperation(value = "提醒配置-默认页面", notes = "提醒配置-默认页面，默认跳转短信提醒", tags = "账户信息管理-提醒配置")
	@GetMapping(path = { "/list", "/sms" })
	public ModelAndView sms(ModelAndView mv, HttpSession session) {
		UserSession user = getUserFromSession(session);

		mv.addObject("menus", AgentUtils.hasMenuRight(user, "短信提醒配置-dxtxpz", "邮件提醒配置-yjtxpz"));
		mv.setViewName("managerCenter/sms/list");
		return mv;
	}

	/**
	 * 列表 --> action
	 * 
	 * @param page
	 * @param params
	 * @return
	 */
	@ApiOperation(value = "查询提醒配置列表数据", notes = "查询提醒配置列表数据", tags = "账户信息管理-提醒配置", response = JsmsPage.class)
	@PostMapping("/sms/list")
//	@ResponseBody
	@JSON(type = JsmsTaskAlarmSetting.class, filter="saleAlarmContent,taskAlarmContent,userAlarmContent")
	public JsmsPage smsList(JsmsPage<JsmsTaskAlarmSetting> page, @RequestParam Map params) {
		page.setParams(params);

		JsmsPage result = (JsmsPage) CacheUtils.get(CacheConstant.TaskSettingPage);
		if (result == null)
		{
			result = jsmsTaskAlarmSettingService.queryList(page);
			CacheUtils.put(CacheConstant.TaskSettingPage, result);
		}

		return result;
	}

	/**
	 * 编辑 --> route
	 * 
	 * @param mv
	 * @param id
	 * @return
	 */
	@ApiOperation(value = "编辑路由", notes = "编辑提醒配置时如果使用页面，可以使用此路由进入编辑页面", tags = "账户信息管理-提醒配置", response = ResultVO.class)
	@GetMapping("/sms/edit/{id}")
	@ResponseBody
	public ResultVO smsEdit(ModelAndView mv, @PathVariable("id") Integer id) {
		ResultVO resultVO = ResultVO.failure();
		try {
			JsmsPage result = (JsmsPage) CacheUtils.get(CacheConstant.TaskSettingPage);
			if (result == null)
			{
				JsmsTaskAlarmSetting taskAlarmSetting = jsmsTaskAlarmSettingService.getById(id);
				resultVO.setData(taskAlarmSetting);
			} else {
				List<JsmsTaskAlarmSetting> taskAlarmSettings =  result.getData();
				for (JsmsTaskAlarmSetting taskAlarmSetting : taskAlarmSettings) {
					if (taskAlarmSetting.getId().equals(id))
					{
						resultVO.setData(taskAlarmSetting);
						break;
					}
				}

				if (null == resultVO.getData())
				{
					JsmsTaskAlarmSetting taskAlarmSetting = jsmsTaskAlarmSettingService.getById(id);
					resultVO.setData(taskAlarmSetting);
				}
			}

		} catch (Exception e) {
			logger.error("查询失败", e);
			resultVO.setMsg("查询提醒配置失败");
		}

		resultVO.setSuccess(true);
		resultVO.setMsg("查询提醒配置成功！");
		return resultVO;
	}

	/**
	 * 编辑 --> action
	 * 
	 * @param taskAlarmSetting
	 * @param errorResult
	 * @return
	 */
	@ApiOperation(value = "更新指定提醒配置信息", notes = "更新指定提醒配置信息", tags = "账户信息管理-提醒配置", response = ResultVO.class)
	@PostMapping("/sms/edit")
	@ResponseBody
	public ResultVO smsEditSave(@Valid JsmsTaskAlarmSetting taskAlarmSetting, BindingResult errorResult) {
		ResultVO resultVO = ResultVO.failure();
		if (errorResult.hasErrors()) {
			for (ObjectError error : errorResult.getAllErrors()) {
				resultVO.setFail(true);
				resultVO.setMsg(error.getDefaultMessage());
				return resultVO;
			}
		}

		// 最小为30分钟的频率
		if (taskAlarmSetting.getScanFrequecy().intValue() < 30) {
			resultVO.setFail(true);
			resultVO.setMsg("检查频率最小为30");
			return resultVO;
		}

		// 提醒手机号最长为1000
		if (StringUtils.isNotBlank(taskAlarmSetting.getTaskAlarmPhone())
				&& taskAlarmSetting.getTaskAlarmPhone().length() > 1000) {
			resultVO.setFail(true);
			resultVO.setMsg("提醒手机号最长为1000");
			return resultVO;
		}

		int row = 0;
		try {
			taskAlarmSetting.setUpdateTime(new Date());
			taskAlarmSetting.setCreateTime(null);
			row = jsmsTaskAlarmSettingService.updateSelective(taskAlarmSetting);
		} catch (Exception e) {
			logger.error("编辑失败", e);
		}
		if (row > 0) {
			resultVO.setSuccess(true);
			resultVO.setMsg("操作成功！");

			CacheUtils.remove(CacheConstant.TaskSettingPage);
			return resultVO;
		}
		return resultVO;
	}

	@ApiOperation(value = "更新提醒配置状态", notes = "更新", tags = "账户信息管理-提醒配置", response = ResultVO.class)
	@PostMapping("/sms/updateStatus")
	@ResponseBody
	public ResultVO smsUpdateStatus(Integer id, Integer status) {
		ResultVO resultVO = ResultVO.failure();
		int row = 0;
		try {
			if (id == null || !(status.equals(0) || status.equals(1))) {
				logger.debug("更新提醒状态请求参数不合法， id = {}，status = {}", id, status);
				resultVO.setMsg("更新提醒状态请求参数不合法");
				resultVO.setSuccess(false);
				return resultVO;
			}

			JsmsTaskAlarmSetting jsmsTaskAlarmSetting = new JsmsTaskAlarmSetting();
			jsmsTaskAlarmSetting.setId(id);
			jsmsTaskAlarmSetting.setStatus(status);
			jsmsTaskAlarmSetting.setUpdateTime(new Date());
			row = jsmsTaskAlarmSettingService.updateSelective(jsmsTaskAlarmSetting);

		} catch (Exception e) {
			logger.error("更新状态失败", e);
		}

		if (row > 0) {
			resultVO.setSuccess(true);
			resultVO.setMsg("操作成功！");
			CacheUtils.remove(CacheConstant.TaskSettingPage);
			return resultVO;
		}
		return resultVO;
	}

	@ApiOperation(value = "邮件提醒配置-页面", notes = "邮件提醒配置", tags = "账户信息管理-提醒配置")
	@GetMapping("/email")
	public ModelAndView email(ModelAndView mv, HttpSession session) {
		UserSession user = getUserFromSession(session);

		mv.addObject("menus", AgentUtils.hasMenuRight(user, "短信提醒配置-dxtxpz", "邮件提醒配置-yjtxpz"));
		mv.setViewName("managerCenter/email/list");
		return mv;
	}

	@ApiOperation(value = "发送邮箱服务器列表数据", notes = "邮件提醒配置", tags = "账户信息管理-提醒配置")
	@PostMapping("/email/server/list")
	@ResponseBody
	public R emailServerList() {
		R r;
		try {
			r = taskAlarmSettingService.findSendEmailList();
		} catch (Exception e) {
			logger.error("发送邮箱服务器获取列表数据失败 {}", e);
			r = R.error("发送邮箱服务器获取列表数据失败");
		}
		return r;
	}

	@ApiOperation(value = "发送邮箱服务器添加-页面", notes = "邮件提醒配置", tags = "账户信息管理-提醒配置")
	@GetMapping("/email/server/add")
	public ModelAndView emailServerAddView(ModelAndView mv) {
		mv.setViewName("managerCenter/email/server/add");
		return mv;
	}

	@ApiOperation(value = "发送邮箱服务器添加-保存", notes = "邮件提醒配置", tags = "账户信息管理-提醒配置")
	@PostMapping("/email/server/add")
	@ResponseBody
	public R emailServerAddSave(@RequestBody JsmsSendEmailList sendEmailList, ModelAndView mv) {
		R r;
		try {
			r = taskAlarmSettingService.addSendEmailList(sendEmailList);
		} catch (Exception e) {
			logger.error("发送邮箱服务器添加失败 {}", e);
			r = R.error("发送邮箱服务器添加失败");
		}
		return r;
	}

	@ApiOperation(value = "发送邮箱服务器编辑-页面", notes = "邮件提醒配置", tags = "账户信息管理-提醒配置")
	@GetMapping("/email/server/edit")
	public ModelAndView emailServerEditView(ModelAndView mv) {
		mv.setViewName("managerCenter/email/server/edit");
		return mv;
	}

	@ApiOperation(value = "发送邮箱服务器编辑-获取数据", notes = "邮件提醒配置", tags = "账户信息管理-提醒配置")
	@GetMapping("/email/server/edit/{id}")
	@ResponseBody
	public R emailAlarmServerEditData(@PathVariable("id") Long id, ModelAndView mv, HttpSession session) {
		R r;
		try {
			r = taskAlarmSettingService.sendEmailListInfo(id);
		} catch (Exception e) {
			logger.error("获取发送邮箱服务器详情失败 {}", e);
			r = R.error("获取发送邮箱服务器详情失败");
		}
		return r;
	}

	@ApiOperation(value = "发送邮箱服务器编辑-保存", notes = "邮件提醒配置", tags = "账户信息管理-提醒配置")
	@PostMapping("/email/server/edit")
	@ResponseBody
	public R emailAlarmServerEditSave(@RequestBody JsmsSendEmailList sendEmailList) {
		R r;
		try {
			r = taskAlarmSettingService.editSendEmailList(sendEmailList);
		} catch (Exception e) {
			logger.error("发送邮箱服务器编辑失败 {}", e);
			r = R.error("发送邮箱服务器编辑失败");
		}
		return r;
	}

	@ApiOperation(value = "发送邮箱服务器-禁用", notes = "邮件提醒配置", tags = "账户信息管理-提醒配置")
	@PostMapping("/email/server/disable/{id}")
	@ResponseBody
	public R emailAlarmServerDisable(@PathVariable("id") Long id) {
		R r;
		try {
			r = taskAlarmSettingService.disableSendEmailList(id);
		} catch (Exception e) {
			logger.error("发送邮箱服务器禁用失败 {}", e);
			r = R.error("发送邮箱服务器禁用失败");
		}
		return r;
	}

	@ApiOperation(value = "发送邮箱服务器-启用", notes = "邮件提醒配置", tags = "账户信息管理-提醒配置")
	@PostMapping("/email/server/enable/{id}")
	@ResponseBody
	public R emailAlarmServerEnable(@PathVariable("id") Long id) {
		R r;
		try {
			r = taskAlarmSettingService.enableSendEmailList(id);
		} catch (Exception e) {
			logger.error("发送邮箱服务器启用失败 {}", e);
			r = R.error("发送邮箱服务器启用失败");
		}
		return r;
	}

	@ApiOperation(value = "发送邮箱服务器编辑-测试", notes = "邮件提醒配置", tags = "账户信息管理-提醒配置")
	@PostMapping("/email/server/test")
	@ResponseBody
	public R emailAlarmServerTest(@RequestBody JsmsSendEmailList sendEmailList) {
		R r;
		try {
			r = taskAlarmSettingService.testSendEmailList(sendEmailList);
		} catch (Exception e) {
			logger.error("发送邮箱服务器测试失败 {}", e);
			r = R.error("发送邮箱服务器测试失败");
		}
		return r;
	}

	@ApiOperation(value = "邮件提醒列表数据", notes = "邮件提醒配置", tags = "账户信息管理-提醒配置")
	@PostMapping("/email/alarm/list")
	@ResponseBody
	public R emailAlarmList() {
		R r;
		try {
			r = taskAlarmSettingService.findEmailAlarmSettingList();
		} catch (Exception e) {
			logger.error("获取邮件提醒列表数据失败 {}", e);
			r = R.error("获取邮件提醒列表数据失败");
		}
		return r;
	}

	@ApiOperation(value = "邮件提醒添加-页面", notes = "邮件提醒配置", tags = "账户信息管理-提醒配置")
	@GetMapping("/email/alarm/add")
	public ModelAndView emailAlarmAddView(ModelAndView mv) {
		mv.setViewName("managerCenter/email/alarm/add");
		return mv;
	}

	@ApiOperation(value = "邮件提醒添加-保存", notes = "邮件提醒配置", tags = "账户信息管理-提醒配置")
	@PostMapping("/email/alarm/add")
	@ResponseBody
	public R emailAlarmAddSave(@RequestBody JsmsEmailAlarmSetting emailAlarmSetting) {
		R r;
		try {
			r = taskAlarmSettingService.addEmailAlarmSetting(emailAlarmSetting);
		} catch (Exception e) {
			logger.error("邮件提醒配置添加失败 {}", e);
			r = R.error("邮件提醒配置添加失败");
		}
		return r;
	}

	@ApiOperation(value = "邮件提醒编辑-页面", notes = "邮件提醒配置", tags = "账户信息管理-提醒配置")
	@GetMapping("/email/alarm/edit")
	public ModelAndView emailAlarmEditView(ModelAndView mv) {
		mv.setViewName("managerCenter/email/alarm/edit");
		return mv;
	}

	@ApiOperation(value = "邮件提醒编辑-获取数据", notes = "邮件提醒配置", tags = "账户信息管理-提醒配置")
	@GetMapping("/email/alarm/edit/{id}")
	@ResponseBody
	public R emailAlarmEditData(@PathVariable("id") Long id, ModelAndView mv, HttpSession session) {
		R r;
		try {
			r = taskAlarmSettingService.emailAlarmSettingInfo(id);
		} catch (Exception e) {
			logger.error("获取邮件提醒配置详情失败 {}", e);
			r = R.error("获取邮件提醒配置详情失败");
		}
		return r;
	}

	@ApiOperation(value = "邮件提醒编辑-保存", notes = "邮件提醒配置", tags = "账户信息管理-提醒配置")
	@PostMapping("/email/alarm/edit")
	@ResponseBody
	public R emailAlarmEditSave(@RequestBody JsmsEmailAlarmSetting emailAlarmSetting) {
		R r;
		try {
			r = taskAlarmSettingService.editEmailAlarmSetting(emailAlarmSetting);
		} catch (Exception e) {
			logger.error("邮件提醒配置编辑失败 {}", e);
			r = R.error("邮件提醒配置编辑失败");
		}
		return r;
	}

	@ApiOperation(value = "邮件提醒配置-禁用", notes = "邮件提醒配置", tags = "账户信息管理-提醒配置")
	@PostMapping("/email/alarm/disable/{id}")
	@ResponseBody
	public R emailAlarmDisable(@PathVariable("id") Long id) {
		R r;
		try {
			r = taskAlarmSettingService.disableEmailAlarmSetting(id);
		} catch (Exception e) {
			logger.error("邮件提醒配置禁用失败 {}", e);
			r = R.error("邮件提醒配置禁用失败");
		}
		return r;
	}

	@ApiOperation(value = "邮件提醒配置-启用", notes = "邮件提醒配置", tags = "账户信息管理-提醒配置")
	@PostMapping("/email/alarm/enable/{id}")
	@ResponseBody
	public R emailAlarmEnable(@PathVariable("id") Long id) {
		R r;
		try {
			r = taskAlarmSettingService.enableEmailAlarmSetting(id);
		} catch (Exception e) {
			logger.error("邮件提醒配置启用失败 {}", e);
			r = R.error("邮件提醒配置启用失败");
		}
		return r;
	}
}
