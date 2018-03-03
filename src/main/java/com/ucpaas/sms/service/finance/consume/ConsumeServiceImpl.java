package com.ucpaas.sms.service.finance.consume;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.dto.ResultVO;
import com.jsmsframework.common.enums.Code;
import com.jsmsframework.common.util.BeanUtil;
import com.jsmsframework.finance.entity.JsmsClientConsumerList;
import com.jsmsframework.finance.service.JsmsClientConsumerListService;
import com.jsmsframework.order.entity.JsmsOemClientOrder;
import com.jsmsframework.order.enums.OEMClientOrderType;
import com.jsmsframework.order.service.JsmsOemClientOrderService;
import com.jsmsframework.user.entity.JsmsAccount;
import com.jsmsframework.user.entity.JsmsUser;
import com.jsmsframework.user.service.JsmsAccountService;
import com.jsmsframework.user.service.JsmsUserService;
import com.ucpaas.sms.common.util.file.ExcelUtils;
import com.ucpaas.sms.dto.ClientConsumerListVO;
import com.ucpaas.sms.dto.OemClientOrderVO;
import com.ucpaas.sms.model.Excel;
import com.ucpaas.sms.util.AgentUtils;
import com.ucpaas.sms.util.ConfigUtils;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by dylan on 2017/10/17.
 */
@Service
public class ConsumeServiceImpl implements ConsumeService{
    private static Logger logger = LoggerFactory.getLogger(ConsumeServiceImpl.class);

    @Autowired
    private JsmsClientConsumerListService clientConsumerListService;
    @Autowired
    private JsmsOemClientOrderService oemClientOrderService;
    @Autowired
    private JsmsUserService jsmsUserService;
    @Autowired
    private JsmsAccountService jsmsAccountService;

    @Override
    public JsmsPage queryBrandList(JsmsPage page) {
        // 设置数据权限
        setAuthority4Consume(page);

        Map params = page.getParams();
        boolean isNeedQuery = updateAndReturnIsNeedQuery(params);
        if (!isNeedQuery){
            return page;
        }
        String endTimeDay = (String) params.get("endTimeDay");
        if(StringUtils.isNotBlank(endTimeDay)){
            params.put("endTimeDay",endTimeDay + " 23:59:59");
        }

        clientConsumerListService.queryList(page);
        List<JsmsClientConsumerList> data = page.getData();
        if(data.isEmpty()){
            return page;
        }
        Set<Long> operatorIds = new HashSet<>();
        Set<String> clientIds = new HashSet<>();
        List<ClientConsumerListVO> resultList = new ArrayList<>();
        for (JsmsClientConsumerList origin : data) {
            operatorIds.add(origin.getOperator());
            clientIds.add(origin.getClientid());
            ClientConsumerListVO temp = new ClientConsumerListVO();
            BeanUtil.copyProperties(origin, temp);
            resultList.add(temp);
        }
        List<JsmsUser> jsmsUsers = jsmsUserService.getByIds(operatorIds);
        Map<Long, String> userNameMap = new HashMap<>();
        for (JsmsUser jsmsUser : jsmsUsers) {
            userNameMap.put(jsmsUser.getId(),jsmsUser.getRealname());
        }
        List<JsmsAccount> accountList = jsmsAccountService.getByIds(clientIds);
        Map<String, String> accountMap = new HashMap<>();
        for (JsmsAccount jsmsAccount : accountList) {
            accountMap.put(jsmsAccount.getClientid(),jsmsAccount.getName());
        }
        for (ClientConsumerListVO temp : resultList) {
            temp.setOperatorStr(userNameMap.get(temp.getOperator()));
            temp.setClientName(accountMap.get(temp.getClientid()));
        }
        page.setData(resultList);
        //去除不必要的数据 v5.19.3
        page.setParams(null);

        return page;
    }

    @Override
    public Integer queryBrandTotal(JsmsPage page) {
        // 设置数据权限
        setAuthority4Consume(page);

        Map params = page.getParams();
        boolean isNeedQuery = updateAndReturnIsNeedQuery(params);
        if (!isNeedQuery){
            return 0;
        }
        String endTimeDay = (String) params.get("endTimeDay");
        if(StringUtils.isNotBlank(endTimeDay)){
            params.put("endTimeDay",endTimeDay + " 23:59:59");
        }

        Integer total = clientConsumerListService.queryBrandConsumeTotal(params);
        if (total == null) {
            total = 0;
        }
        return total;
    }

    private boolean updateAndReturnIsNeedQuery(Map params){

        List<JsmsAccount> likeIdAndName = jsmsAccountService.getLikeIdAndName(params);
        if(likeIdAndName != null && !likeIdAndName.isEmpty()){
            params.put("clientIds", likeIdAndName);
            params.remove("clientid");
            params.remove("clientId");
            return true;
        }else {
            return false;
        }
    }
    /*private boolean updateAndReturnIsNeedQuery(Map params){
        String clientId = (String) params.get("clientId");
        String clientName = (String) params.get("clientName");

        Object dataAuthority = params.get("dataAuthorityCondition");
        if (StringUtils.isNotBlank(clientId) || StringUtils.isNotBlank(clientName)){
            List<JsmsAccount> likeIdAndName = jsmsAccountService.getLikeIdAndName(clientId,clientName);
            if(likeIdAndName != null && !likeIdAndName.isEmpty()){
                params.put("clientIds", likeIdAndName);
            }else {
                return false;
            }
            params.remove("clientId");
        }
        return true;
    }*/


    @Override
    public JsmsPage queryOemList(JsmsPage page) {
        // 设置数据权限
        setAuthority4Consume(page);

        Map params = page.getParams();
        boolean isNeedQuery = updateAndReturnIsNeedQuery(params);
        if (!isNeedQuery){
            return page;
        }
        String endCreateTime = (String) params.get("endCreateTime");
        if(StringUtils.isNotBlank(endCreateTime)){
            params.put("endCreateTime",endCreateTime + " 23:59:59");
        }

        oemClientOrderService.queryList(page);
        List<JsmsOemClientOrder> data = page.getData();
        if(data.isEmpty()){
            return page;
        }
        Set<Long> operatorIds = new HashSet<>();
        Set<String> clientIds = new HashSet<>();
        List<OemClientOrderVO> resultList = new ArrayList<>();
        for (JsmsOemClientOrder origin : data) {
            operatorIds.add(origin.getOperator());
            clientIds.add(origin.getClientId());
            OemClientOrderVO temp = new OemClientOrderVO();
            BeanUtil.copyProperties(origin, temp);
            resultList.add(temp);
        }
        List<JsmsUser> jsmsUsers = jsmsUserService.getByIds(operatorIds);
        Map<Long, String> userNameMap = new HashMap<>();
        for (JsmsUser jsmsUser : jsmsUsers) {
            userNameMap.put(jsmsUser.getId(),jsmsUser.getRealname());
        }
        List<JsmsAccount> accountList = jsmsAccountService.getByIds(clientIds);
        Map<String, String> accountMap = new HashMap<>();
        for (JsmsAccount jsmsAccount : accountList) {
            accountMap.put(jsmsAccount.getClientid(),jsmsAccount.getName());
        }
        for (OemClientOrderVO temp : resultList) {
            temp.setOperatorStr(userNameMap.get(temp.getOperator()));
            temp.setClientName(accountMap.get(temp.getClientId()));
        }
        page.setData(resultList);
        //去除不必要返回的数据 v5.19.3
        page.setParams(null);

        return page;
    }

    @Override
    public Integer queryOemTotal(JsmsPage page) {
        // 设置数据权限
        setAuthority4Consume(page);

        Map params = page.getParams();
        boolean isNeedQuery = updateAndReturnIsNeedQuery(params);
        if (!isNeedQuery) {
            return 0;
        }
        String endCreateTime = (String) params.get("endCreateTime");
        if (StringUtils.isNotBlank(endCreateTime)) {
            params.put("endCreateTime", endCreateTime + " 23:59:59");
        }

        Integer total = oemClientOrderService.queryOEMConsumeTotal(params);
        if (total == null){
            total = 0;
        }
        return total;
    }


    private void setAuthority4Consume(JsmsPage page){
        // 构造数据权限查询条件
        Long userId = page.getParams().get("userId") == null ? null : Long.parseLong(page.getParams().get("userId").toString());
        if (userId != null) {
            page.getParams().put("dataAuthorityCondition", AgentUtils.getDataAuthorityCondition(userId, true, false));
        }
    }



    @Override
    public ResultVO exportBrandList(JsmsPage page, Excel excel) {
        /**
         * Thread.currentThread().getStackTrace()[1].getMethodName() --> 获取当前方法名称, 即 : exportBrandList
         */
        Map<String, String> totalMap = new HashMap<>(2);
        JsmsPage pageTotal = new JsmsPage();
        Map totalParams = new HashMap(page.getParams());
        pageTotal.setParams(totalParams);
        pageTotal.setOrderByClause(page.getOrderByClause());
        pageTotal.setTotalRecord(page.getTotalRecord());
        if (!"".equals(page.getParams().get("operateType"))) {
            Integer total = this.queryBrandTotal(pageTotal);
            totalMap.put("dueTimeStr", "总计");
            totalMap.put("smsNumberStr", total + "条");
        }
        return commonExport(page,excel,Thread.currentThread().getStackTrace()[1].getMethodName(), totalMap);
    }

    @Override
    public ResultVO exportOemList(JsmsPage page, Excel excel) {
        Map<String, String> totalMap = new HashMap<>(2);
        if (!"全部".equals(page.getParams().get("_operateType"))) {
            JsmsPage pageTotal = new JsmsPage();
            Map totalParams = new HashMap(page.getParams());
            pageTotal.setParams(totalParams);
            pageTotal.setOrderByClause(page.getOrderByClause());
            pageTotal.setTotalRecord(page.getTotalRecord());
            Integer total = this.queryOemTotal(pageTotal);
            totalMap.put("dueTimeStr", "总计");
            totalMap.put("orderNumberStr", total+ "条");
        }
        return commonExport(page,excel,Thread.currentThread().getStackTrace()[1].getMethodName(), totalMap);
    }

    private ResultVO commonExport(JsmsPage page, Excel excel, String method,Map totalMap){
        ResultVO resultVO = ResultVO.failure();
        resultVO.setMsg("生成报表失败");
        try {
            page.setRows(Integer.valueOf(ConfigUtils.max_export_excel_num) + 1);
            String queryMethod = method.replaceFirst("export", "query");

            page = (JsmsPage) this.getClass().getMethod(queryMethod,page.getClass()).invoke(this, page);
//            page = queryPage(page);
            List data = page.getData();
            if (data == null || data.size() <= 0){
                return ResultVO.failure("没有数据！先不导出了  ^_^");
            }else if (data.size() > Integer.valueOf(ConfigUtils.max_export_excel_num)){
                return ResultVO.failure("数据量超过"+ConfigUtils.max_export_excel_num+"，请缩小范围后再导出吧  ^_^");
            }

            List result = new ArrayList();
            for (Object obj:data){
                Map<String, String> describe = BeanUtils.describe(obj);
                result.add(describe);
            }
//            添加 总记录
            result.add(totalMap);

            excel.setDataList(result);

            if (ExcelUtils.exportExcel(excel)) {
                resultVO.setSuccess(true);
                resultVO.setMsg("报表生成成功");
                resultVO.setData(excel.getFilePath());
                return resultVO;
            }
        } catch (Exception e) {
            logger.error("生成报表失败", e);
        }
        return resultVO;
    }

}
