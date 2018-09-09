package com.yintong.erp.dto;

import com.yintong.erp.domain.prod.ErpProdOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.Data;

/**
 * @author lucifer.chan
 * @create 2018-09-07 下午4:59
 * 生产制令单-dto
 **/
@Data
public class ProdOrderDto {
    private ErpProdOrder order;

    private List<BomDto> boms;

    private List<MouldDto> moulds;

    public List<BomDto> getBoms(){
        return Objects.isNull(boms) ? new ArrayList<>() : boms;
    }

    public List<MouldDto> getMoulds(){
        return Objects.isNull(moulds) ? new ArrayList<>() : moulds;
    }
}
