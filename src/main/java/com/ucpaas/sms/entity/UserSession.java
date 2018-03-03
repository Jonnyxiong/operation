package com.ucpaas.sms.entity;

import com.ucpaas.sms.entity.message.Role;

import java.util.List;

public class UserSession {
	// t_sms_user主键
	private Long id;
	// 主账号sid
	private String sid;
	// 用户状态：0: 禁用 1:正常
	private String status;
	// 真实姓名
	private String realname;
	// 角色ID
	private Integer roleId;
	// 角色名
	private String roleName;
	// 角色状态：1、正常 0、禁用
	private String roleStatus;
	// web应用ID：1->短信调度系统 2->代理商平台 3->运营平台 4->OEM代理商平台 5->SMSP-WEB
	// 6->SMSP-WEB-OEM
	private Integer webId;

	private Integer departmentId;

	private String departmentName;

	private Integer dataAuthority;

	private String dataAuthorityDesc;

	private List<Role> roles;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getSid() {
		return sid;
	}

	public void setSid(String sid) {
		this.sid = sid;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public Integer getRoleId() {
		return roleId;
	}

	public void setRoleId(Integer roleId) {
		this.roleId = roleId;
	}

	public String getRoleName() {
		return roleName;
	}

	public void setRoleName(String roleName) {
		this.roleName = roleName;
	}

	public String getRoleStatus() {
		return roleStatus;
	}

	public void setRoleStatus(String roleStatus) {
		this.roleStatus = roleStatus;
	}

	public Integer getWebId() {
		return webId;
	}

	public void setWebId(Integer webId) {
		this.webId = webId;
	}

	public Integer getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Integer departmentId) {
		this.departmentId = departmentId;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}

	public Integer getDataAuthority() {
		return dataAuthority;
	}

	public void setDataAuthority(Integer dataAuthority) {
		this.dataAuthority = dataAuthority;
	}

	public String getDataAuthorityDesc() {
		return dataAuthorityDesc;
	}

	public void setDataAuthorityDesc(String dataAuthorityDesc) {
		this.dataAuthorityDesc = dataAuthorityDesc;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}
}
