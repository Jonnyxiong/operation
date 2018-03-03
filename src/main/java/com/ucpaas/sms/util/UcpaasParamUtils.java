package com.ucpaas.sms.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.ucpaas.sms.util.cache.InnerCache;

public class UcpaasParamUtils {
//	private static TagService tagService;
//
//	@Resource
//	public void setTagService(TagService tagService) {
//		if(null == UcpaasParamUtils.tagService){
//			UcpaasParamUtils.tagService = tagService;
//		}
//	}

	final static Map<String, Long> loadTime = new HashMap<String, Long>();
	final static long LOAD_TIME_MS = 3600 * 1000;// 一小时

	final static ConcurrentHashMap<String, ConcurrentHashMap<String, String>> cache = new ConcurrentHashMap<String, ConcurrentHashMap<String, String>>();

//	public static final String get(String type, String key) {
//		String result = null;
//		if (!loadTime.containsKey(type)) {
//			loadTime.put(type, 0L);
//		}
//		long a = loadTime.get(type);
//		long now = System.currentTimeMillis();
//		if (a < now) {
//			if (!cache.containsKey(type)) {
//				load(type);
//			}
//		}
//		ConcurrentHashMap<String, String> tmp = cache.get(type);
//		if (null != tmp) {
//			result = tmp.get(key);
//		}
//		return result;
//	}

//	private static void load(String type) {
//		Map<String, Object> sqlParams = new HashMap<String, Object>();
//		List<Map<String, Object>> tmp = tagService.getTagDataForDictionary(type, sqlParams);
//		ConcurrentHashMap<String, String> cc = new ConcurrentHashMap<String, String>();
//		String key = null;
//		for (Map<String, Object> map : tmp) {
//			key = String.valueOf(map.get("value"));
//			if (!StringUtils.isEmpty(key)) {
//				cc.put(key, String.valueOf(map.get("text")));
//			}
//		}
//		cache.put(type, cc);
//		loadTime.put(type, System.currentTimeMillis() + LOAD_TIME_MS);
//	}

	public static final void remove(String type) {
		cache.remove(type);
		loadTime.put(type, 0L);
	}

//	public static final void reload(String type) {
//		load(type);
//	}
//	
	
//	private static InnerCache<String,String> cityCache = null;
//	public static String getCity(String key) {
//		if(cityCache == null){
//			synchronized (UcpaasParamUtils.class) {
//				if(cityCache == null){
//					cityCache = new InnerCache<String, String>(60000){
//						protected String fetch(String key) {
//							Map<String, Object> params= new HashMap<String, Object>();
//							params.put("area_id", key);
//							return tagService.getCityNameByAreaId(params);
//						}
//					};
//				}
//			}
//		}
//		return cityCache.get(key);
//	}
}
