package org.jvue.upms.service.impl;

import org.jvue.upms.bean.JvPermission;
import org.jvue.upms.mapper.JvPermissionMapper;

import org.jvue.upms.service.JvPermissionService;
import org.springframework.security.access.ConfigAttribute;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;

/**
 * 不出现sql代码
 */
@Service
public class JvPermissionServiceImpl implements JvPermissionService {

    @Resource(name = "jvPermissionMapper")
    private JvPermissionMapper jvPermissionMapper;

    @Override
    public List<JvPermission> list() {
       /* List<JvPermission> list = formatTree(jvPermissionMapper.list());*/
        return jvPermissionMapper.list();
    }

    @Override
    public List<JvPermission> listPermissionByUser(Integer userId) {
        List<JvPermission> list = jvPermissionMapper.listPermissionByUser(userId);
        return formatTree(list);
    }

    @Override
    public List<JvPermission> listPermissionByRole(Integer roleId) {
        List<JvPermission> list = jvPermissionMapper.listPermissionByRole(roleId);
        return formatTree(list);
    }

    @Override
    public Map<RequestMatcher, Collection<ConfigAttribute>> cacheRequestMap() {
        List<JvPermission> list = jvPermissionMapper.cacheRequestMap();
        Map<String, Collection<ConfigAttribute>> map = new HashMap<>();
        Map<RequestMatcher, Collection<ConfigAttribute>> requestMap = new HashMap<>();
        if (list != null){
            for (JvPermission jvPermission : list) {
                Set <ConfigAttribute>roleSet=(Set <ConfigAttribute>)(Object)jvPermission.getRoleSet();
                /*可能存在一个类型的权限配置了多个菜单，*/
                if(map.get(jvPermission.getPermissionValue())!=null){
                    map.get(jvPermission.getPermissionValue()).addAll(roleSet);
                }else{
                    map.put(jvPermission.getPermissionValue(), roleSet);
                }
            }
            for(String rule:map.keySet()){
                if(rule==null||rule.equals("")){
                    continue;
                }
                RequestMatcher requestMatcher=new AntPathRequestMatcher(rule);
                requestMap.put(requestMatcher,map.get(rule));

            }
        }
        return requestMap;
    }

    private List<JvPermission> formatTree(List<JvPermission> datalist) {
        Iterator<JvPermission> iterator = datalist.iterator();
        List<JvPermission> topJvPermission = new ArrayList<JvPermission>();
        while (iterator.hasNext()) {
            JvPermission s = iterator.next();
            //去掉级联关系后需要手动维护这个属性
            boolean getParent = false;
            for (JvPermission p : datalist) {
                if (p.getPermissionId().equals(s.getPid())) {
                    List sunList = p.getChildren();
                    if (sunList == null) {
                        p.setChildren(new ArrayList());
                    }
                    p.getChildren().add(s);
                    getParent = true;
                    break;
                }
            }
            if (!getParent) {
                topJvPermission.add(s);
            }

        }
        return topJvPermission;
    }

}
