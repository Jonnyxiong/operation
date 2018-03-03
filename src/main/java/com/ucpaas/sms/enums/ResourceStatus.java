package com.ucpaas.sms.enums;

/**
 * Created by lpjLiu on 2017/7/10.
 */
public enum ResourceStatus {
    /**
     * 资源状态
     */
    待接入("待接入", 0), 已接入("已接入", 1), 待审批("待审批", 2), 撤销("撤销", 3);

    // 成员变量
    private String name;
    private Integer value;

    ResourceStatus(String name, int value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Integer getValue() {
        return value;
    }

    public static String getNameByValue(int value) {
        String result = null;
        for (ResourceStatus s : ResourceStatus.values()) {
            if (value == s.getValue()) {
                result = s.getName();
                break;
            }
        }
        return result;
    }

    public static ResourceStatus getEnumByValue(Integer value) {
        if(value == null){ return null;}

        ResourceStatus e = null;
        for (ResourceStatus result : ResourceStatus.values()) {
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
