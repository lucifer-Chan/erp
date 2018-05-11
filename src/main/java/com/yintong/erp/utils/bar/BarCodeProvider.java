package com.yintong.erp.utils.bar;

import com.yintong.erp.utils.transform.ReflectUtil;
import lombok.NonNull;
import org.hibernate.event.spi.*;
import org.jooq.lambda.Unchecked;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.yintong.erp.utils.bar.BarCodeConstants.*;

/**
 * @author lucifer.chan
 * @create 2018-05-08 下午8:54
 * 条码自动生成器
 **/
public class BarCodeProvider implements PreInsertEventListener, PreUpdateEventListener {

    /**
     * 属性补位
     *  1：不足9位-前补0
     *  2：大于9位报错
     *
     * @param attribute -要包装的属性
     * @param maxLength -最大位数
     * @return
     */
    private String wrapperAttribute(@NonNull Serializable attribute, int maxLength){
        StringBuilder ret = new StringBuilder(attribute.toString());
        int length = ret.length();
        Assert.isTrue(length <= maxLength, "@Id不能大于"+ maxLength +"位");
        for (int i = 0; i < maxLength - length; i ++)
            ret.insert(0, "0");
        return ret.toString();
    }

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        onPreCommit(event, event.getState());
        return false;
    }

    @Override
    public boolean onPreUpdate(PreUpdateEvent event) {
        onPreCommit(event, event.getState());
        return false;
    }

    private void onPreCommit(AbstractPreDatabaseOperationEvent event, Object[] state){
        Object entity = event.getEntity();
        //id
        String id = wrapperAttribute(event.getId(), ID_LENGTH);
        //前缀
        BAR_CODE_PREFIX prefix;
        Class<?> clazz = ReflectUtil.getClassesUntilRoot(entity).stream()
                .filter(c -> c.isAnnotationPresent(BarCode.class))
                .findFirst().orElse(null);
        //注解在类上的前缀
        if(Objects.nonNull(clazz)) {
            BarCode barCode = clazz.getAnnotation(BarCode.class);
            BAR_CODE_PREFIX [] prefixes = barCode.prefix();
            Assert.isTrue(prefixes.length > 0, "@BarCode注解在实体类上时，必须有value属性值！");
            prefix = prefixes[0];
            id = barCode.excludeId() ? "" : id;
        } else {
            //注解在属性上的前缀-取属性值
            List<Field> prefixes = ReflectUtil.getFieldsByAnnotation(entity, BarCode.class);
            if(CollectionUtils.isEmpty(prefixes)) return;
            Assert.isTrue(prefixes.size() == 1, "实体类必须有且只有一个拥有@BarCode的字段！");
            //供提取前缀的字段
            Field prefixField = prefixes.get(0);
            Assert.isTrue(prefixField.getGenericType().equals(String.class), "@BarCode标注的字段必须为String类型");
            Object value = Unchecked.biFunction(ReflectUtil::getValue).apply(prefixField, entity);
            Assert.isTrue(Objects.nonNull(value) && StringUtils.hasLength(value.toString()), "@BarCode标注的字段必须有值");
            prefix = BAR_CODE_PREFIX.valueOf(value.toString());
            id = prefixField.getAnnotation(BarCode.class).excludeId() ? "" : id;
        }

        List<Field> columns = ReflectUtil.getFieldsByAnnotation(entity, BarCodeColumn.class);
        Assert.isTrue(columns.size() == 1, "实体类必须有且只有一个拥有@BarCodeColumn的字段！");
        //供存储的字段
        Field targetField = columns.get(0);
        Assert.isTrue(targetField.getGenericType().equals(String.class), "@BarCodeColumn标注的字段必须为String类型");

        String[] propertyNames = event.getPersister().getEntityMetamodel().getPropertyNames();
        //1-前缀+id
        StringBuilder barCode = new StringBuilder(prefix.name()).append(id);
        //拥有@BarCodeIndex的字段，排序
        List<Field> indexes = ReflectUtil.getFieldsByAnnotation(entity, BarCodeIndex.class).stream()
                .sorted(Comparator.comparingInt(f -> f.getAnnotation(BarCodeIndex.class).value()))
                .collect(Collectors.toList());

        //2-预条码+indexes
        for (Field field : indexes){
            //2-1 获取属性的真实值
            Object value = Unchecked.biFunction(ReflectUtil::getValue).apply(field, entity);
            //2-2 构造出属性的计算值
            if(Objects.isNull(value) || !StringUtils.hasLength(value.toString())) {
                barCode.append(EMPTY_REPLACE);
            } else {
                BarCodeIndex barCodeIndex = field.getAnnotation(BarCodeIndex.class);
                barCode.append(wrapperAttribute(value.toString(), barCodeIndex.holder() ? 1 : barCodeIndex.length()));
            }
        }

        for (int i = 0; i < propertyNames.length ; i ++) {
            if (targetField.getName().equals(propertyNames[i])){
                state[i] = barCode.toString();
            }
        }
        try{
            ReflectUtil.setValue(targetField, entity, barCode.toString());
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
