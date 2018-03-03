package com.ucpaas.sms.service.statistic;

import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.dto.ClientConsumeVO2Point3;
import com.ucpaas.sms.entity.message.Department;
import com.ucpaas.sms.model.Excel;
import com.ucpaas.sms.model.Page;

import java.util.List;

/**
 * Created by dylan on 2017/7/26.
 */
public interface ClientConsumeService {

    Page queryPage(Page page);

    Page queryPageTotal(Page page);

    ResultVO exportPage(Page page, Excel excel);

    /**
     * 查询所有的一级部门
     */
    List<Department> getAllDept();

    Page getClientConsume2point3(Page page);

    ClientConsumeVO2Point3 getClientConsume2point3Total(Page page);
}
