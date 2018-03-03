package com.ucpaas.sms.service.common;

import com.jsmsframework.common.enums.WebId;
import com.jsmsframework.common.mapper.JsmsNoticeListMapper;
import com.ucpaas.sms.dao.MessageMasterDao;
import com.ucpaas.sms.entity.message.Notice;
import com.ucpaas.sms.entity.po.DataAuthorityCondition;
import com.ucpaas.sms.mapper.message.AccountMapper;
import com.ucpaas.sms.mapper.message.AgentInfoMapper;
import com.ucpaas.sms.mapper.message.NoticeMapper;
import com.ucpaas.sms.util.AgentUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
@Service
@Transactional
public class OpsIndexServiceImpl implements OpsIndexService {

	private static final Logger logger = LoggerFactory.getLogger(OpsIndexServiceImpl.class);
	@Autowired
	private MessageMasterDao masterDao;
	@Autowired
	private NoticeMapper noticeMapper;
	@Autowired
	private AgentInfoMapper agentInfoMapper;
	@Autowired
	private AccountMapper accountMapper;
	@Autowired
	private JsmsNoticeListMapper jsmsNoticeListMapper;
	
	@Override
	public Map<String, Object> view(Map<String, String> params) {
		Map<String, Object> result = new HashMap<String, Object>();
		// 查询月新增代理商个数和当前代理商总数
		//Map<String, Object> agentNumMap = masterDao.getOneInfo("opsplatform.index.getAgentNum", params);
		// 查询月新增客户数和当前用户总数
		//Map<String, Object> agentUserNumMap = masterDao.getOneInfo("opsplatform.index.getAgentUserNum", params);
		// 查询代理商本月消费和当前总消费
		//Map<String, Object> agentConsumeMap = masterDao.getOneInfo("opsplatform.index.getAgentConsume", params);

		//5.17.2修改(xiaoqingwen)
		String id = params.get("id");
		Date now = new Date();
		//获取开始时间和结束时间
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
		//开始时间
		Date startTime = getMonthTime(0, now, format);
		//结束时间
		Date endTime = getMonthTime(1, now, format);

		DataAuthorityCondition dataAuthorityCondition = AgentUtils.getDataAuthorityCondition(Long.valueOf(id), true, false);
		if(null!=dataAuthorityCondition){

			//代理商总数
			int agent_num_total = agentInfoMapper.getAgentNum(dataAuthorityCondition);
			//客户总数
			int user_num_total = accountMapper.getAgentUserNum(dataAuthorityCondition);

			dataAuthorityCondition.setStartTime(startTime);
			dataAuthorityCondition.setEndTime(endTime);
			//本月新增代理个数
			int agent_increase_num=agentInfoMapper.getAgentNum(dataAuthorityCondition);
			//本月新增客户个数
			int user_increase_num=accountMapper.getAgentUserNum(dataAuthorityCondition);
			result.put("agent_num_total",agent_num_total);
			result.put("user_num_total",user_num_total);
			result.put("agent_increase_num",agent_increase_num);
			result.put("user_increase_num",user_increase_num);

		}
		//将到了发布时间但状态还是未发布的公告更新为已发布
		//获取发布时间小于现在,但是还未发布的公告
		List<Notice> noticeLi = noticeMapper.getNotRelease();
		//需要先把到了时间的公告更新为已发布
		if(noticeLi!=null && noticeLi.size()>0){
			for(int i=0;i < noticeLi.size();i++){
				Notice notice = noticeLi.get(i);
				int j=jsmsNoticeListMapper.updateStatus(1,notice.getId());
				logger.debug("更新id为--->"+notice.getId()+"为已发布状态了");
			}
		}
		//查询出公告列表
		List<Notice> noticeList = noticeMapper.getNoticeList(WebId.运营平台.getValue(),0,5);

		//格式化数据
		if(noticeList!=null && noticeList.size()>0){
			SimpleDateFormat sdf = new SimpleDateFormat("MM-dd");
			for (int i=0;i<noticeList.size();i++){
				Notice notice = noticeList.get(i);
				if(null!=notice.getReleaseTime()){
					//置顶
					if(notice.getIsTop().equals(1)){
						notice.setReleaseTimeToStr("置顶");
					}else{
						//非置顶的显示时间
						notice.setReleaseTimeToStr(sdf.format(notice.getReleaseTime()));
					}
				}
				//截取标题长度
				String noticeName = notice.getNoticeName();
				//需要格式化标题,暂时取15位(加上点为12位)
				if(noticeName.length()>15) {
					notice.setNoticeName(noticeName.substring(0, 12) + "...");
				}
				//内容格式化
//				String noticeContent = notice.getNoticeContent().replaceAll("</?[^>]+>", "");//去除html标签
//				if(noticeContent.length()>12){
//					String s = noticeContent.substring(0, 12);
//					notice.setNoticeContent(s);
//				}
				//格式化完毕
			}
		}


		if (noticeList == null)
		{
			noticeList = new ArrayList<>();
		}

		result.put("noticeList",noticeList);

		return result;
	}

	private Date getMonthTime(int months, Date now, SimpleDateFormat format) {
		String nowTime = format.format(now);
		Date date = null;
		try {
			date = format.parse(nowTime);
		} catch (ParseException e) {
			e.printStackTrace();
			logger.debug("时间转换问题.......");
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		calendar.add(Calendar.MONTH,months); // 日期减 如果不够减会将月变动
		Date time = calendar.getTime();
		return time;
	}

	/**
	 * 查询代理商趋势
	 */
	@Override
	public List<Map<String, Object>> queryAgentTendency(Map<String, Object> params) {
		DateTime dt = DateTime.now();
		String startTime = (String) params.get("start_time");
		String endTime = (String) params.get("end_time");
		if(StringUtils.isEmpty(startTime)){
			startTime = dt.minusDays(31).toString("yyyy-MM-dd");
		}
		params.put("start_time", startTime.replace("-", ""));
		
		if(StringUtils.isEmpty(endTime)){
			endTime = dt.minusDays(1).toString("yyyy-MM-dd");
		}
		params.put("end_time", endTime.replace("-", ""));
		params.put("data_type", "1");
		//List<Map<String, Object>> dataList = masterDao.getSearchList("opsplatform.index.queryAgentTendency", params);

		String id = (String) params.get("id");
		DataAuthorityCondition dataAuthorityCondition = AgentUtils.getDataAuthorityCondition(Long.valueOf(id), true, false);
		params.put("ids",dataAuthorityCondition.getIds());
		params.put("needQuerySaleIsNullData",dataAuthorityCondition.isNeedQuerySaleIsNullData());
		List<Map<String, Object>> dataList = masterDao.getSearchList("opsplatform.index.getAgentTendency", params);

		supplement(dataList, startTime, endTime);
		return dataList;
	}

	/**
	 * 查询客户趋势
	 */
	@Override
	public List<Map<String, Object>> queryClientTendency(Map<String, Object> params) {
		DateTime dt = DateTime.now();
		String startTime = (String) params.get("start_time");
		String endTime = (String)params.get("end_time");
		if(StringUtils.isEmpty(startTime)){
			startTime = dt.minusDays(31).toString("yyyy-MM-dd");
		}
		params.put("start_time", startTime.replace("-", ""));
		
		if(StringUtils.isEmpty(endTime)){
			endTime = dt.minusDays(1).toString("yyyy-MM-dd");
		}
		params.put("end_time", endTime.replace("-", ""));
		params.put("data_type", "1");
		//List<Map<String, Object>> dataList = masterDao.getSearchList("opsplatform.index.queryClientTendency", params);

		String id = (String) params.get("id");
		DataAuthorityCondition dataAuthorityCondition = AgentUtils.getDataAuthorityCondition(Long.valueOf(id), true, false);
		params.put("ids",dataAuthorityCondition.getIds());
		params.put("needQuerySaleIsNullData",dataAuthorityCondition.isNeedQuerySaleIsNullData());
		List<Map<String, Object>> dataList = masterDao.getSearchList("opsplatform.index.getClientTendency", params);
		supplement(dataList, startTime, endTime);
		return dataList;
	}

	/**
	 * 查询消费趋势
	 */
	@Override
	public List<Map<String, Object>> queryConsumeTendency(Map<String, String> params) {
		DateTime dt = DateTime.now();
		String startTime = params.get("start_time");
		String endTime = params.get("end_time");
		if(StringUtils.isEmpty(startTime)){
			startTime = dt.minusDays(31).toString("yyyy-MM-dd");
		}
		params.put("start_time", startTime.replace("-", ""));

		if(StringUtils.isEmpty(endTime)){
			endTime = dt.minusDays(1).toString("yyyy-MM-dd");
		}
		params.put("end_time", endTime.replace("-", ""));
		params.put("data_type", "1");
		List<Map<String, Object>> dataList = masterDao.getSearchList("opsplatform.index.queryConsumeTendency", params);
		supplement(dataList, startTime, endTime);
		return dataList;
	}

	/**
	 * @Description: 将时间段内缺少的数据以0补充
	 * @author: Niu.T 
	 * @date: 2017年1月10日    下午8:03:59
	 * @param dataList
	 * @param startTime
	 * @param endTime void
	 */
	private void supplement(List<Map<String, Object>> dataList,String startTime,String endTime){
		DateTime startDay = DateTime.parse(startTime);
		if(startDay.isAfterNow()||(startTime).equals(DateTime.now().toString("yyyy-MM-dd"))){
			return;
		}
		if(DateTime.parse(endTime).isAfterNow()||(endTime).equals(DateTime.now().toString("yyyy-MM-dd"))){
			endTime = DateTime.now().minusDays(1).toString("yyyy-MM-dd");
		}
		int count = 0;
		while (!endTime.equals(startDay.plusDays(count).toString("yyyy-MM-dd"))) {
			String dateStr = startDay.plusDays(count).toString("yyyyMMdd");
			Map<String,Object> dataTemp = new HashMap<>();
			dataTemp.put("increase_num", "0");
			dataTemp.put("data_time", dateStr);
			if(count >= dataList.size()){
				dataList.add(count, dataTemp);
				++count;
				continue;
			}
			Map<String,Object> temp = dataList.get(count);
			String object = temp.get("data_time").toString();
			if(!dateStr.equals(object)){
				dataList.add(count, dataTemp);
			}
			++count;
		}
		String dateStr = startDay.plusDays(count).toString("yyyyMMdd");
		Map<String,Object> dataTemp = new HashMap<>();
		dataTemp.put("increase_num", "0");
		dataTemp.put("data_time", dateStr);
		if(count >= dataList.size()){
			dataList.add(count, dataTemp);
		}
		
	}
	

}
