package com.ucpaas.sms.entity.message;

import java.util.Date;

/**
 * created by xiaoqingwen on 2017/12/4 20:32
 */
//公告管理实体类
public class Notice {
    private Integer id;
    //公告正文
    private String noticeName;
    //公告内容
    private String noticeContent;
    //是否置顶，0:否,1:是
    private Integer isTop;
    //状态，0：待发布,1:已发布,2:已取消
    private Integer status;
    //web应用ID，1:短信调度系统,2:代理商平台,3:运营平台,4:OEM代理商平台,5:品牌客户端,6:OEM客户端
    private Integer webId;
    //操作者id, 关联t_sms_user表中id字段
    private Long adminId;
    //发布时间
    private Date releaseTime;
    //更新时间
    private Date updateTime;
    //发布时间的string模式
    private String releaseTimeToStr;
    //更新时间的string模式
    private String updateTimeToStr;

    public String getReleaseTimeToStr() {
        return releaseTimeToStr;
    }

    public void setReleaseTimeToStr(String releaseTimeToStr) {
        this.releaseTimeToStr = releaseTimeToStr;
    }

    public String getUpdateTimeToStr() {
        return updateTimeToStr;
    }

    public void setUpdateTimeToStr(String updateTimeToStr) {
        this.updateTimeToStr = updateTimeToStr;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNoticeName() {
        return noticeName;
    }

    public void setNoticeName(String noticeName) {
        this.noticeName = noticeName;
    }

    public String getNoticeContent() {
        return noticeContent;
    }

    public void setNoticeContent(String noticeContent) {
        this.noticeContent = noticeContent;
    }

    public Integer getIsTop() {
        return isTop;
    }

    public void setIsTop(Integer isTop) {
        this.isTop = isTop;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getWebId() {
        return webId;
    }

    public void setWebId(Integer webId) {
        this.webId = webId;
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(Long adminId) {
        this.adminId = adminId;
    }

    public Date getReleaseTime() {
        return releaseTime;
    }

    public void setReleaseTime(Date releaseTime) {
        this.releaseTime = releaseTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
