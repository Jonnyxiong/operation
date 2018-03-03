package com.ucpaas.sms.entity.message;

import java.util.Date;
import java.util.List;

import org.apache.ibatis.type.Alias;

@Alias("User")
public class User {

	//
	private Long id;
	// 主账号sid
	private String sid;
	// 昵称(保留)
	private String username;
	// 邮件
	private String email;
	// 密码
	private String password;
	// 用户类型 1:系统管理员，保留字段 配置t_sms_dict.param_type=user_type
	private String userType;
	// 用户状态：0: 禁用 1:正常， 配置t_sms_dict.param_type=user_status
	private String status;
	// 手机
	private String mobile;
	// 真实姓名
	private String realname;
	//
	private Date createDate;
	//
	private Date updateDate;
	//
	private Integer loginTimes;
	// web应用ID：1短信调度系统 2代理商平台 3运营平台 4OEM代理商平台
	private Integer webId;

	private String webIdName;
	// 用户对应的roleId
	private Integer roleId;
	// 用户对应的roleName
	private String roleName;

	private Integer departmentId;

	private Integer dataAuthority;

	private Integer departmentLevel;

	private List<Role> roles;

	public String getWebIdName() {
		return webIdName;
	}

	public void setWebIdName(String webIdName) {
		this.webIdName = webIdName;
	}

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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUserType() {
		return userType;
	}

	public void setUserType(String userType) {
		this.userType = userType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getRealname() {
		return realname;
	}

	public void setRealname(String realname) {
		this.realname = realname;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public Integer getLoginTimes() {
		return loginTimes;
	}

	public void setLoginTimes(Integer loginTimes) {
		this.loginTimes = loginTimes;
	}

	public Integer getWebId() {
		return webId;
	}

	public void setWebId(Integer webId) {
		this.webId = webId;
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

	public Integer getDepartmentId() {
		return departmentId;
	}

	public void setDepartmentId(Integer departmentId) {
		this.departmentId = departmentId;
	}

	public Integer getDataAuthority() {
		return dataAuthority;
	}

	public void setDataAuthority(Integer dataAuthority) {
		this.dataAuthority = dataAuthority;
	}

	public Integer getDepartmentLevel() {
		return departmentLevel;
	}

	public void setDepartmentLevel(Integer departmentLevel) {
		this.departmentLevel = departmentLevel;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	@Override
	public String toString() {
		return "User [id=" + id + ", sid=" + sid + ", username=" + username + ", email=" + email + ", password="
				+ password + ", userType=" + userType + ", status=" + status + ", mobile=" + mobile + ", realname="
				+ realname + ", createDate=" + createDate + ", updateDate=" + updateDate + ", loginTimes=" + loginTimes
				+ ", webId=" + webId + ", webIdName=" + webIdName + ", roleId=" + roleId + ", roleName=" + roleName
				+ ", departmentId=" + departmentId + ", dataAuthority=" + dataAuthority + "]";
	}

}