package com.ucpaas.sms.entity.message;


import org.apache.ibatis.type.Alias;

@Alias("Menu")
public class Menu {
    
    // 菜单ID
    private Integer menuId;
    // 菜单名称
    private String menuName;
    // 备注
    private String remark;
    // 菜单路径
    private String menuUrl;
    // 菜单样式
    private String menuClass;
    // 菜单类型，1:正常菜单，2:功能点
    private String menuType;
    // 级别，从1开始
    private Integer level;
    // 父菜单ID，第1级为0，使用,分割
    private String parentId;
    // 排序
    private Integer sort;
    // 状态：1生效，0失效
    private String status;
    // web应用ID，1：短信调度系统 ，2：代理商平台，3：运营平台，4：OEM代理商平台，5：云运营平台
    private Integer webId;
    // 当前用户是否用户菜单权限,有 "checked" ,无 ""
    private String checked;
    
    public Menu() {
	}

    public Menu(int menuId) {
    	this.menuId = menuId;
    }
    
	public Integer getMenuId() {
        return menuId;
    }
    
    public void setMenuId(Integer menuId) {
        this.menuId = menuId ;
    }
    
    public String getMenuName() {
        return menuName;
    }
    
    public void setMenuName(String menuName) {
        this.menuName = menuName ;
    }
    
    public String getRemark() {
        return remark;
    }
    
    public void setRemark(String remark) {
        this.remark = remark ;
    }
    
    public String getMenuUrl() {
        return menuUrl;
    }
    
    public void setMenuUrl(String menuUrl) {
        this.menuUrl = menuUrl ;
    }
    
    public String getMenuClass() {
        return menuClass;
    }
    
    public void setMenuClass(String menuClass) {
        this.menuClass = menuClass ;
    }
    
    public String getMenuType() {
        return menuType;
    }
    
    public void setMenuType(String menuType) {
        this.menuType = menuType ;
    }
    
    public Integer getLevel() {
        return level;
    }
    
    public void setLevel(Integer level) {
        this.level = level ;
    }
    
    public String getParentId() {
        return parentId;
    }
    
    public void setParentId(String parentId) {
        this.parentId = parentId ;
    }
    
    public Integer getSort() {
        return sort;
    }
    
    public void setSort(Integer sort) {
        this.sort = sort ;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status ;
    }
    
    public Integer getWebId() {
        return webId;
    }
    
    public void setWebId(Integer webId) {
        this.webId = webId ;
    }

	public String getChecked() {
		return checked;
	}

	public void setChecked(String checked) {
		this.checked = checked;
	}
	

	@Override
	public String toString() {
		return "Menu [menuId=" + menuId + ", menuName=" + menuName
				+ ", remark=" + remark + ", menuUrl=" + menuUrl + ", menuClass=" + menuClass + ", menuType=" + menuType
				+ ", level=" + level + ", parentId=" + parentId + ", sort=" + sort + ", status=" + status + ", webId="
				+ webId + ", checked=" + checked + "]";
	}

    
    
}