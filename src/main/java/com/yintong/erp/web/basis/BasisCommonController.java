package com.yintong.erp.web.basis;

import com.yintong.erp.domain.basis.ErpBaseCategory;
import com.yintong.erp.service.basis.CategoryService;
import com.yintong.erp.utils.base.BaseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author lucifer.chan
 * @create 2018-05-14 上午12:22
 * 基础数据的常用服务
 **/
@RestController
@RequestMapping("basis/common")
public class BasisCommonController {

    @Autowired CategoryService categoryService;

    /**
     * 类别树
     * @param code 跟节点编码，可选
     * @return
     */
    @GetMapping("categories/tree")
    public BaseResult categoriesTree(String code){
        List<ErpBaseCategory> tree = StringUtils.hasLength(code) ? categoryService.tree(code) : categoryService.tree();
        return new BaseResult().addList("tree", tree);
    }
}
