package com.yintong.erp.domain.prod;

import com.yintong.erp.utils.base.BaseEntity;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author lucifer.chan
 * @create 2018-09-03 下午10:15
 * 生产制令单操作记录
 **/
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity
public class ErpProdOrderOptLog extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '制令单id'")
    private Long orderId;

    @Column(columnDefinition = "varchar(100) comment '操作内容：修改aaa->bbb'")
    private String content;
}
