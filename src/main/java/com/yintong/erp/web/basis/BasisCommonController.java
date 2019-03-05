package com.yintong.erp.web.basis;

import com.yintong.erp.domain.basis.ErpBaseCategory;
import com.yintong.erp.domain.basis.ErpBaseLookupRepository;
import com.yintong.erp.domain.basis.associator.ErpEndProductSupplier;
import com.yintong.erp.domain.basis.associator.ErpEndProductSupplierRepository;
import com.yintong.erp.domain.basis.associator.ErpModelSupplier;
import com.yintong.erp.domain.basis.associator.ErpModelSupplierRepository;
import com.yintong.erp.domain.basis.security.ErpMiniRoleRepository;
import com.yintong.erp.service.basis.CategoryService;
import com.yintong.erp.service.basis.associator.SupplierRawMaterialService;
import com.yintong.erp.utils.bar.BarCodeUtil;
import com.yintong.erp.utils.base.BaseResult;
import com.yintong.erp.utils.common.Constants;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
/**
 * @author lucifer.chan
 * @create 2018-05-14 上午12:22
 * 基础数据的常用服务
 **/
@RestController
@RequestMapping("basis/common")
public class BasisCommonController {

    @Autowired CategoryService categoryService;

    @Autowired ErpBaseLookupRepository lookupRepository;

    @Autowired SupplierRawMaterialService supplierRawMaterialService;

    @Autowired ErpEndProductSupplierRepository productSupplierRepository;

    @Autowired ErpModelSupplierRepository mouldSupplierRepository;

    @Autowired ErpMiniRoleRepository miniRoleRepository;

    /**
     * 类别树
     * @param code 根节点编码，可选[暂时没用]
     * @return
     */
    @GetMapping("categories/tree")
    public BaseResult categoriesTree(String code){
        List<ErpBaseCategory> tree = StringUtils.hasLength(code) ? categoryService.tree(code) : categoryService.tree();
        return new BaseResult().addList("tree", tree);
    }

    /**
     * 直属子节点-不包括自身[做下拉列表用]
     * @param code 根节点编码，可选
     * @return
     */
    @GetMapping("categories/children/direct")
    public BaseResult categoriesDirect(String code){
        List<ErpBaseCategory> ret = StringUtils.hasLength(code) ? categoryService.children(code) : categoryService.first();
        return new BaseResult().addList(ret);
    }

    /**
     * 从跟节点全部展开-包括自身[做树用]
     * @param code 根节点编码，可选
     * @return
     */
    @GetMapping("categories/children/append")
    public BaseResult categoriesAppend(String code){
        List<ErpBaseCategory> ret = StringUtils.hasLength(code) ? categoryService.append(code, new ArrayList<>()) : categoryService.all();
        return new BaseResult().addList(ret);
    }

    /**
     * 下拉供选项
     * @param type
     * @return
     */
    @GetMapping("lookup/{type}")
    public BaseResult lookup(@PathVariable String type){
        return new BaseResult().addList(
                lookupRepository.findByTypeOrderByTag(type).stream()
                    .map(lookup -> lookup.filter("code", "name"))
                    .collect(Collectors.toList())
        );
    }

    /**
     * 获取条形码图像
     * <img src='...'>
     * @param code
     * @throws IOException
     */
    @GetMapping("barcode/{code}")
    public BaseResult barCode(@PathVariable String code) {

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        BarCodeUtil.generate(code, out);
        String base64 = "data:image/png;base64," + new Base64().encodeToString(out.toByteArray());
        return new BaseResult().put("base64", base64);
    }
//    @GetMapping("barcode/{code}")
//    public void barCode(HttpServletResponse response, @PathVariable String code) throws IOException {
//        //将图片输出给浏览器
//        BarCodeUtil.generate(code, response.getOutputStream());
//    }

    /**
     * 根据货物类型和关联id查找
     * @param waresType
     * @param waresAssId
     * @return
     */
    @GetMapping("barcode/{waresType}/{waresAssId}")
    public BaseResult findBarCode(@PathVariable Constants.WaresType waresType, @PathVariable Long waresAssId){
        if(Constants.WaresType.P == waresType){
            ErpEndProductSupplier ass = productSupplierRepository.findById(waresAssId).orElse(null);
            Assert.notNull(ass, "未找到成品和供应商的关联[" + waresAssId + "]");
            return new BaseResult().put("barcode", ass.getBarCode());
        } else if(Constants.WaresType.D == waresType){
            ErpModelSupplier ass = mouldSupplierRepository.findById(waresAssId).orElse(null);
            Assert.notNull(ass, "未找到模具和供应商的关联[" + waresAssId + "]");
            return new BaseResult().put("barcode", ass.getBarCode());
        }
        throw new IllegalArgumentException("货物类型不合法");
    }

    /**
     * 获取所有供应商和成品的关联
     * @return assId：供应商-名称-规格
     */
    @GetMapping("ass/supplier/material")
    public BaseResult supplierMaterials(){
        return new BaseResult().addList(supplierRawMaterialService.descriptions());
    }

    @GetMapping("mini/roles")
    public BaseResult miniRoles(){
        return new BaseResult().addList(miniRoleRepository.findAll());
    }
}
