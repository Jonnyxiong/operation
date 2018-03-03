package com.ucpaas.sms.service;

import com.jsmsframework.common.dto.R;
import com.jsmsframework.common.entity.JsmsEmailAlarmSetting;
import com.jsmsframework.common.entity.JsmsSendEmailList;

/**
 * Add by lpjLiu 20171206
 */
public interface TaskAlarmSettingService {

	R sendEmailListInfo(Long id);

	R findSendEmailList();

	R addSendEmailList(JsmsSendEmailList sendEmailList);

	R editSendEmailList(JsmsSendEmailList sendEmailList);

	R disableSendEmailList(Long id);

	R enableSendEmailList(Long id);

	R testSendEmailList(JsmsSendEmailList sendEmailList);

	R emailAlarmSettingInfo(Long id);

	R findEmailAlarmSettingList();

	R addEmailAlarmSetting(JsmsEmailAlarmSetting emailAlarmSetting);

	R editEmailAlarmSetting(JsmsEmailAlarmSetting emailAlarmSetting);

    R disableEmailAlarmSetting(Long id);

    R enableEmailAlarmSetting(Long id);
}
