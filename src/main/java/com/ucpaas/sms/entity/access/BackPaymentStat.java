package com.ucpaas.sms.entity.access;

import org.apache.ibatis.type.Alias;

import java.math.BigDecimal;
import java.util.Date;

@Alias("BackPaymentStat")
public class BackPaymentStat {
    
    // 序号id, 自增长
    private Integer id;
    // 代理商预付充值金额, 单位:元
    private BigDecimal prepayRecharge;
    // 直客消耗金额, 单位:元
    private BigDecimal directConsume;
    // 统计类型, 0:每日, 2: 每月
    private Integer stattype;
    // 统计的数据时间(天)
    private Integer date;
    // 创建时间, 信息保存时间
    private Date createtime;
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id ;
    }
    
    public BigDecimal getPrepayRecharge() {
        if(prepayRecharge == null){
            prepayRecharge = BigDecimal.ZERO;
        }
        return prepayRecharge;
    }
    
    public void setPrepayRecharge(BigDecimal prepayRecharge) {
        this.prepayRecharge = prepayRecharge ;
    }
    
    public BigDecimal getDirectConsume() {
        if(directConsume == null){
            directConsume = BigDecimal.ZERO;
        }
        return directConsume;
    }
    
    public void setDirectConsume(BigDecimal directConsume) {
        this.directConsume = directConsume ;
    }
    
    public Integer getStattype() {
        return stattype;
    }
    
    public void setStattype(Integer stattype) {
        this.stattype = stattype ;
    }
    
    public Integer getDate() {
        return date;
    }
    
    public void setDate(Integer date) {
        this.date = date ;
    }
    
    public Date getCreatetime() {
        return createtime;
    }
    
    public void setCreatetime(Date createtime) {
        this.createtime = createtime ;
    }
    
}