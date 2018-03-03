package com.ucpaas.sms.mapper.message;

import com.ucpaas.sms.entity.message.Notice;
import com.ucpaas.sms.entity.message.NoticeExt;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * created by xiaoqingwen on 2017/12/4 20:18
 */
@Repository
public interface NoticeMapper {
    //根据条件搜索公告管理表的信息(t_sms_notice_list)
    public List<NoticeExt> searchNoticeByPara(@Param("webId") Integer webId,@Param("status") Integer status,@Param("noticeNameOrRealname") String noticeNameOrRealname,@Param("startRow") Integer startRow,@Param("pageRowCount") Integer pageRowCount);
    //查询出公告总数
    public int getTotalCount(@Param("webId")Integer webId,@Param("status")Integer status,@Param("noticeNameOrRealname")String noticeNameOrRealname);
    //根据参数条件获取公告列表信息
    public Notice getNoticeByPara(Notice notice);
    //插入数据到公告表中
    public int insert(Notice notice);
    //获取发布时间小于现在,但是还未发布的公告
    public List<Notice> getNotRelease();
    //更改表数据的状态为已发布
    //public int updateStatus(@Param("id") Integer id);
    //公告列表(已经发布,置顶需显示)
    public List<Notice> getNoticeList(@Param("webId") Integer webId,@Param("startRow") Integer startRow,@Param("pageRowCount") Integer pageRowCount);
    //删除公告
    public int deleteNotice(@Param("id") Integer id);

}
