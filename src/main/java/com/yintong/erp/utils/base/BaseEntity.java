package com.yintong.erp.utils.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.yintong.erp.domain.basis.security.ErpEmployee;
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
public abstract class BaseEntity implements Filterable {

    public static final int ENABLED = 0;

//    @JsonIgnore
    private Date createdAt;

    @JsonIgnore
    private Long createdBy;

    /**
     * 创建者名字
     */
    private String createdName;

    @JsonIgnore
    private Integer isDel;//1-逻辑删除;

//    @JsonIgnore
    private Date lastUpdatedAt;

    @JsonIgnore
    private Long lastUpdatedBy;

    /**
     * 更新者名字
     */
    private String lastUpdatedName;

    /**
     * 供Override
     */
    protected void prePersist(){}

    /**
     * 供Override
     */
    protected void preUpdate(){}

    public void copyBase(BaseEntity old) {
        this.setCreatedAt(old.getCreatedAt());
        this.setCreatedBy(old.getCreatedBy());
        this.setCreatedName(old.getCreatedName());
    }

    @PrePersist
    private void _prePersist(){
        createdAt = new Date();
        isDel = 0;
        try{
            ErpEmployee employee = SessionUtil.getCurrentUser();
            createdBy = employee.getId();
            createdName = employee.getName();
        } catch(Exception e){
            createdBy = -1L;
            createdName = "system";
        }
        prePersist();
    }

    @PreUpdate
    private void _preUpdate(){
        lastUpdatedAt = new Date();
        try{
            ErpEmployee employee = SessionUtil.getCurrentUser();
            lastUpdatedBy = employee.getId();
            lastUpdatedName = employee.getName();
        } catch(Exception e){
            lastUpdatedBy = -1L;
            lastUpdatedName = "system";
        }
        preUpdate();
    }

    @Override
    public String toString() {
        return this.toJSONObject().toString();
    }
}