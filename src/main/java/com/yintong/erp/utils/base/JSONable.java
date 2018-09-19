package com.yintong.erp.utils.base;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.processors.JsonValueProcessor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;

public interface JSONable {
    default JSONObject toJSONObject(){
        return JSONObject.fromObject(this, toDateFormat("yyyy-MM-dd HH:mm:ss"));
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
