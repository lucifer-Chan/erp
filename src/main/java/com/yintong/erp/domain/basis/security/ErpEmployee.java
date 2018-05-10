package com.yintong.erp.domain.basis.security;

import com.yintong.erp.utils.bar.BarCode;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import lombok.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.*;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午5:20
 * 员工实体类
 **/
@Entity
@BarCode(UE00)
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
    private String password;
    @Column(columnDefinition = "varchar(40) comment '电话号码'")
    private String mobile;

    @Override
    protected void prePersist(){
        if(StringUtils.hasLength(password))
            this.password = new BCryptPasswordEncoder().encode(password.trim());
    }
}
