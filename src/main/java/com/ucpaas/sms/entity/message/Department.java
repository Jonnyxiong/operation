package com.ucpaas.sms.entity.message;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ucpaas.sms.common.util.Collections3;

/**
 * @description 部门
 * @author
 * @date 2017-07-25
 */
public class Department {

	// 部门id，自增长
	private Integer departmentId;
	// 部门名称
	private String departmentName;
	// 部门描述
	private String departmentDesc;
	// 父级部门，基础部门的父级部门填空
	private Integer parentId;
	// 部门级别，0：基础部门，1：一级部门，2：二级部门，依此类推
	private Integer level;
	// 状态，0：关闭，1：正常
	private Integer state;
	// 创建者ID，关联t_sms_user表中id字段
	private Long createId;
	// 创建时间
	private Date createTime;
	// 修改时间
	private Date updateTime;

	private List<Role> roles;

	private String rolesAsStr;

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

	public String getDepartmentDesc() {
		return departmentDesc;
	}

	public void setDepartmentDesc(String departmentDesc) {
		this.departmentDesc = departmentDesc;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Long getCreateId() {
		return createId;
	}

	public void setCreateId(Long createId) {
		this.createId = createId;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public List<Role> getRoles() {
		return roles;
	}

	public void setRoles(List<Role> roles) {
		this.roles = roles;
	}

	public String getRolesAsStr() {
		rolesAsStr = "";
		if (!Collections3.isEmpty(this.roles)) {
			StringBuilder sb = new StringBuilder();
			for (Role role: this.roles){
				if (sb.length() <= 0){
					sb.append(role.getRoleName());
				}else {
					sb.append("、");
					sb.append(role.getRoleName());
				}
			}
			rolesAsStr = sb.toString();
		}
		return rolesAsStr;
	}
}