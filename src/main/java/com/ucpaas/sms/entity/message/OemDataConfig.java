package com.ucpaas.sms.entity.message;

import java.math.BigDecimal;

import org.apache.ibatis.type.Alias;

/**
 * @description OEM资料配置
 * @author huangwenjie
 * @date 2017-07-13
 */
@Alias("OemDataConfig")
public class OemDataConfig {
    
    // 序号,自增长
    private Integer id;
    // 域名
    private String domainName;
    // 代理商id,唯一
    private Integer agentId;
    // 版权文件
    private String copyrightText;
    // 标签页LOGO图,尺寸48*48
    private String tabLogoUrl;
    // LOGO图,尺寸41*43
    private String logoUrl;
    // 公司名称图,尺寸184*30
    private String companyLogoUrl;
    // 接口文档,文件 <= 2M
    private String apiDocumentUrl;
    // FAQ文档,文件 <= 2M
    private String fAQDocumentUrl;
    // 导航栏背景色,16进制颜色码	如:天蓝色为#87CEEB
    private String navigationBackcolor;
    // 导航栏文字颜色,16进制颜色码	如:天蓝色为#87CEEB
    private String navigationTextColor;
    // 行业短信折扣率
    private BigDecimal hySmsDiscount;
    // 营销短信折扣率
    private BigDecimal yxSmsDiscount;
    // 国际短信折扣率
    private BigDecimal gjSmsDiscount;
    // 测试短信产品id号，关联t_sms_oem_product_info表中product_id字段

    private Integer testProductId;
    // 赠送的测试短信条数
    private Integer testSmsNumber;
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id ;
    }
    
    public String getDomainName() {
        return domainName;
    }
    
    public void setDomainName(String domainName) {
        this.domainName = domainName ;
    }
    
    public Integer getAgentId() {
        return agentId;
    }
    
    public void setAgentId(Integer agentId) {
        this.agentId = agentId ;
    }
    
    public String getCopyrightText() {
        return copyrightText;
    }
    
    public void setCopyrightText(String copyrightText) {
        this.copyrightText = copyrightText ;
    }
    
    public String getTabLogoUrl() {
        return tabLogoUrl;
    }
    
    public void setTabLogoUrl(String tabLogoUrl) {
        this.tabLogoUrl = tabLogoUrl ;
    }
    
    public String getLogoUrl() {
        return logoUrl;
    }
    
    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl ;
    }
    
    public String getCompanyLogoUrl() {
        return companyLogoUrl;
    }
    
    public void setCompanyLogoUrl(String companyLogoUrl) {
        this.companyLogoUrl = companyLogoUrl ;
    }
    
    public String getApiDocumentUrl() {
        return apiDocumentUrl;
    }
    
    public void setApiDocumentUrl(String apiDocumentUrl) {
        this.apiDocumentUrl = apiDocumentUrl ;
    }
    
    public String getFAQDocumentUrl() {
        return fAQDocumentUrl;
    }
    
    public void setFAQDocumentUrl(String fAQDocumentUrl) {
        this.fAQDocumentUrl = fAQDocumentUrl ;
    }
    
    public String getNavigationBackcolor() {
        return navigationBackcolor;
    }
    
    public void setNavigationBackcolor(String navigationBackcolor) {
        this.navigationBackcolor = navigationBackcolor ;
    }
    
    public String getNavigationTextColor() {
        return navigationTextColor;
    }
    
    public void setNavigationTextColor(String navigationTextColor) {
        this.navigationTextColor = navigationTextColor ;
    }
    
    public BigDecimal getHySmsDiscount() {
        return hySmsDiscount;
    }
    
    public void setHySmsDiscount(BigDecimal hySmsDiscount) {
        this.hySmsDiscount = hySmsDiscount ;
    }
    
    public BigDecimal getYxSmsDiscount() {
        return yxSmsDiscount;
    }
    
    public void setYxSmsDiscount(BigDecimal yxSmsDiscount) {
        this.yxSmsDiscount = yxSmsDiscount ;
    }
    
    public BigDecimal getGjSmsDiscount() {
        return gjSmsDiscount;
    }
    
    public void setGjSmsDiscount(BigDecimal gjSmsDiscount) {
        this.gjSmsDiscount = gjSmsDiscount ;
    }
    
    public Integer getTestProductId() {
        return testProductId;
    }
    
    public void setTestProductId(Integer testProductId) {
        this.testProductId = testProductId ;
    }
    
    public Integer getTestSmsNumber() {
        return testSmsNumber;
    }
    
    public void setTestSmsNumber(Integer testSmsNumber) {
        this.testSmsNumber = testSmsNumber ;
    }
    
}