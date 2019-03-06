package com.yintong.erp.web.stock;

import com.yintong.erp.domain.stock.ErpStockPlace;
import com.yintong.erp.service.stock.StockPlaceService;
import com.yintong.erp.utils.base.BaseResult;
import com.yintong.erp.utils.common.CommonUtil;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.yintong.erp.service.stock.StockPlaceService.*;
import static com.yintong.erp.utils.query.PageWrapper.page2BaseResult;

/**
 * @author lucifer.chan
 * @create 2018-08-09 上午11:55
 * 仓管服务
 **/
@RestController
@RequestMapping("stock")
public class StockController {
    @Autowired StockPlaceService placeService;

    /*===========================以下为仓位相关===========================*/

    /**
     * 新增仓位-如果是原材料仓位的话，需要原材料的id和名称
     * @param place
     * @return
     */
    @PostMapping("place")
    public BaseResult createPlace(@RequestBody ErpStockPlace place){
        return new BaseResult().addPojo(placeService.create(place));
    }

    /**
     * 更新仓位
     * @param place
     * @return
     */
    @PutMapping("place")
    public BaseResult updatePlace(@RequestBody ErpStockPlace place){
        return new BaseResult().addPojo(placeService.update(place.getId()
                , place.getLowerLimit()
                , place.getUpperLimit()
                , place.getName()
                , place.getPlaceCode()
                , place.getDescription()));
    }

    /**
     * 组合查询
     * @param parameters
     * @return
     */
    @GetMapping("place")
    public BaseResult findPlaces(PlaceParameterDto parameters){
        Page<ErpStockPlace> page = placeService.query(parameters);
        return page2BaseResult(page);
    }

    /**
     * 删除仓位
     * @param placeId
     * @return
     */
    @DeleteMapping("place/{placeId}")
    public BaseResult deletePlace(@PathVariable Long placeId){
        placeService.delete(placeId);
        return new BaseResult().setErrmsg("删除成功");
    }

    /**
     * 停役仓位
     * @param placeId
     * @return
     */
    @PatchMapping("place/stop/{placeId}")
    public BaseResult stopPlace(@PathVariable Long placeId){
        return new BaseResult().addPojo(placeService.stop(placeId)).setErrmsg("停役成功");
    }

    /**
     * 查找单个仓位
     * @param idOrCode
     * @return
     */
    @GetMapping("place/{idOrCode}")
    public BaseResult one(@PathVariable String idOrCode){
        Long placeId = CommonUtil.parseLong(idOrCode);
        ErpStockPlace place = Objects.isNull(placeId) ?
                placeService.one(idOrCode) : placeService.one(placeId);
        return new BaseResult().addPojo(place);
    }

    /**
     * 扩展的信息：出入库记录和现存
     * @param placeId
     * @return
     */
    @GetMapping("place/ext/{placeId}")
    public BaseResult extInfo(@PathVariable Long placeId){
        return new BaseResult().addPojo(placeService.placeExt(placeId));
    }

    /*===========================以上为仓位相关===========================*/

}
