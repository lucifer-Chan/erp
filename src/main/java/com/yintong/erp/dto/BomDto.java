package com.yintong.erp.dto;

import com.yintong.erp.domain.basis.ErpBaseRawMaterial;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author lucifer.chan
 * @create 2018-09-06 下午12:09
 * 物料清单Dto
 **/
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class BomDto {

    //原材料详细
    private ErpBaseRawMaterial material;

    private Long id;

    private Long supplierId;

    private String supplierName;

    private Long materialId;

    //切丝长度
//    private Double materialNum;
    private String materialNum;


    //耗银|耗铜 总数
    private Double num;
}