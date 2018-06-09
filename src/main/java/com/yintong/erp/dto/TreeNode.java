package com.yintong.erp.dto;

import lombok.*;

/**
 * @author lucifer.chan
 * @create 2018-06-04 上午1:13
 * 树节点
 **/
@Getter @NoArgsConstructor
public class TreeNode {

    private String code;
    private String name;
    private String fullName;
    private String title;
    private String parentCode;
    private Boolean isParent;
    private boolean drag;//可拖拽
    private Object source;

    @Builder
    public TreeNode(String code, String name, String parentCode, Boolean isParent){
        this.code = code;
        this.title = name;
        this.fullName = name;
        this.name = subString(name);
        this.parentCode = parentCode;
        this.isParent = isParent;
        this.drag = !this.isParent;
    }

    public TreeNode setSource(Object source){
        this.source = source;
        return this;
    }

    public TreeNode setName(String name){
        this.name = subString(name);
        this.title = name;
        return this;
    }

    public TreeNode setFullName(String fullName){
        this.fullName = fullName;
        return this;
    }

    private String subString(String string){
        return string.length() > 50 ? string.substring(0, 48) + "..." : string;
    }
}
