package com.ucpaas.sms.entity.po;

import com.ucpaas.sms.common.util.Collections3;
import com.ucpaas.sms.common.util.StringUtils;
import com.ucpaas.sms.entity.message.Role;
import com.ucpaas.sms.entity.message.User;

public class UserPo extends User {

	private String departmentName;

	// 用户状态：0: 禁用 1:正常， 配置t_sms_dict.param_type=user_status
	private String statusDesc;

	// 用户对应的roleName
	private String rolesAsStr;

	private String dataAuthorityDesc;

	public String getStatusDesc() {
		statusDesc = "";
		if (StringUtils.isNotBlank(this.getStatus())) {
			switch (this.getStatus()) {
			case "1":
				statusDesc = "正常";
				break;
			case "2":
				statusDesc = "禁用";
				break;
			}
		}
		return statusDesc;
	}

	public String getRolesAsStr() {
		rolesAsStr = "";
		if (!Collections3.isEmpty(this.getRoles())) {
			StringBuilder sb = new StringBuilder();
			for (Role role : this.getRoles()) {
				if (sb.length() <= 0) {
					sb.append(role.getRoleName());
				} else {
					sb.append("、");
					sb.append(role.getRoleName());
				}
			}
			rolesAsStr = sb.toString();
		}
		return rolesAsStr;
	}

	public String getDataAuthorityDesc() {
		dataAuthorityDesc = "";
		if (this.getDataAuthority() != null) {
			switch (this.getDataAuthority()) {
			case 0:
				dataAuthorityDesc = "仅看自己数据";
				break;
			case 1:
				dataAuthorityDesc = "所在部门及下级部门";
				break;
			}
		}
		return dataAuthorityDesc;
	}

	public String getDepartmentName() {
		return departmentName;
	}

	public void setDepartmentName(String departmentName) {
		this.departmentName = departmentName;
	}
}