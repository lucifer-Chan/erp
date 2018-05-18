package com.yintong.erp.utils.query;

import com.yintong.erp.utils.common.DateUtil;
import com.yintong.erp.utils.query.ParameterItem.TRANSFORMER;
import com.yintong.erp.utils.transform.ReflectUtil;
import lombok.Data;
import org.jooq.lambda.Unchecked;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.yintong.erp.utils.query.ParameterItem.COMPARES.*;

/**
 * @author lucifer.chan
 * @create 2018-05-14 下午3:05
 * 模糊查询参数组装
 **/
@Data
public class QueryParameterBuilder {
    protected int pageNum = 1;
    protected int perPageNum = 20;

    public int getPageNum(){
        return pageNum > 0 ? pageNum - 1 : pageNum;
    }

    /**
     * 构造查询参数
     * @param root
     * @param criteriaBuilder
     * @param <T>
     * @return
     */
    public <T> List<Predicate> build(Root<T> root, CriteriaBuilder criteriaBuilder){
        return ReflectUtil.getAllFields(this).stream()
                .filter(field -> field.isAnnotationPresent(ParameterItem.class))
                .map(Unchecked.function(field -> buildSingle(field, root, criteriaBuilder)))
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * 构造单个查询参数
     * @param field
     * @param root
     * @param criteriaBuilder
     * @param <T>
     * @return
     * @throws ParseException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    private <T> List<Predicate> buildSingle(Field field, Root<T> root, CriteriaBuilder criteriaBuilder) throws ParseException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {

        ParameterItem parameterItem = field.getAnnotation(ParameterItem.class);
        Object value = transValue(field, parameterItem.transformer());
        if(value == null) return null;
        Class clazz = Comparable.class;
        if(like.equals(parameterItem.compare())) {
            value = "%" + value + "%";
            clazz = String.class;
        } else if (equal.equals(parameterItem.compare())){
            clazz = Object.class;
        }
        String [] fieldNames = parameterItem.mappingTo().length == 0 ?
                new String[]{field.getName()} : parameterItem.mappingTo();
        Method method = criteriaBuilder.getClass().getMethod(parameterItem.compare().name(), Expression.class, clazz);
        final Object _value = value;
        return Stream.of(fieldNames)
                .map(Unchecked.function(fieldName -> (Predicate)method.invoke(criteriaBuilder, root.get(fieldName), _value)))
                .collect(Collectors.toList());
    }

    /**
     * 转换查询参数的值
     * @param field
     * @param transformer
     * @return
     * @throws IllegalAccessException
     * @throws ParseException
     */
    private Object transValue(Field field, TRANSFORMER transformer) throws IllegalAccessException, ParseException {
        field.setAccessible(true);
        Object value = field.get(this);
        if(Objects.isNull(value))
            return null;
        if("".equals(value.toString().trim()))
            return null;
        switch(transformer) {
            case NULL:
                return value;
            case str2Time:
                return DateUtil.parseDateTime((String) value);
            case str2Date:
                return DateUtil.parseDate((String) value);
            case strTime2Second:
                return DateUtil.parseDateTime((String) value).getTime()/1000;
            case strDate2Second:
                return DateUtil.parseDate((String) value).getTime()/1000;
            case strTime2Millis:
                return DateUtil.parseDateTime((String) value).getTime();
            case strDate2Millis:
                return DateUtil.parseDate((String) value).getTime();
            case str2Int:
                return Integer.valueOf((String) value);
            default:
                return value;
        }
    }
}
