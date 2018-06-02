package com.yintong.erp.web.basis;

import com.yintong.erp.domain.basis.ErpBaseEquipment;
import com.yintong.erp.service.basis.EquipmentService;
import com.yintong.erp.utils.base.BaseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.yintong.erp.utils.query.PageWrapper.page2BaseResult;

/**
 * Created by jianqiang on 2018/6/2.
 * 设备
 */
@RestController
@RequestMapping("basis/equipment")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;



    @GetMapping
    public BaseResult query(EquipmentService.EquipmentParameterBuilder parameter){
        Page<ErpBaseEquipment> page = equipmentService.query(parameter);
        return page2BaseResult(page);
    }

    /**
     * 新增设备
     * @param equipment
     * @return
     */
    @PostMapping
    public BaseResult create(@RequestBody ErpBaseEquipment equipment){
        return new BaseResult().addPojo(equipmentService.create(equipment));
    }

    /**
     * 根据模具id查找设备
     * @param equipmentId
     * @return
     */
    @GetMapping("{equipmentId}")
    public BaseResult one(@PathVariable Long equipmentId){
        return new BaseResult().addPojo(equipmentService.one(equipmentId));
    }

    /**
     * 根据id删除
     * @param equipmentId
     * @return
     */
    @DeleteMapping("{equipmentId}")
    public BaseResult delete(@PathVariable Long equipmentId){
        equipmentService.delete(equipmentId);
        return new BaseResult();
    }


    /**
     * 更新设备
     * @param equipment
     * @return
     */
    @PutMapping
    public BaseResult update(@RequestBody ErpBaseEquipment equipment){
        return new BaseResult().addPojo(equipmentService.update(equipment));
    }
}
