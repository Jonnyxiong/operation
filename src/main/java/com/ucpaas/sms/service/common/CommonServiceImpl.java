package com.ucpaas.sms.service.common;

import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.common.util.Collections3;
import com.ucpaas.sms.common.util.MD5;
import com.ucpaas.sms.constant.UserConstant;
import com.ucpaas.sms.dao.MessageMasterDao;
import com.ucpaas.sms.entity.UserSession;
import com.ucpaas.sms.entity.message.Role;
import com.ucpaas.sms.entity.message.User;
import com.ucpaas.sms.entity.po.UserPo;
import com.ucpaas.sms.mapper.message.DemandMapper;
import com.ucpaas.sms.mapper.message.ResourceMapper;
import com.ucpaas.sms.mapper.message.UserMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 公共业务
 * 
 * @author xiejiaan
 */
@Service
@Transactional
public class CommonServiceImpl implements CommonService {
	private static final Logger logger = LoggerFactory.getLogger(CommonServiceImpl.class);
	@Autowired
	private MessageMasterDao masterDao;

	@Autowired
	private ResourceMapper resourceMapper;

	@Autowired
	private DemandMapper demandMapper;

	@Autowired
	private UserMapper userMapper;

	// @Autowired
	// private CommonDao commonDao;

	@Override
	public ResultVO login(String username, String password) {
		ResultVO resultVO = new ResultVO();
		// if (AuthorityUtils.isLogin()) {// 已登录
		// data.put("result", "isLogin");
		// data.put("msg", "当前已登录，请勿重复登录");
		// return data;
		// }

		if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
			resultVO.setSuccess(false);
			resultVO.setMsg("用户名或密码不能为空");
			return resultVO;
		}

		User queryUser = new User();
		queryUser.setUsername(username);
		queryUser.setPassword(MD5.md5(password));
		UserPo userPo = userMapper.getUserForLogin(queryUser);
		if (userPo == null) {
			resultVO.setSuccess(false);
			resultVO.setMsg("用户名或密码错误，请重新输入");
			return resultVO;
		}

		if (Integer.parseInt(userPo.getStatus()) != UserConstant.USER_STATUS_1) {
			resultVO.setSuccess(false);
			resultVO.setMsg("用户已禁用");
			return resultVO;
		}

		// 查询用户的角色
		List<Role> roles = userMapper.findRoleInfoByUserId(userPo.getId());
		if (Collections3.isEmpty(roles)) {
			resultVO.setSuccess(false);
			resultVO.setMsg("用户未被授权"); // 实际是没有角色
			return resultVO;
		}

		boolean hasRole = false;
		for (Role role : roles) {
			if (Integer.parseInt(role.getStatus()) == UserConstant.ROLE_STATUS_1) {
				hasRole = true;
				break;
			}
		}

		if (!hasRole) {
			resultVO.setSuccess(false);
			resultVO.setMsg("用户未被授权"); // 实际是没有角色
		}

		// 更新登录信息
		userMapper.updateLoginInfo(userPo.getId());

		userPo.setRoles(roles);
		userPo.setRoleId(roles.get(0).getId());
		userPo.setRoleName(roles.get(0).getRoleName());

		resultVO.setSuccess(true);
		resultVO.setMsg("登录成功");
		resultVO.setData(userPo);
		return resultVO;
	}

	/**
	 * 根据用户ID获取用户状态
	 */
	@Override
	public Map<String, Object> getUserStatusById(long userId) {
		return masterDao.getOneInfo("common.getUserStatusById", userId);
	}

	/**
	 * 获取当月代理商账号最大值
	 */
	@Override
	public String getMostAgentNumForMonth(String agentIdPre) {
		Map<String, Object> params = new HashMap<>();
		params.put("agentIdPre", agentIdPre);
		String num = this.masterDao.getOneInfo("common.getMostAgentNumForMonth", params);
		return num;
	}

	@Override
	public String getMostDemandOrderNumForDay(String orderIdSuf) {
		Map<String, Object> params = new HashMap<>();
		params.put("orderIdSuf", orderIdSuf);
		String num = this.masterDao.getOneInfo("common.getMostDemandOrderNumForDay", params);
		return num;
	}

	// @Override
	// public boolean hasTable(String table_name, String table_schema) {
	// Map<String, String> params = new HashMap<String, String>();
	// params.put("table_name", table_name);
	// params.put("table_schema", table_schema);
	// Map<String, Object> tmp = masterDao.getOneInfo("common.countTableByName",
	// params);
	// Integer total = Integer.valueOf(String.valueOf(tmp.get("total")));
	// if (total > 0) {
	// return true;
	// }
	// return false;
	// }

	// @Override
	// public String queryRemotePath(String table, String localPath) {
	// Map<String, String> params = new HashMap<String, String>();
	// params.put("table_name", table);
	// params.put("local_path", localPath);
	// Map<String, Object> tmp = masterDao.getOneInfo("common.queryRemontePath",
	// params);
	// return null == tmp ? null : String.valueOf(tmp.get("remote_path"));
	// }

	// @Override
	// public Map<String, Object> monitor() {
	// Map<String, Object> data = new HashMap<String, Object>();
	// String time = masterDao.getOneInfo("common.getCurrentTime", null);
	// if (time != null) {
	// data.put("monitor", "success");
	// data.put("time", time);
	// }
	// return data;
	// }

	// @Override
	// public List<String> getExistTable(DbType dbType, TablePrefix tablePrefix,
	// String startDate, String endDate) {
	// List<String> data = new ArrayList<String>();
	// String tablePrefixStr = tablePrefix.name();
	//
	// if (StringUtils.isBlank(tablePrefixStr) || StringUtils.isBlank(startDate)
	// || StringUtils.isBlank(endDate)) {
	// return data;
	// }
	// DateTime d1 = null;
	// DateTime d2 = null;
	// try {
	// d1 = new DateTime(DateUtils.parseDateStrictly(startDate.substring(0, 10),
	// "yyyy-MM-dd"));
	// d2 = new DateTime(DateUtils.parseDateStrictly(endDate.substring(0, 10),
	// "yyyy-MM-dd"));
	// } catch (Throwable e) {
	// logger.error("时间转换【失败】：startDate=" + startDate + ", endDate=" + endDate,
	// e);
	// return data;
	// }
	// if (d1.isAfter(d2)) {
	// return data;
	// }
	// int between = Days.daysBetween(d1, d2).getDays();
	// List<String> tableList = new ArrayList<String>();
	// for (int i = 0; i <= between; i++) {
	// tableList.add(tablePrefixStr + d1.plusDays(i).toString("yyyyMMdd"));
	// }
	//
	// List<Map<String, Object>> existList =
	// commonDao.getDao(dbType).getSearchList("common.getExistTable",
	// tableList);// 查询在日期范围内存在的语音表、短信表、即时消息表
	// for (Map<String, Object> map : existList) {
	// data.add(map.get("table_name").toString());
	// }
	// return data;
	// }

	// @Override
	// public List<String> getExistTable(DbType dbType, TablePrefix tablePrefix,
	// String startDate, String endDate,
	// String identify) {
	//
	// List<String> data = new ArrayList<String>();
	// String tablePrefixStr = tablePrefix.name();
	//
	// if (StringUtils.isBlank(tablePrefixStr) || StringUtils.isBlank(startDate)
	// || StringUtils.isBlank(endDate)) {
	// return data;
	// }
	// DateTime d1 = null;
	// DateTime d2 = null;
	// try {
	// d1 = new DateTime(DateUtils.parseDateStrictly(startDate.substring(0, 10),
	// "yyyy-MM-dd"));
	// d2 = new DateTime(DateUtils.parseDateStrictly(endDate.substring(0, 10),
	// "yyyy-MM-dd"));
	// } catch (Throwable e) {
	// logger.error("时间转换【失败】：startDate=" + startDate + ", endDate=" + endDate,
	// e);
	// return data;
	// }
	// if (d1.isAfter(d2)) {
	// return data;
	// }
	// int between = Days.daysBetween(d1, d2).getDays();
	// List<String> tableList = new ArrayList<String>();
	// for (int i = 0; i <= between; i++) {
	// tableList.add(tablePrefixStr +identify+"_"+
	// d1.plusDays(i).toString("yyyyMMdd"));
	// }
	//
	// List<Map<String, Object>> existList =
	// commonDao.getDao(dbType).getSearchList("common.getExistTable",
	// tableList);// 查询在日期范围内存在的语音表、短信表、即时消息表
	// for (Map<String, Object> map : existList) {
	// data.add(map.get("table_name").toString());
	// }
	// return data;
	//
	// }

	// @Override
	// public List<SplitTableTime> getSplitTableTime(String start_time, String
	// end_time) throws Exception {
	//
	//
	// List<SplitTableTime> splitTableTimeList = new ArrayList<>();
	//
	// String online_time = ConfigUtils.online_time;
	//
	// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	// DateTime dt_start_time = new DateTime(sdf.parse(start_time));
	// DateTime dt_end_time = new DateTime(sdf.parse(end_time));
	// DateTime dt_online_time = new DateTime(sdf.parse(online_time));
	//
	// if(dt_end_time.isBefore(dt_online_time) ||
	// dt_end_time.isEqual(dt_online_time)){
	// //时间都在上线之前（使用老数据）
	// SplitTableTime st = new SplitTableTime();
	// st.setStart_time(start_time);
	// st.setEnd_time(end_time);
	// st.setFlag("old");
	// splitTableTimeList.add(st);
	//
	//
	// }else if(dt_start_time.isAfter(dt_online_time) ||
	// dt_start_time.isEqual(dt_online_time)){
	// //时间都在上线之后（使用新数据）
	// SplitTableTime st = new SplitTableTime();
	// st.setStart_time(start_time);
	// st.setEnd_time(end_time);
	// st.setFlag("new");
	// splitTableTimeList.add(st);
	// }else{
	// //开始时间在上线之前，结束时间在上线之后（使用老数据和新数据）
	//
	// SplitTableTime st_old = new SplitTableTime();
	// st_old.setStart_time(start_time);
	// st_old.setEnd_time(online_time);
	// st_old.setFlag("old");
	// splitTableTimeList.add(st_old);
	//
	// SplitTableTime st_new = new SplitTableTime();
	// st_new.setStart_time(online_time);
	// st_new.setEnd_time(end_time);
	// st_new.setFlag("new");
	// splitTableTimeList.add(st_new);
	// }
	//
	// return splitTableTimeList;
	// }

	// @Override
	// public List<String> getExistTableForAccess(DbType dbType, TablePrefix
	// tablePrefix, String startDate, String endDate,
	// String clientid) {
	//
	// List<String> data = new ArrayList<String>();
	//
	// Map<String,Object> sqlParams = new HashMap<>();
	// sqlParams.put("clientid", clientid);
	// Map<String,Object> identifyMap =
	// masterDao.getOneInfo("common.getIdentifyByClientid", sqlParams);
	// if(identifyMap == null){
	// return data;
	// }
	// int identify = (int) identifyMap.get("identify");
	//
	// String tablePrefixStr = tablePrefix.name();
	//
	// if (StringUtils.isBlank(tablePrefixStr) || StringUtils.isBlank(startDate)
	// || StringUtils.isBlank(endDate)) {
	// return data;
	// }
	// DateTime d1 = null;
	// DateTime d2 = null;
	// try {
	// d1 = new DateTime(DateUtils.parseDateStrictly(startDate.substring(0, 10),
	// "yyyy-MM-dd"));
	// d2 = new DateTime(DateUtils.parseDateStrictly(endDate.substring(0, 10),
	// "yyyy-MM-dd"));
	// } catch (Throwable e) {
	// logger.error("时间转换【失败】：startDate=" + startDate + ", endDate=" + endDate,
	// e);
	// return data;
	// }
	// if (d1.isAfter(d2)) {
	// return data;
	// }
	// int between = Days.daysBetween(d1, d2).getDays();
	// List<String> tableList = new ArrayList<String>();
	// for (int i = 0; i <= between; i++) {
	// tableList.add(tablePrefixStr +identify+"_"+
	// d1.plusDays(i).toString("yyyyMMdd"));
	// }
	//
	// List<Map<String, Object>> existList =
	// commonDao.getDao(dbType).getSearchList("common.getExistTable",
	// tableList);// 查询在日期范围内存在的语音表、短信表、即时消息表
	// for (Map<String, Object> map : existList) {
	// data.add(map.get("table_name").toString());
	// }
	// return data;
	//
	// }

	// @Override
	// public List<String> getExistTableForAccess(DbType dbType, TablePrefix
	// tablePrefix, String startDate,
	// String endDate) {
	//
	// List<String> data = new ArrayList<String>();
	//
	// //获取record流水分表的最大值
	// Map<String,Object> sqlParams = new HashMap<>();
	// sqlParams.put("param_key", "MAX_ACCESS_IDENTIFY");
	// Map<String,Object> paramMap = masterDao.getOneInfo("paramConf.viewByKey",
	// sqlParams);
	// Integer maxIdentify =
	// Integer.valueOf(paramMap.get("param_value").toString());
	//
	// String tablePrefixStr = tablePrefix.name();
	// if (StringUtils.isBlank(tablePrefixStr) || StringUtils.isBlank(startDate)
	// || StringUtils.isBlank(endDate)) {
	// return data;
	// }
	// DateTime d1 = null;
	// DateTime d2 = null;
	// try {
	// d1 = new DateTime(DateUtils.parseDateStrictly(startDate.substring(0, 10),
	// "yyyy-MM-dd"));
	// d2 = new DateTime(DateUtils.parseDateStrictly(endDate.substring(0, 10),
	// "yyyy-MM-dd"));
	// } catch (Throwable e) {
	// logger.error("时间转换【失败】：startDate=" + startDate + ", endDate=" + endDate,
	// e);
	// return data;
	// }
	// if (d1.isAfter(d2)) {
	// return data;
	// }
	// int between = Days.daysBetween(d1, d2).getDays();
	// List<String> tableList = new ArrayList<String>();
	// for (int i = 0; i <= between; i++) {
	// for(int j=0; j<=maxIdentify;j++ ){
	// tableList.add(tablePrefixStr +j+"_"+
	// d1.plusDays(i).toString("yyyyMMdd"));
	// }
	// }
	//
	// List<Map<String, Object>> existList =
	// commonDao.getDao(dbType).getSearchList("common.getExistTable",
	// tableList);// 查询在日期范围内存在的语音表、短信表、即时消息表
	// for (Map<String, Object> map : existList) {
	// data.add(map.get("table_name").toString());
	// }
	// return data;
	//
	// }

	// @Override
	// public List<String> getExistTableForRecord(DbType dbType, TablePrefix
	// tablePrefix, String startDate, String endDate,
	// String cid) {
	//
	// List<String> data = new ArrayList<String>();
	//
	// Map<String,Object> sqlParams = new HashMap<>();
	// sqlParams.put("cid", cid);
	// Map<String,Object> identifyMap =
	// masterDao.getOneInfo("common.getIdentifyByCid", sqlParams);
	// if(identifyMap == null){
	// return data;
	// }
	// int identify = (int) identifyMap.get("identify");
	//
	// String tablePrefixStr = tablePrefix.name();
	// if (StringUtils.isBlank(tablePrefixStr) || StringUtils.isBlank(startDate)
	// || StringUtils.isBlank(endDate)) {
	// return data;
	// }
	//
	// DateTime d1 = null;
	// DateTime d2 = null;
	// try {
	// d1 = new DateTime(DateUtils.parseDateStrictly(startDate.substring(0, 10),
	// "yyyy-MM-dd"));
	// d2 = new DateTime(DateUtils.parseDateStrictly(endDate.substring(0, 10),
	// "yyyy-MM-dd"));
	// } catch (Throwable e) {
	// logger.error("时间转换【失败】：startDate=" + startDate + ", endDate=" + endDate,
	// e);
	// return data;
	// }
	// if (d1.isAfter(d2)) {
	// return data;
	// }
	// int between = Days.daysBetween(d1, d2).getDays();
	// List<String> tableList = new ArrayList<String>();
	// for (int i = 0; i <= between; i++) {
	// tableList.add(tablePrefixStr +identify+"_"+
	// d1.plusDays(i).toString("yyyyMMdd"));
	// }
	//
	// List<Map<String, Object>> existList =
	// commonDao.getDao(dbType).getSearchList("common.getExistTable",
	// tableList);// 查询在日期范围内存在的语音表、短信表、即时消息表
	// for (Map<String, Object> map : existList) {
	// data.add(map.get("table_name").toString());
	// }
	//
	// return data;
	// }

	// @Override
	// public List<String> getExistTableForRecord(DbType dbType, TablePrefix
	// tablePrefix, String startDate,
	// String endDate) {
	//
	// List<String> data = new ArrayList<String>();
	//
	// //获取record流水分表的最大值
	// Map<String,Object> sqlParams = new HashMap<>();
	// sqlParams.put("param_key", "MAX_RECORD_IDENTIFY");
	// Map<String,Object> paramMap = masterDao.getOneInfo("paramConf.viewByKey",
	// sqlParams);
	// Integer maxIdentify =
	// Integer.valueOf(paramMap.get("param_value").toString());
	//
	// String tablePrefixStr = tablePrefix.name();
	// if (StringUtils.isBlank(tablePrefixStr) || StringUtils.isBlank(startDate)
	// || StringUtils.isBlank(endDate)) {
	// return data;
	// }
	//
	// DateTime d1 = null;
	// DateTime d2 = null;
	// try {
	// d1 = new DateTime(DateUtils.parseDateStrictly(startDate.substring(0, 10),
	// "yyyy-MM-dd"));
	// d2 = new DateTime(DateUtils.parseDateStrictly(endDate.substring(0, 10),
	// "yyyy-MM-dd"));
	// } catch (Throwable e) {
	// logger.error("时间转换【失败】：startDate=" + startDate + ", endDate=" + endDate,
	// e);
	// return data;
	// }
	// if (d1.isAfter(d2)) {
	// return data;
	// }
	// int between = Days.daysBetween(d1, d2).getDays();
	// List<String> tableList = new ArrayList<String>();
	// for (int i = 0; i <= between; i++) {
	// for(int j = 0; j <= maxIdentify; j++){
	// tableList.add(tablePrefixStr +j+"_"+
	// d1.plusDays(i).toString("yyyyMMdd"));
	// }
	// }
	//
	// List<Map<String, Object>> existList =
	// commonDao.getDao(dbType).getSearchList("common.getExistTable",
	// tableList);// 查询在日期范围内存在的语音表、短信表、即时消息表
	// for (Map<String, Object> map : existList) {
	// data.add(map.get("table_name").toString());
	// }
	//
	// return data;
	// }

	// @Override
	// public List<String> getExistTableForSplitTable(DbType dbType, TablePrefix
	// tablePrefix, String startDate,
	// String endDate, String clientid) throws Exception {
	//
	// List<String> data = new ArrayList<String>();
	//
	// String tablePrefixStr = tablePrefix.name();
	// if (StringUtils.isBlank(tablePrefixStr) || StringUtils.isBlank(startDate)
	// || StringUtils.isBlank(endDate)) {
	// return data;
	// }
	//
	// Map<String,Object> sqlParams = new HashMap<>();
	// sqlParams.put("clientid", clientid);
	// sqlParams.put("startDate", startDate);
	// sqlParams.put("endDate", endDate);
	//
	// List<String> tableNameList = new ArrayList<>();
	//
	// List<SmsClientIdentifyLog> SmsClientIdentifyLogList =
	// commonDao.getMessageMasterDao().selectList("common.getSmsClientIdentifyLogForAll",
	// sqlParams);
	// if(SmsClientIdentifyLogList == null || SmsClientIdentifyLogList.size() ==
	// 0){
	// return data;
	// }
	// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	//
	// DateTime begin = new DateTime(sdf.parse(startDate));
	// DateTime end = new DateTime(sdf.parse(endDate));
	// Period p = new Period(begin, end, PeriodType.days());
	// int days = p.getDays();
	//
	// for(int i=0; i<=days; i++){
	// DateTime dataTimeTemp1 = begin.plusDays(i);
	// String tableName = null;
	//
	// for(int j=0; j<SmsClientIdentifyLogList.size();j++){
	// SmsClientIdentifyLog po = SmsClientIdentifyLogList.get(j);
	// DateTime dataTimeTemp2 = new DateTime(po.getDate());
	// String formatDateStr = dataTimeTemp1.toString("yyyyMMdd");
	// if(dataTimeTemp1.isEqual(dataTimeTemp2) ||
	// dataTimeTemp1.isAfter(dataTimeTemp2)){
	// tableName = tablePrefixStr + po.getIdentify()+"_"+formatDateStr;
	// }else{
	// if(tableName != null){
	// tableNameList.add(tableName);
	// break;
	// }
	// }
	//
	// if(j == SmsClientIdentifyLogList.size()-1){
	// if(tableName != null){
	// tableNameList.add(tableName);
	// break;
	// }
	// }
	// }
	// }
	//
	// if(tableNameList.size() == 0){
	// return data;
	// }
	//
	// List<Map<String, Object>> existList =
	// commonDao.getDao(dbType).getSearchList("common.getExistTable",
	// tableNameList);// 查询在日期范围内存在的语音表、短信表、即时消息表
	// for (Map<String, Object> map : existList) {
	// data.add(map.get("table_name").toString());
	// }
	// return data;
	// }

	// @Override
	// public Map<String, Object> getBackUpChannelNum(){
	// return masterDao.getOneInfo("common.getBackUpChannelNum", null);
	// }
	//
	// @Override
	// public List<Map<String, Object>> getChannelByOperator(Map<String, String>
	// params){
	// return masterDao.getSearchList("common.getChannelByOperator", params);
	// }
	/**
	 * 获取系统参数
	 */
	@Override
	public Map<String, Object> getSysParams(String paramKey) {
		return masterDao.getOneInfo("common.getSysParams", paramKey);
	}

	@Override
	public int getMaxResourceIdSeq() {
		return resourceMapper.getMaxIdSeq();
	}

	@Override
	public int getMaxDemandIdSeq() {
		return demandMapper.getMaxIdSeq();
	}

	@Override
	public List<UserSession> getAllBusinessMan() {
		return masterDao.selectList("common.getAllBusinessMan", null);
	}

	@Override
	public String getMaxInvoiceIdSeq(String idPre) {
		Map<String,Object> sqlParams = new HashMap<>();
		sqlParams.put("idPre", idPre);
		String numStr = masterDao.getOneInfo("common.getMaxInvoiceIdSeq", sqlParams);
		return numStr;
	}
}
