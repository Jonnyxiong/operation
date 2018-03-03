package com.ucpaas.sms.service;

import com.jsmsframework.common.util.DateUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * created by xiaoqingwen on 2018/1/29 9:43
 */
public class test {
    public static void main(String[] args) {

        String createTimeStart="2018-01-25";
        Pattern patt = Pattern.compile("^[0-9]{4}[-][0-1]{1}[0-9]{1}[-][0-3]{1}[0-9]{1}$");
        Matcher isN = patt.matcher(createTimeStart);

        //匹配成功
        if(isN.matches()){

        }

    }
}
