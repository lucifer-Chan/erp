package com.yintong.erp.domain.basis.associator;

import com.yintong.erp.utils.base.BaseEntity;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * @author lucifer.chan
 * @create 2018-05-13 上午12:03
 **/

@Entity
@Getter @Setter
public class ErpEmployeeDepartment extends BaseEntity{
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long employeeId;

    private Long departmentId;
}
