package com.yintong.erp.utils.base;

import com.yintong.erp.utils.transform.ReflectUtil;
import net.sf.json.JSONObject;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public interface Filterable{
    /**
     * 从一个pojo中过滤出需要的属性
     * @param attrs
     * @return
     */
    default JSONObject filter(String ... attrs){
        return result(this, true, attrs);
    }

    /**
     * 从一个pojo中排除掉属性
     * @param attrs
     * @return
     */
    default JSONObject exclude(String ... attrs){
        return result(this, false, attrs);
    }

    static JSONObject result(Filterable source, boolean include, String ... attrs){
        JSONObject ret = new JSONObject();
        List<Field> fields = ReflectUtil.getAllFields(source)
                .stream()
                .filter(f-> include == hasAttr(f, attrs))
                .collect(Collectors.toList());
        fields.forEach(f->{
            try {
                f.setAccessible(true);
                ret.put(f.getName(), f.get(source));
            } catch (IllegalAccessException e){
                e.printStackTrace();
            }
        });
        return  ret;
    }


    static boolean hasAttr(Field f, String ...attrs){
        return Arrays.asList(attrs).contains(f.getName());
    }

}
