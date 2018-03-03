package com.ucpaas.sms.service.statistic;

import com.jsmsframework.channel.entity.JsmsChannel;
import com.jsmsframework.channel.entity.JsmsComplaintListExt;
import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.R;
import com.jsmsframework.user.entity.JsmsAccount;
import com.jsmsframework.user.entity.JsmsUser;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * created by xiaoqingwen on 2018/1/9 18:17
 * 投诉管理分析统计
 */
public interface ComplaintListService {
    //获取全部客户(account)
    JsmsPage  queryListForAccount(JsmsPage jsmsPage);
    //获取归属销售
    JsmsPage  queryListForUser(Long id,String condition,JsmsPage jsmsPage);
    //获取投诉通道,
    JsmsPage  queryListForChannel(JsmsPage jsmsPage);
    //搜索投诉,分页
    JsmsPage<JsmsComplaintListExt> queryList(JsmsPage page);
    //根据id删除投诉
    R deleteById(Integer id, Long userId);
    //批量添加投诉
    R addComplaintBatch(String adminId,String tempFileSavePath);
    //导出的搜索
    List<JsmsComplaintListExt> findList(Map<String,Object> params);
}