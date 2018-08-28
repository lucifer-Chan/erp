package com.yintong.erp.web;

import com.yintong.erp.utils.base.BaseResult;
import com.yintong.erp.utils.common.Constants;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lucifer.chan
 * @create 2018-05-05 下午11:43
 * 开放性的接口-非登陆用户可访问
 **/
@RestController
@RequestMapping("open")
public class OpenController {
    @GetMapping("test")
    public BaseResult test(){
        return new BaseResult();
    }


    @GetMapping("test/enum/{type}")
    public BaseResult testE(@PathVariable Constants.StockPlaceType type){
        return new BaseResult().put("enum", type);
    }

    @GetMapping("test/long")
    public BaseResult testLong(Long id){
        return new BaseResult().put("long", null == id ? "null!!" :  id);
    }
}
