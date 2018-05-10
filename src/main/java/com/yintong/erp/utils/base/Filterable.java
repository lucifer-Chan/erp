package com.yintong.erp.utils.base;

import lombok.NonNull;
import net.sf.json.JSONObject;

public interface Filterable extends JSONable{
    /**
     * 从一个pojo中过滤出需要的属性
     * @param attrs
     * @return
     */
    default JSONObject filter(@NonNull String ... attrs){
        JSONObject ret = new JSONObject();
        JSONObject that = toJSONObject();
        for (String attr : attrs)
            if(that.containsKey(attr))
                ret.put(attr, that.get(attr));
        return ret;
    }

    /**
     * 从一个pojo中排除掉属性
     * @param attrs
     * @return
     */
    default JSONObject exclude(String ... attrs){
        JSONObject ret = toJSONObject();
        for (String attr : attrs)
            ret.remove(attr);
        return ret;
    }
}
