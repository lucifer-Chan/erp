package com.yintong.erp.utils.base;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import org.springframework.util.StringUtils;

public interface JSONable {
    default JSONObject toJSONObject(){
        return JSONObject.fromObject(this, toDateFormat("yyyy-MM-dd HH:mm:ss"));
    }

    default JSONObject toJSONObject(String format){
        return  JSONObject.fromObject(this, toDateFormat(format));
    }

    /**
     *
     * @param includeEmpty 是否包含空信息
     * @return
     */
    default JSONObject toJSONObject(boolean includeEmpty){
        JSONObject ret = toJSONObject();
        if(includeEmpty) return ret;

        JSONObject that = new JSONObject();
        //noinspection unchecked
        ret.forEach((k, v) -> {
            if(null != v && StringUtils.hasText(v.toString())){
                that.put(k,v);
            }
        });

        return that;

    }

    static JsonConfig toDateFormat(String format) {
        JsonConfig jsonConfig = new JsonConfig();
        jsonConfig.registerJsonValueProcessor(Date.class, new JsonValueProcessor() {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);

            private String process(Object date){
                return Objects.isNull(date) ? "" : simpleDateFormat.format(date);
            }

            @Override
            public Object processObjectValue(String propertyName, Object date, JsonConfig config) {
                return process(date);
            }

            @Override
            public Object processArrayValue(Object date, JsonConfig config) {
                return process(date);
            }
        });
        return jsonConfig;
    }
}
