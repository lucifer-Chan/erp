package com.yintong.erp.domain.stock;

import com.yintong.erp.utils.common.SpringUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;

/**
 * @author lucifer.chan
 * @create 2018-08-23 上午12:11
 * 查找仓位
 **/
public interface StockPlaceFinder {
    /**
     * 获取仓位
     * @param productIds 成品id列表,英文逗号隔开
     * @param materialIds 原材料id列表,英文逗号隔开
     * @return
     */
    default List<ErpStockPlace> getPlaces(String productIds, String materialIds){
        return findPlaces(productIds, materialIds);
    }

    static List<ErpStockPlace> findPlaces(String productIds, String materialIds){
        List<ErpStockPlace> result = new ArrayList<>();

        if(StringUtils.isEmpty(productIds) && StringUtils.isEmpty(materialIds)) return result;
        //成品
        if(StringUtils.hasText(productIds)){
            List<Long> _productIds = Arrays.stream(productIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
            //仓位id列表
            List<Long> placeIds = SpringUtil.getBean(ErpStockOptLogRepository.class)
                    .findByProductIdIn(_productIds).stream().map(ErpStockOptLog::getStockPlaceId).collect(Collectors.toList());
            result.addAll(SpringUtil.getBean(ErpStockPlaceRepository.class).findByIdIn(placeIds));

        }
        //原材料
        if(StringUtils.hasText(materialIds)){
            List<Long> _materialIds = Arrays.stream(materialIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
            result.addAll(SpringUtil.getBean(ErpStockPlaceRepository.class).findByMaterialSupplierAssIdIn(_materialIds));
        }

        return result;
    }

    /**
     * 查找模具的仓位
     * @param moudlIds
     * @return
     */
    static List<ErpStockPlace> findMouldPlaces(String moudlIds){
        if(StringUtils.isEmpty(moudlIds)) return Collections.emptyList();
        List<Long> _moudlIds = Arrays.stream(moudlIds.split(",")).map(Long::parseLong).collect(Collectors.toList());
        //仓位id列表
        List<Long> placeIds = SpringUtil.getBean(ErpStockOptLogRepository.class)
                .findByMouldIdIn(_moudlIds).stream().map(ErpStockOptLog::getStockPlaceId).collect(Collectors.toList());

        return SpringUtil.getBean(ErpStockPlaceRepository.class).findByIdIn(placeIds);
    }
}
