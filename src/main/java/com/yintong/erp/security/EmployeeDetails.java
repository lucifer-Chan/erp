package com.yintong.erp.security;

import com.yintong.erp.domain.basis.security.ErpEmployee;
import com.yintong.erp.domain.basis.security.ErpEmployeeMenuRepository;
import com.yintong.erp.utils.base.BaseEntity;
import com.yintong.erp.utils.common.Constants;
import com.yintong.erp.utils.common.SpringUtil;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午3:53
 **/
@Data
public class EmployeeDetails implements UserDetails, Constants.Roles{

    private ErpEmployee employee;

    private List<SimpleGrantedAuthority> authorities;

    private ErpEmployeeMenuRepository repository = SpringUtil.getBean(ErpEmployeeMenuRepository.class);

    public EmployeeDetails(ErpEmployee employee){
        this.employee = employee;
    }

    /**
     * 获取当前用户的所有权限
     * @return
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if(CollectionUtils.isEmpty(authorities)) {
            authorities = repository.findByEmployeeId(employee.getId()).stream()
                .map(ass -> new SimpleGrantedAuthority("ROLE_" + ass.getMenuCode()))
                .collect(toList());
            authorities.add(new SimpleGrantedAuthority("ROLE_" + ANY_ROLE_CODE));
        }
        return authorities;
    }

    @Override
    public String getPassword() {
        return employee.getPassword();
    }

    @Override
    public String getUsername() {
        return employee.getLoginName();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return BaseEntity.ENABLED == employee.getIsDel();
    }

    public boolean isAdmin(){
        return getAuthorities().stream().anyMatch(authority->authority.getAuthority().contains(ADMIN_ROLE_CODE));
    }

}
