package com.yintong.erp.domain.basis;

import com.yintong.erp.utils.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by jianqiang on 2018/5/12.
 *
 *规格表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class ErpBaseSpecification extends BaseEntity {

    @Id
    @GeneratedValue
    private Long id;
    @Column(columnDefinition = "varchar(10) comment '头径D(mm)上限'")
    private String spHdmmUpper;
    @Column(columnDefinition = "varchar(10) comment '头径D(mm)下限'")
    private String spHdmmLower;
    @Column(columnDefinition = "varchar(10) comment '头厚T(mm)上限'")
    private String spHtmmUpper;
    @Column(columnDefinition = "varchar(10) comment '头厚T(mm)下限'")
    private String spHtmmLower;
    @Column(columnDefinition = "varchar(10) comment '脚径d(mm)上限'")
    private String spFtmmUpper;
    @Column(columnDefinition = "varchar(10) comment '脚径d(mm)下限'")
    private String spFtmmLower;
    @Column(columnDefinition = "varchar(10) comment '脚长L(mm)上限'")
    private String spFlmmUpper;
    @Column(columnDefinition = "varchar(10) comment '脚长L(mm)下限'")
    private String spFlmmLower;
    @Column(columnDefinition = "varchar(10) comment '头径银层S1(mm)上限'")
    private String spHdsmmUpper;
    @Column(columnDefinition = "varchar(10) comment '头径银层S1(mm)下限'")
    private String spHdsmmLower;
    @Column(columnDefinition = "varchar(10) comment '脱模角度θ(° )上限'")
    private String spTmammUpper;
    @Column(columnDefinition = "varchar(10) comment '脱模角度θ(° )下限'")
    private String spTmammLower;
    @Column(columnDefinition = "varchar(10) comment '球半径SR(mm)上限'")
    private String spSrammUpper;
    @Column(columnDefinition = "varchar(10) comment '球半径SR(mm)下限'")
    private String spSrammLower;
    @Column(columnDefinition = "varchar(10) comment '同轴度◎(mm)上限'")
    private String spAxlemmUpper;
    @Column(columnDefinition = "varchar(10) comment '同轴度◎(mm)下限'")
    private String spAxlemmLower;
    @Column(columnDefinition = "varchar(10) comment '边缘S1(mm)上限'")
    private String spEdgemmUpper;
    @Column(columnDefinition = "varchar(10) comment '边缘S1(mm)下限'")
    private String speEdgeemmLower;
    @Column(columnDefinition = "varchar(10) comment '钉脚S2(mm)上限'")
    private String spFdsmmUpper;
    @Column(columnDefinition = "varchar(10) comment '钉脚S2(mm)下限'")
    private String speFdsmmLower;
    @Column(columnDefinition = "varchar(10) comment '脚边缘S2(mm)上限'")
    private String spFaxlemmUpper;
    @Column(columnDefinition = "varchar(10) comment '脚边缘S2(mm)下限'")
    private String speFaxlemmLower;
    @Column(columnDefinition = "varchar(10) comment '头部复合强度'")
    private String spHCstrength;
    @Column(columnDefinition = "varchar(10) comment '脚部复合强度'")
    private String spFCstrength;


}
