package com.ucpaas.sms.entity.message;


import org.apache.ibatis.type.Alias;

/**
 * @description 用户角色关系表
 * @author huangwenjie
 * @date 2017-07-15
 */
@Alias("UserRole")
public class UserRole {
    
    // 
    private Integer ruId;
    // 角色id
    private Integer roleId;
    // 主账号sid
    private Long userId;
    
    public Integer getRuId() {
        return ruId;
    }
    
    public void setRuId(Integer ruId) {
        this.ruId = ruId ;
    }
    
    public Integer getRoleId() {
        return roleId;
    }
    
    public void setRoleId(Integer roleId) {
        this.roleId = roleId ;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId ;
    }
    
}