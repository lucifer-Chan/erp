package com.yintong.erp.domain.basis.barCode;


import com.yintong.erp.utils.bar.BarCode;
import com.yintong.erp.utils.bar.BarCodeIndex;
import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PostPersist;

import static com.yintong.erp.utils.bar.BarCodeConstants.BAR_CODE_PREFIX.MRA0;

/**
 * @author lucifer.chan
 * @create 2018-05-08 下午5:13
 **/
@Entity
@Builder @AllArgsConstructor @NoArgsConstructor
@Getter
@Setter
@BarCode(MRA0)
public class ErpBaseBarCode extends BaseEntityWithBarCode {
    @Id @GeneratedValue
    private Long id;

    @BarCodeIndex(1)
    private String attr;

    @BarCodeIndex(2)
    private String name;

    @BarCodeIndex(value = 3, holder = true)
    private String placeHolder;

    @BarCodeIndex(4)
    private String description;


    protected void prePersist(){
        System.out.println("prePersist");
        System.out.println(this.id);
        System.out.println(getId());
    }

    @PostPersist
    void postPersist(){
        System.out.println("postPersist");
        System.out.println(this.id);
        System.out.println(getId());
    }


}