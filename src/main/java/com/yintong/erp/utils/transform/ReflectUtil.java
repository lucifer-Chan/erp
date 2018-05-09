package com.yintong.erp.utils.transform;

import net.sf.json.JSONObject;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Stream;
import static java.util.stream.Collectors.*;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午3:05
 * 反射工具
 **/
public class ReflectUtil {
    static final Class<TransformerSourceProperty> annotationClass = TransformerSourceProperty.class;

    static final Class<TransformerIgnoreProperty> ignoreAnnotationClass = TransformerIgnoreProperty.class;

    static final Map<String, String> jsonMethodMap = new HashMap<String, String>(){{
        put("String", "getString");
        put("int", "getInt");
        put("Integer", "getInt");
        put("double", "getDouble");
        put("Double", "getDouble");
        put("float", "getFloat");
        put("Float", "getFloat");
        put("long", "getLong");
        put("Long", "getLong");
        put("Date", "get");
        put("Timestamp", "get");
        // TODO ..~
    }};
    
    @SuppressWarnings("unchecked")
    public static <T> T getValueFromJson(JSONObject json, Class<?> clazz, String arg){
        try {
            return (T) getStringKeyMethod(json, jsonMethodMap.get(clazz.getSimpleName())).invoke(json, arg);
        } catch(Exception e){
            return (T)json.get(arg);
        }

    }


    /**
     * 只管参数类型为String的method
     * map 或ResultSet中的取值方法 like getString("") or getInt("")
     * @param o
     * @param methodName
     * @return
     */
    public static Method getStringKeyMethod(Object o, String methodName) {
        Class<?> clazz = o.getClass();
        for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
            for(Method method : clazz.getMethods()){
                if(method.getName().equals(methodName) &&
                        method.getParameterCount() == 1
                        && method.getParameterTypes()[0].getSimpleName().equals("String"))
                    return method;
            }
        }
        return null;
    }

    /**
     * 获取一个对象的自身方法和所有父类的方法，直到Object
     * @param o
     * @return
     */
    public static List<Method> getAllDeclaredMethods(Object o){
        return getClassesUntilRoot(o).stream()
                .map(Class::getDeclaredMethods)
                .flatMap(Stream::of)
                .collect(toList());
    }


    /**
     * 获取一个对象的自身类和所有父类，直到Object
     * @param o
     * @return
     */
    public static List<Class<?>> getClassesUntilRoot(Object o){
        List<Class<?>> ret = new ArrayList<>();
        if(Objects.isNull(o))
            return ret;
        Class<?> clazz = o.getClass();
        while(clazz != Object.class){
            ret.add(clazz);
            clazz = clazz.getSuperclass();
        }
        return ret;
    }

    /**
     * 获取一个对象的所有Field，包括父类
     * @param o
     * @return
     */
    public static List<Field> getAllFields(Object o){
        return getClassesUntilRoot(o).stream()
                .map(Class::getDeclaredFields)
                .flatMap(Stream::of)
                .filter(field -> isEditable(field) && !isIgnored(field))
                .collect(toList());
    }

    /**
     * 获取一个对象的所有Field，包括父类(但不排除加了TransformerIgnoreProperty注解的)
     * @param o
     * @return
     */
    public static List<Field> getAllFieldsWithoutIgnored(Object o){
        return getClassesUntilRoot(o).stream()
                .map(Class::getDeclaredFields)
                .flatMap(Stream::of)
                .filter(ReflectUtil::isEditable)
                .collect(toList());
    }

    /**
     * 调用一个field的getter方法
     * @param field 属性
     * @param holder 持有属性的对象
     * @return
     */
    public static Object getValueByGetter(Field field, Object holder){
        try {
            String fieldName = field.getName();
            Method method = holder.getClass().getMethod("get" + fieldName.substring(0,1).toUpperCase() + fieldName.substring(1,fieldName.length()));
            method.setAccessible(true);
            return method.invoke(holder);
        } catch(Exception e){
            return null;
        }

    }

    public static void main(String[] args) {
        String name = "createdAt";
        System.out.println(name.substring(0,1).toUpperCase() + name.substring(1,name.length()));
    }

    /**
     * 通过fieldName返回field，考虑TransformerSourceProperty
     * @param o
     * @param fieldName
     * @return
     */
    public static Field getFieldByFieldName(Object o, String fieldName){
        List<Field> fields = getAllFields(o);
        //1、名称一致，直接返回
        for (Field field : fields){
            if (field.getName().equals(fieldName))
                return field;
        }
        //2、有@TransformerSourceProperty注解的情况
        for (Field field : fields){
            if (isFieldAnnotationMatchedSourceFieldName(field, fieldName))
                return field;
        }
        return null;
    }

    /**
     * 获取一个类上拥有annotationClass的所有Field
     * @param o
     * @param annotationClass
     * @param <T>
     * @return
     */
    public static <T extends Annotation> List<Field> getFieldsByAnnotation(Object o, Class<T> annotationClass){
        return getAllFields(o).stream()
                .filter(field -> field.isAnnotationPresent(annotationClass))
                .collect(toList());
    }

    /**
     * value和sourceFieldName和Field上的注解的values、value是否匹配
     * @param field
     * @param sourceFieldName
     * @return
     */
    static boolean isFieldAnnotationMatchedSourceFieldName(Field field, String sourceFieldName){
        for(String value : getTransformerSourcePropertyValues(field)){
            if(value.toUpperCase().equals(sourceFieldName.toUpperCase()))
                return true;
        }
        return false;
    }

    static String [] getTransformerSourcePropertyValues(Field field){
        if(field.isAnnotationPresent(annotationClass)){
            TransformerSourceProperty property = field.getAnnotation(annotationClass);
            String [] values = property.values();
            values = Arrays.copyOf(values, values.length + 1);
            values[values.length - 1] = property.value();
            return values;
        }
        return new String []{};
    }

    /**
     * 判断field是否可编辑
     * static和final认为不可编辑
     * @param f
     * @return
     */
    public static boolean isEditable(Field f){
        return !Modifier.isStatic(f.getModifiers()) && !Modifier.isFinal(f.getModifiers());
    }

    /**
     * 判断field是否有TransformerIgnoreProperty标签
     * @param f
     * @return
     */
    public static boolean isIgnored(Field f){
        return f.isAnnotationPresent(ignoreAnnotationClass);
    }

    /**
     * 设值
     * @param f
     * @param obj
     * @param value
     * @throws IllegalAccessException
     */
    public static void setValue(Field f, Object obj, Object value) throws IllegalAccessException {
        f.setAccessible(true);
        f.set(obj, value);
    }

    public static Object getValue(Field f, Object obj) throws IllegalAccessException {
        f.setAccessible(true);
        return f.get(obj);
    }
}
