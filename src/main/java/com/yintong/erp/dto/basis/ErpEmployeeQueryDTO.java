package com.yintong.erp.dto.basis;

import com.yintong.erp.utils.base.query.BaseQueryDTO;
import lombok.Data;
import org.springframework.stereotype.Component;


@Data
@Component
public class ErpEmployeeQueryDTO implements BaseQueryDTO {

    private String loginName;

    private Long branchId;

    private String menuCode;

}
