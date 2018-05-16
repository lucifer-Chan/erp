package com.yintong.erp.service;

import com.yintong.erp.domain.BaseRepository;
import com.yintong.erp.utils.base.query.QueryHandle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface BaseService<T> {

    BaseRepository<T> getRepository();

    QueryHandle getQueryHandle();

    /**
     * 根据查询DTO对实体进行查询。查询DTO需要实现BaseQueryDTO,并且是Component。
     */
    default Page<T> find(Object queryDTO, Integer pageNum, Integer pageSize){
        Pageable pageable = PageRequest.of (pageNum!=null?pageNum:0, pageSize!=null?pageSize:20);

        Specification<T> spec = (root, query, cb) -> getQueryHandle().handle(root,query,cb,queryDTO);

        return getRepository().findAll(spec, pageable);
    }

    default void saveEntity(T entity){
        getRepository().save(entity);
    }

    default T findEntity(Long id){
        return getRepository().findById(id).orElse(null);
    }

    default void delete(Long id){
        getRepository().deleteById(id);
    }


}
