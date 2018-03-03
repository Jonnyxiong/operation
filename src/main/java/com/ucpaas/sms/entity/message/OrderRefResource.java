package com.ucpaas.sms.entity.message;

import java.util.Date;

import org.apache.ibatis.type.Alias;

/**
 * @description 订单资源关联表
 * @author huangwenjie
 * @date 2017-06-29
 */
@Alias("OrderRefResource")
public class OrderRefResource {
    
    // 序号，自增长
    private Integer id;
    // 订单号，关联t_sms_order_list表中demand_id字段
    private String orderId;
    // 资源编号，关联t_sms_resource_list表中resource_id字段
    private String resourceId;
    // 创建时间
    private Date createTime;
    
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
    
    public String getResourceId() {
        return resourceId;
    }
    
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId ;
    }
    
    public Date getCreateTime() {
        return createTime;
    }
    
    public void setCreateTime(Date createTime) {
        this.createTime = createTime ;
    }
    
}