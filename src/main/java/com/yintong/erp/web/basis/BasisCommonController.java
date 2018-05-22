package com.yintong.erp.web.basis;

import com.yintong.erp.domain.basis.ErpBaseCategory;
import com.yintong.erp.domain.basis.ErpBaseLookupRepository;
import com.yintong.erp.service.basis.CategoryService;
import com.yintong.erp.utils.base.BaseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
}
