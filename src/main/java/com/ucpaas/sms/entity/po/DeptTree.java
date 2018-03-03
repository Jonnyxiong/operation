package com.ucpaas.sms.entity.po;

import java.util.List;

/**
 * Created by lpjLiu on 2017/7/27.
 */
public class DeptTree {
	private Integer deptId;
	private Integer level;
	private Integer parentId;
	private String deptName;
	private Integer userCount;

	private List<DeptTree> subDepts;

	public Integer getDeptId() {
		return deptId;
	}

	public void setDeptId(Integer deptId) {
		this.deptId = deptId;
	}

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	public String getDeptName() {
		return deptName;
	}

	public void setDeptName(String deptName) {
		this.deptName = deptName;
	}

	public Integer getUserCount() {
		return userCount;
	}

	public void setUserCount(Integer userCount) {
		this.userCount = userCount;
	}

	public List<DeptTree> getSubDepts() {
		return subDepts;
	}

	public void setSubDepts(List<DeptTree> subDepts) {
		this.subDepts = subDepts;
	}
}
