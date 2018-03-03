package com.ucpaas.sms.util.cache;

import java.util.concurrent.ConcurrentHashMap;

public class InnerCache<K, V> {
	final ConcurrentHashMap<K, Long> loadTime = new ConcurrentHashMap<K, Long>();
	final ConcurrentHashMap<K, V> cache = new ConcurrentHashMap<K, V>();
	final long LOAD_TIME_MS ;// 一小时
	public InnerCache() {
		this(3600 * 1000);
	}
	public InnerCache(long LOAD_TIME_MS) {
		if(0 < LOAD_TIME_MS){
			this.LOAD_TIME_MS = LOAD_TIME_MS;
		}else{
			this.LOAD_TIME_MS = 3600 * 1000;
		}
	}
	

	public final V get(K key) {
		V result = null;
		if (!loadTime.containsKey(key)) {
			loadTime.put(key, 0L);
		}
		long a = loadTime.get(key);
		long now = System.currentTimeMillis();
		if (a < now) {
			if (!cache.containsKey(key)) {
				load(key);
			}
		}
		result = cache.get(key);
		return result;
	}

	private void load(K key) {
		V value = fetch(key);
		if (null != value) {
			cache.put(key, value);
			loadTime.put(key, System.currentTimeMillis() + LOAD_TIME_MS);
		}
	}

	protected V fetch(K key) {
		return null;
	}

	public final void remove(K key) {
		cache.remove(key);
		loadTime.put(key, 0L);
	}

	public final void reload(K type) {
		load(type);
	}
}
