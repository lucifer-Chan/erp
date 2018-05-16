package com.yintong.erp.utils.base.query;

public enum QueryType {

    EQUAL("equal"),LE("le"),LT("lt"),GE("ge"),GT("gt"),LIKE("like");

    private String value;

    QueryType(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }


}
