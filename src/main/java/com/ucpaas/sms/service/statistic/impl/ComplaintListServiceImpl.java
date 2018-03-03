package com.ucpaas.sms.service.statistic.impl;

import cn.afterturn.easypoi.excel.ExcelImportUtil;
import cn.afterturn.easypoi.excel.entity.ImportParams;
import com.jsmsframework.channel.entity.JsmsChannel;
import com.jsmsframework.channel.entity.JsmsComplaintList;
import com.jsmsframework.channel.entity.JsmsComplaintListExt;
import com.jsmsframework.channel.exception.JsmsComplaintListException;
import com.jsmsframework.channel.service.JsmsChannelService;
import com.jsmsframework.channel.service.JsmsComplaintListService;
import com.jsmsframework.common.constant.SysConstant;
import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.JxlExcel;
import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.enums.OperatorType;
import com.jsmsframework.common.enums.SmsTypeEnum;
import com.jsmsframework.common.util.BeanUtil;
import com.jsmsframework.common.util.FileUtils;
import com.jsmsframework.common.util.JsonUtil;
import com.jsmsframework.common.util.JxlExcelUtils;
import com.jsmsframework.record.dto.RecordDTO;
import com.jsmsframework.record.record.entity.JsmsRecord;
import com.jsmsframework.record.service.JsmsRecordService;
import com.jsmsframework.statistics.service.JsmsSignComplaintService;
import com.jsmsframework.user.entity.JsmsAccount;
import com.jsmsframework.user.entity.JsmsUser;
import com.jsmsframework.user.service.JsmsAccountService;
import com.jsmsframework.user.service.JsmsUserService;
import com.ucpaas.sms.common.util.Collections3;
import com.ucpaas.sms.common.util.DateUtils;
import com.ucpaas.sms.entity.po.DataAuthorityCondition;
import com.ucpaas.sms.service.statistic.ComplaintListService;
import com.ucpaas.sms.util.AgentUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * created by xiaoqingwen on 2018/1/9 18:17
 * 投诉管理分析统计
 */
@Service
public class ComplaintListServiceImpl implements ComplaintListService {

    private static Logger logger = LoggerFactory.getLogger(ComplaintListServiceImpl.class);
    @Autowired
    private JsmsAccountService jsmsAccountService;
    @Autowired
    private JsmsUserService jsmsUserService;
    @Autowired
    private JsmsChannelService jsmsChannelService;
    @Autowired
    private JsmsComplaintListService jsmsComplaintListService;
    @Autowired
    private JsmsRecordService jsmsRecordService;
    @Autowired
    private JsmsSignComplaintService jsmsSignComplaintService;

    /**
     * 获取全部客户
     */
    @Override
    public JsmsPage queryListForAccount(JsmsPage jsmsPage) {
        JsmsPage jsmsp = jsmsAccountService.queryList(jsmsPage);
        if(jsmsp!=null){
            List data = jsmsp.getData();
            if(data!=null && data.size()>0){
                for (int i=0;i<data.size();i++){
                    JsmsAccount jsmsAccount = (JsmsAccount) data.get(i);
                    jsmsAccount.setId(jsmsAccount.getClientid());
                }
            }
        }

        //List<JsmsAccount> list = jsmsAccountService.queryAll(map);
        return jsmsp;
    }

    /**
     * 获取归属销售
     */
    @Override
    public JsmsPage queryListForUser(Long id,String condition,JsmsPage jsmsPage) {
        //return jsmsUserService.queryAll();
        DataAuthorityCondition dataAuthorityCondition = AgentUtils.getDataAuthorityCondition(Long.valueOf(id), true, false);
        List<Long> ids = dataAuthorityCondition.getIds();
        List<JsmsUser> list = null;
        Map<String,Object> params=new HashMap<>();
        params.put("condition",condition);
        if (ids != null && ids.size() > 0) {
            Set<Long> idList = new HashSet<>();
            for (int i = 0; i < ids.size(); i++) {
                idList.add(ids.get(i));
            }
            logger.debug("此用户名下的归属销售有-->{}", idList);
            params.put("ids",idList);
            jsmsPage.setParams(params);
            //list = jsmsUserService.getByIds(idList);
            return jsmsUserService.queryList(jsmsPage);
        } else {
            logger.debug("此用户未查找到归属销售,用户id为-->{}", id);
        }
        return jsmsPage;
    }

    /**
     * 获取投诉通道
     *
     * @return
     */
    @Override
    public JsmsPage queryListForChannel(JsmsPage jsmsPage) {

        //return jsmsChannelService.queryAll();
        JsmsPage jsmsp = jsmsChannelService.queryList(jsmsPage);
        if(jsmsp!=null){
            List data = jsmsp.getData();
            if(data!=null && data.size()>0){
                for (int i=0;i<data.size();i++){
                    JsmsChannel jsmsChannel = (JsmsChannel) data.get(i);
                    jsmsChannel.setId(jsmsChannel.getCid());
                }
            }
        }
        return jsmsp;
    }

    /**
     * 搜索投诉,分页
     *
     * @param page
     * @return
     */
    @Override
    public JsmsPage<JsmsComplaintListExt> queryList(JsmsPage page) {
        //return jsmsComplaintListService.searchComplaint(page);
        List<JsmsComplaintListExt> ll = new ArrayList<>();
        Map params = page.getParams();
        String realname = (String) params.get("realname");
        String operatorStr = (String) params.get("operatorStr");
        //根据录入者框的数据查询其对应的用户id
        Set<Long> userIds = new HashSet<>();
        if(StringUtils.isNotBlank(operatorStr)){
            List<JsmsUser> jsmsUsersList = jsmsUserService.getByRealname(operatorStr);
            if(jsmsUsersList!=null  && jsmsUsersList.size()>0){
                for (int i=0;i<jsmsUsersList.size();i++){
                    userIds.add(jsmsUsersList.get(i).getId());
                }
            }else{
                //说明此录入者不存在
                userIds.add(-1l);
            }
            params.put("userIds",userIds);
        }else {
            //录入者输入框无值
            params.put("userIds",null);
        }
        page.setParams(params);
        //投诉搜索的求总条数
        int totalCount = jsmsComplaintListService.countComplaint(params);
        //再分页通道
        JsmsPage<JsmsComplaintList> wait = jsmsComplaintListService.queryListExt(page);
        //JsmsPage<JsmsComplaintList> wait = jsmsComplaintListService.queryList(page);
        //每页显示条数
        int pageRowCount = page.getRows();
        //总页数
        Integer totalPage = totalCount / pageRowCount + (totalCount % pageRowCount == 0 ? 0 : 1);
        List<JsmsComplaintList> list = wait.getData();
        if (Collections3.isEmpty(list)) {
            JsmsPage<JsmsComplaintListExt> result = new JsmsPage<>();
            com.ucpaas.sms.util.beans.BeanUtil.copyProperties(wait, result);
            return result;
        }
        JsmsPage<JsmsComplaintListExt> result = new JsmsPage<>();
        com.ucpaas.sms.util.beans.BeanUtil.copyProperties(wait, result);
        List<JsmsChannel> channels = getJsmsChannels(list);
        //归属销售
        List<JsmsUser> users = getJsmsUsers(list);
        //录入者
        List<JsmsUser> operators = getOperator(list, operatorStr);

        List<JsmsAccount> accounts = getJsmsAccounts(list);
        List<JsmsComplaintListExt> pos = buildPoList(list, channels, users,operators, accounts,operatorStr);
        //组装返回的数据
        dealWith(ll, pos);
        result.setData(ll);
        result.setTotalPage(totalPage);
        result.setTotalRecord(totalCount);
        result.setPage(page.getPage());
        result.setRows(page.getRows());
        return result;
    }

    /**
     * 根据id删除投诉
     *
     * @param id
     * @return
     */
    @Override
    @Transactional("message")
    public R deleteById(Integer id, Long userId) {
        //int i = jsmsComplaintListService.deleteById(id);
        //假删除,把状态置成1--->(状态，0：正常，1：删除)
        JsmsComplaintList jsmsComplaintList = jsmsComplaintListService.getById(id);
        if (jsmsComplaintList == null) {
            logger.debug("不存在此投诉及此id--->{}", id);
            return R.error("删除失败,此投诉不存在!");
        }
        jsmsComplaintList.setStatus(1);
        jsmsComplaintList.setUpdateTime(new Date());
        int i = jsmsComplaintListService.updateSelective(jsmsComplaintList);
        if (i != 1) {
            throw new JsmsComplaintListException("用户id为" + userId + "删除投诉id为" + id + "失败!");
        }
        logger.debug("用户id为 {} 删除投诉id为 {}成功......", userId, id);
        return R.ok("删除成功!");
    }

    /**
     * 批量添加投诉
     *
     * @return
     */
    @Transactional("message")
    @Override
    public R addComplaintBatch(String adminId, String tempFileSavePath) {
        R r = new R();
        String fileAbsPath = tempFileSavePath;
        Map<String, Object> data = new HashMap<>();
        // 获得Excel文件中的数据
        boolean isFilterBlankRow = true;
        List<Map<String,Object>> excelDataList = new ArrayList<>();
        try {
            ImportParams importParams = new ImportParams();
            importParams.setHeadRows(0);
            excelDataList = ExcelImportUtil.importExcel(new File(fileAbsPath), Map.class, importParams);
            logger.debug("号码Excel 读取完成 , 删除文件 ----------> {}", fileAbsPath);
            FileUtils.delete(fileAbsPath);
        } catch (Exception e) {
            logger.debug("号码Excel 读取失败 , 删除文件 ----------> {}", fileAbsPath);
            FileUtils.delete(fileAbsPath);
            logger.error("解析excel失败：filePath=" + fileAbsPath, e);
            return r.error("导入文件格式错误，目前只支持Excel导入，请使用模板");
        }

//        try {
//            excelDataList = JxlExcelUtils.importExcel(fileAbsPath, isFilterBlankRow);
//        } catch (Exception e) {
//            logger.error("导入Excel时发生错误，e = {}", e);
//            return r.error("导入文件格式错误，目前支持.xlsx格式(97-2003)，请使用模板");
//        }
        FileUtils.delete(fileAbsPath);

        if (excelDataList.size() > 1000) {
            return r.error("您选择的excel中数据记录大于1000条，请您拆分后上传");
        }

        List<Map<String, Object>> illegalDataList = new ArrayList<>(); // Excel中非法数据
        List<Map<String, Object>> legalDataList = new ArrayList<>(); // Excel中合法数据
        R validateR = null;
        if (excelDataList != null && excelDataList.size() > 1) {
            //因为excelDataList接受回来的数据是map类型,需要转化成list类型
            List<List<String>> excelDataLists=new ArrayList<>();
            for (int i=0;i<excelDataList.size();i++){
                List<String> ll = new ArrayList<>();
                Map<String,Object> map = excelDataList.get(i);
                /*ll.add((String)map.get("column_0"));
                ll.add((String)map.get("column_1"));
                ll.add((String)map.get("column_2"));
                ll.add((String)map.get("column_3"));*/
                ll.add(String.valueOf(map.get("column_0")));
                ll.add(String.valueOf(map.get("column_1")));
                ll.add(String.valueOf(map.get("column_2")));
                ll.add(String.valueOf(map.get("column_3")));
                excelDataLists.add(ll);
                logger.debug("从map中提取的前四列数据为短信id{}  通道号{}  手机号码{}  处理时间{}",map.get("column_0"),map.get("column_1"),map.get("column_2"),map.get("column_3"));
            }

            //组装数据
            validateR = validataComplaint(adminId, excelDataLists, illegalDataList, legalDataList);
        } else {
            return r.error("excel中没有数据");
        }
        if (validateR.getCode().equals(500)) {
            return validateR;
        }

        //将导入的失败数据记录到excel中
        generateImportResultExcel(illegalDataList, adminId, tempFileSavePath);
        //设置返回值
        data.put("importTotal", excelDataList.size() - 1);
        data.put("importSuccess", excelDataList.size() - 1 - illegalDataList.size());
        data.put("importFall", illegalDataList.size());
        r.setData(data);
        r.ok("导入完成，");
        return r;
    }

    /**
     * 导出的搜索
     *
     * @param params
     * @return
     */
    @Override
    public List<JsmsComplaintListExt> findList(Map<String, Object> params) {

        List<JsmsComplaintListExt> ll = new ArrayList<>();
        String realname = (String) params.get("realname");
        String operatorStr = (String) params.get("operatorStr");

        //根据录入者框的数据查询其对应的用户id
        Set<Long> userIds = new HashSet<>();
        if(StringUtils.isNotBlank(operatorStr)){
            List<JsmsUser> jsmsUsersList = jsmsUserService.getByRealname(operatorStr);
            if(jsmsUsersList!=null  && jsmsUsersList.size()>0){
                for (int i=0;i<jsmsUsersList.size();i++){
                    userIds.add(jsmsUsersList.get(i).getId());
                }
            }else{
                //说明此录入者不存在
                userIds.add(-1l);
            }
            params.put("userIds",userIds);
        }else {
            //录入者输入框无值
            params.put("userIds",null);
        }
        List<JsmsComplaintList> list = jsmsComplaintListService.findList(params);
        if (Collections3.isEmpty(list)) {
            return new ArrayList<>();
        }
        List<JsmsChannel> channels = getJsmsChannels(list);
        //归属销售
        List<JsmsUser> users = getJsmsUsers(list);
        //录入者
        List<JsmsUser> operators = getOperator(list, operatorStr);

        List<JsmsAccount> accounts = getJsmsAccounts(list);
        List<JsmsComplaintListExt> pos = buildPoList(list, channels, users,operators, accounts,operatorStr);
        //组装返回的数据
        dealWith(ll, pos);
        return ll;
    }

    private void dealWith(List<JsmsComplaintListExt> ll, List<JsmsComplaintListExt> pos) {
        //组装返回数据
        if (pos != null && pos.size() > 0) {
            for (int i = 0; i < pos.size(); i++) {
                JsmsComplaintListExt complaintListExt = pos.get(i);
                //处理发送时间
                if (complaintListExt.getSendTime() != null) {
                    String sendTimeStr = DateUtils.formatDate(complaintListExt.getSendTime(), "yyyy-MM-dd");
                    complaintListExt.setSendTimeStr(sendTimeStr);
                }
                //处理创建时间
                if (complaintListExt.getCreateTime() != null) {
                    String createTimeStr = DateUtils.formatDate(complaintListExt.getCreateTime(), "yyyy-MM-dd HH:mm:ss");
                    complaintListExt.setCreateTimeStr(createTimeStr);
                }
                //处理短信类型(0：通知短信，4：验证码短信，5：营销短信，6：告警短信，7：USSD，8：闪信,投诉记录类型type为通道投诉的时候可为空)
                Integer smstype = complaintListExt.getSmstype();
                if (smstype.equals(SmsTypeEnum.通知.getValue())) {
                    complaintListExt.setSmsTypeStr(SmsTypeEnum.通知.getDesc());
                } else if (smstype.equals(SmsTypeEnum.验证码.getValue())) {
                    complaintListExt.setSmsTypeStr(SmsTypeEnum.验证码.getDesc());
                } else if (smstype.equals(SmsTypeEnum.营销.getValue())) {
                    complaintListExt.setSmsTypeStr(SmsTypeEnum.营销.getDesc());
                } else if (smstype.equals(SmsTypeEnum.告警.getValue())) {
                    complaintListExt.setSmsTypeStr(SmsTypeEnum.告警.getDesc());
                } else if (smstype.equals(SmsTypeEnum.USSD.getValue())) {
                    complaintListExt.setSmsTypeStr(SmsTypeEnum.USSD.getDesc());
                } else if (smstype.equals(SmsTypeEnum.闪信.getValue())) {
                    complaintListExt.setSmsTypeStr(SmsTypeEnum.闪信.getDesc());
                }
                //处理运营商类型(0：全网1：移动2：联通3：电信4：国际,投诉记录类型type为通道投诉的时候可为空)
                Integer operatorstype = complaintListExt.getOperatorstype();
                if (operatorstype.equals(OperatorType.全网.getValue())) {
                    complaintListExt.setOperatorstypeStr(OperatorType.全网.getDesc());
                } else if (operatorstype.equals(OperatorType.移动.getValue())) {
                    complaintListExt.setOperatorstypeStr(OperatorType.移动.getDesc());
                } else if (operatorstype.equals(OperatorType.联通.getValue())) {
                    complaintListExt.setOperatorstypeStr(OperatorType.联通.getDesc());
                } else if (operatorstype.equals(OperatorType.电信.getValue())) {
                    complaintListExt.setOperatorstypeStr(OperatorType.电信.getDesc());
                } else if (operatorstype.equals(OperatorType.国际.getValue())) {
                    complaintListExt.setOperatorstypeStr(OperatorType.国际.getDesc());
                }
                ll.add(complaintListExt);
            }
        }
    }

    private List<JsmsAccount> getJsmsAccounts(List<JsmsComplaintList> list) {
        List<JsmsAccount> accounts = null;
        Set<String> clientIds = new HashSet<>();
        for (JsmsComplaintList jsmsComplaintList : list) {
            if (clientIds.contains(jsmsComplaintList.getClientId())) {
                continue;
            }
            clientIds.add(jsmsComplaintList.getClientId());
        }

        if (!Collections3.isEmpty(clientIds)) {
            // 查询客户
            accounts = jsmsAccountService.getByIds(clientIds);
        }
        return accounts;
    }

    private List<JsmsComplaintListExt> buildPoList(List<JsmsComplaintList> list,
                                                   List<JsmsChannel> channels, List<JsmsUser> users,List<JsmsUser> operators, List<JsmsAccount> accounts,String operatorStr) {
        List<JsmsComplaintListExt> pos = new ArrayList<>();

        JsmsComplaintListExt po;
        for (JsmsComplaintList jsmsComplaintList : list) {
            po = new JsmsComplaintListExt();
            com.ucpaas.sms.util.beans.BeanUtil.copyProperties(jsmsComplaintList, po);

            JsmsUser user = getUser(jsmsComplaintList.getBelongSale(), users);
            if (user != null) {
                po.setRealname(user.getRealname());
            }
            JsmsChannel channel = getChannel(jsmsComplaintList.getChannelId(), channels);
            if (channel != null) {
                po.setChannelname(channel.getChannelname());
            }
            JsmsAccount account = getAccount(jsmsComplaintList.getClientId(), accounts);
            if (account != null) {
                po.setName(account.getName());
            }
            JsmsUser us= getUser(jsmsComplaintList.getOperator(), operators);
            if (us != null) {
                po.setOperatorStr(us.getRealname());
            }
            //如果录入者输入框有值,,投诉列表也需要有值
//            if(StringUtils.isNotBlank(operatorStr)){
//                if(StringUtils.isNotBlank(po.getOperatorStr())){
//                    pos.add(po);
//                }else{
//                    logger.debug("本条投诉数据无法匹配录入者,投诉记录id为-->{}",JsonUtil.toJson(jsmsComplaintList));
//                }
//            }else{
//                pos.add(po);
//            }
            pos.add(po);
        }
        return pos;
    }

    private JsmsAccount getAccount(String clientId, List<JsmsAccount> accounts) {
        JsmsAccount result = null;
        if (com.ucpaas.sms.common.util.StringUtils.isBlank(clientId) || Collections3.isEmpty(accounts)) {
            return result;
        }
        for (JsmsAccount client : accounts) {
            if (clientId.equals(client.getClientid())) {
                result = new JsmsAccount();
                result.setClientid(client.getClientid());
                result.setName(client.getName());
                break;
            }
        }
        return result;
    }

    private JsmsUser getUser(Long belongSale, List<JsmsUser> users) {
        JsmsUser result = null;
        if (belongSale == null || Collections3.isEmpty(users)) {
            return result;
        }
        for (JsmsUser user : users) {
            if (belongSale.equals(user.getId())) {
                result = new JsmsUser();
                result.setId(user.getId());
                result.setRealname(user.getRealname());
                break;
            }
        }

        return result;
    }

    private JsmsChannel getChannel(Integer channelId, List<JsmsChannel> channels) {
        JsmsChannel result = null;
        if (channelId == null || Collections3.isEmpty(channels)) {
            return result;
        }

        for (JsmsChannel channel : channels) {
            if (channelId.equals(channel.getCid())) {
                result = new JsmsChannel();
                result.setCid(channel.getCid());
                result.setChannelname(channel.getChannelname());
                break;
            }
        }
        return result;
    }

    private List<JsmsUser> getOperator(List<JsmsComplaintList> list, String operatorStr) {
        List<JsmsUser> users = null;
        //当录入者(operatorStr)无值时
        if (StringUtils.isBlank(operatorStr)) {
            Set<Long> userIds = new HashSet<>();
            for (JsmsComplaintList complaintList : list) {
                if (userIds.contains(complaintList.getOperator())) {
                    continue;
                }
                userIds.add(complaintList.getOperator());
            }
            if (!Collections3.isEmpty(userIds)) {
                users = jsmsUserService.getByIds(userIds);
            }
        } else {
            //录入者(operatorStr)有值时
            List<JsmsUser> jsmsUsers = jsmsUserService.getByRealname(operatorStr);
            if (jsmsUsers != null && jsmsUsers.size() > 0) {
                Set<Long> userIds = new HashSet<>();
                for (JsmsUser jsmsUser : jsmsUsers) {
                    userIds.add(jsmsUser.getId());
                }

                Set<Long> userIdds = new HashSet<>();
                //得到了用户id,可以开始与投诉表中的数据比对
                for (JsmsComplaintList complaintList : list) {
                    if (userIds.contains(complaintList.getOperator())) {
                        userIdds.add(complaintList.getOperator());
                        continue;
                    }
                }
                if (!Collections3.isEmpty(userIdds)) {
                    users = jsmsUserService.getByIds(userIdds);
                }
            }
        }
        return users;
    }

    private List<JsmsUser> getJsmsUsers(List<JsmsComplaintList> list) {
        List<JsmsUser> users = null;
        Set<Long> userIds = new HashSet<>();
        for (JsmsComplaintList jsmsComplaintList : list) {
            if (userIds.contains(jsmsComplaintList.getBelongSale())) {
                continue;
            }
            userIds.add(jsmsComplaintList.getBelongSale());
        }

        if (!Collections3.isEmpty(userIds)) {
            // 查询客户
            users = jsmsUserService.getByIds(userIds);
        }
        return users;
    }
    private List<JsmsChannel> getJsmsChannels(List<JsmsComplaintList> list) {
        List<JsmsChannel> channels = null;
        Set<Integer> cids = new HashSet<>();
        for (JsmsComplaintList complaintList : list) {
            if (cids.contains(complaintList.getChannelId())) {
                continue;
            }
            cids.add(complaintList.getChannelId());
        }
        if (!Collections3.isEmpty(cids)) {
            channels = jsmsChannelService.getByCids(cids);
        }
        return channels;
    }

    private boolean generateImportResultExcel(List<Map<String, Object>> dataList, String adminId, String tempFileSavePath) {
        String filePath = tempFileSavePath + "import" + "/批量添加投诉明细结果-userid-" + adminId
                + ".xls";
        File dir = new File(tempFileSavePath + "/import");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(filePath);
        if (file.exists()) {
            file.delete();
        }
        JxlExcel excel = new JxlExcel();
        excel.setFilePath(filePath);
        excel.setShowPage(false);
        excel.setTitle("批量添加投诉明细结果列表");
        excel.addHeader(20, "短信ID", "smsid");
        excel.addHeader(20, "通道号", "channelid");
        excel.addHeader(20, "手机号码", "phone");
        excel.addHeader(20, "处理时间(精确到秒)", "date");
        excel.addHeader(50, "导入结果(是否添加成功)", "importState");
        excel.addHeader(100, "失败原因", "failReason");
        excel.setDataList(dataList);
        return JxlExcelUtils.exportExcel(excel);
    }

    public R validataComplaint(String adminId, List<List<String>> excelDataList, List<Map<String, Object>> illegalDataList,
                               List<Map<String, Object>> legalDataList) {
        List<String> row = null;
        row = (List<String>)excelDataList.get(0);
        //Excel模板必须包含短信ID、通道号、手机号码、处理时间
        if (!"短信ID".equals(row.get(0)) || !"通道号".equals(row.get(1)) || !"手机号码".equals(row.get(2))
                || !"处理时间".equals(row.get(3))) {
            return R.error("请使用系统提供的批量导入投诉模板Excel文件进行导入");
        }
        for (int pos = 1; pos < excelDataList.size(); pos++) {
            // 得到当前数据进行验证操作
            row = excelDataList.get(pos);

            if (row != null && row.size() > 0) {
                StringBuilder errorMsg = new StringBuilder();
                String smsid = "";
                String channelid = "";
                String phone = "";
                String date = "";
                try {
                    smsid = Objects.toString(row.get(0), "").trim();
                    channelid = Objects.toString(row.get(1), "").trim();
                    phone = Objects.toString(row.get(2), "").trim();
                    date = Objects.toString(row.get(3), "").trim();
                } catch (Exception e) {
                    logger.error("数据格式异常--->{}<-->{}<-->{}<-->{}", row.get(0), row.get(1), row.get(2), row.get(3));
                    errorMsg.append("数据格式异常/");
                }
                //获取每行的数据
                Map<String, Object> complaintParams = new HashMap<>();
                complaintParams.put("smsid", smsid);
                complaintParams.put("channelid", channelid);
                complaintParams.put("phone", phone);
                complaintParams.put("date", date);

                //校验字段(短信ID、通道号、手机号码、处理时间)
                if (StringUtils.isBlank(smsid) || StringUtils.isBlank(channelid) || StringUtils.isBlank(phone) || StringUtils.isBlank(date)) {
                    errorMsg.append("短信ID或通道号或手机号码或处理时间不存在/");
                }
                //正则校验通道号是否存在
                Pattern pattern = Pattern.compile("^-?[0-9]+");
                Matcher isNum = pattern.matcher(channelid);
                JsmsChannel jsmsChannel=null;
                if(isNum.matches() ){
                    //通道号符合规则
                    //校验发送记录是否存在(规则：根据通道号得到通道流水表标识，根据（标识+处理时间）拼接表名，根据（短信ID+手机号码）查询对应流水表，校验短信是否存在)
                    jsmsChannel = jsmsChannelService.getByCid(Integer.valueOf(channelid));
                }

                Integer identify = null;
                if (jsmsChannel == null) {
                    errorMsg.append("通道不存在或者通道标识不符合要求/");
                    logger.debug("通道不存在!--->{}", channelid);
                } else {
                    //获取标识和处理时间
                    identify = jsmsChannel.getIdentify();
                }
                //正则匹配时间(格式为:yyyy-MM-dd HH:mm:ss)
                //String str="2018-01-03 10:11:12";
                Pattern patt = Pattern.compile("^[0-9]{4}[-][0-9]{2}[-][0-9]{2}[ ][0-9]{2}[:][0-9]{2}[:][0-9]{2}$");
                Matcher isN = patt.matcher(date);

                String tableName=null;
                //匹配成功
                if(isN.matches()){
                    //拼接表名,例如:t_sms_record_2_20180111
                    DateTime d2 = null;
                    try {
                        d2 = new DateTime(DateUtils.parseDateStrictly(date.substring(0, 10), "yyyy-MM-dd"));
                    } catch (Throwable e) {
                        logger.debug("时间格式错误! ----> {}", date);
                        errorMsg.append("时间格式错误/");
                    }
                    String dateInt = d2.toString("yyyyMMdd");
                    tableName = SysConstant.T_SMS_RECORD_ + identify + "_" + dateInt;
                }else{
                    //匹配不成功!
                    logger.debug("时间格式错误!---->{}", date);
                    errorMsg.append("时间不是yyyy-MM-dd HH:mm:ss格式/");
                }

                //JsmsRecord record = jsmsRecordService.getBySmsuuid(smsuuid, tableName);
                //根据（短信ID+手机号码）查询对应流水表，校验短信是否存在
                Map<String, String> mm = new HashMap<>();
                mm.put("smsid", smsid);
                mm.put("phone", phone);
                mm.put("tableName", tableName);
                //标识不合法就没必要走下去了
                if(identify!=null){
                    List<JsmsRecord> list = null;
                    try {
                        list = jsmsRecordService.findList(mm);
                    } catch (Exception e) {
                        e.printStackTrace();
                        logger.debug("表不存在-->{}",tableName);
                        errorMsg.append("表不存在/");
                    }
                    JsmsRecord record = null;
                    if (list != null && list.size() > 0) {
                        record = list.get(0);
                        RecordDTO recordDTO = new RecordDTO();
                        BeanUtil.copyProperties(record, recordDTO);
                        recordDTO.setTableName(tableName);
                        recordDTO.setIdentify(Integer.valueOf(identify));
                        recordDTO.setOperator(Long.valueOf(adminId));
                        logger.debug("运营平台正在调取导入投诉的接口--->{}", JsonUtil.toJson(recordDTO));
                        R r = jsmsSignComplaintService.signSingleComplaint(recordDTO);
                        if(r.getCode().equals(500)){
                            errorMsg.append(r.getMsg());
                        }
                    } else {
                        logger.debug("标记投诉查询短信发送记录为空：条件! ----> {} {}  {}", smsid, phone,tableName);
                        errorMsg.append("短信发送记录为空/");
                    }
                }
                String errorMsgStr = errorMsg.toString();
                //将合法和不合法的数据分类整理
                if (StringUtils.isNotBlank(errorMsgStr)) {
                    complaintParams.put("importState", "失败");
                    complaintParams.put("failReason", errorMsgStr);
                    illegalDataList.add(complaintParams);
                } else {
                    complaintParams.put("importState", "成功");
                    complaintParams.put("failReason", errorMsgStr);
                    legalDataList.add(complaintParams);
                }
            }
        }
        R r = new R();
        Map<String, Object> data = new HashMap<>();
        data.put("illegalDataList", illegalDataList);
        data.put("legalDataList", legalDataList);
        r.setData(data);
        return r;
    }
}
