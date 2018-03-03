package com.ucpaas.sms.entity.message;

import java.util.Date;

import org.apache.ibatis.type.Alias;

/**
 * @description 邮件配置表
 * @author huangwenjie
 * @date 2017-07-15
 */
@Alias("Mailprop")
public class Mailprop {
    
    // 
    private Long id;
    // 发送人
    private String frm;
    // 发送人的昵称
    private String fromnickname;
    // 收件人，可以有多个以符号隔开
    private String tonbr;
    // 抄送人，可以有多个以符号隔开
    private String cc;
    // 密送人，可以有多个以符号隔开
    private String bcc;
    // 主题
    private String subject;
    /*内容
1，验证邮件格式：
链接中参数为id=$id$，vcode=$vcode$，vtime=$vtime$
2，重置密码链接参数格式：
参数为email=$email$,vcode=$vcode$,vtime=$vtime$

*/
    private String text;
    // 附件所在磁盘详细地址
    private String atturl;
    // 1:邮箱验证模板，2:系统邮件模板，3：重置密码邮件 4：注册成功通知 5:修改密码成功通知 6:群发修改密码邮件 7:管理平台邮件模板（[@username@]表示昵称）8:账号激活营销邮件
    private String type;
    // 创建时间
    private Date createtime;
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id ;
    }
    
    public String getFrm() {
        return frm;
    }
    
    public void setFrm(String frm) {
        this.frm = frm ;
    }
    
    public String getFromnickname() {
        return fromnickname;
    }
    
    public void setFromnickname(String fromnickname) {
        this.fromnickname = fromnickname ;
    }
    
    public String getTonbr() {
        return tonbr;
    }
    
    public void setTonbr(String tonbr) {
        this.tonbr = tonbr ;
    }
    
    public String getCc() {
        return cc;
    }
    
    public void setCc(String cc) {
        this.cc = cc ;
    }
    
    public String getBcc() {
        return bcc;
    }
    
    public void setBcc(String bcc) {
        this.bcc = bcc ;
    }
    
    public String getSubject() {
        return subject;
    }
    
    public void setSubject(String subject) {
        this.subject = subject ;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text ;
    }
    
    public String getAtturl() {
        return atturl;
    }
    
    public void setAtturl(String atturl) {
        this.atturl = atturl ;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type ;
    }
    
    public Date getCreatetime() {
        return createtime;
    }
    
    public void setCreatetime(Date createtime) {
        this.createtime = createtime ;
    }
    
}