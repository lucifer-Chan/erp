package com.yintong.erp.sandbox;


import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lucifer.chan
 * @create 2018-05-12 下午6:33
 * 测试用
 **/
@RestController
@RequestMapping("test")
public class TestController {
    /*@Autowired CategoryService categoryService;
    @Autowired DepartmentService departmentService;

    @GetMapping("category/{code}")
    public BaseResult getTree(@PathVariable  String code){
        return new BaseResult().addList("tree", categoryService.tree(code));
    }

    @GetMapping("category")
    public BaseResult getTree(){
        return new BaseResult().addList("tree", categoryService.tree());
    }

    @GetMapping("department/{id}")
    public BaseResult department(@PathVariable  Long id){
        return new BaseResult().addList("tree", departmentService.tree(id));
    }

    @GetMapping("department")
    public BaseResult department2(){
        return new BaseResult().addList("tree", departmentService.tree());
    }*/
}

