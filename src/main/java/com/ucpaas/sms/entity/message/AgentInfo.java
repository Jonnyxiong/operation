package com.ucpaas.sms.entity.message;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.ibatis.type.Alias;

/**
 * @description 代理商信息表
 * @author huangwenjie
 * @date 2017-07-13
 */
@Alias("AgentInfo")
public class AgentInfo {
    
    // 代理商id,YYYYMM(年月)+序号（0000－9999），10位
    private Integer agentId;
    // 管理员id
    private Long adminId;
    // 代理商名称
    private String agentName;
    // 代理商简称
    private String shorterName;
    // 代理商类型,1:销售代理商,2:品牌代理商,3:资源合作商,4:代理商和资源合作,5:OEM代理商 配置tb_sms_params.param_type=agent_type
    private Integer agentType;
    // 代理商状态，1：注册完成，5：冻结，6：注销
    private String status;
    // 认证状态，2：待认证 ，3：证件已认证(正常)，4：认证不通过
    private Integer oauthStatus;
    // 认证时间
    private Date oauthDate;
    // 联系地址/公司地址
    private String address;
    // 公司名称
    private String company;
    // 公司电话
    private String companyNbr;
    // 手机号码
    private String mobile;
    // 所属销售，关联t_sms_user表中id字段

    private Long belongSale;
    // 创建时间
    private Date createTime;
    // 更新时间
    private Date updateTime;
    // 备注
    private String remark;
    // 代理商返点使用比例，每个季度末根据代理商本季度总消耗进行更新（激励返点需要在用户有新订单时才能使用，每一笔新订单最多可以使用的返点额度为成交额的N%，N值为此字段值）
    private BigDecimal rebateUseRadio;
    
    public Integer getAgentId() {
        return agentId;
    }
    
    public void setAgentId(Integer agentId) {
        this.agentId = agentId ;
    }
    
    public Long getAdminId() {
        return adminId;
    }
    
    public void setAdminId(Long adminId) {
        this.adminId = adminId ;
    }
    
    public String getAgentName() {
        return agentName;
    }
    
    public void setAgentName(String agentName) {
        this.agentName = agentName ;
    }
    
    public String getShorterName() {
        return shorterName;
    }
    
    public void setShorterName(String shorterName) {
        this.shorterName = shorterName ;
    }
    
    public Integer getAgentType() {
        return agentType;
    }
    
    public void setAgentType(Integer agentType) {
        this.agentType = agentType ;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status ;
    }
    
    public Integer getOauthStatus() {
        return oauthStatus;
    }
    
    public void setOauthStatus(Integer oauthStatus) {
        this.oauthStatus = oauthStatus ;
    }
    
    public Date getOauthDate() {
        return oauthDate;
    }
    
    public void setOauthDate(Date oauthDate) {
        this.oauthDate = oauthDate ;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address ;
    }
    
    public String getCompany() {
        return company;
    }
    
    public void setCompany(String company) {
        this.company = company ;
    }
    
    public String getCompanyNbr() {
        return companyNbr;
    }
    
    public void setCompanyNbr(String companyNbr) {
        this.companyNbr = companyNbr ;
    }
    
    public String getMobile() {
        return mobile;
    }
    
    public void setMobile(String mobile) {
        this.mobile = mobile ;
    }
    
    public Long getBelongSale() {
        return belongSale;
    }
    
    public void setBelongSale(Long belongSale) {
        this.belongSale = belongSale ;
    }
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime ;
    }
    
    public Date getUpdateTime() {
        return updateTime;
    }
    
    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime ;
    }
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark ;
    }
    
    public BigDecimal getRebateUseRadio() {
        return rebateUseRadio;
    }
    
    public void setRebateUseRadio(BigDecimal rebateUseRadio) {
        this.rebateUseRadio = rebateUseRadio ;
    }
    
}
 