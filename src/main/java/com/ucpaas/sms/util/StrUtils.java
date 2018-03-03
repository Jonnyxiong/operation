package com.ucpaas.sms.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 * 
 * @author xiejiaan
 */
public class StrUtils {
	
	/**
	 * 数字装成字符串，不足补0
	 * @param num
	 * @param strLength 长度
	 * @param flag 0左边补0,1右边补0
	 * @return 
	 */
	public static String addZeroForNum(int num, int strLength, String flag) {
		String str = num + "";
		int strLen = str.length();
		if (strLen < strLength) {
			if ("0".equals(flag)) {
				while (strLen < strLength) {
					StringBuffer sb = new StringBuffer();
					sb.append("0").append(str);// 左补0
					str = sb.toString();
					strLen = str.length();
				}
			} else {
				if ("0".equals(flag)) {
					while (strLen < strLength) {
						StringBuffer sb = new StringBuffer();
						sb.append(str).append("0");// 右补0
						str = sb.toString();
						strLen = str.length();
					}
				}
			}
		}

		return str;
	}

	/**
	 * 替换字符串中的占位符，占位符的正则为\[@\w+@\]
	 * 
	 * @param data
	 *            处理的字符串
	 * @param params
	 *            替换的参数
	 * @return
	 */
	public static String replacePlaceholder(String data, Map<String, Object> params) {
		Pattern p = Pattern.compile("\\[@\\w+@\\]");
		Matcher m = p.matcher(data);
		String key;
		Object value;
		while (m.find()) {
			key = m.group();
			value = params.get(key.substring(2, key.length() - 2));
			if (value != null) {
				data = data.replace(key, value.toString());
			}
		}
		return data;
	}
	
    /**
     * 返回字符串长度
     * @param str
     * @param isJudgeLetter（是否一个中文算两个字符）
     * @return
     */
    public static int getStrLength(String str, boolean isJudgeLetter) {
        if (str == null)
            return 0;
        char[] c = str.toCharArray();
        int len = 0;
        for (int i = 0; i < c.length; i++) {
            len++;
            if (isJudgeLetter) {
                if (!isLetter(c[i])) {
                    len++;
                }
            }
        }
        return len;
    }

    public static boolean isLetter(char c) {
        int k = 0x80;
        return c / k == 0 ? true : false;
    }
    
    public static String getHostFromURL(String url){
		String host = "";
		Pattern p = Pattern.compile("(?<=//|)((\\w)+\\.)+\\w+");  
        Matcher matcher = p.matcher(url);  
        if (matcher.find()) {  
            host = matcher.group();  
        }  
		return host; 
	}

    /**
     * 按长度分割字符串
     * @param s
     * @param len
     */
    public static String[] spriltStringByLength(String str, int len){
    	// keyword.split("(?<=\\G.{5})") 以5为长度分割字符串为数组
		String [] result = str.split("(?<=\\G.{" + len + "})");
		return result;
	}

}
