package com.ucpaas.sms.service.statistic;

import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.dto.AccessStatisticVO;
import com.ucpaas.sms.model.Excel;
import com.ucpaas.sms.model.Page;

/**
 * Created by dylan on 2017/7/26.
 */
public interface ChannelConsumeService {

    Page queryPage(Page page);

    Page queryPageTotal(Page page);

    ResultVO exportPage(Page page, Excel excel);

    ResultVO exportPage4BasicStatis(Page page, Excel excel);

    Page<AccessStatisticVO> queryPage4BasicStatis(Page page);

    Page<AccessStatisticVO> queryPage4BasicStatisTotal(Page page);

}
