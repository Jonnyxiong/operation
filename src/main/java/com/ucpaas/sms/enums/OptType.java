package com.ucpaas.sms.enums;

import com.jsmsframework.finance.enums.AgentType;

import java.util.Objects;

public enum OptType {


    新增(1, "发票新增操作"),
    修改(2, "发票修改操作"),
    删除(3, "发票删操作");

    private Integer value;
    private String desc;

    OptType(Integer value, String desc) {
        this.value = value;
        this.desc = desc;
    }


    public Integer getValue(){
        return value;
    }

    public String getDesc(){
        return desc;
    }

    public static String getDescByValue(Integer value) {
        if(value == null){ return null;}
        String result = null;
        for (com.jsmsframework.finance.enums.AgentType s : AgentType.values()) {
            if (Objects.equals(value, s.getValue())){
                result = s.getDesc();
                break;
            }
        }
        return result;
    }
}
