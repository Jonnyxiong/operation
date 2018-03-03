package com.ucpaas.sms.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 分页类
 *
 * @author jevoncode
 * @create 2017-01-02-21:05
 */
/**
 * @author huangwenjie
 *
 * @param <T>
 */
public class Page<T> {
	private static final Logger logger = LoggerFactory.getLogger(Page.class);

	public static String DEFAULT_PAGESIZE = "10";
	private int pageNo; // 当前页码
	private int pageSize; // 每页行数
	private int totalRecord; // 总记录数
	private int totalPage; // 总页数
	private Map<String, Object> params; // 查询条件
	private List<T> data;
	private List<Map<String,Object>> otherData;
	private Map<String,Object> totalOtherData;//报表合计数据
	private String orderByClause; // 排序条件

	public Page() {
		pageNo = 1;
		pageSize = Integer.valueOf(DEFAULT_PAGESIZE);
		totalRecord = 0;
		totalPage = 0;
		params = new HashMap<>();
	}

	public Page(int pageNo, int pageSize) {
		this.pageNo = pageNo;
		this.pageSize = pageSize;
		totalRecord = 0;
		totalPage = 0;
		params = new HashMap<>();
	}

	/**
	 * 总件数变化时，更新总页数并计算显示样式
	 */
	private void refreshPage() {
		// 总页数计算
		totalPage = totalRecord % pageSize == 0 ? totalRecord / pageSize : (totalRecord / pageSize + 1);
		// 防止超出最末页（浏览途中数据被删除的情况）
		if (pageNo > totalPage && totalPage != 0) {
			pageNo = totalPage;
		}
	}

	public int getPageNo() {
		return pageNo;
	}

	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotalRecord() {
		return totalRecord;
	}

	public void setTotalRecord(int totalRecord) {
		this.totalRecord = totalRecord;
		refreshPage();
	}

	public int getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

	public List<Map<String, Object>> getOtherData() {
		return otherData;
	}

	public void setOtherData(List<Map<String, Object>> otherData) {
		this.otherData = otherData;
	}

	public Map<String, Object> getTotalOtherData() {
		return totalOtherData;
	}

	public void setTotalOtherData(Map<String, Object> totalOtherData) {
		this.totalOtherData = totalOtherData;
	}

	public String getOrderByClause() {
		return orderByClause;
	}

	public void setOrderByClause(String orderByClause) {
		this.orderByClause = orderByClause;
	}

}
