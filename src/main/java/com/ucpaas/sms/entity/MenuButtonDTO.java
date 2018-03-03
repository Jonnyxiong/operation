package com.ucpaas.sms.entity;

import java.io.Serializable;

/**
 * 菜单按钮返回对象
 *
 * @outhor tanjiangqiang
 * @create 2018-01-04 21:48
 */
public class MenuButtonDTO implements Serializable {
    private Integer menuId;
    private String menuName;
    private Boolean authority;

    public Integer getMenuId() {
        return menuId;
    }

    public void setMenuId(Integer menuId) {
        this.menuId = menuId;
    }

    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    public Boolean getAuthority() {
        return authority;
    }

    public void setAuthority(Boolean authority) {
        this.authority = authority;
    }
}