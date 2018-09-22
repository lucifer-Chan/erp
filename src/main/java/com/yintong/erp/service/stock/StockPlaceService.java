package com.yintong.erp.service.stock;

import com.yintong.erp.domain.basis.associator.ErpRawMaterialSupplierRepository;
import com.yintong.erp.domain.stock.ErpStockOptLog;
import com.yintong.erp.domain.stock.ErpStockOptLogRepository;
import com.yintong.erp.domain.stock.ErpStockPlace;
import com.yintong.erp.domain.stock.ErpStockPlaceRepository;
import com.yintong.erp.utils.query.OrderBy;
import com.yintong.erp.utils.query.ParameterItem;
import com.yintong.erp.utils.query.QueryParameterBuilder;
import com.yintong.erp.validator.OnDeleteSupplierRawMaterialValidator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import net.sf.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import static com.yintong.erp.utils.common.Constants.StockPlaceStatus.ON;
import static com.yintong.erp.utils.common.Constants.StockPlaceStatus.STOP;
import static com.yintong.erp.utils.query.ParameterItem.COMPARES.equal;
import static com.yintong.erp.utils.query.ParameterItem.COMPARES.like;
import static javax.persistence.criteria.Predicate.BooleanOperator.AND;
import static javax.persistence.criteria.Predicate.BooleanOperator.OR;
import static com.yintong.erp.utils.common.Constants.*;


/**
 * @author lucifer.chan
 * @create 2018-08-04 下午9:53
 * 仓位服务
 **/
@Service
public class StockPlaceService implements OnDeleteSupplierRawMaterialValidator {
    @Autowired ErpStockPlaceRepository stockPlaceRepository;

    @Autowired ErpStockOptLogRepository stockOptLogRepository;

    @Autowired ErpRawMaterialSupplierRepository materialSupplierRepository;

    /**
     * 查找是否有原材料对应的供应商
     * @param id
     */
    @Override
    public void onDeleteSupplierRawMaterial(Long id) {
        String placeNames = stockPlaceRepository.findByMaterialSupplierAssId(id)
                .stream().map(ErpStockPlace::getName).collect(Collectors.joining(","));

        Assert.isTrue(!StringUtils.hasText(placeNames), "仓位[" + placeNames + "]关联了该原材料，请先删除仓位关联");
    }

    /**
     * 创建仓位
     * @param place
     * @return
     */
    public ErpStockPlace create(ErpStockPlace place){
        place.setId(null);
        place.setStatusCode(ON.name());
        Long assId = place.getMaterialSupplierAssId();
        if(Objects.nonNull(assId)){
            materialSupplierRepository.findById(assId).ifPresent(associator -> place.setMaterialSupplierBarCode(associator.getBarCode()));
        }
        return stockPlaceRepository.save(place);
    }

    /**
     * 更新仓位-库存上限、仓位描述
     * @param placeId
     * @param upperLimit
     * @param name
     * @param description
     * @return
     */
    public ErpStockPlace update(Long placeId, Integer upperLimit, String name, String description){
        ErpStockPlace place = one(placeId);
        place.setUpperLimit(upperLimit);
        place.setName(name);
        place.setDescription(description);
        return stockPlaceRepository.save(place);
    }

    /**
     * 停役仓位
     * @param placeId
     * @return
     */
    public ErpStockPlace stop(Long placeId){
        ErpStockPlace place = one(placeId);
        place.setStatusCode(STOP.name());
        return stockPlaceRepository.save(place);
    }

    /**
     * 删除仓位
     * @param placeId
     */
    public void delete(Long placeId){
        Assert.isTrue(CollectionUtils.isEmpty(stockOptLogRepository.findByStockPlaceId(placeId)), "当前仓位有出入库记录，无法删除");
        stockPlaceRepository.deleteById(placeId);
    }

    /**
     * 查找单个仓位
     * @param placeId
     * @return
     */
    public ErpStockPlace one(Long placeId){
        Assert.notNull(placeId, "仓位id不能为空");
        ErpStockPlace place = stockPlaceRepository.findById(placeId).orElse(null);
        Assert.notNull(place, "未找到id为[" + placeId + "]的仓位");
        return place;
    }

    /**
     * 根据条形码查找仓位
     * @param barCode
     * @return
     */
    public ErpStockPlace one(String barCode){
        ErpStockPlace place = stockPlaceRepository.findFirstByBarCode(barCode);
        Assert.notNull(place, "未找到编号为[" + barCode + "]的仓位");
        return place;
    }

    /**
     * 组合查询
     * @param parameters
     * @return
     */
    public Page<ErpStockPlace> query(PlaceParameterDto parameters) {
        return stockPlaceRepository.findAll(parameters.specification(), parameters.pageable());
    }

    /**
     * 仓位信息
     * @param placeId
     * @return history ： optHistory
     *         remain : 当前库存信息 （成品、模具、废品仓位时）
     */
    public Map<String, List<JSONObject>> placeExt(Long placeId){
        ErpStockPlace place = one(placeId);
        List<ErpStockOptLog> optHistory = optHistory(placeId);
        Map<String, List<JSONObject>> map = new HashMap<>();
        map.put("history", optHistory.stream().map(ErpStockOptLog::toJSONObject).collect(Collectors.toList()));

        StockPlaceType placeType = StockPlaceType.valueOf(place.getStockPlaceType());
        List<JSONObject> infoList = null;

        if(StockPlaceType.P == placeType){
            //成品
            infoList = collect(optHistory, log-> Objects.nonNull(log.getProductId()), ErpStockOptLog::getProductId, "productId", "productName");
        } else if(StockPlaceType.D == placeType){
            //模具
            infoList = collect(optHistory, log-> Objects.nonNull(log.getMouldId()), ErpStockOptLog::getMouldId, "mouldId", "mouldName");
        } else if(StockPlaceType.R == placeType){
            //废品
            infoList = collect(optHistory, log-> StringUtils.hasText(log.getRubbishName()), ErpStockOptLog::getRubbishName, "rubbishName");
        }

        if(null != infoList){
            map.put("remain", infoList);
        }
        return map;
    }

    /**
     * 根据模具barcode查找有库存的仓位
     * @param mouldBarCode
     * @return
     */
    public List<ErpStockPlace> findPlacesByMouldCode(String mouldBarCode){
        List<Long> placeIds = filterPlaceIdsOfLogs(stockOptLogRepository.findByMouldCode(mouldBarCode));
        return CollectionUtils.isEmpty(placeIds) ? new ArrayList<>() : stockPlaceRepository.findByIdIn(placeIds);
    }

    /**
     * 根据成品barcode查找有库存的仓位
     * @param productBarCode
     * @return
     */
    public List<ErpStockPlace> findPlacesByProductCode(String productBarCode){
        List<Long> placeIds = filterPlaceIdsOfLogs(stockOptLogRepository.findByProductCode(productBarCode));
        return CollectionUtils.isEmpty(placeIds) ? new ArrayList<>() : stockPlaceRepository.findByIdIn(placeIds);
    }


    /**
     * 从出入库日志中过滤出有库存的placeId
     * @param logs 通过barcode查询后同一类型的logs
     * @return
     */
    private List<Long> filterPlaceIdsOfLogs(List<ErpStockOptLog> logs){
        //根据placeId分组
        Map<Long, List<ErpStockOptLog>> longListMap = logs.stream().collect(Collectors.groupingBy(ErpStockOptLog::getStockPlaceId));

        return longListMap.entrySet().stream()
                .filter(entity -> {
                    Double sum = entity.getValue().stream().mapToDouble(log-> StockOpt.IN.name().equals(log.getOperation()) ? log.getNum() : -1 * log.getNum()).sum();
                    return sum > 0;
                })
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }


    /**
     * 仓位查询入参dto
     */
    @Getter@Setter
    @OrderBy(fieldName = "createdAt")
    public static class PlaceParameterDto extends QueryParameterBuilder {
        @ParameterItem(mappingTo = {"barCode", "name", "description", "materialName"}, compare = like, group = OR)
        String cause;

        @ParameterItem( mappingTo = "stockPlaceType", compare = equal, group = AND)
        String type;
    }

    /**
     * 仓位的出入库记录
     * @param placeId
     * @return
     */
    private List<ErpStockOptLog> optHistory(Long placeId){
        return stockOptLogRepository.findByStockPlaceId(placeId)
                .stream()
                .sorted(Comparator.comparing(ErpStockOptLog::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    /**
     * 搜集出入库日志里的有效信息 成品、模具、废品仓位
     * @param optHistory 历史记录
     * @param filter 无效值过滤
     * @param groupingBy 分组
     * @param attrs 搜集的属性
     * @param <K>
     * @return
     */
    public <K> List<JSONObject> collect(List<ErpStockOptLog> optHistory,
                                         Predicate<? super ErpStockOptLog> filter,
                                         Function<? super ErpStockOptLog, ? extends K> groupingBy,
                                         String ... attrs){
        if(CollectionUtils.isEmpty(optHistory)) {
            return new ArrayList<>();
        }

        Map<K, List<ErpStockOptLog>> infoMap =
                optHistory.stream().filter(filter).collect(Collectors.groupingBy(groupingBy));
        List<JSONObject> infoList = new ArrayList<>();
        //计算每个成品的数量，叠加。
        infoMap.forEach((k,list)->{
            double total = list.stream().mapToDouble(log->{
                if(StockOpt.OUT.name().equals(log.getOperation())){
                    return  -1 * log.getNum();
                }
                return log.getNum();
            }).sum();
            //搜集数量>0的成品
            if(total > 0){
                JSONObject info = list.get(0).filter(attrs);
                info.put("total", total);
                infoList.add(info);
            }
        });
        return infoList;
    }


    ///**
    //     * 仓位信息
    //     * @param placeId
    //     * @return history ： optHistory
    //     *         remain : 当前库存信息 （成品、模具、废品仓位时）
    //     */
    //    public Map<String, List<JSONObject>> placeExt(Long placeId){
    //        ErpStockPlace place = one(placeId);
    //        List<ErpStockOptLog> optHistory = optHistory(placeId);
    //        Map<String, List<JSONObject>> map = new HashMap<>();
    //        map.put("history", optHistory.stream().map(ErpStockOptLog::toJSONObject).collect(Collectors.toList()));
    //        if(StockPlaceType.P.name().equals(place.getStockPlaceType())){
    //            //按照成品分组
    //            Map<Long, List<ErpStockOptLog>> infoMap =
    //                    optHistory.stream().filter(log-> Objects.nonNull(log.getProductId())).collect(Collectors.groupingBy(ErpStockOptLog::getProductId));
    //
    //            List<JSONObject> infoList = new ArrayList<>();
    //
    //            //计算每个成品的数量，叠加。
    //            infoMap.forEach((k,list)->{
    //                double total = list.stream().mapToDouble(log->{
    //                    if(StockOpt.OUT.name().equals(log.getOperation())){
    //                        return  -1 * log.getNum();
    //                    }
    //                    return log.getNum();
    //                }).sum();
    //                //搜集数量>0的成品
    //                if(total > 0){
    //                    JSONObject info = list.get(0).filter("productId", "productName");
    //                    info.put("total", total);
    //                    infoList.add(info);
    //                }
    //            });
    //
    //            map.put("remain", infoList);
    //            return map;
    //        }
    //
    //        return map;
    //    }
}
