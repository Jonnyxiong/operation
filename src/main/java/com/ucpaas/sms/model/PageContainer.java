package com.ucpaas.sms.model;

import java.io.Serializable;
import java.util.*;

/**
 * 分页查询容器
 * 
 * @author xiejiaan
 */
public class PageContainer<T> implements Serializable {
	private static final long serialVersionUID = 8976331697912229579L;

	private List<T> entityList = new ArrayList<T>();// 查询结果

	private List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();// 查询结果
	private Integer currentPage = 1; // 当前页号
	private Integer totalPage = 1; // 总页数
	private Integer totalCount = 0;// 总行数
	private Integer pageRowCount = 30; // 默认每页显示行数
	private Integer[] pageRowArray = { 10, 30, 50, 100 };// 每页显示行数下拉框
	private Double balanceTotal=0.00;
	private Double commissionTotal=0.00;
	private Double rebateTotal=0.00;
	private Double depositTotal=0.00;
	private Double amountTotal=0.00;
	private Map<String,String> totals1=new HashMap<String,String>();

	public List<Map<String, Object>> getList() {
		return list;
	}

	public void setList(List<Map<String, Object>> list) {
		this.list = list;
	}

	public Integer getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(Integer currentPage) {
		this.currentPage = currentPage;
	}

	public Integer getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(Integer totalPage) {
		this.totalPage = totalPage;
	}

	public Integer getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(Integer totalCount) {
		this.totalCount = totalCount;
	}

	public Integer getPageRowCount() {
		return pageRowCount;
	}

	public void setPageRowCount(Integer pageRowCount) {
		this.pageRowCount = pageRowCount;
	}

	public Integer[] getPageRowArray() {
		return pageRowArray;
	}

	public void setPageRowArray(Integer[] pageRowArray) {
		this.pageRowArray = pageRowArray;
	}


	public List<T> getEntityList() {
		return entityList;
	}

	public void setEntityList(List<T> entityList) {
		this.entityList = entityList;
	}


	public Map<String, String> getTotals1() {
		return totals1;
	}

	public void setTotals1(Map<String, String> totals1) {
		this.totals1 = totals1;
	}

	public Double getBalanceTotal() {
		return balanceTotal;
	}

	public void setBalanceTotal(Double balanceTotal) {
		this.balanceTotal = balanceTotal;
	}

	public Double getCommissionTotal() {
		return commissionTotal;
	}

	public void setCommissionTotal(Double commissionTotal) {
		this.commissionTotal = commissionTotal;
	}

	public Double getRebateTotal() {
		return rebateTotal;
	}

	public void setRebateTotal(Double rebateTotal) {
		this.rebateTotal = rebateTotal;
	}

	public Double getDepositTotal() {
		return depositTotal;
	}

	public void setDepositTotal(Double depositTotal) {
		this.depositTotal = depositTotal;
	}

	public Double getAmountTotal() {
		return amountTotal;
	}

	public void setAmountTotal(Double amountTotal) {
		this.amountTotal = amountTotal;
	}

	/**
	 * 重写toString方法
	 */
	@Override
	public String toString() {
		return "PageContainer [list=" + list + ", currentPage=" + currentPage + ", totalPage=" + totalPage
				+", balanceTotal=" + balanceTotal +", commissionTotal=" + commissionTotal +", rebateTotal=" + rebateTotal +
				", depositTotal=" + depositTotal +", amountTotal=" + amountTotal + ", totals1=" + totals1 + ", totalCount=" + totalCount + ", pageRowCount=" + pageRowCount + ", pageRowArray="
				+ Arrays.toString(pageRowArray) + "]";
	}
	
	
}
