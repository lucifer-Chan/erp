package com.yintong.erp.utils.bar;

import com.yintong.erp.utils.base.BaseEntityWithBarCode;
import com.yintong.erp.utils.transform.ReflectUtil;
import lombok.Getter;
import lombok.NonNull;
import org.hibernate.event.spi.*;
import org.jooq.lambda.Unchecked;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

import static com.yintong.erp.utils.bar.BarCodeConstants.*;

/**
 * @author lucifer.chan
 * @create 2018-05-08 下午8:54
 * 条形码自动生成器
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
        Class<?> entityClass = event.getEntity().getClass();
        if(! cache.containsKey(entityClass)) return;
        BarCodeEntity bar = cache.get(entityClass);
        String val = bar.val(event);
        String[] propertyNames = event.getPersister().getEntityMetamodel().getPropertyNames();
        for (int i = 0; i < propertyNames.length ; i ++) {
            if (bar.getTargetField().getName().equals(propertyNames[i])){
                state[i] = val;
            }
        }
        try{
            ReflectUtil.setValue(bar.getTargetField(), event.getEntity(), val);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private boolean collected;

    public void collect(Set<ManagedType<?>> managedTypeSet){
        if(collected) return;
        managedTypeSet.forEach(
                managedType -> {
                    Class<?> clazz = managedType.getJavaType();
                    if(managedType instanceof EntityType && BaseEntityWithBarCode.class.isAssignableFrom(clazz))
                        cache.put(clazz, new BarCodeEntity(clazz));
                }
        );
        collected = true;
    }

    private static Map<Class<?>, BarCodeEntity> cache = new HashMap<>();

    @Getter
    private static class BarCodeEntity{
        //类名
        private Class<?> entityClass;
        //@BarCode 是否作用在类上
        private boolean prefixOnClass;
        //@BarCode作用在具体的Field上
        private Field prefixField;
        //@BarCode
        private BarCode barCode;
        //barCodeIndex列表
        private List<Field> indexes;
        //最终存条形码的Field
        private Field targetField;

        private BarCodeEntity(Class<?> entityClass){
            this.entityClass = entityClass;
            //1-初始化barCode属性和prefixOnClass属性
            Class<?> classWithBarCode = ReflectUtil.getClassesUntilRoot(entityClass).stream()
                    .filter(c -> c.isAnnotationPresent(BarCode.class))
                    .findFirst().orElse(null);
            if(Objects.nonNull(classWithBarCode)){
                prefixOnClass = true;
                barCode = classWithBarCode.getAnnotation(BarCode.class);
                Assert.notEmpty(barCode.prefix(), "@BarCode标注在" + entityClass.getName() + "上时，必须要有value属性值！");
            } else {
                prefixOnClass = false;
                List<Field> prefixes = ReflectUtil.getFieldsByAnnotation(entityClass, BarCode.class);
                Assert.isTrue(prefixes.size() == 1, "实体类" + entityClass.getName() + "必须有且只有一个拥有@BarCode的字段！");
                prefixField = prefixes.get(0);
                Assert.isTrue(prefixField.getGenericType().equals(String.class), "@BarCode标注在" + entityClass.getName() + "的字段" + prefixField.getName() + "必须为String类型");
                barCode = prefixField.getAnnotation(BarCode.class);
            }
            //2-初始化indexes
            indexes = ReflectUtil.getFieldsByAnnotation(entityClass, BarCodeIndex.class).stream()
                    .sorted(Comparator.comparingInt(f -> f.getAnnotation(BarCodeIndex.class).value()))
                    .collect(Collectors.toList());
            //3-初始化target
            List<Field> columns = ReflectUtil.getFieldsByAnnotation(entityClass, BarCodeColumn.class);
            Assert.isTrue(columns.size() == 1, "实体类" + entityClass.getName() + "必须有且只有一个拥有@BarCodeColumn的字段！");
            targetField = columns.get(0);
            Assert.isTrue(targetField.getGenericType().equals(String.class), "@BarCodeColumn标注的字段必须为String类型");
        }

        /**
         * 计算条形码
         * @param event
         * @return
         */
        public String val(AbstractPreDatabaseOperationEvent event){
            return prefix(event.getEntity()) + id(event.getId()) + indexes(event.getEntity());
        }

        /**
         * 计算前缀
         * @return
         */
        public BAR_CODE_PREFIX prefix(Object entity){
            if(prefixOnClass)
                return barCode.prefix()[0];
            Object value = Unchecked.biFunction(ReflectUtil::getValue).apply(prefixField, entity);
            Assert.isTrue(Objects.nonNull(value) && StringUtils.hasLength(value.toString()), "@BarCode标注的Field必须有值");
            return BAR_CODE_PREFIX.valueOf(value.toString());
        }

        /**
         * 计算id
         * @param id
         * @return
         */
        public String id(Serializable id){
            return barCode.excludeId() ? "" : wrapperAttribute(id, ID_LENGTH);
        }

        /**
         * 计算index
         * @param entity
         * @return
         */
        public String indexes(Object entity){
            StringBuilder ret = new StringBuilder();
            for (Field index : indexes){
                BarCodeIndex barCodeIndex = index.getAnnotation(BarCodeIndex.class);
                //2-1 获取属性的真实值
                Object value = Unchecked.biFunction(ReflectUtil::getValue).apply(index, entity);
                //2-2 构造出属性的计算值
                ret.append(Objects.isNull(value) || !StringUtils.hasLength(value.toString()) ?
                        (barCodeIndex.nullable() ? "" : EMPTY_REPLACE) : wrapperAttribute(value.toString(), barCodeIndex.holder() ? 1 : barCodeIndex.length())
                );
            }
            return ret.toString();
        }

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
    }
}
