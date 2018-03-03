package com.ucpaas.sms.service.finance.consume;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.model.Excel;

/**
 * Created by dylan on 2017/10/17.
 */
public interface ConsumeService {


    JsmsPage queryBrandList(JsmsPage page);

    /**
      * @Description: 查询品牌消费记录短信总数
      * @Author: tanjiangqiang
      * @Date: 2018/1/4 - 15:09
      * @param page
      *
      */
    Integer queryBrandTotal(JsmsPage page);

    JsmsPage queryOemList(JsmsPage page);

    /**
      * @Description: 查询子账户消费短信总数
      * @Author: tanjiangqiang
      * @Date: 2018/1/4 - 16:31
      * @param page
      *
      */
    Integer queryOemTotal(JsmsPage page);

    ResultVO exportBrandList(JsmsPage page, Excel excel);

    ResultVO exportOemList(JsmsPage page, Excel excel);

}
