package com.yintong.erp.utils.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yintong.erp.utils.common.SessionUtil;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.util.Date;

/**
 * @author lucifer.chan
 * @create 2018-03-15 下午3:19
 * 实体基类
 **/
@MappedSuperclass
@Getter @Setter
public abstract class BaseEntity implements JSONable, Filterable {

    public static final int ENABLED = 0;

//    @JsonIgnore
    private Date createdAt;

    @JsonIgnore
    private Long createdBy;

    @JsonIgnore
    private Integer isDel;//1-逻辑删除;

    @JsonIgnore
    private Date lastUpdatedAt;

    @JsonIgnore
    private Long lastUpdatedBy;

    /**
     * 供Override
     */
    protected void prePersist(){}

    /**
     * 供Override
     */
    protected void preUpdate(){}

    @PrePersist
    private void _prePersist(){
        createdAt = new Date();
        isDel = 0;
        try{
            createdBy = SessionUtil.getCurrentUserId();
        } catch(Exception e){
            createdBy = -1L;
        }
        prePersist();
    }

    @PreUpdate
    private void _preUpdate(){
        lastUpdatedAt = new Date();
        try{
            lastUpdatedBy = SessionUtil.getCurrentUserId();
        } catch(Exception e){
            lastUpdatedBy = -1L;
        }
        preUpdate();
    }

    @Override
    public String toString() {
        return this.toJSONObject().toString();
    }
}