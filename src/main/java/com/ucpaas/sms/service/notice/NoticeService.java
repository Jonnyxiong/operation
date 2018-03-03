package com.ucpaas.sms.service.notice;

import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.common.entity.JsmsNoticeList;
import com.ucpaas.sms.entity.message.Notice;
import com.ucpaas.sms.entity.message.NoticeExt;
import com.ucpaas.sms.model.PageContainer;
import org.apache.ibatis.annotations.Param;

import java.util.Map;

/**
 * created by xiaoqingwen on 2017/12/4 18:25
 */
//公告管理相关的接口
public interface NoticeService {
    //公告管理列表及搜索
    public Map<String,Object> showNoticeList(String noticeNameOrRealname, Integer webId, Integer status, Integer currentPage, Integer pageRowCount);
    //根据条件查看公告详情
    public R noticeDetails(Notice notice);
    //添加公告
    public R addNotice(JsmsNoticeList jsmsNoticeList);
    //公告列表
    public Map<String,Object> list(Integer currentPage,Integer pageRowCount);
    //公告删除
    public R deleteNotice(Integer id);
    //公告更改状态按钮
    public R updateStatus(Integer status,Integer id);
    //编辑
    public R edit(Notice notice);

}
