package com.yintong.erp.service.basis;

import com.yintong.erp.domain.basis.associator.ErpEmployeeDepartment;
import com.yintong.erp.domain.basis.associator.ErpEmployeeDepartmentRepository;
import com.yintong.erp.domain.basis.security.*;
import com.yintong.erp.utils.common.SessionUtil;
import com.yintong.erp.utils.query.ParameterItem;
import com.yintong.erp.utils.query.QueryParameterBuilder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.util.List;
import java.util.Objects;

import static com.yintong.erp.utils.query.ParameterItem.COMPARES.*;
import static java.util.stream.Collectors.*;

/**
 * @author lucifer.chan
 * @create 2018-05-18 上午1:00
 * 员工管理
 **/
@Service
public class EmployeeService {

    @Autowired ErpEmployeeRepository employeeRepository;

    @Autowired ErpEmployeeDepartmentRepository employeeDepartmentRepository;

    @Autowired ErpEmployeeMenuRepository employeeMenuRepository;

    /**
     * 动态查询
     * @param parameter
     * @return
     */
    public Page<ErpEmployee> query(EmployeeParameterBuilder parameter){
        PageRequest pageRequest = PageRequest.of(parameter.getPageNum(), parameter.getPerPageNum());
        if(StringUtils.isEmpty(parameter.cause) && StringUtils.isEmpty(parameter.departmentId))
            return employeeRepository.findAll(pageRequest);

        return StringUtils.hasLength(parameter.cause) ?
                employeeRepository.findAll((root, criteriaQuery, criteriaBuilder) -> {
                    List<Predicate> predicates = parameter.build(root, criteriaBuilder);
                    System.out.println(root);
                    criteriaQuery.orderBy(criteriaBuilder.desc(root.get("createdAt")));
                    return criteriaBuilder.or(predicates.toArray(new Predicate[predicates.size()]));
                }, pageRequest) :
                employeeRepository.findByDepartmentId(parameter.getDepartmentId() + "%", pageRequest) ;
    }
    /**
     * 创建用户基本信息-包括密码
     * @param employee
     * @return
     */
    public ErpEmployee create(ErpEmployee employee) {
        employee.setId(null);
        validateEmployee(employee);
        return employeeRepository.save(employee);
    }

    /**
     * 更新用户基本信息-不包括密码
     * @param employee
     * @return
     */
    @Transactional
    public ErpEmployee update(ErpEmployee employee){
        Assert.notNull(employee.getId(), "用户id不能为空");
        ErpEmployee inDb = employeeRepository.findById(employee.getId()).orElse(null);
        Assert.notNull(inDb, "未找到id为" + employee.getId() + "的用户");
        validateEmployee(employee);
        inDb.setMobile(employee.getMobile());
        inDb.setLoginName(employee.getLoginName());
        inDb.setName(employee.getName());
        return employeeRepository.save(inDb);
    }

    /**
     * 更新别人的密码
     * @param employeeId
     * @param password
     * @return
     */
    public ErpEmployee updatePassword(Long employeeId, String password){
        Assert.isTrue(StringUtils.hasLength(password), "密码不能为空");
        ErpEmployee employee = employeeRepository.findById(employeeId).orElse(null);
        Assert.notNull(employee, "未找到id为" + employeeId + "的用户");
        employee.setPassword(new BCryptPasswordEncoder().encode(password));
        return employeeRepository.save(employee);
    }

    /**
     * 更新自己的手机号码
     * @param mobile
     * @return
     */
    public ErpEmployee updateMobile(String mobile) {
        ErpEmployee employee = SessionUtil.getCurrentUser();
        employee.setMobile(mobile);
        validateEmployee(employee);
        return employeeRepository.save(employee);
    }

    /**
     * 根据id删除用户
     * @param employeeId
     */
    @Transactional
    public String delete(Long employeeId){
        ErpEmployee employee = employeeRepository.findById(employeeId).orElse(null);
        Assert.notNull(employee, "未找到id为" + employeeId + "的用户");
        String name = employee.getName();
        //TODO 销售单、制令单等有关联业务存在的情况下，不能删除
        employeeRepository.delete(employee);
        employeeMenuRepository.deleteByEmployeeId(employeeId);
        employeeDepartmentRepository.deleteByEmployeeId(employeeId);
        return name;
    }

    /**
     * 更新自己的密码
     * @param old
     * @param newed
     * @return
     */
    public ErpEmployee updatePassword(String old, String newed){
        ErpEmployee employee = SessionUtil.getCurrentUser();
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        Assert.isTrue(encoder.matches(old, employee.getPassword()),"旧密码输入错误");
        employee.setPassword(encoder.encode(newed));
        return employeeRepository.save(employee);
    }


    /**
     * 保存前的验证
     * @param employee
     */
    private void validateEmployee(ErpEmployee employee){
        String loginName = employee.getLoginName();
        String mobile = employee.getMobile();
        if(StringUtils.hasLength(loginName)) {
            List<ErpEmployee> employees = Objects.isNull(employee.getId()) ?
                    employeeRepository.findByLoginName(loginName) :
                    employeeRepository.findByLoginNameAndIdIsNot(loginName, employee.getId());
            Assert.isTrue(CollectionUtils.isEmpty(employees), "登录名" + loginName + "已被使用");
        }

        if(StringUtils.hasLength(mobile)){
            List<ErpEmployee> employees = Objects.isNull(employee.getId()) ?
                    employeeRepository.findByMobile(mobile) :
                    employeeRepository.findByMobileAndIdIsNot(mobile, employee.getId());
            Assert.isTrue(CollectionUtils.isEmpty(employees), "手机号" + mobile + "已被使用");
        }
    }

    /**
     * 保存用户的部门
     * @param employeeId
     * @param departmentIds
     * @return
     */
    @Transactional
    public ErpEmployee saveDepartments(Long employeeId, List<Long> departmentIds) {
        ErpEmployee employee = employeeRepository.findById(employeeId).orElse(null);
        Assert.notNull(employee, "未找到id为" + employeeId + "的用户");
        employeeDepartmentRepository.deleteByEmployeeId(employeeId);
        if(!CollectionUtils.isEmpty(departmentIds)){
            employeeDepartmentRepository.saveAll(
                    departmentIds.stream()
                            .map(departmentId-> ErpEmployeeDepartment.builder().departmentId(departmentId).employeeId(employeeId).build())
                            .collect(toList())
            );
        }
        return employee;
    }

    /**
     * 保存用户的权限
     * @param employeeId
     * @param menuCodes
     * @return
     */
    @Transactional
    public ErpEmployee saveMenus(Long employeeId, List<String> menuCodes) {
        ErpEmployee employee = employeeRepository.findById(employeeId).orElse(null);
        Assert.notNull(employee, "未找到id为" + employeeId + "的用户");
        employeeMenuRepository.deleteByEmployeeId(employeeId);
        if(!CollectionUtils.isEmpty(menuCodes)){
            employeeMenuRepository.saveAll(
                    menuCodes.stream()
                            .map(menuCode-> ErpEmployeeMenu.builder().menuCode(menuCode).employeeId(employeeId).build())
                            .collect(toList())
            );
        }
        return employee;
    }

    @Getter @Setter
    public static class EmployeeParameterBuilder extends QueryParameterBuilder {
        @ParameterItem(mappingTo = {"name", "mobile", "loginName"}, compare = like)
        String cause;
        String departmentId;

        public String getDepartmentId(){
            return StringUtils.hasLength(departmentId) ? departmentId : "";
        }

    }
}
