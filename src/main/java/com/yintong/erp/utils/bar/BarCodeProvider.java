package com.yintong.erp.utils.bar;

import com.yintong.erp.utils.transform.ReflectUtil;
import org.hibernate.event.spi.*;
import org.jooq.lambda.Unchecked;
import org.springframework.util.Assert;
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
        Serializable id = event.getId();
        Object entity = event.getEntity();
        Class<?> clazz = entity.getClass();
        if(clazz.isAnnotationPresent(BarCode.class)) {
            //前缀
            BAR_CODE_PREFIX prefix = clazz.getAnnotation(BarCode.class).value();
            List<Field> columns = ReflectUtil.getFieldsByAnnotation(entity, BarCodeColumn.class);
            //拥有@BarCodeIndex的字段，排序
            List<Field> indexes = ReflectUtil.getFieldsByAnnotation(entity, BarCodeIndex.class).stream()
                    .sorted(Comparator.comparingInt(f -> f.getAnnotation(BarCodeIndex.class).value()))
                    .collect(Collectors.toList());
            Assert.isTrue(columns.size() == 1, "实体类必须有且只有一个拥有@BarCodeColumn的字段！");
            //供存储的字段
            Field targetField = columns.get(0);
            Assert.isTrue(targetField.getGenericType().equals(String.class), "@BarCodeColumn标注的字段必须为String类型");

            String[] propertyNames = event.getPersister().getEntityMetamodel().getPropertyNames();
//            Object[] state = event.getState();

            //1-前缀+id
            StringBuilder barCode = new StringBuilder(prefix.name()).append(id);

            //2-预条码+indexes
            for (Field field : indexes){
                Object value = Unchecked.biFunction(ReflectUtil::getValue).apply(field, entity);
                barCode.append((Objects.isNull(value) || !StringUtils.hasLength(value.toString()))? "K" : value);
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
}
