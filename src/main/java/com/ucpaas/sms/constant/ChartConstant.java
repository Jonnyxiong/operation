package com.ucpaas.sms.constant;

import com.ucpaas.sms.model.Highcharts;

/**
 * 图表常量
 * 
 * @author xiejiaan
 */
public class ChartConstant {

	/**
	 * 图表类型
	 * 
	 * @author xiejiaan
	 */
	public enum ChartType {
		/**
		 * 日期类型
		 */
		date,
		/**
		 * 时间类型
		 */
		time;
	}

	/**
	 * 图表信息：短信通道数量实时监控
	 */
	public static final Highcharts channel_realtime_quantity;
	/**
	 * 图表信息：短信通道成功率实时监控
	 */
	public static final Highcharts channel_realtime_rate;
	/**
	 * 图表信息：短信通道延时时长实时监控
	 */
	public static final Highcharts channel_realtime_delay;
	/**
	 * 图表信息：短信通道并发量实时监控
	 */
	public static final Highcharts channel_realtime_concurrency;

	/**
	 * 图表信息：短信通道数量历史记录监控
	 */
	public static final Highcharts channel_history_quantity;
	/**
	 * 图表信息：短信通道成功率历史记录监控
	 */
	public static final Highcharts channel_history_rate;
	/**
	 * 图表信息：短信通道延时时长历史记录监控
	 */
	public static final Highcharts channel_history_delay;
	/**
	 * 图表信息：短信通道并发量历史记录监控
	 */
	public static final Highcharts channel_history_concurrency;

	/**
	 * 图表信息：验证数量实时监控
	 */
	public static final Highcharts verify_realtime_quantity;
	/**
	 * 图表信息：验证成功率实时监控
	 */
	public static final Highcharts verify_realtime_rate;

	/**
	 * 图表信息：验证应用数量历史记录监控
	 */
	public static final Highcharts verify_history_apptotalcnt;
	/**
	 * 图表信息：验证请求数量历史记录监控
	 */
	public static final Highcharts verify_history_recvtotalcnt;
	/**
	 * 图表信息：发送成功数量历史记录监控
	 */
	public static final Highcharts verify_history_sendsucnt;
	/**
	 * 图表信息：验证成功数量历史记录监控
	 */
	public static final Highcharts verify_history_verifysucnt;
	/**
	 * 图表信息：发送成功率历史记录监控
	 */
	public static final Highcharts verify_history_sendrate;
	/**
	 * 图表信息：验证成功率历史记录监控
	 */
	public static final Highcharts verify_history_verifyrate;

	/**
	 * 图表信息：验证消费实时监控
	 */
	public static final Highcharts verify_realtime_costmoney;

	static {
		channel_realtime_quantity = new Highcharts();
		channel_realtime_quantity.setTitle("短信通道数量实时监控");
		channel_realtime_quantity.setxTitle("时间");
		channel_realtime_quantity.setyTitle("数量");
		channel_realtime_quantity.setyUnit("条");
		channel_realtime_quantity.setxKey("datatime");
		channel_realtime_quantity.addSeries("请求数量", "recvtotalcnt");
		channel_realtime_quantity.addSeries("发送数量", "sendtotalcnt");
		channel_realtime_quantity.addSeries("延时数量", "delaytotalcnt");
		channel_realtime_quantity.addSeries("验证成功数量", "verifytotalcnt");
		channel_realtime_quantity.addSeries("到达数量", "reachtotalcnt");

		channel_history_quantity = channel_realtime_quantity.clone();
		channel_history_quantity.setTitle("短信通道数量历史记录监控");

		channel_realtime_rate = new Highcharts();
		channel_realtime_rate.setTitle("通道成功率实时监控");
		channel_realtime_rate.setxTitle("时间");
		channel_realtime_rate.setyTitle("通道成功率");
		channel_realtime_rate.setyUnit("%");
		channel_realtime_rate.setyMax(100L);
		channel_realtime_rate.setxKey("datatime");
		channel_realtime_rate.addSeries("通道成功率", "reachrate", true, null);

		channel_history_rate = channel_realtime_rate.clone();
		channel_history_rate.setTitle("通道及时率实时监控");

		channel_realtime_delay = new Highcharts();
		channel_realtime_delay.setTitle("通道及时率实时监控");
		channel_realtime_delay.setxTitle("时间");
		channel_realtime_delay.setyTitle("通道及时率");
		channel_realtime_delay.setyUnit("%");
		channel_realtime_delay.setyMax(100L);
		channel_realtime_delay.setxKey("datatime");
		channel_realtime_delay.addSeries("通道及时率", "timelyrate", true, null);

		channel_history_delay = channel_realtime_delay.clone();
		channel_history_delay.setTitle("通道延时时长历史记录监控");

		channel_realtime_concurrency = new Highcharts();
		channel_realtime_concurrency.setTitle("通道并发量实时监控");
		channel_realtime_concurrency.setxTitle("时间");
		channel_realtime_concurrency.setyTitle("并发量");
		channel_realtime_concurrency.setyUnit("条/秒");
		channel_realtime_concurrency.setxKey("datatime");
		channel_realtime_concurrency.addSeries("并发量", "concurrency", true, null);

		channel_history_concurrency = channel_realtime_concurrency.clone();
		channel_history_concurrency.setTitle("通道并发量历史记录监控");

		verify_realtime_quantity = new Highcharts();
		verify_realtime_quantity.setTitle("验证数量实时监控");
		verify_realtime_quantity.setxTitle("时间");
		verify_realtime_quantity.setyTitle("数量");
		verify_realtime_quantity.setyUnit("条");
		verify_realtime_quantity.setxKey("datatime");
		verify_realtime_quantity.addSeries("请求接口数", "recvtotalcnt");
		verify_realtime_quantity.addSeries("请求应用数", "apptotalcnt");
		verify_realtime_quantity.addSeries("发送数量", "sendtotalcnt");
		verify_realtime_quantity.addSeries("短信验证请求数量", "sms_recvtotalcnt");
		verify_realtime_quantity.addSeries("语音验证请求数量", "call_recvtotalcnt");
		verify_realtime_quantity.addSeries("MID验证请求数量", "mid_recvtotalcnt");

		verify_realtime_rate = new Highcharts();
		verify_realtime_rate.setTitle("验证成功率实时监控");
		verify_realtime_rate.setxTitle("时间");
		verify_realtime_rate.setyTitle("成功率");
		verify_realtime_rate.setyUnit("%");
		verify_realtime_rate.setyMax(100L);
		verify_realtime_rate.setxKey("datatime");
		verify_realtime_rate.addSeries("验证成功率", "verifyrate", true, null);
		verify_realtime_rate.addSeries("发送成功率", "sendrate", true, null);

		verify_history_apptotalcnt = new Highcharts();
		verify_history_apptotalcnt.setTitle("验证应用数量历史记录监控");
		verify_history_apptotalcnt.setxTitle("时间");
		verify_history_apptotalcnt.setyTitle("数量");
		verify_history_apptotalcnt.setyUnit("条");
		verify_history_apptotalcnt.setxKey("datatime");
		verify_history_apptotalcnt.addSeries("总的验证应用数量", "apptotalcnt");
		verify_history_apptotalcnt.addSeries("短信验证应用数量", "sms_apptotalcnt");
		verify_history_apptotalcnt.addSeries("语音验证应用数量", "call_apptotalcnt");
		verify_history_apptotalcnt.addSeries("MID验证应用数量", "mid_apptotalcnt");

		verify_history_recvtotalcnt = new Highcharts();
		verify_history_recvtotalcnt.setTitle("验证请求数量历史记录监控");
		verify_history_recvtotalcnt.setxTitle("时间");
		verify_history_recvtotalcnt.setyTitle("数量");
		verify_history_recvtotalcnt.setyUnit("条");
		verify_history_recvtotalcnt.setxKey("datatime");
		verify_history_recvtotalcnt.addSeries("总的验证请求数量", "recvtotalcnt");
		verify_history_recvtotalcnt.addSeries("短信验证请求数量", "sms_recvtotalcnt");
		verify_history_recvtotalcnt.addSeries("语音验证请求数量", "call_recvtotalcnt");
		verify_history_recvtotalcnt.addSeries("MID验证请求数量", "mid_recvtotalcnt");

		verify_history_sendsucnt = new Highcharts();
		verify_history_sendsucnt.setTitle("发送成功数量历史记录监控");
		verify_history_sendsucnt.setxTitle("时间");
		verify_history_sendsucnt.setyTitle("数量");
		verify_history_sendsucnt.setyUnit("条");
		verify_history_sendsucnt.setxKey("datatime");
		verify_history_sendsucnt.addSeries("总的发送成功数量", "sendsucnt");
		verify_history_sendsucnt.addSeries("短信发送成功数量", "sms_sendsucnt");
		verify_history_sendsucnt.addSeries("语音发送成功数量", "call_sendsucnt");
		verify_history_sendsucnt.addSeries("MID发送成功数量", "mid_sendsucnt");

		verify_history_verifysucnt = new Highcharts();
		verify_history_verifysucnt.setTitle("验证成功数量历史记录监控");
		verify_history_verifysucnt.setxTitle("时间");
		verify_history_verifysucnt.setyTitle("数量");
		verify_history_verifysucnt.setyUnit("条");
		verify_history_verifysucnt.setxKey("datatime");
		verify_history_verifysucnt.addSeries("总的验证成功数量", "verifysucnt");
		verify_history_verifysucnt.addSeries("短信验证成功数量", "sms_verifysucnt");
		verify_history_verifysucnt.addSeries("语音验证成功数量", "call_verifysucnt");
		verify_history_verifysucnt.addSeries("MID验证成功数量", "mid_verifysucnt");

		verify_history_sendrate = new Highcharts();
		verify_history_sendrate.setTitle("发送成功率历史记录监控");
		verify_history_sendrate.setxTitle("时间");
		verify_history_sendrate.setyTitle("成功率");
		verify_history_sendrate.setyUnit("%");
		verify_history_sendrate.setyMax(100L);
		verify_history_sendrate.setxKey("datatime");
		verify_history_sendrate.addSeries("总的发送成功率", "sendrate", true, null);
		verify_history_sendrate.addSeries("短信发送成功率", "sms_sendrate", true, null);
		verify_history_sendrate.addSeries("语音发送成功率", "call_sendrate", true, null);
		verify_history_sendrate.addSeries("MID发送成功率", "mid_sendrate", true, null);

		verify_history_verifyrate = new Highcharts();
		verify_history_verifyrate.setTitle("验证成功率历史记录监控");
		verify_history_verifyrate.setxTitle("时间");
		verify_history_verifyrate.setyTitle("成功率");
		verify_history_verifyrate.setyUnit("%");
		verify_history_verifyrate.setyMax(100L);
		verify_history_verifyrate.setxKey("datatime");
		verify_history_verifyrate.addSeries("总的验证成功率", "verifyrate", true, null);
		verify_history_verifyrate.addSeries("短信验证成功率", "sms_verifyrate", true, null);
		verify_history_verifyrate.addSeries("语音验证成功率", "call_verifyrate", true, null);
		verify_history_verifyrate.addSeries("MID验证成功率", "mid_verifyrate", true, null);

		verify_realtime_costmoney = new Highcharts();
		verify_realtime_costmoney.setTitle("验证消费实时监控");
		verify_realtime_costmoney.setxTitle("时间");
		verify_realtime_costmoney.setyTitle("金额");
		verify_realtime_costmoney.setyUnit("元");
		verify_realtime_costmoney.setxKey("datatime");
		verify_realtime_costmoney.addSeries("消费总金额", "costmoney");
		verify_realtime_costmoney.addSeries("平均消费金额", "avg_costmoney");
		verify_realtime_costmoney.addSeries("短信验证消费金额", "sms_costmoney");
		verify_realtime_costmoney.addSeries("语音验证消费金额", "call_costmoney");
		verify_realtime_costmoney.addSeries("MID验证消费金额", "mid_costmoney");
	}

}
