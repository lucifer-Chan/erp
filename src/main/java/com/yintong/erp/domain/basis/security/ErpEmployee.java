package com.yintong.erp.domain.basis.security;

import com.yintong.erp.domain.basis.ErpBaseDepartment;
import com.yintong.erp.domain.basis.ErpBaseDepartmentRepository;
import com.yintong.erp.domain.basis.associator.ErpEmployeeDepartment;
import com.yintong.erp.domain.basis.associator.ErpEmployeeDepartmentRepository;
import com.yintong.erp.utils.bar.BarCode;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import com.yintong.erp.utils.common.SpringUtil;
import com.yintong.erp.utils.transform.IgnoreIfNull;
import com.yintong.erp.utils.transform.IgnoreWhatever;
import lombok.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.UE00;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午5:20
 * 员工实体类
 **/
@Entity
@BarCode(prefix = UE00)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpEmployee extends BaseEntityWithBarCode {
    @Id
    @GeneratedValue
    private Long id;

    @Column(columnDefinition = "varchar(40) comment '登录名'")
    private String loginName;

    @Column(columnDefinition = "varchar(40) comment '姓名'")
    private String name;

    @Column(columnDefinition = "varchar(100) comment '密码-加密'")
    @IgnoreWhatever
    private String password;

    @Column(columnDefinition = "varchar(40) comment '电话号码'")
    private String mobile;

    @Column(columnDefinition = "varchar(100) comment '微信小程序的openId'")
    private String openId;

    @Override
    protected void prePersist(){
        if(StringUtils.hasLength(password))
            this.password = new BCryptPasswordEncoder().encode(password.trim());
    }

    @Transient @IgnoreIfNull
    private List<Long> departmentIds;

    @Transient @IgnoreIfNull
    private List<String> menuCodes;

    @Transient
    private String departmentNames;

    @Transient
    private String menuNames;

    @Transient
    private String status;//1-有密码；0-无密码

    public String getStatus(){
        if(StringUtils.hasLength(status)) return status;
        status = StringUtils.hasLength(password) ? "1" : "0";
        return status;
    }

    public List<Long> getDepartmentIds(){
        if(Objects.isNull(id)) return null;
        if(!CollectionUtils.isEmpty(departmentIds)) return departmentIds;
        try {
            departmentIds = SpringUtil.getBean(ErpEmployeeDepartmentRepository.class).findByEmployeeId(id).stream()
                    .map(ErpEmployeeDepartment::getDepartmentId)
                    .collect(Collectors.toList());
        } catch (Exception e){
            e.printStackTrace();
        }
        return departmentIds;
    }

    public String getDepartmentNames(){
        if(Objects.isNull(id)) return "";
        if(StringUtils.hasLength(departmentNames)) return departmentNames;
        try {
            List<String> names =SpringUtil.getBean(ErpBaseDepartmentRepository.class).findByIdIn(getDepartmentIds()).stream()
                    .map(ErpBaseDepartment::getName)
                    .collect(Collectors.toList());
            departmentNames = StringUtils.collectionToCommaDelimitedString(names);
        } catch (Exception e){
            e.printStackTrace();
        }
        return departmentNames;
    }

    public List<String> getMenuCodes(){
        if(Objects.isNull(id)) return null;
        if(!CollectionUtils.isEmpty(menuCodes)) return menuCodes;
        try {
            menuCodes = SpringUtil.getBean(ErpEmployeeMenuRepository.class).findByEmployeeId(id).stream()
                    .map(ErpEmployeeMenu::getMenuCode)
                    .collect(Collectors.toList());
        } catch (Exception e){
            e.printStackTrace();
        }
        return menuCodes;
    }

    public String getMenuNames(){
        if(Objects.isNull(id)) return "";
        if(StringUtils.hasLength(menuNames)) return menuNames;
        try{
            List<String> names = SpringUtil.getBean(ErpMenuRepository.class).findByCodeInOrderByCode(getMenuCodes()).stream()
                    .map(ErpMenu::getName)
                    .collect(Collectors.toList());
            menuNames = StringUtils.collectionToCommaDelimitedString(names);
        } catch (Exception e){
            e.printStackTrace();
        }
        return menuNames;
    }
}
