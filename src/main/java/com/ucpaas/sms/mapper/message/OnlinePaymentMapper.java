package com.ucpaas.sms.mapper.message;

import com.jsmsframework.common.dto.JsmsPage;
import com.jsmsframework.finance.entity.JsmsOnlinePayment;
import com.ucpaas.sms.entity.po.JsmsOnlinePaymentPo;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * @description 在线支付表
 * @author huangwenjie
 * @date 2018-01-05
 */
@Repository
public interface OnlinePaymentMapper {


	List<JsmsOnlinePaymentPo> queryListPo(JsmsPage page);

	int count(Map<String, Object> params);

}