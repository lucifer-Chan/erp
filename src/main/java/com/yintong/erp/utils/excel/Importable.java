package com.yintong.erp.utils.excel;

import com.yintong.erp.utils.common.SimpleCache;
import com.yintong.erp.utils.transform.ReflectUtil;
import lombok.NonNull;
import org.springframework.util.Assert;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * @author lucifer.chan
 * @create 2018-05-26 下午11:58
 * 可导入的pojo
 **/
public interface Importable {
    /**
     * 必填项验证 Assert.xxx();
     * @return
     */
    void requiredValidate();

    /**
     * 唯一性验证
     */
    void uniqueValidate();

    /**
     * 根据data赋值一个entity
     * @param data - 数据列表row
     * @param fieldSupplier 要导入的列名
     * @return
     */
    default void assign(@NonNull List data, Supplier<List<String>> fieldSupplier) {
        assign(data, fieldSupplier.get());
    }

    default void assign(@NonNull List data, List<String> fieldNames) {
        Map<String, Field> fieldMap = fieldMap();
        Assert.notNull(fieldNames, "列表名不能为空");
        Assert.isTrue(data.size() == fieldNames.size(), "data.size() must be equal with fieldNames.size()");
        for (int i = 0; i < fieldNames.size(); i ++){
            String fieldName = fieldNames.get(i);
            Field field = fieldMap.get(fieldName);
            Assert.notNull(field, this.getClass().getName() +"中未找到" + fieldName + "属性");
            ReflectUtil.setValueBySetter(field, this, data.get(i));
        }
        requiredValidate();
    }


    /**
     * 获取剁手的field->map
     * @return
     */
    default Map<String, Field> fieldMap(){
        SimpleCache<Map<String, Field>> cache = new SimpleCache<>();
        return cache.getDataFromCache("_import_" + this.getClass().getName(), value->
            ReflectUtil.getAllFields(this).stream()
                    .collect(Collectors.toMap(Field::getName, Function.identity()))
        );
    }
}
