package com.ucpaas.sms.service.notice;

import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.entity.JsmsNoticeList;
import com.jsmsframework.common.enums.WebId;
import com.jsmsframework.common.exception.JsmsNoticeListException;
import com.jsmsframework.common.mapper.JsmsNoticeListMapper;
import com.ucpaas.sms.common.util.DateUtils;
import com.ucpaas.sms.entity.message.Notice;
import com.ucpaas.sms.entity.message.NoticeExt;
import com.ucpaas.sms.mapper.message.NoticeMapper;
import com.ucpaas.sms.util.ConfigUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * created by xiaoqingwen on 2017/12/5 11:27
 */
//公告管理相关
@Service
public class NoticeServiceImpl implements NoticeService {
    private static Logger logger = LoggerFactory.getLogger(NoticeServiceImpl.class);
    @Autowired
    private NoticeMapper noticeMapper;
    @Autowired
    private JsmsNoticeListMapper jsmsNoticeListMapper;


    /**
     * 公告管理
     * @return
     */
    public Map<String,Object> showNoticeList(String noticeNameOrRealname, Integer webId, Integer status, Integer currentPage, Integer pageRowCount) {

        Map<String,Object> map=new HashMap<>();
        //将到了发布时间但状态还是未发布的公告更新为已发布
        updateStatusToPublished();
        //查询出公告总数
        int totalCount = noticeMapper.getTotalCount(webId, status, noticeNameOrRealname);
       // if(totalCount<1){
      //      throw new JsmsNoticeListException("未找到符合要求的数据");
      //  }
        //开始条数
        Integer startRow=(currentPage-1)*pageRowCount;
        //总页数
        Integer totalPage = totalCount / pageRowCount + (totalCount % pageRowCount == 0 ? 0 : 1);
        //公告列表
        List<NoticeExt> noticeExtList = noticeMapper.searchNoticeByPara(webId, status, noticeNameOrRealname,startRow,pageRowCount);
       // if(Collections3.isEmpty(noticeExtList)){
        //    throw new JsmsNoticeListException("已无更多页");
       // }
        for (int i=0;i<noticeExtList.size();i++){
            NoticeExt noticeExt = noticeExtList.get(i);
            //发布时间
            if(null!=(noticeExt.getReleaseTime())){
                noticeExt.setReleaseTimeToStr(DateUtils.formatDate(noticeExt.getReleaseTime(),"yyyy-MM-dd HH:mm"));
            }
            if(null!=(noticeExt.getUpdateTime())){
                noticeExt.setUpdateTimeToStr(DateUtils.formatDate(noticeExt.getUpdateTime(),"yyyy-MM-dd HH:mm"));
            }
            //截取标题长度(长度带点为15位)
            String noticeName=noticeExt.getNoticeName();
            if(noticeName.length()>15){
             noticeExt.setNoticeName(noticeName.substring(0,12)+"...");
            }
        }

        //当前页
        map.put("currentPage",currentPage);
        //结果集
        map.put("list",noticeExtList);
        //每页条数
        map.put("pageRowCount",pageRowCount);
        //总条数
        map.put("totalCount",totalCount);
        //总页数
        map.put("totalPage",totalPage);
        //每页开始条数
        map.put("startRow",startRow+1);
        //每页的结尾条
        map.put("endRow",(startRow+noticeExtList.size()));
        return map;
    }
    /**
     * 编辑
     */
    public R edit(Notice notice){

        //将到了发布时间但状态还是未发布的公告更新为已发布
        updateStatusToPublished();
        Map<String,Object> data=new HashMap<>();
        Notice noticeDetails = noticeMapper.getNoticeByPara(notice);
        if(noticeDetails==null){
            throw new JsmsNoticeListException("此公告不存在!");
        }
        //发布时间
        if(null!= noticeDetails.getReleaseTime()){
            noticeDetails.setReleaseTimeToStr(DateUtils.formatDate(noticeDetails.getReleaseTime(),"yyyy-MM-dd HH:mm"));
        }
        data.put("noticeDetails",noticeDetails);


        // 放入上传路径
        String smspImgUrl = ConfigUtils.smsp_img_url.endsWith("/") ? ConfigUtils.smsp_img_url.substring(0, ConfigUtils.smsp_img_url.lastIndexOf("/")) : ConfigUtils.smsp_img_url;
        data.put("path", smspImgUrl+"/upload/uploadAutoNew.html");

        return R.ok("succes",data);
    }

    /**
     * 根据条件查看公告详情
     * @param notice
     * @return
     */
    public R noticeDetails(Notice notice) {

        //将到了发布时间但状态还是未发布的公告更新为已发布
        updateStatusToPublished();
        Map<String,Object> data=new HashMap<>();
        Notice noticeDetails = noticeMapper.getNoticeByPara(notice);
        if(noticeDetails==null){
            throw new JsmsNoticeListException("此公告不存在!");
        }
        //发布时间
        if(null!=(noticeDetails.getReleaseTime())){
            noticeDetails.setReleaseTimeToStr(DateUtils.formatDate(noticeDetails.getReleaseTime(),"yyyy年MM月dd日 HH:mm"));
        }
        //更新时间
        if(null!=(noticeDetails.getUpdateTime())){
            noticeDetails.setUpdateTimeToStr(DateUtils.formatDate(noticeDetails.getUpdateTime(),"yyyy年MM月dd日 HH:mm"));
        }

        data.put("noticeDetails",noticeDetails);
        return R.ok("succes",data);
    }

    /**
     * 添加公告
     * @param notice
     * @return
     */
    @Transactional("message")
    public R addNotice(JsmsNoticeList notice) {

        if(StringUtils.isBlank(notice.getNoticeName())){
            throw new JsmsNoticeListException("公告标题不能为空!");
        }
        if(StringUtils.isBlank(notice.getNoticeContent())){
            throw new JsmsNoticeListException("公告内容不能为空!");
        }
        if(notice.getIsTop()<0 || notice.getIsTop()>1){
            throw new JsmsNoticeListException("是否置顶取值有误!");
        }
        if(null==notice.getWebId()){
            throw new JsmsNoticeListException("应用系统未选值!");
        }
        if(notice.getNoticeName().length()>50){
            throw new JsmsNoticeListException("公告标题不能超过50!");
        }
        if(notice.getNoticeContent().length()>65535){
            throw new JsmsNoticeListException("公告内容长度不能超过65535!");
        }
        Date now = new Date();
        //根据发布时间有值认为是定时,无值认为是立即发送
        if(null ==notice.getReleaseTime()){
            notice.setStatus(1);//已发布
            notice.setReleaseTime(now);
        }else{
            notice.setStatus(0);//待发布
        }
        //更新时间
        notice.setUpdateTime(now);
        //此时为编辑
        int i=0;
        if(notice.getId()!=null){
            i = jsmsNoticeListMapper.update(notice);
        }else{
            //此时为添加
            i = jsmsNoticeListMapper.insert(notice);
        }
        if(i!=1){
            return R.error("添加数据失败!");
        }
        return R.ok("添加数据成功!");
    }

    /**
     * 公告列表
     * @param currentPage
     * @param pageRowCount
     * @return
     */
    @Transactional("message")
    public Map<String,Object> list(Integer currentPage, Integer pageRowCount) {

        Map<String,Object> data=new HashMap<>();
        //将到了发布时间但状态还是未发布的公告更新为已发布
        updateStatusToPublished();

        //查询出已发布公告总数
        //Integer totalCount = noticeMapper.getTotalCountByPara(null, 1, null);
        //状态为已发布
        data.put("status",1);
        data.put("webId",WebId.运营平台.getValue());
        int totalCount = jsmsNoticeListMapper.count(data);
        if(totalCount<1){
            throw new JsmsNoticeListException("未找到符合要求的数据");
        }
        //查询出已发布的公告
        //method(null,null,1,currentPage,pageRowCount,data,totalCount);
        //开始条数
        Integer startRow=(currentPage-1)*pageRowCount;
        //总页数
        Integer totalPage = totalCount / pageRowCount + (totalCount % pageRowCount == 0 ? 0 : 1);
        //公告列表
       // List<NoticeExt> noticeExtList = noticeMapper.searchNoticeByPara(webId, status, noticeNameOrRealname,startRow,pageRowCount);
        List<Notice> noticeExtList = noticeMapper.getNoticeList(WebId.运营平台.getValue(),startRow, pageRowCount);//3为运营平台
        if(noticeExtList!=null && noticeExtList.size()>0){
            for (int i=0;i<noticeExtList.size();i++){
                Notice noticeExt = noticeExtList.get(i);
                //发布时间
                if(null!=(noticeExt.getReleaseTime())){
                    noticeExt.setReleaseTimeToStr(DateUtils.formatDate(noticeExt.getReleaseTime(),"yyyy-MM-dd"));
                }
                if(null!=(noticeExt.getUpdateTime())){
                    noticeExt.setUpdateTimeToStr(DateUtils.formatDate(noticeExt.getUpdateTime(),"yyyy-MM-dd"));
                }
                //截取标题长度(长度带点为15位)
                String noticeName=noticeExt.getNoticeName();
                if(noticeName.length()>15){
                    noticeExt.setNoticeName(noticeName.substring(0,12)+"...");
                }
            }
        }

        Map<String,Object> map=new HashMap<>();
        //当前页
        map.put("currentPage",currentPage);
        //结果集
        map.put("list",noticeExtList);
        //每页条数
        map.put("pageRowCount",pageRowCount);
        //总条数
        map.put("totalCount",totalCount);
        //总页数
        map.put("totalPage",totalPage);
        //每页开始条数
        map.put("startRow",startRow+1);
        //每页的结尾条
        map.put("endRow",(startRow+noticeExtList.size()));
        return map;
    }

    private void updateStatusToPublished() {
        //获取发布时间小于现在,但是还未发布的公告
        List<Notice> noticeList = noticeMapper.getNotRelease();
        //需要先把到了时间的公告更新为已发布
        if(noticeList!=null && noticeList.size()>0){
            for(int i=0;i < noticeList.size();i++){
                Notice notice = noticeList.get(i);
                int j=jsmsNoticeListMapper.updateStatus(1,notice.getId());
                logger.debug("更新id为--->"+notice.getId()+"为已发布状态了");
            }
        }
    }

    /**
     * 删除公告
     * @param id
     * @return
     */
    public R deleteNotice(Integer id) {
        int i = noticeMapper.deleteNotice(id);
        if(i!=1){
            throw new JsmsNoticeListException("删除失败");
        }
        return R.ok("删除成功!");
    }

    /**
     * 公告改变状态按钮
     * @param status
     * @param id
     * @return
     */
    public R updateStatus(Integer status, Integer id) {
        if(null==id){
            throw new JsmsNoticeListException("次公告不存在!");
        }
        if(null==status ||(status>2 || status<0)){
            throw new JsmsNoticeListException("状态取值有误!");
        }
        int i = jsmsNoticeListMapper.updateStatus(status, id);
        if(i!=1){
            throw new JsmsNoticeListException("状态改变失败");
        }
        return R.ok("状态改变成功!");
    }
}
