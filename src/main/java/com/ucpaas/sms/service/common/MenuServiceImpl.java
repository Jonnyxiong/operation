package com.ucpaas.sms.service.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ucpaas.sms.entity.message.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ucpaas.sms.dao.MessageMasterDao;
import com.ucpaas.sms.model.Menu;
import com.ucpaas.sms.util.web.AuthorityUtils;

/**
 * 菜单业务
 * 
 * @author xiejiaan
 */
@Service
@Transactional
public class MenuServiceImpl implements MenuService {
	@Autowired
	private MessageMasterDao masterDao;
	public static final ConcurrentHashMap<Long, String> titleMap = new ConcurrentHashMap<Long, String>();

	@Override
	public List<Menu> getHeaderMenu(Integer roleId) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("roleId", roleId);
		List<Menu> menus = masterDao.selectList("menu.getMenuList", params);
		return menus;
	}

	@Override
	public List<Menu> getHeaderMenu(List<Role> roles) {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("roles", roles);
		List<Menu> menus = masterDao.selectList("menu.getMenuList", params);
		return menus;
	}

	public static final Menu toTree(List<Menu> menus) {
		Menu result = new Menu();
		result.setMenu_id(0L);
		Map<Long, Menu> map = new HashMap<Long, Menu>();
		map.put(0l, result);
		Long mid = null;
		if (null != menus && !menus.isEmpty()) {
			Menu tmp = null;
			List<Menu> noadd = new ArrayList<Menu>();
			for (Menu menu : menus) {
				map.put(menu.getMenu_id(), menu);
				tmp = map.get(menu.getParent_id());
				if (null != tmp) {
					tmp.addSubMenu(menu);
				} else {
					noadd.add(menu);
				}
			}
			for (Menu menu : noadd) {
				tmp = map.get(menu.getParent_id());
				if (null != tmp) {
					tmp.addSubMenu(menu);
				}
			}
			StringBuffer sb = new StringBuffer();
			boolean isLast = false;
			for (Menu menu : menus) {
				mid = menu.getMenu_id();
				if (!titleMap.containsKey(mid)) {
					sb.setLength(0);
					tmp = menu;
					isLast = true;
					do {
						sb.insert(0, "<a " + (isLast ? "class=\"current\"" : "") + " >" + tmp.getMenu_name() + "</a>");
						isLast = false;
					} while (null != (tmp = map.get(tmp.getParent_id())) && !result.equals(tmp));
					titleMap.put(menu.getMenu_id(), sb.toString());
				}
			}

		}
		return result;
	}
}
