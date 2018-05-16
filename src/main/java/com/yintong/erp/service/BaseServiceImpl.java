package com.yintong.erp.service;

import com.yintong.erp.utils.base.query.QueryHandle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;

public abstract class BaseServiceImpl<T> implements BaseService<T> {

    @Autowired
    QueryHandle queryHandle;

    public QueryHandle getQueryHandle(){
        return queryHandle;
    }

    public Page<T> findAll(Object queryDTO,Integer pageNum,Integer pageSize){
        return find(queryDTO,pageNum,pageSize);
    }

    public void save(T entity){
        saveEntity(entity);
    }

    public T findOne(Long id){
        return findEntity(id);
    }

    public void remove(Long id){
        delete(id);
    }

}
