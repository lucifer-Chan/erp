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

        String category = BarCodeConstants.BAR_CODE_PREFIX.valueOf(getCategoryCode()).description();

        String simpleCategory = category;

        if(category.startsWith("原材料-")){
            simpleCategory = category.substring("原材料-".length(), category.length());
        } else if (category.startsWith("成品-")){
            simpleCategory = category.substring("成品-".length(), category.length());
        } else if (category.startsWith("模具-")){
            simpleCategory = category.substring("模具-".length(), category.length());
        }

        //prefix.substring("原材料-".length(), prefix.length());

        return JsonWrapper.builder()
                .add("name", getDescription())
                .add("waresId", getWaresId())
                .add("barCode", getBarCode())
                .add("unit", getUnit())
                .add("simpleName", getSimpleName())
                .add("specification", getSpecification())
                .add("category", category)
                .add("simpleCategory", simpleCategory)
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