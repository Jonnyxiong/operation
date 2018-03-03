package com.ucpaas.sms.controller;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.ucpaas.sms.common.entity.R;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.entity.message.Department;
import com.ucpaas.sms.entity.message.Role;
import com.ucpaas.sms.entity.message.User;
import com.ucpaas.sms.entity.po.DeptTree;
import com.ucpaas.sms.entity.po.UserPo;
import com.ucpaas.sms.exception.OperationException;
import com.ucpaas.sms.service.admin.OfficeService;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.json.MappingJackson2JsonView;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * 组织架构
 */
@Controller
@RequestMapping("/office")
public class OfficeController extends BaseController {

	private static Logger logger = LoggerFactory.getLogger(OfficeController.class);

	private final ObjectMapper customMapper = new ObjectMapper();
	private final MappingJackson2JsonView view = new MappingJackson2JsonView();

	@Autowired
	private OfficeService officeService;

	@ApiOperation(value = "主页", notes = "组织架构主页，返回路径为page/office/tree", tags = "管理中心-组织架构")
	@GetMapping("/query")
	public String tree(Model model) {
		return "office/tree";
	}

	/**
	 * 组织架构 - 树
	 */
	@ApiOperation(value = "树", notes = "树", tags = "管理中心-组织架构", response = R.class)
	@PostMapping("/tree")
	@ResponseBody
	public R tree(HttpSession session) {
		UserSession userSession = getUserFromSession(session);
		DeptTree deptTree = officeService.getTree(userSession.getId());
		return R.ok("获取树成功", deptTree);
	}

	/**
	 * 组织架构 - 添加部门保存
	 */
	@ApiOperation(value = "添加部门保存", notes = "添加部门保存", tags = "管理中心-组织架构", response = R.class)
	@PostMapping("/dept/add")
	@ResponseBody
	public R addDept(@RequestBody Department department, HttpSession session) {
		R r;

		try {
			// 验证对象的安全性
			r = officeService.checkDept(department, true);
			if (r == null) {
				UserSession userSession = getUserFromSession(session);
				department.setCreateId(userSession.getId());
				r = officeService.addDept(department);
			}
		} catch (OperationException e) {
			logger.error("添加部门失败 消息{}", e);
			r = R.error(e.getMessage());
		} catch (Exception e) {
			logger.error("添加部门失败 消息{}", e);
			r = R.error("添加部门失败");
		}
		return r;
	}

	@JsonIgnoreProperties({ "welcomePage", "createDate", "updateDate", "status", "webId", "webIdName", "menu" })
	private static class RoleFilter extends Role {
	}

	@JsonIgnoreProperties({ "sid", "username", "password", "userType", "createDate", "updateDate", "loginTimes",
			"webId", "webIdName", "roleId", "roleName" })
	private static class UserFilter extends UserPo {
	}

	/**
	 * 组织架构 - 获取部门数据
	 */
	@ApiOperation(value = "获取部门数据", notes = "部门基本信息(department)、部门可设置的所有角色(roles)、下级部门信息(lowerDepartments)、部门成员(users)", tags = "管理中心-组织架构", response = R.class)
	@GetMapping("/dept/{id}")
	public View eptData(@ApiParam(value = "部门Id", required = true) @PathVariable("id") Integer id, Model model) {
		customMapper.addMixIn(Role.class, RoleFilter.class);
		customMapper.addMixIn(UserPo.class, UserFilter.class);
		customMapper.setTimeZone(TimeZone.getDefault());
		int roleFlag =0;
		view.setObjectMapper(customMapper);
		if (id == null) {
			model.addAllAttributes(R.error("部门ID不能为空").asMap());
			return view;
		}

		Map<String, Object> result = Maps.newHashMap();

		// 放入部门基本信息
		Department department = officeService.getDepartment(id);
		result.put("department", department);

		// 放入角色信息
		List<Role> roles = officeService.findCanSetRoleListByDepartmentId(id);
		result.put("roles", roles);
		// 下级部门信息
		List<Department> lowerDepartments = officeService.findLowerDepartmentList(department.getDepartmentId());
		result.put("lowerDepartments", lowerDepartments);

		// 部门成员信息
		List<UserPo> users = officeService.findUserListByDepartmentId(department.getDepartmentId());
		result.put("users", users);

		model.addAllAttributes(R.ok("获取数据成功", result).asMap());
		return view;
	}

	/**
	 * 组织架构 - 获取部门角色
	 */
	@ApiOperation(value = "获取部门可以设置的所有角色", notes = "获取部门可以设置的所有角色", tags = "管理中心-组织架构", response = R.class)
	@ApiImplicitParam(name = "id", value = "部门Id", paramType = "query", required = true, dataType = "int")
	@GetMapping("/dept/roles")
	public View deptRoles(Integer id, Model model) {
		customMapper.addMixIn(Role.class, RoleFilter.class);
		customMapper.setTimeZone(TimeZone.getDefault());
		view.setObjectMapper(customMapper);
		if (id == null) {
			model.addAllAttributes(R.error("部门ID不能为空").asMap());
			return view;
		}

		// 放入角色信息
		List<Role> roles = officeService.findCanSetRoleListByDepartmentId(id);
		model.addAllAttributes(R.ok("获取数据成功", roles).asMap());
		return view;
	}

	/**
	 * 组织架构 - 编辑部门数据
	 */
	@ApiOperation(value = "编辑部门时获取数据", notes = "编辑部门时获取的数据", tags = "管理中心-组织架构", response = R.class)
	@ApiImplicitParam(name = "id", value = "部门Id", paramType = "query", required = true, dataType = "int")
	@GetMapping("/dept/edit/data")
	public View modDeptData(Integer id, Model model) {
		customMapper.addMixIn(Role.class, RoleFilter.class);
		customMapper.setTimeZone(TimeZone.getDefault());
		view.setObjectMapper(customMapper);

		if (id == null) {
			model.addAllAttributes(R.error("部门ID不能为空").asMap());
			return view;
		}

		model.addAllAttributes(R.ok("获取数据成功", officeService.getDepartment(id)).asMap());
		return view;
	}

	/**
	 * 组织架构 - 编辑保存
	 */
	@ApiOperation(value = "编辑部门保存", notes = "组织架构编辑后进行保存", tags = "管理中心-组织架构", response = R.class)
	@PostMapping("/dept/edit")
	@ResponseBody
	public R modDept(@RequestBody Department department) {
		R r;
		try {
			// 验证对象的安全性
			r = officeService.checkDept(department, false);
			if (r == null) {
				r = officeService.modDept(department);
			}
		} catch (OperationException e) {
			logger.error("修改部门失败  消息{}", e);
			r = R.error(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("修改部门失败 消息{}", e);
			r = R.error("修改部门失败");
		}
		return r;
	}

	/**
	 * 组织架构 - 删除部门
	 */
	@ApiOperation(value = "删除部门", notes = "删除部门", tags = "管理中心-组织架构", response = R.class)
	@PostMapping("/dept/delete/{id}")
	@ResponseBody
	public R delDept(@ApiParam(value = "部门Id", required = true) @PathVariable("id") Integer id) {
		R r;
		try {
			if (id == null) {
				return R.error("部门Id不能为空");
			}

			r = officeService.delDept(id);
		} catch (OperationException e) {
			logger.error("删除部门失败  消息{}", e);
			r = R.error(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("删除部门失败 消息{}", e);
			r = R.error("删除部门失败");
		}
		return r;
	}

	/**
	 * 组织架构 - 添加成员保存
	 */
	@ApiOperation(value = "添加成员保存", notes = "添加成员保存", tags = "管理中心-组织架构", response = R.class)
	@PostMapping("/user/add")
	@ResponseBody
	public R addUser(@RequestBody User user) {
		R r;
		try {
			r = officeService.addUser(user);
		} catch (OperationException e) {
			logger.error("添加成员失败 消息{}", e);
			r = R.error(e.getMessage());
		} catch (Exception e) {
			logger.error("添加成员失败 消息{}", e);
			r = R.error("添加成员失败");
		}
		return r;
	}

	/**
	 * 组织架构 - 编辑成员保存
	 */
	@ApiOperation(value = "编辑成员保存", notes = "编辑成员保存", tags = "管理中心-组织架构", response = R.class)
	@PostMapping("/user/edit")
	@ResponseBody
	public R modUser(@RequestBody User user) {
		R r;
		try {
			r = officeService.modUser(user);
		} catch (OperationException e) {
			logger.error("修改成员失败  消息{}", e);
			r = R.error(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("修改成员失败 消息{}", e);
			r = R.error("修改成员失败");
		}
		return r;
	}

	/**
	 * 组织架构 - 禁用成员
	 */
	@ApiOperation(value = "禁用成员", notes = "禁用成员", tags = "管理中心-组织架构", response = R.class)
	@PostMapping("/user/disabled/{id}")
	@ResponseBody
	public R disabledUser(@ApiParam(value = "成员Id", required = true) @PathVariable("id") Long id) {
		R r;
		try {
			if (id == null) {
				return R.error("成员Id不能为空");
			}

			r = officeService.disabledUser(id);
		} catch (OperationException e) {
			logger.error("禁用成员失败  消息{}", e);
			r = R.error(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("禁用成员失败 消息{}", e);
			r = R.error("禁用成员失败");
		}
		return r;
	}

	/**
	 * 组织架构 - 启用成员
	 */
	@ApiOperation(value = "启用成员", notes = "启用成员", tags = "管理中心-组织架构", response = R.class)
	@PostMapping("/user/enabled/{id}")
	@ResponseBody
	public R enabledUser(@ApiParam(value = "成员Id", required = true) @PathVariable("id") Long id) {
		R r;
		try {
			if (id == null) {
				return R.error("成员Id不能为空");
			}

			r = officeService.enabledUser(id);
		} catch (OperationException e) {
			logger.error("启用成员失败  消息{}", e);
			r = R.error(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("启用成员失败 消息{}", e);
			r = R.error("启用成员失败");
		}
		return r;
	}

	/**
	 * 组织架构 - 成员调整部门
	 */
	@ApiOperation(value = "成员调整部门", notes = "成员调整部门", tags = "管理中心-组织架构", response = R.class)
	@PostMapping("/user/adjustdept")
	@ResponseBody
	public R adjustUserDept(@RequestBody User user) {
		R r;
		try {
			r = officeService.adjustUserDept(user);
		} catch (OperationException e) {
			logger.error("成员调整部门失败  消息{}", e);
			r = R.error(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("成员调整部门失败 消息{}", e);
			r = R.error("成员调整部门失败");
		}
		return r;
	}


	@ApiOperation(value = "检查销售成员是否含有客户", notes = "检查销售成员是否含有客户", tags = "管理中心-组织架构", response = R.class)
	@PostMapping("/user/hasClient")
	@ResponseBody
	public R hasClient(@RequestParam Long userId) {
		try {
			if (null == userId){
				logger.error("userId为空---{}", userId);
				return R.error("userId为空");
			}
			boolean data = officeService.isClient(userId);
			return R.ok("获取信息成功",data);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("检查销售成员是否含有客户错误---{}", e);
			return R.error("服务器正在检修...");
		}
	}

	@ApiOperation(value = "检查商务人员是否含有通道", notes = "检查商务人员是否含有通道", tags = "管理中心-组织架构", response = R.class)
	@PostMapping("/user/hasChannel")
	@ResponseBody
	public R hasChannel(@RequestParam Long userId) {
		try {
			if (null == userId){
				logger.error("userId为空---{}", userId);
				return R.error("userId为空");
			}
			boolean data = officeService.isChannel(userId);
			return R.ok("获取信息成功",data);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("检查商务人员是否含有通道错误---{}", e);
			return R.error("服务器正在检修...");
		}
	}
}
