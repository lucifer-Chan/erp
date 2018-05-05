package com.yintong.erp.utils.base;

import net.sf.json.JSONObject;

public interface JSONable {
    default JSONObject toJSONObject(){
        return JSONObject.fromObject(this);
    }
}
