package com.ucpaas.sms.enums;

/**
 * Created by lpjLiu on 2017/7/10.
 */
public enum DemandStatus {
    /**
     * 资源状态
     */
    寻资源("寻资源", 0), 已供应("已供应", 1), 无资源("无资源", 2), 撤单("撤单", 3);

    // 成员变量
    private String name;
    private Integer value;

    DemandStatus(String name, Integer value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Integer getValue() {
        return value;
    }

    public static String getNameByValue(Integer value) {
        if(value == null){ return null;}

        String result = null;
        for (DemandStatus s : DemandStatus.values()) {
            if (value == s.getValue()) {
                result = s.getName();
                break;
            }
        }
        return result;
    }

    public static DemandStatus getEnumByValue(Integer value) {
        if(value == null){ return null;}

        DemandStatus e = null;
        for (DemandStatus result : DemandStatus.values()) {
            if (value == result.getValue()) {
                e = result;
                break;
            }
        }
        return e;
    }

    @Override
    public String toString() {
        return this.value.toString();
    }
}
