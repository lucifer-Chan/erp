package com.yintong.erp.service.basis;

import com.yintong.erp.domain.basis.ErpBaseCategory;
import com.yintong.erp.domain.basis.ErpBaseCategoryRepository;
import com.yintong.erp.utils.common.SimpleCache;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

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

    @Autowired SimpleCache<Map<String, ErpBaseCategory>> mapCache;

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
        return cache.getDataFromCache(KEY_PREFIX + "_" + parentCode + "_children",
                ret -> all().stream()
                        .filter(category -> parentCode.equals(category.getParentCode()))
                        .sorted(Comparator.comparing(ErpBaseCategory::getParentCode))
                        .collect(toList())
        );
    }

    /**
     * 展开一棵树
     * @param rootCode
     * @param ret 返回值
     * @return
     */
    public List<ErpBaseCategory> append(String rootCode, List<ErpBaseCategory> ret){
        ret.add(one(rootCode));
        List<ErpBaseCategory> children = children(rootCode);
        if(!CollectionUtils.isEmpty(children)){
            for (ErpBaseCategory child : children)
                ret = append(child.getCode(), ret);
        }
        return ret;
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
     * 根据code获取节点
     * @param code
     * @return
     */
    public ErpBaseCategory one(String code){
        Map<String, ErpBaseCategory> map = mapCache.getDataFromCache(KEY_PREFIX + "_all_map",
                ret -> all().stream().collect(Collectors.toMap(ErpBaseCategory::getCode, c->c)));
        ErpBaseCategory ret = map.get(code);
        Assert.notNull(ret, "未找到编码为" + code + "的类别");
        return ret;
    }

    /**
     * 所有类别
     * @return
     */
    public List<ErpBaseCategory> all(){
        return cache.getDataFromCache(KEY_PREFIX + "_all", ret -> categoryRepository.findAll());
    }
}
