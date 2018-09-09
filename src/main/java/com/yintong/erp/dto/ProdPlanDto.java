package com.yintong.erp.dto;

import com.yintong.erp.domain.prod.ErpProdPlan;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Data;

/**
 * @author lucifer.chan
 * @create 2018-09-04 上午9:06
 * 生产计划单-dto
 **/
@Data
public class ProdPlanDto {
    private ErpProdPlan plan;

    private List<BomDto> boms;

    public List<BomDto> getBoms(){
        return Objects.isNull(boms) ? new ArrayList<>() : boms;
    }


}
