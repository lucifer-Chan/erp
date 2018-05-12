package com.yintong.erp.utils.base;

import com.yintong.erp.sandbox.Table1;
import com.yintong.erp.utils.transform.IgnoreIfNull;
import com.yintong.erp.utils.transform.ReflectUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import net.sf.json.JSONNull;
import net.sf.json.JSONObject;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午4:04
 **/
public class BaseResult{
    public final static String SUCCESS_MSG = "ok";
    public final static String FAILED_MSG = "failed";
    public final static String SUCCESS_CODE = "0";

    private Map<String, Object> all = new HashMap<>();

    private Map<String, Object> ret = new HashMap<>();

    protected String errcode;
    protected String errmsg;

    public BaseResult(){
        this.errcode = SUCCESS_CODE;
        this.errmsg = SUCCESS_MSG;
    }

    public BaseResult(String errcode){
        this.errcode = errcode;
        this.errmsg = SUCCESS_CODE.equals(errcode) ? SUCCESS_MSG : FAILED_MSG;
    }

    public BaseResult(Integer errcode){
        this(errcode+"");
    }

    public BaseResult(Object errcode, String errmsg){
        this.errcode = null == errcode ? "0" : errcode.toString();
        this.errmsg = errmsg;
    }


    public BaseResult put(String key, Object value){
        ret.put(key, Objects.isNull(value) || value instanceof JSONNull ? "" : value);
        return this;
    }

    public BaseResult add(Map<String, Object> map){
        map.forEach(this::put);
        return this;
    }

    @SuppressWarnings("unchecked")
    public BaseResult addPojo(Object pojo){
        return addPojo(pojo, null);
    }

    @SuppressWarnings("unchecked")
    public BaseResult addPojo(Object pojo, String dateFormat){
        if(null == dateFormat) dateFormat = "yyyy-MM-dd";
        return add(pojo2Map(pojo, dateFormat));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> pojo2Map(Object pojo, String dateFormat){
        if(pojo instanceof Map)
            return (Map<String, Object>) pojo;

        List<Field> fields = ReflectUtil.getAllFieldsWithoutIgnored(pojo);
        Map<String, Object> map = new HashMap<>();
        for(Field field : fields){
            if(!field.isAnnotationPresent(JsonIgnore.class)){
                Object value = ReflectUtil.getValueByGetter(field, pojo);
                String fieldName = field.getName();
                if(null == value && !field.isAnnotationPresent(IgnoreIfNull.class)){
                    map.put(fieldName, "");
                } else if(null == value && field.isAnnotationPresent(IgnoreIfNull.class)){

                } else if(value instanceof Date && StringUtils.hasLength(dateFormat)) {
                    map.put(fieldName, new SimpleDateFormat(dateFormat).format((Date) value));
                } else if(value instanceof Iterable){
                    List list = new ArrayList();
                    ((Iterable) value).forEach(obj->list.add(pojo2Map(obj, dateFormat)));
                    map.put(fieldName, list);
                } else if(value instanceof JSONable){
                    map.put(fieldName, pojo2Map(value, dateFormat));
                } else{
                    map.put(fieldName, value);
                }
            }
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    public BaseResult addList(Iterable it, String dateFormat){
        if(null == dateFormat) dateFormat = "yyyy-MM-dd";
        return addList("list", it, dateFormat);
    }

    @SuppressWarnings("unchecked")
    public BaseResult addList(Iterable it){
        return addList(it, null);
    }

    @SuppressWarnings("unchecked")
    public BaseResult addList(String key, Iterable it){
        return addList(key, it, "yyyy-MM-dd");
    }

    @SuppressWarnings("unchecked")
    public BaseResult addList(String key, Iterable it, final String dateFormat){
        List list = new ArrayList();
        it.forEach(obj->{
            if(obj instanceof Number || obj instanceof String)
                list.add(obj);
            else if(obj instanceof Date && StringUtils.hasLength(dateFormat))
                list.add(new SimpleDateFormat(dateFormat).format((Date) obj));
            else
                list.add(pojo2Map(obj, dateFormat));
        });
        ret.put(key, list);
        return this;
    }

    public String toString(){
        return toJSONObject().toString();
    }

    public JSONObject toJSONObject(){
        all.put("errcode", getErrcode());
        all.put("errmsg", errmsg);
        all.put("success", isSuccess());
        all.put("ret", ret);
        return JSONObject.fromObject(all);
    }

    public Object getErrcode(){
        try{
            return Integer.parseInt(errcode);
        }catch(NumberFormatException e){
            return errcode;
        }
    }

    public BaseResult setErrcode(String errcode){
        this.errcode = errcode;
        return this;
    }


    public String getErrmsg(){
        return errmsg;
    }


    public BaseResult setErrmsg(String errmsg){
        this.errmsg = errmsg;
        return this;
    }

    public boolean isSuccess(){
        return SUCCESS_CODE.equals(errcode);
    }

    @JsonInclude(Include.NON_EMPTY)
    public JSONObject getRet(){
        return JSONObject.fromObject(ret);
    }

    public BaseResult setRet(JSONObject ret){
        this.ret = ret;
        return this;
    }

    public static void main(String[] ags){
        Table1 table1 = Table1.builder().build();
        System.out.println(new BaseResult().addPojo(table1));
    }
}
