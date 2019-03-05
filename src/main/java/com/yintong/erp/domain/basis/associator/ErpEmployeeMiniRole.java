package com.yintong.erp.domain.basis.associator;

import com.yintong.erp.utils.base.BaseEntity;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 人员和小程序的关联
 *
 * @author lucifer.chan
 * @create 2019-03-04 下午5:29
 **/
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
public class ErpEmployeeMiniRole extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;

    private Long userId;

    private String miniRoleCode;
}
