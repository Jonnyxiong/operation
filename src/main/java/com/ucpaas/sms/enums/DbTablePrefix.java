package com.ucpaas.sms.enums;

/**
 * Created by lpjLiu on 2017/7/10.数据库流水表前缀
 */
public enum DbTablePrefix {

    T_SMS_ACCESS_YYYYMMDD("t_sms_access_", "短信access流水表");

    private String tablePrefix;
    private String desc;

    private DbTablePrefix(String tablePrefix, String desc) {
        this.tablePrefix = tablePrefix;
        this.desc = desc;
    }

    public String getTablePrefix() {
        return tablePrefix;
    }

    public String getDesc() {
        return desc;
    }

}