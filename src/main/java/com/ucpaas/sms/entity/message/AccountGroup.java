package com.ucpaas.sms.entity.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.ibatis.type.Alias;

import java.util.Date;
import java.util.List;

/**
 * @description 客户组
 * @author
 * @date 2017-07-25
 */
@Alias("AccountGroup")
public class AccountGroup {

	// 客户组id，主键，自增长
	private Integer accountGid;
	// 客户组名称，唯一
	private String accountGname;
	// 客户类型，0：代理商子客户，1：直客
	private Integer type;
	// 状态，0：关闭，1：正常
	private Integer state;
	// 备注
	private String remarks;
	// 创建者ID，关联t_sms_user表中id字段
	private Long createId;
	// 创建时间
	private Date createTime;
	// 修改时间
	private Date updateTime;

	private String groupClients;

	// 客户信息列表
	private List<Account> clients;

	public Integer getAccountGid() {
		return accountGid;
	}

	public void setAccountGid(Integer accountGid) {
		this.accountGid = accountGid;
	}

	public String getAccountGname() {
		return accountGname;
	}

	public void setAccountGname(String accountGname) {
		this.accountGname = accountGname;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
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

    public List<Account> getClients() {
		return clients;
	}

	public void setClients(List<Account> clients) {
		this.clients = clients;
	}

    public String getGroupClients() {
        return groupClients;
    }

    public void setGroupClients(String groupClients) {
        this.groupClients = groupClients;
    }
}