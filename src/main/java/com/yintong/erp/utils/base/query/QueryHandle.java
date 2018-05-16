package com.yintong.erp.utils.base.query;

import lombok.Data;
import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import javax.persistence.criteria.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * 处理查询DTO
 */
@Component
public class QueryHandle {

    Map<String,List<SpecificationItem>> queryDTOMap = null;

    @Autowired
    List<BaseQueryDTO> baseQueryDTOList;

    @PostConstruct
    public void init(){
        //在spring 容器启动之后，加载所有查询DTO，解析这些DTO，把解析结果放在map中。
        queryDTOMap = new HashMap<>();
        if(baseQueryDTOList!=null){
            baseQueryDTOList.stream().forEach(item->
                    queryDTOMap.put(item.getClass().getSimpleName(),handleQueryDTO(item.getClass()))
            );
        }
    }

    public List<SpecificationItem> handleQueryDTO(Class queryDTO){
        List<SpecificationItem> itemList = new ArrayList<>();
        createQueryDTO(queryDTO,itemList);
        return itemList;
    }

    public void createQueryDTO(Class queryDTO,List<SpecificationItem> itemList){
        if(queryDTO.getSuperclass()!=null){
            createQueryDTO(queryDTO.getSuperclass(),itemList);
        }
        Field[] fields = queryDTO.getDeclaredFields();
        for(Field field:fields){
            if(!isExist(field,itemList)){
                itemList.add(createQueryDTO(field));
            }
        }
    }

    private SpecificationItem createQueryDTO(Field field){
        SpecificationItem criteriaQueryDTO = new SpecificationItem();
        criteriaQueryDTO.setFieldName(field.getName());
        CriteriaAnnotation criteriaQuery = field.getAnnotation(CriteriaAnnotation.class);
        //如果这个字段有注解，根据注解解析
        if(criteriaQuery!=null&&!StringUtils.isEmpty(criteriaQuery.columnName())){
            criteriaQueryDTO.setQueryField(criteriaQuery.columnName());
        }else{
            //如果注解，如果这个字段以Start或者End结尾，那证明这个字段是时间字段。表明要查询的是时间段
            if(field.getType()==Long.class&&field.getName().endsWith("Start")){
                criteriaQueryDTO.setQueryField(field.getName().substring(0,field.getName().length()-"Start".length()));
            }else if(field.getType()==Long.class&&field.getName().endsWith("End")){
                criteriaQueryDTO.setQueryField(field.getName().substring(0,field.getName().length()-"End".length()));
            }else{
                criteriaQueryDTO.setQueryField(field.getName());
            }
        }
        if(criteriaQuery!=null&&!StringUtils.isEmpty(criteriaQuery.opType())){
            //设置查询类型，大于，等于等等
            criteriaQueryDTO.setOperType(criteriaQuery.opType());
        }else{
            //时间范围设置
            if(field.getType()==Long.class&&field.getName().endsWith("Start")){
                criteriaQueryDTO.setOperType(QueryType.GT);
            }else if(field.getType()==Long.class&&field.getName().endsWith("End")){
                criteriaQueryDTO.setOperType(QueryType.LE);
            }else{
                //默认是等于
                criteriaQueryDTO.setOperType(QueryType.EQUAL);
            }
        }
        return criteriaQueryDTO;
    }


    private boolean isExist(Field field,List<SpecificationItem> itemList){
        for(SpecificationItem specificationItem:itemList){
            if(specificationItem.getFieldName().equals(field.getName())){
                return true;
            }
        }
        return false;
    }

    public Predicate handle(Root root, CriteriaQuery<?> query, CriteriaBuilder cb, Object queryDTO){
        List<SpecificationItem> specificationItems =  queryDTOMap.get(queryDTO.getClass().getSimpleName());
        return specificationItems.stream().map(item -> {
            try {
                return getPredicate(cb, root, item, queryDTO);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
            return null;
        }).filter(p -> p!=null)
                .reduce(cb::and).get();
    }

    public Predicate getPredicate(CriteriaBuilder cb, Root root, SpecificationItem item, Object queryDTO) throws IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        Object value = PropertyUtils.getProperty(queryDTO,item.getFieldName());
        Expression expression = root.get(item.getQueryField()).as(item.getFieldType());
        if(QueryType.LIKE.equals(item.getOperType())){
            return cb.like(expression,"%"+value+"%");
        }else if(QueryType.EQUAL.equals(item.getOperType())){
            return cb.equal(expression,value);
        }else if(QueryType.LE.equals(item.getOperType())){
            return cb.le(expression,(Number)value);
        }else if(QueryType.LT.equals(item.getOperType())){
            return cb.lt(expression,(Number)value);
        }else if(QueryType.GE.equals(item.getOperType())){
            return cb.ge(expression,(Number)value);
        }else if(QueryType.GT.equals(item.getOperType())){
            return cb.gt(expression,(Number)value);
        }
        return null;
    }

    @Data
    class SpecificationItem{

        private String fieldName;//查询DTO中的属性名称

        private String queryField;//实体类中的属性名称

        private Class fieldType;//查询DTO中属性的类型

        private QueryType operType;//连接类型，大于等于。。。

    }
}
