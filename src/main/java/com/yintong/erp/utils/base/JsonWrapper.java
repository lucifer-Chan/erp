package com.yintong.erp.utils.base;

import net.sf.json.JSONObject;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午3:14
 * json包装器
 **/
public class JsonWrapper {
    private JSONObject ret;

    private JsonWrapper(){
        this.ret = new JSONObject();
    }

    public static JsonWrapper builder(){
        return new JsonWrapper();
    }

    public JsonWrapper add(String key, Object value){
        this.ret.put(key, value);
        return this;
    }

    public JsonWrapper add(JSONObject json){
        this.ret.putAll(json);
        return this;
    }

    public JSONObject build(){
        return this.ret;
    }
}
