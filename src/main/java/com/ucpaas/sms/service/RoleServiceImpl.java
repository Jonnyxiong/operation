package com.ucpaas.sms.service;

import com.jsmsframework.common.dto.ResultVO;
import com.ucpaas.sms.entity.message.Menu;
import com.ucpaas.sms.entity.message.Role;
import com.ucpaas.sms.exception.AuthorityAssignException;
import com.ucpaas.sms.mapper.message.RoleMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class RoleServiceImpl implements RoleService {
	private static final Logger LOGGER = LoggerFactory.getLogger(RoleServiceImpl.class);
	@Autowired
	private RoleMapper roleMapper;

	@Override
	@Transactional("message")
	public List<Role> queryRoleList(String webId) {
		return this.roleMapper.queryRoleList(webId);
	}

	@Override
	public List<Menu> queryRoleMenuList(Map params) {
		return this.roleMapper.queryRoleMenuList(params);
	}

	@Override
	public Role getById(Integer id) {
		Role role = this.roleMapper.getById(id);
		return role;
	}
	

    @Override
	@Transactional("message")
    public List<Menu> queryMenuList(String webId) {

    	 List<Menu> menus =  this.roleMapper.queryMenuList(webId);
    	 Iterator<Menu> mit = menus.iterator();
    	 
    	 while(mit.hasNext()){
    		 Menu menu = mit.next();
    		 if(menu.getMenuUrl()!=null&&menu.getMenuUrl().equals("/opsplatform/index/view")){ //运营平台首页不展示出来
    			 mit.remove();
    		 }
    		 if(menu.getWebId()!=null&&menu.getWebId().equals(2))
    			 menu.setMenuName(menu.getMenuName()+"(代理商平台)");
    		 if(menu.getWebId()!=null&&menu.getWebId().equals(3))
    			 menu.setMenuName(menu.getMenuName()+"          ");
    		 if(menu.getWebId()!=null&&menu.getWebId().equals(4))
    			 menu.setMenuName(menu.getMenuName()+"(OEM代理商平台)");
    	 }
    	 return menus;
    }

	@Override
	@Transactional("message")
	public ResultVO update(Role role) {
        int i = this.roleMapper.checkRoleName(role);
        if(i > 0)
            return ResultVO.failure("角色名称已存在，请更换后再试...");
        int update = this.roleMapper.update(role);
        int countRoleMenu = this.roleMapper.countRoleMenu(role.getId()); //角色对应的菜单数
        int delRoleMenu = this.roleMapper.deleteRoleMenu(role.getId());
        if(delRoleMenu != countRoleMenu || update < 1){
            LOGGER.debug("更新数量 --->{}< \r\n 角色对应的菜单数-------->{}< 实际删除的菜单(删除关联关系)-------->{}< ",update,countRoleMenu,delRoleMenu);
            throw new AuthorityAssignException("\r\n 更新用户信息失败");
        }

        if (role.getMenu() == null || role.getMenu().size() < 1) {
            return ResultVO.successDefault(role);
        }
        int insertRoleMenu = this.roleMapper.insertRoleMenu(role);
        if(insertRoleMenu < 1)
            return ResultVO.failure("修改角色失败！请稍后再试...");
        else if(insertRoleMenu == role.getMenu().size())
            return ResultVO.successDefault(role);
        else
            throw new AuthorityAssignException("应当添加菜单数---->" + role.getMenu().size() + "\r\n 实际添加数-------->" + insertRoleMenu);
	}

}
