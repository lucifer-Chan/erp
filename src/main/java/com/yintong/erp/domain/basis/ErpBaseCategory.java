package com.yintong.erp.domain.basis;

import com.yintong.erp.utils.base.BaseEntity;
import com.yintong.erp.utils.transform.IgnoreIfNull;
import lombok.*;
import org.springframework.util.StringUtils;

import javax.persistence.*;
import java.util.List;

/**
 * @author lucifer.chan
 * @create 2018-05-11 上午1:33
 * 基础数据类别
 **/
@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpBaseCategory extends BaseEntity {
    @Id @GeneratedValue
    private Long id;

    @Column(columnDefinition = "varchar(4) comment '编号：父节点+自身编号'")
    private String code;

    @Column(columnDefinition = "varchar(40) comment '名称'")
    private String name;

    @Column(columnDefinition = "varchar(100) comment '全名'")
    private String fullName;

    @Column(columnDefinition = "varchar(10) comment '父节点编号'")
    private String parentCode;

    @Transient @IgnoreIfNull
    private List<ErpBaseCategory> children;

    @Transient
    private boolean leaf;

    @Transient
    private boolean root;

    @Transient
    private int level;

    public int getLevel(){
        return code.length();
    }

    public boolean isLeaf(){
        return code.length() == 4;
    }

    public boolean isRoot(){
        return StringUtils.isEmpty(parentCode);
    }


}
