package com.yintong.erp.domain.basis;

import com.yintong.erp.utils.bar.BarCodeConstants;
import com.yintong.erp.utils.base.JsonWrapper;
import net.sf.json.JSONObject;

/**
 * @author lucifer.chan
 * @create 2018-09-01 上午8:52
 * 模版货物
 **/
public interface TemplateWares {
    
    String getDescription();

    String getBarCode();
    
    Long getWaresId();
    
    String getUnit();
    
    String getSimpleName();
    
    String getSpecification();
    
    String getCategoryCode();
    
    default JSONObject getTemplate(){
        return JsonWrapper.builder()
                .add("name", getDescription())
                .add("waresId", getWaresId())
                .add("barCode", getBarCode())
                .add("unit", getUnit())
                .add("simpleName", getSimpleName())
                .add("specification", getSpecification())
                .add("category", BarCodeConstants.BAR_CODE_PREFIX.valueOf(getCategoryCode()).description())
                .add(extInfo())
            .build();
    }

    default JSONObject getTemplate(Long code){
        JSONObject ret = getTemplate();
        ret.put("code", code);
        return ret;
    }

    default JSONObject extInfo(){
        return new JSONObject();
    }
}
