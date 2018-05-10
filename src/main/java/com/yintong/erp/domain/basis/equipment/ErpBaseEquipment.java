package com.yintong.erp.domain.basis.equipment;

import com.yintong.erp.domain.basis.baseCommon.ErpBaseCommon;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by jianqiang on 2018/5/10 0010.
 * 设备表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ErpBaseEquipment extends ErpBaseCommon {

    @Id
    @GeneratedValue
    private Long id;
}
