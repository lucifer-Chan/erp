package com.yintong.erp.service.basis;

import com.yintong.erp.domain.basis.ErpBaseModelTool;
import com.yintong.erp.domain.basis.ErpBaseModelToolRepository;
import com.yintong.erp.utils.query.OrderBy;
import com.yintong.erp.utils.query.ParameterItem;
import com.yintong.erp.utils.query.QueryParameterBuilder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import static com.yintong.erp.utils.query.ParameterItem.COMPARES.equal;
import static com.yintong.erp.utils.query.ParameterItem.COMPARES.like;
import static javax.persistence.criteria.Predicate.BooleanOperator.OR;

/**
 * Created by jianqiang on 2018/5/22 0022.
 * 模具
 */
@Service
public class MouldService {

    @Autowired
    private ErpBaseModelToolRepository modelToolRepositor;

    /**
     *查询列表
     * @param parameter
     * @return
     */
    public Page<ErpBaseModelTool> query(MouldParameterBuilder parameter){
        return modelToolRepositor.findAll(parameter.specification(), parameter.pageable());
    }

    /**
     * 构造前端返回的参数
     */
    @Getter
    @Setter
    @OrderBy(fieldName = "id")
    public static class MouldParameterBuilder extends QueryParameterBuilder {
        @ParameterItem(mappingTo = {"barCode", "modelToolName"}, compare = like, group = OR)
        String cause;
        @ParameterItem(mappingTo = "modelToolTypeCode", compare = equal)
        String type;
    }
}
