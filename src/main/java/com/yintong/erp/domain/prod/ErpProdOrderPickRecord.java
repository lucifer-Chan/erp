package com.yintong.erp.domain.prod;

import com.yintong.erp.utils.bar.BarCode;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import com.yintong.erp.utils.common.Assert;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.KREC;


/**
 * @author lucifer.chan
 * @create 2018-09-03 下午3:58
 * 制令单挑拣记录
 **/
@Entity
@BarCode(prefix = KREC)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ErpProdOrderPickRecord extends BaseEntityWithBarCode {
    @Id
    @GeneratedValue
    private Long id;

    @Column(columnDefinition = "bigint(20) comment '制令单id'")
    private Long orderId;

    @Column(columnDefinition = "double(20,5) comment '供调减的成品重量kg'")
    private Double totalNum;

    @Column(columnDefinition = "double(20,5) comment '挑拣的质量合格的成品重量kg'")
    private Double validNum;

    @Column(columnDefinition = "int(20) comment '挑拣的质量合格的成品个数'")
    private Integer validOne;

    @Column(columnDefinition = "varchar(100) DEFAULT '' comment '备注'")
    private String remark;

    public void requiredValidate(){
        Assert.notNull(orderId, "制令单不能为空");
        Assert.notNull(totalNum, "成品总数不能为空");
        Assert.notNull(validNum, "质量合格的成品数不能为空");
    }
}
