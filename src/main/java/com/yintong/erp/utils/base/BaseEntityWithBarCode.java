package com.yintong.erp.utils.base;

import com.yintong.erp.utils.bar.BarCodeColumn;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

/**
 * @author lucifer.chan
 * @create 2018-05-10 下午3:42
 * 带条形码字段的实体基类
 **/
@MappedSuperclass
@Getter @Setter
public class BaseEntityWithBarCode extends BaseEntity{
    @Column(columnDefinition = "varchar(100) comment '条形码'")
    @BarCodeColumn
    private String barCode;
}
