package com.yintong.erp.domain.basis.provider;

import com.yintong.erp.domain.basis.baseCommon.ErpBaseCustCommon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by jianqiang on 2018/5/10 0010.、
 * 供应商
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ErpBaseProvider extends ErpBaseCustCommon {

    @Id
    @GeneratedValue
    private Long id;
}
