package com.yintong.erp.web;

import com.yintong.erp.domain.basis.ErpBaseRawMaterial;
import com.yintong.erp.domain.basis.ErpBaseRawMaterialRepository;
import com.yintong.erp.domain.basis.ErpBaseSupplier;
import com.yintong.erp.domain.basis.ErpBaseSupplierRepository;
import com.yintong.erp.domain.basis.associator.ErpRawMaterialSupplier;
import com.yintong.erp.domain.basis.associator.ErpRawMaterialSupplierRepository;
import com.yintong.erp.domain.stock.ErpStockPlace;
import com.yintong.erp.domain.stock.ErpStockPlaceRepository;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.yintong.erp.utils.common.Constants.StockPlaceStatus.ON;

/**
 * 后门
 *
 * @author lucifer.chan
 * @create 2019-03-27 上午9:56
 **/
@RestController
@RequestMapping("bd")
@Slf4j
public class BackDoorController {

    @Autowired ErpStockPlaceRepository stockPlaceRepository;

    @Autowired ErpRawMaterialSupplierRepository materialSupplierRepository;

    @Autowired ErpBaseRawMaterialRepository materialRepository;

    @Autowired ErpBaseSupplierRepository supplierRepository;

    @GetMapping("material/place/sync")
    public Object syncMaterialPlace(){

        //已经存在的原材料仓位的关联id
        Set<Long> existsAssId = stockPlaceRepository.findByStockPlaceType("M").stream()
                .filter(it -> Objects.nonNull(it.getMaterialSupplierAssId()))
                .map(ErpStockPlace::getMaterialSupplierAssId).collect(Collectors.toSet());

        //需要创建的关联id
        List<ErpRawMaterialSupplier> toCreateAssList = materialSupplierRepository.findAll().stream()
                .filter(it -> !existsAssId.contains(it.getId()))
                .collect(Collectors.toList());

        toCreateAssList.forEach(it -> {
            ErpBaseRawMaterial material = materialRepository.findById(it.getRawMaterId()).orElse(null);
            ErpBaseSupplier supplier = supplierRepository.findById(it.getSupplierId()).orElse(null);

            if(Objects.nonNull(material) && Objects.nonNull(supplier)){
                String name = supplier.getSupplierName() + "【" + material.getDescription() + "】";
                String type = material.getRawTypeCode().substring(0, 2);
                //上限默认到Cu类的，重量默认做到1000kg，  Ag类 默认到200kg，
                Integer upperLimit = type.startsWith("MA") ? 200 : 1000;

                ErpStockPlace place = stockPlaceRepository.save(ErpStockPlace.builder()
                        .stockPlaceType("M")
                        .materialName(name)
                        .name(name)
                        .lowerLimit(0)
                        .upperLimit(upperLimit)
                        .materialSupplierAssId(it.getId())
                        .statusCode(ON.name())
                        .closeValidate(true)
                    .build());
                log.info("保存仓位：[{}] 成功", place.getBarCode());
            }

        });
        return "ok";
    }
}