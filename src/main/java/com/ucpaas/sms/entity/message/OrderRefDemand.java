package com.ucpaas.sms.entity.message;

import java.util.Date;

import org.apache.ibatis.type.Alias;

/**
 * @description 订单资源关联表
 * @author huangwenjie
 * @date 2017-06-29
 */
@Alias("OrderRefDemand")
public class OrderRefDemand {
    
    // 序号，自增长
    private Integer id;
    // 订单号，关联t_sms_order_list表中demand_id字段
    private String orderId;
    // 需求编号，关联t_sms_demand_list表中demain_id字段
    private String demandId;
    // 状态，0：关闭，1：开启
    private Integer state;
    // 创建时间
    private Date createTime;
    // 修改时间
    private Date updateTime;
    
    public Integer getId() {
        return id;
    }
    
    public void setId(Integer id) {
        this.id = id ;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId ;
    }
    
    public String getDemandId() {
        return demandId;
    }
    
    public void setDemandId(String demandId) {
        this.demandId = demandId ;
    }
    
    public Integer getState() {
        return state;
    }
    
    public void setState(Integer state) {
        this.state = state ;
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
    
}