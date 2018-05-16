package com.yintong.erp.service.basis;

import com.yintong.erp.domain.basis.ErpBaseCategory;
import com.yintong.erp.domain.basis.ErpBaseCategoryRepository;
import com.yintong.erp.utils.common.SimpleCache;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

/**
 * @author lucifer.chan
 * @create 2018-05-12 下午4:15
 * 类别服务
 **/
@Service
public class CategoryService {

    private static final String KEY_PREFIX = CategoryService.class.getName();
    @Autowired ErpBaseCategoryRepository categoryRepository;

    /**
     * 类别缓存
     */
    @Autowired SimpleCache<List<ErpBaseCategory>> cache;

    /**
     * 获取一级类别
     * @return
     */
    public List<ErpBaseCategory> first(){
        return cache.getDataFromCache(KEY_PREFIX + "_first", ret -> all().stream().filter(ErpBaseCategory::isRoot).collect(toList()));
    }
    /**
     * 根据父节点code获取直属子节点列表
     * @param parentCode
     * @return
     */
    public List<ErpBaseCategory> children(@NonNull String parentCode) {
        return cache.getDataFromCache(KEY_PREFIX + "_" + parentCode,
                ret -> all().stream()
                        .filter(category -> parentCode.equals(category.getParentCode()))
                        .sorted(Comparator.comparing(ErpBaseCategory::getParentCode))
                        .collect(toList())
        );
    }

    /**
     * 树型结构
     * @return
     */
    public List<ErpBaseCategory> tree(String rootCode){
        ErpBaseCategory root = all().stream()
                .filter(category->category.getCode().equals(rootCode))
                .findAny().orElse(null);
        if(Objects.isNull(root))
            return new ArrayList<>();
        List<ErpBaseCategory> children = children(root.getCode());
        if(!CollectionUtils.isEmpty(children)){
            for (ErpBaseCategory child : children)
                child.setChildren(tree(child.getCode()));
            root.setChildren(children);
        }
        return root.getChildren();
    }

    /**
     * 树型结构
     * @return
     */
    public List<ErpBaseCategory> tree(){
        List<ErpBaseCategory> roots = first();
        for (ErpBaseCategory category : roots){
            category.setChildren(tree(category.getCode()));
        }
        return roots;
    }

    /**
     * 所有类别
     * @return
     */
    private List<ErpBaseCategory> all(){
        return cache.getDataFromCache(KEY_PREFIX + "_all", ret -> categoryRepository.findAll());
    }

}
