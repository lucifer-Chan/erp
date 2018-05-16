package com.yintong.erp.service.basis;

import com.yintong.erp.domain.BaseRepository;
import com.yintong.erp.domain.basis.ErpBaseDepartment;
import com.yintong.erp.domain.basis.ErpBaseDepartmentRepository;
import com.yintong.erp.domain.basis.associator.ErpEmployeeDepartment;
import com.yintong.erp.domain.basis.associator.ErpEmployeeDepartmentRepository;
import com.yintong.erp.domain.basis.security.*;
import com.yintong.erp.dto.basis.ErpEmployeeDTO;
import com.yintong.erp.service.BaseServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ErpEmployeeService extends BaseServiceImpl{

    @Autowired
    private ErpEmployeeRepository erpEmployeeRepository;

    @Autowired
    private ErpEmployeeMenuRepository erpEmployeeMenuRepository;

    @Autowired
    private ErpEmployeeDepartmentRepository erpEmployeeDepartmentRepository;

    @Autowired
    private ErpMenuRepository erpMenuRepository;

    @Autowired
    ErpBaseDepartmentRepository erpBaseDepartmentRepository;

    @Override
    public BaseRepository<ErpEmployee> getRepository() {
        return erpEmployeeRepository;
    }

    @Transactional
    public void save(ErpEmployeeDTO erpEmployeeDTO){
        ErpEmployee erpEmployee = new ErpEmployee();
        BeanUtils.copyProperties(erpEmployeeDTO,erpEmployee);
        super.save(erpEmployee);
        erpEmployeeMenuRepository.deleteByEmployeeId(erpEmployee.getId());
        List<ErpEmployeeMenu> employeeMenuList = erpEmployeeDTO.getMenuCodes().stream()
                .map(menuCode-> {
                    ErpEmployeeMenu erpEmployeeMenu = new ErpEmployeeMenu();
                    erpEmployeeMenu.setEmployeeId(erpEmployee.getId());
                    erpEmployeeMenu.setMenuCode(menuCode);
                    return erpEmployeeMenu;
                }).collect(Collectors.toList());
        erpEmployeeMenuRepository.saveAll(employeeMenuList);
        erpEmployeeDepartmentRepository.deleteByEmployeeId(erpEmployeeDTO.getId());
        List<ErpEmployeeDepartment> erpEmployeeDepartments = erpEmployeeDTO.getDepartmentIds().stream()
                .map(branchId->{
                    ErpEmployeeDepartment erpEmployeeDepartment = new ErpEmployeeDepartment();
                    erpEmployeeDepartment.setEmployeeId(erpEmployee.getId());
                    erpEmployeeDepartment.setDepartmentId(branchId);
                    return erpEmployeeDepartment;
                }).collect(Collectors.toList());
        erpEmployeeDepartmentRepository.saveAll(erpEmployeeDepartments);
    }

    public void updatePassword(Long employeeId,String password){
        Optional<ErpEmployee> erpEmployee = erpEmployeeRepository.findById(employeeId);
        if(erpEmployee.isPresent()){
            erpEmployee.get().setPassword(new BCryptPasswordEncoder().encode(password.trim()));
            erpEmployeeRepository.save(erpEmployee.get());
        }
    }

    public ErpEmployeeDTO findById(Long employeeId){
        Optional<ErpEmployee> erpEmployee = erpEmployeeRepository.findById(employeeId);
        if(erpEmployee.isPresent()){
            return this.convert(erpEmployee.get());
        }else{
            return null;
        }
    }

    public List<ErpBaseDepartment> allDepartments(){
        return erpBaseDepartmentRepository.findAll();
    }

    public List<ErpEmployeeDTO> findAll(){
        List<ErpEmployee> erpEmployees = erpEmployeeRepository.findAll();
        return erpEmployees.stream().map(this::convert).collect(Collectors.toList());
    }

    private ErpEmployeeDTO convert(ErpEmployee erpEmployee){
        if(erpEmployee==null){
            return null;
        }
        ErpEmployeeDTO resultDTO = new ErpEmployeeDTO();
        resultDTO.setLoginName(erpEmployee.getLoginName());
        resultDTO.setName(erpEmployee.getName());
        resultDTO.setMobile(erpEmployee.getMobile());
        resultDTO.setId(erpEmployee.getId());
        List<ErpEmployeeMenu> erpEmployeeMenus = erpEmployeeMenuRepository.findByEmployeeId(erpEmployee.getId());
        List<String> menuCodes = erpEmployeeMenus.stream().map(ErpEmployeeMenu::getMenuCode).collect(Collectors.toList());
        List<String> menuNames = erpEmployeeMenus.stream().map(ErpEmployeeMenu::getMenuCode)
                                .map(erpMenuRepository::findById)
                                .filter(erpMenu -> erpMenu.isPresent())
                                .map(Optional::get)
                                .map(ErpMenu::getName)
                                .collect(Collectors.toList());
        List<ErpEmployeeDepartment> erpEmployeeDepartments = erpEmployeeDepartmentRepository.findByEmployeeId(erpEmployee.getId());
        List<Long> departmentIds = erpEmployeeDepartments.stream().map(ErpEmployeeDepartment::getDepartmentId).collect(Collectors.toList());
        List<String> departmentNames = erpEmployeeDepartments.stream().map(ErpEmployeeDepartment::getDepartmentId)
                                    .map(erpBaseDepartmentRepository::findById)
                                    .filter(erpBaseDepartment -> erpBaseDepartment.isPresent())
                                    .map(Optional::get)
                                    .map(ErpBaseDepartment::getName)
                                    .collect(Collectors.toList());
        resultDTO.setDepartmentNames(departmentNames);
        resultDTO.setMenuNames(menuNames);
        resultDTO.setDepartmentIds(departmentIds);
        resultDTO.setMenuCodes(menuCodes);
        return resultDTO;
    }

}
