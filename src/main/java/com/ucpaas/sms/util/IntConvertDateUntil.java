package com.ucpaas.sms.util;

/**
 * Created by dylan on 2017/10/17.
 */
public class IntConvertDateUntil {

    public static String convertToString(Integer integer ){
        String string = integer.toString();
        if(!string.matches("^\\d{8}$")){
            throw new IllegalArgumentException("参数格式错误");
        }
        StringBuilder stringBuilder = new StringBuilder(string);
        stringBuilder = stringBuilder.insert(4, "-");
        stringBuilder = stringBuilder.insert(7, "-");
        return stringBuilder.toString();
    }


}
