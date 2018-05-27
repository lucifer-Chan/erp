package com.yintong.erp.utils.query;

import com.yintong.erp.utils.base.BaseResult;
import com.yintong.erp.utils.base.JsonWrapper;
import net.sf.json.JSONObject;
import org.springframework.data.domain.Page;
import org.springframework.util.Assert;

import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author lucifer.chan
 * @create 2018-05-17 下午2:15
 * page -> baseResult
 **/
public interface PageWrapper {
    static <T> BaseResult page2BaseResult(Page<T> page){
        //noinspection unchecked
        return new BaseResult().add(commonAttrs(page)).addList(page.getContent());
    }

    static <T> JSONObject commonAttrs(Page<T> page){
        Assert.notNull(page, "入参page不能为null");
        return JsonWrapper.builder()
                .add("totalElements", page.getTotalElements())
                .add("totalPages", page.getTotalPages())
                .add("last", page.isLast())
                .add("first", page.isFirst())
                .add("size", page.getSize())
                .add("sort", page.getSort())
                .add("numberOfElements", page.getNumberOfElements())
                .add("number", page.getNumber())
                .build();
    }

    static <T> BaseResult page2BaseResult(Page<T> page, Predicate<? super T> filter){
        //noinspection unchecked
        return new BaseResult().add(commonAttrs(page)).addList(
                page.getContent().stream().filter(filter).collect(Collectors.toList())
        );
    }
}
